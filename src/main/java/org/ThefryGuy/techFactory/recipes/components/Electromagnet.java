package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.alloys.NickelIngot;
import org.ThefryGuy.techFactory.recipes.alloys.CobaltIngot;

import java.util.List;

public class Electromagnet implements RecipeItem {

    @Override
    public String getId() {
        return "electromagnet";
    }

    @Override
    public String getDisplayName() {
        return "Electromagnet";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.LODESTONE;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An electrically powered magnet",
                ChatColor.GRAY + "Requires a battery to function",
                ChatColor.GRAY + "Used in advanced machinery"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Nickel Ingot] [Magnet] [Cobalt Ingot]
        // [empty] [Battery] [empty]
        // [empty] [empty] [empty]
        
        NickelIngot nickelIngot = new NickelIngot();
        Magnet magnet = new Magnet();
        CobaltIngot cobaltIngot = new CobaltIngot();
        Battery battery = new Battery();
        
        return new ItemStack[] {
            nickelIngot.getItemStack(),
            magnet.getItemStack(),
            cobaltIngot.getItemStack(),
            new ItemStack(Material.AIR),
            battery.getItemStack(),
            new ItemStack(Material.AIR),
            new ItemStack(Material.AIR),
            new ItemStack(Material.AIR),
            new ItemStack(Material.AIR)
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

