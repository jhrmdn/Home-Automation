package mdn.jh.automation.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** File-backed web users. Passwords are stored only as salted PBKDF2 hashes. */
public class WebUserStore {
	public static final int DEFAULT_MINIMUM_PASSWORD_LENGTH = 10;
	private static final int ITERATIONS = 210_000;
	private static final int SALT_BYTES = 16;
	private static final int HASH_BITS = 256;
	private static final String USER_PREFIX = "user.";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final Path file;
	private final int minimumPasswordLength;
	private final Properties properties = new Properties();

	public WebUserStore(Path file) throws IOException {
		this(file, true);
	}

	public WebUserStore(Path file, boolean minimumPasswordLengthEnabled) throws IOException {
		this.file = file.toAbsolutePath().normalize();
		this.minimumPasswordLength = minimumPasswordLengthEnabled ? DEFAULT_MINIMUM_PASSWORD_LENGTH : 0;
		if (Files.exists(this.file)) {
			try (InputStream input = Files.newInputStream(this.file)) {
				properties.load(input);
			}
		}
	}

	public synchronized boolean hasUsers() {
		return properties.stringPropertyNames().stream().anyMatch(k -> k.startsWith(USER_PREFIX) && k.endsWith(".hash"));
	}

	public synchronized boolean isAnonymousViewEnabled() {
		return Boolean.parseBoolean(properties.getProperty("anonymousView", "false"));
	}

	public synchronized void setAnonymousViewEnabled(boolean enabled) throws IOException {
		properties.setProperty("anonymousView", Boolean.toString(enabled));
		save();
	}

	public synchronized boolean isAnonymousDashboardActionsEnabled() {
		return Boolean.parseBoolean(properties.getProperty("anonymousDashboardActions", "false"));
	}

	public synchronized void setAnonymousDashboardActionsEnabled(boolean enabled) throws IOException {
		properties.setProperty("anonymousDashboardActions", Boolean.toString(enabled));
		save();
	}

	public synchronized void initializeAdmin(String username, String password) throws IOException {
		if (hasUsers()) throw new IllegalStateException("The administrator has already been initialized");
		putUser(username, password, true, true, true, true);
	}

	public synchronized void putUser(String username, String password, boolean read, boolean write, boolean admin)
			throws IOException {
		putUser(username, password, read, write, write, admin);
	}

	public synchronized void putUser(String username, String password, boolean read, boolean action, boolean write, boolean admin)
			throws IOException {
		validateUsername(username);
		String prefix = prefix(username);
		boolean exists = properties.containsKey(prefix + "hash");
		User existingUser = readUser(username);
		if (existingUser != null && existingUser.isAdmin() && !admin
				&& listUsers().stream().filter(User::isAdmin).count() <= 1) {
			throw new IllegalArgumentException("The last administrator cannot lose administrator access");
		}
		if (!exists || (password != null && !password.isEmpty())) {
			validatePassword(password);
			byte[] salt = new byte[SALT_BYTES];
			RANDOM.nextBytes(salt);
			properties.setProperty(prefix + "salt", Base64.getEncoder().encodeToString(salt));
			properties.setProperty(prefix + "hash", Base64.getEncoder().encodeToString(hash(password, salt, ITERATIONS)));
			properties.setProperty(prefix + "iterations", Integer.toString(ITERATIONS));
		}
		boolean effectiveWrite = write || admin;
		boolean effectiveAction = action || effectiveWrite;
		properties.setProperty(prefix + "read", Boolean.toString(read || effectiveAction));
		properties.setProperty(prefix + "action", Boolean.toString(effectiveAction));
		properties.setProperty(prefix + "write", Boolean.toString(effectiveWrite));
		properties.setProperty(prefix + "admin", Boolean.toString(admin));
		save();
	}

