package org.ThefryGuy.techFactory.recipes.energy;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.resources.Carbon;
import org.ThefryGuy.techFactory.recipes.components.CopperWire;

import java.util.List;

/**
 * Energy Connector - Extends the energy network range
 * 
 * FUNCTIONALITY:
 * - Range: 6 blocks (can place another connector 6 blocks away to extend)
 * - Connects power panels to the energy network
 * - Allows building distributed energy networks
 * 
 * SCALABILITY:
 * - Uses database for persistence (like other placed blocks)
 * - Efficient range checking with distance calculations
 * - Thread-safe network connections via EnergyManager
 */
public class EnergyConnector implements RecipeItem {

    @Override
    public String getId() {
        return "energy_connector";
    }

    @Override
    public String getDisplayName() {
        return "Energy Connector";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.CONDUIT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Extends the energy network range",
                ChatColor.GRAY + "Connect power panels to the network",
                ChatColor.GRAY + "Range: 6 blocks",
                "",
                ChatColor.YELLOW + "Place to extend your energy network",
                ChatColor.YELLOW + "Connect multiple connectors to expand"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe (3x3 grid):
        // [Carbon] [Copper Wire] [Carbon]
        // [Copper Wire] [Redstone Block] [Copper Wire]
        // [Carbon] [Copper Wire] [Carbon]
        
        Carbon carbon = new Carbon();
        CopperWire copperWire = new CopperWire();
        
        return new ItemStack[] {
            carbon.getItemStack(),              // [0] Top-left
            copperWire.getItemStack(),          // [1] Top-center
            carbon.getItemStack(),              // [2] Top-right
            copperWire.getItemStack(),          // [3] Middle-left
            new ItemStack(Material.REDSTONE_BLOCK), // [4] Center
            copperWire.getItemStack(),          // [5] Middle-right
            carbon.getItemStack(),              // [6] Bottom-left
            copperWire.getItemStack(),          // [7] Bottom-center
            carbon.getItemStack()               // [8] Bottom-right
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }

    /**
     * Override getItemStack to output 8 connectors (like Copper Wire)
     * This makes the recipe more economical for building large networks
     */
    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(getMaterial(), 8); // Output 8 connectors
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
}

