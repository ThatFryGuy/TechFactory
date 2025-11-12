package org.ThefryGuy.techFactory.recipes;

import org.ThefryGuy.techFactory.recipes.dusts.SiftedOreDust;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.alloys.*;
import org.ThefryGuy.techFactory.recipes.components.*;
import org.ThefryGuy.techFactory.recipes.energy.EnergyRegulator;
import org.ThefryGuy.techFactory.recipes.energy.EnergyConnector;
import org.ThefryGuy.techFactory.recipes.energy.SmallEnergyCapacitor;
import org.ThefryGuy.techFactory.recipes.resources.*;
import org.ThefryGuy.techFactory.recipes.tools.GoldPan;
import org.ThefryGuy.techFactory.recipes.workstations.electric.ElectricFurnace;
import org.ThefryGuy.techFactory.recipes.workstations.electric.ElectricGoldPan;

import java.util.*;

/**
 * Central registry for ALL machine recipes across ALL workstations
 * 
 * WHY THIS EXISTS:
 * - Before: Each machine (Smelter, Ore Crusher, Basic Workbench) had its own hardcoded recipe map
 * - After: One centralized place to register and query recipes
 * - When adding new machines, just call RecipeRegistry.getRecipesFor("MachineName")
 * - If recipe support changes, edit ONE file instead of 7+
 * 
 * INSPIRED BY: Slimefun's RecipeRegistry pattern
 * 
 * USAGE:
 *   // Initialize on plugin startup:
 *   RecipeRegistry.initialize();
 *   
 *   // Query recipes for a machine:
 *   List<RecipeEntry> smelterRecipes = RecipeRegistry.getRecipesFor("Smelter");
 *   
 *   // Find a specific recipe by sorted item key:
 *   RecipeItem output = RecipeRegistry.findRecipe("Smelter", "copper_dust:3|tin_dust:1");
 */
public class RecipeRegistry {
    
    /**
     * Recipe entry: machine type -> sorted item key -> output item
     * Example: "Smelter" -> "copper_dust:3|tin_dust:1" -> BronzeIngot
     */
    private static final Map<String, Map<String, RecipeItem>> RECIPES_BY_MACHINE = new HashMap<>();
    
    /**
     * All registered recipes (for listing/debugging)
     */
    private static final List<RecipeEntry> ALL_RECIPES = new ArrayList<>();
    
    /**
     * Initialize all recipes on plugin startup
     * Called from TechFactory.onEnable()
     */
    public static void initialize() {
        RECIPES_BY_MACHINE.clear();
        ALL_RECIPES.clear();

        // Register all machine recipes
        registerSmelterRecipes();
        registerCrusherRecipes();
        registerWorkbenchRecipes();
        registerCompressorRecipes();
        registerPressureChamberRecipes();
        registerOreWasherRecipes();
        // NOTE: AutomatedPanningMachine uses simple 1:1 conversion + random loot, not RecipeRegistry

        // FUTURE: Power panel recipes, cargo recipes, etc.
    }
    
