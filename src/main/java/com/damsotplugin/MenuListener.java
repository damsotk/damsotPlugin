package com.damsotplugin;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("Menu")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                String factionName = event.getCurrentItem().getItemMeta().getDisplayName();
                event.getWhoClicked().sendMessage("Вы нажали " + factionName);
            }
        }
    }
}
