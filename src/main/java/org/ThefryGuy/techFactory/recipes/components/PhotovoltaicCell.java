package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.resources.Silicon;
import org.ThefryGuy.techFactory.recipes.alloys.Ferrosilicon;

import java.util.List;

public class PhotovoltaicCell implements RecipeItem {

    @Override
    public String getId() {
        return "photovoltaic_cell";
    }

    @Override
    public String getDisplayName() {
        return "Photovoltaic Cell";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_BLUE;
    }

    @Override
    public Material getMaterial() {
        return Material.DAYLIGHT_DETECTOR;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Advanced solar energy conversion cell",
                ChatColor.GRAY + "Converts sunlight into electrical energy",
                ChatColor.GRAY + "Essential component for solar generators"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Glass] [Glass] [Glass]
        // [Silicon] [Silicon] [Silicon]
        // [Ferrosilicon] [Ferrosilicon] [Ferrosilicon]
        
        Silicon silicon = new Silicon();
        Ferrosilicon ferrosilicon = new Ferrosilicon();
        
        return new ItemStack[] {
            new ItemStack(Material.GLASS),
            new ItemStack(Material.GLASS),
            new ItemStack(Material.GLASS),
            silicon.getItemStack(),
            silicon.getItemStack(),
            silicon.getItemStack(),
            ferrosilicon.getItemStack(),
            ferrosilicon.getItemStack(),
            ferrosilicon.getItemStack()
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

