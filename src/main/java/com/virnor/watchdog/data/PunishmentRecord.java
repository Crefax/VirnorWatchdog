package com.virnor.watchdog.data;

import java.util.HashMap;
import java.util.Map;

public class PunishmentRecord {

    public enum PunishmentType {
        BAN, MUTE
    }

    private final PunishmentType type;
    private final String reason;
    private final String staffName;
    private final long timestamp;
    private final int duration; // dakika cinsinden

    public PunishmentRecord(PunishmentType type, String reason, String staffName, long timestamp, int duration) {
        this.type = type;
        this.reason = reason;
        this.staffName = staffName;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public String getStaffName() {
        return staffName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        map.put("reason", reason);
        map.put("staff", staffName);
        map.put("timestamp", timestamp);
        map.put("duration", duration);
        return map;
    }

    public static PunishmentRecord fromMap(Map<?, ?> map) {
        try {
            PunishmentType type = PunishmentType.valueOf((String) map.get("type"));
            String reason = (String) map.get("reason");
            String staff = (String) map.get("staff");
            long timestamp = ((Number) map.get("timestamp")).longValue();
            int duration = ((Number) map.get("duration")).intValue();
            
            return new PunishmentRecord(type, reason, staff, timestamp, duration);
        } catch (Exception e) {
            return null;
        }
    }
}
