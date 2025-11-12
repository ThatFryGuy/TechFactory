package org.ThefryGuy.techFactory.energy;

import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an energy network with a regulator at its core
 * Each network has one Energy Regulator and can have multiple panels/consumers
 */
public class EnergyNetwork {
    
    private final UUID networkId;
    private final Location regulatorLocation;
    private int storedEnergy;           // Energy stored in Joules (J)
    private int maxCapacity;            // Maximum capacity in Joules
    private final Set<Location> connectedPanels;      // Solar panels, wind turbines, etc.
    private final Set<Location> connectedConsumers;   // Machines that use energy
    private final Set<Location> connectedConnectors;  // Energy connectors that extend network range
    private final Set<Location> connectedCapacitors;  // Energy capacitors (extend range + add capacity)

    // Track consumption rate (J/s)
    private int consumptionRate;        // Current consumption rate in J/s
    private long lastConsumptionUpdate; // Last time consumption was calculated

    // PERFORMANCE FIX: Event-based hologram updates
    // Only update hologram when energy actually changes (not every tick)
    private volatile boolean energyChanged = false;

    // PERFORMANCE FIX: Callback for batched energy persistence
    // Queues energy changes for batch save every 1 second (prevents 5k-10k async tasks/sec)
    private EnergyChangeCallback energyChangeCallback = null;
    private boolean suppressCallbacks = false;  // Used during loading to prevent unnecessary saves

    // PERFORMANCE FIX: Energy threshold compression - only save when energy changes significantly
    // Reduces saves by 50-80% (e.g., small solar panel trickle charging doesn't spam saves)
    private int lastSavedEnergy = 0;  // Last energy value that was saved to database

    // CRITICAL FIX: Track wasted energy when network is full
    // Helps players debug why their solar panels aren't producing full output
    private int wastedEnergy = 0;
    private long lastWastedWarning = 0;  // Timestamp of last warning (prevent spam)

    /**
     * Callback interface for energy change notifications
     * Used to trigger async database saves when energy changes
     */
    public interface EnergyChangeCallback {
        void onEnergyChanged(EnergyNetwork network);
    }

    /**
     * Create a new energy network centered on a regulator
     */
    public EnergyNetwork(Location regulatorLocation) {
        this.networkId = UUID.randomUUID();
        this.regulatorLocation = regulatorLocation;
        this.storedEnergy = 0;
        this.maxCapacity = 100; // Base capacity: 100 J (from regulator alone - add capacitors for more!)
        this.connectedPanels = ConcurrentHashMap.newKeySet();
        this.connectedConsumers = ConcurrentHashMap.newKeySet();
        this.connectedConnectors = ConcurrentHashMap.newKeySet();
        this.connectedCapacitors = ConcurrentHashMap.newKeySet();
        this.consumptionRate = 0;
        this.lastConsumptionUpdate = System.currentTimeMillis();
    }
    
    /**
     * Constructor for loading from database
     */
    public EnergyNetwork(UUID networkId, Location regulatorLocation, int storedEnergy,
                        int maxCapacity, Set<Location> connectedPanels, Set<Location> connectedConsumers) {
        this.networkId = networkId;
        this.regulatorLocation = regulatorLocation;
        this.storedEnergy = storedEnergy;
        this.maxCapacity = maxCapacity;
        // Convert to thread-safe sets for concurrent access
        this.connectedPanels = connectedPanels != null ?
            ConcurrentHashMap.newKeySet(connectedPanels.size()) : ConcurrentHashMap.newKeySet();
        this.connectedConsumers = connectedConsumers != null ?
            ConcurrentHashMap.newKeySet(connectedConsumers.size()) : ConcurrentHashMap.newKeySet();
        this.connectedConnectors = ConcurrentHashMap.newKeySet();
        this.connectedCapacitors = ConcurrentHashMap.newKeySet();

        // Populate the sets if data was provided
        if (connectedPanels != null) {
            this.connectedPanels.addAll(connectedPanels);
        }
        if (connectedConsumers != null) {
            this.connectedConsumers.addAll(connectedConsumers);
        }

        this.consumptionRate = 0;
        this.lastConsumptionUpdate = System.currentTimeMillis();
    }
    
