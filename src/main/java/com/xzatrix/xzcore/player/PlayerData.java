package com.xzatrix.xzcore.player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Player data container with automatic persistence.
 * 
 * <p>This class holds all player-related data and tracks dirty state
 * for efficient database updates.
 * 
 * <p>Example usage:
 * <pre>{@code
 * PlayerData data = core.getPlayerData(player);
 * data.addExperience(100);
 * data.setMetadata("kills", data.getMetadata("kills", 0) + 1);
 * }</pre>
 */
public class PlayerData {
    
    private final UUID uuid;
    private volatile String username;
    
    // Core stats
    private final AtomicLong totalExperience = new AtomicLong(0);
    private final AtomicInteger level = new AtomicInteger(1);
    private final AtomicLong playTime = new AtomicLong(0);
    private volatile long firstJoin = 0;
    private volatile long lastJoin = 0;
    
    // Dirty tracking
    private volatile boolean dirty = false;
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();
    private final Map<String, Boolean> dirtyMetadata = new ConcurrentHashMap<>();
    
    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }
    
    // Getters
    public UUID getUuid() {
        return uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        markDirty();
    }
    
    public long getTotalExperience() {
        return totalExperience.get();
    }
    
    public void setTotalExperience(long experience) {
        this.totalExperience.set(experience);
        markDirty();
    }
    
    public void addExperience(long amount) {
        this.totalExperience.addAndGet(amount);
        markDirty();
    }
    
    public int getLevel() {
        return level.get();
    }
    
    public void setLevel(int level) {
        this.level.set(level);
        markDirty();
    }
    
    public boolean checkLevelUp() {
        long xp = totalExperience.get();
        int currentLevel = level.get();
        int required = getXpForLevel(currentLevel + 1);
        
        if (xp >= required) {
            level.incrementAndGet();
            markDirty();
            return true;
        }
        return false;
    }
    
    public long getPlayTime() {
        return playTime.get();
    }
    
    public void setPlayTime(long playTime) {
        this.playTime.set(playTime);
        markDirty();
    }
    
    public void addPlayTime(long milliseconds) {
        this.playTime.addAndGet(milliseconds);
        markDirty();
    }
    
    public long getFirstJoin() {
        return firstJoin;
    }
    
    public void setFirstJoin(long timestamp) {
        this.firstJoin = timestamp;
        markDirty();
    }
    
    public long getLastJoin() {
        return lastJoin;
    }
    
    public void setLastJoin(long timestamp) {
        this.lastJoin = timestamp;
        markDirty();
    }
    
    // Metadata for extensibility
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        Object value = metadata.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
        dirtyMetadata.put(key, true);
        markDirty();
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    public Map<String, Object> getAllMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }
    
    // Dirty tracking
    public boolean isDirty() {
        return dirty;
    }
    
    public void markDirty() {
        this.dirty = true;
    }
    
    public void markClean() {
        this.dirty = false;
        dirtyMetadata.clear();
    }
    
    // Utility
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }
    
    public void sendMessage(String message) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(message);
        }
    }
    
    public void sendMessage(Component component) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(component);
        }
    }
    
    /**
     * Get XP required for a specific level.
     * Override this for custom level formulas.
     */
    protected int getXpForLevel(int level) {
        // Default: exponential growth
        return (int) (100 * Math.pow(level, 1.5));
    }
    
    @Override
    public String toString() {
        return "PlayerData{" +
            "uuid=" + uuid +
            ", username='" + username + '\'' +
            ", level=" + level +
            ", xp=" + totalExperience +
            '}';
    }
}
