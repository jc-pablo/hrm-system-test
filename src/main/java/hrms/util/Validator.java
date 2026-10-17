package hrms.util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Common input conversion and validation helpers. */
public final class Validator {
    private Validator() { }

    public static Date parseDate(String value, String label) {
        try {
            return Date.valueOf(LocalDate.parse(value.trim()));
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            throw new IllegalArgumentException(label + " must use YYYY-MM-DD format.");
        }
    }

    public static Time parseTime(String value, String label) {
        try {
            String normalized = value.trim();
            if (normalized.matches("[0-9]{2}:[0-9]{2}")) normalized += ":00";
            return Time.valueOf(LocalTime.parse(normalized));
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            throw new IllegalArgumentException(label + " must use HH:MM or HH:MM:SS format.");
        }
    }

    /**
     * Converts a 12-hour clock entry and its selected AM/PM period into a 24-hour HH:mm value.
     * The result is suitable for storage in a SQL TIME column.
     */
    public static String parseAmPmTime(String value, String period, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) return "";
        Matcher matcher = Pattern.compile("^(\\d{1,2}):(\\d{2})$").matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(label + " must use h:mm format, with AM or PM selected.");
        }
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(matcher.group(1));
            minute = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must use h:mm format, with AM or PM selected.");
        }
        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException(label + " must use a valid 12-hour time such as 07:00 AM or 06:30 PM.");
        }
        if (period == null || !(period.equalsIgnoreCase("AM") || period.equalsIgnoreCase("PM"))) {
            throw new IllegalArgumentException("Select AM or PM for " + label + ".");
        }
        int hour24 = hour % 12;
        if (period.toUpperCase(Locale.ROOT).equals("PM")) hour24 += 12;
        return String.format(Locale.ROOT, "%02d:%02d", hour24, minute);
    }

    public static void validateEmail(String email) {
        if (email != null && !email.isBlank()
                && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }
}
