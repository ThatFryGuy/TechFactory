package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

/**
 * Energy menu - shows all energy-related categories
 */
public class EnergyMenu extends Menu {

    public EnergyMenu(Player player) {
        super(player, ChatColor.AQUA + "Energy", 54);
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();

        // === ENERGY CATEGORIES ===

        // Energy Components
        addCategory(10, Material.LIGHTNING_ROD, ChatColor.AQUA + "Energy Components",
            "Energy Regulators, Solar Panels, and more!");

        // You can add more energy categories here as the system expands!
        // Examples:
        // - Energy Storage
        // - Energy Generators
        // - Energy Cables
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
            case 10 -> openCategory("Energy Components", ItemRegistry.getEnergyItems());
            // Add more cases as you add categories
        }
    }

    private void openCategory(String name, java.util.List<org.ThefryGuy.techFactory.recipes.RecipeItem> items) {
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
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);

            if (comingSoon) {
                meta.setLore(java.util.List.of(
                    ChatColor.GRAY + description,
                    "",
                    ChatColor.DARK_GRAY + "(Coming soon)"
                ));
            } else {
                meta.setLore(java.util.List.of(
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

