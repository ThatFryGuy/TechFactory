package org.ThefryGuy.techFactory.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.data.SmeltingQueue;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.registry.ItemRegistry;
import org.ThefryGuy.techFactory.workstations.multiblocks.SmelterMachine;

import java.util.List;

/**
 * Handles /techfactory queue commands
 * 
 * PHASE 3: Recipe Queueing System
 * 
 * Commands:
 * - /techfactory queue add <recipe_id> - Add a recipe to the queue
 * - /techfactory queue remove <position> - Remove a recipe from the queue
 * - /techfactory queue clear - Clear all queued recipes
 * - /techfactory queue view - View all queued recipes
 */
public class QueueCommand implements CommandExecutor {
    
    private final TechFactory plugin;
    
    public QueueCommand(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // Must be a player (need to look at smelter)
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check for subcommand
        if (args.length < 2) {
            sendUsage(player);
            return true;
        }
        
        String subcommand = args[1].toLowerCase();
        
        switch (subcommand) {
            case "add":
                return handleAdd(player, args);
            case "remove":
                return handleRemove(player, args);
            case "clear":
                return handleClear(player);
            case "view":
            case "list":
                return handleView(player);
            default:
                sendUsage(player);
                return true;
        }
    }
    
    /**
     * Handle /techfactory queue add <recipe_id>
     */
    private boolean handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /techfactory queue add <recipe_id>");
            player.sendMessage(ChatColor.GRAY + "Example: /techfactory queue add bronze_ingot");
            return true;
        }
        
        String recipeId = args[2].toLowerCase();
        
        // Validate recipe exists
        RecipeItem recipe = ItemRegistry.getItemById(recipeId);
        if (recipe == null) {
            player.sendMessage(ChatColor.RED + "Unknown recipe: " + recipeId);
            return true;
        }
        
        // Get the smelter the player is looking at
        Location smelterLoc = getSmelterLookingAt(player);
        if (smelterLoc == null) {
            player.sendMessage(ChatColor.RED + "You must be looking at a Smelter!");
            player.sendMessage(ChatColor.GRAY + "Hint: Look at the Blast Furnace block");
            return true;
        }
        
        // Get the queue
        SmeltingQueue queue = plugin.getSmeltingManager().getQueue(smelterLoc);
        
        // Add recipe to queue
        queue.addRecipe(recipeId);
        
        // Save to database
        plugin.getSmeltingManager().saveQueue(queue);
        
        player.sendMessage(ChatColor.GREEN + "✓ Added to queue: " + recipe.getColor() + recipe.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Queue position: #" + queue.size());
        
        return true;
    }
    
    /**
     * Handle /techfactory queue remove <position>
     */
    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /techfactory queue remove <position>");
            player.sendMessage(ChatColor.GRAY + "Example: /techfactory queue remove 1");
            return true;
        }
        
        int position;
        try {
            position = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid position: " + args[2]);
            return true;
        }
        
        // Get the smelter the player is looking at
        Location smelterLoc = getSmelterLookingAt(player);
        if (smelterLoc == null) {
            player.sendMessage(ChatColor.RED + "You must be looking at a Smelter!");
            return true;
        }
        
        // Get the queue
        SmeltingQueue queue = plugin.getSmeltingManager().getQueue(smelterLoc);
        
        // Remove recipe (convert 1-based to 0-based index)
        boolean removed = queue.removeRecipe(position - 1);
        
        if (!removed) {
            player.sendMessage(ChatColor.RED + "Invalid position: " + position);
            player.sendMessage(ChatColor.GRAY + "Queue has " + queue.size() + " recipes");
            return true;
        }
        
        // Save to database
        plugin.getSmeltingManager().saveQueue(queue);
        
        player.sendMessage(ChatColor.GREEN + "✓ Removed recipe at position " + position);
        
        return true;
    }
    
    /**
     * Handle /techfactory queue clear
     */
    private boolean handleClear(Player player) {
        // Get the smelter the player is looking at
        Location smelterLoc = getSmelterLookingAt(player);
        if (smelterLoc == null) {
            player.sendMessage(ChatColor.RED + "You must be looking at a Smelter!");
            return true;
        }
        
        // Get the queue
        SmeltingQueue queue = plugin.getSmeltingManager().getQueue(smelterLoc);
        
        if (queue.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Queue is already empty!");
            return true;
        }
        
        int count = queue.size();
        queue.clear();
        
        // Save to database
        plugin.getSmeltingManager().saveQueue(queue);
        
        player.sendMessage(ChatColor.GREEN + "✓ Cleared " + count + " recipes from queue");
        
        return true;
    }
    
    /**
     * Handle /techfactory queue view
     */
    private boolean handleView(Player player) {
        // Get the smelter the player is looking at
        Location smelterLoc = getSmelterLookingAt(player);
        if (smelterLoc == null) {
            player.sendMessage(ChatColor.RED + "You must be looking at a Smelter!");
            return true;
        }
        
        // Get the queue
        SmeltingQueue queue = plugin.getSmeltingManager().getQueue(smelterLoc);
        
        if (queue.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Queue is empty!");
            player.sendMessage(ChatColor.GRAY + "Use /techfactory queue add <recipe_id> to add recipes");
            return true;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Smelter Queue ===");
        
        List<String> recipeIds = queue.getQueuedRecipeIds();
        for (int i = 0; i < recipeIds.size(); i++) {
            String recipeId = recipeIds.get(i);
            RecipeItem recipe = ItemRegistry.getItemById(recipeId);
            
            if (recipe != null) {
                player.sendMessage(ChatColor.GRAY + "" + (i + 1) + ". " + recipe.getColor() + recipe.getDisplayName());
            } else {
                player.sendMessage(ChatColor.GRAY + "" + (i + 1) + ". " + ChatColor.RED + recipeId + " (unknown)");
            }
        }
        
        player.sendMessage(ChatColor.GRAY + "Total: " + queue.size() + " recipes");
        
        return true;
    }
    
    /**
     * Get the smelter location the player is looking at
     * Returns null if not looking at a smelter
     */
    private Location getSmelterLookingAt(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            return null;
        }
        
        // Check if it's a valid smelter structure
        if (SmelterMachine.isValidStructure(target)) {
            return target.getLocation();
        }
        
        return null;
    }
    
    /**
     * Send usage message
     */
    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Queue Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/techfactory queue add <recipe_id>" + ChatColor.GRAY + " - Add recipe to queue");
        player.sendMessage(ChatColor.YELLOW + "/techfactory queue remove <position>" + ChatColor.GRAY + " - Remove recipe from queue");
        player.sendMessage(ChatColor.YELLOW + "/techfactory queue clear" + ChatColor.GRAY + " - Clear all queued recipes");
        player.sendMessage(ChatColor.YELLOW + "/techfactory queue view" + ChatColor.GRAY + " - View queued recipes");
    }
}

