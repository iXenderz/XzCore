# XzCore NPC Manager - Design Document

## Overview

A centralized NPC management system for the XzPlugin suite, providing unified NPC creation, tracking, and interaction handling across all plugins (XzPvP, XzDungeon, XzGraves, etc.).

**Status:** Design Phase  
**Target Version:** XzCore 1.1.0  
**Dependencies:** Citizens (optional but recommended), Paper 1.21+

---

## Goals

1. **Unified API** - Single interface for NPC management across all XzPlugins
2. **Plugin Isolation** - Namespaced NPC IDs prevent conflicts
3. **Consistent Behavior** - All NPCs behave the same way (invulnerable, no AI, etc.)
4. **Centralized Cleanup** - One cleanup routine on startup
5. **Admin Visibility** - Global view of all NPCs from all plugins
6. **Fallback Support** - Works with or without Citizens plugin

---

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        XzCore NPC Manager                    │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ NPC Registry │  │ Click Router │  │ Cleanup Service  │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Persistence  │  │ Admin GUI    │  │ Trait System     │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   XzPvP      │   │  XzDungeon   │   │   XzGraves   │
│ NPCs: 5      │   │ NPCs: 12     │   │ NPCs: 3      │
└──────────────┘   └──────────────┘   └──────────────┘
```

---

## Core Classes

### 1. XzNPC (The NPC Model)

```java
package com.xenderz.xzcore.npc;

public class XzNPC {
    // Identity
    private final UUID uuid;           // Unique across server
    private final String pluginId;     // "xzpvp", "xzdungeon", etc.
    private final String localId;      // "bountymaster_1"
    private final String fullId;       // "xzpvp:bountymaster_1"
    
    // Appearance
    private String displayName;
    private EntityType entityType;
    private Skin skin;                 // For player NPCs
    
    // State
    private Location location;
    private boolean spawned;
    private NPCState state;
    
    // Behavior
    private boolean lookAtPlayers;
    private boolean invulnerable;
    private boolean silent;
    
    // Interaction
    private Consumer<Player> clickHandler;
    private String permission;         // Required to interact
    
    // Internal
    private Object citizensNPC;        // Citizens reference (if using)
    private Entity vanillaEntity;      // Bukkit entity (fallback)
}
```

### 2. XzNPCManager (Central Service)

```java
package com.xenderz.xzcore.npc;

public class XzNPCManager {
    
    // Registry
    private final Map<String, XzNPC> npcsByFullId;
    private final Map<UUID, XzNPC> npcsByUUID;
    private final Multimap<Plugin, XzNPC> npcsByPlugin;
    
    // Configuration
    private final boolean useCitizens;
    private final boolean autoCleanup;
    private final double cleanupRadius;
    
    // Services
    private final NPCClickRouter clickRouter;
    private final NPCCleanupService cleanupService;
    private final NPCPersistenceService persistence;
    
    /**
     * Create a new NPC builder for a plugin
     */
    public XzNPCBuilder createNPC(Plugin plugin, String localId);
    
    /**
     * Get NPC by full ID (plugin:localId)
     */
    public Optional<XzNPC> getNPC(String fullId);
    
    /**
     * Get NPC by UUID
     */
    public Optional<XzNPC> getNPC(UUID uuid);
    
    /**
     * Get all NPCs for a plugin
     */
    public Collection<XzNPC> getNPCsForPlugin(Plugin plugin);
    
    /**
     * Get all NPCs (admin view)
     */
    public Collection<XzNPC> getAllNPCs();
    
    /**
     * Remove an NPC
     */
    public boolean removeNPC(String fullId);
    
    /**
     * Remove all NPCs for a plugin
     */
    public int removeAllForPlugin(Plugin plugin);
    
    /**
     * Teleport NPC to new location
     */
    public boolean teleport(String fullId, Location location);
    
    /**
     * Update NPC display name
     */
    public boolean setDisplayName(String fullId, String name);
}
```

### 3. XzNPCBuilder (Fluent API)

```java
package com.xenderz.xzcore.npc;

public class XzNPCBuilder {
    
    public XzNPCBuilder type(EntityType type);
    public XzNPCBuilder name(String name);
    public XzNPCBuilder location(Location location);
    public XzNPCBuilder skin(Skin skin);           // Player NPCs only
    public XzNPCBuilder lookAtPlayers(boolean enabled);
    public XzNPCBuilder invulnerable(boolean enabled);
    public XzNPCBuilder silent(boolean enabled);
    public XzNPCBuilder onClick(Consumer<Player> handler);
    public XzNPCBuilder permission(String permission);
    public XzNPCBuilder metadata(String key, Object value);
    
    /**
     * Build and spawn the NPC
     */
    public XzNPC spawn();
    
