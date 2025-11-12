package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.registry.WorkstationRegistry;

import java.util.List;

/**
 * Workstations menu - shows all workstation categories
 */
public class WorkstationsMenu extends Menu {

    public WorkstationsMenu(Player player) {
        super(player, ChatColor.AQUA + "Workstations", 54);
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();

        // === WORKSTATION CATEGORIES ===

        // Multiblocks
        addCategory(10, Material.BRICKS, ChatColor.RED + "Multiblocks",
            "Large structures built in the world");

        // Advanced Multiblocks
        addCategory(11, Material.CRAFTING_TABLE, ChatColor.GOLD + "Advanced Multiblocks",
            "Enhanced multiblock structures");

        // You can add more categories here!
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
            case 10 -> openCategory("Multiblocks", WorkstationRegistry.getMultiblocks());
            case 11 -> {
                // Open Advanced Multiblocks list
                if (WorkstationRegistry.getAdvancedMultiblocks().isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Advanced Multiblocks coming soon!");
                } else {
                    openCategory("Advanced Multiblocks", WorkstationRegistry.getAdvancedMultiblocks());
                }
            }
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

