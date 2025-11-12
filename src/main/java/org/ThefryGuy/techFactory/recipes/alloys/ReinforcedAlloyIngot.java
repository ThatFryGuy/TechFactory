package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class ReinforcedAlloyIngot implements RecipeItem {

    @Override
    public String getId() {
        return "reinforced_alloy_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Reinforced Alloy Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.LIGHT_PURPLE;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "The ultimate alloy",
                ChatColor.GRAY + "Combines the best properties of all metals",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Damascus Steel + Hardened Metal + Corinthian Bronze + Solder + Billon + 24 Karat Gold (6 items)
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.alloys.DamascusSteelIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.HardenedMetalIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.CorinthianBronzeIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.SolderIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.BillonIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.TwentyFourKaratGoldIngot().getItemStack(),
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

