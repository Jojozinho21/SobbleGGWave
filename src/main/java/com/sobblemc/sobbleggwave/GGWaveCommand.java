package com.sobblemc.sobbleggwave;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command executor and tab completer for /ggwave.
 */
public class GGWaveCommand implements CommandExecutor, TabCompleter {

    private final SobbleGGWavePlugin plugin;
    private final WaveManager waveManager;

    public GGWaveCommand(SobbleGGWavePlugin plugin, WaveManager waveManager) {
        this.plugin = plugin;
        this.waveManager = waveManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(sender, this.plugin.getConfig().getString("messages.usage", ""));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "iniciar":
                handleIniciar(sender);
                break;
            case "parar":
                handleParar(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            default:
                sendMessage(sender, this.plugin.getConfig().getString("messages.usage", ""));
                break;
        }
        return true;
    }

    private void handleIniciar(CommandSender sender) {
        // Permission check FIRST
        if (!sender.hasPermission("sobbleggwave.admin")) {
            sendMessage(sender, this.plugin.getConfig().getString("messages.no-permission", ""));
            return;
        }

        if (this.waveManager.isActive()) {
            String message = this.plugin.getConfig().getString("messages.already-active", "");
            message = MessageUtil.replace(message, "current", String.valueOf(this.waveManager.getGGCount()));
            message = MessageUtil.replace(message, "size", String.valueOf(this.waveManager.getWaveSize()));
            message = MessageUtil.replace(message, "remaining", String.valueOf(this.waveManager.getRemainingSeconds()));
            sendMessage(sender, message);
            return;
        }

        this.waveManager.startWave();
        sendMessage(sender, this.plugin.getConfig().getString("messages.wave-started-admin", ""));
    }

    private void handleParar(CommandSender sender) {
        if (!sender.hasPermission("sobbleggwave.admin")) {
            sendMessage(sender, this.plugin.getConfig().getString("messages.no-permission", ""));
            return;
        }

        if (!this.waveManager.isActive()) {
            sendMessage(sender, this.plugin.getConfig().getString("messages.no-wave", ""));
            return;
        }

        this.waveManager.endWave(false);
        sendMessage(sender, this.plugin.getConfig().getString("messages.wave-stopped-admin", ""));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("sobbleggwave.admin")) {
            sendMessage(sender, this.plugin.getConfig().getString("messages.no-permission", ""));
            return;
        }

        this.plugin.reloadConfig();
        this.plugin.validateConfig();
        this.waveManager.refreshConfig();
        sendMessage(sender, this.plugin.getConfig().getString("messages.reload", ""));
    }

    private void handleStatus(CommandSender sender) {
        if (this.waveManager.isActive()) {
            String[] topInfo = this.waveManager.getTopParticipant();
            String message = this.plugin.getConfig().getString("messages.status-active", "");
            message = MessageUtil.replace(message, "current", String.valueOf(this.waveManager.getGGCount()));
            message = MessageUtil.replace(message, "size", String.valueOf(this.waveManager.getWaveSize()));
            message = MessageUtil.replace(message, "remaining", String.valueOf(this.waveManager.getRemainingSeconds()));
            message = MessageUtil.replace(message, "top", topInfo[0]);
            message = MessageUtil.replace(message, "top_count", topInfo[1]);
            sendMessage(sender, message);
        } else {
            sendMessage(sender, this.plugin.getConfig().getString("messages.status-inactive", ""));
        }
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        String prefix = this.plugin.getConfig().getString("prefix", "");
        sender.sendMessage(MessageUtil.colorize(prefix + message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<String>();
            completions.add("status");
            if (sender.hasPermission("sobbleggwave.admin")) {
                completions.addAll(Arrays.asList("iniciar", "parar", "reload"));
            }
            List<String> filtered = new ArrayList<String>();
            for (String completion : completions) {
                if (completion.startsWith(args[0].toLowerCase())) {
                    filtered.add(completion);
                }
            }
            return filtered;
        }
        return Collections.emptyList();
    }
}
