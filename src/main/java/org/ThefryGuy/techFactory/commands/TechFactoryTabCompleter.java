package org.ThefryGuy.techFactory.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion for /techfactory command
 */
public class TechFactoryTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("guide", "guidebook", "give", "status", "metrics", "reload", "queue", "help");
    private static final List<String> QUEUE_SUBCOMMANDS = Arrays.asList("add", "remove", "clear", "view", "list");
    private static final List<String> AMOUNTS = Arrays.asList("1", "8", "16", "32", "64");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // First argument: subcommands
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());
        }

        // Handle "give" subcommand
        else if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {

            // Second argument: player names
            if (args.length == 2) {
                String input = args[1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }

            // Third argument: item IDs
            else if (args.length == 3) {
                String input = args[2].toLowerCase();
                completions = ItemRegistry.getAllItemIds().stream()
                        .filter(id -> id.startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
            }

            // Fourth argument: amount
            else if (args.length == 4) {
                String input = args[3];
                completions = AMOUNTS.stream()
                        .filter(amount -> amount.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        // Handle "queue" subcommand
        else if (args.length >= 2 && args[0].equalsIgnoreCase("queue")) {

            // Second argument: queue subcommands
            if (args.length == 2) {
                String input = args[1].toLowerCase();
                completions = QUEUE_SUBCOMMANDS.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }

            // Third argument for "add": recipe IDs
            else if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
                String input = args[2].toLowerCase();
                completions = ItemRegistry.getAllItemIds().stream()
                        .filter(id -> id.startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
            }

            // Third argument for "remove": position numbers
            else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
                completions = Arrays.asList("1", "2", "3", "4", "5");
            }
        }

        return completions;
    }
}

