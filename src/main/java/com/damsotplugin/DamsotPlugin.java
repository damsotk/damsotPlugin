package com.damsotplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class DamsotPlugin extends JavaPlugin implements CommandExecutor, Listener {
    private RiftManager riftManager;
    private FactionManager factionManager;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        riftManager = new RiftManager(this);
        getCommand("spawnRazlom").setExecutor(this);

        Bukkit.getPluginManager().registerEvents(new magicCerebrum(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        factionManager = new FactionManager(this);
        getCommand("fracmenu").setExecutor(factionManager);
        getCommand("fracadd").setExecutor(factionManager);

        teamManager = new TeamManager(this);
        getCommand("viewteam").setExecutor(teamManager);
    }

    @Override
    public void onDisable() {
        // Cleanup resources if necessary
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spawnRazlom")) {
            if (args.length < 2) {
                sender.sendMessage("§cИспользование: /spawnRazlom <RiftName> <open|close>");
                return true;
            }
        
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cЭту команду может использовать только игрок!");
                return true;
            }
        
            Player player = (Player) sender;
            String riftName = args[0];
            boolean open = args[1].equalsIgnoreCase("open");
        
            if (!args[1].equalsIgnoreCase("open") && !args[1].equalsIgnoreCase("close")) {
                player.sendMessage("§cВторой аргумент должен быть 'open' или 'close'!");
                return true;
            }
        
            riftManager.toggleRift(riftName, open, player);
        
            
            if (open) {
                player.sendMessage("§eЗапущен мониторинг для разлома: " + riftName);
                riftManager.monitorPlayersInRift(riftName);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("fracmenu") && sender instanceof Player) {
            Player player = (Player) sender;
            factionManager.showFactionMenu(player);
            return true;
        }
        return false;
    }
}
