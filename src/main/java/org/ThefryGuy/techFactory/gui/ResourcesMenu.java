package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.List;

/**
 * Resources menu - shows all resource categories
 */
public class ResourcesMenu extends Menu {

    public ResourcesMenu(Player player) {
        super(player, ChatColor.GOLD + "Resources", 54); // Bigger menu for more categories
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();

        // === CURRENT CATEGORIES ===

        // Dusts
        addCategory(10, Material.BLAZE_POWDER, ChatColor.LIGHT_PURPLE + "Dusts",
            "Iron Dust, Gold Dust, etc.");

        // Ingots
        addCategory(11, Material.IRON_INGOT, ChatColor.WHITE + "Ingots",
            "Iron, Gold, Copper, Tin, etc.");

        // Resources
        addCategory(12, Material.COAL, ChatColor.DARK_GRAY + "Resources",
            "Carbon, etc.");

        // Alloys
        addCategory(13, Material.GOLD_INGOT, ChatColor.GOLD + "Alloys",
            "Bronze, Steel, etc.");

        // Technical Components
        addCategory(14, Material.REPEATER, ChatColor.AQUA + "Technical Components",
            "Circuit Boards and more!");

        // === FUTURE CATEGORIES (examples) ===

        // You can add 40+ more categories here!
        // The menu has 54 slots, minus navigation = 52 slots for categories
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
            case 10 -> openCategory("Dusts", ItemRegistry.getDusts());
            case 11 -> openCategory("Ingots", ItemRegistry.getIngots());
            case 12 -> openCategory("Resources", ItemRegistry.getResources());
            case 13 -> openCategory("Alloys", ItemRegistry.getAlloys());
            case 14 -> openCategory("Technical Components", ItemRegistry.getComponents());
            // Add more cases as you add categories
        }
    }

    private void openCategory(String name, List<RecipeItem> items) {
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

