package org.ThefryGuy.techFactory.energy;

import java.util.Arrays;
import java.util.List;

/**
 * Registry of all energy device types
 * Makes it easy to add new machines without modifying EnergyManager
 */
public class EnergyDeviceTypes {
    
    /**
     * Block types that consume energy (electric machines)
     * Add new electric machines here!
     */
    public static final List<String> ENERGY_CONSUMERS = Arrays.asList(
        "electric_furnace",
        "electric_gold_pan"
        // Add more electric machines here as you create them:
        // "electric_crusher",
        // "electric_compressor",
        // etc.
    );
    
    /**
     * Block types that generate energy (power generators)
     * Add new generators here!
     */
    public static final List<String> ENERGY_GENERATORS = Arrays.asList(
        "solar_generator"
        // Add more generators here as you create them:
        // "wind_turbine",
        // "coal_generator",
        // etc.
    );
    
    /**
     * Block types that extend network range
     * Add new connectors here!
     */
    public static final List<String> ENERGY_CONNECTORS = Arrays.asList(
        "energy_connector"
        // Add more connectors here as you create them:
        // "energy_capacitor",
        // "energy_cable",
        // etc.
    );
    
    /**
     * Check if a block type is an energy consumer
     */
    public static boolean isConsumer(String blockType) {
        return ENERGY_CONSUMERS.contains(blockType);
    }
    
    /**
     * Check if a block type is an energy generator
     */
    public static boolean isGenerator(String blockType) {
        return ENERGY_GENERATORS.contains(blockType);
    }
    
    /**
     * Check if a block type is an energy connector
     */
    public static boolean isConnector(String blockType) {
        return ENERGY_CONNECTORS.contains(blockType);
    }
    
    /**
     * Check if a block type is any energy device
     */
    public static boolean isEnergyDevice(String blockType) {
        return isConsumer(blockType) || isGenerator(blockType) || isConnector(blockType);
    }
}

