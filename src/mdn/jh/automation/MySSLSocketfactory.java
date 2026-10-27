package mdn.jh.automation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MySSLSocketfactory {

	private static SSLSocketFactory mySSLSocketfactory = null;
	private static KeyStore trustStore = null;
	private static SSLContext sslContext = null;

	public MySSLSocketfactory() {
		// TODO Auto-generated constructor stub
	}

	public static boolean addCertificate(String certfile) {

		if (!initSSLSocketFactory()) {
			return false;
		}

		X509Certificate result = null;
		try (InputStream input = new FileInputStream(certfile)) {
			result = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		// Add it to the trust store
		try {
			trustStore.setCertificateEntry(certfile, result);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		TrustManagerFactory tmf = null;
		try {
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
			TrustManager[] trustManagers = tmf.getTrustManagers();
			sslContext.init(null, trustManagers, null);
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		mySSLSocketfactory = sslContext.getSocketFactory();
		return true;
	}

	private static boolean initSSLSocketFactory() {
		if (sslContext != null && trustStore != null) {
			return true;
		}

		try {
			sslContext = SSLContext.getInstance("SSL");
			// Create a new trust store, use getDefaultType for .jks files or "pkcs12" for
			// .p12 files
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// You can supply a FileInputStream to a .jks or .p12 file and the keystore
			// password as an alternative to loading the crt file
			trustStore.load(null, null);
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;

	}

	

	public static SSLSocketFactory getMySSLSocketfactory() {
		return mySSLSocketfactory;
	}

}
