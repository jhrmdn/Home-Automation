package mdn.jh.automation;

import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Path;

import mdn.jh.automation.device.Device;
import mdn.jh.automation.storage.Store;
import mdn.jh.automation.security.WebUserStore;
import mdn.jh.automation.webserver.TomcatServer;

public class Main {

	private static SmartHomeHandler mySmartHomeHandler = null;
	private static boolean test = false;
	private static TomcatServer tomcatServer = null;
	private static boolean usersEnabled = false;
	private static WebUserStore webUserStore;

	public static SmartHomeHandler getMySmartHomeHandler() {
		return mySmartHomeHandler;
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

		startTomcatServer();

		Iterator<Device> devices = mySmartHomeHandler.getDevices().iterator();
		getLogger().log(Level.ALL, "Starting Devices");
		while (devices.hasNext()) {
			Device d = devices.next();
			// System.out.println("Device: "+d.getName());
			d.startUpdateThreads();
		}

		while (true) {

		}
	}

	public static void save() {
		Store.save2XML(mySmartHomeHandler);
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
				+ "\n-with-users               Enable web users and access control (default: disabled)");
	}

	public static boolean isUsersEnabled() {
		return usersEnabled;
	}

	public static synchronized WebUserStore getWebUserStore() {
		if (webUserStore == null) {
			try {
				webUserStore = new WebUserStore(Path.of("web-users.properties"));
			} catch (IOException e) {
				throw new IllegalStateException("Cannot read web-users.properties", e);
			}
		}
		return webUserStore;
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static Logger getLogger() {
		// if (logger == null) {
		// logger = Logger.getLogger("Home-Automation");
		// }
		return logger;
	}
}
