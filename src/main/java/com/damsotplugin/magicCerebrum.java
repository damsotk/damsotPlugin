package com.damsotplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class magicCerebrum implements Listener {

    private final DamsotPlugin plugin;

    private final Map<UUID, Long> justiceCooldowns = new HashMap<>();
    private final Map<UUID, Long> prosvecticoCooldowns = new HashMap<>();
    private final Map<UUID, Long> reaktimCooldowns = new HashMap<>();
    private final Map<UUID, Long> sleptioCooldowns = new HashMap<>();
    private final Map<UUID, Long> krikCooldowns = new HashMap<>();
    private final Map<UUID, Long> travalCooldowns = new HashMap<>();
    private final Map<UUID, Long> uravniumCooldowns = new HashMap<>();
    private final Map<UUID, Long> polectumCooldowns = new HashMap<>();
    private final Map<UUID, Long> zashitnumCooldowns = new HashMap<>();
    private final Map<UUID, Long> regenioCooldowns = new HashMap<>();
    private final Map<UUID, Long> utopiticumCooldowns = new HashMap<>();
    private final Map<UUID, Long> kastremCooldowns = new HashMap<>();
    private final Map<UUID, Long> sumasvodCooldowns = new HashMap<>();
    private final Map<UUID, Long> nevidumsCooldowns = new HashMap<>();
    private final Map<UUID, Long> levetoCooldowns = new HashMap<>();
    private final Map<UUID, Long> zazhivgomCooldowns = new HashMap<>();
    private final Map<UUID, Long> svezhiumCooldowns = new HashMap<>();
    private final Map<UUID, Long> oktaniumCooldowns = new HashMap<>();
    private final Map<UUID, Long> reaktionCooldowns = new HashMap<>();
    private final Map<UUID, Long> neodoleoCooldowns = new HashMap<>();

    private final Map<String, Set<String>> learnedSpells = new HashMap<>();
    private final File dataFile;

    public magicCerebrum(DamsotPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "learned_spells.yml");
        loadLearnedSpells();
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
    
            if (item.getType() == Material.ENCHANTED_BOOK && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
    
                if (meta.hasDisplayName()) {
                    String spellName = extractSpellName(meta.getDisplayName());
    
                    if (!spellName.isEmpty()) {
                    
                        if (learnSpell(player, spellName)) {
                            
                            int newAmount = item.getAmount() - 1;
                            if (newAmount > 0) {
                                item.setAmount(newAmount);
                            } else {
                                player.getInventory().setItemInMainHand(null);
                            }
                            player.sendMessage("§aКнига заклинания использована.");
                        } else {
                            player.sendMessage("§cВы уже изучили это заклинание.");
                        }
                    } else {
                        player.sendMessage("§cЭто не заклинание для изучения.");
                    }
                }
            }
        }
    }

    private boolean isSpellLearned(Player player, String spellName) {
        String playerName = player.getName();
        String normalizedSpellName = spellName.trim().toLowerCase();

        System.out.println("Игрок: " + playerName + " пытается использовать заклинание: " + spellName);
        System.out.println("Заклинание в нижнем регистре и без пробелов: " + normalizedSpellName);

        if (learnedSpells.containsKey(playerName)) {
            System.out.println("Изученные заклинания игрока " + playerName + ": " + learnedSpells.get(playerName));
        } else {
            System.out.println("У игрока " + playerName + " нет изученных заклинаний.");
        }

        Set<String> learnedSpellsSet = learnedSpells.get(playerName);
        if (learnedSpellsSet != null) {

            Set<String> normalizedLearnedSpells = new HashSet<>();
            for (String spell : learnedSpellsSet) {
                normalizedLearnedSpells.add(spell.trim().toLowerCase());
            }

            System.out.println("Нормализованные изученные заклинания: " + normalizedLearnedSpells);

            boolean isLearned = normalizedLearnedSpells.contains(normalizedSpellName);
            System.out.println("Заклинание " + normalizedSpellName + " изучено: " + isLearned);
            return isLearned;
        }

        return false;
    }

    private void saveLearnedSpells() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Set<String>> entry : learnedSpells.entrySet()) {
            config.set(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLearnedSpells() {
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String playerName : config.getKeys(false)) {
            List<String> spells = config.getStringList(playerName);
            learnedSpells.put(playerName, new HashSet<>(spells));
        }
    }

    private String extractSpellName(String displayName) {
        if (displayName.startsWith("§8")) {
            return displayName.substring(2).trim();
        }
        return "";
    }

    private boolean learnSpell(Player player, String spellName) {
        String playerName = player.getName();
    
        if (learnedSpells.computeIfAbsent(playerName, k -> new HashSet<>()).contains(spellName)) {
            return false; 
        }
    
        learnedSpells.get(playerName).add(spellName);
        player.sendMessage("§aВы изучили заклинание: " + spellName);
        saveLearnedSpells();
        return true; 
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.PAPER && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§6Книга заклинаний")) {

                Set<String> learnedSpellsSet = learnedSpells.get(player.getName());
                System.out.println("Игрок " + player.getName() + " пытается использовать заклинание: " + message);
                System.out.println("Изученные заклинания игрока: " + (learnedSpellsSet != null ? learnedSpellsSet.toString() : "Нет изученных заклинаний"));

                if (isSpellLearned(player, message)) {
                    int resourceCost = 0;

                    switch (message) {
                        case "неодолео":
                            resourceCost = 25;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleNeodoleoSpell(player);
                            }
                            break;
                        case "октаниум":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleOktaniumSpell(player);
                            }
                            break;
                        case "реактио":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleReaktioSpell(player);
                            }
                            break;
                        case "заживгом":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleZazhivgomSpell(player);
                            }
                            break;
                        case "свежиум":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleSvezhiumSpell(player);
                            }
                            break;
                        case "слептио":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleSleptioSpell(player);
                            }
                            break;
                        case "крик":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleKrikSpell(player);
                            }
                            break;
                        case "джастис":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleJusticeSpell(player);
                            }
                            break;
                        case "просвектико":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleProsvecticoSpell(player);
                            }
                            break;
                        case "реактим":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleReaktimSpell(player);
                            }
                            break;
                        case "кастрем":
                            resourceCost = 25;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleKastremSpell(player);
                            }
                            break;
                        case "сумасвод":
                            resourceCost = 25;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleSumasvodSpell(player);
                            }
                            break;
                        case "травл":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleTravalSpell(player);
                            }
                            break;
                        case "левето":
                            resourceCost = 5;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleLevetoSpell(player);
                            }
                            break;
                        case "уравниум":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleUravniumSpell(player);
                            }
                            break;
                        case "полектум":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handlePolectumSpell(player);
                            }
                            break;
                        case "защитнум":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleZashitnumSpell(player);
                            }
                            break;
                        case "регенио":
                            resourceCost = 15;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleRegenioSpell(player);
                            }
                            break;
                        case "утоптикум":
                            resourceCost = 25;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleUtopiticumSpell(player);
                            }
                            break;
                        case "невидумс":
                            resourceCost = 25;
                            if (consumeResource(player, "§aИхор", resourceCost)) {
                                handleNevidumsSpell(player);
                            }
                            break;
                        default:
                            player.sendMessage("§cВы не знаете такого заклинания!");
                            break;
                    }
                } else {
                    player.sendMessage("§cВы ещё не изучили это заклинание.");
                }
            }
        }
    }

    private boolean consumeResource(Player player, String resourceName, int amount) {
        Inventory inventory = player.getInventory();
        int totalAmount = 0;

        // Сканируем инвентарь на наличие ресурса
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getItemMeta() != null && resourceName.equals(item.getItemMeta().getDisplayName())) {
                totalAmount += item.getAmount();
            }
        }

        // Проверяем, достаточно ли ресурса
        if (totalAmount >= amount) {
            int remaining = amount;

            // Удаляем нужное количество ресурса
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getItemMeta() != null && resourceName.equals(item.getItemMeta().getDisplayName())) {
                    if (item.getAmount() <= remaining) {
                        remaining -= item.getAmount();
                        inventory.remove(item);
                    } else {
                        item.setAmount(item.getAmount() - remaining);
                        break;
                    }
                }
            }
            return true;
        } else {
            player.sendMessage("§cУ вас недостаточно ресурса " + resourceName + " для использования заклинания.");
            return false;
        }
    }

    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, int cooldownSeconds) {
        long currentTime = System.currentTimeMillis();
        if (cooldownMap.containsKey(playerId)) {
            long cooldownEnd = cooldownMap.get(playerId);
            if (currentTime < cooldownEnd) {
                return true;
            }
        }

        cooldownMap.put(playerId, currentTime + (cooldownSeconds * 1000L));
        return false;
    }

    private String formatCooldownTime(long cooldownEnd) {
        long remainingTime = cooldownEnd - System.currentTimeMillis();
        long seconds = remainingTime / 1000 % 60;
        long minutes = (remainingTime / 1000 / 60) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private Player findNearestPlayer(Player player) {
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != player) {
                double distance = player.getLocation().distance(onlinePlayer.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = onlinePlayer;
                }
            }
        }
        return closestPlayer;
    }

    private void handleSleptioSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, sleptioCooldowns, 25)) {
            player.sendMessage("§cЗаклятие Слептио ещё восстанавливается!  Осталось:" + formatCooldownTime(sleptioCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0));
                    }
                }
                player.sendMessage("§6Все существа рядом ослепли!");
            }
        }.runTask(plugin);
    }

    private void handleKrikSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, krikCooldowns, 20)) {
            player.sendMessage("§cЗаклятие Крик ещё восстанавливается! Осталось: " + formatCooldownTime(krikCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                player.getWorld().spawnParticle(
                        org.bukkit.Particle.BLOCK_CRACK,
                        player.getLocation(),
                        10000,
                        5, 5, 5,
                        0.1,
                        org.bukkit.Material.MYCELIUM.createBlockData(),
                        true
                );

                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    }
                }
                player.sendMessage("§6Все существа в радиусе получили урон!");
            }
        }.runTask(plugin);
    }

    private void handleJusticeSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, justiceCooldowns, 20 * 60)) {
            player.sendMessage("§cЗаклятие Джастис ещё восстанавливается! Осталось: " + formatCooldownTime(justiceCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                double newHealth = player.getHealth() - 4;
                if (newHealth > 0) {
                    player.setHealth(newHealth);
                } else {
                    player.setHealth(0);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 9));
                player.sendMessage("§6Вы получили силу Джастиса!");
            }
        }.runTask(plugin);
    }

    private void handleProsvecticoSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, prosvecticoCooldowns, 8 * 60)) {
            player.sendMessage("§cЗаклятие Просвектико ещё восстанавливается! Осталось: " + formatCooldownTime(prosvecticoCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0));
                    }
                }
                player.sendMessage("§6Все существа рядом теперь видны!");
            }
        }.runTask(plugin);
    }

    private void handleReaktimSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, reaktimCooldowns, 90)) {
            player.sendMessage("§cЗаклятие Реактим ещё восстанавливается! Осталось: " + formatCooldownTime(reaktimCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 800, 2));
                player.sendMessage("§6Вы получили ускорение Реактим!");
            }
        }.runTask(plugin);
    }

    private void handleTravalSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, travalCooldowns, 25)) {
            player.sendMessage("§cЗаклятие Травл ещё восстанавливается! Осталось: " + formatCooldownTime(travalCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
                    if (entity instanceof LivingEntity && entity != player) {

                        entity.setVelocity(entity.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.5));
                    }
                }
                player.sendMessage("§6Все существа в радиусе были оттолкнуты!");
            }
        }.runTask(plugin);
    }

    private void handleUravniumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, uravniumCooldowns, 120)) {
            player.sendMessage("§cЗаклятие Уравниум ещё восстанавливается! Осталось: " + formatCooldownTime(uravniumCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                Player target = findNearestPlayer(player);
                if (target != null) {

                    if (target.getInventory().getItemInMainHand() != null) {
                        target.getInventory().setItemInMainHand(null);
                    }

                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 1));
                    player.sendMessage("§6Заклятие Уравниум использовано на " + target.getName() + "!");
                    target.sendMessage("§6На вас наложено заклятие Уравниум!");
                } else {
                    player.sendMessage("§cНету вокруг тебя путников :()");
                }
            }
        }.runTask(plugin);
    }

    private void handlePolectumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, polectumCooldowns, 1200)) {
            player.sendMessage("§cЗаклятие Полектум ещё восстанавливается! Осталось: " + formatCooldownTime(polectumCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack bow = new ItemStack(Material.BOW);
                ItemMeta bowMeta = bow.getItemMeta();
                bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 10, true);
                bowMeta.setUnbreakable(true);
                bowMeta.setDisplayName("§6Зачарованный лук");
                bow.setItemMeta(bowMeta);

                ItemStack arrows = new ItemStack(Material.ARROW, 10);
                player.getInventory().addItem(bow);
                player.getInventory().addItem(arrows);

                Bukkit.getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onPlayerDropItem(PlayerDropItemEvent event) {
                        if (event.getPlayer().equals(player) && event.getItemDrop().getItemStack().isSimilar(bow)) {
                            event.setCancelled(true);
                        }
                    }
                }, plugin);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getInventory().removeItem(bow);
                        player.getInventory().removeItem(new ItemStack(Material.ARROW, 10));
                        player.sendMessage("§6Твой лук пропал воооооу");
                    }
                }.runTaskLater(plugin, 200);

                player.sendMessage("§6Заклятие Полектум использовано! Вы получили лук!");
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (droppedItem.getType() == Material.BOW && droppedItem.getItemMeta() != null && "§6Лук Полектум".equals(droppedItem.getItemMeta().getDisplayName())) {
            player.sendMessage("§cТы не можешь выбросить Лук Полектум!");
            event.setCancelled(true);
        }
    }

    private void handleZashitnumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, zashitnumCooldowns, 240)) {
            player.sendMessage("§cЗаклятие Защитнум ещё восстанавливается! Осталось: " + formatCooldownTime(zashitnumCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof Player) {
                        LivingEntity target = (LivingEntity) entity;
                        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 700, 2));
                        player.sendMessage("§6На " + target.getName() + " наложено заклятие Защитнум!");
                    }
                }
            }
        }.runTask(plugin);
    }

    private void handleRegenioSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, regenioCooldowns, 90)) {
            player.sendMessage("§cЗаклятие Регенио ещё восстанавливается! Осталось: " + formatCooldownTime(regenioCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 240, 1));
                player.sendMessage("§6Вы получили регенерацию от заклятия Регенио!");
            }
        }.runTask(plugin);
    }

    private void handleUtopiticumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, utopiticumCooldowns, 30)) {
            player.sendMessage("§cЗаклятие Утоптикум ещё восстанавливается! Осталось: " + formatCooldownTime(utopiticumCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                List<LivingEntity> immobilizedEntities = new ArrayList<>();
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 10));
                        immobilizedEntities.add((LivingEntity) entity);
                    }
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 4));
                player.sendMessage("§6Все существа в радиусе обездвижены на 30 секунд!");

                if (!immobilizedEntities.isEmpty()) {
                    StringBuilder message = new StringBuilder("§6Обездвижены: ");
                    for (LivingEntity entity : immobilizedEntities) {
                        message.append(entity.getName()).append(", ");
                    }
                    player.sendMessage(message.substring(0, message.length() - 2));
                } else {
                    player.sendMessage("§cНет существ для обездвиживания в радиусе.");
                }
            }
        }.runTask(plugin);
    }

    private void handleKastremSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, kastremCooldowns, 600)) {
            player.sendMessage("§cЗаклятие Кастрем ещё восстанавливается! Осталось: " + formatCooldownTime(kastremCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 255, true, false));
                player.sendMessage("§6Вы стали неуязвимым!");
            }
        }.runTask(plugin);
    }

    private void handleSumasvodSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, sumasvodCooldowns, 600)) {
            player.sendMessage("§cЗаклятие Сумасвод ещё восстанавливается! Осталось: " + formatCooldownTime(sumasvodCooldowns.get(playerId)));
            return;
        }

        Player targetPlayer = findNearestPlayer(player);
        if (targetPlayer != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    double newHealth = targetPlayer.getHealth() - 12;
                    if (newHealth > 0) {
                        targetPlayer.setHealth(newHealth);
                    } else {
                        targetPlayer.setHealth(0);
                    }

                    targetPlayer.setFoodLevel(0);
                    player.sendMessage("§6Вы использовали заклятие Сумасвод на " + targetPlayer.getName() + " и он потерял 6 сердец!");
                    targetPlayer.sendMessage("§cНа вас использовали заклятие Сумасвод и вы потеряли 6 сердец!");
                }
            }.runTask(plugin);
        } else {
            player.sendMessage("§cНету ближайшего игрока для использования.");
        }
    }

    private void handleNevidumsSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, nevidumsCooldowns, 300)) {
            player.sendMessage("§cЗаклятие Невидумс ещё восстанавливается! Осталось: " + formatCooldownTime(nevidumsCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 0));

                double offsetX = (Math.random() * 60) - 30;
                double offsetZ = (Math.random() * 60) - 30;
                player.teleport(player.getLocation().add(offsetX, 0, offsetZ));

                player.sendMessage("§6Вы стали невидимым на 5 минут и телепортировались!");
            }
        }.runTask(plugin);

        nevidumsCooldowns.put(playerId, System.currentTimeMillis() + (300 * 1000L));
    }

    private void handleLevetoSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, levetoCooldowns, 60)) {
            player.sendMessage("§cЗаклятие Левето ещё восстанавливается! Осталось: " + formatCooldownTime(levetoCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 0));
                player.sendMessage("§6Вы получили левитацию на 10 секунд!");
            }
        }.runTask(plugin);
    }

    private void handleZazhivgomSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, zazhivgomCooldowns, 90)) {
            player.sendMessage("§cЗаклятие Заживгом ещё восстанавливается! Осталось: " + formatCooldownTime(zazhivgomCooldowns.get(playerId)));
            return;
        }
        Player nearestPlayer = findNearestPlayer(player);
        if (nearestPlayer == null) {
            player.sendMessage("§cРядом нет игроков для исцеления!");
            return;
        }

        double maxHealth = nearestPlayer.getMaxHealth();
        double healAmount = maxHealth * 0.20;
        double newHealth = Math.min(nearestPlayer.getHealth() + healAmount, maxHealth);
        nearestPlayer.setHealth(newHealth);

        nearestPlayer.sendMessage("§6Вас исцелил " + player + " на 20%!");
        player.sendMessage("§6Вы исцелили ближайшего игрока!");

        zazhivgomCooldowns.put(playerId, System.currentTimeMillis() + (90 * 1000L));
    }

    private void handleSvezhiumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, svezhiumCooldowns, 300)) {
            player.sendMessage("§cЗаклятие Свежиум ещё восстанавливается! Осталось: " + formatCooldownTime(svezhiumCooldowns.get(playerId)));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof Player) {
                        Player nearbyPlayer = (Player) entity;
                        nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 0));
                    }
                }
                player.sendMessage("§6Эффект насыщения вокруг активирован!");
            }
        }.runTask(plugin);

        svezhiumCooldowns.put(playerId, System.currentTimeMillis() + (300 * 1000L));
    }

    private void handleOktaniumSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, oktaniumCooldowns, 10 * 60)) {
            player.sendMessage("§cЗаклятие Октаниум ещё восстанавливается! Осталось: " + formatCooldownTime(oktaniumCooldowns.get(playerId)));
            return;
        }

        Player nearestPlayer = findNearestPlayer(player);

        if (nearestPlayer == null || player.getLocation().distance(nearestPlayer.getLocation()) > 10) {
            player.sendMessage("§cРядом нет игроков, заклятие не может быть активировано!");
            return;
        }

        nearestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 3 * 60 * 20, 0));
        nearestPlayer.sendMessage("§6Вы получили насыщение на 3 минуты!");

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 60 * 20, 0));
        player.sendMessage("§6Вам накладывается эффект ночного зрения на 2 минуты!");

        oktaniumCooldowns.put(playerId, System.currentTimeMillis());
    }

    private void handleReaktioSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, reaktionCooldowns, 10 * 60)) {
            player.sendMessage("§cЗаклятие Реактио ещё восстанавливается! Осталось: " + formatCooldownTime(reaktionCooldowns.get(playerId)));
            return;
        }

        Player nearestPlayer = findNearestPlayer(player);

        if (nearestPlayer == null || player.getLocation().distance(nearestPlayer.getLocation()) > 3) {
            player.sendMessage("§cРядом нет игроков, заклятие не может быть активировано!");
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != player && player.getLocation().distance(onlinePlayer.getLocation()) <= 3) {
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2));
                onlinePlayer.sendMessage("§6Вы получили эффект скорости на 20 секунд!");
            }
        }

        player.sendMessage("§6Вы активировали заклятие Реактио, ближайшие игроки получили скорость!");

        reaktionCooldowns.put(playerId, System.currentTimeMillis());
    }

    private void handleNeodoleoSpell(Player player) {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId, neodoleoCooldowns, 10 * 60)) {
            player.sendMessage("§cЗаклятие Неодолео ещё восстанавливается! Осталось: " + formatCooldownTime(neodoleoCooldowns.get(playerId)));
            return;
        }

        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != player && player.getLocation().distance(onlinePlayer.getLocation()) <= 3) {
                nearbyPlayers.add(onlinePlayer);
            }
        }

        if (nearbyPlayers.isEmpty()) {
            nearbyPlayers.add(player);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {

            Map<Player, Location> playerLocations = new HashMap<>();

            for (Player onlinePlayer : nearbyPlayers) {
                playerLocations.put(onlinePlayer, onlinePlayer.getLocation());
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 20, 2));
                onlinePlayer.sendMessage("§6Вы получили эффект слабости 3 на 20 секунд!");
            }

            for (Player onlinePlayer : nearbyPlayers) {
                onlinePlayer.teleport(new Location(player.getWorld(), 1179, 170, -564));
                onlinePlayer.sendMessage("§6Вы были телепортированы на координаты 1179, 170, -564!");
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player onlinePlayer : nearbyPlayers) {
                    Location originalLocation = playerLocations.get(onlinePlayer);
                    if (originalLocation != null) {
                        onlinePlayer.teleport(originalLocation);
                        onlinePlayer.sendMessage("§6Вы были телепортированы обратно.");
                    }
                }
            }, 5 * 20);
        });

        player.sendMessage("§6Вы активировали заклятие Неодолео, ближайшие игроки получили слабость и были телепортированы!");

        neodoleoCooldowns.put(playerId, System.currentTimeMillis());
    }

}
