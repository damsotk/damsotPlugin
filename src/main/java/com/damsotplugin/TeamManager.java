package com.damsotplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TeamManager implements CommandExecutor, Listener {
    private final HashMap<String, List<UUID>> teams = new HashMap<>();

    public TeamManager(DamsotPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("viewteam").setExecutor(this);
    }

    public void createTeam(String teamName) {
        teams.putIfAbsent(teamName, new ArrayList<>());
    }

    public void addPlayerToTeam(String teamName, Player player) {
        if (!teams.containsKey(teamName)) {
            player.sendMessage(ChatColor.RED + "Команда с таким названием не существует!");
            return;
        }
        teams.get(teamName).add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Вы присоединились к команде " + teamName + "!");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            for (List<UUID> team : teams.values()) {
                if (team.contains(damager.getUniqueId()) && team.contains(target.getUniqueId())) {
                    event.setCancelled(true);
                    damager.sendMessage(ChatColor.YELLOW + "Вы не можете атаковать своего союзника по команде!");
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("viewteam") && sender instanceof Player) {
            Player player = (Player) sender;
            for (String teamName : teams.keySet()) {
                if (teams.get(teamName).contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.AQUA + "Ваша команда: " + teamName);
                    for (UUID uuid : teams.get(teamName)) {
                        Player teamPlayer = Bukkit.getPlayer(uuid);
                        if (teamPlayer != null) {
                            player.sendMessage(ChatColor.GRAY + "- " + teamPlayer.getName());
                        }
                    }
                    return true;
                }
            }
            player.sendMessage(ChatColor.RED + "Вы не состоите ни в одной команде.");
            return true;
        }
        return false;
    }
}