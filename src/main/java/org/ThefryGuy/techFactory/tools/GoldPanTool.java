package org.ThefryGuy.techFactory.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.dusts.SiftedOreDust;

import java.util.Random;

/**
 * Gold Pan Tool - Handles the logic for using the Gold Pan on gravel
 * 
 * Functionality:
 * - Right-click gravel while holding Gold Pan
 * - Random drops: Iron Nugget (40%), Sifted Ore Dust (35%), Clay Ball (15%), Flint (10%)
 * - Gravel block is removed after panning
 */
public class GoldPanTool {

    private static final Random random = new Random();

    /**
     * Process the Gold Pan on gravel.
     * 
     * @param player The player using the pan
     * @param clickedBlock The block being clicked
     * @param event The interaction event (to cancel if needed)
     */
    public static void process(Player player, Block clickedBlock, PlayerInteractEvent event) {
        // Only works on gravel
        if (clickedBlock.getType() != Material.GRAVEL) {
            return;
        }

        // Cancel the event to prevent other interactions
        event.setCancelled(true);

        // Randomly choose what the player gets
        int roll = random.nextInt(100);

        ItemStack result;

        if (roll < 40) {
            // 40% chance: Iron Nugget
            result = new ItemStack(Material.IRON_NUGGET);
            player.sendMessage(ChatColor.GOLD + "You panned out an " + ChatColor.WHITE + "Iron Nugget!");
        } else if (roll < 75) {
            // 35% chance: Sifted Ore Dust
            SiftedOreDust dust = new SiftedOreDust();
            result = dust.getItemStack();
            player.sendMessage(ChatColor.GOLD + "You panned out " + ChatColor.WHITE + "Sifted Ore Dust!");
        } else if (roll < 90) {
            // 15% chance: Clay Ball
            result = new ItemStack(Material.CLAY_BALL);
            player.sendMessage(ChatColor.GOLD + "You panned out a " + ChatColor.WHITE + "Clay Ball!");
        } else {
            // 10% chance: Flint
            result = new ItemStack(Material.FLINT);
            player.sendMessage(ChatColor.GOLD + "You panned out " + ChatColor.WHITE + "Flint!");
        }

        // Drop the item at the gravel block location
        if (clickedBlock.getWorld() != null) {
            clickedBlock.getWorld().dropItem(clickedBlock.getLocation().add(0.5, 1, 0.5), result);
        } else {
            player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
            return;
        }

        // Remove the gravel block
        clickedBlock.setType(Material.AIR);

        // Add slight cooldown message
        player.sendMessage(ChatColor.GRAY + "Ready to pan again!");
    }
}

