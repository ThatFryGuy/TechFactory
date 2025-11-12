package org.ThefryGuy.techFactory.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Base class for all menus.
 * Every menu extends this and implements build() to add items.
 */
public abstract class Menu {
    
    protected final Player player;
    protected final Inventory inventory;
    
    /**
     * Create a new menu
     * @param player The player viewing the menu
     * @param title The menu title
     * @param size The menu size (must be multiple of 9, max 54)
     */
    public Menu(Player player, String title, int size) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, size, title);
    }
    
    /**
     * Build the menu - add all items here
     * This is called before the menu is opened
     */
    protected abstract void build();
    
    /**
     * Handle a click in this menu
     * @param slot The slot that was clicked
     * @param clicked The item that was clicked (can be null)
     */
    public void handleClick(int slot, ItemStack clicked) {
        // Override in subclasses to handle clicks
    }
    
    /**
     * Open this menu for the player
     */
    public void open() {
        build();
        player.openInventory(inventory);
        MenuManager.setCurrentMenu(player, this);
    }
    
    /**
     * Add an item to the menu
     */
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }
    
    /**
     * Clear the menu
     */
    protected void clear() {
        inventory.clear();
    }
    
    /**
     * Get the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Add a "Back" button to slot 0.
     * This is a common navigation pattern used across all menus.
     * Clicking this button calls MenuManager.goBack(player).
     */
    protected void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "← Back");
            back.setItemMeta(meta);
        }
        setItem(0, back);
    }

    /**
     * Add a "Home" button to slot 8.
     * This is a common navigation pattern used across all menus.
     * Clicking this button calls MenuManager.goHome(player).
     */
    protected void addHomeButton() {
        ItemStack home = new ItemStack(Material.DARK_OAK_DOOR);
        ItemMeta meta = home.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "⌂ Home");
            home.setItemMeta(meta);
        }
        setItem(8, home);
    }
}

