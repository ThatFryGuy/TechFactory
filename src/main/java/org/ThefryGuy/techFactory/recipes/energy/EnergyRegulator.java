package org.ThefryGuy.techFactory.recipes.energy;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.alloys.DamascusSteelIngot;
import org.ThefryGuy.techFactory.recipes.ingots.SilverIngot;
import org.ThefryGuy.techFactory.recipes.components.ElectricMotor;

import java.util.List;

public class EnergyRegulator implements RecipeItem {

    @Override
    public String getId() {
        return "energy_regulator";
    }

    @Override
    public String getDisplayName() {
        return "Energy Regulator";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHTNING_ROD;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Core component of the energy network",
                ChatColor.GRAY + "Regulates power flow from energy panels",
                ChatColor.GRAY + "Place in the world to create an energy hub",
                "",
                ChatColor.YELLOW + "Right-click to place",
                ChatColor.YELLOW + "Right-click placed block to configure"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Silver Ingot] [Damascus Steel] [Silver Ingot]
        // [Damascus Steel] [Electric Motor] [Damascus Steel]
        // [Silver Ingot] [Damascus Steel] [Silver Ingot]
        
        SilverIngot silverIngot = new SilverIngot();
        DamascusSteelIngot damascusSteel = new DamascusSteelIngot();
        ElectricMotor electricMotor = new ElectricMotor();
        
        return new ItemStack[] {
            silverIngot.getItemStack(),
            damascusSteel.getItemStack(),
            silverIngot.getItemStack(),
            damascusSteel.getItemStack(),
            electricMotor.getItemStack(),
            damascusSteel.getItemStack(),
            silverIngot.getItemStack(),
            damascusSteel.getItemStack(),
            silverIngot.getItemStack()
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

