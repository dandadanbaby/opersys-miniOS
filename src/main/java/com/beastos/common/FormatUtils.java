package com.beastos.common;

import java.text.DecimalFormat;

// we threw all our shared formatting helpers in here so our OS workbench
// displays numbers consistently across all the tabs (CPU, memory, etc.)
public final class FormatUtils {

    // we made these static formatters so we don't keep recreating DecimalFormat objects every frame
    private static final DecimalFormat DECIMAL_1 = new DecimalFormat("#,##0.0");
    private static final DecimalFormat DECIMAL_2 = new DecimalFormat("#,##0.00");
    private static final DecimalFormat INTEGER_FMT = new DecimalFormat("#,##0");

    // private constructor so nobody tries to do "new FormatUtils()" — it's strictly static helpers
    private FormatUtils() {}

    // quick percent formatter for CPU usage and progress bars
    public static String formatPercent(double value) {
        return DECIMAL_1.format(value) + "%";
    }

    // formats to 1 decimal place (e.g. 98.6)
    public static String formatDecimal1(double value) {
        return DECIMAL_1.format(value);
    }

    // formats to 2 decimal places, perfect for stuff like average waiting or turnaround times
    public static String formatDecimal2(double value) {
        return DECIMAL_2.format(value);
    }

    // standard integer formatting with commas so big numbers like 1,000,000 are readable
    public static String formatInteger(int value) {
        return INTEGER_FMT.format(value);
    }

    // zero-padding for process IDs (e.g., PID 001) using String.format magic
    public static String zeroPad(int value, int digits) {
        return String.format("%0" + digits + "d", value);
    }

    /* basically what happens here is if memory is at least 1 GB we show it in GB,
       otherwise we multiply by 1024 to show it in MB so the user doesn't see "0.2 GB" */
    public static String formatMemory(double gb) {
        if (gb >= 1.0) {
            return DECIMAL_1.format(gb) + " GB";
        } else {
            return INTEGER_FMT.format(gb * 1024) + " MB";
        }
    }
}
