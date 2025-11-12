package org.ThefryGuy.techFactory;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ThefryGuy.techFactory.config.ConfigKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Configuration manager for TechFactory plugin
 *
 * PRIORITY 1: Allows server admins to modify game balance without code changes
 *
 * Benefits:
 * - Admin autonomy - No dev needed for balance tweaks
 * - Zero downtime - Changes apply immediately with /reload
 * - Safe defaults - First-time installation works out of the box
 * - Future-proof - Easy to add new options without code changes
 * - Professional - Expected feature for production servers
 *
 * REFACTORED: Now uses type-safe ConfigKey enum instead of string-based keys
 * - Compile-time safety: Typos caught at compile time
 * - IDE autocomplete: Easy to discover config options
 * - Refactoring support: Renaming updates all references
 *
 * Usage:
 * - TechFactoryConfig.load(plugin) - Load config on startup
 * - TechFactoryConfig.reload() - Reload config without restart
 * - TechFactoryConfig.getSmeltingDurationMs() - Get config value (backward compatible)
 * - ConfigKey.SMELTING_DURATION_MS.getLong(config) - New type-safe way
 */
public final class TechFactoryConfig {

    private static TechFactory plugin;
    private static FileConfiguration config;
    private static File configFile;

    // Prevent instantiation
    private TechFactoryConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Load configuration from config.yml
     * Creates default config if it doesn't exist
     * 
     * @param pluginInstance The TechFactory plugin instance
     */
    public static void load(TechFactory pluginInstance) {
        plugin = pluginInstance;
        
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Create config file reference
        configFile = new File(plugin.getDataFolder(), "config.yml");

        // If config doesn't exist, save default from resources
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Created default config.yml");
        }

