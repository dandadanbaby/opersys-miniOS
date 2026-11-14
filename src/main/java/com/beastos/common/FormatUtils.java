package com.beastos.common;

import java.text.DecimalFormat;

/**
 * Shared formatting utilities used across all BEAST OS modules.
 */
public final class FormatUtils {

    private static final DecimalFormat DECIMAL_1 = new DecimalFormat("#,##0.0");
    private static final DecimalFormat DECIMAL_2 = new DecimalFormat("#,##0.00");
    private static final DecimalFormat INTEGER_FMT = new DecimalFormat("#,##0");

    private FormatUtils() {}

    public static String formatPercent(double value) {
        return DECIMAL_1.format(value) + "%";
    }

    public static String formatDecimal1(double value) {
        return DECIMAL_1.format(value);
    }

    public static String formatDecimal2(double value) {
        return DECIMAL_2.format(value);
    }

    public static String formatInteger(int value) {
        return INTEGER_FMT.format(value);
    }

    public static String zeroPad(int value, int digits) {
        return String.format("%0" + digits + "d", value);
    }

    public static String formatMemory(double gb) {
        if (gb >= 1.0) {
            return DECIMAL_1.format(gb) + " GB";
        } else {
            return INTEGER_FMT.format(gb * 1024) + " MB";
        }
    }
}
