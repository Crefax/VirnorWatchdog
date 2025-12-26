package com.virnor.watchdog.gui;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.data.PunishmentRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BanMenuGUI {

    private final VirnorWatchdog plugin;
    private final Player staff;
    private final Player target;
    private final Inventory inventory;

    public BanMenuGUI(VirnorWatchdog plugin, Player staff, Player target) {
        this.plugin = plugin;
        this.staff = staff;
        this.target = target;
        String title = "§cŞüpheli: " + target.getName() + " §7Ping: §e" + target.getPing();
        this.inventory = Bukkit.createInventory(null, 54, title);
        
        setupItems();
    }

    private void setupItems() {
        ConfigurationSection banReasons = plugin.getConfig().getConfigurationSection("ban-reasons");
        if (banReasons == null) return;

        // Mini gecmis - oyuncu kafasi (slot 4)
        addPlayerHistoryHead();

        int slot = 9; // Alt satirdan basla
        for (String reasonKey : banReasons.getKeys(false)) {
            if (slot >= 45) break; // 45-52 arasi bos, 53'te history
            if (slot % 9 == 8) slot++; // Her satırın son slotunu atla
            
            String displayName = banReasons.getString(reasonKey + ".display-name", reasonKey);
            int duration = banReasons.getInt(reasonKey + ".duration", 0);
            
            ItemStack item = createBanItem(displayName, reasonKey, duration);
            inventory.setItem(slot, item);
            slot++;
        }

        // Detayli gecmis button (slot 53 - sag alt kose)
        ItemStack historyItem = new ItemStack(Material.BOOK);
        ItemMeta historyMeta = historyItem.getItemMeta();
        historyMeta.setDisplayName("§eDetayli Oyuncu Gecmisi");
        
        int totalBans = plugin.getDataManager().getTotalBans(target.getUniqueId());
        int totalMutes = plugin.getDataManager().getTotalMutes(target.getUniqueId());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Toplam Ban: §c" + totalBans);
        lore.add("§7Toplam Mute: §e" + totalMutes);
        lore.add("");
        lore.add("§aDetaylar icin tiklayin!");
        historyMeta.setLore(lore);
        historyItem.setItemMeta(historyMeta);
        inventory.setItem(53, historyItem);
    }

    private void addPlayerHistoryHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName("§6" + target.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Son Cezalar:");
        lore.add("");
        
        // Son 5 cezayi goster
        List<PunishmentRecord> records = plugin.getDataManager().getPunishmentHistory(target.getUniqueId());
        int count = 0;
        for (int i = records.size() - 1; i >= 0 && count < 5; i--) {
            PunishmentRecord record = records.get(i);
            String typeColor = record.getType() == PunishmentRecord.PunishmentType.BAN ? "§c" : "§e";
            lore.add(typeColor + record.getType().name() + " §7- " + record.getReason());
            count++;
        }
        
        if (count == 0) {
            lore.add("§7Ceza kaydı yok");
        }
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        inventory.setItem(4, head);
    }

    private ItemStack createBanItem(String displayName, String reasonKey, int duration) {
        Material material = Material.RED_CONCRETE;
        
        // Sebebe gore materyal
        if (reasonKey.contains("fly")) material = Material.FEATHER;
        else if (reasonKey.contains("killaura") || reasonKey.contains("kill")) material = Material.DIAMOND_SWORD;
        else if (reasonKey.contains("speed")) material = Material.SUGAR;
        else if (reasonKey.contains("xray")) material = Material.DIAMOND_ORE;
        else if (reasonKey.contains("bhop")) material = Material.RABBIT_FOOT;
        else if (reasonKey.contains("scaffold")) material = Material.OAK_PLANKS;
        else if (reasonKey.contains("permanent")) material = Material.BARRIER;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Sebep: §f" + reasonKey);
        
        if (duration == 0) {
            lore.add("§7Sure: §cKalici");
        } else {
            lore.add("§7Sure: §e" + formatDuration(duration));
        }
        
        lore.add("");
        lore.add("§aOyuncuyu banlamak icin tiklayin!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
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
        // Action bar mesajı gönder
        String actionBarMsg = "§cŞüpheli: §e" + target.getName() + " §7Ping: §e" + target.getPing() + "ms";
        staff.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarMsg));
    }

    public void handleClick(int slot) {
        ItemStack clicked = inventory.getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // History button (slot 53)
        if (slot == 53) {
            new HistoryGUI(plugin, staff, target).open();
            return;
        }

        // Oyuncu kafasi (slot 4) - tiklanamaz
        if (slot == 4) {
            return;
        }

        // Ban reason'lar (slot 9'dan basla)
        if (slot < 9) {
            return;
        }

        ConfigurationSection banReasons = plugin.getConfig().getConfigurationSection("ban-reasons");
        if (banReasons == null) return;

        int currentSlot = 9;
        for (String reasonKey : banReasons.getKeys(false)) {
            if (currentSlot == slot) {
                executeBan(reasonKey);
                return;
            }
            currentSlot++;
        }
    }

    private void executeBan(String reasonKey) {
        ConfigurationSection reason = plugin.getConfig().getConfigurationSection("ban-reasons." + reasonKey);
        if (reason == null) return;

        String displayName = reason.getString("display-name", reasonKey).replace("&", "§");
        int duration = reason.getInt("duration", 0);
        String command = reason.getString("command", "ban {player} {duration} {reason}");

        // Komutu hazirla
        String formattedCommand = command
                .replace("{player}", target.getName())
                .replace("{duration}", duration + "m")
                .replace("{reason}", displayName);

        // Komutu calistir
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);

        // Kayit tut
        PunishmentRecord record = new PunishmentRecord(
                PunishmentRecord.PunishmentType.BAN,
                displayName,
                staff.getName(),
                System.currentTimeMillis(),
                duration
        );
        plugin.getDataManager().addPunishment(target.getUniqueId(), record);

        // Mesaj gonder
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.ban-success", "")
                        .replace("{player}", target.getName())
                        .replace("{reason}", displayName);
        staff.sendMessage(message.replace("&", "§"));
        
        staff.closeInventory();
    }

    public Player getTarget() {
        return target;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
