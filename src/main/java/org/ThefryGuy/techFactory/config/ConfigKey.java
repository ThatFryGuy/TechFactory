package org.ThefryGuy.techFactory.config;

/**
 * Type-safe configuration keys for TechFactory plugin
 * 
 * BENEFITS:
 * - Compile-time safety: Typos caught at compile time, not runtime
 * - IDE autocomplete: Easy to discover available config options
 * - Refactoring support: Renaming a key updates all references
 * - Self-documenting: Each key has type, path, and default value
 * - Centralized: All config keys in one place
 * 
 * USAGE:
 * Instead of: config.getLong("smelting.check_interval_ticks", 5L)
 * Use: ConfigKey.SMELTING_CHECK_INTERVAL_TICKS.getLong(config)
 */
public enum ConfigKey {
    
    // ========================================
    // SMELTING SYSTEM
    // ========================================
    SMELTING_CHECK_INTERVAL_TICKS("smelting.check_interval_ticks", 5L, ConfigType.LONG,
        "How often to check for completed smelting operations (in ticks)"),
    
    SMELTING_DURATION_MS("smelting.duration_ms", 1300L, ConfigType.LONG,
        "How long smelting operations take (in milliseconds)"),
    
    SMELTER_SAVE_INTERVAL_MS("smelting.save_interval_ms", 5000L, ConfigType.LONG,
        "How often to save smelter inventories to database (in milliseconds)"),
    
    SMELTER_DIRTY_CHECK_INTERVAL_TICKS("smelting.dirty_check_interval_ticks", 100L, ConfigType.LONG,
        "How often to check for dirty smelters that need saving (in ticks)"),
    
    SMELTING_NOTIFICATION_DISTANCE("smelting.notification_distance", 16.0, ConfigType.DOUBLE,
        "Maximum distance for smelting completion notifications (in blocks)"),
    
    // ========================================
    // ENERGY SYSTEM
    // ========================================
    HOLOGRAM_RENDER_DISTANCE("energy.hologram_render_distance", 32.0, ConfigType.DOUBLE,
        "Maximum distance for rendering energy holograms (in blocks) - reduced from 64 for performance"),

    ENERGY_UPDATE_INTERVAL_TICKS("energy.update_interval_ticks", 20L, ConfigType.LONG,
        "How often to update energy networks (in ticks)"),

    // ========================================
    // ELECTRIC MACHINES
    // ========================================
    ELECTRIC_MACHINE_IDLE_THRESHOLD_TICKS("electric_machines.idle_threshold_ticks", 20L, ConfigType.LONG,
        "How many ticks a machine can be idle before being removed from active set (20 ticks = 1 second)"),

    ELECTRIC_MACHINE_TASK_INTERVAL_TICKS("electric_machines.task_interval_ticks", 2L, ConfigType.LONG,
        "How often to process electric machines (in ticks) - 2 ticks = 10 times per second"),

    ELECTRIC_MACHINE_MAX_PER_TICK("electric_machines.max_per_tick", 100, ConfigType.INT,
        "Maximum machines to process per tick (Slimefun-style queue-based processing) - 100 = can handle 1000+ machines"),

    // ========================================
    // RATE LIMITING & ANTI-SPAM
    // ========================================
    MULTIBLOCK_CLICK_COOLDOWN_MS("rate_limit.multiblock_cooldown_ms", 500L, ConfigType.LONG,
        "Cooldown between multiblock interactions (in milliseconds)"),
    
    INVENTORY_CLICK_COOLDOWN_MS("rate_limit.inventory_cooldown_ms", 50L, ConfigType.LONG,
        "Cooldown between inventory clicks (in milliseconds)"),
    
    RATE_LIMITER_CLEANUP_INTERVAL_TICKS("rate_limit.cleanup_interval_ticks", 600L, ConfigType.LONG,
        "How often to clean up old rate limiter entries (in ticks)"),
    
    RATE_LIMITER_MAX_AGE_MS("rate_limit.max_age_ms", 300000L, ConfigType.LONG,
        "Maximum age of rate limiter entries before cleanup (in milliseconds)"),
    
    RATE_LIMITER_MAX_SIZE("rate_limit.max_size", 10000, ConfigType.INT,
        "Maximum number of entries in rate limiter before forced cleanup"),
    
    // ========================================
    // PLAYER OPERATION QUEUE LIMITS
    // ========================================
    MAX_MULTIBLOCK_CRAFTS_PER_SECOND("player_limits.max_multiblock_crafts_per_second", 5, ConfigType.INT,
        "Maximum multiblock crafts per second per player"),
    
    MAX_SMELTING_STARTS_PER_SECOND("player_limits.max_smelting_starts_per_second", 10, ConfigType.INT,
        "Maximum smelting operations started per second per player"),
    
    MAX_INVENTORY_CLICKS_PER_SECOND("player_limits.max_inventory_clicks_per_second", 20, ConfigType.INT,
        "Maximum inventory clicks per second per player"),
    
