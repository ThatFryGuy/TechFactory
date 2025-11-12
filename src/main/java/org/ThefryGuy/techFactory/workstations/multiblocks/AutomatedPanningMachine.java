package org.ThefryGuy.techFactory.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.*;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Automated Panning Machine - Handles the actual functionality
 * 
 * Structure:
 * [Trapdoor]   ← Top
 * [Cauldron]   ← Bottom (player interacts here)
 * 
 * Two-stage sifting process:
 * 1. Gravel → Sifted Ore Dust
 * 2. Sifted Ore Dust → Random Metal Dust (Iron, Copper, Gold, etc.)
 * 
 * The recipe data is in recipes/workstations/multiblocks/AutomatedPanning.java
 */
public class AutomatedPanningMachine {

    private static final Random random = new Random();

    // Store inventories for each Automated Panning Machine location
    private static final Map<Location, ItemStack[]> MACHINE_INVENTORIES = new HashMap<>();

    /**
     * Check if a valid Automated Panning Machine multiblock exists at this location.
     * 
     * Structure:
     * [Trapdoor]   ← Top
     * [Cauldron]   ← Bottom (player clicks here)
     * 
     * @param cauldron The cauldron block (bottom)
     * @return true if valid multiblock structure
     */
    public static boolean isValidStructure(Block cauldron) {
        // Check if bottom is a cauldron
        if (cauldron.getType() != Material.CAULDRON) {
            return false;
        }

        // Check if block above is a trapdoor
        Block top = cauldron.getRelative(0, 1, 0);
        return isTrapdoor(top.getType());
    }

    /**
     * Check if a material is any type of trapdoor
     */
    private static boolean isTrapdoor(Material material) {
        return material == Material.OAK_TRAPDOOR ||
               material == Material.SPRUCE_TRAPDOOR ||
               material == Material.BIRCH_TRAPDOOR ||
               material == Material.JUNGLE_TRAPDOOR ||
               material == Material.ACACIA_TRAPDOOR ||
               material == Material.DARK_OAK_TRAPDOOR ||
               material == Material.MANGROVE_TRAPDOOR ||
               material == Material.CHERRY_TRAPDOOR ||
               material == Material.BAMBOO_TRAPDOOR ||
               material == Material.CRIMSON_TRAPDOOR ||
               material == Material.WARPED_TRAPDOOR ||
               material == Material.IRON_TRAPDOOR;
    }

    /**
     * Get the cauldron from the structure.
     * 
     * @param cauldron The cauldron block
     * @return The cauldron block state, or null if not valid structure
     */
    public static BlockState getCauldron(Block cauldron) {
        if (!isValidStructure(cauldron)) {
            return null;
        }

        return cauldron.getState();
    }

    /**
     * Open the custom inventory for the Automated Panning Machine.
     * Called when player right-clicks the cauldron.
     *
     * @param cauldronBlock The cauldron block
     * @param player The player who clicked
     */
    public static void openInventory(Block cauldronBlock, Player player) {
        Location loc = cauldronBlock.getLocation();
        TechFactory plugin = (TechFactory) JavaPlugin.getProvidingPlugin(AutomatedPanningMachine.class);
        AutomatedPanningInventoryHolder holder = new AutomatedPanningInventoryHolder(loc);
        Inventory panningInventory = Bukkit.createInventory(holder, 27, ChatColor.GOLD + "Automated Panning Machine");

        // Load existing items from memory cache first
        ItemStack[] storedItems = MACHINE_INVENTORIES.get(loc);

        // If not in memory, try loading from database
        if (storedItems == null) {
            storedItems = plugin.getDatabaseManager().loadMultiblockInventory(loc);
        }

        // Restore items if found
        if (storedItems != null) {
            for (int i = 0; i < Math.min(storedItems.length, panningInventory.getSize()); i++) {
                if (storedItems[i] != null) {
                    panningInventory.setItem(i, storedItems[i].clone());
                }
            }
        }

        player.openInventory(panningInventory);
        // Message is now shown in MultiblockListener for consistency
    }

