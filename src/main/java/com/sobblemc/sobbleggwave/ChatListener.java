package com.sobblemc.sobbleggwave;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Intercepts chat messages during an active wave.
 * Only exact-match trigger words are intercepted; all other chat passes through.
 * Processes GG inline on the async thread (synchronized via WaveManager).
 * Reads trigger word from WaveManager's cache — no getConfig() on async thread.
 */
public class ChatListener implements Listener {

    private final WaveManager waveManager;

    public ChatListener(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!this.waveManager.isActive()) {
            return;
        }

        String triggerWord = this.waveManager.getTriggerWord();
        String message = event.getMessage().trim();

        // Exact match only — no contains()
        if (!message.equalsIgnoreCase(triggerWord)) {
            return;
        }

        // Prevent the original message from being sent by any plugin
        event.setCancelled(true);
        event.getRecipients().clear();

        // Process GG directly — WaveManager is synchronized for thread safety
        this.waveManager.processGG(event.getPlayer());
    }
}