    /**
     * Register all Smelter recipes
     */
    private static void registerSmelterRecipes() {
        // ===== COMPLEX RECIPES FROM BASIC SMELTER =====
        
        // Synthic Sapphire: Aluminum Dust + Glass + Glass Pane + Aluminum Ingot + Lapis Lazuli
        addRecipe("Smelter", new SynthicSapphire(), "aluminum_dust", "glass", "glass_pane", "aluminum_ingot", "lapis_lazuli");
        
        // Raw Carbonado: Synthetic Diamond + Carbon Chunk + Glass Pane
        addRecipe("Smelter", new RawCarbonado(), "synthetic_diamond", "carbon_chunk", "glass_pane");
        
        // Silicon: Block of Quartz
        addRecipe("Smelter", new Silicon(), "quartz_block");
        
        // Gilded Iron: 24 Karat Gold Ingot + Iron Dust
        addRecipe("Smelter", new GildedIron(), "24_karat_gold_ingot", "iron_dust");
        
        // Synthetic Emerald: Synthetic Sapphire + Aluminum Dust + Aluminum Ingot + Glass Pane
        addRecipe("Smelter", new SyntheticEmerald(), "synthic_sapphire", "aluminum_dust", "aluminum_ingot", "glass_pane");
        
        // Ferrosilicon: Iron Ingot + Iron Dust + Silicon
        addRecipe("Smelter", new Ferrosilicon(), "iron_ingot", "iron_dust", "silicon");
        
        // Redstone Alloy Ingot: Redstone Dust + Block of Redstone + Ferrosilicon + Hardened Metal
        addRecipe("Smelter", new RedstoneAlloyIngot(), "redstone", "redstone_block", "ferrosilicon", "hardened_metal_ingot");
        
        // ===== ORIGINAL SMELTER ALLOY RECIPES =====
        
        // Bronze: Copper Dust + Tin Dust + Copper Ingot (3 items)
        addRecipe("Smelter", new BronzeIngot(), "copper_dust", "tin_dust", "copper_ingot");
        
        // Duralumin: Aluminum Dust + Copper Dust + Aluminum Ingot (3 items)
        addRecipe("Smelter", new DuraluminIngot(), "aluminum_dust", "copper_dust", "aluminum_ingot");

        // Brass: Copper Dust + Zinc Dust + Copper Ingot (3 items)
        addRecipe("Smelter", new BrassIngot(), "copper_dust", "zinc_dust", "copper_ingot");

        // Aluminum Bronze: Aluminum Dust + Bronze Ingot + Aluminum Ingot (3 items)
        // FIXED: Was using copper_dust, should use bronze_ingot (caught by validation!)
        addRecipe("Smelter", new AluminumBronzeIngot(), "aluminum_dust", "bronze_ingot", "aluminum_ingot");
        
        // Corinthian Bronze: Copper Dust + Gold Dust + Copper Ingot (3 items)
        addRecipe("Smelter", new CorinthianBronzeIngot(), "copper_dust", "gold_dust", "copper_ingot");
        
        // Solder: Lead Dust + Tin Dust + Lead Ingot (3 items)
        addRecipe("Smelter", new SolderIngot(), "lead_dust", "tin_dust", "lead_ingot");
        
        // Steel: Iron Dust + Carbon + Iron Ingot (3 items)
        addRecipe("Smelter", new SteelIngot(), "iron_dust", "carbon", "iron_ingot");
        
        // Cobalt: Iron Dust + Copper Dust + Nickel Ingot (3 items)
        addRecipe("Smelter", new CobaltIngot(), "iron_dust", "copper_dust", "nickel_ingot");
        
        // Damascus Steel: Steel Ingot + Iron Dust + Carbon + Iron Ingot (4 items)
        addRecipe("Smelter", new DamascusSteelIngot(), "steel_ingot", "iron_dust", "carbon", "iron_ingot");
        
        // Hardened Metal: Damascus Steel + Duralumin + Compressed Carbon + Aluminum Bronze (4 items)
        addRecipe("Smelter", new HardenedMetalIngot(), "damascus_steel_ingot", "duralumin_ingot", "compressed_carbon", "aluminum_bronze_ingot");
        
        // Billon: Silver Dust + Copper Dust + Silver Ingot (3 items)
        addRecipe("Smelter", new BillonIngot(), "silver_dust", "copper_dust", "silver_ingot");
        
        // 24 Karat Gold: 1 Gold Dust + 10 Gold Ingots (2 items with quantities!)
        addRecipe("Smelter", new TwentyFourKaratGoldIngot(), "gold_dust:1", "gold_ingot:10");
        
        // Reinforced Alloy: Damascus Steel + Hardened Metal + Corinthian Bronze + Solder + Billon + 24 Karat Gold (6 items!)
        addRecipe("Smelter", new ReinforcedAlloyIngot(),
            "damascus_steel_ingot", "hardened_metal_ingot", "corinthian_bronze_ingot",
            "solder_ingot", "billon_ingot", "24_karat_gold_ingot");
        
        // Magnet: Nickel Ingot + Aluminum Dust + Iron Dust + Cobalt Ingot (4 items)
        addRecipe("Smelter", new Magnet(), "nickel_ingot", "aluminum_dust", "iron_dust", "cobalt_ingot");
    }
    
    /**
     * Register all Ore Crusher recipes
     */
    private static void registerCrusherRecipes() {
        // Sulfate: 16x Netherrack → 1x Sulfate
        addRecipe("Ore Crusher", new Sulfate(), "netherrack:16");
    }
    
