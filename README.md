# XzCore

Core API and services for the XzPlugin suite.

## Overview

XzCore provides shared infrastructure for all XzPlugins:
- **Database Manager** - HikariCP connection pooling (SQLite/MySQL)
- **Event Bus** - Inter-plugin communication
- **Player Data** - Unified player data management
- **Configuration** - Centralized config management

## Architecture

```
XzCore (Service Container)
├── ConfigurationManager
├── DatabaseManager (HikariCP)
├── EventBus
└── PlayerDataManager
```

## Usage for Plugin Developers

### Getting the API

```java
XzCoreAPI core = XzCore.getAPI();
```

### Database Operations

```java
// Async query
core.getDatabase().queryAsync(
    "SELECT * FROM players WHERE uuid = ?",
    rs -> { /* process results */ },
    uuid.toString()
);

// Async update
core.getDatabase().executeAsync(
    "UPDATE players SET kills = kills + 1 WHERE uuid = ?",
    uuid.toString()
);
```

### Player Data

```java
PlayerData data = core.getPlayerData(player);
data.addExperience(100);
data.setMetadata("kills", kills + 1);
```

### Events

```java
// Post event
core.getEventBus().post(new CustomEvent());

// Subscribe to events
core.getEventBus().subscribe(plugin, CustomEvent.class, event -> {
    // Handle event
});
```

## Configuration

```yaml
database:
  type: SQLITE  # or MYSQL
  
  sqlite:
    file: "xzcore.db"
    max-pool-size: 5
  
  mysql:
    host: "localhost"
    port: 3306
    database: "xzcore"
    username: "root"
    password: ""
    max-pool-size: 10
```

## Building

```bash
./gradlew shadowJar
```

## License

MIT
