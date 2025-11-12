package org.ThefryGuy.techFactory.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.commands.GuidebookCommand;
import org.ThefryGuy.techFactory.gui.GuideMenu;
import org.ThefryGuy.techFactory.gui.Menu;
import org.ThefryGuy.techFactory.gui.MenuManager;

/**
 * Listener for TechFactory Guidebook interactions.
 * Handles right-clicking the guidebook to open the guide GUI.
 * 
 * Similar to Slimefun's guidebook system.
 */
public class GuidebookListener implements Listener {

    /**
     * Handle player right-clicking with the guidebook.
     * Opens the guide GUI when the player right-clicks while holding the guidebook.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only care about right-clicks (air or block)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if player is holding the TechFactory Guidebook
        if (GuidebookCommand.isGuidebook(itemInHand)) {
            // Cancel the event to prevent the book from opening normally
            event.setCancelled(true);
            
            // Open the guide GUI
            // Check if player has a last menu - reopen it if they do
            Menu lastMenu = MenuManager.getLastMenu(player);
            if (lastMenu != null) {
                lastMenu.open();
            } else {
                // First time opening - show guide menu
                GuideMenu.open(player);
            }
        }
    }
}

