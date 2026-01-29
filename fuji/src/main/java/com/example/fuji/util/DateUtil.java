package com.example.fuji.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date/Time utilities
 */
public class DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get current datetime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Format datetime to string
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Format datetime with custom pattern
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse string to datetime
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }
}
