package mdn.jh.automation.webserver;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mdn.jh.automation.Main;

public class TomcatServer {
	private static final String EMBEDDED_WEBAPP = "embedded-webapp";

	Tomcat tomcat = null;
	private Path runtimeDirectory;

	public TomcatServer() {
		tomcat = new Tomcat();
	}

	public Tomcat getTomcat() {
		return tomcat;
	}

	public void start() throws Exception {
		Main.getLogger().log(Level.INFO, "Starting Tomcat Webserver");

		Path webappDirectory = prepareWebappDirectory();
		Path baseDirectory = runtimeDirectory == null
				? Files.createTempDirectory("home-automation-tomcat-") : runtimeDirectory.resolve("tomcat");
		Files.createDirectories(baseDirectory);
		tomcat.setBaseDir(baseDirectory.toString());
		tomcat.setSilent(false);
		tomcat.setPort(Main.getServerPort());
		Connector connector = tomcat.getConnector();
		connector.setProperty("address", Main.getServerAddress());
		if (Main.isHttpsEnabled()) configureHttps(connector);
		tomcat.getHost().setAppBase(webappDirectory.toString());
		tomcat.getHost().setAutoDeploy(false);
		tomcat.getHost().setCreateDirs(true);
		tomcat.getHost().setDeployOnStartup(false);

		StandardContext ctx = (StandardContext) tomcat.addWebapp("", webappDirectory.toString());
		Tomcat.addServlet(ctx, "rootRedirect", new HttpServlet() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
				response.sendRedirect(request.getContextPath() + "/home-automation");
			}
		});
		ctx.addServletMappingDecoded("", "rootRedirect");
		Tomcat.addServlet(ctx, "outputApi", new OutputApiServlet());
		ctx.addServletMappingDecoded("/api/output", "outputApi");

		try {
			tomcat.start();

			Main.getLogger().log(Level.INFO, "Tomcat Webserver Started - Status: " + tomcat.getServer().getStateName()
					+ " on " + Main.getServerAddress() + ":" + Main.getServerPort());
//			org.apache.catalina.util.ServerInfo.main(null);
			tomcat.getServer().await();
			
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, "Tomcat Webserver Failed to Start: " + e.getLocalizedMessage());

		}
		

	}

	private void configureHttps(Connector connector) throws Exception {
		Path pem = TlsCertificate.resolve(Main.getHttpsCertificate());
		connector.setScheme("https");
		connector.setSecure(true);
		connector.setProperty("SSLEnabled", "true");
		SSLHostConfig host = new SSLHostConfig();
		SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(host,
				SSLHostConfigCertificate.Type.RSA);
		certificate.setCertificateFile(pem.toString());
		certificate.setCertificateKeyFile(pem.toString());
		host.addCertificate(certificate);
		connector.addSslHostConfig(host);
		Main.getLogger().log(Level.INFO, "HTTPS enabled with certificate " + pem);
	}

	private Path prepareWebappDirectory() throws Exception {
		URL resource = TomcatServer.class.getClassLoader().getResource(EMBEDDED_WEBAPP);
		if (resource == null) throw new IllegalStateException("Embedded web application is missing from the JAR");
		if ("file".equals(resource.getProtocol())) return Path.of(resource.toURI());
		if (!"jar".equals(resource.getProtocol()))
			throw new IllegalStateException("Unsupported embedded web application URL: " + resource);

		runtimeDirectory = Files.createTempDirectory("home-automation-");
		Path destination = runtimeDirectory.resolve("webapp");
		Files.createDirectories(destination);
		JarURLConnection connection = (JarURLConnection) resource.openConnection();
		URI jarUri = connection.getJarFileURL().toURI();
		try (JarFile jar = new JarFile(Path.of(jarUri).toFile())) {
			Enumeration<JarEntry> entries = jar.entries();
			String prefix = EMBEDDED_WEBAPP + "/";
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.getName().startsWith(prefix)) continue;
				String relativeName = entry.getName().substring(prefix.length());
				if (relativeName.isEmpty()) continue;
				Path target = destination.resolve(relativeName).normalize();
				if (!target.startsWith(destination)) throw new IllegalStateException("Invalid embedded path: " + entry.getName());
				if (entry.isDirectory()) Files.createDirectories(target);
				else {
					Files.createDirectories(target.getParent());
					try (var input = jar.getInputStream(entry)) {
						Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteRuntimeDirectory(), "tomcat-temp-cleanup"));
		return destination;
	}

	private void deleteRuntimeDirectory() {
		if (runtimeDirectory == null) return;
		try (var paths = Files.walk(runtimeDirectory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try { Files.deleteIfExists(path); } catch (Exception ignored) { }
			});
		} catch (Exception ignored) { }
	}

}
