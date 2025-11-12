package org.ThefryGuy.techFactory.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represents a placed multiblock structure in the world
 * This is stored in the database for persistence across server restarts
 */
public class MultiblockData {
    
    private int id;                    // Database ID (auto-increment)
    private String worldName;          // World name
    private int x;                     // Core block X coordinate
    private int y;                     // Core block Y coordinate
    private int z;                     // Core block Z coordinate
    private String multiblockType;     // Type of multiblock (e.g., "smelter", "basic_workbench")
    private UUID ownerUUID;            // UUID of the player who created it
    private String metadata;           // JSON metadata for multiblock-specific data
    private long createdTimestamp;     // When the multiblock was created (Unix timestamp)
    
    /**
     * Constructor for creating a new MultiblockData
     */
    public MultiblockData(Location location, String multiblockType, UUID ownerUUID) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Invalid location: location or world is null");
        }

        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.multiblockType = multiblockType;
        this.ownerUUID = ownerUUID;
        this.metadata = "{}"; // Empty JSON object by default
        this.createdTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor for loading from database
     */
    public MultiblockData(int id, String worldName, int x, int y, int z, String multiblockType, 
                         UUID ownerUUID, String metadata, long createdTimestamp) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.multiblockType = multiblockType;
        this.ownerUUID = ownerUUID;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
    }
    
    // Getters
    
    public int getId() {
        return id;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public String getMultiblockType() {
        return multiblockType;
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public long getCreatedTimestamp() {
        return createdTimestamp;
    }
    
    /**
     * Get the Location object for this multiblock
     */
    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }
    
    /**
     * Get a unique key for this location (world,x,y,z)
     */
    public String getLocationKey() {
        return worldName + "," + x + "," + y + "," + z;
    }
    
    /**
     * Create a location key from a Location object
     */
    public static String locationToKey(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Invalid location: location or world is null");
        }

        return location.getWorld().getName() + "," +
               location.getBlockX() + "," +
               location.getBlockY() + "," +
               location.getBlockZ();
    }
    
    // Setters

    public void setId(int id) {
        this.id = id;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setMultiblockType(String multiblockType) {
        this.multiblockType = multiblockType;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    
    @Override
    public String toString() {
        return "MultiblockData{" +
                "id=" + id +
                ", type=" + multiblockType +
                ", location=" + worldName + "(" + x + "," + y + "," + z + ")" +
                ", owner=" + ownerUUID +
                '}';
    }
}

