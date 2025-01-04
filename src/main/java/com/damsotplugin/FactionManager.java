package com.damsotplugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

import io.th0rgal.oraxen.api.OraxenItems;
import me.clip.placeholderapi.PlaceholderAPI;

public class FactionManager implements CommandExecutor, Listener {

    /////////////////////////////////////////////////////
    private final DamsotPlugin plugin;
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final Map<String, String> capturePoints = new HashMap<>();
    private final Map<String, Location> capturePointLocations = new HashMap<>();
    private final Map<String, Map<String, Integer>> pointTreasuries = new HashMap<>();
    private final Map<String, Material> pointResources = new HashMap<>();

    private final List<String> allowedFactions = List.of("Кагорта", "Ковенант", "Орден", "Отрекшиеся", "Рогороссо");

    public FactionManager(DamsotPlugin plugin) {
        this.plugin = plugin;
        loadFactionData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        initializeCapturePoints();
        startResourceProductionTask();
    }

    private void initializeCapturePoints() {

        if (capturePoints.isEmpty()) {
            capturePoints.put("Заброшенная шахта", "Никто");
            capturePoints.put("Неизвестная деревня", "Никто");
            capturePoints.put("Росток", "Никто");
        }

        capturePointLocations.put("Заброшенная шахта", new Location(Bukkit.getWorld("Arnhold"), -1618, 23, 1499));
        capturePointLocations.put("Неизвестная деревня", new Location(Bukkit.getWorld("Arnhold"), -510, 18, 2325));
        capturePointLocations.put("Росток", new Location(Bukkit.getWorld("Arnhold"), -1257, 128, 1601));

        pointResources.put("Заброшенная шахта", Material.IRON_INGOT);
        pointResources.put("Неизвестная деревня", Material.BOOK);
        pointResources.put("Росток", Material.DIAMOND);

        for (String point : capturePoints.keySet()) {
            pointTreasuries.put(point, new HashMap<>());
        }
    }

