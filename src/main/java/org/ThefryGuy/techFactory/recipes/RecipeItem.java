package org.ThefryGuy.techFactory.recipes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Interface for all recipe items (dusts, ingots, machines, circuits, etc.)
 *
 * SIMPLIFIED DESIGN (Slimefun-style):
 * - Just define the item properties (id, name, material, lore)
 * - Define the recipe as an ItemStack array (like Slimefun)
 * - Define the machine type
 * - Everything else is automatic!
 */
public interface RecipeItem {

    // ========================================
    // ITEM PROPERTIES (Required)
    // ========================================

    /**
     * Get the unique ID for this recipe item (e.g. "iron_dust")
     */
    String getId();

    /**
     * Get the display name (e.g. "Iron Dust")
     */
    String getDisplayName();

    /**
     * Get the chat color for this item
     */
    ChatColor getColor();

    /**
     * Get the Minecraft material representation
     */
    Material getMaterial();

    /**
     * Get the lore/description lines
     */
    List<String> getLore();

    // ========================================
    // RECIPE DEFINITION (Required)
    // ========================================

    /**
     * Get the recipe as a 9-slot ItemStack array (like Slimefun).
     *
     * Example for Iron Dust (grinding):
     *   [Iron Ore, null, null, null, null, null, null, null, null]
     *
     * Example for Bronze Ingot (smeltery - 2 inputs):
     *   [Copper Dust, Copper Dust, Copper Dust, Tin Dust, null, null, null, null, null]
     *
     * The array represents a 3x3 grid:
     *   [0] [1] [2]
     *   [3] [4] [5]
     *   [6] [7] [8]
     */
    ItemStack[] getRecipe();

    /**
     * Get the machine type for this recipe (e.g. "Grinding Stone", "Smelter", "Ore Washer")
     */
    String getMachineType();

    // ========================================
    // AUTOMATIC METHODS (Don't override)
    // ========================================

    /**
     * Get the ItemStack representation for the inventory.
     * This is automatically generated from the item properties.
     *
     * IMPORTANT: This adds NBT data (PersistentDataContainer) to prevent players
     * from faking items with anvil renames!
     */
    default ItemStack getItemStack() {
        ItemStack stack = new ItemStack(getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // Set display name and lore
            meta.setDisplayName(getColor() + getDisplayName());
            meta.setLore(getLore());

            // Add NBT data with unique ID (prevents anvil faking!)
            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(RecipeItem.class);
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getId());

            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Check if an ItemStack is a valid TechFactory item with the given ID.
     * This checks the NBT data, not just the display name!
     *
     * @param item The ItemStack to check
     * @param id The expected item ID (e.g. "iron_dust")
     * @return true if the item has the correct NBT ID
     */
    static boolean isValidItem(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(RecipeItem.class);
        NamespacedKey key = new NamespacedKey(plugin, "item_id");

        String storedId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return storedId != null && storedId.equals(id);
    }

    /**
     * Get the TechFactory item ID from an ItemStack (if it has one).
     *
     * @param item The ItemStack to check
     * @return The item ID, or null if not a TechFactory item
     */
    static String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {  // Defensive null check
            return null;
        }

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(RecipeItem.class);
        NamespacedKey key = new NamespacedKey(plugin, "item_id");

        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}