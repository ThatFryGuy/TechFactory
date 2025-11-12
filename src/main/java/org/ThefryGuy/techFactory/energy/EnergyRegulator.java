package org.ThefryGuy.techFactory.energy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.TechFactory;

import java.util.logging.Level;

/**
 * Energy Regulator - Core component of the energy network
 * 
 * This class handles the specific logic for Energy Regulator blocks:
 * - Structure validation (single block for now)
 * - GUI for viewing/managing energy
 * - Interaction with the energy network
 * - Future: Connecting panels and consumers
 */
public class EnergyRegulator {
    
    /**
     * Check if a block is a valid Energy Regulator
     * For now, it's just a Lightning Rod block that's registered in the database
     */
    public static boolean isValidStructure(Block block) {
        // Check if it's a lightning rod
        if (block.getType() != Material.LIGHTNING_ROD) {
            return false;
        }
        
        // Check if it's registered in the database
        TechFactory plugin = TechFactory.getInstance();
        return plugin.getDatabaseManager().hasBlock(block.getLocation());
    }
    
    /**
     * Open the Energy Regulator GUI for a player
     */
    public static void openGUI(Player player, Block regulatorBlock, EnergyNetwork network) {
        if (network == null) {
            player.sendMessage(ChatColor.RED + "Error: Energy network not found!");
            return;
        }
        
        // Create a 27-slot inventory (3 rows)
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "⚡ Energy Regulator");
        
