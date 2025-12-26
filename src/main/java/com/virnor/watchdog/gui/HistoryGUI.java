package com.virnor.watchdog.gui;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.data.PunishmentRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryGUI {

    private final VirnorWatchdog plugin;
    private final Player staff;
    private final Player target;
    private final Inventory inventory;
    private int page = 0;

    public HistoryGUI(VirnorWatchdog plugin, Player staff, Player target) {
        this.plugin = plugin;
        this.staff = staff;
        this.target = target;
        this.inventory = Bukkit.createInventory(null, 54, "§6Gecmis - " + target.getName());
        
        setupItems();
    }

    private void setupItems() {
        inventory.clear();
        
        List<PunishmentRecord> history = plugin.getDataManager().getPunishmentHistory(target.getUniqueId());
        
        // Baslik bilgisi
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6" + target.getName() + " - Ceza Gecmisi");
        
        int totalBans = plugin.getDataManager().getTotalBans(target.getUniqueId());
        int totalMutes = plugin.getDataManager().getTotalMutes(target.getUniqueId());
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Toplam Ceza: §e" + history.size());
        infoLore.add("§7Toplam Ban: §c" + totalBans);
        infoLore.add("§7Toplam Mute: §e" + totalMutes);
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        // Gecmis kayitlari
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, history.size());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        for (int i = startIndex; i < endIndex; i++) {
            PunishmentRecord record = history.get(i);
            int slot = (i - startIndex) + 9; // Ilk satiri atla
            
            Material material = record.getType() == PunishmentRecord.PunishmentType.BAN 
                    ? Material.RED_CONCRETE 
                    : Material.YELLOW_CONCRETE;
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            String type = record.getType() == PunishmentRecord.PunishmentType.BAN ? "§cBAN" : "§eMUTE";
            meta.setDisplayName(type + " §7- " + record.getReason());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Yetkili: §f" + record.getStaffName());
            lore.add("§7Tarih: §f" + dateFormat.format(new Date(record.getTimestamp())));
            
            if (record.getDuration() == 0) {
                lore.add("§7Sure: §cKalici");
            } else {
                lore.add("§7Sure: §e" + formatDuration(record.getDuration()));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
        }

        // Navigasyon butonlari
        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName("§aOnceki Sayfa");
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(48, prevPage);
        }

        if (endIndex < history.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName("§aSonraki Sayfa");
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(50, nextPage);
        }

        // Geri button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cKapat");
        backItem.setItemMeta(backMeta);
        inventory.setItem(49, backItem);
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " dakika";
        } else if (minutes < 1440) {
            return (minutes / 60) + " saat";
        } else {
            return (minutes / 1440) + " gun";
        }
    }

    public void open() {
        staff.openInventory(inventory);
    }

    public void nextPage() {
        page++;
        setupItems();
    }

    public void previousPage() {
        if (page > 0) {
            page--;
            setupItems();
        }
    }

    public void handleClick(int slot) {
        if (slot == 48 && page > 0) {
            previousPage();
        } else if (slot == 50) {
            nextPage();
        } else if (slot == 49) {
            staff.closeInventory();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getTarget() {
        return target;
    }
}
