package com.xzatrix.xzcore;

import com.xzatrix.xzcore.api.XzCoreAPI;
import com.xzatrix.xzcore.database.DatabaseManager;
import com.xzatrix.xzcore.events.EventBus;
import com.xzatrix.xzcore.player.PlayerDataManager;
import com.xzatrix.xzcore.service.ServiceContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * XzCore - Central service provider for the XzPlugin suite.
 * 
 * <p>This plugin provides core services that other XzPlugins depend on:
 * <ul>
 *   <li>Database connection pooling (HikariCP)</li>
 *   <li>Inter-plugin event bus</li>
 *   <li>Player data management</li>
 *   <li>Configuration management</li>
 * </ul>
 * 
 * <p>Plugins should depend on XzCore and access services via:
 * <pre>{@code
 * XzCoreAPI core = XzCore.getAPI();
 * DatabaseManager db = core.getDatabase();
 * EventBus events = core.getEventBus();
 * }</pre>
 * 
 * @author Xzatrix
 * @version 1.0.0
 */
public class XzCore extends JavaPlugin {
    
    private static XzCore instance;
    private static XzCoreAPI api;
    
    private ServiceContainer services;
    private boolean initialized = false;
    
    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("XzCore v" + getDescription().getVersion() + " loading...");
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        try {
            // Initialize service container
            this.services = new ServiceContainer(this);
            api = services.getApi();
            
            // Initialize services that need the API available
            services.initialize();
            
            initialized = true;
            getLogger().info("XzCore v" + getDescription().getVersion() + " enabled successfully!");
            getLogger().info("Services: " + services.getActiveServices());
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize XzCore: " + e.getMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (services != null) {
            services.shutdown();
        }
        
        initialized = false;
        api = null;
        instance = null;
        
        getLogger().info("XzCore disabled");
    }
    
    /**
     * Get the XzCore API for accessing services.
     * 
     * @return XzCoreAPI instance
     * @throws IllegalStateException if XzCore is not enabled
     */
    public static XzCoreAPI getAPI() {
        if (api == null) {
            throw new IllegalStateException("XzCore is not enabled or still initializing");
        }
        return api;
    }
    
    /**
     * Get the XzCore plugin instance.
     * 
     * @return XzCore instance
     * @throws IllegalStateException if XzCore is not enabled
     */
    public static XzCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("XzCore is not enabled");
        }
        return instance;
    }
    
    /**
     * Check if XzCore is initialized and ready.
     * 
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return instance != null && instance.initialized;
    }
    
    /**
     * Get the service container (internal use only).
     * 
     * @return ServiceContainer
     */
    ServiceContainer getServiceContainer() {
        return services;
    }
}
