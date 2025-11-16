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
    public static String getThreeDaysFromNextFriday() {
        LocalDate today = LocalDate.now();
        LocalDate nextFriday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        LocalDate targetDate = nextFriday.plusDays(3); // 3 days from Friday = Monday

        log.info("Today: {}, Next Friday: {}, Target date (3 days later): {}",
                today, nextFriday, targetDate);

        return targetDate.format(ISO_DATE_FORMAT);
    }

    public static String getCurrentTimestamp(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.now().format(formatter);
    }
}