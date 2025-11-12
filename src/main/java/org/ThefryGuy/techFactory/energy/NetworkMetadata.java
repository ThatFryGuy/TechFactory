package org.ThefryGuy.techFactory.energy;

/**
 * CRITICAL FIX: Proper JSON metadata class with versioning
 * Prevents database desync and data corruption
 * 
 * Schema versioning allows for future changes without breaking old data
 */
public class NetworkMetadata {
    /**
     * Schema version - increment when adding/removing fields
     * Version 1: stored_energy, max_capacity
     */
    public int version = 1;
    
    /**
     * Current stored energy in Joules
     */
    public int stored_energy;
    
    /**
     * Maximum capacity in Joules
     */
    public int max_capacity;
    
    /**
     * Default constructor for Gson deserialization
     */
    public NetworkMetadata() {
    }
    
    /**
     * Constructor for creating new metadata
     */
    public NetworkMetadata(int storedEnergy, int maxCapacity) {
        this.stored_energy = storedEnergy;
        this.max_capacity = maxCapacity;
    }
    
    /**
     * Validate metadata values
     * @return true if valid, false if corrupted
     */
    public boolean isValid() {
        return version > 0 && 
               stored_energy >= 0 && 
               max_capacity > 0 && 
               stored_energy <= max_capacity;
    }
}

