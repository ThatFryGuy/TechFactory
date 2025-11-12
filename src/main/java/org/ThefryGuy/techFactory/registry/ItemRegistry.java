package org.ThefryGuy.techFactory.registry;

import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.*;
import org.ThefryGuy.techFactory.recipes.ingots.*;
import org.ThefryGuy.techFactory.recipes.resources.*;
import org.ThefryGuy.techFactory.recipes.alloys.*;
import org.ThefryGuy.techFactory.recipes.tools.*;
import org.ThefryGuy.techFactory.recipes.components.*;
import org.ThefryGuy.techFactory.recipes.energy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registry for all recipe items (dusts, ingots, machines, tools, alloys, etc.)
 * This is initialized at plugin startup and provides easy access to all items.
 */
public class ItemRegistry {

    /**
     * PERFORMANCE OPTIMIZATION: O(1) lookup cache for item IDs
     * Built once at startup, avoids O(n) searches through all registries
     */
    private static final Map<String, Supplier<? extends RecipeItem>> ID_CACHE = new HashMap<>();

    /**
     * Registry map: item name -> supplier that creates new instances
     * Using Supplier allows lazy instantiation and clean registry pattern
     */
    private static final Map<String, Supplier<? extends RecipeItem>> DUST_REGISTRY = Map.ofEntries(
            Map.entry("Iron Dust", IronDust::new),
            Map.entry("Gold Dust", GoldDust::new),
            Map.entry("Copper Dust", CopperDust::new),
            Map.entry("Tin Dust", TinDust::new),
            Map.entry("Sifted Ore Dust", SiftedOreDust::new),
            Map.entry("Silver Dust", SilverDust::new),
            Map.entry("Aluminum Dust", AluminumDust::new),
            Map.entry("Lead Dust", LeadDust::new),
            Map.entry("Zinc Dust", ZincDust::new),
            Map.entry("Magnesium Dust", MagnesiumDust::new)
    );

    /**
     * Registry for ingot items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> INGOT_REGISTRY = Map.ofEntries(
            Map.entry("Iron Ingot", IronIngot::new),
            Map.entry("Gold Ingot", GoldIngot::new),
            Map.entry("Copper Ingot", CopperIngot::new),
            Map.entry("Tin Ingot", TinIngot::new),
            Map.entry("Silver Ingot", SilverIngot::new),
            Map.entry("Lead Ingot", LeadIngot::new),
            Map.entry("Aluminum Ingot", AluminumIngot::new),
            Map.entry("Zinc Ingot", ZincIngot::new),
            Map.entry("Magnesium Ingot", MagnesiumIngot::new)
    );

    /**
     * Registry for resource items (carbon, etc.)
     */
    private static final Map<String, Supplier<? extends RecipeItem>> RESOURCE_REGISTRY = Map.ofEntries(
            Map.entry("Carbon", Carbon::new),
            Map.entry("Compressed Carbon", org.ThefryGuy.techFactory.recipes.resources.CompressedCarbon::new),
            Map.entry("Carbon Chunk", org.ThefryGuy.techFactory.recipes.resources.CarbonChunk::new),
            Map.entry("Synthic Sapphire", org.ThefryGuy.techFactory.recipes.resources.SynthicSapphire::new),
            Map.entry("Synthetic Diamond", org.ThefryGuy.techFactory.recipes.resources.SyntheticDiamond::new),
            Map.entry("Raw Carbonado", org.ThefryGuy.techFactory.recipes.resources.RawCarbonado::new),
            Map.entry("Carbonado", org.ThefryGuy.techFactory.recipes.resources.Carbonado::new),
            Map.entry("Silicon", org.ThefryGuy.techFactory.recipes.resources.Silicon::new),
            Map.entry("Synthetic Emerald", org.ThefryGuy.techFactory.recipes.resources.SyntheticEmerald::new),
            Map.entry("Sulfate", org.ThefryGuy.techFactory.recipes.resources.Sulfate::new)
    );

