package com.virnor.watchdog.listeners;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.gui.BanMenuGUI;
import com.virnor.watchdog.gui.HistoryGUI;
import com.virnor.watchdog.gui.KnockbackGUI;
import com.virnor.watchdog.gui.MuteMenuGUI;
import com.virnor.watchdog.gui.PlayerTeleportGUI;
import com.virnor.watchdog.gui.PunishmentChoiceGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GUIListener implements Listener {

    private final VirnorWatchdog plugin;

    public GUIListener(VirnorWatchdog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();

        // Ban Menu
        if (title.startsWith("§cŞüpheli: ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            // Title'dan oyuncu adini ayikla "Şüpheli: OyuncuAdi Ping: X"
            String[] parts = title.split(" ");
            if (parts.length >= 2) {
                String targetName = parts[1]; // "OyuncuAdi"
                Player target = plugin.getServer().getPlayer(targetName);
                if (target != null) {
                    BanMenuGUI gui = new BanMenuGUI(plugin, player, target);
                    gui.handleClick(event.getSlot());
                }
            }
        }
        // Mute Menu
        else if (title.startsWith("§eŞüpheli: ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            String[] parts = title.split(" ");
            if (parts.length >= 2) {
                String targetName = parts[1];
                Player target = plugin.getServer().getPlayer(targetName);
                if (target != null) {
                    MuteMenuGUI gui = new MuteMenuGUI(plugin, player, target);
                    gui.handleClick(event.getSlot());
                }
            }
        }
        // History GUI
        else if (title.startsWith("§6Gecmis - ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            String targetName = title.replace("§6Gecmis - ", "");
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null) {
                HistoryGUI gui = new HistoryGUI(plugin, player, target);
                gui.handleClick(event.getSlot());
            }
        }
        // Punishment Choice GUI
        else if (title.startsWith("§6Ceza Seç - ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            String targetName = title.replace("§6Ceza Seç - ", "");
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null) {
                PunishmentChoiceGUI gui = new PunishmentChoiceGUI(plugin, player, target);
                gui.handleClick(event.getSlot());
            }
        }
        // Knockback GUI
        else if (title.equals("§6Savurma Gücü Seç")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            KnockbackGUI gui = new KnockbackGUI(plugin, player);
            gui.handleClick(event.getSlot());
        }
        // Player Teleport GUI
        else if (title.equals("§6Oyunculara Işınlan")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            PlayerTeleportGUI gui = new PlayerTeleportGUI(plugin, player);
            gui.handleClick(event.getSlot());
        }
    }
}
