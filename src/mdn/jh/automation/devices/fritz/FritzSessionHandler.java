package mdn.jh.automation.devices.fritz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import mdn.jh.automation.Main;

public class FritzSessionHandler implements Serializable {

	private static final long serialVersionUID = -8135780471286570110L;
	private static final String NO_SESSION_ID = "0000000000000000";
	public static final String TLS_SYSTEM = "system";
	public static final String TLS_CERTIFICATE = "certificate";
	public static final String TLS_INSECURE = "insecure";
	private static final int CONNECT_TIMEOUT_MILLIS = 7000;
	private static final int READ_TIMEOUT_MILLIS = 10000;

	private String sessionID = NO_SESSION_ID;
	private final String urlFritzBox;
	private final String fritzPass;
	private final String fritzUser;
	private final String fritzIp;
	private final String tlsMode;
	private final String certificateBase64;
	private transient SSLSocketFactory socketFactory;
	private volatile boolean connected;
	private volatile String lastError = "Not connected yet";
	private volatile Instant lastSuccessfulRequest;

	public FritzSessionHandler(String fritzIP, String user, String pass) {
		this(fritzIP, user, pass, TLS_INSECURE, "");
	}

	public FritzSessionHandler(String fritzIP, String user, String pass, String tlsMode, String certificateBase64) {
		this.fritzIp = fritzIP == null ? "" : fritzIP.trim();
		this.urlFritzBox = "https://" + this.fritzIp;
		this.fritzUser = user == null ? "" : user;
		this.fritzPass = pass == null ? "" : pass;
		this.tlsMode = normalizeTlsMode(tlsMode);
		this.certificateBase64 = normalizeCertificate(certificateBase64);
	}

	public String getFritz_pass() { return fritzPass; }
	public String getFritz_user() { return fritzUser; }
	public String getFritz_ip() { return fritzIp; }
	public String getBaseURL() { return urlFritzBox; }
	public String getTlsMode() { return tlsMode; }
	public String getCertificateBase64() { return certificateBase64; }
	public boolean isConnected() { return connected; }
	public String getLastError() { return lastError; }
	public Instant getLastSuccessfulRequest() { return lastSuccessfulRequest; }
	public boolean hasSession() { return !NO_SESSION_ID.equals(sessionID); }

	public String requestNewSessionID() {
		sessionID = NO_SESSION_ID;
		return getSessionID();
	}

	public synchronized boolean checkConnectionOK() {
		sessionID = NO_SESSION_ID;
		return getSessionID() != null && hasSession();
	}

	public synchronized String getSessionID() {
		Document document = requestXml(urlFritzBox + "/login_sid.lua?sid=" + encode(sessionID));
		if (document == null) return null;

		String sid = value(document, "/SessionInfo/SID");
		if (sid != null && !NO_SESSION_ID.equals(sid) && !sid.isBlank()) {
			sessionID = sid;
			markConnected();
			return sessionID;
		}

		String challenge = value(document, "/SessionInfo/Challenge");
		if (challenge == null || challenge.isBlank()) {
			fail("FRITZ!Box did not return an authentication challenge", null);
			return null;
		}

		try {
			String response = createChallengeResponse(challenge, fritzPass);
			String loginUrl = urlFritzBox + "/login_sid.lua?username=" + encode(fritzUser)
					+ "&response=" + encode(response);
			document = requestXml(loginUrl);
			if (document == null) return null;
			sid = value(document, "/SessionInfo/SID");
			if (sid == null || sid.isBlank() || NO_SESSION_ID.equals(sid)) {
				String blockTime = value(document, "/SessionInfo/BlockTime");
				String suffix = blockTime != null && !"0".equals(blockTime) && !blockTime.isBlank()
						? " (login blocked for " + blockTime + " seconds)" : "";
				fail("Authentication failed for user '" + fritzUser + "'" + suffix, null);
				return null;
			}
			sessionID = sid;
			markConnected();
			return sessionID;
		} catch (Exception error) {
			fail("Unable to create the FRITZ!Box login response: " + message(error), error);
			return null;
		}
	}

	public Document getReplyFromFritzBoxNodeList(String urlString) {
		return requestXml(urlString);
	}

	public String getReplyFromFritzBoxText(String urlString) {
		HttpsURLConnection connection = null;
		try {
			connection = open(urlString);
			int status = connection.getResponseCode();
			String response = readResponse(connection, status).trim();
			if (status >= 400) throw new IOException("FRITZ!Box returned HTTP " + status + ": " + response);
			markConnected();
			return response;
		} catch (Exception error) {
			fail(connectionError(error), error);
			return null;
		} finally {
			if (connection != null) connection.disconnect();
		}
	}

	private Document requestXml(String urlString) {
		HttpsURLConnection connection = null;
		try {
			connection = open(urlString);
			int status = connection.getResponseCode();
			if (status >= 400) throw new IOException("FRITZ!Box returned HTTP " + status + ": " + readResponse(connection, status));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			try (InputStream input = connection.getInputStream()) {
				Document result = factory.newDocumentBuilder().parse(input);
				markConnected();
				return result;
			}
		} catch (Exception error) {
			fail(connectionError(error), error);
			return null;
		} finally {
			if (connection != null) connection.disconnect();
		}
	}

