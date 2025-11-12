package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.List;

/**
 * Machines menu - shows all machine categories
 */
public class MachinesMenu extends Menu {

    public MachinesMenu(Player player) {
        super(player, ChatColor.RED + "Machines", 54);
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();

        // === MACHINE CATEGORIES ===

        // Electric Machines
        addCategory(10, Material.FURNACE, ChatColor.AQUA + "Electric Machines",
            "Electric Furnace and more!");

        // You can add more categories here as machines expand!
        // Examples:
        // - Manual Machines
        // - Advanced Machines
        // - Processing Machines
    }
    
    @Override
    public void handleClick(int slot, ItemStack clicked) {
        // Navigation
        if (slot == 0) {
            MenuManager.goBack(player);
            return;
        }
        if (slot == 8) {
            MenuManager.goHome(player);
            return;
        }

        // Categories
        switch (slot) {
            case 10 -> openCategory("Electric Machines", ItemRegistry.getMachines());
            // Add more cases as you add categories
        }
    }

    private void openCategory(String name, List<org.ThefryGuy.techFactory.recipes.RecipeItem> items) {
        MenuManager.pushHistory(player, this);
        new ItemListMenu(player, name, items).open();
    }

    /**
     * Helper method to add a category button
     */
    private void addCategory(int slot, Material icon, String name, String description) {
        addCategory(slot, icon, name, description, false);
    }

    /**
     * Helper method to add a category button with optional "coming soon" flag
     */
    private void addCategory(int slot, Material icon, String name, String description, boolean comingSoon) {
        ItemStack item = new ItemStack(icon);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (comingSoon) {
                meta.setLore(List.of(
                    ChatColor.GRAY + description,
                    "",
                    ChatColor.DARK_GRAY + "(Coming soon)"
                ));
            } else {
                meta.setLore(List.of(
                    ChatColor.GRAY + description,
                    "",
                    ChatColor.YELLOW + "Click to open"
                ));
            }
            item.setItemMeta(meta);
        }
        setItem(slot, item);
    }
}

