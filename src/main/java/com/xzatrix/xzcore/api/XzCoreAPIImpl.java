package com.xzatrix.xzcore.api;

import com.xzatrix.xzcore.database.DatabaseManager;
import com.xzatrix.xzcore.events.EventBus;
import com.xzatrix.xzcore.player.PlayerDataManager;
import com.xzatrix.xzcore.service.EmbeddedServiceContainer;
import com.xzatrix.xzcore.service.ServiceContainer;

/**
 * Implementation of XzCoreAPI.
 * 
 * <p>This class delegates to the ServiceContainer while exposing
 * only the public API methods.
 */
public class XzCoreAPIImpl implements XzCoreAPI {
    
    private final ServiceProvider services;
    
    /**
     * Create API implementation backed by ServiceContainer (plugin mode).
     */
    public XzCoreAPIImpl(ServiceContainer services) {
        this.services = new ServiceContainerAdapter(services);
    }
    
    /**
     * Create API implementation backed by EmbeddedServiceContainer (embedded mode).
     */
    public XzCoreAPIImpl(EmbeddedServiceContainer services) {
        this.services = new EmbeddedServiceContainerAdapter(services);
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
        return services.getDatabaseManager().isInitialized() &&
               services.getEventBus().isInitialized() &&
               services.getPlayerDataManager().isInitialized();
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    /**
     * Internal interface to abstract over ServiceContainer and EmbeddedServiceContainer.
     */
    private interface ServiceProvider {
        DatabaseManager getDatabaseManager();
        EventBus getEventBus();
        PlayerDataManager getPlayerDataManager();
    }
    
    private record ServiceContainerAdapter(ServiceContainer container) implements ServiceProvider {
        @Override
        public DatabaseManager getDatabaseManager() {
            return container.getDatabaseManager();
        }
        
        @Override
        public EventBus getEventBus() {
            return container.getEventBus();
        }
        
        @Override
        public PlayerDataManager getPlayerDataManager() {
            return container.getPlayerDataManager();
        }
    }
    
    private record EmbeddedServiceContainerAdapter(EmbeddedServiceContainer container) implements ServiceProvider {
        @Override
        public DatabaseManager getDatabaseManager() {
            return container.getDatabaseManager();
        }
        
        @Override
        public EventBus getEventBus() {
            return container.getEventBus();
        }
        
        @Override
        public PlayerDataManager getPlayerDataManager() {
            return container.getPlayerDataManager();
        }
    }
}
