package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class ElectricMotor implements RecipeItem {

    @Override
    public String getId() {
        return "electric_motor";
    }

    @Override
    public String getDisplayName() {
        return "Electric Motor";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.PISTON;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A powerful electric motor",
                ChatColor.GRAY + "Made with copper wire and electromagnet",
                ChatColor.GRAY + "Used in advanced machinery"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Copper Wire] [Copper Wire] [Copper Wire]
        // [empty] [Electromagnet] [empty]
        // [Copper Wire] [Copper Wire] [Copper Wire]
        
        CopperWire copperWire = new CopperWire();
        Electromagnet electromagnet = new Electromagnet();
        
        return new ItemStack[] {
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            copperWire.getItemStack(),
            new ItemStack(Material.AIR),
            electromagnet.getItemStack(),
            new ItemStack(Material.AIR),
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

