package com.xzatrix.xzcore.events;

import org.bukkit.plugin.java.JavaPlugin;
import com.xzatrix.xzcore.service.Service;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Event bus for inter-plugin communication within the XzPlugin suite.
 * 
 * <p>This event bus allows plugins to communicate without direct dependencies:
 * <pre>{@code
 * // In XzPvP
 * core.getEventBus().post(new PvPKillEvent(killer, victim));
 * 
 * // In XzRank
 * core.getEventBus().subscribe(this, PvPKillEvent.class, event -> {
 *     core.getPlayerData(event.getKiller()).addExperience(XP_SOURCE_PVP, 100);
 * });
 * }</pre>
 * 
 * <p>Events are posted on the Bukkit event bus for compatibility.
 */
public class EventBus implements Service, Listener {
    
    private final JavaPlugin plugin;
    private boolean initialized = false;
    
    // Store subscribers for XzCore events
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();
    
    public EventBus(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        // Register as Bukkit listener for compatibility
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initialized = true;
    }
    
    @Override
    public void shutdown() {
        // Unregister all subscribers
        subscribers.clear();
        initialized = false;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public String getName() {
        return "EventBus";
    }
    
    /**
     * Post an event to all subscribers.
     * 
     * <p>This calls Bukkit's event system for maximum compatibility.
     * 
     * @param <T> event type
     * @param event the event to post
     */
    public <T extends Event> void post(T event) {
        if (!initialized) {
            throw new IllegalStateException("EventBus not initialized");
        }
        
        // Post to Bukkit event system
        plugin.getServer().getPluginManager().callEvent(event);
        
        // Also notify XzCore subscribers
        @SuppressWarnings("unchecked")
        Set<Consumer<T>> eventSubscribers = (Set<Consumer<T>>) (Set<?>) subscribers.get(event.getClass());
        if (eventSubscribers != null) {
            for (Consumer<T> subscriber : eventSubscribers) {
                try {
                    subscriber.accept(event);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Error in event subscriber for " + event.getClass().getSimpleName() + ": " + e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Subscribe to events of a specific type.
     * 
     * @param <T> event type
     * @param plugin the subscribing plugin (for lifecycle management)
     * @param eventClass the event class to subscribe to
     * @param handler the event handler
     */
    public <T extends Event> void subscribe(Plugin plugin, Class<T> eventClass, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventClass, k -> new CopyOnWriteArraySet<>()).add(handler);
        
        // Also register with Bukkit for compatibility
        plugin.getServer().getPluginManager().registerEvent(
            eventClass, 
            new Listener() {}, 
            EventPriority.NORMAL, 
            (listener, event) -> {
                if (eventClass.isInstance(event)) {
                    handler.accept(eventClass.cast(event));
                }
            }, 
            plugin
        );
    }
    
    /**
     * Unsubscribe a handler from events.
     * 
     * @param <T> event type
     * @param eventClass the event class
     * @param handler the handler to remove
     */
    public <T extends Event> void unsubscribe(Class<T> eventClass, Consumer<T> handler) {
        @SuppressWarnings("unchecked")
        Set<Consumer<T>> eventSubscribers = (Set<Consumer<T>>) (Set<?>) subscribers.get(eventClass);
        if (eventSubscribers != null) {
            eventSubscribers.remove(handler);
        }
    }
    
    /**
     * Register a Bukkit listener.
     * 
     * @param listener the listener to register
     * @param plugin the plugin registering the listener
     */
    public void registerListener(Listener listener, Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    /**
     * Unregister a Bukkit listener.
     * 
     * @param listener the listener to unregister
     */
    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }
}
