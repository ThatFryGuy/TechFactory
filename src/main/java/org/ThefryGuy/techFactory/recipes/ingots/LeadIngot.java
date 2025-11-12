package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.LeadDust;

import java.util.List;

public class LeadIngot implements RecipeItem {

    @Override
    public String getId() {
        return "lead_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Lead Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;  // Using iron ingot as placeholder
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted lead ingot",
                ChatColor.GRAY + "Created from Lead Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        LeadDust leadDust = new LeadDust();
        return new ItemStack[] {
            leadDust.getItemStack(),
            null, null,
            null, null, null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