    /**
     * Build without spawning (for batch creation)
     */
    public XzNPC build();
}
```

---

## API Usage Examples

### XzPvP - Bounty Master NPC

```java
public class BountyNPCManager {
    
    private final XzPvP plugin;
    private final XzNPCManager npcManager;
    
    public void createBountyMaster(Location location) {
        XzNPC npc = npcManager.createNPC(plugin, "bountymaster_1")
            .type(EntityType.WANDERING_TRADER)
            .name("§e§lBounty Master")
            .location(location)
            .lookAtPlayers(true)
            .invulnerable(true)
            .silent(true)
            .onClick(player -> {
                new BountyGUI(plugin).openMainMenu(player);
            })
            .spawn();
        
        // Store reference if needed
        this.bountyMasterNPC = npc;
    }
}
```

### XzDungeon - Quest NPC

```java
public void createQuestNPC(String questId, Location location) {
    XzNPC npc = npcManager.createNPC(plugin, "quest_" + questId)
        .type(EntityType.VILLAGER)
        .name("§eQuest Giver")
        .location(location)
        .lookAtPlayers(true)
        .metadata("quest_id", questId)
        .onClick(player -> {
            String questId = npc.getMetadata("quest_id");
            openQuestGUI(player, questId);
        })
        .spawn();
}
```

### XzGraves - Grave Keeper NPC

```java
public void createGraveKeeper(Location location) {
    XzNPC npc = npcManager.createNPC(plugin, "gravekeeper")
        .type(EntityType.ZOMBIE)
        .name("§7Grave Keeper")
        .location(location)
        .invulnerable(true)
        .onClick(player -> {
            openGravesGUI(player);
        })
        .spawn();
}
```

---

## Admin Commands

### Global NPC Management

```
/xznpc list [plugin]           - List all NPCs (or for specific plugin)
/xznpc info <id>               - Show detailed NPC info
/xznpc tp <id>                 - Teleport to NPC
/xznpc tphere <id>             - Teleport NPC to you
/xznpc remove <id>             - Remove specific NPC
/xznpc removeall <plugin>      - Remove all NPCs from plugin
/xznpc cleanup                 - Force cleanup orphaned entities
/xznpc reload                  - Reload all NPCs from storage
```

### Example Output

```
> /xznpc list
┌──────────────────────────────────────────────┐
│           XzCore NPC Registry                │
├──────────────────────────────────────────────┤
│ XzPvP (3 NPCs)                               │
│   • xzpvp:bountymaster_1 - Bounty Master     │
│     [WANDERING_TRADER] at world:100,64,200   │
│   • xzpvp:hunter_1 - Elite Hunter            │
│     [PILLAGER] at world:105,64,205           │
│                                              │
│ XzDungeon (12 NPCs)                          │
│   • xzdungeon:guide_1 - Dungeon Guide        │
│     [VILLAGER] at dungeon_lobby:50,70,50     │
│   • xzdungeon:merchant_1 - Item Shop         │
│     [WANDERING_TRADER] at dungeon_1:25,65,30 │
│   ... (9 more)                               │
│                                              │
│ XzGraves (2 NPCs)                            │
│   • xzgraves:keeper_1 - Grave Keeper         │
│     [ZOMBIE] at world:-100,64,-100           │
├──────────────────────────────────────────────┤
│ Total: 17 NPCs across 3 plugins              │
└──────────────────────────────────────────────┘
```

---

## Implementation Strategy

### Phase 1: Core Infrastructure (Week 1)
- [ ] XzNPC data model
- [ ] XzNPCManager registry
- [ ] XzNPCBuilder fluent API
- [ ] Citizens integration (primary)
- [ ] Vanilla fallback (if Citizens not present)

### Phase 2: Interaction System (Week 1-2)
- [ ] Click event routing
- [ ] Metadata management
- [ ] Permission checks
- [ ] Look-at-player behavior

### Phase 3: Persistence & Cleanup (Week 2)
- [ ] npcs.yml storage format
- [ ] Auto-cleanup on startup
- [ ] Backup/restore system
- [ ] Migration from plugin-specific storage

### Phase 4: Admin Tools (Week 2-3)
- [ ] /xznpc commands
- [ ] Admin GUI (optional)
- [ ] Real-time NPC list
- [ ] Debug information

### Phase 5: Plugin Integration (Week 3+)
- [ ] Refactor XzPvP to use XzCore NPC
- [ ] Refactor XzDungeon to use XzCore NPC
- [ ] Refactor XzGraves to use XzCore NPC
- [ ] Deprecate old NPC systems

---

## Storage Format

### npcs.yml (Central Storage)

```yaml
version: 2
npcs:
  # Format: "plugin:localid"
  "xzpvp:bountymaster_1":
    plugin: "xzpvp"
    localId: "bountymaster_1"
    uuid: "550e8400-e29b-41d4-a716-446655440000"
    type: "WANDERING_TRADER"
    displayName: "§e§lBounty Master"
    location:
      world: "world"
      x: 100.0
      y: 64.0
      z: 200.0
      yaw: 0.0
      pitch: 0.0
    behavior:
      lookAtPlayers: true
      invulnerable: true
      silent: true
    metadata:
      npc_type: "bountymaster"
      
  "xzdungeon:guide_1":
    plugin: "xzdungeon"
    localId: "guide_1"
    # ... similar structure
