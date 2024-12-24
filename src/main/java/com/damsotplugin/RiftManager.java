package com.damsotplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
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
    private final Map<String, RiftData> riftDataMap;
    private final Map<UUID, Long> playerStayTime;

    public RiftManager(DamsotPlugin plugin) {
        this.plugin = plugin;
        this.riftDataMap = new HashMap<>();
        this.playerStayTime = new HashMap<>();
        initializeRiftData();
    }

    private void initializeRiftData() {
        riftDataMap.put("Rift1", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), 476, 61, -756),
            BlockVector3.at(476, 61, -756),
            BlockVector3.at(485, 66, -755)
        ));
        riftDataMap.put("Rift2", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), 474, 45, -206),
            BlockVector3.at(474, 45, -206),
            BlockVector3.at(471, 50, -207)
        ));
        riftDataMap.put("Rift3", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), 467, 49, 536),
            BlockVector3.at(467, 49, 536),
            BlockVector3.at(479, 51, 548)
        ));
        riftDataMap.put("Rift4", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), 164, 54, 104),
            BlockVector3.at(164, 54, 104),
            BlockVector3.at(161, 64, 103)
        ));
        riftDataMap.put("Rift5", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), -759, 47, 163),
            BlockVector3.at(-759, 47, 163),
            BlockVector3.at(-757, 52, 158)
        ));
        riftDataMap.put("Rift6", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), -1130, 52, -265),
            BlockVector3.at(-1130, 52, -265),
            BlockVector3.at(-1129, 59, -265)
        ));
        riftDataMap.put("Rift7", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), -87, 60, -332),
            BlockVector3.at(-87, 60, -332),
            BlockVector3.at(-82, 62, -336)
        ));
        riftDataMap.put("Rift8", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), -633, 50, -136),
            BlockVector3.at(-633, 50, -136),
            BlockVector3.at(-640, 60, -135)
        ));
        riftDataMap.put("Rift9", new RiftData(
            new Location(Bukkit.getWorld("Arnhold"), -581, 47, -838),
            BlockVector3.at(-581, 47, -838),
            BlockVector3.at(-585, 54, -835)
        ));
    }

    public void toggleRift(String riftName, boolean open, Player player) {
        RiftData riftData = riftDataMap.get(riftName);
        if (riftData == null) {
            player.sendMessage("§cРазлом с именем " + riftName + " не найден!");
            return;
        }
    
        String schematicFileName = open ? ("open_" + riftName.toLowerCase() + ".schem") : ("close_" + riftName.toLowerCase() + ".schem");
        File schematicFile = new File(plugin.getDataFolder(), schematicFileName);
    
        if (!schematicFile.exists()) {
            player.sendMessage("§cСхематик '" + schematicFileName + "' не найден!");
            return;
        }
    
        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(fis)) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(riftData.getLocation().getWorld()))) {
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                Operations.complete(holder.createPaste(editSession)
                        .to(open ? riftData.getOpenPasteLocation() : riftData.getClosePasteLocation())
                        .ignoreAirBlocks(false)
                        .build());
    
                String action = open ? "открыт" : "закрыт";
                player.sendMessage("§aРазлом '" + riftName + "' был успешно " + action + "!");

                if (open) {
                    spawnParticles(riftData.getLocation());
                }
            }
        } catch (IOException | WorldEditException e) {
            player.sendMessage("§cОшибка обработки разлома.");
            e.printStackTrace();
        }
    }

    private void spawnParticles(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 1, 1, 1, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }



    private void teleportNearbyPlayers(Location center, int radius, int yOffset) {
        center.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distance(center) <= radius)
            .forEach(player -> {
                Location newLocation = player.getLocation().clone().add(0, yOffset, 0);
                player.teleport(newLocation);
                player.sendMessage("§eВас телепортировало из зоны разлома!");
                plugin.getLogger().info("Игрок " + player.getName() + " телепортирован на " + newLocation.toString());
            });
    }

    public void monitorPlayersInRift(String riftName) {
        RiftData riftData = riftDataMap.get(riftName);
        if (riftData == null) {
            plugin.getLogger().warning("Разлом с именем " + riftName + " не найден!");
            return;
        }
    
        Map<UUID, Long> localPlayerStayTime = new HashMap<>(); 
    
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location playerLocation = player.getLocation();
                    Location riftLocation = riftData.getLocation();
    
                    double distance = playerLocation.distance(riftLocation);
    
                    if (distance <= 2.0) {
                        UUID playerUUID = player.getUniqueId();
                        if (!localPlayerStayTime.containsKey(playerUUID)) {
                            localPlayerStayTime.put(playerUUID, System.currentTimeMillis());
                            player.sendMessage("§eВы вошли в зону разлома. Начинается отсчёт времени...");
                            plugin.getLogger().info("Игрок " + player.getName() + " вошёл в зону разлома.");
                        } else {
                            long elapsedTime = System.currentTimeMillis() - localPlayerStayTime.get(playerUUID);
                            player.sendMessage("§eВы находитесь в зоне разлома " + (elapsedTime / 1000) + " секунд.");
                            plugin.getLogger().info("Игрок " + player.getName() + " находится в зоне разлома " + (elapsedTime / 1000) + " секунд.");
    
                            if (elapsedTime >= 5000) {
                                closeRift(riftName, player);
                                Bukkit.broadcastMessage(String.format("§f[Ковенант] Игрок §a%s §fзакрыл разлом '%s'!", player.getName(), riftName));
                                teleportNearbyPlayers(riftLocation, 20, 10); 
                                localPlayerStayTime.clear(); 
                                plugin.getLogger().info("Разлом " + riftName + " закрыт игроком " + player.getName() + ".");
                                cancel(); 
                            }
                        }
                    } else {
                        UUID playerUUID = player.getUniqueId();
                        if (localPlayerStayTime.containsKey(playerUUID)) {
                            localPlayerStayTime.remove(playerUUID);
                            player.sendMessage("§cВы покинули зону разлома. Таймер сброшен.");
                            plugin.getLogger().info("Игрок " + player.getName() + " покинул зону разлома.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); 
    }

    public void openRift(String riftName, Player player) {
        toggleRift(riftName, true, player);
        monitorPlayersInRift(riftName);
    }

    public void closeRift(String riftName, Player player) {
        toggleRift(riftName, false, player);
    }

    private static class RiftData {
        private final Location location;
        private final BlockVector3 openPasteLocation;
        private final BlockVector3 closePasteLocation;

        public RiftData(Location location, BlockVector3 openPasteLocation, BlockVector3 closePasteLocation) {
            this.location = location;
            this.openPasteLocation = openPasteLocation;
            this.closePasteLocation = closePasteLocation;
        }

        public Location getLocation() {
            return location;
        }

        public BlockVector3 getOpenPasteLocation() {
            return openPasteLocation;
        }

        public BlockVector3 getClosePasteLocation() {
            return closePasteLocation;
        }
    }
}