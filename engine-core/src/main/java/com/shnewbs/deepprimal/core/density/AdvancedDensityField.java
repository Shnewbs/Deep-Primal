package com.shnewbs.deepprimal.core.density;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.TerrainNoise;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

/**
 * Advanced density field with caching, LOD, and derivative computation.
 * Used for high-quality terrain generation and collision.
 */
public final class AdvancedDensityField {
    private final TerrainNoise noise;
    private final TerrainOptions options;
    private final TerrainEngine engine;
    
    // Caches for different LODs
    private final ConcurrentHashMap<Long, DensityChunk> chunkCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, DensityChunk> lodCache = new ConcurrentHashMap<>();
    
    // Configuration
    private final int baseResolution = 32; // 32^3 per chunk
    private final int maxLod = 3;
    
    public AdvancedDensityField(TerrainEngine engine) {
        this.engine = engine;
        this.noise = engine.noise;
        this.options = engine.options;
    }

    /**
     * Sample density at a world position with automatic LOD selection.
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param lod Level of detail (0 = full res, higher = lower res)
     */
    public double sample(double x, double y, double z, int lod) {
        if (lod <= 0) {
            return sampleFullRes(x, y, z);
        }
        
        // Use cached LOD data
        long key = lodKey(x, z, lod);
        DensityChunk chunk = lodCache.computeIfAbsent(key, k -> generateLodChunk(k, lod));
        return chunk.sample(x, y, z);
    }

    /**
     * Sample at full resolution.
     */
    public double sampleFullRes(double x, double y, double z) {
        // Use the engine's raw height function
        double groundHeight = engine.raw(x, z);
        
        // Convert to density: positive = solid, negative = air
        // Using a smooth transition around the surface
        double density = groundHeight - y;
        
        // Add noise for caves/overhangs
        if (options.caves()) {
            double caveNoise = noise.volume(x, y, z, 54, 140);
            double caveNoise2 = noise.volume(x, y, z, 62, 740);
            double caveFactor = caveNoise * caveNoise + caveNoise2 * caveNoise2;
            if (caveFactor < 0.012) {
                density = -Math.abs(density) - 1.0; // Force air
            }
        }
        
        return density;
    }

    /**
     * Sample with derivatives (gradient) for normal computation.
     */
    public DensitySample sampleWithDerivatives(double x, double y, double z) {
        double eps = 0.01;
        
        double d = sampleFullRes(x, y, z);
        double dx = sampleFullRes(x + eps, y, z) - d;
        double dy = sampleFullRes(x, y + eps, z) - d;
        double dz = sampleFullRes(x, y, z + eps) - d;
        
        return new DensitySample(d, dx / eps, dy / eps, dz / eps);
    }

    /**
     * Generate a full-resolution density chunk.
     */
    public DensityChunk generateChunk(int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        return chunkCache.computeIfAbsent(key, k -> {
            DensityChunk chunk = new DensityChunk(chunkX, chunkZ, baseResolution);
            int baseX = chunkX * 16;
            int baseZ = chunkZ * 16;
            
            for (int x = 0; x < baseResolution; x++) {
                for (int y = 0; y < baseResolution; y++) {
                    for (int z = 0; z < baseResolution; z++) {
                        double wx = baseX + (x + 0.5) * 16.0 / baseResolution;
                        double wy = y * 256.0 / baseResolution; // Full height
                        double wz = baseZ + (z + 0.5) * 16.0 / baseResolution;
                        chunk.set(x, y, z, sampleFullRes(wx, wy, wz));
                    }
                }
            }
            return chunk;
        });
    }

    private DensityChunk generateLodChunk(long key, int lod) {
        int resolution = baseResolution >> lod;
        int chunkX = (int) (key >> 32);
        int chunkZ = (int) (key & 0xFFFFFFFFL);
        
        DensityChunk chunk = new DensityChunk(chunkX, chunkZ, resolution);
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    double wx = baseX + (x + 0.5) * 16.0 / resolution;
                    double wy = y * 256.0 / resolution;
                    double wz = baseZ + (z + 0.5) * 16.0 / resolution;
                    chunk.set(x, y, z, sampleFullRes(wx, wy, wz));
                }
            }
        }
        return chunk;
    }

    /**
     * Compute the surface mesh for a chunk using dual contouring.
     */
    public MeshData computeMesh(int chunkX, int chunkZ) {
        DensityChunk chunk = generateChunk(chunkX, chunkZ);
        return DualContouring.extractMesh(chunk);
    }

    /**
     * Find the exact surface intersection along a ray.
     * Used for precise block placement and collision.
     */
    public SurfaceHit raycast(double ox, double oy, double oz,
                               double dx, double dy, double dz,
                               double maxDist) {
        double t = 0;
        double step = 0.5;
        double prevDensity = sampleFullRes(ox, oy, oz);
        
        while (t < maxDist) {
            double x = ox + dx * t;
            double y = oy + dy * t;
            double z = oz + dz * t;
            
            double density = sampleFullRes(x, y, z);
            
            if (prevDensity > 0 && density <= 0) {
                // Crossed surface - binary search for exact intersection
                double t0 = t - step;
                double t1 = t;
                for (int i = 0; i < 8; i++) {
                    double tm = (t0 + t1) * 0.5;
                    double xm = ox + dx * tm;
                    double ym = oy + dy * tm;
                    double zm = oz + dz * tm;
                    double dm = sampleFullRes(xm, ym, zm);
                    if (dm > 0) t0 = tm;
                    else t1 = tm;
                }
                double hitT = (t0 + t1) * 0.5;
                double hx = ox + dx * hitT;
                double hy = oy + dy * hitT;
                double hz = oz + dz * hitT;
                var deriv = sampleWithDerivatives(hx, hy, hz);
                return new SurfaceHit(true, hitT, hx, hy, hz, deriv.nx(), deriv.ny(), deriv.nz());
            }
            
            prevDensity = density;
            t += step;
        }
        
        return new SurfaceHit(false, maxDist, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Clear all caches.
     */
    public void clearCaches() {
        chunkCache.clear();
        lodCache.clear();
    }

    /**
     * Get cache statistics.
     */
    public record CacheStats(int chunkCount, int lodCount, long estimatedMemoryBytes) {}
    
    public CacheStats getCacheStats() {
        return new CacheStats(
            chunkCache.size(),
            lodCache.size(),
            (chunkCache.size() + lodCache.size()) * baseResolution * baseResolution * baseResolution * 8
        );
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static long lodKey(double x, double z, int lod) {
        int chunkX = (int) Math.floor(x / 16);
        int chunkZ = (int) Math.floor(z / 16);
        return (((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL)) << 8 | lod;
    }

    public record DensitySample(double density, double dx, double dy, double dz) {
        public double nx() { return dx; }
        public double ny() { return dy; }
        public double nz() { return dz; }
        public double length() { return Math.sqrt(dx * dx + dy * dy + dz * dz); }
        public DensitySample normalized() {
            double len = length();
            return len > 0 ? new DensitySample(density, dx / len, dy / len, dz / len) : this;
        }
    }

    public record SurfaceHit(boolean hit, double distance, double x, double y, double z,
                              double nx, double ny, double nz) {}

    public record MeshData(
        float[] positions,
        int[] indices,
        byte[] normals,
        short[] materialIds
    ) {}

    /**
     * Dual contouring mesh extraction (simplified).
     */
    private static class DualContouring {
        static MeshData extractMesh(DensityChunk chunk) {
            // Simplified - real implementation would use proper dual contouring
            // with QEF solving for sharp features
            return new MeshData(new float[0], new int[0], new byte[0], new short[0]);
        }
    }
}