    /**
     * Register all Basic Workbench recipes
     */
    private static void registerWorkbenchRecipes() {
        // Carbon Chunk: 8x Compressed Carbon + 1x Flint
        addRecipe("Basic Workbench", new CarbonChunk(), "compressed_carbon:8", "flint:1");
        
        // Gold Pan: 5x Stone + 1x Bowl
        addRecipe("Basic Workbench", new GoldPan(), "stone:5", "bowl:1");
        
        // Basic Circuit Board: 1x Redstone, 1x Iron Ingot, 2x Copper Ingot, 1x Gold Nugget, 2x Glass Pane, 1x Iron Nugget
        addRecipe("Basic Workbench", new BasicCircuitBoard(), "redstone:1", "iron_ingot:1", "copper_ingot:2", "gold_nugget:1", "glass_pane:2", "iron_nugget:1");
        
        // Advanced Circuit Board: 3x Lapis Block, 2x Redstone Block, 1x Basic Circuit Board
        addRecipe("Basic Workbench", new AdvancedCircuitBoard(), "lapis_block:3", "redstone_block:2", "basic_circuit_board:1");
        
        // Battery: 1x Redstone, 2x Zinc Ingot, 2x Sulfate, 2x Copper Ingot
        addRecipe("Basic Workbench", new Battery(), "redstone:1", "zinc_ingot:2", "sulfate:2", "copper_ingot:2");
        
        // Electromagnet: 1x Nickel Ingot, 1x Magnet, 1x Cobalt Ingot, 1x Battery
        addRecipe("Basic Workbench", new Electromagnet(), "nickel_ingot:1", "magnet:1", "cobalt_ingot:1", "battery:1");
        
        // Copper Wire: 3x Copper Ingot (outputs 8x Copper Wire)
        addRecipe("Basic Workbench", new CopperWire(), "copper_ingot:3");
        
        // Electric Motor: 6x Copper Wire, 1x Electromagnet
        addRecipe("Basic Workbench", new ElectricMotor(), "copper_wire:6", "electromagnet:1");
        
        // Heating Coil: 8x Copper Wire, 1x Electric Motor
        addRecipe("Basic Workbench", new HeatingCoil(), "copper_wire:8", "electric_motor:1");
        
        // Energy Regulator: 4x Silver Ingot, 4x Damascus Steel Ingot, 1x Electric Motor
        addRecipe("Basic Workbench", new EnergyRegulator(), "silver_ingot:4", "damascus_steel_ingot:4", "electric_motor:1");

        // Energy Connector: 4x Carbon, 4x Copper Wire, 1x Redstone Block (outputs 8x Energy Connector)
        addRecipe("Basic Workbench", new EnergyConnector(), "carbon:4", "copper_wire:4", "redstone_block:1");

        // Photovoltaic Cell: 3x Glass, 3x Silicon, 3x Ferrosilicon
        addRecipe("Basic Workbench", new org.ThefryGuy.techFactory.recipes.components.PhotovoltaicCell(), "glass:3", "silicon:3", "ferrosilicon:3");

        // Solar Generator: 3x Photovoltaic Cell, 3x Aluminum Ingot, 1x Electric Motor
        addRecipe("Basic Workbench", new org.ThefryGuy.techFactory.recipes.energy.SolarGenerator(), "photovoltaic_cell:3", "aluminum_ingot:3", "electric_motor:1");

        // Small Energy Capacitor: 4x Duralumin Ingot, 1x Sulfate, 2x Redstone Alloy Ingot, 1x Energy Connector, 1x Redstone Dust
        addRecipe("Enhanced Crafting Table", new SmallEnergyCapacitor(), "duralumin_ingot:4", "sulfate:1", "redstone_alloy_ingot:2", "energy_connector:1", "redstone:1");

        // Electric Furnace: 1x Furnace, 4x Gilded Iron, 1x Heating Coil, 1x Electric Motor
        addRecipe("Enhanced Crafting Table", new ElectricFurnace(), "furnace:1", "gilded_iron:4", "heating_coil:1", "electric_motor:1");

        // Electric Gold Pan: 1x Gold Pan, 2x Flint, 3x Aluminum Ingot, 1x Electric Motor
        addRecipe("Enhanced Crafting Table", new ElectricGoldPan(), "gold_pan:1", "flint:2", "aluminum_ingot:3", "electric_motor:1");
    }

