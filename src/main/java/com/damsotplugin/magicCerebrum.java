package com.damsotplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

    public magicCerebrum(DamsotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        
        if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§2Книга заклятий")) {
                
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore(); 

                    UUID playerId = player.getUniqueId();

            
                    if (lore.contains("§fАтакующее заклинание II")) {
                        switch (message.toLowerCase()) {
                            case "джастис":
                                handleJusticeSpell(player);
                                break;
                            case "просвектико":
                                handleProsvecticoSpell(player);
                                break;
                            case "реактим":
                                handleReaktimSpell(player);
                                break;
                        }
                    }

                    
                    if (lore.contains("§fЗащитное заклинание I")) {
                        switch (message.toLowerCase()) {
                            case "травл":
                                handleTravalSpell(player);
                                break;
                        }
                    }

                    
                    if (lore.contains("§fЗащитное заклинание II")) {
                        switch (message.toLowerCase()) {
                            case "уравниум":
                                handleUravniumSpell(player);
                                break;
                            case "полектум":
                                handlePolectumSpell(player);
                                break;
                        }
                    }

                    
                    if (lore.contains("§fАтакующее заклинание I") || lore.contains("§fАтакующее заклинание II")) {
                        switch (message.toLowerCase()) {
                            case "слептио":
                                handleSleptioSpell(player);
                                break;
                            case "крик":
                                handleKrikSpell(player);
                                break;
                        }
                    }
                }
            }
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
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0)); // 20 секунд
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
                bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 5, true);
                bow.setItemMeta(bowMeta);
                player.getInventory().addItem(bow);
    
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getInventory().removeItem(bow);
                        player.sendMessage("§6Твой лук пропал воооооу");
                    }
                }.runTaskLater(plugin, 200); 
    
                player.sendMessage("§6Заклятие Полектум использовано! Вы получили лук!");
            }
        }.runTask(plugin);
    }
}
