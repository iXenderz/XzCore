# Contributing to XzCore

Thank you for contributing to XzCore! This is the core library that powers all XzPlugins.

## What is XzCore?

XzCore is a shared library providing:
- **DatabaseManager** - HikariCP connection pooling with SQLite/MySQL support
- **EventBus** - Inter-plugin communication
- **PlayerDataManager** - Caching and auto-save for player data

## Quick Setup

### 1. Prerequisites

```bash
# Check Java version (must be 21)
java -version
```

### 2. Clone and Build

```bash
git clone <repo-url>
cd XzCore

# Build
./gradlew build

# Publish to Maven Local (for other plugins to use)
./gradlew publishToMavenLocal
```

### 3. IDE Setup

**VS Code (Recommended):**
- Open folder in VS Code
- Install "Extension Pack for Java"
- Debug configs included in `.vscode/`

**IntelliJ IDEA:**
- Import as Gradle project
- Set Project SDK to Java 21

### 4. Verify Setup

```bash
# Run tests
./gradlew test

# Publish to local Maven
./gradlew publishToMavenLocal

# Verify it's available
ls ~/.m2/repository/com/xzatrix/xzcore/
```

## Development Workflow

### Building

```bash
# Quick build
./scripts/build.sh

# Or use VS Code tasks (Ctrl+Shift+P → Run Task):
# - gradle: build
# - gradle: test
```

### Publishing to Maven Local

After making changes to XzCore, you need to publish it for other plugins to use:

```bash
./scripts/publish-local.sh
```

This makes XzCore available at `~/.m2/repository/com/xzatrix/xzcore/1.0.0/`.

Other plugins will automatically pick up the new version on their next build.

## Project Structure

```
src/
├── main/java/com/xzatrix/xzcore/
│   ├── XzCore.java             # Library entry point
│   ├── XzCoreAPI.java          # Public API
│   ├── database/               # Database management
│   │   ├── DatabaseManager.java
│   │   └── ConnectionPool.java
│   ├── event/                  # Event bus
│   │   ├── EventBus.java
│   │   └── XzEvent.java
│   ├── player/                 # Player data
│   │   ├── PlayerDataManager.java
│   │   └── PlayerData.java
│   └── util/                   # Utilities
│       └── Logger.java
└── test/java/                  # Unit tests
```

## Coding Standards

### Style Guide

- **Indentation:** 4 spaces (no tabs)
- **Line length:** 120 characters max
- **Braces:** Same line (K&R style)
- **Imports:** No wildcards, organize imports

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `DatabaseManager`, `EventBus` |
| Methods | camelCase | `getConnection()`, `publishEvent()` |
| Variables | camelCase | `dataSource`, `eventListeners` |
| Constants | UPPER_SNAKE_CASE | `DEFAULT_POOL_SIZE`, `MAX_RETRIES` |
| Packages | lowercase | `com.xzatrix.xzcore.database` |

### Code Patterns

**Singleton Service:**
```java
public class DatabaseManager {
    private static DatabaseManager instance;
    
    public static synchronized DatabaseManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new DatabaseManager(plugin);
        }
        return instance;
    }
    
    private DatabaseManager(JavaPlugin plugin) {
        // Private constructor
    }
}
```

**Thread-Safe Operations:**
```java
private final ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

public PlayerData getPlayerData(UUID uuid) {
    return playerData.computeIfAbsent(uuid, k -> new PlayerData(k));
}
```

**Resource Management:**
```java
public void close() {
    if (dataSource != null && !dataSource.isClosed()) {
        dataSource.close();
    }
}
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Skip tests for faster build
./gradlew build -x test
```

### Test Structure

```java
@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {
    
    @Mock
    private JavaPlugin plugin;
    
    private DatabaseManager manager;
    
    @BeforeEach
    void setUp() {
        when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
        manager = DatabaseManager.getInstance(plugin);
    }
    
    @Test
    void testGetConnection() {
        // Given
        manager.initialize();
        
        // When
        Connection conn = manager.getConnection();
        
        // Then
        assertNotNull(conn);
    }
}
```

## Code Templates

Use the included templates in `.templates/`:

```bash
# Generate new service
./scripts/new-service.sh Cache

# Generate new manager
./scripts/new-manager.sh Connection
```

## Pull Request Process

1. **Create Branch:**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes:**
   - Write code following style guide
   - Add/update tests
   - Update documentation if needed

3. **Verify:**
   ```bash
   ./gradlew check
   ./gradlew publishToMavenLocal
   ```

4. **Test with Dependent Plugins:**
   After publishing locally, build plugins that use XzCore:
   ```bash
   cd ../XzDungeon
   ./gradlew shadowJar  # Should pick up new XzCore
   ```

5. **Commit:**
   ```bash
   git commit -m "feat: add new feature"
   ```

   Commit message format:
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation changes
   - `test:` Adding tests
   - `refactor:` Code refactoring
   - `chore:` Maintenance tasks

6. **Push and Create PR**

## Troubleshooting

### Build Fails with Java Version

```bash
export JAVA_HOME=/path/to/java21
./gradlew build
```

### IDE Can't Find Dependencies

```bash
./gradlew --refresh-dependencies
```

### Changes Not Reflecting in Other Plugins

Make sure you've published to Maven Local:
```bash
./gradlew publishToMavenLocal
```

Then force refresh in dependent plugin:
```bash
cd ../XzDungeon
./gradlew --refresh-dependencies shadowJar
```

## Resources

- [Paper API Docs](https://jd.papermc.io/paper/1.21/)
- [Gradle Docs](https://docs.gradle.org/)
- [HikariCP Wiki](https://github.com/brettwooldridge/HikariCP/wiki)

## Questions?

Open an issue or ask in the project Discord.
