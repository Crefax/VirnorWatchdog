package com.virnor.watchdog.managers;

import com.virnor.watchdog.VirnorWatchdog;
import com.virnor.watchdog.data.PunishmentRecord;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private final VirnorWatchdog plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, List<PunishmentRecord>> punishmentHistory;

    public DataManager(VirnorWatchdog plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.punishmentHistory = new HashMap<>();
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Data dosyasi olusturulamadi!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Gecmis kayitlari yukle
        if (dataConfig.contains("history")) {
            for (String uuidStr : dataConfig.getConfigurationSection("history").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    List<PunishmentRecord> records = new ArrayList<>();
                    
                    List<Map<?, ?>> recordMaps = dataConfig.getMapList("history." + uuidStr);
                    for (Map<?, ?> map : recordMaps) {
                        PunishmentRecord record = PunishmentRecord.fromMap(map);
                        if (record != null) {
                            records.add(record);
                        }
                    }
                    
                    punishmentHistory.put(uuid, records);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Gecersiz UUID: " + uuidStr);
                }
            }
        }
    }

    public void saveData() {
        if (!plugin.getConfig().getBoolean("data.save-history", true)) {
            return;
        }

        // Gecmis kayitlari kaydet
        for (Map.Entry<UUID, List<PunishmentRecord>> entry : punishmentHistory.entrySet()) {
            List<Map<String, Object>> recordMaps = new ArrayList<>();
            
            int maxHistory = plugin.getConfig().getInt("data.max-history-per-player", 50);
            List<PunishmentRecord> records = entry.getValue();
            
            // Son N kaydi al
            int startIndex = Math.max(0, records.size() - maxHistory);
            List<PunishmentRecord> limitedRecords = records.subList(startIndex, records.size());
            
            for (PunishmentRecord record : limitedRecords) {
                recordMaps.add(record.toMap());
            }
            
            dataConfig.set("history." + entry.getKey().toString(), recordMaps);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Data dosyasi kaydedilemedi!");
            e.printStackTrace();
        }
    }

    public void addPunishment(UUID playerUUID, PunishmentRecord record) {
        List<PunishmentRecord> records = punishmentHistory.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        records.add(record);
        saveData();
    }

    public List<PunishmentRecord> getPunishmentHistory(UUID playerUUID) {
        return punishmentHistory.getOrDefault(playerUUID, new ArrayList<>());
    }

    public int getTotalBans(UUID playerUUID) {
        return (int) getPunishmentHistory(playerUUID).stream()
                .filter(r -> r.getType() == PunishmentRecord.PunishmentType.BAN)
                .count();
    }

    public int getTotalMutes(UUID playerUUID) {
        return (int) getPunishmentHistory(playerUUID).stream()
                .filter(r -> r.getType() == PunishmentRecord.PunishmentType.MUTE)
                .count();
    }
}
