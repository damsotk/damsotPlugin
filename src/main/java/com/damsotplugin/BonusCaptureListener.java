package com.damsotplugin;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BonusCaptureListener implements Listener {

    private final DamsotPlugin plugin;
    private final File bonusesFile;
    private final FileConfiguration bonusesConfig;
    private final Location capturePoint;

    public BonusCaptureListener(DamsotPlugin plugin) {
        this.plugin = plugin;

        bonusesFile = new File(plugin.getDataFolder(), "bonuses.yml");
        if (!bonusesFile.exists()) {
            plugin.saveResource("bonuses.yml", false);
        }
        bonusesConfig = YamlConfiguration.loadConfiguration(bonusesFile);

        String[] coords = bonusesConfig.getString("capturePoint.coordinates").split(",");
        World world = Bukkit.getWorld("Arnhold");
        if (world == null) {
            Bukkit.getLogger().severe("Мир не найден");
            capturePoint = null;
            return;
        }

        capturePoint = new Location(
                world,
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2])
        );
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (capturePoint == null) return;

        Player player = event.getPlayer();
        String playerFaction = plugin.getPlayerFactions().get(player.getName());

        if (playerFaction != null && event.getTo().distance(capturePoint) < 2) {
            String currentFaction = bonusesConfig.getString("capturePoint.faction");

            if (!playerFaction.equals(currentFaction)) {
                bonusesConfig.set("capturePoint.faction", playerFaction);
                saveBonusesConfig();

                player.sendMessage(ChatColor.GREEN + "Ваша фракция захватила точку!");
                if (currentFaction != null && !currentFaction.isEmpty()) {
                    Bukkit.broadcastMessage(ChatColor.RED + currentFaction + " утратила контроль над точкой.");
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + playerFaction + " теперь контролирует точку!");
            }
        }
    }

    private void saveBonusesConfig() {
        try {
            bonusesConfig.save(bonusesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
