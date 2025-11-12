package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class CopperWire implements RecipeItem {

    @Override
    public String getId() {
        return "copper_wire";
    }

    @Override
    public String getDisplayName() {
        return "Copper Wire";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.STRING;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Thin copper wire for electrical work",
                ChatColor.GRAY + "Used in motors and coils",
                ChatColor.GRAY + "Crafted from copper ingots"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 3x Copper Ingot (outputs 8x Copper Wire)
        return new ItemStack[] {
            new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.COPPER_INGOT),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(getMaterial(), 8); // Output 8 wires
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

