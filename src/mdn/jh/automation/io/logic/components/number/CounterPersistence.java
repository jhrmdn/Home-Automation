package mdn.jh.automation.io.logic.components.number;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;

import mdn.jh.automation.Main;

/** Stores persistent counter values independently of settings.xml. */
final class CounterPersistence {
	static Path persistenceFile = Path.of("counter-values.properties");

	private CounterPersistence() { }

	static synchronized Double load(int id) {
		Properties values = readValues();
		String value = values.getProperty(Integer.toString(id));
		if (value == null) return null;
		try {
			double parsed = Double.parseDouble(value);
			return Double.isFinite(parsed) ? parsed : null;
		} catch (NumberFormatException exception) {
			Main.getLogger().log(Level.WARNING, "Invalid persistent value for counter " + id, exception);
			return null;
		}
	}

	static synchronized void save(int id, double value) {
		if (!Double.isFinite(value)) return;
		Properties values = readValues();
		values.setProperty(Integer.toString(id), Double.toString(value));
		writeValues(values);
	}

	static synchronized void remove(int id) {
		Properties values = readValues();
		if (values.remove(Integer.toString(id)) != null) writeValues(values);
	}

	private static Properties readValues() {
		Properties values = new Properties();
		if (!Files.isRegularFile(persistenceFile)) return values;
		try (InputStream input = Files.newInputStream(persistenceFile)) {
			values.load(input);
		} catch (IOException exception) {
			Main.getLogger().log(Level.WARNING, "Unable to read persistent counter values", exception);
		}
		return values;
	}

	private static void writeValues(Properties values) {
		Path absolute = persistenceFile.toAbsolutePath();
		Path parent = absolute.getParent();
		try {
			if (parent != null) Files.createDirectories(parent);
			Path temporary = Files.createTempFile(parent, "counter-values-", ".tmp");
			try {
				try (OutputStream output = Files.newOutputStream(temporary)) {
					values.store(output, "Persistent Home Automation counter values");
				}
				try {
					Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING,
							StandardCopyOption.ATOMIC_MOVE);
				} catch (IOException exception) {
					Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally {
				Files.deleteIfExists(temporary);
			}
		} catch (IOException exception) {
			Main.getLogger().log(Level.WARNING, "Unable to store persistent counter values", exception);
		}
	}
}
