package org.ThefryGuy.techFactory.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.ThefryGuy.techFactory.data.PerformanceMetrics;

/**
 * /techfactory metrics command
 * 
 * Shows detailed performance metrics for TechFactory:
 * - Batch operation performance (flush times, queue sizes)
 * - Energy update rates (current, peak, total)
 * - Database operation stats (errors, retries)
 * - Cache performance (hit rate, sizes)
 * 
 * ADMIN ONLY: Requires permission techfactory.admin
 * 
 * This command is useful for:
 * - Diagnosing performance issues
 * - Monitoring system health in production
 * - Tuning configuration parameters
 * - Identifying bottlenecks
 */
public class MetricsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("techfactory.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        PerformanceMetrics metrics = PerformanceMetrics.getInstance();
        
        // ========================================
        // HEADER
        // ========================================
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.YELLOW + "TechFactory Performance Metrics" + ChatColor.GOLD + " ==========");
        sender.sendMessage("");
        
        // ========================================
        // UPTIME
        // ========================================
        long uptimeSeconds = metrics.getUptimeSeconds();
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        sender.sendMessage(ChatColor.AQUA + "Uptime: " + ChatColor.WHITE + 
                          String.format("%dh %dm %ds", hours, minutes, seconds));
        sender.sendMessage("");
        
        // ========================================
        // BATCH OPERATION METRICS
        // ========================================
        sender.sendMessage(ChatColor.AQUA + "Batch Operations:");
        
        long totalFlushes = metrics.getTotalBatchFlushes();
        long lastFlushMs = metrics.getLastBatchFlushTimeMs();
        long avgFlushMs = metrics.getAvgBatchFlushTimeMs();
        long maxFlushMs = metrics.getMaxBatchFlushTimeMs();
        long slowFlushes = metrics.getSlowBatchFlushCount();
        
        sender.sendMessage("  " + ChatColor.GRAY + "Total Flushes: " + ChatColor.WHITE + totalFlushes);
        sender.sendMessage("  " + ChatColor.GRAY + "Last Flush: " + ChatColor.WHITE + lastFlushMs + "ms");
        sender.sendMessage("  " + ChatColor.GRAY + "Avg Flush: " + ChatColor.WHITE + avgFlushMs + "ms");
        sender.sendMessage("  " + ChatColor.GRAY + "Max Flush: " + ChatColor.WHITE + maxFlushMs + "ms");
        
        if (slowFlushes > 0) {
            sender.sendMessage("  " + ChatColor.YELLOW + "Slow Flushes (>1s): " + ChatColor.WHITE + slowFlushes);
        } else {
            sender.sendMessage("  " + ChatColor.GREEN + "Slow Flushes (>1s): " + ChatColor.WHITE + "0 ✓");
        }
        
        sender.sendMessage("");
        
        // ========================================
        // QUEUE SIZES
        // ========================================
        sender.sendMessage(ChatColor.AQUA + "Pending Queues:");
        
        int pendingSaves = metrics.getPendingSavesCount();
        int pendingDeletes = metrics.getPendingDeletesCount();
        int pendingEnergyUpdates = metrics.getPendingEnergyUpdatesCount();
        int totalPending = pendingSaves + pendingDeletes + pendingEnergyUpdates;
        
        sender.sendMessage("  " + ChatColor.GRAY + "Saves: " + ChatColor.WHITE + pendingSaves);
        sender.sendMessage("  " + ChatColor.GRAY + "Deletes: " + ChatColor.WHITE + pendingDeletes);
        sender.sendMessage("  " + ChatColor.GRAY + "Energy Updates: " + ChatColor.WHITE + pendingEnergyUpdates);
        sender.sendMessage("  " + ChatColor.GRAY + "Total Pending: " + ChatColor.WHITE + totalPending);
        
        if (totalPending > 1000) {
            sender.sendMessage("  " + ChatColor.YELLOW + "⚠ High queue size - may indicate database bottleneck");
        }
        
        sender.sendMessage("");
        
        // ========================================
        // ENERGY UPDATE METRICS
        // ========================================
        sender.sendMessage(ChatColor.AQUA + "Energy Updates:");
        
        int currentRate = metrics.getEnergyUpdatesThisSecond();
        int peakRate = metrics.getPeakEnergyUpdatesPerSecond();
        long totalUpdates = metrics.getTotalEnergyUpdates();
        
        sender.sendMessage("  " + ChatColor.GRAY + "Current Rate: " + ChatColor.WHITE + currentRate + "/s");
        sender.sendMessage("  " + ChatColor.GRAY + "Peak Rate: " + ChatColor.WHITE + peakRate + "/s");
        sender.sendMessage("  " + ChatColor.GRAY + "Total Updates: " + ChatColor.WHITE + totalUpdates);
        
        // Calculate average rate
        if (uptimeSeconds > 0) {
            double avgRate = totalUpdates / (double) uptimeSeconds;
            sender.sendMessage("  " + ChatColor.GRAY + "Avg Rate: " + ChatColor.WHITE + String.format("%.1f/s", avgRate));
        }
        
        sender.sendMessage("");
        
        // ========================================
        // DATABASE OPERATION METRICS
        // ========================================
        sender.sendMessage(ChatColor.AQUA + "Database Operations:");
        
        long totalErrors = metrics.getTotalDatabaseErrors();
        long totalRetries = metrics.getTotalDatabaseRetries();
        
        if (totalErrors > 0) {
            sender.sendMessage("  " + ChatColor.RED + "Errors: " + ChatColor.WHITE + totalErrors);
        } else {
            sender.sendMessage("  " + ChatColor.GREEN + "Errors: " + ChatColor.WHITE + "0 ✓");
        }
        
        if (totalRetries > 0) {
            sender.sendMessage("  " + ChatColor.YELLOW + "Retries: " + ChatColor.WHITE + totalRetries);
        } else {
            sender.sendMessage("  " + ChatColor.GREEN + "Retries: " + ChatColor.WHITE + "0 ✓");
        }
        
        // Calculate retry rate
        if (totalFlushes > 0) {
            double retryRate = (totalRetries * 100.0) / totalFlushes;
            if (retryRate > 5.0) {
                sender.sendMessage("  " + ChatColor.YELLOW + "Retry Rate: " + ChatColor.WHITE + String.format("%.1f%%", retryRate));
            } else {
                sender.sendMessage("  " + ChatColor.GREEN + "Retry Rate: " + ChatColor.WHITE + String.format("%.1f%%", retryRate));
            }
        }
        
        sender.sendMessage("");
        
        // ========================================
        // CACHE METRICS
        // ========================================
        sender.sendMessage(ChatColor.AQUA + "Cache Performance:");
        
        int blockCacheSize = metrics.getBlockCacheSize();
        int multiblockCacheSize = metrics.getMultiblockCacheSize();
        long cacheHits = metrics.getCacheHits();
        long cacheMisses = metrics.getCacheMisses();
        double hitRate = metrics.getCacheHitRate();
        
        sender.sendMessage("  " + ChatColor.GRAY + "Block Cache: " + ChatColor.WHITE + blockCacheSize + " entries");
        sender.sendMessage("  " + ChatColor.GRAY + "Multiblock Cache: " + ChatColor.WHITE + multiblockCacheSize + " entries");
        sender.sendMessage("  " + ChatColor.GRAY + "Cache Hits: " + ChatColor.WHITE + cacheHits);
        sender.sendMessage("  " + ChatColor.GRAY + "Cache Misses: " + ChatColor.WHITE + cacheMisses);
        
        // Color-code hit rate
        ChatColor hitRateColor;
        if (hitRate >= 95.0) {
            hitRateColor = ChatColor.GREEN;
        } else if (hitRate >= 80.0) {
            hitRateColor = ChatColor.YELLOW;
        } else {
            hitRateColor = ChatColor.RED;
        }
        
        sender.sendMessage("  " + ChatColor.GRAY + "Hit Rate: " + hitRateColor + String.format("%.1f%%", hitRate));
        
        sender.sendMessage("");
        
        // ========================================
        // HEALTH WARNINGS
        // ========================================
        boolean hasWarnings = false;
        
        if (avgFlushMs > 500) {
            sender.sendMessage(ChatColor.RED + "⚠ WARNING: High average flush time (>500ms)");
            sender.sendMessage(ChatColor.GRAY + "  Consider reducing batch size or optimizing database");
            hasWarnings = true;
        }
        
        if (totalPending > 5000) {
            sender.sendMessage(ChatColor.RED + "⚠ WARNING: Very high queue size (>5000)");
            sender.sendMessage(ChatColor.GRAY + "  Database may be overloaded");
            hasWarnings = true;
        }
        
        if (totalErrors > 100) {
            sender.sendMessage(ChatColor.RED + "⚠ WARNING: High error count (>100)");
            sender.sendMessage(ChatColor.GRAY + "  Check server logs for database issues");
            hasWarnings = true;
        }
        
        if (hitRate < 80.0 && (cacheHits + cacheMisses) > 1000) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ NOTICE: Low cache hit rate (<80%)");
            sender.sendMessage(ChatColor.GRAY + "  Consider increasing cache size");
            hasWarnings = true;
        }
        
        if (!hasWarnings) {
            sender.sendMessage(ChatColor.GREEN + "✓ All metrics within normal ranges");
        }
        
        sender.sendMessage("");
        
        // ========================================
        // FOOTER
        // ========================================
        sender.sendMessage(ChatColor.GOLD + "=======================================================");
        sender.sendMessage("");
        
        return true;
    }
}