    /**
     * Save inventory contents when player closes the GUI.
     * This allows the items to persist so clicking trapdoor can process them.
     * Saves to both memory cache and database for persistence across restarts.
     */
    public static void saveInventory(Inventory inventory, Location cauldronLocation) {
        ItemStack[] contents = inventory.getContents();
        ItemStack[] copy = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                copy[i] = contents[i].clone();
            }
        }

        // Save to memory cache
        MACHINE_INVENTORIES.put(cauldronLocation, copy);

        // Save to database (async to avoid lag)
        TechFactory plugin = (TechFactory) JavaPlugin.getProvidingPlugin(AutomatedPanningMachine.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().saveMultiblockInventory(cauldronLocation, copy);
        });
    }

    /**
     * Process crafting when player clicks the trapdoor (Slimefun style).
     * Gets the stored inventory and processes it.
     */
    public static void processCraft(Player player, Block cauldronBlock) {
        Location loc = cauldronBlock.getLocation();

        TechFactory plugin = (TechFactory) JavaPlugin.getProvidingPlugin(AutomatedPanningMachine.class);

        // Get stored inventory from memory first
        ItemStack[] storedItems = MACHINE_INVENTORIES.get(loc);

        // If not in memory, try loading from database
        if (storedItems == null) {
            storedItems = plugin.getDatabaseManager().loadMultiblockInventory(loc);

            if (storedItems != null) {
                MACHINE_INVENTORIES.put(loc, storedItems);
            }
        }

        if (storedItems == null) {
            player.sendMessage(ChatColor.RED + "The Automated Panning Machine is empty!");
            return;
        }

        // Create a temporary inventory to process
        Inventory tempInv = Bukkit.createInventory(null, storedItems.length);
        for (int i = 0; i < storedItems.length; i++) {
            if (storedItems[i] != null) {
                tempInv.setItem(i, storedItems[i].clone());
            }
        }

        // Process the recipe
        processRecipe(tempInv, player, loc);

        // Save the modified inventory back
        ItemStack[] newContents = tempInv.getContents();
        ItemStack[] copy = new ItemStack[newContents.length];
        for (int i = 0; i < newContents.length; i++) {
            if (newContents[i] != null) {
                copy[i] = newContents[i].clone();
            }
        }
        MACHINE_INVENTORIES.put(loc, copy);
    }

    /**
     * Process sifting recipes when the player clicks the trapdoor (Slimefun style).
     * Shows "Invalid recipe" if no recipe matches.
     *
     * Stage 1: Gravel → Sifted Ore Dust
     * Stage 2: Sifted Ore Dust → Random Metal Dust
     *
     * @param inventory The inventory contents
     * @param player The player who clicked
     * @param cauldronLocation The location of the cauldron
     */
    public static void processRecipe(Inventory inventory, Player player, Location cauldronLocation) {
        Block cauldron = cauldronLocation.getBlock();

        // Check if inventory is empty
        boolean isEmpty = true;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getAmount() > 0) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            player.sendMessage(ChatColor.RED + "The Automated Panning Machine is empty!");
            return;
        }

        // Track if any valid recipe was processed
        boolean processedAny = false;

        // Process all items in the inventory
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getAmount() == 0) {
                continue;
            }

            // Check for Gravel → Sifted Ore Dust
            if (item.getType() == Material.GRAVEL) {
                int gravelCount = item.getAmount();

                SiftedOreDust siftedOreDust = new SiftedOreDust();
                ItemStack output = siftedOreDust.getItemStack();
                output.setAmount(gravelCount);

                // Try to output to adjacent chest first
                java.util.List<Block> multiblockBlocks = java.util.Collections.singletonList(cauldron);
                if (!ItemUtils.outputToChest(multiblockBlocks, output)) {
                    // No chest found, drop at cauldron location
                    if (cauldron.getWorld() != null) {
                        cauldron.getWorld().dropItemNaturally(cauldron.getLocation(), output);
                    } else {
                        player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
                        return;
                    }
                }

                item.setAmount(0);
                player.sendMessage(ChatColor.GREEN + "✓ Sifted " + gravelCount + " gravel into Sifted Ore Dust!");
                processedAny = true;
                continue;
            }

            // Check for Sifted Ore Dust → Random Metal Dust
            if (RecipeItem.isValidItem(item, "sifted_ore_dust")) {
                int siftedCount = item.getAmount();
                
                // Give random metal dusts
                Map<String, Integer> dustCounts = new HashMap<>();

                for (int i = 0; i < siftedCount; i++) {
                    RecipeItem randomDust = getRandomMetalDust();
                    String dustName = randomDust.getDisplayName();
                    dustCounts.put(dustName, dustCounts.getOrDefault(dustName, 0) + 1);
                }

                // Output the dusts to chest or drop
                java.util.List<Block> multiblockBlocks = java.util.Collections.singletonList(cauldron);
                for (Map.Entry<String, Integer> entry : dustCounts.entrySet()) {
                    RecipeItem dust = getMetalDustByName(entry.getKey());
                    ItemStack output = dust.getItemStack();
                    output.setAmount(entry.getValue());

                    // Try to output to adjacent chest first
                    if (!ItemUtils.outputToChest(multiblockBlocks, output)) {
                        // No chest found, drop at cauldron location
                        if (cauldron.getWorld() != null) {
                            cauldron.getWorld().dropItemNaturally(cauldron.getLocation(), output);
                        } else {
                            player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
                            return;
                        }
                    }
                }

                item.setAmount(0);
                player.sendMessage(ChatColor.GREEN + "✓ Sifted " + siftedCount + " Sifted Ore Dust!");
                player.sendMessage(ChatColor.GRAY + "Received various metal dusts!");
                processedAny = true;
            }
        }

        // If no valid recipe was processed, show error
        if (!processedAny) {
            player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Automated Panning Machine requires Gravel or Sifted Ore Dust.");
        }
    }

    /**
     * Legacy method for processing recipes with item in hand.
     * Kept for backward compatibility.
     * 
     * @param cauldron The cauldron block
     * @param player The player who interacted
     * @param itemInHand The item the player is holding (if any)
     */
    public static void processRecipeLegacy(Block cauldron, Player player, ItemStack itemInHand) {
        // Check for Gravel → Sifted Ore Dust
        if (itemInHand != null && itemInHand.getType() == Material.GRAVEL) {
            int gravelCount = itemInHand.getAmount();
            
            SiftedOreDust siftedOreDust = new SiftedOreDust();
            ItemStack output = siftedOreDust.getItemStack();
            output.setAmount(gravelCount);

            // Try to output to adjacent chest first
            java.util.List<Block> multiblockBlocks = java.util.Collections.singletonList(cauldron);
            if (!ItemUtils.outputToChest(multiblockBlocks, output)) {
                // No chest found, drop at cauldron location
                if (cauldron.getWorld() != null) {
                    cauldron.getWorld().dropItemNaturally(cauldron.getLocation(), output);
                } else {
                    player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
                    return;
                }
            }

            // Remove from player's hand
            itemInHand.setAmount(0);
            player.sendMessage(ChatColor.GREEN + "Sifted " + gravelCount + " gravel into Sifted Ore Dust!");
            return;
        }

        // Check for Sifted Ore Dust → Random Metal Dust
        if (itemInHand != null && RecipeItem.isValidItem(itemInHand, "sifted_ore_dust")) {
            int siftedCount = itemInHand.getAmount();
            
            // Give random metal dusts
            Map<String, Integer> dustCounts = new HashMap<>();

            for (int i = 0; i < siftedCount; i++) {
                RecipeItem randomDust = getRandomMetalDust();
                String dustName = randomDust.getDisplayName();
                dustCounts.put(dustName, dustCounts.getOrDefault(dustName, 0) + 1);
            }

            // Output the dusts to chest or drop
            java.util.List<Block> multiblockBlocks = java.util.Collections.singletonList(cauldron);
            for (Map.Entry<String, Integer> entry : dustCounts.entrySet()) {
                RecipeItem dust = getMetalDustByName(entry.getKey());
                ItemStack output = dust.getItemStack();
                output.setAmount(entry.getValue());

                // Try to output to adjacent chest first
                if (!ItemUtils.outputToChest(multiblockBlocks, output)) {
                    // No chest found, drop at cauldron location
                    if (cauldron.getWorld() != null) {
                        cauldron.getWorld().dropItemNaturally(cauldron.getLocation(), output);
                    } else {
                        player.sendMessage(ChatColor.RED + "Error: World is unloaded!");
                        return;
                    }
                }
            }

            // Remove from player's hand
            itemInHand.setAmount(0);
            player.sendMessage(ChatColor.GREEN + "Sifted " + siftedCount + " Sifted Ore Dust!");
            player.sendMessage(ChatColor.GRAY + "Received various metal dusts!");
            return;
        }

        // No valid recipe found
        player.sendMessage(ChatColor.YELLOW + "No valid sifting recipe found!");
    }



    /**
     * Get a random metal dust from the sifting pool.
     * 
     * @return A random metal dust RecipeItem
     */
    private static RecipeItem getRandomMetalDust() {
        RecipeItem[] dusts = {
            new IronDust(),      // Common
            new IronDust(),      // Common (weighted)
            new CopperDust(),    // Common
            new CopperDust(),    // Common (weighted)
            new TinDust(),       // Uncommon
            new ZincDust(),      // Uncommon
            new LeadDust(),      // Uncommon
            new AluminumDust(),  // Rare
            new SilverDust(),    // Rare
            new GoldDust(),      // Very Rare
            new MagnesiumDust()  // Very Rare
        };
        
        return dusts[random.nextInt(dusts.length)];
    }

    /**
     * Get a metal dust by its display name.
     * 
     * @param name The display name of the dust
     * @return The RecipeItem for that dust
     */
    private static RecipeItem getMetalDustByName(String name) {
        return switch (name) {
            case "Iron Dust" -> new IronDust();
            case "Copper Dust" -> new CopperDust();
            case "Gold Dust" -> new GoldDust();
            case "Tin Dust" -> new TinDust();
            case "Silver Dust" -> new SilverDust();
            case "Aluminum Dust" -> new AluminumDust();
            case "Lead Dust" -> new LeadDust();
            case "Zinc Dust" -> new ZincDust();
            case "Magnesium Dust" -> new MagnesiumDust();
            default -> new IronDust(); // Fallback
        };
    }

    /**
     * Break the multiblock and drop items.
     * Called when any part of the multiblock is broken.
     *
     * NOTE: This multiblock is built from vanilla blocks (Cauldron + Trapdoor),
     * so no special controller item needs to be dropped. Players get the vanilla blocks back.
     *
     * @param location The location where it was broken
     * @param player The player who broke it (can be null)
     */
    public static void breakMultiblock(Location location, Player player) {
        if (player != null) {
            player.sendMessage(ChatColor.RED + "Automated Panning Machine destroyed!");
        }
    }

    /**
     * Custom inventory holder for the Automated Panning Machine.
     * Stores the cauldron location so we can process recipes when the inventory is closed.
     */
    public static class AutomatedPanningInventoryHolder implements InventoryHolder {
        private final Location cauldronLocation;

        public AutomatedPanningInventoryHolder(Location cauldronLocation) {
            this.cauldronLocation = cauldronLocation;
        }

        @Override
        public Inventory getInventory() {
            return null; // Inventory is managed by the system
        }

        public Location getCauldronLocation() {
            return cauldronLocation;
        }
    }
}