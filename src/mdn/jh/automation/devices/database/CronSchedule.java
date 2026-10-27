package mdn.jh.automation.devices.database;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Five-field cron schedule with explicitly selectable values and '*'. */
public class CronSchedule {
    private String minute = "*", hour = "*", day = "*", month = "*", weekday = "*";

    public CronSchedule() { }
    public CronSchedule(String expression) { setExpression(expression); }

    public void setExpression(String expression) {
        String[] fields = expression == null ? new String[0] : expression.trim().split("\\s+");
        if (fields.length != 5) throw new IllegalArgumentException("Cron needs minute hour day month weekday");
        minute = validate(fields[0], 0, 59); hour = validate(fields[1], 0, 23);
        day = validate(fields[2], 1, 31); month = validate(fields[3], 1, 12);
        weekday = validate(fields[4], 0, 7);
    }

    private String validate(String field, int min, int max) {
        if ("*".equals(field)) return field;
        Set<Integer> values = new LinkedHashSet<Integer>();
        for (String value : field.split(",")) {
            int number = Integer.parseInt(value);
            if (number < min || number > max) throw new IllegalArgumentException("Cron value out of range: " + value);
            values.add(number);
        }
        if (values.isEmpty()) throw new IllegalArgumentException("Empty cron field");
        StringBuilder result = new StringBuilder();
        for (Integer value : values) {
            if (result.length() > 0) result.append(',');
            result.append(value);
        }
        return result.toString();
    }

    public boolean matches(ZonedDateTime time) {
        int cronWeekday = time.getDayOfWeek().getValue() % 7;
        return matches(minute, time.getMinute(), false) && matches(hour, time.getHour(), false)
                && matches(day, time.getDayOfMonth(), false) && matches(month, time.getMonthValue(), false)
                && matches(weekday, cronWeekday, true);
    }

    private boolean matches(String field, int actual, boolean weekdayField) {
        if ("*".equals(field)) return true;
        for (String value : field.split(",")) {
            int candidate = Integer.parseInt(value);
            if (weekdayField && candidate == 7) candidate = 0;
            if (candidate == actual) return true;
        }
        return false;
    }

    public String getExpression() { return minute + " " + hour + " " + day + " " + month + " " + weekday; }
}
