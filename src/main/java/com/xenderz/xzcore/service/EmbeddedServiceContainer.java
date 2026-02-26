package com.xenderz.xzcore.service;

import com.xenderz.xzcore.api.XzCoreAPIImpl;
import com.xenderz.xzcore.config.ConfigurationManager;
import com.xenderz.xzcore.database.DatabaseManager;
import com.xenderz.xzcore.events.EventBus;
import com.xenderz.xzcore.player.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Service container for embedded mode (when XzCore is shaded into another plugin).
 * 
 * <p>This is a lightweight version of {@link ServiceContainer} that works with
 * any JavaPlugin instead of requiring the XzCore plugin specifically.
 * 
 * <p>Use this when bundling XzCore as a library within another plugin:
 * <pre>{@code
 * EmbeddedXzCore core = new EmbeddedXzCore(this);
 * core.initialize();
 * }</pre>
 * 
 * @author Xzatrix
 * @version 1.0.0
 */
public class EmbeddedServiceContainer {
    
    private final JavaPlugin plugin;
    private final XzCoreAPIImpl api;
    
    // Core services
    private final ConfigurationManager configManager;
    private final DatabaseManager databaseManager;
    private final EventBus eventBus;
    private final PlayerDataManager playerDataManager;
    
    private final List<Service> services = new ArrayList<>();
    private boolean initialized = false;
    
    /**
     * Create a new embedded service container.
     * 
     * @param plugin The host plugin (not XzCore itself)
     */
    public EmbeddedServiceContainer(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Create services (same as ServiceContainer)
        this.configManager = new ConfigurationManager(plugin);
        this.databaseManager = new DatabaseManager(plugin, configManager);
        this.eventBus = new EventBus(plugin);
        this.playerDataManager = new PlayerDataManager(plugin, databaseManager, eventBus);
        
        // Register for lifecycle management
        services.add(configManager);
        services.add(databaseManager);
        services.add(eventBus);
        services.add(playerDataManager);
        
        // Create API implementation
        this.api = new XzCoreAPIImpl(this);
        
        plugin.getLogger().info("[XzCore Embedded] Services created: " + services.size());
    }
    
    /**
     * Initialize all services. Must be called after construction.
     */
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("Services already initialized");
        }
        
        for (Service service : services) {
            try {
                service.initialize();
                plugin.getLogger().fine("[XzCore Embedded] Initialized service: " + service.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, 
                    "[XzCore Embedded] Failed to initialize service " + service.getName() + ": " + e.getMessage(), e);
                throw new RuntimeException("Service initialization failed: " + service.getName(), e);
            }
        }
        
        initialized = true;
        plugin.getLogger().info("[XzCore Embedded] All services initialized");
    }
    
    /**
     * Shutdown all services in reverse order.
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        // Shutdown in reverse order
        List<Service> reverseServices = new ArrayList<>(services);
        java.util.Collections.reverse(reverseServices);
        
        for (Service service : reverseServices) {
            try {
                service.shutdown();
                plugin.getLogger().fine("[XzCore Embedded] Shutdown service: " + service.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "[XzCore Embedded] Error shutting down service " + service.getName() + ": " + e.getMessage(), e);
            }
        }
        
        initialized = false;
        plugin.getLogger().info("[XzCore Embedded] All services shutdown");
    }
    
    /**
     * Check if services are initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    // Service getters
    public XzCoreAPIImpl getApi() {
        return api;
    }
    
    public ConfigurationManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public JavaPlugin getPlugin() {
        return plugin;
    }
}
