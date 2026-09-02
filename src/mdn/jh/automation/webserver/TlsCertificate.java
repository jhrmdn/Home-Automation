package mdn.jh.automation.webserver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.EnumSet;
import java.util.UUID;

final class TlsCertificate {
	private static final Path DEFAULT_FILE = Path.of("default.pem");

	private TlsCertificate() { }

	static Path resolve(Path configured) throws Exception {
		Path target = (configured == null ? DEFAULT_FILE : configured).toAbsolutePath().normalize();
		if (Files.isRegularFile(target)) return target;
		if (configured != null) throw new IllegalArgumentException("HTTPS certificate does not exist: " + target);
		generate(target);
		return target;
	}

	private static void generate(Path target) throws Exception {
		Path parent = target.getParent();
		if (parent != null) Files.createDirectories(parent);
		Path keyStoreFile = Files.createTempFile("home-automation-certificate-", ".p12");
		Files.deleteIfExists(keyStoreFile);
		String password = UUID.randomUUID().toString();
		try {
			Path keytool = Path.of(System.getProperty("java.home"), "bin",
					System.getProperty("os.name", "").toLowerCase().contains("win") ? "keytool.exe" : "keytool");
			Process process = new ProcessBuilder(keytool.toString(), "-genkeypair", "-alias", "home-automation",
					"-keyalg", "RSA", "-keysize", "2048", "-validity", "3650", "-dname", "CN=localhost",
					"-ext", "SAN=dns:localhost,ip:127.0.0.1", "-storetype", "PKCS12", "-keystore",
					keyStoreFile.toString(), "-storepass", password, "-keypass", password, "-noprompt")
					.redirectErrorStream(true).start();
			byte[] output = process.getInputStream().readAllBytes();
			if (process.waitFor() != 0) {
				throw new IllegalStateException("Could not generate HTTPS certificate: "
						+ new String(output, StandardCharsets.UTF_8));
			}
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			try (var input = Files.newInputStream(keyStoreFile)) {
				keyStore.load(input, password.toCharArray());
			}
			PrivateKey key = (PrivateKey) keyStore.getKey("home-automation", password.toCharArray());
			Certificate certificate = keyStore.getCertificate("home-automation");
			String pem = pem("PRIVATE KEY", key.getEncoded()) + pem("CERTIFICATE", certificate.getEncoded());
			Files.writeString(target, pem, StandardCharsets.US_ASCII);
			try {
				Files.setPosixFilePermissions(target, EnumSet.of(PosixFilePermission.OWNER_READ,
						PosixFilePermission.OWNER_WRITE));
			} catch (UnsupportedOperationException ignored) { }
		} finally {
			Files.deleteIfExists(keyStoreFile);
		}
	}

	private static String pem(String type, byte[] bytes) {
		return "-----BEGIN " + type + "-----\n"
				+ Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(bytes)
				+ "\n-----END " + type + "-----\n";
	}
}
