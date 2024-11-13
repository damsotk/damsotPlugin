package com.damsotplugin;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class RiftManager {
    private final DamsotPlugin plugin;

    public RiftManager(DamsotPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnRift(CommandSender sender) {
        World world = Bukkit.getWorld("Arnhold");
        if (world != null) {
            Location[] locations = {
                new Location(world, 300, 80, -200),
                new Location(world, 590, 94, -367),
                new Location(world, 359, 63, 208),
                new Location(world, 241, 79, 395)
            };

            Random random = new Random();
            Location selectedLocation = locations[random.nextInt(locations.length)];

            createRift(selectedLocation);

            String message = "§6[§r§fКовенант§r§6]§r §6Кошмары вырвались наружу! Разлом замечен на§r §6"
                    + selectedLocation.getBlockX() + " "
                    + selectedLocation.getBlockY() + " "
                    + selectedLocation.getBlockZ();
            Bukkit.broadcastMessage(message);

            startMobWaves(selectedLocation);
        }
    }

    public void createRift(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.getBlockAt(location.clone().add(0, 0, 0)).setType(Material.RED_WOOL);
            world.getBlockAt(location.clone().add(1, 0, 0)).setType(Material.RED_WOOL);
            world.getBlockAt(location.clone().add(-1, 0, 0)).setType(Material.RED_WOOL);
            world.getBlockAt(location.clone().add(0, 0, 1)).setType(Material.RED_WOOL);
            world.getBlockAt(location.clone().add(0, 0, -1)).setType(Material.RED_WOOL);
        }
    }

    public void spawnCustomMob(Location location, int amount) {
        String command = "mm mobs spawn Forester " + amount;
        String commandWithLocation = command + " " +  location.getWorld().getName() + ","
                + location.getBlockX() + ","
                + (location.getBlockY() - 1) + ","
                + location.getBlockZ();

        Bukkit.getLogger().info("command to try: " + commandWithLocation);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandWithLocation);
    }

    public void startMobWaves(Location riftLocation) {
        spawnCustomMob(riftLocation, 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnCustomMob(riftLocation, 10);
            }
        }.runTaskLater(plugin, 5 * 60 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                String stabilizationMessage = "§6[§r§fКовенант§r§6]§r §6Разлом на координатах: "
                        + riftLocation.getBlockX() + " "
                        + riftLocation.getBlockY() + " "
                        + riftLocation.getBlockZ() + " стабилизировался";
                Bukkit.broadcastMessage(stabilizationMessage);
            }
        }.runTaskLater(plugin, 10 * 60 * 20);
    }
}
