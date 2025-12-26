package com.virnor.watchdog.gui;

import com.virnor.watchdog.VirnorWatchdog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerTeleportGUI {

    private final VirnorWatchdog plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final List<Player> players;

    public PlayerTeleportGUI(VirnorWatchdog plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.players = new ArrayList<>();
        
        // Online oyunculari topla (kendisi haric)
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(viewer)) {
                players.add(online);
            }
        }
        
        // Inventory boyutunu hesapla (9'un kati olmali)
        int size = (int) Math.ceil(players.size() / 9.0) * 9;
        if (size > 54) size = 54; // Maksimum 54 slot
        if (size < 9) size = 9;   // Minimum 9 slot
        
        this.inventory = Bukkit.createInventory(null, size, "§6Oyunculara Işınlan");
        setupItems();
    }

    private void setupItems() {
        int slot = 0;
        for (Player target : players) {
            if (slot >= 54) break;
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§e" + target.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Ping: §a" + target.getPing() + "ms");
            lore.add("§7Dünya: §b" + target.getWorld().getName());
            lore.add("");
            lore.add("§aIşınlanmak için tıklayın!");
            meta.setLore(lore);
            
            head.setItemMeta(meta);
            inventory.setItem(slot, head);
            slot++;
        }
    }

    public void open() {
        viewer.openInventory(inventory);
    }

    public void handleClick(int slot) {
        if (slot >= players.size()) return;
        
        Player target = players.get(slot);
        if (target == null || !target.isOnline()) {
            viewer.sendMessage("§cOyuncu artık çevrimiçi değil!");
            viewer.closeInventory();
            return;
        }
        
        viewer.teleport(target.getLocation());
        viewer.sendMessage("§a§l" + target.getName() + " §aoyuncusuna ışınlandınız!");
        
        // Spectator modda target'i kaydet
        if (plugin.getSpectatorManager().isSpectating(viewer)) {
            plugin.getSpectatorManager().setTarget(viewer, target);
        }
        
        viewer.closeInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }
}
