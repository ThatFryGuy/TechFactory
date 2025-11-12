package org.ThefryGuy.techFactory.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Centralized registry for vanilla Minecraft item IDs
 * Used for recipe matching across ALL machines (Smelter, Ore Crusher, Basic Workbench, etc.)
 * 
 * WHY THIS EXISTS:
 * - Before: Each machine had its own getVanillaItemId() method with duplicate switch statements
 * - After: One centralized place to manage vanilla item support
 * - When adding new machines, just use VanillaItemRegistry.getVanillaItemId()
 * - If vanilla item support changes, edit ONE file instead of 7+
 * 
 * USAGE:
 *   String itemId = VanillaItemRegistry.getVanillaItemId(itemStack);
 *   if (itemId != null) {
 *       // It's a supported vanilla item
 *   }
 */
public class VanillaItemRegistry {
    
    /**
     * Get the item ID string for a vanilla Minecraft item
     * 
     * @param item The ItemStack to check
     * @return The item ID (e.g., "netherrack", "glass"), or null if not a supported vanilla item
     */
    public static String getVanillaItemId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        Material mat = item.getType();
        switch (mat) {
            // ===== ORE CRUSHER ITEMS =====
            case NETHERRACK: return "netherrack";
            
            // ===== SMELTER ITEMS =====
            case GLASS: return "glass";
            case GLASS_PANE: return "glass_pane";
            case LAPIS_LAZULI: return "lapis_lazuli";
            case FLINT: return "flint";
            case QUARTZ_BLOCK: return "quartz_block";
            case REDSTONE: return "redstone";
            case REDSTONE_BLOCK: return "redstone_block";
            
            // ===== BASIC WORKBENCH ITEMS =====
            // (Some overlap with Smelter - that's OK, centralized now!)
            case LAPIS_BLOCK: return "lapis_block";
            case STONE: return "stone";
            case BOWL: return "bowl";
            case IRON_INGOT: return "iron_ingot";
            case COPPER_INGOT: return "copper_ingot";
            case GOLD_NUGGET: return "gold_nugget";
            case IRON_NUGGET: return "iron_nugget";
            
            // ===== FUTURE MACHINES =====
            // Add more vanilla items here as you create new machines
            
            default: return null;
        }
    }
    
    /**
     * Check if an item is a supported vanilla item
     * 
     * @param item The ItemStack to check
     * @return true if this is a supported vanilla item, false otherwise
     */
    public static boolean isVanillaItem(ItemStack item) {
        return getVanillaItemId(item) != null;
    }
}