    public void showFactionMenu(Player player) {
        String faction = playerFactions.get(player.getUniqueId());
        if (faction == null) {
            player.sendMessage("Вы не состоите ни в одной фракции.");
            return;
        }
        String title;
        switch (faction) {
            case "Орден":
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_orden_menu%");
                break;
            case "Когорта":
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_kogorta_menu%");
                break;
            case "Ковенант":
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_kovenant_menu%");
                break;
            case "Отрекшиеся":
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_otrek_menu%");
                break;
            case "Рогороссо":
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_rogoroso_menu%");
                break;
            default:
                title = PlaceholderAPI.setPlaceholders(player, "&f<shift:-14>%oraxen_default_menu%");
                break;
        }

        Inventory menu = Bukkit.createInventory(null, 54, title);

        ItemStack topPlayersItem = OraxenItems.getItemById("icon_razlom").build();
        ItemMeta topPlayersMeta = topPlayersItem.getItemMeta();
        if (topPlayersMeta != null) {
            topPlayersMeta.setDisplayName("§fСписок закрывающих разломы");
            List<String> topPlayersLore = new ArrayList<>();
            File topPlayersFile = new File(plugin.getDataFolder(), "listOfTopPlayers.yml");

            if (topPlayersFile.exists()) {
                try (FileReader reader = new FileReader(topPlayersFile)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(reader);

                    if (data != null && data.containsKey("closedRiftsByPlayer")) {
                        Object nestedData = data.get("closedRiftsByPlayer");

                        if (nestedData instanceof Map) {
                            Map<String, Object> topPlayersData = (Map<String, Object>) nestedData;
                            final int[] rank = {1};

                            topPlayersData.entrySet().stream()
                                    .sorted((e1, e2) -> {
                                        try {
                                            int value1 = Integer.parseInt(e1.getValue().toString());
                                            int value2 = Integer.parseInt(e2.getValue().toString());
                                            return Integer.compare(value2, value1);
                                        } catch (NumberFormatException ex) {
                                            return 0;
                                        }
                                    })
                                    .limit(10)
                                    .forEach(entry -> {
                                        try {
                                            String playerName = entry.getKey();
                                            int value = Integer.parseInt(entry.getValue().toString());
                                            topPlayersLore.add("§8" + rank[0] + ". §7" + playerName + " - §8" + value);
                                            rank[0]++;
                                        } catch (NumberFormatException ex) {
                                            plugin.getLogger().warning("Некорректные данные для игрока: " + entry);
                                        }
                                    });
                        } else {
                            topPlayersLore.add("§cНеверный формат данных в секции closedRiftsByPlayer.");
                        }
                    } else {
                        topPlayersLore.add("§cНет данных для отображения.");
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Не удалось загрузить файл listOfTopPlayers.yml");
                    topPlayersLore.add("§cОшибка при загрузке данных.");
                } catch (Exception e) {
                    plugin.getLogger().warning("Неизвестная ошибка при обработке файла: " + e.getMessage());
                    topPlayersLore.add("§cОшибка при обработке данных.");
                }
            } else {
                topPlayersLore.add("§cФайл с данными отсутствует.");
            }

            topPlayersMeta.setLore(topPlayersLore);
            topPlayersItem.setItemMeta(topPlayersMeta);
        }

        menu.setItem(37, topPlayersItem);

        ItemStack membersItem = OraxenItems.getItemById("icon_people").build();
        ItemMeta membersMeta = membersItem.getItemMeta();
        if (membersMeta != null) {
            List<String> lore = getFactionMembers(faction);

            List<String> coloredLore = new ArrayList<>();
            for (int i = 0; i < lore.size(); i++) {
                if (i % 2 == 0) {
                    coloredLore.add("§8○ " + lore.get(i));
                } else {
                    coloredLore.add("§7○ " + lore.get(i));
                }
            }

            membersMeta.setDisplayName("§fУчастники " + faction);
            membersMeta.setLore(coloredLore);
            membersItem.setItemMeta(membersMeta);
        }
        menu.setItem(43, membersItem);

        int i = 48;
        for (Map.Entry<String, String> entry : capturePoints.entrySet()) {
            String pointName = entry.getKey();
            String ownerFaction = entry.getValue();

            
            String iconId = "icon_avanpost" + (i - 48 + 1); 
            ItemStack pointItem = OraxenItems.getItemById(iconId).build(); 

            ItemMeta pointMeta = pointItem.getItemMeta();
            if (pointMeta != null) {
                pointMeta.setDisplayName("§f" + pointName);

                List<String> lore = new ArrayList<>();

                lore.add("§7Владелец: " + ownerFaction);

                
                switch (pointName) {
                    case "Заброшенная шахта":
                        lore.add("§8Когда-то здесь копали ихор");
                        break;
                    case "Неизвестная деревня":
                        lore.add("§8Когда-то здесь жило много мудрецов");
                        break;
                    case "Росток":
                        lore.add("§8Кажется дерево наполнено магией");
                        break;
                    default:
                        lore.add("§8Ресурс: Неизвестно");
                        break;
                }

                
                lore.add("§7Кол-во данного ресурса " + pointTreasuries.get(pointName).getOrDefault(faction, 0));

                pointMeta.setLore(lore);
                pointItem.setItemMeta(pointMeta);
            }

            menu.setItem(i, pointItem);
            i++;
        }

        ItemStack menuCommandItem = OraxenItems.getItemById("icon_back").build();

        ItemMeta menuCommandMeta = menuCommandItem.getItemMeta();
        if (menuCommandMeta != null) {
            menuCommandMeta.setDisplayName("§7Вернуться обратно в меню");
            menuCommandItem.setItemMeta(menuCommandMeta);
        }

        
        menu.setItem(8, menuCommandItem);

        player.openInventory(menu);
    }

    private final Map<UUID, String> playerOnPoint = new HashMap<>();
    private final Map<String, Long> playerTimeOnPoint = new HashMap<>();
    private final Map<UUID, Long> lastNotificationTime = new HashMap<>();
    private final long CAPTURE_TIME = 10 * 60 * 1000;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location playerLocation = player.getLocation();
        String faction = playerFactions.get(playerId);

        if (faction == null) {
            return;
        }

        for (Map.Entry<String, Location> entry : capturePointLocations.entrySet()) {
            String pointName = entry.getKey();
            Location pointLocation = entry.getValue();
            String ownerFaction = capturePoints.get(pointName); 

            
            if (playerLocation.getWorld().equals(pointLocation.getWorld())
                    && playerLocation.distance(pointLocation) < 9.0) {

                
                if (faction.equals(ownerFaction)) {
                    return;
                }

                if (!playerOnPoint.containsKey(playerId) || !playerOnPoint.get(playerId).equals(pointName)) {
                    playerOnPoint.put(playerId, pointName);
                    playerTimeOnPoint.put(playerId.toString(), System.currentTimeMillis());
                    lastNotificationTime.put(playerId, 0L);
                    Bukkit.broadcastMessage("§7[§8Троица§7] §7" + "Кто-то пытается захватить точку " + pointName + "!");
                }
            } else if (playerOnPoint.containsKey(playerId) && playerOnPoint.get(playerId).equals(pointName)) {
                playerOnPoint.remove(playerId);
                playerTimeOnPoint.remove(playerId.toString());
                lastNotificationTime.remove(playerId);
                player.sendMessage("§7[§8Троица§7] Вы покинули точку " + pointName + ".");
            }
        }
    }

