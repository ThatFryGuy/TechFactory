package org.ThefryGuy.techFactory.recipes.workstations.electric;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.components.ElectricMotor;
import org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot;
import org.ThefryGuy.techFactory.recipes.tools.GoldPan;

import java.util.List;

/**
 * Electric Gold Pan - Automated version of Gold Pan and Nether Gold Pan
 * 
 * Processes:
 * - Gravel → Iron Nugget, Sifted Ore Dust, Clay Ball, Flint
 * - Soul Sand/Soil → Nether Quartz, Nether Wart, Blaze Powder, Gold Nugget, Glowstone Dust, Ghast Tear
 * 
 * Speed: 1.0x (same as manual)
 * Energy: 2 J/s
 */
public class ElectricGoldPan implements RecipeItem {

    @Override
    public String getId() {
        return "electric_gold_pan";
    }

    @Override
    public String getDisplayName() {
        return "Electric Gold Pan";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.BROWN_TERRACOTTA;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Automated panning machine",
                ChatColor.GRAY + "Processes gravel and soul sand/soil",
                ChatColor.DARK_GRAY + "⚡ Energy: 2 J/s",
                ChatColor.DARK_GRAY + "⏱ Speed: 1.0x"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe (Enhanced Crafting Table):
        // [empty]           [Gold Pan]       [empty]
        // [Flint]           [Electric Motor] [Flint]
        // [Aluminum Ingot]  [Aluminum Ingot] [Aluminum Ingot]
        
        GoldPan goldPan = new GoldPan();
        ElectricMotor electricMotor = new ElectricMotor();
        AluminumIngot aluminumIngot = new AluminumIngot();
        
        return new ItemStack[] {
            new ItemStack(Material.AIR),
            goldPan.getItemStack(),
            new ItemStack(Material.AIR),
            new ItemStack(Material.FLINT),
            electricMotor.getItemStack(),
            new ItemStack(Material.FLINT),
            aluminumIngot.getItemStack(),
            aluminumIngot.getItemStack(),
            aluminumIngot.getItemStack()
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

