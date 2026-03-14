package com.ixyber.xyplaytimerewards.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String text) {
        if (text == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "§x"
                    + "§" + hex.charAt(0)
                    + "§" + hex.charAt(1)
                    + "§" + hex.charAt(2)
                    + "§" + hex.charAt(3)
                    + "§" + hex.charAt(4)
                    + "§" + hex.charAt(5);
            matcher.appendReplacement(builder, replacement);
        }

        matcher.appendTail(builder);
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    public static List<String> color(List<String> lines) {
        List<String> colored = new ArrayList<>();
        for (String line : lines) {
            colored.add(color(line));
        }
        return colored;
    }

    public static String formatCoins(long amount) {
        if (amount >= 1_000_000_000L) {
            return trim(String.format("%.1fB", amount / 1_000_000_000.0));
        }
        if (amount >= 1_000_000L) {
            return trim(String.format("%.1fM", amount / 1_000_000.0));
        }
        if (amount >= 1_000L) {
            return trim(String.format("%.1fK", amount / 1_000.0));
        }
        return String.valueOf(amount);
    }

    public static String formatMoney(long amount) {
        return "$" + String.format("%,d", amount);
    }

    private static String trim(String input) {
        return input.replace(".0", "");
    }
}