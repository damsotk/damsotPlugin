package com.damsotplugin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FactionMenu {

    public static void openMenu(Player player, String faction) {
        Inventory menu = Bukkit.createInventory(null, 9, faction + " Menu");

        menu.addItem(createFactionItem(faction));
        
        switch (faction) {
            case "Дамсот":
                menu.addItem(createItem(Material.GOLDEN_APPLE, "Золотое яблоко", "Специальный предмет"));
                menu.addItem(createItem(Material.IRON_SWORD, "Меч Ордена", "Сильный меч нахуй!"));
                break;
            case "Ковенант":
                menu.addItem(createItem(Material.POTION, "Зелье", "Зелье силы"));
                menu.addItem(createItem(Material.STONE_AXE, "Топор ковенанта", "Топор для работы!"));
                break;
            case "Орден":
                menu.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Броня ордена", "Надежная броня"));
                menu.addItem(createItem(Material.SHIELD, "Щит", "Щит для крутоты"));
                break;
            default:
                menu.addItem(createItem(Material.BARRIER, "Хз что за фрака", "Не могу найти"));
                break;
        }

        player.openInventory(menu);
    }

    private static ItemStack createFactionItem(String faction) {
        Material material = Material.DIAMOND;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(faction);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createItem(Material material, String displayName, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
