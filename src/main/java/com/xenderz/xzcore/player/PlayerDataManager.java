package com.xenderz.xzcore.player;

import org.bukkit.plugin.java.JavaPlugin;
import com.xenderz.xzcore.database.DatabaseManager;
import com.xenderz.xzcore.events.EventBus;
import com.xenderz.xzcore.service.Service;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manager for player data lifecycle and persistence.
 * 
 * <p>Handles loading player data on join, caching during play,
 * and saving on quit or periodically.
 */
public class PlayerDataManager implements Service, Listener {
    
    private final JavaPlugin plugin;
    private final DatabaseManager database;
    private final EventBus eventBus;
    
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    public PlayerDataManager(JavaPlugin plugin, DatabaseManager database, EventBus eventBus) {
        this.plugin = plugin;
        this.database = database;
        this.eventBus = eventBus;
    }
    
    @Override
    public void initialize() {
        eventBus.registerListener(this, plugin);
        
        // Start auto-save task
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            this::saveAllDirty, 
            20L * 60 * 5, // 5 minutes
            20L * 60 * 5  // Every 5 minutes
        );
        
        initialized = true;
    }
    
    @Override
    public void shutdown() {
        // Save all cached data
        saveAll();
        cache.clear();
        initialized = false;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public String getName() {
        return "PlayerDataManager(" + cache.size() + " cached)";
    }
    
    /**
     * Get player data (cached or load from database).
     * 
     * @param uuid player UUID
     * @return PlayerData
     */
    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            // Load synchronously (should only happen if player is online)
            data = loadPlayerDataSync(uuid);
            if (data != null) {
                cache.put(uuid, data);
            }
        }
        return data;
    }
    
    /**
     * Check if player data is cached.
     */
    public boolean isCached(UUID uuid) {
        return cache.containsKey(uuid);
    }
    
    /**
     * Pre-load player data asynchronously.
     */
    public CompletableFuture<PlayerData> preloadPlayerData(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData data = loadOrCreatePlayerData(uuid, username);
            cache.put(uuid, data);
            return data;
        });
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // Preload data
        UUID uuid = event.getUniqueId();
        String username = event.getName();
        
        preloadPlayerData(uuid, username).join();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        PlayerData data = cache.get(uuid);
        if (data == null) {
            // Fallback if preload failed
            data = loadOrCreatePlayerData(uuid, player.getName());
            cache.put(uuid, data);
        }
        
        // Update last join
        data.setLastJoin(System.currentTimeMillis());
        data.setUsername(player.getName());
        
        // Save to ensure first_join is recorded
        if (data.getFirstJoin() == 0) {
            data.setFirstJoin(System.currentTimeMillis());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            savePlayerDataAsync(data);
        }
    }
    
    private PlayerData loadPlayerDataSync(UUID uuid) {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM xzcore_players WHERE uuid = ?")) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToPlayerData(rs);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
        }
        return null;
    }
    
    private PlayerData loadOrCreatePlayerData(UUID uuid, String username) {
        PlayerData data = loadPlayerDataSync(uuid);
        if (data == null) {
            data = new PlayerData(uuid, username);
            data.setFirstJoin(System.currentTimeMillis());
            data.markDirty();
            savePlayerDataAsync(data);
        }
        return data;
    }
    
    private PlayerData resultSetToPlayerData(ResultSet rs) throws SQLException {
        PlayerData data = new PlayerData(
            UUID.fromString(rs.getString("uuid")),
            rs.getString("username")
        );
        data.setFirstJoin(rs.getLong("first_join"));
        data.setLastJoin(rs.getLong("last_join"));
        data.setPlayTime(rs.getLong("play_time"));
        
        // Load experience
        try (PreparedStatement stmt = rs.getStatement().getConnection().prepareStatement(
            "SELECT * FROM xzcore_experience WHERE uuid = ?")) {
            stmt.setString(1, rs.getString("uuid"));
            ResultSet xpRs = stmt.executeQuery();
            if (xpRs.next()) {
                data.setTotalExperience(xpRs.getLong("total_xp"));
                data.setLevel(xpRs.getInt("level"));
            }
        }
        
        data.markClean();
        return data;
    }
    
    /**
     * Save player data asynchronously.
     */
    public CompletableFuture<Void> savePlayerDataAsync(PlayerData data) {
        return database.executeAsync(
            "INSERT OR REPLACE INTO xzcore_players (uuid, username, first_join, last_join, play_time) VALUES (?, ?, ?, ?, ?)",
            data.getUuid().toString(),
            data.getUsername(),
            data.getFirstJoin(),
            data.getLastJoin(),
            data.getPlayTime()
        ).thenRun(() -> {
            // Save experience
            database.executeAsync(
                "INSERT OR REPLACE INTO xzcore_experience (uuid, total_xp, level, last_updated) VALUES (?, ?, ?, ?)",
                data.getUuid().toString(),
                data.getTotalExperience(),
                data.getLevel(),
                System.currentTimeMillis()
            );
        }).thenRun(data::markClean);
    }
    
    /**
     * Save all dirty player data.
     */
    public void saveAllDirty() {
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                savePlayerDataAsync(data);
            }
        }
    }
    
    /**
     * Save all cached player data.
     */
    public void saveAll() {
        for (PlayerData data : cache.values()) {
            savePlayerDataAsync(data).join();
        }
    }
    
    /**
     * Get cache size.
     */
    public int getCacheSize() {
        return cache.size();
    }
}