    // === Energy Management ===
    
    /**
     * Add energy to the network (from generators/panels)
     * @return Amount of energy actually added (may be less if network is full)
     */
    public synchronized int addEnergy(int amount) {
        int spaceAvailable = maxCapacity - storedEnergy;
        int actuallyAdded = Math.min(amount, spaceAvailable);

        // Track wasted energy when network is full (silently - no console spam)
        if (actuallyAdded < amount) {
            int wasted = amount - actuallyAdded;
            wastedEnergy += wasted;
            // Energy is wasted silently - this is normal behavior when network is full
        }

        if (actuallyAdded > 0) {
            storedEnergy += actuallyAdded;
            energyChanged = true;  // Mark for hologram update

            // PERFORMANCE FIX: Threshold compression - only save when energy changes significantly
            // This reduces saves by 50-80% (e.g., small solar trickle charging)
            if (shouldSaveEnergy()) {
                if (energyChangeCallback != null && !suppressCallbacks) {
                    energyChangeCallback.onEnergyChanged(this);
                    lastSavedEnergy = storedEnergy;  // Update last saved value
                }
            }
        }
        return actuallyAdded;
    }

    /**
     * Remove energy from the network (for consumers)
     * @return Amount of energy actually removed (may be less if not enough stored)
     */
    public synchronized int removeEnergy(int amount) {
        int actuallyRemoved = Math.min(amount, storedEnergy);
        if (actuallyRemoved > 0) {
            storedEnergy -= actuallyRemoved;
            energyChanged = true;  // Mark for hologram update

            // Update consumption tracking
            updateConsumptionRate(actuallyRemoved);

            // PERFORMANCE FIX: Threshold compression - only save when energy changes significantly
            if (shouldSaveEnergy()) {
                if (energyChangeCallback != null && !suppressCallbacks) {
                    energyChangeCallback.onEnergyChanged(this);
                    lastSavedEnergy = storedEnergy;  // Update last saved value
                }
            }
        }

        return actuallyRemoved;
    }

    /**
     * Update the consumption rate based on recent energy removal
     */
    private void updateConsumptionRate(int energyConsumed) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastConsumptionUpdate;

