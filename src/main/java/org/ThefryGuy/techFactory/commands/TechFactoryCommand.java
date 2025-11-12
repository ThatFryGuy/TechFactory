package org.ThefryGuy.techFactory.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TechFactoryCommand implements CommandExecutor {

    private GuideCommand guideCommand; // Reference to guide command
    private GuidebookCommand guidebookCommand; // Reference to guidebook command
    private GiveCommand giveCommand;   // Reference to give command
    private StatusCommand statusCommand; // Reference to status command
    private ReloadCommand reloadCommand; // Reference to reload command
    private QueueCommand queueCommand; // Reference to queue command
    private MetricsCommand metricsCommand; // Reference to metrics command

    // Setter to inject GuideCommand
    public void setGuideCommand(GuideCommand guideCommand) {
        this.guideCommand = guideCommand;
    }

    // Setter to inject GuidebookCommand
    public void setGuidebookCommand(GuidebookCommand guidebookCommand) {
        this.guidebookCommand = guidebookCommand;
    }

    // Setter to inject GiveCommand
    public void setGiveCommand(GiveCommand giveCommand) {
        this.giveCommand = giveCommand;
    }

    // Setter to inject StatusCommand
    public void setStatusCommand(StatusCommand statusCommand) {
        this.statusCommand = statusCommand;
    }

    // Setter to inject ReloadCommand
    public void setReloadCommand(ReloadCommand reloadCommand) {
        this.reloadCommand = reloadCommand;
    }

    // Setter to inject QueueCommand
    public void setQueueCommand(QueueCommand queueCommand) {
        this.queueCommand = queueCommand;
    }

    // Setter to inject MetricsCommand
    public void setMetricsCommand(MetricsCommand metricsCommand) {
        this.metricsCommand = metricsCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments, send welcome message
        if (args.length == 0) {
            if (sender instanceof Player player) {
                sendWelcomeMessage(player);
            } else {
                sender.sendMessage(ChatColor.GREEN + "TechFactory plugin is running!");
                sender.sendMessage(ChatColor.GRAY + "Use /techfactory status for server statistics");
            }
            return true;
        }

        // Subcommand: guide (player only)
        if (args[0].equalsIgnoreCase("guide")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (guideCommand != null) {
                guideCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Guide command is not set up!");
            }
            return true;
        }

        // Subcommand: guidebook (player only)
        if (args[0].equalsIgnoreCase("guidebook")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (guidebookCommand != null) {
                guidebookCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Guidebook command is not set up!");
            }
            return true;
        }

        // Subcommand: give
        if (args[0].equalsIgnoreCase("give")) {
            if (giveCommand != null) {
                giveCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Give command is not set up!");
            }
            return true;
        }

        // Subcommand: status (admin only)
        if (args[0].equalsIgnoreCase("status")) {
            if (statusCommand != null) {
                statusCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Status command is not set up!");
            }
            return true;
        }

        // Subcommand: reload (admin only)
        if (args[0].equalsIgnoreCase("reload")) {
            if (reloadCommand != null) {
                reloadCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Reload command is not set up!");
            }
            return true;
        }

        // Subcommand: queue (player only)
        if (args[0].equalsIgnoreCase("queue")) {
            if (queueCommand != null) {
                queueCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Queue command is not set up!");
            }
            return true;
        }

        // Subcommand: metrics (admin only)
        if (args[0].equalsIgnoreCase("metrics")) {
            if (metricsCommand != null) {
                metricsCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Metrics command is not set up!");
            }
            return true;
        }

        // Subcommand: help
        if (args[0].equalsIgnoreCase("help")) {
            if (sender instanceof Player player) {
                sendHelpMessage(player);
            } else {
                sendConsoleHelpMessage(sender);
            }
            return true;
        }

        // Unknown subcommand
        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try /techfactory help");
        return true;
    }

    private void sendWelcomeMessage(Player player) {
        player.sendMessage(ChatColor.GREEN + "📘 Welcome to TechFactory!");
        player.sendMessage(ChatColor.GRAY + "This plugin adds new automation, machines, and advanced crafting systems.");
        player.sendMessage(ChatColor.GRAY + "Type /techfactory help for more info.");
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "╔═══════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.BOLD + "TechFactory Commands" + ChatColor.GOLD + "      ║");
        player.sendMessage(ChatColor.GOLD + "╚═══════════════════════════════╝");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "/techfactory guide");
        player.sendMessage(ChatColor.GRAY + "  Open the TechFactory guide menu");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "/techfactory guidebook");
        player.sendMessage(ChatColor.GRAY + "  Get a TechFactory guidebook (right-click to open guide)");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "/techfactory give <player> <item> [amount]");
        player.sendMessage(ChatColor.GRAY + "  Give TechFactory items to a player");
        player.sendMessage(ChatColor.GRAY + "  Example: /tf give Steve iron_dust 64");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "/techfactory queue <add|remove|clear|view>");
        player.sendMessage(ChatColor.GRAY + "  Manage smelter recipe queues");
        player.sendMessage(ChatColor.GRAY + "  Example: /tf queue add bronze_ingot");
        player.sendMessage("");

        if (player.hasPermission("techfactory.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/techfactory status");
            player.sendMessage(ChatColor.GRAY + "  View server performance and statistics");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "/techfactory metrics");
            player.sendMessage(ChatColor.GRAY + "  View detailed performance metrics");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "/techfactory reload");
            player.sendMessage(ChatColor.GRAY + "  Reload configuration from config.yml");
            player.sendMessage("");
        }

        player.sendMessage(ChatColor.YELLOW + "/techfactory help");
        player.sendMessage(ChatColor.GRAY + "  Show this help message");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "Aliases: /tf, /tfactory");
    }

    private void sendConsoleHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== TechFactory Commands ==========");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/techfactory give <player> <item> [amount]");
        sender.sendMessage(ChatColor.GRAY + "  Give TechFactory items to a player");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/techfactory status");
        sender.sendMessage(ChatColor.GRAY + "  View server performance and statistics");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/techfactory metrics");
        sender.sendMessage(ChatColor.GRAY + "  View detailed performance metrics");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/techfactory reload");
        sender.sendMessage(ChatColor.GRAY + "  Reload configuration from config.yml");
        sender.sendMessage("");
    }
}
