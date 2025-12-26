package com.virnor.watchdog.gui;

import com.virnor.watchdog.VirnorWatchdog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KnockbackGUI {

    private final VirnorWatchdog plugin;
    private final Player staff;
    private final Inventory inventory;

    public KnockbackGUI(VirnorWatchdog plugin, Player staff) {
        this.plugin = plugin;
        this.staff = staff;
        this.inventory = Bukkit.createInventory(null, 9, "§6Savurma Gücü Seç");
        
        setupItems();
    }

    private void setupItems() {
        int currentPower = plugin.getSpectatorManager().getKnockbackPower(staff);
        
        for (int i = 0; i <= 8; i++) {
            Material material = (i == currentPower) ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6Güç: §e" + i);
            
            List<String> lore = new ArrayList<>();
            if (i == 0) {
                lore.add("§7Savurma yok");
            } else if (i <= 3) {
                lore.add("§7Hafif savurma");
            } else if (i <= 6) {
                lore.add("§7Orta savurma");
            } else {
                lore.add("§7Güçlü savurma");
            }
            
            if (i == currentPower) {
                lore.add("");
                lore.add("§a§l✔ Seçili");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
    }

    public void open() {
        staff.openInventory(inventory);
    }

    public void handleClick(int slot) {
        if (slot >= 0 && slot <= 8) {
            plugin.getSpectatorManager().setKnockbackPower(staff, slot);
            staff.sendMessage("§a§lSavurma gücü §e" + slot + " §aolarak ayarlandı!");
            staff.closeInventory();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
