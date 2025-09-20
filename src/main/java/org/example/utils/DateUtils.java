package org.example.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Slf4j
public class DateUtils {
    
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    /**
     * Get date that is 3 days from next Friday
     */
    public static String getThreeDaysFromNextFriday() {
        LocalDate today = LocalDate.now();
        LocalDate nextFriday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        LocalDate targetDate = nextFriday.plusDays(3); // 3 days from Friday = Monday
        
        log.info("Today: {}, Next Friday: {}, Target date (3 days later): {}", 
                today, nextFriday, targetDate);
        
        return targetDate.format(ISO_DATE_FORMAT);
    }
    
    /**
     * Get check-out date (1 day after check-in for 1 night stay)
     */
    public static String getCheckOutDate(String checkInDate) {
        LocalDate checkIn = LocalDate.parse(checkInDate, ISO_DATE_FORMAT);
        LocalDate checkOut = checkIn.plusDays(1);
        
        return checkOut.format(ISO_DATE_FORMAT);
    }
    
    /**
     * Format date for display
     */
    public static String formatForDisplay(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate, ISO_DATE_FORMAT);
        return date.format(DISPLAY_DATE_FORMAT);
    }
    
    /**
     * Get current date in ISO format - no parameters
     */
    public static String getCurrentDate() {
        // Use LocalDateTime instead of LocalDate if you need time fields
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
        
        // OR if you only need date without time:
        // LocalDate today = LocalDate.now();
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // return today.format(formatter);
    }
    
    /**
     * Get current date with custom format
     */
    public static String getCurrentDate(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.now().format(formatter);
    }
    
    /**
     * Add days to a date
     */
    public static String addDays(String isoDate, int days) {
        LocalDate date = LocalDate.parse(isoDate, ISO_DATE_FORMAT);
        return date.plusDays(days).format(ISO_DATE_FORMAT);
    }
}