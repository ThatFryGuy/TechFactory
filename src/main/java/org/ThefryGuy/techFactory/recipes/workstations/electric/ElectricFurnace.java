package org.ThefryGuy.techFactory.recipes.workstations.electric;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.alloys.GildedIron;
import org.ThefryGuy.techFactory.recipes.components.ElectricMotor;
import org.ThefryGuy.techFactory.recipes.components.HeatingCoil;

import java.util.List;

/**
 * Electric Furnace - Tier 1 Electric Machine
 * 
 * Smelts items using electricity instead of fuel.
 * Stats:
 * - Power: 4 J/SF (Joules per Smelt Furnace tick)
 * - Tick Speed Multiplier: 1.0x (same speed as vanilla furnace)
 * - Tier: 1
 */
public class ElectricFurnace implements RecipeItem {

    @Override
    public String getId() {
        return "electric_furnace";
    }

    @Override
    public String getDisplayName() {
        return "Electric Furnace";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.FURNACE;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A furnace powered by electricity",
                ChatColor.GRAY + "Smelts items without fuel",
                "",
                ChatColor.AQUA + "⚡ Tier: " + ChatColor.WHITE + "1",
                ChatColor.AQUA + "⚡ Power: " + ChatColor.WHITE + "4 J/SF",
                ChatColor.AQUA + "⚡ Speed: " + ChatColor.WHITE + "1.0x",
                "",
                ChatColor.YELLOW + "Right-click to place",
                ChatColor.YELLOW + "Must be connected to energy network"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe (Enhanced Crafting Table):
        // [empty]        [Furnace]       [empty]
        // [Gilded Iron]  [Heating Coil]  [Gilded Iron]
        // [Gilded Iron]  [Electric Motor][Gilded Iron]
        
        GildedIron gildedIron = new GildedIron();
        HeatingCoil heatingCoil = new HeatingCoil();
        ElectricMotor electricMotor = new ElectricMotor();
        
        return new ItemStack[] {
            new ItemStack(Material.AIR),        // [0]
            new ItemStack(Material.FURNACE),    // [1]
            new ItemStack(Material.AIR),        // [2]
            gildedIron.getItemStack(),          // [3]
            heatingCoil.getItemStack(),         // [4]
            gildedIron.getItemStack(),          // [5]
            gildedIron.getItemStack(),          // [6]
            electricMotor.getItemStack(),       // [7]
            gildedIron.getItemStack()           // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

