package com.damsotplugin;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.clip.placeholderapi.PlaceholderAPI;

public class MobSpawnAddon {

    private final HashMap<UUID, BukkitRunnable> playerTasks = new HashMap<>();
    private final Random random = new Random();

    public MobSpawnAddon() {
        startPlayerTrackingTask();
    }

    private void startPlayerTrackingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!playerTasks.containsKey(player.getUniqueId())) {
                        startPlayerSpawnTask(player);
                    }
                }
            }
        }.runTaskTimer(DamsotPlugin.getInstance(), 0L, 20L); 
    }

    private void startPlayerSpawnTask(Player player) {
        int intervalTicks = (random.nextInt(5) + 4) * 60 * 20; 
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                
                if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.ADVENTURE) {
                    return; 
                }
                
                String townName = PlaceholderAPI.setPlaceholders(player, "%townyadvanced_player_location_town_or_wildname%");
    
                if (townName == null || townName.isEmpty() || !"Независимые земли".equals(townName)) {
                    return; 
                }
    
                Location playerLocation = player.getLocation();
                String[] mobNames = {"Summus", "Volans", "Forester"};
                int[] mobChances = {65, 20, 15};
                int mobCount = random.nextInt(6) + 1; 
    
                for (int i = 0; i < mobCount; i++) {
                    String mobName = null;
                    try {
                        double xOffset, zOffset;
                        Location spawnLocation;
                        do {
                            xOffset = (random.nextDouble() * 40) - 20;
                            zOffset = (random.nextDouble() * 40) - 20;
                            spawnLocation = playerLocation.clone().add(xOffset, 0, zOffset);
                        } while (!isSafeSpawnLocation(spawnLocation));
    
                        
                        spawnLocation = adjustSpawnHeight(spawnLocation);
    
                        mobName = getRandomMobByChance(mobNames, mobChances, random);
                        Entity spawnedEntity = MythicBukkit.inst().getAPIHelper().spawnMythicMob(mobName, spawnLocation);
    
                        if (spawnedEntity != null) {
                            ActiveMob spawnedMob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(spawnedEntity);
                            if (spawnedMob != null) {
                                player.sendMessage("§aМоб " + mobName + " успешно заспавнен!");
                            }
                        }
                    } catch (InvalidMobTypeException e) {
                        e.printStackTrace();
                        player.sendMessage("§cОшибка: Неверный тип моба для: " + mobName);
                    }
                }
            }
        };
    
        task.runTaskTimer(DamsotPlugin.getInstance(), 0L, intervalTicks);
        playerTasks.put(player.getUniqueId(), task);
    }
    
    private boolean isSafeSpawnLocation(Location location) {
        
        return location.getBlock().isEmpty() && location.clone().add(0, 1, 0).getBlock().isEmpty();
    }
    
    private Location adjustSpawnHeight(Location location) {
        
        while (!location.getBlock().isEmpty() || !location.clone().add(0, 1, 0).getBlock().isEmpty()) {
            location.add(0, 1, 0);
            if (location.getY() > location.getWorld().getMaxHeight()) {
                break; 
            }
        }
        return location;
    }

    private String getRandomMobByChance(String[] mobNames, int[] mobChances, Random random) {
        int totalChance = 0;
        for (int chance : mobChances) {
            totalChance += chance;
        }

        int randomValue = random.nextInt(totalChance);
        int cumulativeChance = 0;

        for (int i = 0; i < mobNames.length; i++) {
            cumulativeChance += mobChances[i];
            if (randomValue < cumulativeChance) {
                return mobNames[i];
            }
        }

        return mobNames[mobNames.length - 1];
    }

    public void stopAllTasks() {
        for (BukkitRunnable task : playerTasks.values()) {
            task.cancel();
        }
        playerTasks.clear();
    }
}