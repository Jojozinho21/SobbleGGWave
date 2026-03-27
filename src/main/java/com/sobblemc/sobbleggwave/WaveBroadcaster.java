package com.sobblemc.sobbleggwave;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles all broadcast messages for the GG wave system.
 * Pure formatter — receives all data as parameters; holds no plugin reference
 * and never calls getConfig(), making it safe to use from any thread.
 */
public class WaveBroadcaster {

    public WaveBroadcaster() {
        // no-arg: no plugin reference needed
    }

    public void broadcastWaveStart(int waveSize, int timeoutSeconds,
            String prefix, String msgTemplate, String triggerWord) {
        if (msgTemplate == null || msgTemplate.isEmpty()) {
            return;
        }
        String message = msgTemplate;
        message = MessageUtil.replace(message, "trigger", triggerWord);
        message = MessageUtil.replace(message, "size", String.valueOf(waveSize));
        message = MessageUtil.replace(message, "timeout", String.valueOf(timeoutSeconds));
        Bukkit.broadcastMessage(MessageUtil.colorize(prefix + message));
    }

    public void broadcastWaveEnd(boolean timeout, int ggCount, int waveSize,
            String[] topInfo, String prefix, String msgTimeout, String msgCount) {
        String message = timeout ? msgTimeout : msgCount;
        if (message == null || message.isEmpty()) {
            return;
        }
        message = MessageUtil.replace(message, "total", String.valueOf(ggCount));
        message = MessageUtil.replace(message, "size", String.valueOf(waveSize));
        message = MessageUtil.replace(message, "top", topInfo[0]);
        message = MessageUtil.replace(message, "top_count", topInfo[1]);
        Bukkit.broadcastMessage(MessageUtil.colorize(prefix + message));
    }

    public void broadcastGG(Player player, int currentCount, int waveSize,
            String color, String prefix, String ggFormat) {
        if (ggFormat == null || ggFormat.isEmpty()) {
            return;
        }
        String format = ggFormat;
        format = MessageUtil.replace(format, "color", color);
        format = MessageUtil.replace(format, "player", player.getName());
        format = MessageUtil.replace(format, "current", String.valueOf(currentCount));
        format = MessageUtil.replace(format, "size", String.valueOf(waveSize));
        Bukkit.broadcastMessage(MessageUtil.colorize(prefix + format));
    }

    public void broadcastFirstGG(Player player, String prefix, String msgTemplate) {
        if (msgTemplate == null || msgTemplate.isEmpty()) {
            return;
        }
        String message = MessageUtil.replace(msgTemplate, "player", player.getName());
        Bukkit.broadcastMessage(MessageUtil.colorize(prefix + message));
    }

    public void broadcastLastGG(Player player, String prefix, String msgTemplate) {
        if (msgTemplate == null || msgTemplate.isEmpty()) {
            return;
        }
        String message = MessageUtil.replace(msgTemplate, "player", player.getName());
        Bukkit.broadcastMessage(MessageUtil.colorize(prefix + message));
    }

    public void sendCooldownMessage(Player player, long remaining,
            String prefix, String msgTemplate) {
        if (msgTemplate == null || msgTemplate.isEmpty()) {
            return;
        }
        String message = MessageUtil.replace(msgTemplate, "remaining", String.valueOf(remaining));
        player.sendMessage(MessageUtil.colorize(prefix + message));
    }
}
