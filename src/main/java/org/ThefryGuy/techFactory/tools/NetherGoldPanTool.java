package org.ThefryGuy.techFactory.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Nether Gold Pan Tool - Handles the logic for using the Nether Gold Pan on soul sand/soil
 * 
 * Functionality:
 * - Right-click soul sand or soul soil while holding Nether Gold Pan
 * - Random drops (equal probability):
 *   - Nether Quartz (16.67%)
 *   - Nether Wart (16.67%)
 *   - Blaze Powder (16.67%)
 *   - Gold Nugget (16.67%)
 *   - Glowstone Dust (16.67%)
 *   - Ghast Tear (16.67%)
 * - Soul sand/soil block is removed after panning
 */
public class NetherGoldPanTool {

    private static final Random random = new Random();

    /**
     * Process the Nether Gold Pan on soul sand/soul soil.
     * 
     * @param player The player using the pan
     * @param clickedBlock The block being clicked
     * @param event The interaction event (to cancel if needed)
     */
    public static void process(Player player, Block clickedBlock, PlayerInteractEvent event) {
        // Only works on soul sand or soul soil
        if (clickedBlock.getType() != Material.SOUL_SAND && clickedBlock.getType() != Material.SOUL_SOIL) {
            return;
        }

        // Cancel the event to prevent other interactions
        event.setCancelled(true);

        // Randomly choose what the player gets (6 equal outcomes)
        int roll = random.nextInt(12);

        ItemStack result;
        String itemName;

        if (roll < 2) {
            // Nether Quartz
            result = new ItemStack(Material.QUARTZ);
            itemName = "Nether Quartz";
        } else if (roll < 4) {
            // Nether Wart
            result = new ItemStack(Material.NETHER_WART);
            itemName = "Nether Wart";
        } else if (roll < 6) {
            // Blaze Powder
            result = new ItemStack(Material.BLAZE_POWDER);
            itemName = "Blaze Powder";
        } else if (roll < 8) {
            // Gold Nugget
            result = new ItemStack(Material.GOLD_NUGGET);
            itemName = "Gold Nugget";
        } else if (roll < 10) {
            // Glowstone Dust
            result = new ItemStack(Material.GLOWSTONE_DUST);
            itemName = "Glowstone Dust";
        } else {
            // Ghast Tear
            result = new ItemStack(Material.GHAST_TEAR);
            itemName = "Ghast Tear";
        }

        player.sendMessage(ChatColor.DARK_RED + "You panned out " + ChatColor.WHITE + itemName + ChatColor.DARK_RED + "!");

        // Drop the item at the soul block location
        if (clickedBlock.getWorld() != null) {
            clickedBlock.getWorld().dropItem(clickedBlock.getLocation().add(0.5, 1, 0.5), result);
        } else {
            player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
            return;
        }

        // Remove the soul sand/soil block
        clickedBlock.setType(Material.AIR);

        // Add slight cooldown message
        player.sendMessage(ChatColor.GRAY + "Ready to pan again!");
    }
}

