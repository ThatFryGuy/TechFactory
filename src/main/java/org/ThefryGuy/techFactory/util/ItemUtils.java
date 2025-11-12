package org.ThefryGuy.techFactory.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Utility class for safe ItemStack and ItemMeta operations.
 * 
 * Prevents NullPointerExceptions by providing defensive null checks
 * for common ItemMeta operations.
 * 
 * Benefits:
 * - Centralized null-safety logic (DRY principle)
 * - Consistent error handling across all machines
 * - Easy to extend for future features (localization, custom formatting)
 * - Reduces code duplication (used in 10+ machine classes)
 */
public class ItemUtils {

    /**
     * Safely get the display name of an ItemStack.
     * 
     * Returns the custom display name if present, otherwise returns
     * the material type name as a fallback.
     * 
     * @param item The ItemStack to get the name from
     * @return The display name, or material name, or "Unknown" if item is null
     */
    public static String getSafeDisplayName(ItemStack item) {
        if (item == null) {
            return "Unknown";
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        
        // Fallback to material name (formatted)
        return formatMaterialName(item.getType());
    }

    /**
     * Safely get the display name with a custom color.
     * 
     * Useful for success/error messages with colored item names.
     * 
     * @param item The ItemStack to get the name from
     * @param color The ChatColor to apply
     * @return The colored display name
     */
    public static String getSafeDisplayName(ItemStack item, ChatColor color) {
        return color + getSafeDisplayName(item);
    }

    /**
     * Format a Material enum name into a readable string.
     * 
     * Examples:
     * - IRON_INGOT -> "Iron Ingot"
     * - BLAST_FURNACE -> "Blast Furnace"
     * 
     * @param material The material to format
     * @return Formatted material name
     */
    public static String formatMaterialName(Material material) {
        if (material == null) {
            return "Unknown";
        }
        
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            // Capitalize first letter
            formatted.append(Character.toUpperCase(word.charAt(0)))
                     .append(word.substring(1));
        }
        
        return formatted.toString();
    }

    /**
     * Safely get the TechFactory item ID from an ItemStack.
     * 
     * Checks the PersistentDataContainer for the "item_id" key.
     * 
     * @param item The ItemStack to check
     * @param plugin The plugin instance (for NamespacedKey)
     * @return The item ID, or null if not a TechFactory item
     */
    public static String getItemId(ItemStack item, JavaPlugin plugin) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Check if an ItemStack has a specific TechFactory item ID.
     * 
     * @param item The ItemStack to check
     * @param itemId The expected item ID
     * @param plugin The plugin instance
     * @return true if the item has the specified ID
     */
    public static boolean hasItemId(ItemStack item, String itemId, JavaPlugin plugin) {
        String actualId = getItemId(item, plugin);
        return actualId != null && actualId.equals(itemId);
    }

    /**
     * Safely check if an ItemStack is null or AIR.
     * 
     * @param item The ItemStack to check
     * @return true if the item is null or AIR
     */
    public static boolean isNullOrAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Safely clone an ItemStack.
     * 
     * @param item The ItemStack to clone
     * @return A clone of the item, or null if input is null
     */
    public static ItemStack safeClone(ItemStack item) {
        return (item != null) ? item.clone() : null;
    }

    /**
     * Create a success message for crafting operations.
     * 
     * Example: "✓ Crafted 5x Iron Dust!"
     * 
     * @param item The crafted item
     * @param verb The action verb ("Crafted", "Crushed", "Smelted", etc.)
     * @return Formatted success message
     */
    public static String createCraftMessage(ItemStack item, String verb) {
        int amount = (item != null) ? item.getAmount() : 1;
        String itemName = getSafeDisplayName(item);
        
        if (amount > 1) {
            return ChatColor.GREEN + "✓ " + verb + " " + amount + "x " + itemName + "!";
        } else {
            return ChatColor.GREEN + "✓ " + verb + " " + itemName + "!";
        }
    }

    /**
     * Create a progress message for smelting/processing operations.
     * 
     * Example: "Smelting 5x Iron Ingot..."
     * 
     * @param item The item being processed
     * @param action The action ("Smelting", "Crushing", "Compressing", etc.)
     * @return Formatted progress message
     */
    public static String createProgressMessage(ItemStack item, String action) {
        int amount = (item != null) ? item.getAmount() : 1;
        String itemName = getSafeDisplayName(item);
        
        if (amount > 1) {
            return ChatColor.YELLOW + action + " " + amount + "x " + itemName + ChatColor.YELLOW + "...";
        } else {
            return ChatColor.YELLOW + action + " " + itemName + ChatColor.YELLOW + "...";
        }
    }

