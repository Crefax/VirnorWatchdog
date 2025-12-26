package com.virnor.watchdog.managers;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.gui.BanMenuGUI;
import com.virnor.watchdog.gui.MuteMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputManager implements Listener {

    public enum InputType {
        BAN_TARGET,
        MUTE_TARGET,
        TELEPORT_TARGET
    }

    private final VirnorWatchdog plugin;
    private final Map<UUID, InputType> awaitingInput;

    public ChatInputManager(VirnorWatchdog plugin) {
        this.plugin = plugin;
        this.awaitingInput = new HashMap<>();
    }

    public void requestInput(Player player, InputType type) {
        awaitingInput.put(player.getUniqueId(), type);
        
        switch (type) {
            case BAN_TARGET:
                player.sendMessage("§e§lBan menüsü için oyuncu ismini yazın (iptal: 'cancel'):");
                break;
            case MUTE_TARGET:
                player.sendMessage("§e§lMute menüsü için oyuncu ismini yazın (iptal: 'cancel'):");
                break;
            case TELEPORT_TARGET:
                player.sendMessage("§e§lIşınlanmak için oyuncu ismini yazın (iptal: 'cancel'):");
                break;
        }
    }

    public boolean isAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    public void cancelInput(Player player) {
        awaitingInput.remove(player.getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!awaitingInput.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        InputType type = awaitingInput.remove(player.getUniqueId());
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cİşlem iptal edildi.");
            return;
        }

        // Sync task olarak çalıştır (GUI async'te açılamaz)
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(input);
            
            if (target == null) {
                player.sendMessage("§cOyuncu bulunamadı: §e" + input);
                return;
            }

            switch (type) {
                case BAN_TARGET:
                    new BanMenuGUI(plugin, player, target).open();
                    break;
                case MUTE_TARGET:
                    new MuteMenuGUI(plugin, player, target).open();
                    break;
                case TELEPORT_TARGET:
                    player.teleport(target.getLocation());
                    player.sendMessage("§a§l" + target.getName() + " §aoyuncusuna ışınlandınız!");
                    // Spectator modda target'ı kaydet
                    if (plugin.getSpectatorManager().isSpectating(player)) {
                        plugin.getSpectatorManager().setTarget(player, target);
                    }
                    break;
            }
        });
    }
}