    MAX_BLOCK_PLACES_PER_SECOND("player_limits.max_block_places_per_second", 10, ConfigType.INT,
        "Maximum block placements per second per player"),
    
    MAX_BLOCK_BREAKS_PER_SECOND("player_limits.max_block_breaks_per_second", 10, ConfigType.INT,
        "Maximum block breaks per second per player"),
    
    RATE_LIMIT_TIME_WINDOW_MS("player_limits.rate_limit_time_window_ms", 1000L, ConfigType.LONG,
        "Time window for rate limiting (in milliseconds)"),
    
    // ========================================
    // NOTIFICATION SYSTEM
    // ========================================
    MESSAGE_SPAM_COOLDOWN_MS("notifications.message_spam_cooldown_ms", 1000L, ConfigType.LONG,
        "Cooldown between duplicate messages to prevent spam (in milliseconds)"),
    
    ENABLE_SMELTING_MESSAGES("notifications.enable_smelting_messages", true, ConfigType.BOOLEAN,
        "Whether to send smelting completion messages to players"),
    
    // ========================================
    // DATABASE & PERFORMANCE
    // ========================================
    SQLITE_CACHE_SIZE_KB("database.sqlite_cache_size_kb", 65536, ConfigType.INT,
        "SQLite cache size (in kilobytes)"),
    
    SQLITE_MMAP_SIZE_BYTES("database.sqlite_mmap_size_bytes", 268435456, ConfigType.INT,
        "SQLite memory-mapped I/O size (in bytes)"),
    
    AUTO_SAVE_INTERVAL_TICKS("database.auto_save_interval_ticks", 6000L, ConfigType.LONG,
        "How often to auto-save all data (in ticks)"),
    
    // ========================================
    // SMELTER GUI LAYOUT
    // ========================================
    SMELTER_GUI_SIZE("smelter_gui.size", 27, ConfigType.INT,
        "Size of smelter GUI inventory (in slots)"),
    
    SMELTER_OUTPUT_SLOT("smelter_gui.output_slot", 16, ConfigType.INT,
        "Slot number for smelter output"),
    
    // ========================================
    // VALIDATION & LIMITS
    // ========================================
    MAX_MULTIBLOCK_INVENTORY_SIZE("limits.max_multiblock_inventory_size", 54, ConfigType.INT,
        "Maximum multiblock inventory size (in slots)"),
    
    MAX_RECIPE_COMPLEXITY("limits.max_recipe_complexity", 9, ConfigType.INT,
        "Maximum recipe complexity (number of unique items)"),
    
    // ========================================
    // LOGGING & DEBUGGING
    // ========================================
    LOG_CHUNK_LOADS("debug.log_chunk_loads", false, ConfigType.BOOLEAN,
        "Whether to log chunk load events"),
    
    LOG_RATE_LIMITER_CLEANUP("debug.log_rate_limiter_cleanup", false, ConfigType.BOOLEAN,
        "Whether to log rate limiter cleanup operations"),
    
    LOG_DATABASE_OPERATIONS("debug.log_database_operations", false, ConfigType.BOOLEAN,
        "Whether to log database operations");
    
    // ========================================
    // ENUM IMPLEMENTATION
    // ========================================
    
    private final String path;
    private final Object defaultValue;
    private final ConfigType type;
    private final String description;
    
    ConfigKey(String path, Object defaultValue, ConfigType type, String description) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.type = type;
        this.description = description;
    }
    
    public String getPath() {
        return path;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public ConfigType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get value from config as Long
     */
    public long getLong(org.bukkit.configuration.file.FileConfiguration config) {
        if (type != ConfigType.LONG) {
            throw new IllegalStateException("ConfigKey " + name() + " is not a LONG type!");
        }
        return config.getLong(path, (Long) defaultValue);
    }
    
    /**
     * Get value from config as Integer
     */
    public int getInt(org.bukkit.configuration.file.FileConfiguration config) {
        if (type != ConfigType.INT) {
            throw new IllegalStateException("ConfigKey " + name() + " is not an INT type!");
        }
        return config.getInt(path, (Integer) defaultValue);
    }
    
    /**
     * Get value from config as Double
     */
    public double getDouble(org.bukkit.configuration.file.FileConfiguration config) {
        if (type != ConfigType.DOUBLE) {
            throw new IllegalStateException("ConfigKey " + name() + " is not a DOUBLE type!");
        }
        return config.getDouble(path, (Double) defaultValue);
    }
    
    /**
     * Get value from config as Boolean
     */
    public boolean getBoolean(org.bukkit.configuration.file.FileConfiguration config) {
        if (type != ConfigType.BOOLEAN) {
            throw new IllegalStateException("ConfigKey " + name() + " is not a BOOLEAN type!");
        }
        return config.getBoolean(path, (Boolean) defaultValue);
    }
    
    /**
     * Get value from config as String
     */
    public String getString(org.bukkit.configuration.file.FileConfiguration config) {
        if (type != ConfigType.STRING) {
            throw new IllegalStateException("ConfigKey " + name() + " is not a STRING type!");
        }
        return config.getString(path, (String) defaultValue);
    }
}

