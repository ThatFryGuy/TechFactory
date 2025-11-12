package org.ThefryGuy.techFactory.data;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance metrics tracker for TechFactory.
 * 
 * Tracks critical performance indicators:
 * - Batch flush times and counts
 * - Energy update rates
 * - Database operation performance
 * - Cache hit/miss rates
 * 
 * Thread-safe using atomic operations.
 * 
 * USAGE:
 * - DatabaseManager updates metrics during batch flushes
 * - MetricsCommand reads metrics to display system health
 * - Automatically resets counters every second for rate calculations
 */
public class PerformanceMetrics {
    
    // Singleton instance
    private static final PerformanceMetrics instance = new PerformanceMetrics();
    
    // ========================================
    // BATCH OPERATION METRICS
    // ========================================
    
    /** Total number of batch flushes performed */
    private final AtomicLong totalBatchFlushes = new AtomicLong(0);
    
    /** Last batch flush duration in milliseconds */
    private volatile long lastBatchFlushTimeMs = 0;
    
    /** Longest batch flush duration in milliseconds (since startup) */
    private volatile long maxBatchFlushTimeMs = 0;
    
    /** Average batch flush time in milliseconds (rolling average) */
    private volatile long avgBatchFlushTimeMs = 0;
    
    /** Number of slow batch flushes (>1000ms) */
    private final AtomicLong slowBatchFlushCount = new AtomicLong(0);
    
    // ========================================
    // ENERGY UPDATE METRICS
    // ========================================
    
    /** Energy updates queued this second */
    private final AtomicInteger energyUpdatesThisSecond = new AtomicInteger(0);
    
    /** Peak energy updates per second (since startup) */
    private volatile int peakEnergyUpdatesPerSecond = 0;
    
    /** Total energy updates processed (since startup) */
    private final AtomicLong totalEnergyUpdates = new AtomicLong(0);
    
    // ========================================
    // DATABASE OPERATION METRICS
    // ========================================
    
    /** Pending saves in queue */
    private volatile int pendingSavesCount = 0;
    
    /** Pending deletes in queue */
    private volatile int pendingDeletesCount = 0;
    
    /** Pending energy updates in queue */
    private volatile int pendingEnergyUpdatesCount = 0;
    
    /** Total database errors (since startup) */
    private final AtomicLong totalDatabaseErrors = new AtomicLong(0);
    
    /** Total database retries (since startup) */
    private final AtomicLong totalDatabaseRetries = new AtomicLong(0);
    
    // ========================================
    // CACHE METRICS
    // ========================================
    
    /** Block cache size */
    private volatile int blockCacheSize = 0;
    
    /** Multiblock cache size */
    private volatile int multiblockCacheSize = 0;
    
    /** Cache hit count (since last reset) */
    private final AtomicLong cacheHits = new AtomicLong(0);
    
    /** Cache miss count (since last reset) */
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // ========================================
    // TIMING
    // ========================================
    
    /** Last time metrics were reset (for per-second calculations) */
    private volatile long lastResetTime = System.currentTimeMillis();
    
    /** Plugin startup time */
    private final long startupTime = System.currentTimeMillis();
    
    // ========================================
    // SINGLETON ACCESS
    // ========================================
    
    private PerformanceMetrics() {
        // Private constructor for singleton
    }
    
    public static PerformanceMetrics getInstance() {
        return instance;
    }
    
    // ========================================
    // BATCH OPERATION TRACKING
    // ========================================
    
    /**
     * Record a batch flush operation
     * @param durationMs Duration in milliseconds
     * @param saveCount Number of saves in batch
     * @param deleteCount Number of deletes in batch
     * @param energyUpdateCount Number of energy updates in batch
     */
    public void recordBatchFlush(long durationMs, int saveCount, int deleteCount, int energyUpdateCount) {
        totalBatchFlushes.incrementAndGet();
        lastBatchFlushTimeMs = durationMs;
        
        // Update max flush time
        if (durationMs > maxBatchFlushTimeMs) {
            maxBatchFlushTimeMs = durationMs;
        }
        
        // Update rolling average (simple moving average)
        long flushCount = totalBatchFlushes.get();
        avgBatchFlushTimeMs = (avgBatchFlushTimeMs * (flushCount - 1) + durationMs) / flushCount;
        
        // Track slow flushes
        if (durationMs > 1000) {
            slowBatchFlushCount.incrementAndGet();
        }
    }
    
