# XzCore

Core API and services for the XzPlugin suite.

[![Maven Central](https://img.shields.io/badge/Maven-com.xenderz-blue)](https://search.maven.org/search?q=g:com.xenderz)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Paper](https://img.shields.io/badge/Paper-1.21+-brightgreen)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Overview

XzCore provides shared infrastructure for all XzPlugins:
- **Database Manager** - HikariCP connection pooling (SQLite/MySQL)
- **Event Bus** - Inter-plugin communication
- **Player Data** - Unified player data management
- **Configuration** - Centralized config management
- **GUI Utilities** - Mobile-first design tokens and touch targets

## Architecture

```
XzCore (Service Container)
├── ConfigurationManager
├── DatabaseManager (HikariCP)
├── EventBus
├── PlayerDataManager
└── GUI Utilities
    ├── SemanticTokens (colors)
    ├── ComponentTokens (slots)
    ├── TouchTargets (mobile-friendly)
    └── XzCoreSounds (audio)
```

## Installation

### For Plugin Developers

Add XzCore as a dependency in your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    mavenLocal() // For local development
}

dependencies {
    // XzCore - Embedded core services (shaded)
    implementation("com.xenderz:xzcore:1.0.0")
}

// Shade XzCore into your plugin
tasks.shadowJar {
    relocate("com.xenderz.xzcore", "com.xenderz.yourplugin.lib.xzcore")
    relocate("com.zaxxer.hikari", "com.xenderz.yourplugin.lib.hikari")
    relocate("org.sqlite", "com.xenderz.yourplugin.lib.sqlite")
}
```

### For Server Administrators

1. Download `XzCore-1.0.0.jar` from [Releases](../../releases)
2. Place in your server's `plugins/` folder
3. Restart server
4. Configure in `plugins/XzCore/config.yml`

## Usage for Plugin Developers

### Getting the API

```java
import com.xenderz.xzcore.XzCore;
import com.xenderz.xzcore.api.XzCoreAPI;

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
PlayerData data = core.getPlayer(player);
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

## GUI Utilities

XzCore provides mobile-first GUI utilities for consistent player experience:

### Semantic Tokens (Colors)

```java
import com.xenderz.xzcore.gui.tokens.SemanticTokens;
import net.kyori.adventure.text.Component;

Component.text("Success!", SemanticTokens.STATE_SUCCESS)
Component.text("Error!", SemanticTokens.STATE_ERROR)
Component.text("Primary Action", SemanticTokens.ACTION_PRIMARY)
```

Available tokens:
- **Action**: `ACTION_PRIMARY`, `ACTION_SECONDARY`, `ACTION_ACCENT`
- **State**: `STATE_SUCCESS`, `STATE_WARNING`, `STATE_ERROR`, `STATE_INFO`
- **Content**: `CONTENT_PRIMARY`, `CONTENT_SECONDARY`, `CONTENT_MUTED`
- **Rank**: `RANK_COMMON` to `RANK_MYTHIC`
- **PvP**: `COMBAT_HOSTILE`, `COMBAT_FRIENDLY`, `COMBAT_NEUTRAL`

### Component Tokens (Slot Positions)

```java
import com.xenderz.xzcore.gui.tokens.ComponentTokens;

// Navigation slots
int backSlot = ComponentTokens.NAV_BACK;      // 45
int closeSlot = ComponentTokens.NAV_CLOSE;    // 49
int nextSlot = ComponentTokens.NAV_NEXT;      // 53

// Calculate slot from row/col
int slot = ComponentTokens.slot(2, 4);  // Row 2, Col 4 = slot 22
```

### Touch Targets (Mobile-Friendly)

```java
import com.xenderz.xzcore.gui.TouchTargets;

// 2x2 grid for mobile
int[] grid = TouchTargets.touchGrid(6);  // Returns {10, 12, 19, 21}

// Check if slot is thumb-friendly
boolean easy = TouchTargets.isThumbFriendly(49);  // true

// Vertical stack with spacing
int[] stack = TouchTargets.verticalStack(10, 3, 1);  // 3 items, spaced

// Horizontal row
int[] row = TouchTargets.horizontalRow(5, 1, 4, 1);  // Row 5, 4 items
```

### XzCore Sounds

```java
import com.xenderz.xzcore.gui.XzCoreSounds;

// Play sound
XzCoreSounds.CLICK.play(player);
XzCoreSounds.SUCCESS.play(player);
XzCoreSounds.ERROR.play(player);

// With pitch variation
XzCoreSounds.COINS.play(player, 0.2f);
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

Publish to local Maven:
```bash
./gradlew publishToMavenLocal
```

## Migration from com.xzatrix

If you're upgrading from the old `com.xzatrix` group:

```java
// Old (deprecated)
import com.xzatrix.xzcore.XzCore;

// New
import com.xenderz.xzcore.XzCore;
```

See [MIGRATION.md](../MIGRATION.md) for complete migration guide.

## License

MIT
