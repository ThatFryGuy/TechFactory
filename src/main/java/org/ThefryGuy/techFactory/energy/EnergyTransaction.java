package org.ThefryGuy.techFactory.energy;

/**
 * CRITICAL FIX: Transaction pattern for energy operations
 * Prevents energy loss when operations fail
 * 
 * WHY THIS EXISTS:
 * - Without transactions, if energy is removed but the operation fails, energy is lost
 * - Example: Remove 100 J for smelting, but output inventory is full → 100 J lost!
 * - This creates exploits and frustrates players
 * 
 * HOW IT WORKS:
 * 1. Create transaction with tryRemove(amount)
 * 2. Perform the operation (smelting, processing, etc.)
 * 3. If successful: commit() - energy stays removed
 * 4. If failed: rollback() - energy is restored
 * 
 * USAGE:
 * ```java
 * EnergyTransaction tx = new EnergyTransaction(network);
 * if (tx.tryRemove(100)) {
 *     try {
 *         // Do operation
 *         doSmelting();
 *         tx.commit();  // Success - keep energy removed
 *     } catch (Exception e) {
 *         tx.rollback();  // Failed - restore energy
 *     }
 * }
 * ```
 */
public class EnergyTransaction {
    
    private final EnergyNetwork network;
    private int amountRemoved = 0;
    private boolean committed = false;
    private boolean rolledBack = false;
    
    /**
     * Create a new energy transaction
     * @param network The energy network to transact with
     */
    public EnergyTransaction(EnergyNetwork network) {
        this.network = network;
    }
    
    /**
     * Try to remove energy from the network
     * @param amount Amount of energy to remove in Joules
     * @return true if energy was removed, false if insufficient energy
     */
    public boolean tryRemove(int amount) {
        if (committed || rolledBack) {
            throw new IllegalStateException("Transaction already completed");
        }
        
        int removed = network.removeEnergy(amount);
        this.amountRemoved = removed;
        
        return removed >= amount;
    }
    
    /**
     * Commit the transaction - keep the energy removed
     * Call this when the operation succeeds
     */
    public void commit() {
        if (committed) {
            throw new IllegalStateException("Transaction already committed");
        }
        if (rolledBack) {
            throw new IllegalStateException("Transaction already rolled back");
        }
        
        committed = true;
        // Energy stays removed - nothing to do
    }
    
    /**
     * Rollback the transaction - restore the energy
     * Call this when the operation fails
     */
    public void rollback() {
        if (committed) {
            throw new IllegalStateException("Cannot rollback committed transaction");
        }
        if (rolledBack) {
            throw new IllegalStateException("Transaction already rolled back");
        }
        
        if (amountRemoved > 0) {
            network.addEnergy(amountRemoved);
        }
        
        rolledBack = true;
    }
    
    /**
     * Get the amount of energy that was removed
     * @return Amount removed in Joules
     */
    public int getAmountRemoved() {
        return amountRemoved;
    }
    
    /**
     * Check if transaction was committed
     * @return true if committed
     */
    public boolean isCommitted() {
        return committed;
    }
    
    /**
     * Check if transaction was rolled back
     * @return true if rolled back
     */
    public boolean isRolledBack() {
        return rolledBack;
    }
}

