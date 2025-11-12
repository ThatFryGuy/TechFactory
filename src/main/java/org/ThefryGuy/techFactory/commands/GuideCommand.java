package org.ThefryGuy.techFactory.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.ThefryGuy.techFactory.gui.GuideMenu;
import org.ThefryGuy.techFactory.gui.Menu;
import org.ThefryGuy.techFactory.gui.MenuManager;

public class GuideCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        // Check if player has a last menu - reopen it if they do
        Menu lastMenu = MenuManager.getLastMenu(player);
        if (lastMenu != null) {
            lastMenu.open();
        } else {
            // First time opening - show guide menu
            GuideMenu.open(player);
        }

        return true;
    }
}