    /**
     * Record an energy update
     */
    public void recordEnergyUpdate() {
        energyUpdatesThisSecond.incrementAndGet();
        totalEnergyUpdates.incrementAndGet();
    }
    
    /**
     * Record a database error
     */
    public void recordDatabaseError() {
        totalDatabaseErrors.incrementAndGet();
    }
    
    /**
     * Record a database retry
     */
    public void recordDatabaseRetry() {
        totalDatabaseRetries.incrementAndGet();
    }
    
    /**
     * Record a cache hit
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    /**
     * Record a cache miss
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    // ========================================
    // QUEUE SIZE UPDATES
    // ========================================
    
    /**
     * Update pending queue sizes
     * Called by DatabaseManager during batch flush
     */
    public void updateQueueSizes(int saves, int deletes, int energyUpdates) {
        this.pendingSavesCount = saves;
        this.pendingDeletesCount = deletes;
        this.pendingEnergyUpdatesCount = energyUpdates;
    }
    
    /**
     * Update cache sizes
     * Called by DatabaseManager periodically
     */
    public void updateCacheSizes(int blockCache, int multiblockCache) {
        this.blockCacheSize = blockCache;
        this.multiblockCacheSize = multiblockCache;
    }
    
    // ========================================
    // PERIODIC RESET (for per-second rates)
    // ========================================
    
    /**
     * Reset per-second counters
     * Should be called every second by a scheduled task
     */
    public void resetPerSecondCounters() {
        // Update peak energy updates
        int currentRate = energyUpdatesThisSecond.get();
        if (currentRate > peakEnergyUpdatesPerSecond) {
            peakEnergyUpdatesPerSecond = currentRate;
        }
        
        // Reset counter
        energyUpdatesThisSecond.set(0);
        
        // Update reset time
        lastResetTime = System.currentTimeMillis();
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    public long getTotalBatchFlushes() {
        return totalBatchFlushes.get();
    }
    
    public long getLastBatchFlushTimeMs() {
        return lastBatchFlushTimeMs;
    }
    
    public long getMaxBatchFlushTimeMs() {
        return maxBatchFlushTimeMs;
    }
    
    public long getAvgBatchFlushTimeMs() {
        return avgBatchFlushTimeMs;
    }
    
    public long getSlowBatchFlushCount() {
        return slowBatchFlushCount.get();
    }
    
    public int getEnergyUpdatesThisSecond() {
        return energyUpdatesThisSecond.get();
    }
    
    public int getPeakEnergyUpdatesPerSecond() {
        return peakEnergyUpdatesPerSecond;
    }
    
    public long getTotalEnergyUpdates() {
        return totalEnergyUpdates.get();
    }
    
    public int getPendingSavesCount() {
        return pendingSavesCount;
    }
    
    public int getPendingDeletesCount() {
        return pendingDeletesCount;
    }
    
    public int getPendingEnergyUpdatesCount() {
        return pendingEnergyUpdatesCount;
    }
    
    public long getTotalDatabaseErrors() {
        return totalDatabaseErrors.get();
    }
    
    public long getTotalDatabaseRetries() {
        return totalDatabaseRetries.get();
    }
    
    public int getBlockCacheSize() {
        return blockCacheSize;
    }
    
    public int getMultiblockCacheSize() {
        return multiblockCacheSize;
    }
    
    public long getCacheHits() {
        return cacheHits.get();
    }
    
    public long getCacheMisses() {
        return cacheMisses.get();
    }
    
    /**
     * Get cache hit rate as a percentage
     * @return Hit rate 0.0 to 100.0, or 0.0 if no cache operations
     */
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        if (total == 0) {
            return 0.0;
        }
        
        return (hits * 100.0) / total;
    }
    
    /**
     * Get uptime in seconds
     */
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startupTime) / 1000;
    }
}