        // Load config from file
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load defaults from resources (for any missing keys)
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            try (InputStreamReader reader = new InputStreamReader(defaultStream)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                config.setDefaults(defaultConfig);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load default config: " + e.getMessage());
            }
        }

        plugin.getLogger().info("Configuration loaded successfully!");
    }

    /**
     * Reload configuration from disk
     * Called by /techfactory reload command
     */
    public static void reload() {
        if (configFile == null || plugin == null) {
            throw new IllegalStateException("Config not initialized! Call load() first.");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Reload defaults
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            try (InputStreamReader reader = new InputStreamReader(defaultStream)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                config.setDefaults(defaultConfig);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[TechFactory] Failed to reload default config: " + e.getMessage());
            }
        }

        Bukkit.getLogger().info("[TechFactory] Configuration reloaded!");
    }

    /**
     * Save current config to disk
     */
    public static void save() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config.yml!", e);
        }
    }

    // ========================================
    // SMELTING SYSTEM
    // ========================================

    public static long getSmeltingCheckIntervalTicks() {
        return ConfigKey.SMELTING_CHECK_INTERVAL_TICKS.getLong(config);
    }

    public static long getSmeltingDurationMs() {
        return ConfigKey.SMELTING_DURATION_MS.getLong(config);
    }

    public static long getSmelterSaveIntervalMs() {
        return ConfigKey.SMELTER_SAVE_INTERVAL_MS.getLong(config);
    }

    public static long getSmelterDirtyCheckIntervalTicks() {
        return ConfigKey.SMELTER_DIRTY_CHECK_INTERVAL_TICKS.getLong(config);
    }

    public static double getSmeltingNotificationDistance() {
        return ConfigKey.SMELTING_NOTIFICATION_DISTANCE.getDouble(config);
    }

    // ========================================
    // ENERGY SYSTEM
    // ========================================

    public static double getHologramRenderDistance() {
        return ConfigKey.HOLOGRAM_RENDER_DISTANCE.getDouble(config);
    }

    public static long getEnergyUpdateIntervalTicks() {
        return ConfigKey.ENERGY_UPDATE_INTERVAL_TICKS.getLong(config);
    }

    // ========================================
    // ELECTRIC MACHINES
    // ========================================

    public static long getElectricMachineIdleThresholdTicks() {
        return ConfigKey.ELECTRIC_MACHINE_IDLE_THRESHOLD_TICKS.getLong(config);
    }

    public static long getElectricMachineTaskIntervalTicks() {
        return ConfigKey.ELECTRIC_MACHINE_TASK_INTERVAL_TICKS.getLong(config);
    }

    public static int getElectricMachineMaxPerTick() {
        return ConfigKey.ELECTRIC_MACHINE_MAX_PER_TICK.getInt(config);
    }

    // ========================================
    // RATE LIMITING & ANTI-SPAM
    // ========================================

    public static long getMultiblockClickCooldownMs() {
        return ConfigKey.MULTIBLOCK_CLICK_COOLDOWN_MS.getLong(config);
    }

    public static long getInventoryClickCooldownMs() {
        return ConfigKey.INVENTORY_CLICK_COOLDOWN_MS.getLong(config);
    }

    public static long getRateLimiterCleanupIntervalTicks() {
        return ConfigKey.RATE_LIMITER_CLEANUP_INTERVAL_TICKS.getLong(config);
    }

    public static long getRateLimiterMaxAgeMs() {
        return ConfigKey.RATE_LIMITER_MAX_AGE_MS.getLong(config);
    }

    public static int getRateLimiterMaxSize() {
        return ConfigKey.RATE_LIMITER_MAX_SIZE.getInt(config);
    }

    // ========================================
    // PLAYER OPERATION QUEUE LIMITS
    // ========================================

    public static int getMaxMultiblockCraftsPerSecond() {
        return ConfigKey.MAX_MULTIBLOCK_CRAFTS_PER_SECOND.getInt(config);
    }

    public static int getMaxSmeltingStartsPerSecond() {
        return ConfigKey.MAX_SMELTING_STARTS_PER_SECOND.getInt(config);
    }

    public static int getMaxInventoryClicksPerSecond() {
        return ConfigKey.MAX_INVENTORY_CLICKS_PER_SECOND.getInt(config);
    }

    public static int getMaxBlockPlacesPerSecond() {
        return ConfigKey.MAX_BLOCK_PLACES_PER_SECOND.getInt(config);
    }

    public static int getMaxBlockBreaksPerSecond() {
        return ConfigKey.MAX_BLOCK_BREAKS_PER_SECOND.getInt(config);
    }

    public static long getRateLimitTimeWindowMs() {
        return ConfigKey.RATE_LIMIT_TIME_WINDOW_MS.getLong(config);
    }

    // ========================================
    // NOTIFICATION SYSTEM
    // ========================================

    public static long getMessageSpamCooldownMs() {
        return ConfigKey.MESSAGE_SPAM_COOLDOWN_MS.getLong(config);
    }

    public static boolean isSmeltingMessagesEnabled() {
        return ConfigKey.ENABLE_SMELTING_MESSAGES.getBoolean(config);
    }

    // ========================================
    // DATABASE & PERFORMANCE
    // ========================================

    public static int getSqliteCacheSizeKb() {
        return ConfigKey.SQLITE_CACHE_SIZE_KB.getInt(config);
    }

    public static int getSqliteMmapSizeBytes() {
        return ConfigKey.SQLITE_MMAP_SIZE_BYTES.getInt(config);
    }

    public static long getAutoSaveIntervalTicks() {
        return ConfigKey.AUTO_SAVE_INTERVAL_TICKS.getLong(config);
    }

    // ========================================
    // SMELTER GUI LAYOUT
    // ========================================

    public static int getSmelterGuiSize() {
        return ConfigKey.SMELTER_GUI_SIZE.getInt(config);
    }

    public static int getSmelterOutputSlot() {
        return ConfigKey.SMELTER_OUTPUT_SLOT.getInt(config);
    }

    // ========================================
    // VALIDATION & LIMITS
    // ========================================

    public static int getMaxMultiblockInventorySize() {
        return ConfigKey.MAX_MULTIBLOCK_INVENTORY_SIZE.getInt(config);
    }

    public static int getMaxRecipeComplexity() {
        return ConfigKey.MAX_RECIPE_COMPLEXITY.getInt(config);
    }

    // ========================================
    // LOGGING & DEBUGGING
    // ========================================

    public static boolean isLogChunkLoads() {
        return ConfigKey.LOG_CHUNK_LOADS.getBoolean(config);
    }

    public static boolean isLogRateLimiterCleanup() {
        return ConfigKey.LOG_RATE_LIMITER_CLEANUP.getBoolean(config);
    }

    public static boolean isLogDatabaseOperations() {
        return ConfigKey.LOG_DATABASE_OPERATIONS.getBoolean(config);
    }

    /**
     * Get raw FileConfiguration for advanced usage
     */
    public static FileConfiguration getConfig() {
        return config;
    }
}

