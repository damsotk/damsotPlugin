package com.damsotplugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

public class FactionManager implements CommandExecutor, Listener {

    private final DamsotPlugin plugin;
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final Map<String, String> capturePoints = new HashMap<>();
    private final Map<String, Location> capturePointLocations = new HashMap<>();
    private final List<String> allowedFactions = List.of("Дамсот", "Ковенант", "Орден");

    public FactionManager(DamsotPlugin plugin) {
        this.plugin = plugin;
        loadFactionData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        
        capturePoints.put("Заброшенные шахты", "Никто");
        capturePoints.put("Точка 2", "Никто");
        capturePoints.put("Точка 3", "Никто");

        
        capturePointLocations.put("Заброшенные шахты", new Location(Bukkit.getWorld("Arnhold"), 1173, 170, -557));
        capturePointLocations.put("Точка 2", new Location(Bukkit.getWorld("Arnhold"), 1173, 170, -555));
        capturePointLocations.put("Точка 3", new Location(Bukkit.getWorld("Arnhold"), 1173, 170, -553));
    }

    public void showFactionMenu(Player player) {
        String faction = playerFactions.get(player.getUniqueId());
        if (faction == null) {
            player.sendMessage("Вы не состоите ни в одной фракции.");
            return;
        }

        Inventory menu = Bukkit.createInventory(null, 27, "Фракция " + faction);

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = getFactionMembers(faction);
            meta.setDisplayName("Участники " + faction);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        menu.addItem(item);

        
        for (Map.Entry<String, String> entry : capturePoints.entrySet()) {
            ItemStack pointItem = new ItemStack(Material.RED_BANNER);
            ItemMeta pointMeta = pointItem.getItemMeta();
            if (pointMeta != null) {
                pointMeta.setDisplayName(entry.getKey());
                pointMeta.setLore(List.of("Владелец: " + entry.getValue()));
                pointItem.setItemMeta(pointMeta);
            }
            menu.addItem(pointItem);
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String faction = playerFactions.get(playerId);
    
        
        if (faction == null) {
            return;
        }
    
        
        Location playerLocation = player.getLocation();
    
        
        for (Map.Entry<String, Location> entry : capturePointLocations.entrySet()) {
            String pointName = entry.getKey();
            Location pointLocation = entry.getValue();
    
            
            if (playerLocation.getWorld().equals(pointLocation.getWorld()) &&
                playerLocation.distance(pointLocation) < 1.0) {  
    
                
                if (!capturePoints.get(pointName).equals(faction)) {
                    capturePoints.put(pointName, faction);
                    Bukkit.broadcastMessage("Точка " + pointName + " захвачена фракцией " + faction + "!");
                    plugin.getLogger().info("Точка " + pointName + " захвачена фракцией " + faction);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Фракция")) {
            event.setCancelled(true);
        }
    }

    private List<String> getFactionMembers(String faction) {
        return playerFactions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(faction))
                .map(entry -> Bukkit.getOfflinePlayer(entry.getKey()).getName())
                .collect(Collectors.toList());
    }

    private void loadFactionData() {
        File file = new File(plugin.getDataFolder(), "playerFaction.yml");
        if (!file.exists()) {
            saveDefaultFactionData(file);
        }
        try (FileReader reader = new FileReader(file)) {
            Yaml yaml = new Yaml();
            Map<String, String> loadedData = yaml.load(reader);
            if (loadedData != null) {
                loadedData.forEach((playerName, faction) -> {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName); 
                    if (player != null && player.getUniqueId() != null) {
                        playerFactions.put(player.getUniqueId(), faction);
                    } else {
                        plugin.getLogger().warning("Неверное имя игрока в playerFaction.yml: " + playerName);
                    }
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось загрузить playerFaction.yml");
        }
    }

    private void saveDefaultFactionData(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("PlayerUUID: Дамсот\n");
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить данные по умолчанию");
        }
    }

    public void saveFactionData() {
        File file = new File(plugin.getDataFolder(), "playerFaction.yml");
        Yaml yaml = new Yaml();
        Map<String, String> dataToSave = new HashMap<>();

        for (Map.Entry<UUID, String> entry : playerFactions.entrySet()) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (playerName != null) {
                dataToSave.put(playerName, entry.getValue());
            } else {
                plugin.getLogger().warning("Не удалось найти имя игрока для UUID: " + entry.getKey());
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(dataToSave, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить playerFaction.yml");
        }
    }

    public void assignPlayerToFaction(Player player, String faction) {
        if (playerFactions.containsKey(player.getUniqueId())) {
            plugin.getLogger().info("Игрок " + player.getName() + " сменил фракцию.");
        }
        playerFactions.put(player.getUniqueId(), faction);
        saveFactionData();
    }

    public String getPlayerFaction(UUID playerId) {
        return playerFactions.get(playerId);
    }

    public void removePlayerFromFaction(UUID playerId) {
        playerFactions.remove(playerId);
        saveFactionData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fracadd")) {
            if (args.length < 2) {
                sender.sendMessage("Использование: /fracadd <ник> <название_фракции>");
                return true;
            }

            String playerName = args[0];
            String faction = args[1];

            if (!allowedFactions.contains(faction)) {
                sender.sendMessage("Неверное название фракции. Разрешенные фракции: Дамсот, Ковенант, Орден.");
                return true;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("Игрок не в сети.");
                return true;
            }

            assignPlayerToFaction(target, faction);
            sender.sendMessage("Игрок " + playerName + " добавлен во фракцию " + faction + ".");
            target.sendMessage("Вы были добавлены во фракцию " + faction + ".");
            return true;
        } else if (command.getName().equalsIgnoreCase("fracmenu") && sender instanceof Player) {
            showFactionMenu((Player) sender);
            return true;
        }
        return false;
    }

    public void capturePoint(String pointName, String faction) {
        if (capturePoints.containsKey(pointName)) {
            capturePoints.put(pointName, faction);
            plugin.getLogger().info("Точка " + pointName + " захвачена фракцией " + faction);
        } else {
            plugin.getLogger().warning("Точка с именем " + pointName + " не найдена.");
        }
    }

}