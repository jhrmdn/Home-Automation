package mdn.jh.automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {
	private static Properties properties = null;
	private static File iniFile = null;

	public static boolean init(String iniFile1) {
		/*
		 * new LogHandler("Settings","Ribbat").logOutput("Init Settings File: " +
		 * iniFile1, LogHandler.DEBUG);
		 */
		iniFile = new File(iniFile1);
		try {
			properties = new Properties();
			properties.load(new FileInputStream(iniFile));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean saveSettings() {
		try {
			properties.store(new FileOutputStream(iniFile), "");
		} catch (Exception e) {
		}
		return true;
	}

	public static String getParameter(String parameterName) {
		if (properties == null) {
			init("xml/settingsTest.ini");
		}
		return properties.getProperty(parameterName);
	}

	public static void setParameter(String parameterName, String value) {
		properties.setProperty(parameterName, value);
		saveSettings();
	}

}