    /**
     * Registry for alloy items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> ALLOY_REGISTRY = Map.ofEntries(
            Map.entry("Bronze Ingot", BronzeIngot::new),
            Map.entry("Brass Ingot", BrassIngot::new),
            Map.entry("Steel Ingot", SteelIngot::new),
            Map.entry("Duralumin Ingot", DuraluminIngot::new),
            Map.entry("Aluminum Bronze Ingot", org.ThefryGuy.techFactory.recipes.alloys.AluminumBronzeIngot::new),
            Map.entry("Aluminum Brass Ingot", AluminumBrassIngot::new),
            Map.entry("Solder Ingot", SolderIngot::new),
            Map.entry("Corinthian Bronze Ingot", org.ThefryGuy.techFactory.recipes.alloys.CorinthianBronzeIngot::new),
            Map.entry("Nickel Ingot", org.ThefryGuy.techFactory.recipes.alloys.NickelIngot::new),
            Map.entry("Cobalt Ingot", org.ThefryGuy.techFactory.recipes.alloys.CobaltIngot::new),
            Map.entry("Damascus Steel Ingot", org.ThefryGuy.techFactory.recipes.alloys.DamascusSteelIngot::new),
            Map.entry("Hardened Metal Ingot", org.ThefryGuy.techFactory.recipes.alloys.HardenedMetalIngot::new),
            Map.entry("Billon Ingot", org.ThefryGuy.techFactory.recipes.alloys.BillonIngot::new),
            Map.entry("24 Karat Gold Ingot", org.ThefryGuy.techFactory.recipes.alloys.TwentyFourKaratGoldIngot::new),
            Map.entry("Reinforced Alloy Ingot", org.ThefryGuy.techFactory.recipes.alloys.ReinforcedAlloyIngot::new),
            Map.entry("Gilded Iron", org.ThefryGuy.techFactory.recipes.alloys.GildedIron::new),
            Map.entry("Ferrosilicon", org.ThefryGuy.techFactory.recipes.alloys.Ferrosilicon::new),
            Map.entry("Redstone Alloy Ingot", org.ThefryGuy.techFactory.recipes.alloys.RedstoneAlloyIngot::new)
    );

    /**
     * Registry for tool items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> TOOL_REGISTRY = Map.ofEntries(
            Map.entry("Gold Pan", GoldPan::new),
            Map.entry("Nether Gold Pan", org.ThefryGuy.techFactory.recipes.tools.NetherGoldPan::new)
    );

    /**
     * Registry for technical component items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> COMPONENT_REGISTRY = Map.ofEntries(
            Map.entry("Basic Circuit Board", BasicCircuitBoard::new),
            Map.entry("Advanced Circuit Board", AdvancedCircuitBoard::new),
            Map.entry("Battery", Battery::new),
            Map.entry("Magnet", org.ThefryGuy.techFactory.recipes.components.Magnet::new),
            Map.entry("Electromagnet", org.ThefryGuy.techFactory.recipes.components.Electromagnet::new),
            Map.entry("Copper Wire", org.ThefryGuy.techFactory.recipes.components.CopperWire::new),
            Map.entry("Electric Motor", org.ThefryGuy.techFactory.recipes.components.ElectricMotor::new),
            Map.entry("Heating Coil", org.ThefryGuy.techFactory.recipes.components.HeatingCoil::new),
            Map.entry("Photovoltaic Cell", org.ThefryGuy.techFactory.recipes.components.PhotovoltaicCell::new)
    );

    /**
     * Registry for energy items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> ENERGY_REGISTRY = Map.ofEntries(
            Map.entry("Energy Regulator", EnergyRegulator::new),
            Map.entry("Energy Connector", EnergyConnector::new),
            Map.entry("Solar Generator", org.ThefryGuy.techFactory.recipes.energy.SolarGenerator::new),
            Map.entry("Small Energy Capacitor", org.ThefryGuy.techFactory.recipes.energy.SmallEnergyCapacitor::new)
    );

    /**
     * Registry for electric machine items
     */
    private static final Map<String, Supplier<? extends RecipeItem>> MACHINE_REGISTRY = Map.ofEntries(
            Map.entry("Electric Furnace", org.ThefryGuy.techFactory.recipes.workstations.electric.ElectricFurnace::new),
            Map.entry("Electric Gold Pan", org.ThefryGuy.techFactory.recipes.workstations.electric.ElectricGoldPan::new)
    );

