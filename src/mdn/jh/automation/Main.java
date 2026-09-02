package mdn.jh.automation;

import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;

import mdn.jh.automation.device.Device;
import mdn.jh.automation.storage.Store;
import mdn.jh.automation.security.WebUserStore;
import mdn.jh.automation.webserver.TomcatServer;

public class Main {

	private static SmartHomeHandler mySmartHomeHandler = null;
	private static SmartHomeHandler productionHandler = null;
	private static SmartHomeHandler developmentHandler = null;
	private static boolean test = false;
	private static TomcatServer tomcatServer = null;
	private static boolean usersEnabled = false;
	private static boolean minimumPasswordLengthEnabled = true;
	private static WebUserStore webUserStore;

	public static SmartHomeHandler getMySmartHomeHandler() {
		return mySmartHomeHandler;
	}

	/** Runtime used by the production dashboard, also while development mode is active. */
	public static synchronized SmartHomeHandler getProductionHandler() {
		return productionHandler != null ? productionHandler : mySmartHomeHandler;
	}

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static boolean isTest() {
		return test;
	}

	private static String iniFile = "settings.ini";
	private static Level logLevel = Level.INFO;
	private static String serverAddress = "0.0.0.0";
	private static int serverPort = 8080;
	private static boolean httpsEnabled = false;
	private static Path httpsCertificate;
	// Cycle time in ms
	static int cycleTime = 2000;
	// private static Logger logger = null;
	private static final Logger logger = Logger.getLogger("Home-Automation");
	private static final InMemoryLogHandler inMemoryLogHandler = new InMemoryLogHandler();

	public static void main(String[] args) {
		processParameter(args);

/*		if (!Settings.init(iniFile)) {
			System.err.println("INI-File initialisation failed (settings.ini)");
			System.exit(2);
		}
*/
		// TODO Read all certs from the cert folder and add to configuration file
		/*
		getLogger().log(Level.ALL, "Init SSL Certificates from folder:....");
		if (!MySSLSocketfactory.addCertificate("certs/boxcert.cer")) {
		}
		getLogger().log(Level.ALL, "Init Fritz Box");
*/
		// mySmartHomeHandler=Save.recoverState();
		mySmartHomeHandler = new SmartHomeHandler();

		try {
			Store.loadFromXML(mySmartHomeHandler);
		} catch (Exception e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			getLogger().log(Level.WARNING, "Failed to load settings from file: " + e.getLocalizedMessage());
			mySmartHomeHandler = new SmartHomeHandler();
		}

		mySmartHomeHandler.initCoreDevice();
		mySmartHomeHandler.initModbusServerDevice();
		mySmartHomeHandler.initMqttBrokerDevice();
		productionHandler = mySmartHomeHandler;

		startTomcatServer();

		Iterator<Device> devices = mySmartHomeHandler.getDevices().iterator();
		getLogger().log(Level.ALL, "Starting Devices");
		while (devices.hasNext()) {
			Device d = devices.next();
			// System.out.println("Device: "+d.getName());
			d.startUpdateThreads();
		}

		try {
			new CountDownLatch(1).await();
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			getLogger().log(Level.INFO, "Main application thread interrupted; shutting down");
		}
	}

	public static void save() {
		Store.save2XML(mySmartHomeHandler, mdn.jh.automation.storage.ConfigurationManager.getActiveFile());
	}

	public static synchronized void validateConfiguration(Path file) throws Exception {
		SmartHomeHandler candidate = new SmartHomeHandler();
		if (!Store.loadFromXML(candidate, file)) throw new IOException("Configuration is not valid XML");
		shutdownHandler(candidate);
	}

