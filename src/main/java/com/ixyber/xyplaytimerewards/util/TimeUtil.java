package com.ixyber.xyplaytimerewards.util;

public class TimeUtil {

    public static String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 seconds";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();

        append(builder, days, "day");
        append(builder, hours, "hour");
        append(builder, minutes, "minute");
        append(builder, seconds, "second");

        return builder.toString();
    }

    public static String formatShortDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 && days == 0) {
            builder.append(seconds).append("s");
        }

        return builder.toString().trim();
    }

    public static String formatMilestone(long minutes) {
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " Minute" : " Minutes");
        }

        if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + (hours == 1 ? " Hour" : " Hours");
        }

        if (minutes < 43200) {
            long days = minutes / 1440;
            return days + (days == 1 ? " Day" : " Days");
        }

        long months = minutes / 43200;
        return months + (months == 1 ? " Month" : " Months");
    }

    private static void append(StringBuilder builder, long value, String unit) {
        if (value <= 0) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(", ");
        }

        builder.append(value).append(" ").append(unit);
        if (value != 1) {
            builder.append("s");
        }
    }
}