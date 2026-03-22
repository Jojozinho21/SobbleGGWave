package com.sobblemc.sobbleggwave;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Main plugin class for SobbleGGWave.
 * Handles enable/disable lifecycle and config validation.
 */
public class SobbleGGWavePlugin extends JavaPlugin {

    private WaveManager waveManager;

    @Override
    public void onEnable() {
        // Phase 1: Config
        saveDefaultConfig();
        validateConfig();

        // Phase 2: Services
        this.waveManager = new WaveManager(this);

        // Phase 3: Register commands and listeners
        GGWaveCommand commandHandler = new GGWaveCommand(this, this.waveManager);
        PluginCommand ggwaveCommand = getCommand("ggwave");
        if (ggwaveCommand != null) {
            ggwaveCommand.setExecutor(commandHandler);
            ggwaveCommand.setTabCompleter(commandHandler);
        } else {
            getLogger().severe("Failed to register /ggwave command. Check plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(
                new ChatListener(this.waveManager), this);

        getLogger().info("SobbleGGWave v" + getDescription().getVersion() + " habilitado!");
    }

    @Override
    public void onDisable() {
        // Idempotent shutdown: cancel task, then end wave
        if (this.waveManager != null) {
            this.waveManager.cancelTask();
            if (this.waveManager.isActive()) {
                this.waveManager.endWave(false);
            }
        }
        getLogger().info("SobbleGGWave desabilitado.");
    }

    /**
     * Validates all config values, applying min/max limits and logging corrections.
     */
    public void validateConfig() {
        // Wave size: min 5, max 200
        int waveSize = getConfig().getInt("wave.size", 20);
        if (waveSize < 5) {
            getLogger().warning("wave.size (" + waveSize + ") abaixo do minimo. Usando 5.");
            getConfig().set("wave.size", 5);
        } else if (waveSize > 200) {
            getLogger().warning("wave.size (" + waveSize + ") acima do maximo. Usando 200.");
            getConfig().set("wave.size", 200);
        }

        // Timeout: min 10, max 600
        int timeout = getConfig().getInt("wave.timeout", 60);
        if (timeout < 10) {
            getLogger().warning("wave.timeout (" + timeout + ") abaixo do minimo. Usando 10.");
            getConfig().set("wave.timeout", 10);
        } else if (timeout > 600) {
            getLogger().warning("wave.timeout (" + timeout + ") acima do maximo. Usando 600.");
            getConfig().set("wave.timeout", 600);
        }

        // Cooldown: min 1, max 30
        int cooldown = getConfig().getInt("wave.cooldown", 3);
        if (cooldown < 1) {
            getLogger().warning("wave.cooldown (" + cooldown + ") abaixo do minimo. Usando 1.");
            getConfig().set("wave.cooldown", 1);
        } else if (cooldown > 30) {
            getLogger().warning("wave.cooldown (" + cooldown + ") acima do maximo. Usando 30.");
            getConfig().set("wave.cooldown", 30);
        }

        // Trigger word: must not be empty
        String triggerWord = getConfig().getString("trigger-word", "gg");
        if (triggerWord == null || triggerWord.trim().isEmpty()) {
            getLogger().warning("trigger-word vazio. Usando 'gg'.");
            getConfig().set("trigger-word", "gg");
        }

        // Colors: must have at least 1
        List<String> colors = getConfig().getStringList("colors");
        if (colors == null || colors.isEmpty()) {
            getLogger().warning("Lista de cores vazia. Usando cor padrao '&f&l'.");
            getConfig().set("colors", java.util.Collections.singletonList("&f&l"));
        }

        // Prefix: default if missing
        if (getConfig().getString("prefix") == null) {
            getConfig().set("prefix", "&8[&b&lGGWave&8] ");
        }
    }
}