    /**
     * Register all Compressor recipes
     */
    private static void registerCompressorRecipes() {
        // 9x Coal → 1x Compressed Carbon
        addRecipe("Compressor", new CompressedCarbon(), "coal:9");

        // 4x Carbon → 1x Compressed Carbon (legacy recipe)
        addRecipe("Compressor", new CompressedCarbon(), "carbon:4");
    }

    /**
     * Register all Pressure Chamber recipes
     */
    private static void registerPressureChamberRecipes() {
        // 1x Carbon Chunk → 1x Synthetic Diamond
        addRecipe("Pressure Chamber", new SyntheticDiamond(), "carbon_chunk:1");

        // 1x Raw Carbonado → 1x Carbonado
        addRecipe("Pressure Chamber", new Carbonado(), "raw_carbonado:1");
    }

    /**
     * Register all Ore Washer recipes
     * NOTE: Ore Washer has special random output logic, but we still register the input
     */
    private static void registerOreWasherRecipes() {
        // Sifted Ore Dust → Random Metal Dust (special case - handled in OreWasherMachine)
        // We register this for completeness, but the actual output is random
        addRecipe("Ore Washer", new SiftedOreDust(), "sifted_ore_dust:1");
    }

    /**
     * Add a recipe with sorted item IDs
     * Items should be in format "item_id:quantity" (e.g., "gold_ingot:10")
     * If no quantity specified, defaults to ":1"
     *
     * INPUT VALIDATION: Throws IllegalArgumentException if inputs are invalid
     * This catches recipe registration bugs during plugin startup instead of runtime
     */
    private static void addRecipe(String machineType, RecipeItem output, String... items) {
        // VALIDATION: Machine type cannot be null or empty
        if (machineType == null || machineType.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe registration failed: Machine type cannot be null or empty");
        }

        // VALIDATION: Output cannot be null
        if (output == null) {
            throw new IllegalArgumentException("Recipe registration failed for " + machineType + ": Output item cannot be null");
        }

        // VALIDATION: Must have at least 1 input item
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Recipe registration failed for " + machineType + ": Recipe must have at least 1 input item (output: " + output.getDisplayName() + ")");
        }

        List<String> itemList = new ArrayList<>();
        for (String item : items) {
            // VALIDATION: Item cannot be null or empty
            if (item == null || item.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipe registration failed for " + machineType + ": Item ID cannot be null or empty (output: " + output.getDisplayName() + ")");
            }

            // Add ":1" if no quantity specified
            if (!item.contains(":")) {
                item = item + ":1";
            }

            // VALIDATION: Check item format (must be "item_id:quantity")
            if (!isValidItemFormat(item)) {
                throw new IllegalArgumentException("Recipe registration failed for " + machineType + ": Invalid item format '" + item + "' (expected 'item_id:quantity', e.g. 'iron_dust:3')");
            }

            itemList.add(item);
        }

        // Sort items to create a consistent key (order-independent matching)
        Collections.sort(itemList);
        String recipeKey = String.join("|", itemList);

        // VALIDATION: Check for duplicate recipes
        Map<String, RecipeItem> machineRecipes = RECIPES_BY_MACHINE.get(machineType);
        if (machineRecipes != null && machineRecipes.containsKey(recipeKey)) {
            RecipeItem existingOutput = machineRecipes.get(recipeKey);
            throw new IllegalArgumentException("Recipe registration failed for " + machineType + ": Duplicate recipe detected! Recipe '" + recipeKey + "' already outputs " + existingOutput.getDisplayName() + ", cannot also output " + output.getDisplayName());
        }

        // Add to machine-specific map
        RECIPES_BY_MACHINE.computeIfAbsent(machineType, k -> new HashMap<>())
            .put(recipeKey, output);

