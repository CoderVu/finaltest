package org.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatUtils {

    public static String formatPrice(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(0, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
    public static String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\n", " ").replaceAll("\\s+", " ");
    }
}
