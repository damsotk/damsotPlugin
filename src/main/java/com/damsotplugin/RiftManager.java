package com.damsotplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

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

    public void spawnTestSchematic(Player player) {
        File schematicFile = new File(plugin.getDataFolder(), "close_razlom1.schem");
        if (!schematicFile.exists()) {
            player.sendMessage("§cСхематик 'close_razlom1.schem' не знайдено!");
            return;
        }
    
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
        com.sk89q.worldedit.math.BlockVector3 playerLocation = BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint();
    
        try (FileInputStream fis = new FileInputStream(schematicFile); ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(fis)) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                BlockVector3 pasteLocation = playerLocation.add(1, 0, 1);
                player.sendMessage("§aСхематик вставляється на координати: "
                    + pasteLocation.getX() + ", "
                    + pasteLocation.getY() + ", "
                    + pasteLocation.getZ());
    
                Operations.complete(holder.createPaste(editSession)
                        .to(pasteLocation)
                        .ignoreAirBlocks(false)
                        .build());
                player.sendMessage("§aСхематик успішно завантажено!");
            }
        } catch (IOException e) {
            player.sendMessage("§cПомилка читання файлу схематичного.");
            e.printStackTrace();
        } catch (WorldEditException e) {
            player.sendMessage("§cПомилка вставки схематичного.");
            e.printStackTrace();
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
        String commandWithLocation = command + " " + location.getWorld().getName() + ","
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
