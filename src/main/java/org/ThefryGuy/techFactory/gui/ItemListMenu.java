package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Generic item list menu with pagination.
 * Works for ANY category - Dusts, Alloys, Machines, Tools, etc.
 */
public class ItemListMenu extends Menu {
    
    private final String categoryName;
    private final List<RecipeItem> items;
    private static final int ITEMS_PER_PAGE = 36; // 4 rows of 9
    
    public ItemListMenu(Player player, String categoryName, List<RecipeItem> items) {
        super(player, ChatColor.LIGHT_PURPLE + categoryName, 54);
        this.categoryName = categoryName;
        this.items = items;
    }
    
    @Override
    protected void build() {
        clear();
        
        int currentPage = MenuManager.getCurrentPage(player, categoryName);
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        
        // Navigation buttons (top row)
        addBackButton();
        addHomeButton();
        
        // Items (rows 1-4, slots 9-44)
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        int slot = 9; // Start at row 1
        for (int i = startIndex; i < endIndex; i++) {
            RecipeItem item = items.get(i);
            ItemStack itemStack = item.getItemStack();
            if (itemStack != null) {
                setItem(slot++, itemStack);
            }
        }
        
        // Pagination controls (bottom row)
        if (totalPages > 1) {
            addPaginationButtons(currentPage, totalPages);
        }
    }
    
    @Override
    public void handleClick(int slot, ItemStack clicked) {
        // Navigation
        if (slot == 0) { // Back
            MenuManager.goBack(player);
            return;
        }
        if (slot == 8) { // Home
            MenuManager.goHome(player);
            return;
        }
        
        // Pagination
        if (slot == 45) { // Previous page
            int currentPage = MenuManager.getCurrentPage(player, categoryName);
            if (currentPage > 0) {
                MenuManager.setCurrentPage(player, categoryName, currentPage - 1);
                build();
                player.openInventory(inventory);
            }
            return;
        }
        if (slot == 53) { // Next page
            int currentPage = MenuManager.getCurrentPage(player, categoryName);
            int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                MenuManager.setCurrentPage(player, categoryName, currentPage + 1);
                build();
                player.openInventory(inventory);
            }
            return;
        }
        
        // Item clicks (slots 9-44)
        if (slot >= 9 && slot <= 44 && clicked != null) {
            // Find which item was clicked
            int currentPage = MenuManager.getCurrentPage(player, categoryName);
            int startIndex = currentPage * ITEMS_PER_PAGE;
            int itemIndex = startIndex + (slot - 9);

            if (itemIndex < items.size()) {
                RecipeItem item = items.get(itemIndex);

                // Special handling for info items (no recipe view)
                if (item.getId().equals("output_chest_info")) {
                    // Just show the lore - no recipe to view
                    player.sendMessage(ChatColor.GOLD + "ℹ Output Chest Info:");
                    for (String line : item.getLore()) {
                        player.sendMessage(line);
                    }
                    return;
                }

                MenuManager.pushHistory(player, this);
                new RecipeMenu(player, item).open();
            }
        }
    }
    


    private void addPaginationButtons(int currentPage, int totalPages) {
        // Previous page
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "← Previous Page");
                prev.setItemMeta(meta);
            }
            setItem(45, prev);
        }

        // Page info
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta meta = pageInfo.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Page " + (currentPage + 1) + " / " + totalPages);
            pageInfo.setItemMeta(meta);
        }
        setItem(49, pageInfo);

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack next = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta2 = next.getItemMeta();
            if (meta2 != null) {
                meta2.setDisplayName(ChatColor.GREEN + "Next Page →");
                next.setItemMeta(meta2);
            }
            setItem(53, next);
        }
    }
}

