package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.List;

/**
 * Tools menu - shows all tool categories
 */
public class ToolsMenu extends Menu {

    public ToolsMenu(Player player) {
        super(player, ChatColor.LIGHT_PURPLE + "Tools", 54);
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();

        // === TOOL CATEGORIES ===

        // Hand Tools
        addCategory(10, Material.DIAMOND_PICKAXE, ChatColor.LIGHT_PURPLE + "Hand Tools",
            "Gold Pan, and more!");

        // You can add more categories here as tools expand!
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
            case 10 -> openCategory("Hand Tools", ItemRegistry.getTools());
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

    private void addCategory(int slot, Material icon, String name, String description, boolean comingSoon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
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