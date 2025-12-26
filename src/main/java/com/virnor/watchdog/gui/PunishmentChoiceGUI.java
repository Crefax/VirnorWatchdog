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

public class PunishmentChoiceGUI {

    private final VirnorWatchdog plugin;
    private final Player staff;
    private final Player target;
    private final Inventory inventory;

    public PunishmentChoiceGUI(VirnorWatchdog plugin, Player staff, Player target) {
        this.plugin = plugin;
        this.staff = staff;
        this.target = target;
        this.inventory = Bukkit.createInventory(null, 9, "§6Ceza Seç - " + target.getName());
        
        setupItems();
    }

    private void setupItems() {
        // Ban butonu
        ItemStack banItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta banMeta = banItem.getItemMeta();
        banMeta.setDisplayName("§c§lBAN MENÜSÜ");
        List<String> banLore = new ArrayList<>();
        banLore.add("§7Oyuncuyu banlamak için");
        banLore.add("§7tıklayın.");
        banMeta.setLore(banLore);
        banItem.setItemMeta(banMeta);
        inventory.setItem(3, banItem);
        
        // Mute butonu
        ItemStack muteItem = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta muteMeta = muteItem.getItemMeta();
        muteMeta.setDisplayName("§e§lMUTE MENÜSÜ");
        List<String> muteLore = new ArrayList<>();
        muteLore.add("§7Oyuncuyu susturmak için");
        muteLore.add("§7tıklayın.");
        muteMeta.setLore(muteLore);
        muteItem.setItemMeta(muteMeta);
        inventory.setItem(5, muteItem);
    }

    public void open() {
        staff.openInventory(inventory);
    }

    public void handleClick(int slot) {
        if (slot == 3) {
            // Ban menüsü
            new BanMenuGUI(plugin, staff, target).open();
        } else if (slot == 5) {
            // Mute menüsü
            new MuteMenuGUI(plugin, staff, target).open();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getTarget() {
        return target;
    }
}
