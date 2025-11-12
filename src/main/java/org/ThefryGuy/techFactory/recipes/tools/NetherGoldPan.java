package org.ThefryGuy.techFactory.recipes.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.tools.GoldPan;

import java.util.List;

/**
 * Nether Gold Pan - A tool for manually panning soul sand/soul soil to get nether resources
 * 
 * Right-click while holding to pan soul sand/soul soil and get:
 * - Nether Quartz
 * - Nether Wart
 * - Blaze Powder
 * - Gold Nugget
 * - Glowstone Dust
 * - Ghast Tear
 */
public class NetherGoldPan implements RecipeItem {

    @Override
    public String getId() {
        return "nether_gold_pan";
    }

    @Override
    public String getDisplayName() {
        return "Nether Gold Pan";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_RED;
    }

    @Override
    public Material getMaterial() {
        return Material.BOWL;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A tool for panning soul sand/soil",
                ChatColor.GRAY + "Right-click soul sand or soul soil",
                ChatColor.GRAY + "to pan for nether resources",
                "",
                ChatColor.GOLD + "Possible Drops:",
                ChatColor.GRAY + "• Nether Quartz",
                ChatColor.GRAY + "• Nether Wart",
                ChatColor.GRAY + "• Blaze Powder",
                ChatColor.GRAY + "• Gold Nugget",
                ChatColor.GRAY + "• Glowstone Dust",
                ChatColor.GRAY + "• Ghast Tear",
                "",
                ChatColor.DARK_GRAY + "Crafted at: Enhanced Crafting Table"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Nether Brick] [Gold Pan] [Nether Brick]
        // [Nether Brick] [Nether Brick] [Nether Brick]
        // [null]  [null]  [null]
        GoldPan goldPan = new GoldPan();
        return new ItemStack[] {
            new ItemStack(Material.NETHER_BRICK), goldPan.getItemStack(), new ItemStack(Material.NETHER_BRICK),
            new ItemStack(Material.NETHER_BRICK), new ItemStack(Material.NETHER_BRICK), new ItemStack(Material.NETHER_BRICK),
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

