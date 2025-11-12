package org.ThefryGuy.techFactory.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represents a placed TechFactory block in the world
 * This is stored in the database for persistence across server restarts
 */
public class PlacedBlock {
    
    private int id;                    // Database ID (auto-increment)
    private String worldName;          // World name
    private int x;                     // Block X coordinate
    private int y;                     // Block Y coordinate
    private int z;                     // Block Z coordinate
    private String blockType;          // Type of block (e.g., "energy_regulator")
    private UUID ownerUUID;            // UUID of the player who placed it
    private String metadata;           // JSON metadata for block-specific data
    private long placedTimestamp;      // When the block was placed (Unix timestamp)
    
    /**
     * Constructor for creating a new PlacedBlock
     */
    public PlacedBlock(Location location, String blockType, UUID ownerUUID) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Invalid location: location or world is null");
        }

        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.blockType = blockType;
        this.ownerUUID = ownerUUID;
        this.metadata = "{}"; // Empty JSON object by default
        this.placedTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor for loading from database
     */
    public PlacedBlock(int id, String worldName, int x, int y, int z, String blockType, 
                       UUID ownerUUID, String metadata, long placedTimestamp) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
        this.ownerUUID = ownerUUID;
        this.metadata = metadata;
        this.placedTimestamp = placedTimestamp;
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
    
    public String getBlockType() {
        return blockType;
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public long getPlacedTimestamp() {
        return placedTimestamp;
    }
    
    /**
     * Get the Location object for this placed block
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

    /**
     * Parse a location key back into a Location object
     * Key format: "worldName,x,y,z"
     */
    public static Location keyToLocation(String locationKey) {
        if (locationKey == null || locationKey.isEmpty()) {
            return null;
        }

        String[] parts = locationKey.split(",");
        if (parts.length != 4) {
            return null;
        }

        try {
            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }

            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Setters
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "PlacedBlock{" +
                "id=" + id +
                ", type=" + blockType +
                ", location=" + worldName + "(" + x + "," + y + "," + z + ")" +
                ", owner=" + ownerUUID +
                '}';
    }
}

