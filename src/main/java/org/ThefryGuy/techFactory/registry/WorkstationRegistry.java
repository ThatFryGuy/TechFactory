package org.ThefryGuy.techFactory.registry;

import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.workstations.multiblocks.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registry for all workstation items (multiblocks, advanced multiblocks)
 * This is initialized at plugin startup and provides easy access to all workstations.
 */
public class WorkstationRegistry {

    /**
     * Registry for multiblock workstations
     */
    private static final Map<String, Supplier<? extends RecipeItem>> MULTIBLOCK_REGISTRY = Map.ofEntries(
            Map.entry("Automated Panning Machine", AutomatedPanning::new),
            Map.entry("Enhanced Crafting Table", BasicWorkbench::new),
            Map.entry("Smelter", Smelter::new),
            Map.entry("Compressor", Compressor::new),
            Map.entry("Pressure Chamber", PressureChamber::new),
            Map.entry("Ore Washer", OreWasher::new),
            Map.entry("Ore Crusher", OreCrusher::new),
            Map.entry("Output Chest", OutputChestInfo::new)
            // Add more multiblocks here as you create them:
    );

    /**
     * Registry for advanced multiblocks (future)
     */
    private static final Map<String, Supplier<? extends RecipeItem>> ADVANCED_MULTIBLOCK_REGISTRY = Map.ofEntries(
            // Map.entry("Enhanced Crafting Table", EnhancedCraftingTable::new),
            // Map.entry("Armor Forge", ArmorForge::new),
    );

    /**
     * Initialize the registry (called at plugin startup)
     */
    public static void initialize() {
        // Nothing to initialize - registries are static final
    }

    /**
     * Get a multiblock workstation by name
     */
    public static RecipeItem getMultiblock(String name) {
        Supplier<? extends RecipeItem> supplier = MULTIBLOCK_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all multiblock workstations as a list
     */
    public static List<RecipeItem> getMultiblocks() {
        List<RecipeItem> multiblocks = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : MULTIBLOCK_REGISTRY.values()) {
            multiblocks.add(supplier.get());
        }
        return multiblocks;
    }

    /**
     * Get all advanced multiblocks as a list
     */
    public static List<RecipeItem> getAdvancedMultiblocks() {
        List<RecipeItem> advanced = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : ADVANCED_MULTIBLOCK_REGISTRY.values()) {
            advanced.add(supplier.get());
        }
        return advanced;
    }

    /**
     * Get the multiblock registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getMultiblockRegistry() {
        return MULTIBLOCK_REGISTRY;
    }

    /**
     * Get the advanced multiblocks registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getAdvancedMultiblockRegistry() {
        return ADVANCED_MULTIBLOCK_REGISTRY;
    }
}

