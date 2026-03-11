package org.dalvsync.tickflow.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.dalvsync.tickflow.TickFlow;
import org.dalvsync.tickflow.config.Config;

public class ReloadCommand implements CommandExecutor {

    private final TickFlow plugin;

    public ReloadCommand(TickFlow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tickflow.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            Config.load(plugin);
            sender.sendMessage(ChatColor.GREEN + "[TickFlow] Configuration successfully reloaded!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /tickflow reload");
        return true;
    }
}