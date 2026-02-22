package com.xzatrix.xzcore.config;

import org.bukkit.plugin.java.JavaPlugin;
import com.xzatrix.xzcore.service.Service;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Configuration manager for XzCore.
 * 
 * <p>Manages the main config.yml and provides utility methods
 * for other plugins to manage their configs.
 */
public class ConfigurationManager implements Service {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private boolean initialized = false;
    
    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        plugin.saveDefaultConfig();
        reload();
        initialized = true;
    }
    
    @Override
    public void shutdown() {
        save();
        initialized = false;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public String getName() {
        return "ConfigurationManager";
    }
    
    /**
     * Reload configuration from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    /**
     * Save configuration to disk.
     */
    public void save() {
        plugin.saveConfig();
    }
    
    // Delegation methods to FileConfiguration
    
    public String getString(String path, String def) {
        return config.getString(path, def);
    }
    
    public String getString(String path) {
        return config.getString(path);
    }
    
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }
    
    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }
    
    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }
    
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }
    
    public java.util.List<String> getStringList(String path) {
        return config.getStringList(path);
    }
    
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    public void set(String path, Object value) {
        config.set(path, value);
    }
    
    /**
     * Load a configuration file for another plugin.
     * 
     * @param plugin the plugin
     * @param filename the config file name
     * @return FileConfiguration
     */
    public FileConfiguration loadConfig(org.bukkit.plugin.Plugin plugin, String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Save a configuration file.
     * 
     * @param config the configuration
     * @param file the file to save to
     */
    public void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save config to " + file, e);
        }
    }
}