	private HttpsURLConnection open(String urlString) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection) URI.create(urlString).toURL().openConnection();
		connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
		connection.setReadTimeout(READ_TIMEOUT_MILLIS);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("GET");
		connection.setUseCaches(false);
		if (TLS_INSECURE.equals(tlsMode)) {
			connection.setSSLSocketFactory(getSocketFactory());
			connection.setHostnameVerifier((hostname, session) -> true);
		} else if (TLS_CERTIFICATE.equals(tlsMode)) {
			connection.setSSLSocketFactory(getSocketFactory());
		}
		return connection;
	}

	private synchronized SSLSocketFactory getSocketFactory() throws Exception {
		if (socketFactory != null) return socketFactory;
		SSLContext context = SSLContext.getInstance("TLS");
		if (TLS_INSECURE.equals(tlsMode)) {
			TrustManager[] trustAll = { new X509TrustManager() {
				@Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
				@Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
				@Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
			} };
			context.init(null, trustAll, new SecureRandom());
		} else if (TLS_CERTIFICATE.equals(tlsMode)) {
			if (certificateBase64.isBlank()) throw new IllegalArgumentException("No TLS certificate was uploaded");
			X509Certificate certificate;
			try (InputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(certificateBase64))) {
				certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
			}
			certificate.checkValidity();
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			store.load(null, null);
			store.setCertificateEntry("fritzbox", certificate);
			TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			factory.init(store);
			context.init(null, factory.getTrustManagers(), new SecureRandom());
		} else {
			context.init(null, null, null);
		}
		socketFactory = context.getSocketFactory();
		return socketFactory;
	}

	static String createChallengeResponse(String challenge, String password) throws Exception {
		if (challenge.startsWith("2$")) {
			String[] parts = challenge.split("\\$");
			if (parts.length != 5) throw new IllegalArgumentException("Unsupported PBKDF2 challenge format");
			int iterations1 = Integer.parseInt(parts[1]);
			byte[] salt1 = hex(parts[2]);
			int iterations2 = Integer.parseInt(parts[3]);
			byte[] salt2 = hex(parts[4]);
			byte[] hash1 = pbkdf2(password.getBytes(StandardCharsets.UTF_8), salt1, iterations1);
			byte[] hash2 = pbkdf2(hash1, salt2, iterations2);
			return parts[4] + "$" + toHex(hash2);
		}
		byte[] digest = MessageDigest.getInstance("MD5")
				.digest((challenge + "-" + password).getBytes(StandardCharsets.UTF_16LE));
		return challenge + "-" + String.format("%032x", new BigInteger(1, digest));
	}

	private static byte[] pbkdf2(byte[] value, byte[] salt, int iterations) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(value, "HmacSHA256"));
		byte[] saltAndBlock = java.util.Arrays.copyOf(salt, salt.length + 4);
		saltAndBlock[salt.length + 3] = 1;
		byte[] current = mac.doFinal(saltAndBlock);
		byte[] result = current.clone();
		for (int i = 1; i < iterations; i++) {
			current = mac.doFinal(current);
			for (int j = 0; j < result.length; j++) result[j] ^= current[j];
		}
		return result;
	}

	private static byte[] hex(String value) {
		return java.util.HexFormat.of().parseHex(value);
	}

	private static String toHex(byte[] value) {
		return java.util.HexFormat.of().formatHex(value);
	}

	private static String value(Document document, String expression) {
		if (document == null) return null;
		try { return (String) XPathFactory.newInstance().newXPath().evaluate(expression, document, XPathConstants.STRING); }
		catch (Exception error) { return null; }
	}

	private static String encode(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}

	private static String readResponse(HttpURLConnection connection, int status) throws IOException {
		InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
		if (stream == null) return "";
		try (InputStream input = stream) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); }
	}

	private void markConnected() {
		connected = true;
		lastError = "";
		lastSuccessfulRequest = Instant.now();
	}

	private void fail(String detail, Throwable error) {
		connected = false;
		lastError = detail;
		Main.getLogger().log(Level.WARNING, "FRITZ!Box " + fritzIp + ": " + detail, error);
	}

	private String connectionError(Throwable error) {
		String detail = message(error);
		if (detail.contains("PKIX path building failed") || detail.contains("unable to find valid certification path"))
			return "TLS certificate validation failed. Upload the FRITZ!Box certificate or disable validation";
		if (detail.contains("No subject alternative") || detail.contains("No name matching"))
			return "TLS hostname validation failed for " + fritzIp + ". Use a matching host name or disable validation";
		if (detail.contains("timed out")) return "Connection timed out";
		if (detail.contains("Connection refused")) return "Connection refused";
		return "Connection failed: " + detail;
	}

	private static String message(Throwable error) {
		Throwable current = error;
		while (current.getCause() != null && current.getCause() != current) current = current.getCause();
		String message = current.getMessage();
		return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
	}

	private static String normalizeTlsMode(String mode) {
		if (TLS_SYSTEM.equals(mode) || TLS_CERTIFICATE.equals(mode) || TLS_INSECURE.equals(mode)) return mode;
		return TLS_INSECURE;
	}

	private static String normalizeCertificate(String certificate) {
		if (certificate == null) return "";
		String value = certificate.trim();
		int comma = value.indexOf(',');
		if (value.startsWith("data:") && comma >= 0) value = value.substring(comma + 1);
		return value.replace("-----BEGIN CERTIFICATE-----", "")
				.replace("-----END CERTIFICATE-----", "").replaceAll("\\s", "");
	}
}
