package com.xenderz.xzcore.service;

/**
 * Base interface for all XzCore services.
 * 
 * <p>Services have a defined lifecycle:
 * <ol>
 *   <li>Construction - Create the service object</li>
 *   <li>Initialization - Start the service (database connections, tasks, etc.)</li>
 *   <li>Operation - Service is active</li>
 *   <li>Shutdown - Clean up resources</li>
 * </ol>
 */
public interface Service {
    
    /**
     * Initialize the service. Called after all services are constructed.
     * 
     * @throws Exception if initialization fails
     */
    void initialize() throws Exception;
    
    /**
     * Shutdown the service and clean up resources.
     */
    void shutdown();
    
    /**
     * Check if the service has been initialized.
     * 
     * @return true if initialized
     */
    boolean isInitialized();
    
    /**
     * Get the service name for logging.
     * 
     * @return service name
     */
    String getName();
}
