package mdn.jh.automation.io.logic;

/** Shared fixed-width timer display used by Automation and Dashboard. */
public final class TimerTimeFormat {
	private TimerTimeFormat() { }

	public static String format(long durationMillis) {
		long safe = Math.max(0, durationMillis);
		long hours = safe / 3_600_000;
		long minutes = safe / 60_000 % 60;
		long seconds = safe / 1_000 % 60;
		long millis = safe % 1_000;
		return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
	}
}
