package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class HeatingCoil implements RecipeItem {

    @Override
    public String getId() {
        return "heating_coil";
    }

    @Override
    public String getDisplayName() {
        return "Heating Coil";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_ROD;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An advanced heating element",
                ChatColor.GRAY + "Made with copper wire and electric motor",
                ChatColor.GRAY + "Used in high-temperature machinery"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Copper Wire] [Copper Wire] [Copper Wire]
        // [Copper Wire] [Electric Motor] [Copper Wire]
        // [Copper Wire] [Copper Wire] [Copper Wire]
        
        CopperWire copperWire = new CopperWire();
        ElectricMotor electricMotor = new ElectricMotor();
        
        return new ItemStack[] {
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            electricMotor.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack()
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

