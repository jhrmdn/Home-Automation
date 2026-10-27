package mdn.jh.automation;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/** Keeps recent warnings and errors in RAM for display in the web interface. */
public final class InMemoryLogHandler extends Handler {
	private static final int CAPACITY = 50;
	private final Deque<Entry> entries = new ArrayDeque<Entry>(CAPACITY);

	public InMemoryLogHandler() {
		setLevel(Level.WARNING);
	}

	@Override
	public synchronized void publish(LogRecord record) {
		if (record == null || !isLoggable(record)) return;
		String message = record.getMessage() == null ? "" : record.getMessage();
		if (record.getThrown() != null) {
			Throwable cause = record.getThrown();
			while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
			String detail = cause.getMessage();
			message += " — " + cause.getClass().getSimpleName() + (detail == null ? "" : ": " + detail);
		}
		while (entries.size() >= CAPACITY) entries.removeFirst();
		entries.addLast(new Entry(record.getInstant(), record.getLevel().getName(), message));
	}

	public synchronized List<Entry> snapshot() {
		return new ArrayList<Entry>(entries);
	}

	@Override public void flush() { }
	@Override public void close() { clear(); }
	public synchronized void clear() { entries.clear(); }

	public static final class Entry {
		private final Instant timestamp;
		private final String level;
		private final String message;

		private Entry(Instant timestamp, String level, String message) {
			this.timestamp = timestamp;
			this.level = level;
			this.message = message;
		}

		public Instant getTimestamp() { return timestamp; }
		public String getLevel() { return level; }
		public String getMessage() { return message; }
	}
}
