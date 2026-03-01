package com.xenderz.xzcore.database;

import org.bukkit.plugin.java.JavaPlugin;
import com.xenderz.xzcore.config.ConfigurationManager;
import com.xenderz.xzcore.service.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Database manager with HikariCP connection pooling.
 * 
 * <p>Supports SQLite (default) and MySQL backends.
 * All database operations can be sync or async.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Synchronous query
 * try (Connection conn = db.getConnection();
 *      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
 *     stmt.setString(1, uuid.toString());
 *     ResultSet rs = stmt.executeQuery();
 *     // Process results
 * }
 * 
 * // Asynchronous query
 * db.queryAsync("SELECT * FROM players WHERE uuid = ?", 
 *     rs -> { 
 *         // Process on async thread
 *     }, 
 *     uuid.toString()
 * );
 * 
 * // Async update with callback
 * db.executeAsync("UPDATE players SET kills = kills + 1 WHERE uuid = ?", uuid.toString())
 *     .thenRun(() -> player.sendMessage("Stats updated!"));
 * }</pre>
 */
public class DatabaseManager implements Service {
    
    private final JavaPlugin plugin;
    private final ConfigurationManager config;
    
    private HikariDataSource dataSource;
    private DatabaseType databaseType;
    private ExecutorService asyncExecutor;
    private boolean initialized = false;
    
    public DatabaseManager(JavaPlugin plugin, ConfigurationManager config) {
        this.plugin = plugin;
        this.config = config;
    }
    
    @Override
    public void initialize() throws Exception {
        String dbType = config.getString("database.type", "SQLITE").toUpperCase();
        this.databaseType = DatabaseType.valueOf(dbType);
        
        this.asyncExecutor = Executors.newFixedThreadPool(
            config.getInt("database.async-threads", 2),
            r -> {
                Thread t = new Thread(r, "XzCore-DB");
                t.setDaemon(true);
                return t;
            }
        );
        
        setupDataSource();
        
        // Mark as initialized before creating tables so getConnection() works
        initialized = true;
        createCoreTables();
        
        plugin.getLogger().info("Database initialized: " + databaseType + " with HikariCP pool");
    }
    
    @Override
    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
            }
        }
        
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed");
        }
        
        initialized = false;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public String getName() {
        return "DatabaseManager(" + databaseType + ")";
    }
    
    private void setupDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        
        if (databaseType == DatabaseType.SQLITE) {
            File dbFile = new File(plugin.getDataFolder(), 
                config.getString("database.sqlite.file", "xzcore.db"));
            dbFile.getParentFile().mkdirs();
            
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setMaximumPoolSize(config.getInt("database.sqlite.max-pool-size", 5));
            hikariConfig.setMinimumIdle(config.getInt("database.sqlite.min-idle", 1));
            
            // SQLite optimizations
            hikariConfig.addDataSourceProperty("journal_mode", config.getString("database.sqlite.journal-mode", "WAL"));
            hikariConfig.addDataSourceProperty("synchronous", config.getString("database.sqlite.synchronous", "NORMAL"));
            hikariConfig.addDataSourceProperty("foreign_keys", "true");
            hikariConfig.addDataSourceProperty("busy_timeout", "5000");
            
        } else if (databaseType == DatabaseType.MYSQL) {
            String host = config.getString("database.mysql.host", "localhost");
            int port = config.getInt("database.mysql.port", 3306);
            String database = config.getString("database.mysql.database", "xzcore");
            String username = config.getString("database.mysql.username", "root");
            String password = config.getString("database.mysql.password", "");
            
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=true&serverTimezone=UTC", 
                host, port, database));
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(config.getInt("database.mysql.max-pool-size", 10));
            hikariConfig.setMinimumIdle(config.getInt("database.mysql.min-idle", 5));
            
            // MySQL optimizations
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        }
        
        // Common settings
        hikariConfig.setPoolName("XzCore-DB-Pool");
        hikariConfig.setConnectionTimeout(config.getLong("database.connection-timeout", 5000));
        hikariConfig.setIdleTimeout(config.getLong("database.idle-timeout", 300000));
        hikariConfig.setMaxLifetime(config.getLong("database.max-lifetime", 1800000));
        hikariConfig.setLeakDetectionThreshold(config.getLong("database.leak-detection", 60000));
        
        this.dataSource = new HikariDataSource(hikariConfig);
    }
    
    private void createCoreTables() throws SQLException {
        try (Connection conn = getConnection()) {
            // Player data table
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS xzcore_players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "username VARCHAR(16) NOT NULL," +
                "first_join BIGINT DEFAULT 0," +
                "last_join BIGINT DEFAULT 0," +
                "play_time BIGINT DEFAULT 0" +
                ")")) {
                stmt.execute();
            }
            
            // Experience/progression table
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS xzcore_experience (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "total_xp BIGINT DEFAULT 0," +
                "level INT DEFAULT 1," +
                "last_updated BIGINT DEFAULT 0," +
                "FOREIGN KEY (uuid) REFERENCES xzcore_players(uuid) ON DELETE CASCADE" +
                ")")) {
                stmt.execute();
            }
            
            // Plugin metadata table
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS xzcore_plugin_data (" +
                "plugin_name VARCHAR(64) NOT NULL," +
                "key VARCHAR(128) NOT NULL," +
                "uuid VARCHAR(36)," +
                "value TEXT," +
                "updated_at BIGINT DEFAULT 0," +
                "PRIMARY KEY (plugin_name, key, uuid)" +
                ")")) {
                stmt.execute();
            }
        }
    }
    
    /**
     * Get a database connection from the pool.
     * 
     * @return Connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (!initialized || dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Execute a query asynchronously.
     * 
     * @param sql SQL query
     * @param resultHandler handler for the ResultSet
     * @param params query parameters
     * @return CompletableFuture for chaining
     */
    public CompletableFuture<Void> queryAsync(String sql, Consumer<ResultSet> resultHandler, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                
                ResultSet rs = stmt.executeQuery();
                resultHandler.accept(rs);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Async query failed: " + sql, e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }
    
    /**
     * Execute an update asynchronously.
     * 
     * @param sql SQL update
     * @param params update parameters
     * @return CompletableFuture with row count
     */
    public CompletableFuture<Integer> executeAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                
                return stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Async execute failed: " + sql, e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }
    
    /**
     * Execute a batch operation asynchronously.
     * 
     * @param sql SQL statement
     * @param batchParams list of parameter arrays
     * @return CompletableFuture with int array of update counts
     */
    public CompletableFuture<int[]> executeBatchAsync(String sql, java.util.List<Object[]> batchParams) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (Object[] params : batchParams) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    stmt.addBatch();
                }
                
                return stmt.executeBatch();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Async batch failed: " + sql, e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }
    
    /**
     * Execute a transaction asynchronously.
     * 
     * @param operations transaction operations
     * @return CompletableFuture
     */
    public CompletableFuture<Void> transactionAsync(Consumer<Connection> operations) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    operations.accept(conn);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Async transaction failed", e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }
    
    /**
     * Get the database type.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }
    
    /**
     * Check if connection pool is healthy.
     */
    public boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Get connection pool statistics.
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "Not initialized";
        }
        return String.format("Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}
