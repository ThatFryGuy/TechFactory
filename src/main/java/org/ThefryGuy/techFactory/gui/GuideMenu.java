package org.ThefryGuy.techFactory.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Main guide menu - opened with /techfactory guide
 */
public class GuideMenu extends Menu {
    
    public GuideMenu(Player player) {
        super(player, ChatColor.GOLD + "TechFactory Guide", 54); // Bigger menu for more categories
    }
    
    @Override
    protected void build() {
        clear();

        // Row 1: Main categories

        // Resources button
        ItemStack resources = new ItemStack(Material.CHEST);
        ItemMeta resourcesMeta = resources.getItemMeta();
        if (resourcesMeta != null) {
            resourcesMeta.setDisplayName(ChatColor.GOLD + "Resources");
            resourcesMeta.setLore(List.of(
                ChatColor.GRAY + "View all resources",
                ChatColor.GRAY + "Dusts, Alloys, Ingots, and more!",
                "",
                ChatColor.YELLOW + "Click to open"
            ));
            resources.setItemMeta(resourcesMeta);
        }
        setItem(10, resources);

        // Machines button
        ItemStack machines = new ItemStack(Material.FURNACE);
        ItemMeta machinesMeta = machines.getItemMeta();
        if (machinesMeta != null) {
            machinesMeta.setDisplayName(ChatColor.RED + "Machines");
            machinesMeta.setLore(List.of(
                ChatColor.GRAY + "View all machines",
                ChatColor.GRAY + "Electric Furnace and more!",
                "",
                ChatColor.YELLOW + "Click to open"
            ));
            machines.setItemMeta(machinesMeta);
        }
        setItem(11, machines);

        // Workstations button
        ItemStack workstations = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta workstationsMeta = workstations.getItemMeta();
        if (workstationsMeta != null) {
            workstationsMeta.setDisplayName(ChatColor.AQUA + "Workstations");
            workstationsMeta.setLore(List.of(
                ChatColor.GRAY + "View all workstations",
                ChatColor.GRAY + "Multiblocks & Advanced Multiblocks!",
                "",
                ChatColor.YELLOW + "Click to open"
            ));
            workstations.setItemMeta(workstationsMeta);
        }
        setItem(12, workstations);

        // Tools button
        ItemStack tools = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta toolsMeta = tools.getItemMeta();
        if (toolsMeta != null) {
            toolsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Tools");
            toolsMeta.setLore(List.of(
                ChatColor.GRAY + "View all tools",
                ChatColor.GRAY + "Gold Pan and more!",
                "",
                ChatColor.YELLOW + "Click to open"
            ));
            tools.setItemMeta(toolsMeta);
        }
        setItem(13, tools);

        // Energy button
        ItemStack energy = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta energyMeta = energy.getItemMeta();
        if (energyMeta != null) {
            energyMeta.setDisplayName(ChatColor.AQUA + "Energy");
            energyMeta.setLore(List.of(
                ChatColor.GRAY + "View all energy items",
                ChatColor.GRAY + "Energy Regulators, Solar Panels!",
                "",
                ChatColor.YELLOW + "Click to open"
            ));
            energy.setItemMeta(energyMeta);
        }
        setItem(14, energy);
    }

    @Override
    public void handleClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 10 -> { // Resources
                MenuManager.pushHistory(player, this);
                new ResourcesMenu(player).open();
            }
            case 11 -> { // Machines
                MenuManager.pushHistory(player, this);
                new MachinesMenu(player).open();
            }
            case 12 -> { // Workstations
                MenuManager.pushHistory(player, this);
                new WorkstationsMenu(player).open();
            }
            case 13 -> { // Tools
                MenuManager.pushHistory(player, this);
                new ToolsMenu(player).open();
            }
            case 14 -> { // Energy
                MenuManager.pushHistory(player, this);
                new EnergyMenu(player).open();
            }
        }
    }
    
    /**
     * Static helper to open the guide menu
     */
    public static void open(Player player) {
        new GuideMenu(player).open();
    }
}

