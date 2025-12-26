package com.virnor.watchdog.listeners;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.gui.KnockbackGUI;
import com.virnor.watchdog.gui.PlayerTeleportGUI;
import com.virnor.watchdog.gui.PunishmentChoiceGUI;
import com.virnor.watchdog.managers.ChatInputManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {

    private final VirnorWatchdog plugin;

    public PlayerListener(VirnorWatchdog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Yeni giris yapan oyuncudan gizli moddaki yoneticileri gizle
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (plugin.getSpectatorManager().isSpectating(online)) {
                    player.hidePlayer(plugin, online);
                }
            }
        }, 5L); // 5 tick bekle (sunucuya tam olarak baglansin diye)
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Oyuncu spectator moddaysa modu kapat
        if (plugin.getSpectatorManager().isSpectating(player)) {
            plugin.getSpectatorManager().stopSpectating(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Spectator moddayken item kontrolu
        if (plugin.getSpectatorManager().isSpectating(player)) {
            if (event.getItem() != null) {
                Material type = event.getItem().getType();
                
                // TV - Oyunculara Işınlanma (Player Head)
                if (type == Material.PLAYER_HEAD) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        new PlayerTeleportGUI(plugin, player).open();
                        event.setCancelled(true);
                        return;
                    }
                }
                
                // Cikis butonu (Yatak)
                else if (type == Material.RED_BED) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        plugin.getSpectatorManager().stopSpectating(player);
                        event.setCancelled(true);
                        return;
                    }
                }
                
                // Ceza menusu (Barrier)
                else if (type == Material.BARRIER) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Player target = plugin.getSpectatorManager().getTarget(player);
                        if (target != null) {
                            new PunishmentChoiceGUI(plugin, player, target).open();
                        } else {
                            // Target yoksa chat'ten isim iste
                            player.sendMessage("§e§lCeza vermek istediğiniz oyuncunun ismini yazın:");
                            player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
                            plugin.getChatInputManager().requestInput(player, ChatInputManager.InputType.BAN_TARGET);
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
                
                // Teleport (Ender Pearl)
                else if (type == Material.ENDER_PEARL) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Player target = plugin.getSpectatorManager().getTarget(player);
                        if (target != null && target.isOnline()) {
                            player.teleport(target.getLocation());
                            player.sendMessage("§a§l" + target.getName() + " §aoyuncusuna ışınlandınız!");
                        } else if (target != null) {
                            player.sendMessage("§cHedef oyuncu çevrimiçi değil!");
                        } else {
                            // Target yoksa chat'ten isim iste
                            player.sendMessage("§e§lIşınlanmak istediğiniz oyuncunun ismini yazın:");
                            player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
                            plugin.getChatInputManager().requestInput(player, ChatInputManager.InputType.TELEPORT_TARGET);
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
                
                // Knockback Stick
                else if (type == Material.STICK) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        new KnockbackGUI(plugin, player).open();
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            
            // Spectator moddayken blok etkilesimini engelle
            if (!plugin.getConfig().getBoolean("spectator.open-inventory", false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Spectator moddayken entity etkilesimini engelle
        if (plugin.getSpectatorManager().isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // Spectator moddayken hasar almayi engelle
        if (plugin.getSpectatorManager().isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Hasari veren spectator modda mi?
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (plugin.getSpectatorManager().isSpectating(damager)) {
                // Sadece Stick ile vurabilir
                if (damager.getInventory().getItemInMainHand().getType() == Material.STICK) {
                    // Knockback uygula
                    if (event.getEntity() instanceof Player) {
                        Player victim = (Player) event.getEntity();
                        int power = plugin.getSpectatorManager().getKnockbackPower(damager);
                        
                        if (power > 0) {
                            Vector direction = victim.getLocation().toVector()
                                .subtract(damager.getLocation().toVector())
                                .normalize()
                                .multiply(power * 0.5)
                                .setY(0.3 * power);
                            
                            victim.setVelocity(direction);
                        }
                        
                        event.setDamage(0); // Hasar verme
                    }
                } else {
                    // Stick degilse tamamen engelle
                    event.setCancelled(true);
                }
                return;
            }
        }
        
        // Hasari alan spectator modda mi?
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (plugin.getSpectatorManager().isSpectating(victim)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Spectator moddayken kendi envanterinde değişiklik yapmasını engelle
        if (plugin.getSpectatorManager().isSpectating(player)) {
            // Sadece kendi envanterindeki itemleri taşımasını engelle
            if (event.getClickedInventory() == player.getInventory()) {
                event.setCancelled(true);
            }
        }
    }
}