	public synchronized User authenticate(String username, String password) {
		if (username == null || password == null) return null;
		String prefix;
		try {
			validateUsername(username);
			prefix = prefix(username);
		} catch (IllegalArgumentException e) {
			return null;
		}
		String storedHash = properties.getProperty(prefix + "hash");
		String storedSalt = properties.getProperty(prefix + "salt");
		if (storedHash == null || storedSalt == null) return null;
		try {
			int iterations = Integer.parseInt(properties.getProperty(prefix + "iterations", Integer.toString(ITERATIONS)));
			byte[] actual = hash(password, Base64.getDecoder().decode(storedSalt), iterations);
			if (!MessageDigest.isEqual(Base64.getDecoder().decode(storedHash), actual)) return null;
			return readUser(username);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public synchronized List<User> listUsers() {
		List<User> users = new ArrayList<>();
		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(USER_PREFIX) && key.endsWith(".hash")) {
				String encoded = key.substring(USER_PREFIX.length(), key.length() - ".hash".length());
				try { users.add(readUser(decodeUsername(encoded))); } catch (IllegalArgumentException ignored) { }
			}
		}
		users.sort(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
		return users;
	}

	public synchronized User getUser(String username) {
		if (username == null) return null;
		try { validateUsername(username); } catch (IllegalArgumentException e) { return null; }
		return readUser(username);
	}

	public synchronized void deleteUser(String username) throws IOException {
		validateUsername(username);
		User existing = readUser(username);
		if (existing == null) return;
		if (existing.isAdmin()) throw new IllegalArgumentException("Administrator accounts cannot be deleted");
		String prefix = prefix(username);
		properties.stringPropertyNames().stream().filter(k -> k.startsWith(prefix)).toList().forEach(properties::remove);
		save();
	}

	private User readUser(String username) {
		String prefix = prefix(username);
		if (!properties.containsKey(prefix + "hash")) return null;
		return new User(username, Boolean.parseBoolean(properties.getProperty(prefix + "read", "false")),
				Boolean.parseBoolean(properties.getProperty(prefix + "action", properties.getProperty(prefix + "write", "false"))),
				Boolean.parseBoolean(properties.getProperty(prefix + "write", "false")),
				Boolean.parseBoolean(properties.getProperty(prefix + "admin", "false")));
	}

	private static void validateUsername(String username) {
		if (username == null || !username.matches("[A-Za-z0-9._-]{1,64}")) {
			throw new IllegalArgumentException("Username must contain 1-64 letters, numbers, dots, underscores or hyphens");
		}
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < minimumPasswordLength) throw new IllegalArgumentException("Password must contain at least " + minimumPasswordLength + " characters");
		if (password.length() > 1024) throw new IllegalArgumentException("Password is too long");
	}

	public int getMinimumPasswordLength() { return minimumPasswordLength; }

	private static String prefix(String username) {
		return USER_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(username.getBytes(StandardCharsets.UTF_8)) + ".";
	}

	private static String decodeUsername(String encoded) {
		return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
	}

	private static byte[] hash(String password, byte[] salt, int iterations) {
		PBEKeySpec specification = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("PBKDF2 password hashing is not available", e);
		} finally {
			specification.clearPassword();
		}
	}

	private void save() throws IOException {
		Path parent = file.getParent();
		if (parent != null) Files.createDirectories(parent);
		Path temporary = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
		try {
			try (OutputStream output = Files.newOutputStream(temporary)) {
				properties.store(output, "Home Automation web users - do not edit while the application is running");
			}
			setOwnerOnlyPermissions(temporary);
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
			setOwnerOnlyPermissions(file);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static void setOwnerOnlyPermissions(Path path) {
		try {
			Set<PosixFilePermission> permissions = new HashSet<>();
			permissions.add(PosixFilePermission.OWNER_READ);
			permissions.add(PosixFilePermission.OWNER_WRITE);
			Files.setPosixFilePermissions(path, permissions);
		} catch (UnsupportedOperationException | IOException ignored) { }
	}

	public static final class User {
		private final String username;
		private final boolean read;
		private final boolean action;
		private final boolean write;
		private final boolean admin;

		public User(String username, boolean read, boolean action, boolean write, boolean admin) {
			this.username = username;
			this.read = read;
			this.action = action || write || admin;
			this.write = write;
			this.admin = admin;
		}

		public String getUsername() { return username; }
		public boolean canRead() { return read; }
		public boolean canAct() { return action; }
		public boolean canWrite() { return write; }
		public boolean isAdmin() { return admin; }
	}
}