    /**
     * Initialize the registry (called at plugin startup)
     * Builds the ID_CACHE for O(1) lookups
     *
     * @param logger The plugin logger for logging cache size
     */
    public static void initialize(java.util.logging.Logger logger) {
        ID_CACHE.clear();

        // Build ID cache from all registries
        // This allows O(1) lookup instead of O(n) search through 7+ registries

        // Add all dusts to cache
        for (Supplier<? extends RecipeItem> supplier : DUST_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all ingots to cache
        for (Supplier<? extends RecipeItem> supplier : INGOT_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all resources to cache
        for (Supplier<? extends RecipeItem> supplier : RESOURCE_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all alloys to cache
        for (Supplier<? extends RecipeItem> supplier : ALLOY_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all tools to cache
        for (Supplier<? extends RecipeItem> supplier : TOOL_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all components to cache
        for (Supplier<? extends RecipeItem> supplier : COMPONENT_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all energy items to cache
        for (Supplier<? extends RecipeItem> supplier : ENERGY_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all machine items to cache
        for (Supplier<? extends RecipeItem> supplier : MACHINE_REGISTRY.values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Add all workstation/multiblock items to cache
        for (Supplier<? extends RecipeItem> supplier : WorkstationRegistry.getMultiblockRegistry().values()) {
            RecipeItem item = supplier.get();
            ID_CACHE.put(item.getId(), supplier);
        }

        // Log cache size for debugging
        if (logger != null) {
            logger.info("ItemRegistry: Built ID cache with " + ID_CACHE.size() + " items for O(1) lookups");
        }
    }

    /**
     * Get a dust item by name
     */
    public static RecipeItem getDust(String name) {
        Supplier<? extends RecipeItem> supplier = DUST_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all dusts as a list
     */
    public static List<RecipeItem> getDusts() {
        List<RecipeItem> dusts = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : DUST_REGISTRY.values()) {
            dusts.add(supplier.get());
        }
        return dusts;
    }

    /**
     * Get an ingot item by name
     */
    public static RecipeItem getIngot(String name) {
        Supplier<? extends RecipeItem> supplier = INGOT_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all ingots as a list
     */
    public static List<RecipeItem> getIngots() {
        List<RecipeItem> ingots = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : INGOT_REGISTRY.values()) {
            ingots.add(supplier.get());
        }
        return ingots;
    }

    /**
     * Get the dust registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getDustRegistry() {
        return DUST_REGISTRY;
    }

    /**
     * Get the ingot registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getIngotRegistry() {
        return INGOT_REGISTRY;
    }

    /**
     * Get a resource item by name
     */
    public static RecipeItem getResource(String name) {
        Supplier<? extends RecipeItem> supplier = RESOURCE_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all resources as a list
     */
    public static List<RecipeItem> getResources() {
        List<RecipeItem> resources = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : RESOURCE_REGISTRY.values()) {
            resources.add(supplier.get());
        }
        return resources;
    }

    /**
     * Get the resource registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getResourceRegistry() {
        return RESOURCE_REGISTRY;
    }

    /**
     * Get an alloy item by name
     */
    public static RecipeItem getAlloy(String name) {
        Supplier<? extends RecipeItem> supplier = ALLOY_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all alloys as a list
     */
    public static List<RecipeItem> getAlloys() {
        List<RecipeItem> alloys = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : ALLOY_REGISTRY.values()) {
            alloys.add(supplier.get());
        }
        return alloys;
    }

    /**
     * Get the alloy registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getAlloyRegistry() {
        return ALLOY_REGISTRY;
    }

    /**
     * Get a tool item by name
     */
    public static RecipeItem getTool(String name) {
        Supplier<? extends RecipeItem> supplier = TOOL_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all tools as a list
     */
    public static List<RecipeItem> getTools() {
        List<RecipeItem> tools = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : TOOL_REGISTRY.values()) {
            tools.add(supplier.get());
        }
        return tools;
    }

    /**
     * Get the tool registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getToolRegistry() {
        return TOOL_REGISTRY;
    }

    /**
     * Get a component item by name
     */
    public static RecipeItem getComponent(String name) {
        Supplier<? extends RecipeItem> supplier = COMPONENT_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all components as a list
     */
    public static List<RecipeItem> getComponents() {
        List<RecipeItem> components = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : COMPONENT_REGISTRY.values()) {
            components.add(supplier.get());
        }
        return components;
    }

    /**
     * Get the component registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getComponentRegistry() {
        return COMPONENT_REGISTRY;
    }

    /**
     * Get an energy item by name
     */
    public static RecipeItem getEnergy(String name) {
        Supplier<? extends RecipeItem> supplier = ENERGY_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all energy items as a list
     */
    public static List<RecipeItem> getEnergyItems() {
        List<RecipeItem> energyItems = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : ENERGY_REGISTRY.values()) {
            energyItems.add(supplier.get());
        }
        return energyItems;
    }

    /**
     * Get the energy registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getEnergyRegistry() {
        return ENERGY_REGISTRY;
    }

    /**
     * Get a machine item by name
     */
    public static RecipeItem getMachine(String name) {
        Supplier<? extends RecipeItem> supplier = MACHINE_REGISTRY.get(name);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Get all machines as a list
     */
    public static List<RecipeItem> getMachines() {
        List<RecipeItem> machines = new ArrayList<>();
        for (Supplier<? extends RecipeItem> supplier : MACHINE_REGISTRY.values()) {
            machines.add(supplier.get());
        }
        return machines;
    }

    /**
     * Get the machine registry
     */
    public static Map<String, Supplier<? extends RecipeItem>> getMachineRegistry() {
        return MACHINE_REGISTRY;
    }

    /**
     * Get any item by its ID (searches all registries)
     * PERFORMANCE: Uses O(1) cache lookup instead of O(n) search through 7+ registries
     *
     * @param itemId The item ID (e.g. "iron_dust", "bronze_ingot")
     * @return The RecipeItem, or null if not found
     */
    public static RecipeItem getItemById(String itemId) {
        if (itemId == null) {
            return null;
        }

        // O(1) lookup from cache (built at startup in initialize())
        Supplier<? extends RecipeItem> supplier = ID_CACHE.get(itemId);
        if (supplier != null) {
            return supplier.get();
        }

        return null;
    }

    /**
     * Get all item IDs from all registries (for tab completion)
     *
     * @return List of all item IDs (e.g. "iron_dust", "bronze_ingot", etc.)
     */
    public static List<String> getAllItemIds() {
        List<String> itemIds = new ArrayList<>();

        // Add all dust IDs
        for (Supplier<? extends RecipeItem> supplier : DUST_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all ingot IDs
        for (Supplier<? extends RecipeItem> supplier : INGOT_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all resource IDs
        for (Supplier<? extends RecipeItem> supplier : RESOURCE_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all alloy IDs
        for (Supplier<? extends RecipeItem> supplier : ALLOY_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all tool IDs
        for (Supplier<? extends RecipeItem> supplier : TOOL_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all component IDs
        for (Supplier<? extends RecipeItem> supplier : COMPONENT_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all energy item IDs
        for (Supplier<? extends RecipeItem> supplier : ENERGY_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all machine IDs
        for (Supplier<? extends RecipeItem> supplier : MACHINE_REGISTRY.values()) {
            itemIds.add(supplier.get().getId());
        }

        // Add all workstation IDs
        for (Supplier<? extends RecipeItem> supplier : WorkstationRegistry.getMultiblockRegistry().values()) {
            itemIds.add(supplier.get().getId());
        }

        return itemIds;
    }

    // Future methods:
    // public static RecipeItem getMachine(String name) { ... }
    // etc.
}