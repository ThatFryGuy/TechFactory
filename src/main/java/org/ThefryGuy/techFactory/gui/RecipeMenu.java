package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.*;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe display menu - shows how to craft an item
 */
public class RecipeMenu extends Menu {

    private final RecipeItem recipeItem;

    public RecipeMenu(Player player, RecipeItem recipeItem) {
        super(player, ChatColor.AQUA + recipeItem.getDisplayName() + " Recipe", 54);
        this.recipeItem = recipeItem;
    }
    
    @Override
    protected void build() {
        clear();

        // Navigation buttons
        addBackButton();
        addHomeButton();

        // Check if this is an Automated Panning Machine - show special recipe view
        if (recipeItem.getId().equals("automated_panning_machine")) {
            buildAutomatedPanningRecipes();
            return;
        }

        // Normal recipe display
        buildNormalRecipe();
    }

    /**
     * Build the normal recipe display (3x3 grid → output)
     */
    private void buildNormalRecipe() {
        // Get the recipe (ItemStack array)
        ItemStack[] recipe = recipeItem.getRecipe();

        // Display recipe inputs in a 3x3 grid (slots 19-21, 28-30, 37-39)
        int[] recipeSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
        for (int i = 0; i < Math.min(recipe.length, 9); i++) {
            if (recipe[i] != null) {
                // Add clickable lore to recipe ingredients
                ItemStack clickableIngredient = addClickableLore(recipe[i]);
                setItem(recipeSlots[i], clickableIngredient);
            }
        }

        // Machine info (top center)
        Material machineIcon = getMachineIcon(recipeItem.getMachineType());
        ItemStack machine = new ItemStack(machineIcon);
        ItemMeta machineMeta = machine.getItemMeta();
        if (machineMeta != null) {
            machineMeta.setDisplayName(ChatColor.GOLD + recipeItem.getMachineType());
            machineMeta.setLore(List.of(
                ChatColor.GRAY + "Machine required for crafting",
                "",
                ChatColor.YELLOW + "Click to view machine!"
            ));
            machine.setItemMeta(machineMeta);
        }
        setItem(4, machine);

        // Arrow indicators (pointing to output)
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName(ChatColor.GREEN + "→");
            arrow.setItemMeta(arrowMeta);
        }
        setItem(23, arrow);  // Right of recipe grid
        setItem(32, arrow);  // Below first arrow

