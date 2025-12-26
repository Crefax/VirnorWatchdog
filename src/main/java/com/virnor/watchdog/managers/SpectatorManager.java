package com.virnor.watchdog.managers;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.virnor.watchdog.VirnorWatchdog;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpectatorManager {

    private final VirnorWatchdog plugin;
    private final Map<UUID, SpectatorData> spectators;
    private final Map<UUID, UUID> spectatorTargets; // spectator -> target
    private final Map<UUID, Integer> knockbackPower; // spectator -> knockback power
    private final Map<UUID, BukkitTask> actionBarTasks; // spectator -> action bar task
    private Essentials essentials;

    public SpectatorManager(VirnorWatchdog plugin) {
        this.plugin = plugin;
        this.spectators = new HashMap<>();
        this.spectatorTargets = new HashMap<>();
        this.knockbackPower = new HashMap<>();
        this.actionBarTasks = new HashMap<>();
        
        // Essentials'i yukle (varsa)
        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }
    }

    public void startSpectating(Player staff, Player target) {
        // Eski spectator modunu kapat
        if (isSpectating(staff)) {
            stopSpectating(staff);
        }

        // Eski durumu kaydet (konum hariç - kaldığı yerde kalacak)
        SpectatorData data = new SpectatorData(
            staff.getGameMode(),
            staff.getAllowFlight(),
            staff.isFlying(),
            staff.getInventory().getContents().clone()
        );
        spectators.put(staff.getUniqueId(), data);

        // GameMode 0 (Survival) yap
        staff.setGameMode(GameMode.SURVIVAL);
        staff.setAllowFlight(true);
        staff.setFlying(true);
        staff.setFlySpeed((float) plugin.getConfig().getDouble("spectator.fly-speed", 2.0) / 10f);
        
        // TUM oyunculardan gizle (Essentials vanish gibi) - sadece hidePlayer kullan
        hideFromAllPlayers(staff);
        
        // Nametag'i gizle
        hideNameTag(staff);
        
        // Envanteri temizle ve butonlari ekle
        staff.getInventory().clear();
        
        if (target != null) {
            // Hedef varsa tracking itemleri ekle ve teleport et
            spectatorTargets.put(staff.getUniqueId(), target.getUniqueId());
            addTrackingItems(staff);
            staff.teleport(target.getLocation());
            
            // Action bar task başlat
            startActionBarTask(staff, target);
            
            // Mesaj gonder
            String message = plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.spec-start", "")
                            .replace("{player}", target.getName());
            staff.sendMessage(message.replace("&", "§"));
        } else {
            // Sadece gizli mod - tracking itemleri yine de ekle (barrier ile target seçebilir)
            addTrackingItems(staff);
            String message = plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.spec-mode", "&aGizli mod aktif!");
            staff.sendMessage(message.replace("&", "§"));
        }
    }

    public void stopSpectating(Player staff) {
        SpectatorData data = spectators.remove(staff.getUniqueId());
        if (data == null) return;

        // Hedef ve knockback bilgilerini temizle
        spectatorTargets.remove(staff.getUniqueId());
        knockbackPower.remove(staff.getUniqueId());
        
        // Action bar task'i iptal et
        BukkitTask task = actionBarTasks.remove(staff.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Eski durumu geri yukle
        staff.setGameMode(data.gameMode);
        staff.setAllowFlight(data.allowFlight);
        staff.setFlying(data.flying);
        staff.setFlySpeed(0.1f); // Normal fly speed
        
        // Invisibility efektini kaldir (varsa)
        if (staff.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            staff.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        
        // TUM oyunculara goster
        showToAllPlayers(staff);
        
        // Spec'teki diğer oyuncuları bu kişiden gizle
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isSpectating(online)) {
                staff.hidePlayer(plugin, online);
            }
        }
        
        // Nametag'i goster
        showNameTag(staff);
        
        // Envanteri geri yukle
        staff.getInventory().clear();
        staff.getInventory().setContents(data.inventory);
        
        // Mesaj gonder (kaldığı yerde kalıyor)
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.spec-stop", "");
        staff.sendMessage(message.replace("&", "§"));
    }
    
    private void hideFromAllPlayers(Player staff) {
        // TUM online oyunculardan gizle (spectator modda olanlar hariç)
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(staff) && !isSpectating(online)) {
                online.hidePlayer(plugin, staff);
            }
        }
        
        // Diğer spectatorlara bu yeni spectator'ı göster
        for (Player spectator : Bukkit.getOnlinePlayers()) {
            if (!spectator.equals(staff) && isSpectating(spectator)) {
                spectator.showPlayer(plugin, staff);
                staff.showPlayer(plugin, spectator);
            }
        }
    }
    
    private void showToAllPlayers(Player staff) {
        // TUM online oyunculara goster
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(staff)) {
                online.showPlayer(plugin, staff);
            }
        }
    }
    
    private void hideNameTag(Player player) {
        // Scoreboard team kullanarak nametag'i gizle
        org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }
        
        org.bukkit.scoreboard.Team team = scoreboard.getTeam("virnor_vanish");
        if (team == null) {
            team = scoreboard.registerNewTeam("virnor_vanish");
        }
        
        team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, 
                      org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        team.addEntry(player.getName());
    }
    
    private void showNameTag(Player player) {
        // Scoreboard team'den cikar
        org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            org.bukkit.scoreboard.Team team = scoreboard.getTeam("virnor_vanish");
            if (team != null) {
                team.removeEntry(player.getName());
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        }
    }
    
    private void addExitButton(Player player) {
        ItemStack exitButton = new ItemStack(Material.RED_BED);
        ItemMeta meta = exitButton.getItemMeta();
        meta.setDisplayName("§c§lGizli Moddan Çık");
        List<String> lore = new ArrayList<>();
        lore.add("§7Sağ tıklayarak gizli moddan");
        lore.add("§7çıkabilirsiniz.");
        meta.setLore(lore);
        exitButton.setItemMeta(meta);
        player.getInventory().setItem(8, exitButton);
    }
    
    private void addTrackingItems(Player player) {
        // 1. Slot - TV (Oyunculara Işınlanma)
        ItemStack tvItem = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta tvMeta = (org.bukkit.inventory.meta.SkullMeta) tvItem.getItemMeta();
        tvMeta.setOwner("computer"); // Computer skini
        tvMeta.setDisplayName("§b§lOyunculara Işınlan");
        List<String> tvLore = new ArrayList<>();
        tvLore.add("§7Sunucudaki tüm oyuncuları");
        tvLore.add("§7görmek için tıklayın.");
        tvMeta.setLore(tvLore);
        tvItem.setItemMeta(tvMeta);
        player.getInventory().setItem(0, tvItem);
        
        // 4. Slot - Ceza Menu (Barrier)
        ItemStack punishItem = new ItemStack(Material.BARRIER);
        ItemMeta punishMeta = punishItem.getItemMeta();
        punishMeta.setDisplayName("§c§lCeza Menüsü");
        List<String> punishLore = new ArrayList<>();
        punishLore.add("§7Oyuncuya ceza vermek için");
        punishLore.add("§7tıklayın.");
        punishMeta.setLore(punishLore);
        punishItem.setItemMeta(punishMeta);
        player.getInventory().setItem(3, punishItem);
        
        // 5. Slot - Teleport (Ender Pearl)
        ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tpItem.getItemMeta();
        tpMeta.setDisplayName("§d§lOyuncuya Işınlan");
        List<String> tpLore = new ArrayList<>();
        tpLore.add("§7Hedef oyuncuya tekrar");
        tpLore.add("§7ışınlanmak için tıklayın.");
        tpMeta.setLore(tpLore);
        tpItem.setItemMeta(tpMeta);
        player.getInventory().setItem(4, tpItem);
        
        // 6. Slot - Knockback Stick
        ItemStack stickItem = new ItemStack(Material.STICK);
        ItemMeta stickMeta = stickItem.getItemMeta();
        stickMeta.setDisplayName("§6§lSavurma Çubuğu");
        List<String> stickLore = new ArrayList<>();
        int power = knockbackPower.getOrDefault(player.getUniqueId(), 2);
        stickLore.add("§7Mevcut Güç: §e" + power);
        stickLore.add("§7Sağ tık ile gücü ayarlayın");
        stickLore.add("§7Sol tık ile oyuncuları savurun");
        stickMeta.setLore(stickLore);
        stickItem.setItemMeta(stickMeta);
        player.getInventory().setItem(5, stickItem);
        
        // Çıkış butonu
        addExitButton(player);
    }
    
    public Player getTarget(Player spectator) {
        UUID targetUUID = spectatorTargets.get(spectator.getUniqueId());
        return targetUUID != null ? Bukkit.getPlayer(targetUUID) : null;
    }
    
    public void setTarget(Player spectator, Player target) {
        if (target != null) {
            spectatorTargets.put(spectator.getUniqueId(), target.getUniqueId());
            
            // Eski task'ı iptal et ve yeni task başlat
            BukkitTask oldTask = actionBarTasks.remove(spectator.getUniqueId());
            if (oldTask != null) {
                oldTask.cancel();
            }
            startActionBarTask(spectator, target);
        } else {
            spectatorTargets.remove(spectator.getUniqueId());
            
            // Task'ı iptal et
            BukkitTask task = actionBarTasks.remove(spectator.getUniqueId());
            if (task != null) {
                task.cancel();
            }
        }
    }
    
    public int getKnockbackPower(Player spectator) {
        return knockbackPower.getOrDefault(spectator.getUniqueId(), 2);
    }
    
    public void setKnockbackPower(Player spectator, int power) {
        knockbackPower.put(spectator.getUniqueId(), power);
        // Item'i güncelle
        if (isSpectating(spectator) && getTarget(spectator) != null) {
            ItemStack stick = spectator.getInventory().getItem(5);
            if (stick != null && stick.getType() == Material.STICK) {
                ItemMeta meta = stick.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add("§7Mevcut Güç: §e" + power);
                lore.add("§7Sağ tık ile gücü ayarlayın");
                lore.add("§7Sol tık ile oyuncuları savurun");
                meta.setLore(lore);
                stick.setItemMeta(meta);
            }
        }
    }

    public boolean isSpectating(Player player) {
        return spectators.containsKey(player.getUniqueId());
    }
    
    private void startActionBarTask(Player spectator, Player target) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!spectator.isOnline() || !isSpectating(spectator)) {
                BukkitTask t = actionBarTasks.remove(spectator.getUniqueId());
                if (t != null) t.cancel();
                return;
            }
            
            Player currentTarget = getTarget(spectator);
            if (currentTarget != null && currentTarget.isOnline()) {
                String actionBarMsg = "§cŞüpheli: §e" + currentTarget.getName() + 
                                      " §7Ping: §e" + currentTarget.getPing() + "ms";
                spectator.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    TextComponent.fromLegacyText(actionBarMsg));
            } else {
                BukkitTask t = actionBarTasks.remove(spectator.getUniqueId());
                if (t != null) t.cancel();
            }
        }, 0L, 20L); // Her saniye (20 tick)
        
        actionBarTasks.put(spectator.getUniqueId(), task);
    }

    public boolean canSeePlayer(Player viewer, Player target) {
        // Eger izleyici spectator modda degilse normal gorünürluk
        if (!isSpectating(viewer)) {
            return true;
        }

        // Config'den ayari al
        if (!plugin.getConfig().getBoolean("spectator.can-see-vanished", true)) {
            return true;
        }

        // Essentials varsa ve hedef vanish'deyse
        if (essentials != null) {
            User user = essentials.getUser(target);
            if (user != null && user.isVanished()) {
                // Spectator modda vanish'li oyunculari gorebiliriz
                return true;
            }
        }

        return true;
    }

    public boolean isVisibleToEssentials(Player spectator) {
        if (!isSpectating(spectator)) {
            return true;
        }
        
        return plugin.getConfig().getBoolean("spectator.visible-to-essentials-vanish", false);
    }

    public void disableAllSpectators() {
        for (UUID uuid : spectators.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                stopSpectating(player);
            }
        }
        spectators.clear();
    }

    private static class SpectatorData {
        final GameMode gameMode;
        final boolean allowFlight;
        final boolean flying;
        final ItemStack[] inventory;

        SpectatorData(GameMode gameMode, boolean allowFlight, boolean flying, ItemStack[] inventory) {
            this.gameMode = gameMode;
            this.allowFlight = allowFlight;
            this.flying = flying;
            this.inventory = inventory;
        }
    }
}
