package com.churchflow360.memberservice.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating membership numbers.
 * Format: MEM-MMDD-YYYY-HHMMSS
 * Example: MEM-0316-2026-125135
 */
public class MembershipNumberGenerator {

    private static final String PREFIX = "MEM-";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMdd-yyyy-HHmmss");

    /**
     * Generates a unique membership number using the current date and time.
     * Format: MEM-MMDD-YYYY-HHMMSS
     *
     * @return A unique membership number string
     */
    public static String generate() {
        LocalDateTime now = LocalDateTime.now();
        return PREFIX + now.format(FORMATTER);
    }

    /**
     * Generates a membership number using a specific date and time.
     * This method is primarily for testing purposes.
     *
     * @param dateTime The LocalDateTime to use for generation
     * @return A membership number string based on the provided dateTime
     */
    public static String generate(LocalDateTime dateTime) {
        return PREFIX + dateTime.format(FORMATTER);
    }
}

