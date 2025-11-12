package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Pressure Chamber - A 3x3x3 multiblock machine for extreme compression
 * Structure: Smooth Stone Slabs + Dispenser + Glass + Pistons + Cauldron
 */
public class PressureChamber implements RecipeItem {

    @Override
    public String getId() {
        return "pressure_chamber";
    }

    @Override
    public String getDisplayName() {
        return "Pressure Chamber";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_PURPLE;
    }

    @Override
    public Material getMaterial() {
        return Material.GLASS;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A 3x3x3 multiblock for extreme compression",
                ChatColor.GRAY + "Compresses items even further than the Compressor",
                "",
                ChatColor.GOLD + "How to Build:",
                ChatColor.GRAY + "Layer 1 (Top):",
                ChatColor.GRAY + "  [Smooth Stone Slab] [Dispenser ↓] [Smooth Stone Slab]",
                ChatColor.GRAY + "Layer 2 (Middle):",
                ChatColor.GRAY + "  [Piston ↑] [Glass] [Piston ↑]",
                ChatColor.GRAY + "Layer 3 (Bottom):",
                ChatColor.GRAY + "  [Piston ↑] [Cauldron] [Piston ↑]",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Dispenser",
                ChatColor.GRAY + "Place items in the Dispenser",
                ChatColor.GRAY + "Close inventory to compress",
                "",
                ChatColor.GOLD + "Recipes:",
                ChatColor.GRAY + "Coming soon!",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to any block",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the Dispenser if no chest"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Multiblock structure layout (showing middle slice):
        // Row 1 (top):    [Smooth Stone Slab] [Dispenser] [Smooth Stone Slab]
        // Row 2 (middle): [Piston] [Glass] [Piston]
        // Row 3 (bottom): [Piston] [Cauldron] [Piston]

        // Create dispenser with custom lore
        ItemStack dispenser = new ItemStack(Material.DISPENSER);
        ItemMeta dispenserMeta = dispenser.getItemMeta();
        if (dispenserMeta != null) {
            dispenserMeta.setLore(List.of(ChatColor.YELLOW + "Facing: DOWN"));
            dispenser.setItemMeta(dispenserMeta);
        }

        // Create pistons with custom lore
        ItemStack piston = new ItemStack(Material.PISTON);
        ItemMeta pistonMeta = piston.getItemMeta();
        if (pistonMeta != null) {
            pistonMeta.setLore(List.of(ChatColor.YELLOW + "Facing: UP"));
            piston.setItemMeta(pistonMeta);
        }

        return new ItemStack[] {
            new ItemStack(Material.SMOOTH_STONE_SLAB),  // Slot 0: Smooth Stone Slab
            dispenser,                                  // Slot 1: Dispenser (facing down)
            new ItemStack(Material.SMOOTH_STONE_SLAB),  // Slot 2: Smooth Stone Slab
            piston.clone(),                             // Slot 3: Piston (facing up)
            new ItemStack(Material.GLASS),              // Slot 4: Glass
            piston.clone(),                             // Slot 5: Piston (facing up)
            piston.clone(),                             // Slot 6: Piston (facing up)
            new ItemStack(Material.CAULDRON),           // Slot 7: Cauldron
            piston.clone()                              // Slot 8: Piston (facing up)
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock";
    }
}

