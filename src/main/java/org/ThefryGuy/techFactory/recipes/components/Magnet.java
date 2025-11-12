package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.alloys.NickelIngot;
import org.ThefryGuy.techFactory.recipes.alloys.CobaltIngot;
import org.ThefryGuy.techFactory.recipes.dusts.AluminumDust;
import org.ThefryGuy.techFactory.recipes.dusts.IronDust;

import java.util.List;

public class Magnet implements RecipeItem {

    @Override
    public String getId() {
        return "magnet";
    }

    @Override
    public String getDisplayName() {
        return "Magnet";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A powerful magnetic component",
                ChatColor.GRAY + "Made from nickel, cobalt, and iron",
                ChatColor.GRAY + "Used in electromagnetic devices"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Nickel Ingot + Aluminum Dust + Iron Dust + Cobalt Ingot
        NickelIngot nickelIngot = new NickelIngot();
        AluminumDust aluminumDust = new AluminumDust();
        IronDust ironDust = new IronDust();
        CobaltIngot cobaltIngot = new CobaltIngot();
        
        return new ItemStack[] {
            nickelIngot.getItemStack(),
            aluminumDust.getItemStack(),
            ironDust.getItemStack(),
            cobaltIngot.getItemStack(),
            null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