	public static synchronized void reloadConfiguration(Path file, Map<Integer, Double> counterTakeovers) throws Exception {
		SmartHomeHandler replacement = new SmartHomeHandler();
		if (!Store.loadFromXML(replacement, file)) throw new IOException("Configuration could not be loaded");
		replacement.initCoreDevice(); replacement.initModbusServerDevice(); replacement.initMqttBrokerDevice();
		boolean development = mdn.jh.automation.storage.ConfigurationManager.isDevelopment();
		for (mdn.jh.automation.io.logic.LogicBase logic : replacement.getDataProcessors())
			if (logic instanceof mdn.jh.automation.io.logic.components.number.Counter) {
				mdn.jh.automation.io.logic.components.number.Counter counter=(mdn.jh.automation.io.logic.components.number.Counter)logic;
				if(development)counter.isolateDevelopmentPersistence();
				if(counterTakeovers!=null&&counterTakeovers.containsKey(logic.getId()))counter.takeOverValue(counterTakeovers.get(logic.getId()));
			}
		setSinkSuppression(replacement, development);
		SmartHomeHandler previous = development ? developmentHandler : productionHandler;
		if (development) developmentHandler = replacement; else productionHandler = replacement;
		mySmartHomeHandler = replacement;
		shutdownHandler(previous); startHandler(replacement);
		if(!development&&developmentHandler!=null){shutdownHandler(developmentHandler);developmentHandler=null;}
	}

	public static synchronized boolean hasProductionRuntime() { return productionHandler != null; }
	public static synchronized void switchToProductionRuntime() {
		if (productionHandler == null) throw new IllegalStateException("Production runtime is not available");
		mySmartHomeHandler = productionHandler;
		if(developmentHandler!=null){shutdownHandler(developmentHandler);developmentHandler=null;}
	}

	private static void setSinkSuppression(SmartHomeHandler handler, boolean suppressed) {
		for (Device device : handler.getDevices())
			for (mdn.jh.automation.io.sink.DataSink sink : device.getDataSinkHandler().getMyDataSinks()) sink.setActionsSuppressed(suppressed);
	}

	public static synchronized Map<Integer, Boolean> inspectCounters(Path file) throws Exception {
		SmartHomeHandler candidate = new SmartHomeHandler();
		if (!Store.loadFromXML(candidate, file)) throw new IOException("Configuration could not be loaded");
		Map<Integer, Boolean> result = new LinkedHashMap<>();
		for (mdn.jh.automation.io.logic.LogicBase logic : candidate.getDataProcessors())
			if (logic instanceof mdn.jh.automation.io.logic.components.number.Counter)
				result.put(logic.getId(), ((mdn.jh.automation.io.logic.components.number.Counter) logic).isPersistenceEnabled());
		shutdownHandler(candidate); return result;
	}

	private static void startHandler(SmartHomeHandler handler) { for (Device device : handler.getDevices()) device.startUpdateThreads(); }
	private static void shutdownHandler(SmartHomeHandler handler) {
		if (handler == null) return;
		for (Device device : handler.getDevices()) device.stopUpdateThreads();
		for (mdn.jh.automation.io.logic.LogicBase logic : handler.getDataProcessors()) logic.shutdownRuntime();
	}

	private static void startTomcatServer() {
		getLogger().log(Level.INFO, "Starting Tomcat Server");
		tomcatServer = new TomcatServer();
		Thread t = new Thread() {

			public void run() {
				try {
					tomcatServer.start();
				} catch (Exception e) {
					getLogger().log(Level.SEVERE, "Tomcat Server Start Failed.\n" + e.getMessage());
				}
				getLogger().log(Level.INFO, "Tomcat Server Started");
			}

		};

		t.start();

	}

