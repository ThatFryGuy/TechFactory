package org.ThefryGuy.techFactory.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.ElectricMachine;
import org.ThefryGuy.techFactory.registry.ElectricMachineRegistry;
import org.ThefryGuy.techFactory.util.ItemUtils;
import org.ThefryGuy.techFactory.machines.electric.ElectricFurnaceMachine;
import org.ThefryGuy.techFactory.machines.electric.ElectricGoldPanMachine;

/**
 * Unified listener for ALL electric machines
 * 
 * This is a DISPATCHER that delegates to individual machine handlers.
 * Each machine has its own handler in the machines/electric/ package for scalability.
 * 
 * BENEFITS:
 * - Adding a new electric machine = 1 line of code in TechFactory.onEnable()
 * - No need to create a new listener for each machine
 * - Consistent behavior across all machines
 * - Scales to 500+ machines easily
 * 
 * Registered Machines:
 * - Electric Furnace → ElectricFurnaceHandler
 * - (Future machines will be added here)
 */
public class ElectricMachineListener implements Listener {

    private final TechFactory plugin;

    public ElectricMachineListener(TechFactory plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle electric machine placement
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();

        // Check if this item is a registered electric machine
        String itemId = ItemUtils.getItemId(item);
        if (itemId == null) {
            return;
        }

        ElectricMachine machine = ElectricMachineRegistry.getMachine(itemId);
        if (machine == null) {
            return; // Not an electric machine
        }

        // Verify block material matches
        if (block.getType() != machine.getBlockMaterial()) {
            return;
        }

        // Dispatch to machine handler
        machine.handlePlacement(block, player, plugin);
    }

    /**
     * Handle electric machine interaction (right-click to open GUI)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();

        // Check if this is a placed electric machine
        ElectricMachine machine = ElectricMachineRegistry.getPlacedMachine(block, plugin);
        if (machine == null) {
            return; // Not an electric machine
        }

        // Cancel the default block interaction (e.g., furnace GUI)
        event.setCancelled(true);

        // Dispatch to machine handler
        machine.handleInteraction(block, player, plugin);
    }

    /**
     * Handle electric machine breaking
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Check if this is a placed electric machine
        ElectricMachine machine = ElectricMachineRegistry.getPlacedMachine(block, plugin);
        if (machine == null) {
            return; // Not an electric machine
        }

        // Cancel default drop
        event.setDropItems(false);

        // Dispatch to machine handler
        machine.handleBreak(block, player, plugin);
    }

    /**
     * Handle inventory close
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        if (inv.getSize() != 45) {
            return;
        }

        // Handle Electric Furnace
        if (title.equals(ChatColor.YELLOW + "Electric Furnace")) {
            Location furnaceLoc = ElectricFurnaceMachine.getViewingLocation(player);
            if (furnaceLoc != null) {
                ElectricFurnaceMachine.onClose(player, inv, plugin, furnaceLoc);
                ElectricFurnaceMachine.stopViewing(player);
            }
        }
        // Handle Electric Gold Pan
        else if (title.equals(ChatColor.GOLD + "Electric Gold Pan")) {
            Location machineLoc = ElectricGoldPanMachine.getViewingLocation(player);
            if (machineLoc != null) {
                ElectricGoldPanMachine.onClose(player, inv, plugin, machineLoc);
                ElectricGoldPanMachine.stopViewing(player);
            }
        }
    }

    /**
     * Prevent clicking on decorative items in the GUI and prevent placing items in output slots
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        if (inv.getSize() != 45) {
            return;
        }

        int slot = event.getRawSlot();

        // Allow clicking in player inventory (slots 45+)
        if (slot >= 45) {
            return;
        }

        // Check if this is an electric machine inventory and get the correct slots
        int[] inputSlots;
        int[] outputSlots;

        if (title.equals(ChatColor.YELLOW + "Electric Furnace")) {
            inputSlots = new int[]{10, 11, 19, 20};
            outputSlots = new int[]{14, 15, 23, 24};
        } else if (title.equals(ChatColor.GOLD + "Electric Gold Pan")) {
            inputSlots = new int[]{10, 11, 19, 20};
            outputSlots = new int[]{24, 25};
        } else {
            return; // Not an electric machine
        }

        boolean isInputSlot = false;
        boolean isOutputSlot = false;

        for (int inputSlot : inputSlots) {
            if (slot == inputSlot) {
                isInputSlot = true;
                break;
            }
        }

        for (int outputSlot : outputSlots) {
            if (slot == outputSlot) {
                isOutputSlot = true;
                break;
            }
        }

        // If clicking on a decorative slot (not input or output), cancel
        if (!isInputSlot && !isOutputSlot) {
            event.setCancelled(true);
            return;
        }

        // If clicking on output slot, only allow TAKING items (not placing)
        if (isOutputSlot) {
            ItemStack cursor = event.getCursor();
            ItemStack slotItem = event.getCurrentItem();

            // Prevent placing items in output slots
            // Allow: Taking items (cursor is empty), shift-clicking out
            // Deny: Placing items (cursor has item and slot is empty or different item)
            if (cursor != null && cursor.getType() != Material.AIR) {
                // Player is trying to place an item in output slot
                event.setCancelled(true);
                return;
            }
        }
    }
}