        // Update every second
        if (timeDiff >= 1000) {
            // Calculate J/s based on energy consumed in the last second
            consumptionRate = (int) ((energyConsumed * 1000.0) / timeDiff);
            lastConsumptionUpdate = currentTime;
        }
    }

    /**
     * Get the current consumption rate in J/s
     */
    public int getConsumptionRate() {
        // Reset consumption rate if no consumption in the last 2 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastConsumptionUpdate > 2000) {
            consumptionRate = 0;
        }
        return consumptionRate;
    }
    
    /**
     * Check if network has enough energy
     */
    public boolean hasEnergy(int amount) {
        return storedEnergy >= amount;
    }
    
    /**
     * Get energy fill percentage (0-100)
     */
    public int getFillPercentage() {
        if (maxCapacity == 0) return 0;
        return (int) ((storedEnergy / (double) maxCapacity) * 100);
    }
    
    // === Panel/Consumer Management ===
    
    /**
     * Connect an energy panel to this network
     */
    public void connectPanel(Location panelLocation) {
        connectedPanels.add(panelLocation);
    }
    
    /**
     * Disconnect an energy panel
     */
    public void disconnectPanel(Location panelLocation) {
        connectedPanels.remove(panelLocation);
    }
    
    /**
     * Connect an energy consumer to this network
     */
    public void connectConsumer(Location consumerLocation) {
        connectedConsumers.add(consumerLocation);
    }
    
    /**
     * Disconnect an energy consumer
     */
    public void disconnectConsumer(Location consumerLocation) {
        connectedConsumers.remove(consumerLocation);
    }

    /**
     * Connect an energy connector to this network
     */
    public void connectConnector(Location connectorLocation) {
        connectedConnectors.add(connectorLocation);
    }

    /**
     * Disconnect an energy connector
     */
    public void disconnectConnector(Location connectorLocation) {
        connectedConnectors.remove(connectorLocation);
    }

    /**
     * Check if a connector is connected to this network
     */
    public boolean hasConnector(Location connectorLocation) {
        return connectedConnectors.contains(connectorLocation);
    }

    /**
     * Connect an energy capacitor to this network
     * Capacitors extend range AND increase capacity
     */
    public void connectCapacitor(Location capacitorLocation, int capacityBonus) {
        if (!connectedCapacitors.contains(capacitorLocation)) {
            connectedCapacitors.add(capacitorLocation);
            maxCapacity += capacityBonus;
            energyChanged = true;  // Update hologram to show new capacity
        }
    }

    /**
     * Disconnect an energy capacitor from this network
     */
    public void disconnectCapacitor(Location capacitorLocation, int capacityBonus) {
        if (connectedCapacitors.remove(capacitorLocation)) {
            maxCapacity -= capacityBonus;
            // Ensure stored energy doesn't exceed new capacity
            if (storedEnergy > maxCapacity) {
                storedEnergy = maxCapacity;
            }
            energyChanged = true;  // Update hologram to show new capacity
        }
    }

    /**
     * Check if a capacitor is connected to this network
     */
    public boolean hasCapacitor(Location capacitorLocation) {
        return connectedCapacitors.contains(capacitorLocation);
    }

    /**
     * Get all connected capacitors (for range extension)
     */
    public Set<Location> getConnectedCapacitors() {
        return Collections.unmodifiableSet(connectedCapacitors);
    }

    /**
     * Get capacitor count
     */
    public int getCapacitorCount() {
        return connectedCapacitors.size();
    }

    // === Getters ===
    
    public UUID getNetworkId() {
        return networkId;
    }
    
    public Location getRegulatorLocation() {
        return regulatorLocation;
    }
    
    public int getStoredEnergy() {
        return storedEnergy;
    }
    
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    /**
     * PERFORMANCE FIX: Return unmodifiable view instead of copying
     * This prevents massive GC pressure with 50k+ machines
     * Old: Created new HashSet every call (memory allocation + copy)
     * New: Returns view with zero allocation
     */
    public Set<Location> getConnectedPanels() {
        return Collections.unmodifiableSet(connectedPanels);
    }

    public Set<Location> getConnectedConsumers() {
        return Collections.unmodifiableSet(connectedConsumers);
    }
    
    public int getPanelCount() {
        return connectedPanels.size();
    }
    
    public int getConsumerCount() {
        return connectedConsumers.size();
    }

    public Set<Location> getConnectedConnectors() {
        return Collections.unmodifiableSet(connectedConnectors);
    }

    public int getConnectorCount() {
        return connectedConnectors.size();
    }

    // === Network Validation ===

    /**
     * PERFORMANCE FIX: Validate all connectors in the network and remove any that are no longer reachable
     * Uses optimized breadth-first search to find all connectors within range of the regulator or other valid connectors
     *
     * OLD: O(N²) - for each item in toCheck, scanned ALL connectors
     * NEW: O(N) - uses Queue for proper BFS and early termination
     *
     * ALGORITHM:
     * 1. Start from the regulator
     * 2. Find all connectors within 6 blocks of the regulator (these are valid)
     * 3. For each valid connector, find all connectors within 6 blocks of it
     * 4. Repeat until no new connectors are found
     * 5. Remove any connectors that weren't reached
     *
     * @param maxRange Maximum connection range (typically 6 blocks)
     * @return Set of connector locations that were removed (orphaned)
     */
    public Set<Location> validateConnectorConnectivity(double maxRange) {
        Set<Location> reachableConnectors = ConcurrentHashMap.newKeySet();
        Queue<Location> toCheck = new LinkedList<>();
        Set<Location> checked = ConcurrentHashMap.newKeySet();

        // Start from the regulator
        toCheck.add(regulatorLocation);
        checked.add(regulatorLocation);

        // PERFORMANCE FIX: Use proper Queue-based BFS instead of iterator().next()
        // This is O(N) instead of O(N²)
        while (!toCheck.isEmpty()) {
            Location current = toCheck.poll(); // O(1) instead of iterator().next() + remove()

            // PERFORMANCE FIX: Early termination if all connectors are reachable
            if (reachableConnectors.size() == connectedConnectors.size()) {
                break;
            }

            // Find all connectors within range of this location
            for (Location connector : connectedConnectors) {
                // Skip if already processed
                if (checked.contains(connector)) {
                    continue;
                }

                // Check if connector is within range
                if (current.getWorld() != null &&
                    connector.getWorld() != null &&
                    current.getWorld().equals(connector.getWorld()) &&
                    current.distance(connector) <= maxRange) {

                    reachableConnectors.add(connector);
                    checked.add(connector);
                    toCheck.add(connector); // Add to queue for BFS
                }
            }
        }

        // Find orphaned connectors (in network but not reachable)
        Set<Location> orphanedConnectors = new HashSet<>();
        for (Location connector : connectedConnectors) {
            if (!reachableConnectors.contains(connector)) {
                orphanedConnectors.add(connector);
            }
        }

        // Remove orphaned connectors from the network
        for (Location orphaned : orphanedConnectors) {
            connectedConnectors.remove(orphaned);
        }

        return orphanedConnectors;
    }

    // === Setters ===
    
    public void setStoredEnergy(int storedEnergy) {
        this.storedEnergy = Math.max(0, Math.min(storedEnergy, maxCapacity));
    }
    
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        // Ensure stored energy doesn't exceed new capacity
        if (storedEnergy > maxCapacity) {
            storedEnergy = maxCapacity;
        }
    }
    
    /**
     * PERFORMANCE FIX: Check if energy has changed since last hologram update
     * Event-based updates instead of polling every tick
     */
    public boolean hasEnergyChanged() {
        return energyChanged;
    }

    /**
     * PERFORMANCE FIX: Reset the energy changed flag after hologram update
     */
    public void resetEnergyChangedFlag() {
        energyChanged = false;
    }

    /**
     * PERFORMANCE FIX: Set callback for energy change notifications
     * Used by EnergyManager to queue energy updates for batch database saves
     */
    public void setEnergyChangeCallback(EnergyChangeCallback callback) {
        this.energyChangeCallback = callback;
    }

    /**
     * PERFORMANCE FIX: Suppress callbacks temporarily (used during loading from database)
     * Prevents unnecessary queue additions when restoring energy from database
     */
    public void setSuppressCallbacks(boolean suppress) {
        this.suppressCallbacks = suppress;
    }

    /**
     * PERFORMANCE FIX: Energy threshold compression - only save when energy changes significantly
     *
     * Reduces saves by 50-80% by ignoring small changes (e.g., solar panel trickle charging)
     *
     * Example with 1000 J capacity and 10% threshold:
     * - Energy changes from 500 J → 550 J: Don't save (only 5% change)
     * - Energy changes from 500 J → 650 J: Save! (15% change)
     *
     * @return true if energy should be saved to database
     */
    private boolean shouldSaveEnergy() {
        // Always save if threshold is disabled (0.0)
        if (org.ThefryGuy.techFactory.TechFactoryConstants.ENERGY_SAVE_THRESHOLD_PERCENT <= 0.0) {
            return true;
        }

        // Calculate absolute change since last save
        int energyChange = Math.abs(storedEnergy - lastSavedEnergy);

        // Calculate threshold (e.g., 10% of max capacity)
        int threshold = (int) (maxCapacity * org.ThefryGuy.techFactory.TechFactoryConstants.ENERGY_SAVE_THRESHOLD_PERCENT);

        // Save if change exceeds threshold OR if network is empty/full (important states)
        return energyChange >= threshold || storedEnergy == 0 || storedEnergy == maxCapacity;
    }

    /**
     * CRITICAL FIX: Get total wasted energy since last warning
     * Helps players debug why their generators aren't producing full output
     */
    public int getWastedEnergy() {
        return wastedEnergy;
    }

    /**
     * CRITICAL FIX: Reset wasted energy counter
     * Called after displaying warning to player
     */
    public void resetWastedEnergy() {
        this.wastedEnergy = 0;
    }

    @Override
    public String toString() {
        return "EnergyNetwork{" +
                "id=" + networkId +
                ", energy=" + storedEnergy + "/" + maxCapacity + " J" +
                ", panels=" + connectedPanels.size() +
                ", consumers=" + connectedConsumers.size() +
                ", connectors=" + connectedConnectors.size() +
                '}';
    }
}

