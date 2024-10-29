package com.damsotplugin;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionCommand implements CommandExecutor {

    private final DamsotPlugin plugin;
    private final Map<String, String> playerFactions;

    public FactionCommand(DamsotPlugin plugin, Map<String, String> playerFactions) {
        this.plugin = plugin;
        this.playerFactions = playerFactions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только человек!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("fracadd")) {
            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "Использование: /fracadd <nickname> <name_fraction>");
                return true;
            }
            String nickname = args[0];
            String faction = args[1];

            playerFactions.put(nickname, faction);
            plugin.savePlayerFactions();
            player.sendMessage(ChatColor.GREEN + "Игрок " + nickname + " добавлен к " + faction + ".");
            return true;

        } else if (command.getName().equalsIgnoreCase("fracmenu")) {
            String faction = playerFactions.get(player.getName());
            if (faction == null) {
                player.sendMessage(ChatColor.RED + "Вы не в фракции!");
                return true;
            }
            FactionMenu.openMenu(player, faction);
            return true;
        }

        return false;
    }
}