	private static void processParameter(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("-h".equals(args[i])) {
				printHelpMessage();
				System.exit(0);
			}

			if ("-c".equals(args[i])) {
				cycleTime = Integer.valueOf(args[i + 1]).intValue() * 1000;
				if (cycleTime > 30000) {
					System.err.println("Cycle time must be <30s. Default is 2s - Exiting");
					System.exit(1);
				}
			}

			if ("-l".equals(args[i])) {
				int level = Integer.valueOf(args[i + 1]).intValue();
				if (level >= 0 && level <= 2) {
					logLevel = Level.ALL;
				} else if (level == 3) {
					logLevel = Level.FINEST;
				} else if (level == 4) {
					logLevel = Level.FINER;
				} else if (level == 5) {
					logLevel = Level.FINE;
				} else if (level == 6 || level == 7) {
					logLevel = Level.CONFIG;
				} else if (level == 8) {
					logLevel = Level.INFO;
				} else if (level == 9) {
					logLevel = Level.WARNING;
				} else if (level == 10) {
					logLevel = Level.SEVERE;
				} else if (level == 11) {
					logLevel = Level.OFF;
				}

				System.out.println("LogLevel set to: " + logLevel);
				getLogger().log(Level.ALL, "LogLevel set to: " + logLevel);

			}

			if ("-v".equals(args[i])) {
				logLevel = Level.ALL;
				System.out.println("LogLevel set to verbose mode: " + logLevel);
			}

			if ("-test".equals(args[i])) {
				test = true;
			}

			if ("-with-users".equals(args[i])) {
				usersEnabled = true;
			}
			if ("-no-min-password-length".equals(args[i])) {
				minimumPasswordLengthEnabled = false;
			}

			if ("--server-address".equals(args[i])) {
				if (i + 1 >= args.length || args[i + 1].trim().isEmpty()) {
					parameterError("--server-address requires an IP address or host name");
				}
				serverAddress = args[++i].trim();
			}

			if ("--server-port".equals(args[i])) {
				if (i + 1 >= args.length) {
					parameterError("--server-port requires a port number");
				}
				try {
					serverPort = Integer.parseInt(args[++i]);
				} catch (NumberFormatException e) {
					parameterError("--server-port must be a number between 1 and 65535");
				}
				if (serverPort < 1 || serverPort > 65535) {
					parameterError("--server-port must be between 1 and 65535");
				}
			}

			if ("--https".equals(args[i])) {
				httpsEnabled = true;
			}

			if ("--https-certificate".equals(args[i])) {
				if (i + 1 >= args.length || args[i + 1].trim().isEmpty()) {
					parameterError("--https-certificate requires a PEM file");
				}
				httpsCertificate = Path.of(args[++i].trim());
				httpsEnabled = true;
			}

		}

		Handler handlerObj = new ConsoleHandler();
		handlerObj.setLevel(logLevel);
		logger.addHandler(handlerObj);
		logger.addHandler(inMemoryLogHandler);
		logger.setLevel(logLevel);
	}

	public static InMemoryLogHandler getInMemoryLogHandler() {
		return inMemoryLogHandler;
	}

	private static void parameterError(String message) {
		System.err.println(message);
		printHelpMessage();
		System.exit(1);
	}

	public static void printHelpMessage() {
		System.err.println("Parameter:\n" + "-c TIME  cycle time (updates) in seconds" + "\n-h print help message"
				+ "\n-v Verbose mode\n-l LOGLEVEL (0=All (like -v), 10=Serious, 11=Off; Default=8 (Info))"
				+ "\n--server-address ADDRESS  Tomcat bind address (default: 0.0.0.0)"
				+ "\n--server-port PORT        Tomcat port (default: 8080)"
				+ "\n--https                  Enable HTTPS (creates default.pem when needed)"
				+ "\n--https-certificate FILE Use a combined PEM certificate and private key"
				+ "\n-with-users               Enable web users and access control (default: disabled)"
				+ "\n-no-min-password-length   Disable the 10-character minimum for new web-user passwords");
	}

	public static boolean isUsersEnabled() {
		return usersEnabled;
	}

	public static synchronized WebUserStore getWebUserStore() {
		if (webUserStore == null) {
			try {
				webUserStore = new WebUserStore(Path.of("web-users.properties"), minimumPasswordLengthEnabled);
			} catch (IOException e) {
				throw new IllegalStateException("Cannot read web-users.properties", e);
			}
		}
		return webUserStore;
	}

	public static int getMinimumPasswordLength() { return minimumPasswordLengthEnabled ? WebUserStore.DEFAULT_MINIMUM_PASSWORD_LENGTH : 0; }

	public static String getServerAddress() {
		return serverAddress;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static boolean isHttpsEnabled() {
		return httpsEnabled;
	}

	public static Path getHttpsCertificate() {
		return httpsCertificate;
	}

	public static Logger getLogger() {
		// if (logger == null) {
		// logger = Logger.getLogger("Home-Automation");
		// }
		return logger;
	}
}
