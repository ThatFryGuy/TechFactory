package org.ThefryGuy.techFactory;

import org.ThefryGuy.techFactory.commands.*;
import org.ThefryGuy.techFactory.data.SmeltingManager;
import org.ThefryGuy.techFactory.data.DatabaseManager;
import org.ThefryGuy.techFactory.data.AutoSaveManager;
import org.ThefryGuy.techFactory.data.MultiblockCache;
import org.ThefryGuy.techFactory.energy.EnergyManager;
import org.ThefryGuy.techFactory.gui.MenuManager;
import org.ThefryGuy.techFactory.listeners.MultiblockListener;
import org.ThefryGuy.techFactory.listeners.ToolListener;
import org.ThefryGuy.techFactory.listeners.GuidebookListener;
import org.ThefryGuy.techFactory.listeners.EnergyBlockListener;
import org.ThefryGuy.techFactory.listeners.ElectricMachineListener;
import org.ThefryGuy.techFactory.listeners.BlockProtectionListener;
import org.ThefryGuy.techFactory.listeners.ChunkLoadListener;
import org.ThefryGuy.techFactory.listeners.WorldUnloadListener;
import org.ThefryGuy.techFactory.recipes.RecipeRegistry;
import org.ThefryGuy.techFactory.registry.ItemRegistry;
import org.ThefryGuy.techFactory.registry.MachineRegistry;
import org.ThefryGuy.techFactory.registry.ElectricMachineRegistry;
import org.ThefryGuy.techFactory.registry.ManagerRegistry;
import org.ThefryGuy.techFactory.registry.WorkstationRegistry;
import org.ThefryGuy.techFactory.registry.handlers.*;
import org.ThefryGuy.techFactory.workstations.multiblocks.SmelterMachine;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TechFactory extends JavaPlugin {

    private static TechFactory instance;
    private SmeltingManager smeltingManager;
    private DatabaseManager databaseManager;
    private EnergyManager energyManager;
    private AutoSaveManager autoSaveManager;
    private MultiblockCache multiblockCache;

    @Override
    public void onEnable() {
        instance = this;

        // ========================================
        // CONFIGURATION SYSTEM
        // ========================================
        // Load config.yml FIRST (before anything else depends on it)
        TechFactoryConfig.load(this);

        // Initialize all registries at startup
        ItemRegistry.initialize(getLogger());
        WorkstationRegistry.initialize();
        RecipeRegistry.initialize();  // CENTRALIZED: All machine recipes in one place

        // ========================================
        // MACHINE REGISTRY PATTERN
        // ========================================
        // Register all multiblock machines
        // BENEFIT: Adding a new machine = 1 line of code here, no changes to MultiblockListener!
        MachineRegistry.register(new SmelterMachineHandler(this));
        MachineRegistry.register(new OreCrusherMachineHandler(this));
        MachineRegistry.register(new PressureChamberMachineHandler(this));
        MachineRegistry.register(new CompressorMachineHandler(this));
        MachineRegistry.register(new OreWasherMachineHandler(this));
        MachineRegistry.register(new AutomatedPanningMachineHandler(this));
        MachineRegistry.register(new EnhancedCraftingTableMachineHandler(this));
        MachineRegistry.initialize(getLogger());

        // ========================================
        // ELECTRIC MACHINE REGISTRY PATTERN
        // ========================================
        // Register all electric machines
        // BENEFIT: Adding a new electric machine = 1 line of code here, no changes to ElectricMachineListener!
        ElectricMachineRegistry.register(new ElectricFurnaceHandler());
        ElectricMachineRegistry.register(new ElectricGoldPanHandler());
        ElectricMachineRegistry.initialize(getLogger());

        // ========================================
        // MANAGER REGISTRY PATTERN
        // ========================================
        // Create all managers (but don't initialize yet)
        databaseManager = new DatabaseManager(this);
        multiblockCache = new MultiblockCache(this);
        energyManager = new EnergyManager(this);
        smeltingManager = new SmeltingManager(this);
        // Convert ticks to minutes for AutoSaveManager (6000 ticks = 5 minutes)
        int saveIntervalMinutes = (int) (TechFactoryConstants.AUTO_SAVE_INTERVAL_TICKS() / 1200L); // 1200 ticks = 1 minute
        autoSaveManager = new AutoSaveManager(this, saveIntervalMinutes);

        // Register all managers in initialization order
        // CRITICAL: DatabaseManager MUST be first (others depend on it)
        ManagerRegistry.register("DatabaseManager", databaseManager);
        ManagerRegistry.register("MultiblockCache", multiblockCache);
        ManagerRegistry.register("EnergyManager", energyManager);
        ManagerRegistry.register("SmeltingManager", smeltingManager);
        ManagerRegistry.register("AutoSaveManager", autoSaveManager);

        // Initialize all managers in order (with error handling)
        ManagerRegistry.initializeAll(getLogger());

        // PERFORMANCE FIX: Start smelter auto-save task (batches DB saves)
        SmelterMachine.startAutoSaveTask(this);

        // PERFORMANCE FIX: Initialize Electric Furnace recipe cache
        org.ThefryGuy.techFactory.machines.electric.ElectricFurnaceMachine.initialize(this);

        // Initialize Electric Gold Pan
        org.ThefryGuy.techFactory.machines.electric.ElectricGoldPanMachine.initialize(this);

        // PERFORMANCE FIX: Start consolidated electric machine processor
        // This replaces separate tasks for each machine type (50% scheduler overhead reduction)
        org.ThefryGuy.techFactory.machines.electric.ElectricMachineProcessor.initialize(this);

        // Register the /techfactory command
        if (getCommand("techfactory") != null) {
            TechFactoryCommand mainCommand = new TechFactoryCommand();

            // Inject subcommands for routing
            mainCommand.setGuideCommand(new GuideCommand());
            mainCommand.setGuidebookCommand(new GuidebookCommand());
            mainCommand.setGiveCommand(new GiveCommand());
            mainCommand.setStatusCommand(new StatusCommand());
            mainCommand.setMetricsCommand(new MetricsCommand());
            mainCommand.setReloadCommand(new ReloadCommand());
            mainCommand.setQueueCommand(new QueueCommand(this));

            // Register command executor and tab completer
            Objects.requireNonNull(getCommand("techfactory")).setExecutor(mainCommand);
            Objects.requireNonNull(getCommand("techfactory")).setTabCompleter(new TechFactoryTabCompleter());
        }

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MenuManager(), this);
        getServer().getPluginManager().registerEvents(new MultiblockListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolListener(), this);
        getServer().getPluginManager().registerEvents(new GuidebookListener(), this);
        getServer().getPluginManager().registerEvents(new EnergyBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ElectricMachineListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(this, databaseManager), this);
        getServer().getPluginManager().registerEvents(new WorldUnloadListener(this), this);  // CRITICAL FIX: Cleanup on world unload

        getLogger().info("TechFactory has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Shutdown consolidated electric machine processor
        org.ThefryGuy.techFactory.machines.electric.ElectricMachineProcessor.shutdown();

        // Shutdown individual machine data
        org.ThefryGuy.techFactory.machines.electric.ElectricFurnaceMachine.shutdown();
        org.ThefryGuy.techFactory.machines.electric.ElectricGoldPanMachine.shutdown();

        // ========================================
        // MANAGER REGISTRY PATTERN
        // ========================================
        // Disable all managers in REVERSE order (LIFO)
        // This ensures dependencies are still available during shutdown
        // Example: DatabaseManager is disabled LAST (others may need it)
        ManagerRegistry.disableAll(getLogger());

        getLogger().info("TechFactory has been disabled.");
    }

    public static TechFactory getInstance() {
        return instance;
    }

    public SmeltingManager getSmeltingManager() {
        return smeltingManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }

    public AutoSaveManager getAutoSaveManager() {
        return autoSaveManager;
    }

    public MultiblockCache getMultiblockCache() {
        return multiblockCache;
    }
}
