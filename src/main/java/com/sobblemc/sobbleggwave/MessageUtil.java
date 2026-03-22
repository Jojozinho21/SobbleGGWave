package com.sobblemc.sobbleggwave;

import org.bukkit.ChatColor;

/**
 * Utility class for color translation and message formatting.
 */
public final class MessageUtil {

    private MessageUtil() {
        // Utility class — no instantiation
    }

    /**
     * Translates '&' color codes to ChatColor format.
     *
     * @param text the text with '&' color codes
     * @return the translated text, or empty string if null
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Replaces a placeholder in a message with a value.
     *
     * @param message     the message template
     * @param placeholder the placeholder key (without braces)
     * @param value       the replacement value
     * @return the message with placeholder replaced
     */
    public static String replace(String message, String placeholder, String value) {
        if (message == null) {
            return "";
        }
        return message.replace("{" + placeholder + "}", value);
    }
}
