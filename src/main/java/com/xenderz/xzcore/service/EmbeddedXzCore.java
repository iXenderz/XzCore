package com.xenderz.xzcore.service;

import com.xenderz.xzcore.api.XzCoreAPI;
import com.xenderz.xzcore.database.DatabaseManager;
import com.xenderz.xzcore.events.EventBus;
import com.xenderz.xzcore.player.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Embedded XzCore implementation for shaded/embedded usage.
 * 
 * <p>This class provides the XzCoreAPI when XzCore is bundled (shaded) into another
 * plugin rather than running as a standalone plugin. This enables zero-friction
 * distribution where users only need to install one JAR.
 * 
 * <p>Usage example:
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     private EmbeddedXzCore xzCore;
 *     
 *     @Override
 *     public void onEnable() {
 *         // Initialize embedded XzCore
 *         this.xzCore = new EmbeddedXzCore(this);
 *         this.xzCore.initialize();
 *         
 *         // Use the API
 *         DatabaseManager db = xzCore.getDatabase();
 *         EventBus events = xzCore.getEventBus();
 *     }
 *     
 *     @Override
 *     public void onDisable() {
 *         if (xzCore != null) {
 *             xzCore.shutdown();
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @author Xzatrix
 * @version 1.0.0
 * @see XzCoreAPI
 * @see EmbeddedServiceContainer
 */
public class EmbeddedXzCore implements XzCoreAPI {
    
    private final EmbeddedServiceContainer services;
    
    /**
     * Create a new embedded XzCore instance.
     * 
     * @param hostPlugin The plugin that is hosting/shading XzCore
     */
    public EmbeddedXzCore(JavaPlugin hostPlugin) {
        this.services = new EmbeddedServiceContainer(hostPlugin);
    }
    
    /**
     * Initialize all services. Must be called before using any API methods.
     */
    public void initialize() {
        services.initialize();
    }
    
    /**
     * Shutdown all services. Should be called in onDisable().
     */
    public void shutdown() {
        services.shutdown();
    }
    
    /**
     * Check if services are initialized.
     */
    public boolean isInitialized() {
        return services.isInitialized();
    }
    
    @Override
    public DatabaseManager getDatabase() {
        return services.getDatabaseManager();
    }
    
    @Override
    public EventBus getEventBus() {
        return services.getEventBus();
    }
    
    @Override
    public PlayerDataManager getPlayerDataManager() {
        return services.getPlayerDataManager();
    }
    
    @Override
    public boolean isReady() {
        return services.isInitialized();
    }
    
    @Override
    public String getVersion() {
        return "1.0.0-embedded";
    }
    
    /**
     * Get the internal service container (for advanced usage).
     * 
     * @return The embedded service container
     */
    public EmbeddedServiceContainer getServiceContainer() {
        return services;
    }
}
