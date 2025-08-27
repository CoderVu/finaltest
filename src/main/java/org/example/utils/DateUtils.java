package org.example.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DateUtils {
    public static String getCurrentDate(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.now().format(formatter);
    }
}