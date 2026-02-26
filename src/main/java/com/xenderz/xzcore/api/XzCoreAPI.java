package com.xenderz.xzcore.api;

import com.xenderz.xzcore.database.DatabaseManager;
import com.xenderz.xzcore.events.EventBus;
import com.xenderz.xzcore.player.PlayerData;
import com.xenderz.xzcore.player.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Public API for XzCore services.
 * 
 * <p>This is the main interface that other plugins use to access XzCore functionality.
 * Obtain an instance via {@code XzCore.getAPI()}.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Get the API
 * XzCoreAPI core = XzCore.getAPI();
 * 
 * // Access player data
 * PlayerData data = core.getPlayerData(player.getUniqueId());
 * data.addExperience(ExperienceSource.PVP_KILL, 100);
 * 
 * // Listen to events
 * core.getEventBus().subscribe(this, PlayerLevelUpEvent.class, event -> {
 *     player.sendMessage("Level up! " + event.getNewLevel());
 * });
 * 
 * // Database access
 * DatabaseManager db = core.getDatabase();
 * db.executeAsync("INSERT INTO stats (uuid, kills) VALUES (?, ?)", uuid, kills);
 * }</pre>
 */
public interface XzCoreAPI {
    
    /**
     * Get the database manager for SQL operations.
     * 
     * @return DatabaseManager instance
     */
    DatabaseManager getDatabase();
    
    /**
     * Get the event bus for inter-plugin communication.
     * 
     * @return EventBus instance
     */
    EventBus getEventBus();
    
    /**
     * Get the player data manager.
     * 
     * @return PlayerDataManager instance
     */
    PlayerDataManager getPlayerDataManager();
    
    /**
     * Get player data for a specific player.
     * 
     * <p>This will load data from database if not cached.
     * 
     * @param uuid player UUID
     * @return PlayerData instance
     */
    default PlayerData getPlayerData(UUID uuid) {
        return getPlayerDataManager().getPlayerData(uuid);
    }
    
    /**
     * Get player data for a specific player.
     * 
     * @param player the player
     * @return PlayerData instance
     */
    default PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    /**
     * Check if the API is ready for use.
     * 
     * @return true if all services are initialized
     */
    boolean isReady();
    
    /**
     * Get the API version.
     * 
     * @return version string
     */
    String getVersion();
}