        // Add to all recipes list
        ALL_RECIPES.add(new RecipeEntry(machineType, recipeKey, output));
    }

    /**
     * Validate item format: "item_id:quantity"
     * - Must contain exactly one ":"
     * - Quantity must be a positive integer
     *
     * @param item The item string to validate
     * @return true if valid, false otherwise
     */
    private static boolean isValidItemFormat(String item) {
        String[] parts = item.split(":");

        // Must have exactly 2 parts (item_id and quantity)
        if (parts.length != 2) {
            return false;
        }

        // Item ID cannot be empty
        if (parts[0].trim().isEmpty()) {
            return false;
        }

        // Quantity must be a positive integer
        try {
            int quantity = Integer.parseInt(parts[1]);
            return quantity > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Get all recipes for a specific machine type
     * @param machineType The machine type (e.g., "Smelter", "Ore Crusher")
     * @return List of recipe entries for that machine
     */
    public static List<RecipeEntry> getRecipesFor(String machineType) {
        return ALL_RECIPES.stream()
            .filter(entry -> entry.machineType.equals(machineType))
            .toList();
    }
    
    /**
     * Find a recipe by machine type and sorted item key
     * @param machineType The machine type (e.g., "Smelter")
     * @param recipeKey The sorted item key (e.g., "copper_dust:3|tin_dust:1")
     * @return The output RecipeItem, or null if no match
     */
    public static RecipeItem findRecipe(String machineType, String recipeKey) {
        Map<String, RecipeItem> machineRecipes = RECIPES_BY_MACHINE.get(machineType);
        return machineRecipes != null ? machineRecipes.get(recipeKey) : null;
    }

    /**
     * Find a recipe that matches the available items (with "at least" quantity matching).
     * This allows players to put stacks of items and craft multiple times.
     *
     * Example: If recipe requires "carbon_chunk:1" and player has "carbon_chunk:64",
     * this will match and return the recipe.
     *
     * @param machineType The machine type (e.g., "Pressure Chamber")
     * @param availableItems Map of item_id -> quantity available
     * @return The matching RecipeItem and required quantities, or null if no match
     */
    public static RecipeMatch findRecipeWithQuantities(String machineType, java.util.Map<String, Integer> availableItems) {
        Map<String, RecipeItem> machineRecipes = RECIPES_BY_MACHINE.get(machineType);
        if (machineRecipes == null) {
            return null;
        }

        // Try each registered recipe for this machine
        for (Map.Entry<String, RecipeItem> entry : machineRecipes.entrySet()) {
            String recipeKey = entry.getKey();
            RecipeItem output = entry.getValue();

            // Parse the recipe key to get required items and quantities
            // Format: "item_id:quantity|item_id:quantity|..."
            String[] recipeParts = recipeKey.split("\\|");
            java.util.Map<String, Integer> requiredItems = new java.util.HashMap<>();

            for (String part : recipeParts) {
                String[] itemAndQty = part.split(":");
                String itemId = itemAndQty[0];
                int quantity = Integer.parseInt(itemAndQty[1]);
                requiredItems.put(itemId, quantity);
            }

            // Check if player has AT LEAST the required quantities
            boolean matches = true;
            for (Map.Entry<String, Integer> required : requiredItems.entrySet()) {
                String itemId = required.getKey();
                int requiredQty = required.getValue();
                int availableQty = availableItems.getOrDefault(itemId, 0);

                if (availableQty < requiredQty) {
                    matches = false;
                    break;
                }
            }

            // Also check that player doesn't have EXTRA items not in the recipe
            if (matches && availableItems.size() != requiredItems.size()) {
                matches = false;
            }

            if (matches) {
                return new RecipeMatch(output, requiredItems);
            }
        }

        return null;
    }

    /**
     * Result of a recipe match with quantity information
     */
    public static class RecipeMatch {
        public final RecipeItem output;
        public final java.util.Map<String, Integer> requiredQuantities;

        public RecipeMatch(RecipeItem output, java.util.Map<String, Integer> requiredQuantities) {
            this.output = output;
            this.requiredQuantities = requiredQuantities;
        }
    }
    
    /**
     * Get all registered recipes (for debugging/listing)
     * @return List of all recipe entries
     */
    public static List<RecipeEntry> getAllRecipes() {
        return new ArrayList<>(ALL_RECIPES);
    }
    
    /**
     * Get total number of registered recipes
     * @return Total recipe count
     */
    public static int getRecipeCount() {
        return ALL_RECIPES.size();
    }
    
    /**
     * Recipe entry data class
     */
    public static class RecipeEntry {
        public final String machineType;
        public final String recipeKey;
        public final RecipeItem output;
        
        public RecipeEntry(String machineType, String recipeKey, RecipeItem output) {
            this.machineType = machineType;
            this.recipeKey = recipeKey;
            this.output = output;
        }
    }
}