        // Fill with glass panes
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta == null) {
            player.sendMessage(ChatColor.RED + "Error: Failed to create Energy Regulator GUI!");
            return;
        }
        paneMeta.setDisplayName(" ");
        grayPane.setItemMeta(paneMeta);
        
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, grayPane);
        }
        
        // Energy display (slot 13 - center)
        ItemStack energyDisplay = createEnergyDisplay(network);
        gui.setItem(13, energyDisplay);
        
        // Network info (slot 11)
        ItemStack networkInfo = createNetworkInfo(network);
        gui.setItem(11, networkInfo);
        
        // Connected devices (slot 15)
        ItemStack devicesInfo = createDevicesInfo(network);
        gui.setItem(15, devicesInfo);
        

        
        player.openInventory(gui);
    }
    
    /**
     * Create the energy display item
     */
    private static ItemStack createEnergyDisplay(EnergyNetwork network) {
        int percentage = network.getFillPercentage();
        Material material;
        ChatColor color;
        
        // Choose material and color based on fill percentage
        if (percentage >= 75) {
            material = Material.LIME_DYE;
            color = ChatColor.GREEN;
        } else if (percentage >= 50) {
            material = Material.YELLOW_DYE;
            color = ChatColor.YELLOW;
        } else if (percentage >= 25) {
            material = Material.ORANGE_DYE;
            color = ChatColor.GOLD;
        } else {
            material = Material.RED_DYE;
            color = ChatColor.RED;
        }
        
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new ItemStack(Material.BARRIER);
        }
        // Calculate generation rate (4 J/s per solar generator)
        int generationRate = network.getPanelCount() * 4;
        int consumptionRate = network.getConsumptionRate();

        meta.setDisplayName(color + "⚡ Energy Storage");

        // Build lore with generation and consumption rates
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.WHITE + "Stored: " + color + "" + network.getStoredEnergy() + " J");
        lore.add(ChatColor.WHITE + "Capacity: " + ChatColor.GRAY + "" + network.getMaxCapacity() + " J");
        lore.add("");

        // Add generation rate if there are generators
        if (generationRate > 0) {
            lore.add(ChatColor.WHITE + "Generation: " + ChatColor.GREEN + "+" + generationRate + " J/s");
        }

        // Add consumption rate if there is consumption
        if (consumptionRate > 0) {
            lore.add(ChatColor.WHITE + "Consumption: " + ChatColor.RED + "-" + consumptionRate + " J/s");
        }

        // Show net rate if both generation and consumption exist
        if (generationRate > 0 || consumptionRate > 0) {
            int netRate = generationRate - consumptionRate;
            ChatColor netColor = netRate >= 0 ? ChatColor.GREEN : ChatColor.RED;
            String netSign = netRate >= 0 ? "+" : "";
            lore.add(ChatColor.WHITE + "Net Rate: " + netColor + netSign + netRate + " J/s");
        }

        lore.add("");
        lore.add(color + "█".repeat(Math.max(1, percentage / 5)) + ChatColor.DARK_GRAY + "█".repeat(20 - percentage / 5));
        lore.add(ChatColor.GRAY + "" + percentage + "% Full");

        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create the network info item
     */
    private static ItemStack createNetworkInfo(EnergyNetwork network) {
        ItemStack item = new ItemStack(Material.COMPASS);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new ItemStack(Material.BARRIER);
        }
        meta.setDisplayName(ChatColor.AQUA + "Network Information");
        
        Location loc = network.getRegulatorLocation();
        String networkId = network.getNetworkId().toString().substring(0, 8);
        
        meta.setLore(java.util.List.of(
            ChatColor.GRAY + "Network ID: " + ChatColor.WHITE + networkId + "...",
            ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(),
            "",
            ChatColor.YELLOW + "This is your energy hub!",
            ChatColor.GRAY + "Connect panels to generate power",
            ChatColor.GRAY + "Connect machines to use power"
        ));
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create the connected devices info item
     */
    private static ItemStack createDevicesInfo(EnergyNetwork network) {
        ItemStack item = new ItemStack(Material.REDSTONE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Connected Devices");

        int panels = network.getPanelCount();
        int consumers = network.getConsumerCount();
        int connectors = network.getConnectorCount();

        meta.setLore(java.util.List.of(
            ChatColor.GREEN + "⚡ Energy Panels: " + ChatColor.WHITE + "" + panels,
            ChatColor.GRAY + "  (Solar, Wind, etc.)",
            "",
            ChatColor.AQUA + "⚙ Energy Consumers: " + ChatColor.WHITE + "" + consumers,
            ChatColor.GRAY + "  (Machines, etc.)",
            "",
            ChatColor.GOLD + "🔌 Energy Connectors: " + ChatColor.WHITE + "" + connectors,
            ChatColor.GRAY + "  (Network range extenders)",
            "",
            ChatColor.DARK_GRAY + "Panels/Consumers coming soon!"
        ));
        item.setItemMeta(meta);

        return item;
    }
    
    /**
     * Handle GUI click events
     * TODO: Implement when we add interactive buttons
     */
    public static void handleGUIClick(Player player, Inventory inventory, int slot, EnergyNetwork network) {
        // Prevent taking items
        // Future: Handle button clicks for adding/removing energy, connecting devices, etc.
    }
    
    /**
     * Add energy to the regulator (for testing or future energy input items)
     */
    public static void addEnergy(EnergyNetwork network, int amount) {
        int added = network.addEnergy(amount);
        // Energy added, hologram will update automatically
    }
    
    /**
     * Remove energy from the regulator (for testing or future energy output)
     */
    public static void removeEnergy(EnergyNetwork network, int amount) {
        int removed = network.removeEnergy(amount);
        // Energy removed, hologram will update automatically
    }
    
    /**
     * Get the energy network for a regulator block
     */
    public static EnergyNetwork getNetwork(Block regulatorBlock) {
        TechFactory plugin = TechFactory.getInstance();
        return plugin.getEnergyManager().getNetwork(regulatorBlock.getLocation());
    }
    
    /**
     * Check if a block is a registered Energy Regulator
     */
    public static boolean isEnergyRegulator(Block block) {
        if (block.getType() != Material.LIGHTNING_ROD) {
            return false;
        }
        
        TechFactory plugin = TechFactory.getInstance();
        return plugin.getDatabaseManager().hasBlock(block.getLocation());
    }
}

