package org.ThefryGuy.techFactory.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

/**
 * /techfactory give <player> <item> [amount]
 * 
 * Give TechFactory items to players
 */
public class GiveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("techfactory.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Usage: /techfactory give <player> <item> [amount]
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /techfactory give <player> <item> [amount]");
            sender.sendMessage(ChatColor.GRAY + "Example: /techfactory give Steve iron_dust 64");
            return true;
        }

        // Get target player
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
            return true;
        }

        // Get item ID (convert to lowercase with underscores)
        String itemId = args[2].toLowerCase().replace(" ", "_");
        
        // Get amount (default 1)
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
                return true;
            }
        }

        // Get the item from registry
        RecipeItem recipeItem = ItemRegistry.getItemById(itemId);
        
        if (recipeItem == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item: " + itemId);
            sender.sendMessage(ChatColor.GRAY + "Use tab completion to see available items");
            return true;
        }

        // Create the item stack
        ItemStack itemStack = recipeItem.getItemStack();
        if (itemStack == null) {
            sender.sendMessage(ChatColor.RED + "Error: Could not create item!");
            return true;
        }

        itemStack.setAmount(amount);

        // Give to player
        target.getInventory().addItem(itemStack);

        // Success messages
        String displayName = recipeItem.getDisplayName();
        sender.sendMessage(ChatColor.GREEN + "✓ Gave " + ChatColor.WHITE + amount + "x " + 
                          displayName + ChatColor.GREEN + " to " + ChatColor.WHITE + target.getName());
        
        if (!sender.equals(target)) {
            target.sendMessage(ChatColor.GREEN + "You received " + ChatColor.WHITE + amount + "x " + 
                              displayName + ChatColor.GREEN + " from " + ChatColor.WHITE + sender.getName());
        }

        return true;
    }
}

