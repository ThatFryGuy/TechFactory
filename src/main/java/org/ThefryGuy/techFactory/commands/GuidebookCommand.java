package org.ThefryGuy.techFactory.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command to give players the TechFactory Guidebook.
 * The guidebook is a written book that opens the guide GUI when right-clicked.
 * 
 * Similar to Slimefun's guidebook system.
 */
public class GuidebookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        // Create the guidebook
        ItemStack guidebook = createGuidebook();
        
        // Give to player
        player.getInventory().addItem(guidebook);
        
        // Success message
        player.sendMessage(ChatColor.GREEN + "✓ You received the " + ChatColor.GOLD + "TechFactory Guidebook" + ChatColor.GREEN + "!");
        player.sendMessage(ChatColor.GRAY + "Right-click the book to open the guide.");
        
        return true;
    }
    
    /**
     * Create the TechFactory Guidebook item.
     * This is a written book with NBT data to identify it.
     * 
     * @return The guidebook ItemStack
     */
    public static ItemStack createGuidebook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        
        if (meta != null) {
            // Set book metadata
            meta.setTitle(ChatColor.GOLD + "TechFactory Guide");
            meta.setAuthor(ChatColor.GRAY + "TechFactory");
            
            // Add a welcome page
            String page1 = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "TechFactory\n" +
                          ChatColor.BLACK + "" + ChatColor.RESET + "\n" +
                          ChatColor.DARK_GRAY + "Welcome to TechFactory!\n\n" +
                          ChatColor.BLACK + "This guidebook provides access to all recipes, machines, and resources.\n\n" +
                          ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Right-click" + ChatColor.RESET + ChatColor.BLACK + " this book to open the interactive guide menu!";
            
            meta.addPage(page1);
            
            // Add NBT data to identify this as a TechFactory guidebook
            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GuidebookCommand.class);
            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "techfactory_guidebook");
            
            book.setItemMeta(meta);
        }
        
        return book;
    }
    
    /**
     * Check if an ItemStack is a TechFactory Guidebook.
     * 
     * @param item The ItemStack to check
     * @return true if the item is a guidebook
     */
    public static boolean isGuidebook(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK || !item.hasItemMeta()) {
            return false;
        }
        
        BookMeta meta = (BookMeta) item.getItemMeta();
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GuidebookCommand.class);
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        
        String storedId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return storedId != null && storedId.equals("techfactory_guidebook");
    }
}