```

---

## Citizens vs Vanilla Comparison

| Feature | With Citizens | Vanilla Fallback |
|---------|--------------|------------------|
| Player NPCs | ✅ Yes (skins) | ❌ No |
| Pathfinding | ✅ Yes | ❌ No |
| Look at player | ✅ Yes (trait) | ✅ Custom implementation |
| Name visibility | ✅ Always | ✅ Always |
| Invulnerability | ✅ Yes | ✅ Yes |
| Persistence | ✅ Citizens handles | ⚠️ We handle |
| Performance | ✅ Better | ⚠️ OK for few NPCs |

**Decision:** Use Citizens if available, fallback to vanilla.

---

## Configuration

### config.yml (XzCore)

```yaml
npc:
  # Backend
  use-citizens: true           # Use Citizens if available
  vanilla-fallback: true       # Use vanilla entities if no Citizens
  
  # Cleanup
  auto-cleanup: true           # Remove orphaned entities on startup
  cleanup-radius: 2.0          # Blocks to check for duplicates
  
  # Behavior defaults
  default:
    invulnerable: true
    silent: true
    look-at-players: true
    look-range: 10.0           # Blocks
    
  # Performance
  tick-interval: 5             # Ticks between look-updates
  max-npcs-per-chunk: 10       # Warning threshold
  
  # Persistence
  save-interval: 300           # Seconds between auto-saves
  backup-count: 5              # Number of backup files to keep
```

---

## Event System

### XzNPC Events (for plugins to listen)

```java
@EventHandler
public void onNPCClick(XzNPCClickEvent event) {
    XzNPC npc = event.getNPC();
    Player player = event.getPlayer();
    
    // Check if it's our NPC
    if (!npc.getPlugin().equals(myPlugin)) return;
    
    // Handle click
    plugin.getLogger().info(player.getName() + " clicked " + npc.getFullId());
}

@EventHandler
public void onNPCSpawn(XzNPCSpawnEvent event) {
    // NPC spawned
}

@EventHandler
public void onNPCDespawn(XzNPCDespawnEvent event) {
    // NPC despawned
}
```

---

## Migration Plan

### From XzPvP Current System

```java
// Old (current)
BountyMasterNPC npc = new BountyMasterNPC(plugin, id, name, location);
npc.spawn();
npcs.put(id, npc);

// New (with XzCore)
XzNPC npc = npcManager.createNPC(plugin, id)
    .type(EntityType.WANDERING_TRADER)
    .name(name)
    .location(location)
    .onClick(player -> openBountyGUI(player))
    .spawn();
```

### Migration Script
- Auto-detect old `npcs.yml` format
- Convert to new format on first load
- Keep backup of old file

---

## Open Questions

1. **Should NPCs persist across plugin reloads?**
   - Yes, but only if plugin is still enabled

2. **What happens if a plugin is disabled?**
   - Option A: Despawn all its NPCs
   - Option B: Keep NPCs but disable interaction
   - **Decision:** Option A (clean state)

3. **Should we support NPC skins (player NPCs)?**
   - Yes, but only with Citizens
   - Add `skin(UUID playerUUID)` or `skin(String username)` to builder

4. **Should NPCs have pathfinding/waypoints?**
   - Phase 2 feature
   - Basic: Stay still or patrol small area
   - Advanced: Complex paths (Citizens only)

5. **Integration with other plugins?**
   - Expose API for non-XzPlugins?
   - **Decision:** Yes, but documented as "unstable"

---

## Success Criteria

- [ ] Single command `/xznpc list` shows all server NPCs
- [ ] No duplicate NPCs on server restart
- [ ] Consistent behavior across XzPvP, XzDungeon, XzGraves
- [ ] Works with and without Citizens
- [ ] Admin can manage all NPCs from one interface
- [ ] Migration from old systems is seamless

---

## Next Steps

1. **Review this document** - Any changes needed?
2. **Approve architecture** - Ready to implement?
3. **Create task breakdown** - Assign to sprints
4. **Start Phase 1** - Core infrastructure

---

*Document Version: 1.0*  
*Last Updated: 2026-02-27*  
*Author: XzPlugin Team*
