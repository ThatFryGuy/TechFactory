package org.ThefryGuy.techFactory.recipes.energy;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.alloys.DuraluminIngot;
import org.ThefryGuy.techFactory.recipes.alloys.RedstoneAlloyIngot;
import org.ThefryGuy.techFactory.recipes.resources.Sulfate;

import java.util.List;

/**
 * Small Energy Capacitor - Stores and transfers energy
 * 
 * FUNCTIONALITY:
 * - Capacity: 128 J
 * - Range: 7 blocks (connects to machines, generators, regulators, or other capacitors)
 * - Transfers energy between connected blocks
 * - Stores excess energy indefinitely (until broken or used)
 * 
 * USAGE:
 * - Place within 7 blocks of an Electric Machine, Energy Generator, Energy Regulator, or another capacitor
 * - Extends your energy network
 * - Acts as a buffer for excess energy
 */
public class SmallEnergyCapacitor implements RecipeItem {

    @Override
    public String getId() {
        return "small_energy_capacitor";
    }

    @Override
    public String getDisplayName() {
        return "Small Energy Capacitor";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_BULB;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Stores and transfers energy",
                ChatColor.AQUA + "⚡ Capacity: 128 J",
                ChatColor.AQUA + "⚡ Range: 7 blocks",
                "",
                ChatColor.YELLOW + "Place within 7 blocks of:",
                ChatColor.YELLOW + "• Electric Machines",
                ChatColor.YELLOW + "• Energy Generators",
                ChatColor.YELLOW + "• Energy Regulators",
                ChatColor.YELLOW + "• Other Capacitors"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe (Enhanced Crafting Table):
        // [Duralumin Ingot] [Sulfate]              [Duralumin Ingot]
        // [Redstone Alloy]  [Energy Connector]     [Redstone Alloy]
        // [Duralumin Ingot] [Redstone Dust]        [Duralumin Ingot]
        
        DuraluminIngot duralumin = new DuraluminIngot();
        Sulfate sulfate = new Sulfate();
        RedstoneAlloyIngot redstoneAlloy = new RedstoneAlloyIngot();
        EnergyConnector energyConnector = new EnergyConnector();
        
        return new ItemStack[] {
            duralumin.getItemStack(),           // [0] - Duralumin Ingot
            sulfate.getItemStack(),             // [1] - Sulfate
            duralumin.getItemStack(),           // [2] - Duralumin Ingot
            redstoneAlloy.getItemStack(),       // [3] - Redstone Alloy Ingot
            energyConnector.getItemStack(),     // [4] - Energy Connector
            redstoneAlloy.getItemStack(),       // [5] - Redstone Alloy Ingot
            duralumin.getItemStack(),           // [6] - Duralumin Ingot
            new ItemStack(Material.REDSTONE),   // [7] - Redstone Dust
            duralumin.getItemStack()            // [8] - Duralumin Ingot
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

