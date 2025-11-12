package org.ThefryGuy.techFactory.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConfig;

import java.util.logging.Level;

/**
 * /techfactory reload command
 * 
 * Reloads configuration from config.yml without restarting the server
 * 
 * Benefits:
 * - Instant balance changes (no downtime)
 * - Test different settings quickly
 * - No need to restart server at 3am
 * 
 * ADMIN ONLY: Requires permission techfactory.admin
 */
public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("techfactory.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            sender.sendMessage(ChatColor.GRAY + "Required permission: techfactory.admin");
            return true;
        }

        try {
            // Reload configuration
            TechFactoryConfig.reload();

            // Success message
            sender.sendMessage(ChatColor.GREEN + "✓ TechFactory configuration reloaded successfully!");
            sender.sendMessage(ChatColor.GRAY + "All settings from config.yml have been applied.");
            sender.sendMessage(ChatColor.YELLOW + "Note: Some changes may require restarting active operations.");

            return true;

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✗ Failed to reload configuration!");
            sender.sendMessage(ChatColor.GRAY + "Check console for details.");
            TechFactory.getInstance().getLogger()
                .log(Level.SEVERE, "Failed to reload config", e);
            return true;
        }
    }
}

