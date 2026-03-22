package com.sobblemc.sobbleggwave;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages wave state, GG counting, color distribution, cooldowns, and participant tracking.
 * All mutable state is synchronized for thread safety (AsyncPlayerChatEvent fires async).
 * Config values are cached at wave-start and via refreshConfig(), so no getConfig() calls
 * occur on async threads.
 */
public class WaveManager {

    private final SobbleGGWavePlugin plugin;
    private final WaveBroadcaster broadcaster;

    // --- Cached config fields (read on main thread, used on any thread) ---
    private volatile String triggerWord;
    private volatile List<String> colors;
    private volatile String prefix;
    private volatile String ggFormat;
    private volatile String msgWaveStart;
    private volatile String msgWaveEndCount;
    private volatile String msgWaveEndTimeout;
    private volatile String msgFirstGG;
    private volatile String msgLastGG;
    private volatile String msgCooldown;

    // --- Wave state ---
    private volatile boolean active;
    private int ggCount;
    private int waveSize;
    private long startTime;
    private int timeoutSeconds;
    private int cooldownSeconds;
    private BukkitTask timeoutTask;

    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, Integer> participantCounts = new HashMap<UUID, Integer>();

    public WaveManager(SobbleGGWavePlugin plugin) {
        this.plugin = plugin;
        this.broadcaster = new WaveBroadcaster();
        this.active = false;
        this.ggCount = 0;
        refreshConfig();
    }

    /**
     * Re-caches all config values from the current config state.
     * Must be called from the main thread (e.g. after reloadConfig()).
     */
    public synchronized void refreshConfig() {
        this.triggerWord = this.plugin.getConfig().getString("trigger-word", "gg");
        this.colors = this.plugin.getConfig().getStringList("colors");
        this.prefix = this.plugin.getConfig().getString("prefix", "");
        this.ggFormat = this.plugin.getConfig().getString("gg-format", "{color}{player}: GG!");
        this.msgWaveStart = this.plugin.getConfig().getString("messages.wave-start", "");
        this.msgWaveEndCount = this.plugin.getConfig().getString("messages.wave-end-count", "");
        this.msgWaveEndTimeout = this.plugin.getConfig().getString("messages.wave-end-timeout", "");
        this.msgFirstGG = this.plugin.getConfig().getString("messages.first-gg", "");
        this.msgLastGG = this.plugin.getConfig().getString("messages.last-gg", "");
        this.msgCooldown = this.plugin.getConfig().getString("messages.cooldown", "");
    }

    /** Returns the cached trigger word for use by ChatListener on the async thread. */
    public String getTriggerWord() {
        return this.triggerWord;
    }

    /**
     * Starts a new GG wave. Called from main thread (command handler).
     */
    public synchronized boolean startWave() {
        if (this.active) {
            return false;
        }

        this.active = true;
        this.ggCount = 0;
        this.waveSize = this.plugin.getConfig().getInt("wave.size", 20);
        this.timeoutSeconds = this.plugin.getConfig().getInt("wave.timeout", 60);
        this.cooldownSeconds = this.plugin.getConfig().getInt("wave.cooldown", 3);
        this.startTime = System.currentTimeMillis();
        this.cooldowns.clear();
        this.participantCounts.clear();

        // Snapshot message-templates into local finals for the Runnable below
        final String snapPrefix = this.prefix;
        final String snapMsgWaveStart = this.msgWaveStart;
        final String snapTriggerWord = this.triggerWord;

        long timeoutTicks = this.timeoutSeconds * 20L;
        this.timeoutTask = Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                endWave(true);
            }
        }, timeoutTicks);

        this.broadcaster.broadcastWaveStart(this.waveSize, this.timeoutSeconds,
                snapPrefix, snapMsgWaveStart, snapTriggerWord);
        return true;
    }

    /**
     * Ends the active wave. Idempotent and synchronized.
     */
    public synchronized void endWave(boolean timeout) {
        if (!this.active) {
            return;
        }
        this.active = false;

        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
            this.timeoutTask = null;
        }

        this.broadcaster.broadcastWaveEnd(timeout, this.ggCount, this.waveSize,
                getTopParticipant(), this.prefix, this.msgWaveEndTimeout, this.msgWaveEndCount);
        this.cooldowns.clear();
        this.participantCounts.clear();
    }

    /**
     * Processes a GG from a player. Synchronized for async thread safety.
     * Cooldown check BEFORE incrementing counter.
     */
    public synchronized boolean processGG(Player player) {
        if (!this.active) {
            return false;
        }

        UUID playerUuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Cooldown check BEFORE incrementing
        Long lastGG = this.cooldowns.get(playerUuid);
        if (lastGG != null) {
            long elapsed = (now - lastGG) / 1000L;
            if (elapsed < this.cooldownSeconds) {
                long remaining = this.cooldownSeconds - elapsed;
                this.broadcaster.sendCooldownMessage(player, remaining,
                        this.prefix, this.msgCooldown);
                return false;
            }
        }

        // Increment and track
        this.ggCount++;
        this.cooldowns.put(playerUuid, now);
        Integer currentCount = this.participantCounts.get(playerUuid);
        this.participantCounts.put(playerUuid, (currentCount == null ? 0 : currentCount) + 1);

        // First GG announcement
        if (this.ggCount == 1) {
            this.broadcaster.broadcastFirstGG(player, this.prefix, this.msgFirstGG);
        }

        // Broadcast the GG with sequential color
        String color = getColorForGG(this.ggCount);
        this.broadcaster.broadcastGG(player, this.ggCount, this.waveSize,
                color, this.prefix, this.ggFormat);

        // Check if wave is complete by count
        if (this.ggCount >= this.waveSize) {
            this.broadcaster.broadcastLastGG(player, this.prefix, this.msgLastGG);
            endWave(false);
        }

        return true;
    }

    /**
     * Calculates the color for a given GG number based on sequential distribution.
     * Uses the cached colors list — safe to call from any thread inside synchronized block.
     */
    public String getColorForGG(int ggNumber) {
        List<String> cachedColors = this.colors;
        if (cachedColors == null || cachedColors.isEmpty()) {
            return "&f&l";
        }
        int numColors = cachedColors.size();
        int colorIndex = ((ggNumber - 1) * numColors) / this.waveSize;
        if (colorIndex >= numColors) {
            colorIndex = numColors - 1;
        }
        return cachedColors.get(colorIndex);
    }

    public synchronized String[] getTopParticipant() {
        String topName = "Ninguem";
        int topCount = 0;
        for (Map.Entry<UUID, Integer> entry : this.participantCounts.entrySet()) {
            if (entry.getValue() > topCount) {
                topCount = entry.getValue();
                Player topPlayer = Bukkit.getPlayer(entry.getKey());
                if (topPlayer != null) {
                    topName = topPlayer.getName();
                }
            }
        }
        return new String[]{topName, String.valueOf(topCount)};
    }

    public synchronized int getRemainingSeconds() {
        if (!this.active) {
            return 0;
        }
        long elapsed = (System.currentTimeMillis() - this.startTime) / 1000L;
        long remaining = this.timeoutSeconds - elapsed;
        return (int) Math.max(0, remaining);
    }

    public synchronized void cancelTask() {
        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
            this.timeoutTask = null;
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public synchronized int getGGCount() {
        return this.ggCount;
    }

    public synchronized int getWaveSize() {
        return this.waveSize;
    }
}
