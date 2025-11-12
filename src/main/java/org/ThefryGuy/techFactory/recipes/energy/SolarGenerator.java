package org.ThefryGuy.techFactory.recipes.energy;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.components.PhotovoltaicCell;
import org.ThefryGuy.techFactory.recipes.components.ElectricMotor;
import org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot;

import java.util.List;

public class SolarGenerator implements RecipeItem {

    @Override
    public String getId() {
        return "solar_generator";
    }

    @Override
    public String getDisplayName() {
        return "Solar Generator";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.DAYLIGHT_DETECTOR;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Basic solar power generator",
                ChatColor.GRAY + "Generates energy from sunlight",
                ChatColor.AQUA + "⚡ Power: 4 J/s",
                ChatColor.AQUA + "⚡ Buffer: 0 J",
                "",
                ChatColor.YELLOW + "Right-click to place",
                ChatColor.YELLOW + "Must have direct sunlight access"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Photovoltaic Cell] [Photovoltaic Cell] [Photovoltaic Cell]
        // [Aluminum Ingot] [Electric Motor] [Aluminum Ingot]
        // [empty] [Aluminum Ingot] [empty]
        
        PhotovoltaicCell cell = new PhotovoltaicCell();
        AluminumIngot aluminum = new AluminumIngot();
        ElectricMotor motor = new ElectricMotor();
        
        return new ItemStack[] {
            cell.getItemStack(),
            cell.getItemStack(),
            cell.getItemStack(),
            aluminum.getItemStack(),
            motor.getItemStack(),
            aluminum.getItemStack(),
            new ItemStack(Material.AIR),
            aluminum.getItemStack(),
            new ItemStack(Material.AIR)
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

