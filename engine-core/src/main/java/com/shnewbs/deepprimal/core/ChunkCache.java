package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Thread-safe LRU chunk cache with memory pressure awareness.
 * Supports both CPU-side density graphs and GPU-side mesh data.
 */
public final class ChunkCache {
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> accessOrder = new ConcurrentLinkedQueue<>();
    private final AtomicLong currentMemoryBytes = new AtomicLong(0);
    private final long maxMemoryBytes;
    private final int maxEntries;
    private final CachePolicy policy;
    
    // Statistics
    public final AtomicLong hits = new AtomicLong(0);
    public final AtomicLong misses = new AtomicLong(0);
    public final AtomicLong evictions = new AtomicLong(0);
    public final AtomicLong memoryPressureEvents = new AtomicLong(0);

    public ChunkCache(long maxMemoryMB, int maxEntries) {
        this(maxMemoryMB, maxEntries, CachePolicy.LRU);
    }

    public ChunkCache(long maxMemoryMB, int maxEntries, CachePolicy policy) {
        this.maxMemoryBytes = maxMemoryMB * 1024L * 1024L;
        this.maxEntries = maxEntries;
        this.policy = policy;
    }

    /**
     * Get a chunk from cache, computing if absent.
     */
    public <T> T getOrCompute(long key, Function<Long, T> computer, long estimatedSizeBytes) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            entry.touch();
            updateAccessOrder(key);
            return (T) entry.value;
        }
        
        misses.incrementAndGet();
        T value = computer.apply(key);
        put(key, value, estimatedSizeBytes);
        return value;
    }

    /**
     * Put a value in the cache.
     */
    public void put(long key, Object value, long sizeBytes) {
        // Check memory pressure before adding
        ensureCapacity(sizeBytes);
        
        CacheEntry entry = new CacheEntry(value, sizeBytes, System.currentTimeMillis());
        CacheEntry old = cache.put(key, entry);
        
        if (old != null) {
            currentMemoryBytes.addAndGet(-old.sizeBytes);
        }
        
        currentMemoryBytes.addAndGet(sizeBytes);
        updateAccessOrder(key);
        
        // Enforce entry count limit
        enforceEntryLimit();
    }

    /**
     * Get a value without computing if absent.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(long key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            entry.touch();
            updateAccessOrder(key);
            return (T) entry.value;
        }
        misses.incrementAndGet();
        return null;
    }

    /**
     * Remove a specific key.
     */
    public boolean remove(long key) {
        CacheEntry entry = cache.remove(key);
        if (entry != null) {
            currentMemoryBytes.addAndGet(-entry.sizeBytes);
            accessOrder.remove(key);
            return true;
        }
        return false;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        cache.clear();
        accessOrder.clear();
        currentMemoryBytes.set(0);
    }

    /**
     * Get current memory usage in bytes.
     */
    public long getMemoryUsage() {
        return currentMemoryBytes.get();
    }

    /**
     * Get current entry count.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Get cache hit rate (0.0 - 1.0).
     */
    public double getHitRate() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        return total > 0 ? (double) h / total : 1.0;
    }

    /**
     * Trim cache to target memory usage.
     */
    public void trimTo(long targetBytes) {
        while (currentMemoryBytes.get() > targetBytes && !accessOrder.isEmpty()) {
            Long oldest = accessOrder.poll();
            if (oldest != null) {
                CacheEntry entry = cache.remove(oldest);
                if (entry != null) {
                    currentMemoryBytes.addAndGet(-entry.sizeBytes);
                    evictions.incrementAndGet();
                }
            }
        }
    }

    /**
     * Handle memory pressure - aggressively evict.
     */
    public void handleMemoryPressure() {
        memoryPressureEvents.incrementAndGet();
        // Evict 25% of cache
        long target = (long) (maxMemoryBytes * 0.75);
        trimTo(target);
    }

    private void ensureCapacity(long incomingSize) {
        long current = currentMemoryBytes.get();
        if (current + incomingSize > maxMemoryBytes) {
            // Need to evict
            long target = maxMemoryBytes - incomingSize;
            trimTo(target);
        }
    }

    private void enforceEntryLimit() {
        while (cache.size() > maxEntries && !accessOrder.isEmpty()) {
            Long oldest = accessOrder.poll();
            if (oldest != null) {
                CacheEntry entry = cache.remove(oldest);
                if (entry != null) {
                    currentMemoryBytes.addAndGet(-entry.sizeBytes);
                    evictions.incrementAndGet();
                }
            }
        }
    }

    private void updateAccessOrder(long key) {
        accessOrder.remove(key);
        accessOrder.add(key);
    }

    private static class CacheEntry {
        final Object value;
        final long sizeBytes;
        final long createdAt;
        volatile long lastAccess;
        
        CacheEntry(Object value, long sizeBytes, long createdAt) {
            this.value = value;
            this.sizeBytes = sizeBytes;
            this.createdAt = createdAt;
            this.lastAccess = createdAt;
        }
        
        void touch() {
            lastAccess = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            // Default: expire after 5 minutes of no access
            return System.currentTimeMillis() - lastAccess > 300_000;
        }
    }

    public enum CachePolicy {
        LRU,
        LFU,
        TTL
    }

    /**
     * Cache snapshot for debugging/monitoring.
     */
    public record Snapshot(
        int entryCount,
        long memoryBytes,
        double hitRate,
        long hits,
        long misses,
        long evictions,
        long memoryPressureEvents
    ) {
        public static Snapshot from(ChunkCache cache) {
            return new Snapshot(
                cache.size(),
                cache.getMemoryUsage(),
                cache.getHitRate(),
                cache.hits.get(),
                cache.misses.get(),
                cache.evictions.get(),
                cache.memoryPressureEvents.get()
            );
        }
    }
}