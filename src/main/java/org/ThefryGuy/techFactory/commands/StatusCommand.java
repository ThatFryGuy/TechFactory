package org.ThefryGuy.techFactory.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;

/**
 * /techfactory status command
 * 
 * Shows server health and performance statistics
 * - Active smelting operations
 * - Energy networks
 * - Database size
 * - Memory usage
 * - Cache statistics
 * 
 * ADMIN ONLY: Requires permission techfactory.admin
 */
public class StatusCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("techfactory.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        TechFactory plugin = TechFactory.getInstance();
        
        // Gather statistics
        int activeSmelts = plugin.getSmeltingManager().getActiveCount();
        int energyNetworks = plugin.getEnergyManager().getNetworkCount();
        
        long dbSizeBytes = plugin.getDatabaseManager().getDatabaseSizeBytes();
        double dbSizeMB = dbSizeBytes / 1024.0 / 1024.0;
        
        int totalBlocks = plugin.getDatabaseManager().getTotalBlockCount();
        int totalMultiblocks = plugin.getDatabaseManager().getTotalMultiblockCount();
        int cachedBlocks = plugin.getDatabaseManager().getCachedBlockCount();
        int cachedMultiblocks = plugin.getDatabaseManager().getCachedMultiblockCount();
        
        Runtime runtime = Runtime.getRuntime();
        long memUsed = runtime.totalMemory() - runtime.freeMemory();
        long memMax = runtime.maxMemory();
        double memUsedMB = memUsed / 1024.0 / 1024.0;
        double memMaxMB = memMax / 1024.0 / 1024.0;
        double memPercent = (memUsed * 100.0) / memMax;
        
        String cacheStats = plugin.getMultiblockCache().getStats();
        
        // Calculate TPS (approximate)
        double tps = getTPS();
        ChatColor tpsColor = tps >= 19.5 ? ChatColor.GREEN : tps >= 15 ? ChatColor.YELLOW : ChatColor.RED;
        
        // Send formatted status
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.YELLOW + "TechFactory Status" + ChatColor.GOLD + " ==========");
        sender.sendMessage("");
        
        // Server Performance
        sender.sendMessage(ChatColor.AQUA + "Server Performance:");
        sender.sendMessage("  " + ChatColor.GRAY + "TPS: " + tpsColor + String.format("%.2f", tps) + ChatColor.GRAY + " / 20.0");
        sender.sendMessage("  " + ChatColor.GRAY + "Memory: " + ChatColor.WHITE + String.format("%.1f MB", memUsedMB) + 
                          ChatColor.GRAY + " / " + ChatColor.WHITE + String.format("%.1f MB", memMaxMB) + 
                          ChatColor.GRAY + " (" + String.format("%.1f%%", memPercent) + ")");
        sender.sendMessage("");
        
        // Active Operations
        sender.sendMessage(ChatColor.AQUA + "Active Operations:");
        sender.sendMessage("  " + ChatColor.GRAY + "Smelting: " + ChatColor.WHITE + activeSmelts);
        sender.sendMessage("  " + ChatColor.GRAY + "Energy Networks: " + ChatColor.WHITE + energyNetworks);
        sender.sendMessage("");
        
        // Database Statistics
        sender.sendMessage(ChatColor.AQUA + "Database:");
        sender.sendMessage("  " + ChatColor.GRAY + "Size: " + ChatColor.WHITE + String.format("%.2f MB", dbSizeMB));
        sender.sendMessage("  " + ChatColor.GRAY + "Total Blocks: " + ChatColor.WHITE + totalBlocks);
        sender.sendMessage("  " + ChatColor.GRAY + "Total Multiblocks: " + ChatColor.WHITE + totalMultiblocks);
        sender.sendMessage("  " + ChatColor.GRAY + "Cached Blocks: " + ChatColor.WHITE + cachedBlocks + ChatColor.DARK_GRAY + " (loaded chunks)");
        sender.sendMessage("  " + ChatColor.GRAY + "Cached Multiblocks: " + ChatColor.WHITE + cachedMultiblocks + ChatColor.DARK_GRAY + " (loaded chunks)");
        sender.sendMessage("");
        
        // Cache Performance
        sender.sendMessage(ChatColor.AQUA + "Cache Performance:");
        sender.sendMessage("  " + ChatColor.GRAY + cacheStats);
        sender.sendMessage("");
        
        // Health Warnings
        boolean hasWarnings = false;
        if (tps < 19.0) {
            sender.sendMessage(ChatColor.RED + "⚠ WARNING: Low TPS detected!");
            hasWarnings = true;
        }
        if (memPercent > 90) {
            sender.sendMessage(ChatColor.RED + "⚠ WARNING: High memory usage!");
            hasWarnings = true;
        }
        if (activeSmelts > 1000) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ NOTICE: High number of active smelts");
            hasWarnings = true;
        }
        if (dbSizeMB > 100) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ NOTICE: Large database size (consider cleanup)");
            hasWarnings = true;
        }
        
        if (!hasWarnings) {
            sender.sendMessage(ChatColor.GREEN + "✓ All systems operating normally");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "=========================================");
        sender.sendMessage("");
        
        return true;
    }
    
    /**
     * Get approximate TPS
     * This is a simplified version - for production use a proper TPS tracker
     */
    private double getTPS() {
        try {
            // Try to get TPS from server (Spigot/Paper API)
            Object server = org.bukkit.Bukkit.getServer();
            java.lang.reflect.Method getTPS = server.getClass().getMethod("getTPS");
            double[] tpsArray = (double[]) getTPS.invoke(server);
            return tpsArray[0]; // 1-minute average
        } catch (Exception e) {
            // Fallback: assume 20 TPS if we can't get it
            return 20.0;
        }
    }
}

