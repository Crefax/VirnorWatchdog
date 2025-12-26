package com.virnor.watchdog.commands;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.gui.BanMenuGUI;
import com.virnor.watchdog.gui.HistoryGUI;
import com.virnor.watchdog.gui.MuteMenuGUI;
import com.virnor.watchdog.gui.PunishmentChoiceGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WatchdogCommand implements CommandExecutor {

    private final VirnorWatchdog plugin;

    public WatchdogCommand(VirnorWatchdog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafindan kullanilabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spec":
            case "spectate":
                handleSpec(player, args);
                break;
            case "ban":
                handleBan(player, args);
                break;
            case "mute":
                handleMute(player, args);
                break;
            case "ceza":
            case "punish":
                handlePunish(player, args);
                break;
            case "history":
            case "h":
                handleHistory(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleSpec(Player player, String[] args) {
        if (!player.hasPermission("virnorwatchdog.spec")) {
            sendMessage(player, "no-permission");
            return;
        }

        // Zaten spectator modda mi?
        if (plugin.getSpectatorManager().isSpectating(player)) {
            // Spectator modu kapat
            plugin.getSpectatorManager().stopSpectating(player);
            return;
        }

        // Eger isim belirtilmediyse sadece gizli modu ac
        if (args.length < 2) {
            plugin.getSpectatorManager().startSpectating(player, null);
            return;
        }

        // Isim belirtildiyse TP et
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        if (target.equals(player)) {
            player.sendMessage("§cKendini izleyemezsin!");
            return;
        }

        plugin.getSpectatorManager().startSpectating(player, target);
    }

    private void handleBan(Player player, String[] args) {
        if (!player.hasPermission("virnorwatchdog.ban")) {
            sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cKullanim: /wd ban <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        // Ban GUI'sini ac
        new BanMenuGUI(plugin, player, target).open();
    }

    private void handleMute(Player player, String[] args) {
        if (!player.hasPermission("virnorwatchdog.mute")) {
            sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cKullanim: /wd mute <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        // Mute GUI'sini ac
        new MuteMenuGUI(plugin, player, target).open();
    }

    private void handleHistory(Player player, String[] args) {
        if (!player.hasPermission("virnorwatchdog.history")) {
            sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cKullanim: /wd history <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        // History GUI'sini ac
        new HistoryGUI(plugin, player, target).open();
    }

    private void handlePunish(Player player, String[] args) {
        if (!player.hasPermission("virnorwatchdog.ban") && !player.hasPermission("virnorwatchdog.mute")) {
            sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cKullanim: /wd ceza <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        // Ceza secim GUI'sini ac
        new PunishmentChoiceGUI(plugin, player, target).open();
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m--------------------§r §cVirnor§fWatchdog §8§m--------------------");
        player.sendMessage("§e/wd spec §7- Gizli moda gec/cik");
        player.sendMessage("§e/wd spec <oyuncu> §7- Gizli modda oyuncuya isinlan");
        player.sendMessage("§e/wd ceza <oyuncu> §7- Ceza menusu ac");
        player.sendMessage("§e/wd ban <oyuncu> §7- Ban menusu ac");
        player.sendMessage("§e/wd mute <oyuncu> §7- Mute menusu ac");
        player.sendMessage("§e/wd history <oyuncu> §7- Oyuncu gecmisini gor");
        player.sendMessage("§8§m--------------------------------------------------");
    }

    private void sendMessage(Player player, String key) {
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages." + key, "");
        player.sendMessage(message.replace("&", "§"));
    }
}
