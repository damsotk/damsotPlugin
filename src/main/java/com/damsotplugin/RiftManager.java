package com.damsotplugin;

import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;

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

            spawnSchematic(selectedLocation, "wave1.schem");

            String message = "§6[§r§fКовенант§r§6]§r §6Кошмары вырвались наружу! Разлом замечен на§r §6"
                    + selectedLocation.getBlockX() + " "
                    + selectedLocation.getBlockY() + " "
                    + selectedLocation.getBlockZ();
            Bukkit.broadcastMessage(message);

            startMobWaves(selectedLocation);
        }
    }

    public void spawnSchematic(Location location, String schematicName) {
        File schematicFile = new File(plugin.getDataFolder(), schematicName);
        if (!schematicFile.exists()) {
            Bukkit.getLogger().warning("Схематика " + schematicName + " не найдена.");
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            Bukkit.getLogger().warning("Неверный формат схематики: " + schematicName);
            return;
        }

        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            Clipboard clipboard = reader.read();
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(location.getWorld());
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                holder.createPaste(editSession)
                        .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                        .build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void spawnCustomMob(Location location, int amount) {
        String command = "mm mobs spawn Forester " + amount;
        String commandWithLocation = command + " " + location.getWorld().getName() + ","
                + location.getBlockX() + ","
                + (location.getBlockY() - 1) + ","
                + location.getBlockZ();

        Bukkit.getLogger().info("command to try: " + commandWithLocation);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandWithLocation);
    }

    public void startMobWaves(Location riftLocation) {
        spawnCustomMob(riftLocation, 2);
        spawnSchematic(riftLocation, "wave1.schem");

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnCustomMob(riftLocation, 10);
                spawnSchematic(riftLocation, "wave2.schem");
            }
        }.runTaskLater(plugin, 5 * 60 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnCustomMob(riftLocation, 15);
                spawnSchematic(riftLocation, "wave3.schem");
            }
        }.runTaskLater(plugin, 7 * 60 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnCustomMob(riftLocation, 20);
                spawnSchematic(riftLocation, "wave4.schem");
                applyWeaknessEffect(riftLocation, 30, 1);
            }
        }.runTaskLater(plugin, 9 * 60 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnCustomMob(riftLocation, 30);
                spawnSchematic(riftLocation, "wave5.schem");
                applyWeaknessEffect(riftLocation, 60, 2);

                String stabilizationMessage = "§6[§r§fКовенант§r§6]§r §6Разлом на координатах: "
                        + riftLocation.getBlockX() + " "
                        + riftLocation.getBlockY() + " "
                        + riftLocation.getBlockZ() + " стабилизировался";
                Bukkit.broadcastMessage(stabilizationMessage);
            }
        }.runTaskLater(plugin, 10 * 60 * 20);
    }

    private void applyWeaknessEffect(Location center, int duration, int amplifier) {
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= 50) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration * 20, amplifier));
            }
        }
    }
}
