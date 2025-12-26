package com.virnor.watchdog;

import com.virnor.watchdog.commands.WatchdogCommand;
import com.virnor.watchdog.commands.WatchdogTabCompleter;
import com.virnor.watchdog.listeners.GUIListener;
import com.virnor.watchdog.listeners.PlayerListener;
import com.virnor.watchdog.managers.ChatInputManager;
import com.virnor.watchdog.managers.DataManager;
import com.virnor.watchdog.managers.SpectatorManager;
import org.bukkit.plugin.java.JavaPlugin;

public class VirnorWatchdog extends JavaPlugin {

    private static VirnorWatchdog instance;
    private SpectatorManager spectatorManager;
    private DataManager dataManager;
    private ChatInputManager chatInputManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Config dosyasini yukle
        saveDefaultConfig();
        
        // Manager'lari baslat
        this.spectatorManager = new SpectatorManager(this);
        this.dataManager = new DataManager(this);
        this.chatInputManager = new ChatInputManager(this);
        
        // Komutlari kaydet
        getCommand("wd").setExecutor(new WatchdogCommand(this));
        getCommand("wd").setTabCompleter(new WatchdogTabCompleter());
        
        // Event listener'lari kaydet
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputManager, this);
        
        getLogger().info("VirnorWatchdog basariyla yuklendi!");
    }

    @Override
    public void onDisable() {
        // Tum spectator modlari kapat
        if (spectatorManager != null) {
            spectatorManager.disableAllSpectators();
        }
        
        // Data'yi kaydet
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        getLogger().info("VirnorWatchdog kapatildi!");
    }

    public static VirnorWatchdog getInstance() {
        return instance;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
}
