package com.shazam.teebay.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String formatLocalDateTime(LocalDateTime dateTime) {
        int day = dateTime.getDayOfMonth();
        String daySuffix = getDayOfMonthSuffix(day);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy"); // e.g., June 2025
        String monthYear = dateTime.format(formatter);

        return String.format("%d%s %s", day, daySuffix, monthYear);
    }

    private static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        return switch (n % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