    public void startCaptureCheckTask() {
        Bukkit.getLogger().info("Запущена задача проверки захвата точек.");
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<UUID, String> entry : playerOnPoint.entrySet()) {
                UUID playerId = entry.getKey();
                String pointName = entry.getValue();
                Player player = Bukkit.getPlayer(playerId);

                if (player != null && player.isOnline() && playerTimeOnPoint.containsKey(playerId.toString())) {
                    long timeSpent = currentTime - playerTimeOnPoint.get(playerId.toString());
                    long timeRemaining = CAPTURE_TIME - timeSpent;

                    if (timeRemaining <= 0) {
                        // Игрок успешно захватил точку
                        String faction = playerFactions.get(playerId);

                        if (faction != null && !capturePoints.get(pointName).equals(faction)) {
                            capturePoints.put(pointName, faction);
                            Bukkit.broadcastMessage("§7[§8Троица§7] §7Аванпост " + pointName + " §7захвачена фракцией " + faction + "!");
                            plugin.getLogger().info("Точка " + pointName + " захвачена фракцией " + faction);
                            saveFactionData();
                        }

                        playerOnPoint.remove(playerId);
                        playerTimeOnPoint.remove(playerId.toString());
                        lastNotificationTime.remove(playerId);
                    } else {
                        long lastNotify = lastNotificationTime.getOrDefault(playerId, 0L);
                        if (currentTime - lastNotify >= 5000) {
                            int minutes = (int) (timeRemaining / 60000);
                            int seconds = (int) ((timeRemaining % 60000) / 1000);
                            player.sendMessage("§7[§8Троица§7] §7Осталось времени до захвата: " + String.format("%02d:%02d", minutes, seconds));
                            lastNotificationTime.put(playerId, currentTime);
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    private void startResourceProductionTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            capturePoints.forEach((point, faction) -> {
                if (!faction.equals("Никто")) {
                    Material resource = pointResources.get(point);
                    pointTreasuries.get(point).put(faction,
                            pointTreasuries.get(point).getOrDefault(faction, 0) + 1);
                    saveFactionData();
                }
            });
        }, 0L, 24000L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("ꐐ") || title.contains("ꐑ") || title.contains("ꐒ") || title.contains("ꐓ") || title.contains("ꐔ")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null) {
                Player player = (Player) event.getWhoClicked();
                String faction = playerFactions.get(player.getUniqueId());

                if (faction == null) {
                    return;
                }

                String pointName = getPointNameFromItem(clickedItem);

                
                if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                    String itemName = clickedItem.getItemMeta().getDisplayName();
                    if (itemName.equals("§7Вернуться обратно в меню")) {
                        
                        player.chat("/menu");
                        return;
                    }
                }

                if (pointName != null && !removeFormatting(pointName).equals("Неизвестная деревня") && pointResources.containsKey(pointName)) {
                    String formattedPointName = removeFormatting(pointName);  
                    int resourceAmount = pointTreasuries.get(formattedPointName).getOrDefault(faction, 0);
                    if (resourceAmount > 0) {
                        pointTreasuries.get(formattedPointName).put(faction, 0);
                        player.getInventory().addItem(new ItemStack(pointResources.get(pointName), resourceAmount));
                        player.sendMessage("Вы забрали ресурсы из сокровищницы " + pointName + ".");
                    } else {
                        player.sendMessage("Сокровищница точки пуста.");
                    }
                } else if (pointName != null && removeFormatting(pointName).equals("Росток")) {
                    String formattedPointName = removeFormatting(pointName);  
                    int resourceAmount = pointTreasuries.get(formattedPointName).getOrDefault(faction, 0);
                    if (resourceAmount > 0) {
                        pointTreasuries.get(formattedPointName).put(faction, 0);
                        for (int i = 0; i < resourceAmount; i++) {
                            player.getInventory().addItem(generateRandomEnchantedBook());  
                        }
                        player.sendMessage("Вы забрали зачарованные книги из сокровищницы " + pointName + ".");
                    } else {
                        player.sendMessage("Сокровищница точки пуста.");
                    }
                } else if (pointName != null && removeFormatting(pointName).equals("Неизвестная деревня")) {
                    String formattedPointName = removeFormatting(pointName);  
                    int resourceAmount = pointTreasuries.get(formattedPointName).getOrDefault(faction, 0);
                    if (resourceAmount > 0) {
                        pointTreasuries.get(formattedPointName).put(faction, 0);
                        for (int i = 0; i < resourceAmount; i++) {
                            player.getInventory().addItem(generateRandomBook());
                        }
                        player.sendMessage("Вы забрали книги из сокровищницы " + pointName + ".");
                    } else {
                        player.sendMessage("Сокровищница точки пуста.");
                    }
                }
            }
        }
    }

    private String removeFormatting(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");  
    }

    private ItemStack generateRandomBook() {
        double random = Math.random();
        String title = "&7Обычныя свиток";
        String lore2 = " ";
        String lore3 = "&7&lМагический предмет";
        String lore4 = "&7Для обмена у торговца";

        if (random < 0.7) {
            title = "&7Обычныя свиток";
        } else if (random < 0.9) {
            title = "&5Редкий свиток";
        } else {
            title = "&6Легендарный свиток";
        }

        
        title = ChatColor.translateAlternateColorCodes('&', title);
        lore2 = ChatColor.translateAlternateColorCodes('&', lore2);
        lore3 = ChatColor.translateAlternateColorCodes('&', lore3);
        lore4 = ChatColor.translateAlternateColorCodes('&', lore4);

       
        ItemStack book = new ItemStack(Material.MOJANG_BANNER_PATTERN);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(title);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore2);
            loreList.add(lore3);
            loreList.add(lore4);
            meta.setLore(loreList);
            book.setItemMeta(meta);
        }

        return book;
    }

    private ItemStack generateRandomEnchantedBook() {
        
        Enchantment[] enchantments = Enchantment.values();
        Random random = new Random();
        Enchantment randomEnchantment = enchantments[random.nextInt(enchantments.length)];
        int level = random.nextInt(randomEnchantment.getMaxLevel()) + 1;

        
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.addEnchant(randomEnchantment, level, true);  
            book.setItemMeta(meta);
        }

        return book;
    }

    private String getPointNameFromItem(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return null;
    }

    private boolean isPlayerAllowedToTakeIron(Player player) {
        String playerName = player.getName().toLowerCase();
        return playerName.equals("damsot") || playerName.equals("bn_1") || playerName.equals("arnhold01");
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
            Map<String, Object> data = yaml.load(reader);
            if (data != null) {

                Map<String, String> loadedFactions = (Map<String, String>) data.get("playerFactions");
                if (loadedFactions != null) {
                    loadedFactions.forEach((playerName, faction) -> {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                        if (player != null && player.getUniqueId() != null) {
                            playerFactions.put(player.getUniqueId(), faction);
                        }
                    });
                }

                Map<String, String> loadedPoints = (Map<String, String>) data.get("capturePoints");
                if (loadedPoints != null) {
                    capturePoints.putAll(loadedPoints);
                }

                Map<String, Map<String, Integer>> loadedTreasuries = (Map<String, Map<String, Integer>>) data.get("pointTreasuries");
                if (loadedTreasuries != null) {
                    pointTreasuries.putAll(loadedTreasuries);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось загрузить playerFaction.yml");
        }
    }

    private void saveDefaultFactionData(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("playerFactions: {}\n");
            writer.write("capturePoints: {Заброшенная шахта: 'Никто', Неизвестная деревня: 'Никто', Росток: 'Никто'}\n");
            writer.write("pointTreasuries: {}\n");
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить данные по умолчанию");
        }
    }

    public void saveFactionData() {
        File file = new File(plugin.getDataFolder(), "playerFaction.yml");
        Yaml yaml = new Yaml();

        Map<String, Object> dataToSave = new HashMap<>();
        Map<String, String> playerFactionData = new HashMap<>();
        playerFactions.forEach((uuid, faction) -> {
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName != null) {
                playerFactionData.put(playerName, faction);
            }
        });

        dataToSave.put("playerFactions", playerFactionData);
        dataToSave.put("capturePoints", capturePoints);
        dataToSave.put("pointTreasuries", pointTreasuries);

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
                sender.sendMessage("Неверное название фракции. Разрешенные фракции: " + allowedFactions);
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
