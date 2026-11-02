package mdn.jh.automation.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import mdn.jh.automation.Main;
import mdn.jh.automation.SmartHomeHandler;

/** Owns the isolated production/development configuration files and active mode. */
public final class ConfigurationManager {
	public enum Mode { PRODUCTION, DEVELOPMENT }
	private static final Path PRODUCTION = Path.of("settings.xml").toAbsolutePath().normalize();
	private static final Path DIRECTORY = Path.of("configurations").toAbsolutePath().normalize();
	private static final long MAX_BYTES = 5L * 1024 * 1024;
	private static volatile Mode mode = Mode.PRODUCTION;
	private static volatile Path activeFile = PRODUCTION;

	private ConfigurationManager() { }
	public static Mode getMode() { return mode; }
	public static boolean isDevelopment() { return mode == Mode.DEVELOPMENT; }
	public static String getActiveFileName() { return activeFile.getFileName().toString(); }
	public static Path getActiveFile() { return activeFile; }

	public static synchronized List<String> listDevelopmentFiles() throws IOException {
		Files.createDirectories(DIRECTORY);
		List<String> result = new ArrayList<>();
		try (var files = Files.list(DIRECTORY)) { files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".xml"))
				.map(path -> path.getFileName().toString()).sorted(String.CASE_INSENSITIVE_ORDER).forEach(result::add); }
		return result;
	}

	public static synchronized Path developmentFile(String name) throws IOException {
		if (name == null || !name.matches("[A-Za-z0-9._-]{1,120}\\.xml")) throw new IllegalArgumentException("Development file name must end in .xml and use letters, numbers, dots, underscores or hyphens");
		Files.createDirectories(DIRECTORY);
		Path resolved = DIRECTORY.resolve(name).normalize();
		if (!resolved.getParent().equals(DIRECTORY)) throw new IllegalArgumentException("Invalid development configuration path");
		return resolved;
	}

	public static synchronized void activateProduction() { mode = Mode.PRODUCTION; activeFile = PRODUCTION; }
	public static synchronized void activateDevelopment(String name) throws IOException { mode = Mode.DEVELOPMENT; activeFile = developmentFile(name); }
	public static synchronized void saveAsDevelopment(String name) throws IOException {
		Path target = developmentFile(name); if(!Store.save2XML(Main.getMySmartHomeHandler(), target))throw new IOException("Unable to save development configuration");
	}
	public static synchronized Path createEmptyDevelopment(String name) throws IOException {
		Path target=developmentFile(name);if(!Store.save2XML(new SmartHomeHandler(),target))throw new IOException("Unable to create empty development configuration");return target;
	}
	public static synchronized String download(String name) throws IOException {
		Path file = developmentFile(name); enforceSize(file); return Base64.getEncoder().encodeToString(Files.readAllBytes(file));
	}
	public static synchronized void upload(String name, String base64) throws Exception {
		byte[] bytes = Base64.getDecoder().decode(base64); if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("Configuration upload exceeds 5 MB");
		Path file = developmentFile(name), temporary = Files.createTempFile(DIRECTORY, "upload-", ".xml");
		try { Files.write(temporary, bytes); Main.validateConfiguration(temporary); try{Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException unsupported){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);} }
		finally { Files.deleteIfExists(temporary); }
	}
	public static synchronized Path promoteDevelopment() throws IOException {
		if (!isDevelopment()) throw new IllegalStateException("A development configuration must be active");
		String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
		Path backup = Path.of("settings-backup-" + stamp + ".xml").toAbsolutePath().normalize();
		if (Files.exists(PRODUCTION)) Files.copy(PRODUCTION, backup, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(activeFile, PRODUCTION, StandardCopyOption.REPLACE_EXISTING); return backup;
	}
	private static void enforceSize(Path file) throws IOException { if (!Files.exists(file)) throw new IOException("Configuration file not found"); if (Files.size(file) > MAX_BYTES) throw new IOException("Configuration exceeds 5 MB"); }
}
