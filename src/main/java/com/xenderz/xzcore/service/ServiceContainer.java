package com.xenderz.xzcore.service;

import com.xenderz.xzcore.XzCore;
import com.xenderz.xzcore.api.XzCoreAPI;
import com.xenderz.xzcore.api.XzCoreAPIImpl;
import com.xenderz.xzcore.config.ConfigurationManager;
import com.xenderz.xzcore.database.DatabaseManager;
import com.xenderz.xzcore.events.EventBus;
import com.xenderz.xzcore.player.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Service container for dependency injection and lifecycle management.
 * 
 * <p>This class manages all core services and their initialization order.
 * Services are created in the constructor and initialized in the initialize() method.
 * 
 * <p>Service initialization order:
 * <ol>
 *   <li>ConfigurationManager</li>
 *   <li>DatabaseManager</li>
 *   <li>EventBus</li>
 *   <li>PlayerDataManager</li>
 * </ol>
 */
public class ServiceContainer {
    
    private final XzCore plugin;
    private final XzCoreAPIImpl api;
    
    // Core services
    private final ConfigurationManager configManager;
    private final DatabaseManager databaseManager;
    private final EventBus eventBus;
    private final PlayerDataManager playerDataManager;
    
    private final List<Service> services = new ArrayList<>();
    private boolean initialized = false;
    
    public ServiceContainer(XzCore plugin) {
        this.plugin = plugin;
        
        // Create services (no dependencies yet)
        this.configManager = new ConfigurationManager(plugin);
        this.databaseManager = new DatabaseManager(plugin, configManager);
        this.eventBus = new EventBus(plugin);
        this.playerDataManager = new PlayerDataManager(plugin, databaseManager, eventBus);
        
        // Register for lifecycle management
        services.add(configManager);
        services.add(databaseManager);
        services.add(eventBus);
        services.add(playerDataManager);
        
        // Create API (circular reference avoided by using interface)
        this.api = new XzCoreAPIImpl(this);
        
        plugin.getLogger().info("Services created: " + services.size());
    }
    
    /**
     * Initialize all services. Called after ServiceContainer is assigned to plugin.
     */
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("Services already initialized");
        }
        
        for (Service service : services) {
            try {
                service.initialize();
                plugin.getLogger().fine("Initialized service: " + service.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, 
                    "Failed to initialize service " + service.getName() + ": " + e.getMessage(), e);
                throw new RuntimeException("Service initialization failed: " + service.getName(), e);
            }
        }
        
        initialized = true;
        plugin.getLogger().info("All services initialized");
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
                plugin.getLogger().fine("Shutdown service: " + service.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Error shutting down service " + service.getName() + ": " + e.getMessage(), e);
            }
        }
        
        initialized = false;
        plugin.getLogger().info("All services shutdown");
    }
    
    /**
     * Get a comma-separated list of active service names.
     */
    public String getActiveServices() {
        List<String> names = new ArrayList<>();
        for (Service service : services) {
            if (service.isInitialized()) {
                names.add(service.getName());
            }
        }
        return String.join(", ", names);
    }
    
    // Service getters
    public XzCoreAPI getApi() {
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
}
