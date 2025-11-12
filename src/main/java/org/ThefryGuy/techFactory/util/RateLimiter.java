package org.ThefryGuy.techFactory.util;

import org.ThefryGuy.techFactory.TechFactoryConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic rate limiter for preventing spam/DoS attacks
 * Thread-safe with automatic cleanup and memory leak protection
 *
 * BUG FIX 1: Added max size enforcement to prevent unbounded growth
 * - When size exceeds MAX_SIZE, forces cleanup of old entries
 * - Prevents memory leak with 1000+ players over long server uptime
 *
 * Usage:
 *   RateLimiter limiter = new RateLimiter(500); // 500ms cooldown
 *   if (!limiter.tryAccess(player.getUniqueId().toString())) {
 *       player.sendMessage("Too fast!");
 *       return;
 *   }
 */
public class RateLimiter {

    private final long cooldownMs;
    private final Map<String, Long> lastAccess = new ConcurrentHashMap<>();
    private final int maxSize;

    /**
     * Create a rate limiter with specified cooldown and default max size
     * @param cooldownMs Minimum time between accesses in milliseconds
     */
    public RateLimiter(long cooldownMs) {
        this(cooldownMs, TechFactoryConstants.RATE_LIMITER_MAX_SIZE());
    }

    /**
     * Create a rate limiter with specified cooldown and max size
     * @param cooldownMs Minimum time between accesses in milliseconds
     * @param maxSize Maximum number of entries before forced cleanup
     */
    public RateLimiter(long cooldownMs, int maxSize) {
        this.cooldownMs = cooldownMs;
        this.maxSize = maxSize;
    }
    
    /**
     * Try to access a resource
     *
     * BUG FIX 1: Now enforces max size limit to prevent memory leaks
     * - If size exceeds maxSize, forces cleanup before allowing access
     * - Prevents unbounded growth with 1000+ players
     *
     * @param key Unique identifier (e.g., player UUID, location key)
     * @return true if access allowed, false if still on cooldown
     */
    public boolean tryAccess(String key) {
        // BUG FIX 1: Enforce max size limit to prevent memory leak
        // If we've exceeded the max size, force a cleanup
        if (lastAccess.size() >= maxSize) {
            forceCleanup();
        }

        long now = System.currentTimeMillis();
        Long last = lastAccess.get(key);

        if (last != null && (now - last) < cooldownMs) {
            return false; // Still on cooldown
        }

        lastAccess.put(key, now);
        return true;
    }
    
    /**
     * Remove a specific key from the rate limiter
     * @param key Key to remove
     */
    public void remove(String key) {
        lastAccess.remove(key);
    }
    
    /**
     * Cleanup old entries to prevent memory leaks
     * Call this periodically (e.g., every 30 seconds)
     * @param maxAgeMs Remove entries older than this
     * @return Number of entries removed
     */
    public int cleanup(long maxAgeMs) {
        long now = System.currentTimeMillis();
        int sizeBefore = lastAccess.size();
        lastAccess.entrySet().removeIf(entry ->
            (now - entry.getValue()) > maxAgeMs
        );
        return sizeBefore - lastAccess.size();
    }

    /**
     * Force cleanup when max size is exceeded
     *
     * BUG FIX 1: Emergency cleanup to prevent memory leak
     * - Removes entries older than the cooldown period
     * - If still too large, removes oldest 50% of entries
     * - Prevents unbounded growth in high-traffic scenarios
     *
     * PERFORMANCE FIX: Removed .sorted() call to avoid O(n log n) performance hit
     * - Uses min/max approach instead of sorting (O(n) instead of O(n log n))
     */
    private void forceCleanup() {
        long now = System.currentTimeMillis();

        // First pass: Remove entries older than cooldown period
        // These are definitely stale and safe to remove
        lastAccess.entrySet().removeIf(entry ->
            (now - entry.getValue()) > cooldownMs
        );

        // Second pass: If still too large, remove oldest 50% of entries
        // This is aggressive but necessary to prevent memory leak
        if (lastAccess.size() >= maxSize) {
            // Find min and max timestamps (O(n) instead of sorting O(n log n))
            long minTimestamp = Long.MAX_VALUE;
            long maxTimestamp = Long.MIN_VALUE;

            for (Long timestamp : lastAccess.values()) {
                if (timestamp < minTimestamp) minTimestamp = timestamp;
                if (timestamp > maxTimestamp) maxTimestamp = timestamp;
            }

            // Calculate median as midpoint between min and max
            // This is an approximation but avoids expensive sorting
            long medianTimestamp = (minTimestamp + maxTimestamp) / 2;

            // Remove all entries older than median
            lastAccess.entrySet().removeIf(entry ->
                entry.getValue() <= medianTimestamp
            );
        }
    }

    /**
     * Get current size of the rate limiter cache
     * @return Number of tracked keys
     */
    public int size() {
        return lastAccess.size();
    }

    /**
     * Get maximum allowed size
     * @return Maximum number of entries before forced cleanup
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Clear all entries
     */
    public void clear() {
        lastAccess.clear();
    }
}

