package org.ThefryGuy.techFactory.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.*;

/**
 * Manages all menus - handles clicks, navigation, and pagination.
 * This is the only event listener needed for the entire GUI system.
 */
public class MenuManager implements Listener {
    
    // Track which menu each player has open
    private static final Map<UUID, Menu> currentMenus = new HashMap<>();

    // Track the last menu each player had open (for reopening after close)
    private static final Map<UUID, Menu> lastMenus = new HashMap<>();

    // Track navigation history for back button
    private static final Map<UUID, Deque<Menu>> menuHistory = new HashMap<>();

    // Track pagination state (player -> menu type -> page number)
    private static final Map<UUID, Map<String, Integer>> pageState = new HashMap<>();

    /**
     * Set the current menu for a player
     */
    public static void setCurrentMenu(Player player, Menu menu) {
        currentMenus.put(player.getUniqueId(), menu);
        lastMenus.put(player.getUniqueId(), menu); // Remember this menu
    }

    /**
     * Get the current menu for a player
     */
    public static Menu getCurrentMenu(Player player) {
        return currentMenus.get(player.getUniqueId());
    }

    /**
     * Get the last menu a player had open (for reopening after close)
     */
    public static Menu getLastMenu(Player player) {
        return lastMenus.get(player.getUniqueId());
    }
    
    /**
     * Push a menu onto the history stack (for back button)
     */
    public static void pushHistory(Player player, Menu menu) {
        menuHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>()).push(menu);
    }
    
    /**
     * Go back to the previous menu
     */
    public static void goBack(Player player) {
        Deque<Menu> history = menuHistory.get(player.getUniqueId());
        if (history != null && !history.isEmpty()) {
            Menu previous = history.pop();
            previous.open();
        }
    }
    
    /**
     * Clear history and go to main menu
     */
    public static void goHome(Player player) {
        menuHistory.remove(player.getUniqueId());
        new GuideMenu(player).open();
    }
    
    /**
     * Get the current page for a menu type
     */
    public static int getCurrentPage(Player player, String menuType) {
        return pageState
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .getOrDefault(menuType, 0);
    }
    
    /**
     * Set the current page for a menu type
     */
    public static void setCurrentPage(Player player, String menuType, int page) {
        pageState
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(menuType, page);
    }
    
    /**
     * Clean up data when player leaves
     */
    public static void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        currentMenus.remove(uuid);
        lastMenus.remove(uuid);
        menuHistory.remove(uuid);
        pageState.remove(uuid);
    }
    
    /**
     * Handle inventory clicks
     *
     * SECURITY HARDENED: Blocks ALL interaction types to prevent exploits
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Menu menu = getCurrentMenu(player);
        if (menu == null) return;

        // Check if they clicked in our menu
        if (!event.getInventory().equals(menu.getInventory())) return;

        // Cancel ALL click types (shift-click, number keys, double-click, etc.)
        event.setCancelled(true);

        // Handle the click only if it's a normal left/right click
        if (event.getClick() == org.bukkit.event.inventory.ClickType.LEFT ||
            event.getClick() == org.bukkit.event.inventory.ClickType.RIGHT) {
            menu.handleClick(event.getSlot(), event.getCurrentItem());
        }
    }

    /**
     * Handle inventory drag events
     *
     * SECURITY: Blocks ALL drag operations in recipe GUIs
     */
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Menu menu = getCurrentMenu(player);
        if (menu == null) return;

        // Check if they're dragging in our menu
        if (!event.getInventory().equals(menu.getInventory())) return;

        // Block ALL dragging in recipe GUIs
        event.setCancelled(true);
    }
    
    /**
     * Clean up when inventory closes
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        Menu menu = getCurrentMenu(player);
        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            // Don't remove from currentMenus - they might be opening another menu
            // Only clean up on logout
        }
    }
}

