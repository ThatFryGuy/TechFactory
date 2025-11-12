package org.ThefryGuy.techFactory.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.tools.GoldPanTool;
import org.ThefryGuy.techFactory.tools.NetherGoldPanTool;

/**
 * Listener for tool interactions.
 *
 * This is a DISPATCHER that delegates to individual tool handlers.
 * Each tool has its own class in the tools/ package for scalability.
 *
 * Registered Tools:
 * - Gold Pan → GoldPanTool
 * - Nether Gold Pan → NetherGoldPanTool
 */
public class ToolListener implements Listener {

    /**
     * Handle player right-clicking with tools.
     * Dispatches to the appropriate tool handler based on item ID.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only care about right-clicks on blocks
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null || itemInHand == null) {
            return;
        }

        // Dispatch to appropriate tool handler
        if (RecipeItem.isValidItem(itemInHand, "gold_pan")) {
            GoldPanTool.process(player, clickedBlock, event);
        } else if (RecipeItem.isValidItem(itemInHand, "nether_gold_pan")) {
            NetherGoldPanTool.process(player, clickedBlock, event);
        }
        // Future tools: just add one line here to dispatch to the new tool handler
    }
}