        // Output item (right side)
        ItemStack outputStack = recipeItem.getItemStack();
        if (outputStack != null) {
            ItemStack output = outputStack.clone();
            setItem(25, output);  // Right of arrows
        }
    }

    /**
     * Build the Automated Panning Machine recipe display (shows structure + Gravel → Sifted Ore Dust only)
     */
    private void buildAutomatedPanningRecipes() {
        // === TOP SECTION: HOW TO BUILD THE MULTIBLOCK ===

        // Get the structure recipe
        ItemStack[] recipe = recipeItem.getRecipe();

        // Display structure in a 3x3 grid
        int[] recipeSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < Math.min(recipe.length, 9); i++) {
            if (recipe[i] != null) {
                ItemStack clickableIngredient = addClickableLore(recipe[i]);
                setItem(recipeSlots[i], clickableIngredient);
            }
        }

        // Machine info (top center)
        ItemStack machine = new ItemStack(Material.CAULDRON);
        ItemMeta machineMeta = machine.getItemMeta();
        if (machineMeta != null) {
            machineMeta.setDisplayName(ChatColor.GOLD + "Automated Panning Machine");
            machineMeta.setLore(List.of(
                ChatColor.GRAY + "Build this multiblock in-game"
            ));
            machine.setItemMeta(machineMeta);
        }
        setItem(4, machine);

        // Arrow indicator (pointing down)
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName(ChatColor.GREEN + "→");
            arrow.setItemMeta(arrowMeta);
        }
        setItem(14, arrow);

        // Output/Info (shows the Automated Panning Machine item)
        ItemStack outputStack = recipeItem.getItemStack();
        if (outputStack != null) {
            ItemStack output = outputStack.clone();
            setItem(16, output);
        }

        // === BOTTOM SECTION: GRAVEL → SIFTED ORE DUST RECIPE ===

        SiftedOreDust siftedOreDust = new SiftedOreDust();

        // Row 5: Gravel → Sifted Ore Dust
        ItemStack gravel = new ItemStack(Material.GRAVEL);
        setItem(36, gravel);

        ItemStack arrow1 = new ItemStack(Material.ARROW);
        ItemMeta arrow1Meta = arrow1.getItemMeta();
        if (arrow1Meta != null) {
            arrow1Meta.setDisplayName(ChatColor.GREEN + "→ Pan");
            arrow1.setItemMeta(arrow1Meta);
        }
        setItem(37, arrow1);

        setItem(38, addClickableLore(siftedOreDust.getItemStack()));

        // Add info text
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "What's Next?");
            infoMeta.setLore(List.of(
                ChatColor.GRAY + "Take Sifted Ore Dust to the",
                ChatColor.GRAY + "Ore Washer to separate it into",
                ChatColor.GRAY + "specific metal dusts!"
            ));
            info.setItemMeta(infoMeta);
        }
        setItem(39, info);
    }
    
    @Override
    public void handleClick(int slot, ItemStack clicked) {
        if (slot == 0) { // Back
            MenuManager.goBack(player);
        } else if (slot == 8) { // Home
            MenuManager.goHome(player);
        } else if (slot == 4) { // Machine icon clicked
            navigateToMachine(recipeItem.getMachineType());
        } else {
            // Check if a recipe ingredient was clicked
            if (clicked != null && clicked.hasItemMeta()) {
                String itemId = RecipeItem.getItemId(clicked);
                if (itemId != null) {
                    // Look up the recipe for this item
                    RecipeItem ingredientRecipe = ItemRegistry.getItemById(itemId);
                    if (ingredientRecipe != null) {
                        // Navigate to the ingredient's recipe
                        MenuManager.pushHistory(player, this);
                        new RecipeMenu(player, ingredientRecipe).open();
                    }
                }
            }
        }
    }

    /**
     * Get the appropriate icon for a machine type
     */
    private Material getMachineIcon(String machineType) {
        return switch (machineType) {
            case "Automated Panning Machine" -> Material.CAULDRON;
            case "Ore Washer" -> Material.CAULDRON;
            case "Enhanced Crafting Table" -> Material.CRAFTING_TABLE;
            case "Smelter" -> Material.BLAST_FURNACE;
            case "Compressor" -> Material.PISTON;
            case "Pressure Chamber" -> Material.DISPENSER;
            case "Multiblock" -> Material.CRAFTING_TABLE;
            default -> Material.BLAST_FURNACE;
        };
    }

    /**
     * Navigate to the machine's recipe when the machine icon is clicked
     */
    private void navigateToMachine(String machineType) {
        // Try to find the machine in the workstation registry
        RecipeItem machine = null;

        if (machineType.equals("Automated Panning Machine")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.AutomatedPanning();
        } else if (machineType.equals("Ore Washer")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.OreWasher();
        } else if (machineType.equals("Enhanced Crafting Table")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.BasicWorkbench();
        } else if (machineType.equals("Smelter")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.Smelter();
        } else if (machineType.equals("Compressor")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.Compressor();
        } else if (machineType.equals("Pressure Chamber")) {
            machine = new org.ThefryGuy.techFactory.recipes.workstations.multiblocks.PressureChamber();
        }

        if (machine != null) {
            MenuManager.pushHistory(player, this);
            new RecipeMenu(player, machine).open();
        } else {
            player.sendMessage(ChatColor.YELLOW + "Machine recipe not available yet!");
        }
    }
    


    /**
     * Add clickable lore to a recipe ingredient
     * This adds a hint that the player can click to view the recipe
     */
    private ItemStack addClickableLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        // Check if this is a TechFactory item (has item_id NBT)
        String itemId = RecipeItem.getItemId(item);
        if (itemId == null) {
            // Not a TechFactory item, return as-is
            return item;
        }

        // Clone the item so we don't modify the original
        ItemStack clickable = item.clone();
        ItemMeta meta = clickable.getItemMeta();

        // Safety check for null meta
        if (meta == null) {
            return clickable;
        }

        // Add clickable hint to lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.YELLOW + "▶ Click to view recipe!");
        meta.setLore(lore);

        clickable.setItemMeta(meta);
        return clickable;
    }
}

