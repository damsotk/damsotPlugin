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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

public class FactionManager implements CommandExecutor, Listener {
    private final DamsotPlugin plugin;
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final List<String> allowedFactions = List.of("Дамсот", "Ковенант", "Орден");

    public FactionManager(DamsotPlugin plugin) {
        this.plugin = plugin;
        loadFactionData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Участники фракции")) {
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
                loadedData.forEach((uuidString, faction) -> {
                    try {
                        playerFactions.put(UUID.fromString(uuidString), faction);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Неверный формат UUID в playerFaction.yml: " + uuidString);
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
        Map<String, String> dataToSave = playerFactions.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(dataToSave, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить playerFaction.yml");
        }
    }

    public void assignPlayerToFaction(Player player, String faction) {
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
}