    /**
     * Create an error message for missing ingredients.
     * 
     * Example: "✗ Missing: 64x Magnesium Dust"
     * 
     * @param item The missing item
     * @param required The required amount
     * @return Formatted error message
     */
    public static String createMissingItemMessage(ItemStack item, int required) {
        String itemName = getSafeDisplayName(item);
        return ChatColor.RED + "✗ Missing: " + required + "x " + itemName;
    }

    /**
     * Get a short description of an ItemStack for logging.
     * 
     * Example: "IRON_INGOT x5 (Iron Ingot)"
     * 
     * @param item The ItemStack to describe
     * @return Short description string
     */
    public static String getItemDescription(ItemStack item) {
        if (item == null) {
            return "null";
        }
        
        String displayName = getSafeDisplayName(item);
        int amount = item.getAmount();
        Material type = item.getType();
        
        if (amount > 1) {
            return type.name() + " x" + amount + " (" + displayName + ")";
        } else {
            return type.name() + " (" + displayName + ")";
        }
    }

    /**
     * Check if a material is a fence type.
     *
     * This centralizes fence type checking used by multiblock machines,
     * eliminating 30+ lines of duplicate code across 4 files.
     *
     * Supports all vanilla fence types including Nether variants.
     *
     * @param material The material to check
     * @return true if the material is any fence type, false otherwise
     */
    public static boolean isFenceType(Material material) {
        return material == Material.OAK_FENCE
            || material == Material.SPRUCE_FENCE
            || material == Material.BIRCH_FENCE
            || material == Material.JUNGLE_FENCE
            || material == Material.ACACIA_FENCE
            || material == Material.DARK_OAK_FENCE
            || material == Material.CRIMSON_FENCE
            || material == Material.WARPED_FENCE
            || material == Material.MANGROVE_FENCE
            || material == Material.BAMBOO_FENCE
            || material == Material.CHERRY_FENCE
            || material == Material.NETHER_BRICK_FENCE;
    }

    /**
     * Get the TechFactory item ID from an ItemStack's NBT data.
     *
     * This centralizes the logic for extracting custom item IDs from persistent data containers,
     * eliminating 30+ lines of duplicate code across 5 machine files.
     *
     * @param item The item to check
     * @return The item ID, or null if not a TechFactory item
     */
    public static String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        NamespacedKey key = new NamespacedKey("techfactory", "item_id");

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }

        return null;
    }

    /**
     * Try to output an item to an adjacent chest.
     *
     * Checks all blocks adjacent to the provided multiblock blocks for chests
     * (CHEST or TRAPPED_CHEST) and attempts to add the output item.
     *
     * This centralizes the chest output logic used by all multiblock machines,
     * eliminating 150+ lines of duplicate code across 5 machine files.
     *
     * @param multiblockBlocks List of all blocks that are part of the multiblock structure
     * @param output The item to output to the chest
     * @return true if the item was successfully added to a chest, false otherwise
     */
    public static boolean outputToChest(List<Block> multiblockBlocks, ItemStack output) {
        if (multiblockBlocks == null || output == null) {
            return false;
        }

        // Check all blocks adjacent to each multiblock block
        for (Block centerBlock : multiblockBlocks) {
            Block[] adjacent = {
                centerBlock.getRelative(1, 0, 0),   // East
                centerBlock.getRelative(-1, 0, 0),  // West
                centerBlock.getRelative(0, 0, 1),   // South
                centerBlock.getRelative(0, 0, -1),  // North
                centerBlock.getRelative(0, 1, 0),   // Up
                centerBlock.getRelative(0, -1, 0)   // Down
            };

            for (Block block : adjacent) {
                // Skip if this block is part of the multiblock itself
                if (multiblockBlocks.contains(block)) {
                    continue;
                }

                // Check if it's a chest
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                    if (block.getState() instanceof org.bukkit.block.Chest) {
                        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                        Inventory chestInv = chest.getInventory();
                        chestInv.addItem(output);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}

