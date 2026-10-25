package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.density.AdvancedDensityField;
import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Unified engine core facade.
 * Provides high-level access to all engine subsystems.
 */
public final class EngineCore {
    private static final Logger LOGGER = Logger.getLogger(EngineCore.class.getName());
    
    // Core systems
    private final TerrainEngine terrainEngine;
    private final ChunkMaterializer materializer;
    private final ChunkCache densityCache;
    private final ChunkCache meshCache;
    private final WorldGenPipeline worldGenPipeline;
    private final AdvancedDensityField densityField;
    private final WaterSimulation waterSimulation;
    
    // Executors
    private final Executor workerExecutor;
    private final Executor renderExecutor;
    private final ScheduledExecutorService maintenanceExecutor;
    
    // State
    private volatile boolean running = false;
    private long worldSeed;
    private int terrainRevision;
    private TerrainOptions terrainOptions;

    private EngineCore(Builder builder) {
        this.worldSeed = builder.seed;
        this.terrainRevision = builder.revision;
        this.terrainOptions = builder.options;
        
        // Create terrain engine
        this.terrainEngine = new TerrainEngine(worldSeed, terrainRevision, terrainOptions);
        
        // Create executors
        this.workerExecutor = builder.workerExecutor != null ? builder.workerExecutor : 
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
                Thread t = new Thread(r, "DeepPrimal-Worker");
                t.setDaemon(true);
                return t;
            });
        
        this.renderExecutor = builder.renderExecutor != null ? builder.renderExecutor : 
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "DeepPrimal-Render");
                t.setDaemon(true);
                return t;
            });
        
        this.maintenanceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeepPrimal-Maintenance");
            t.setDaemon(true);
            return t;
        });
        
        // Create caches
        this.densityCache = new ChunkCache(builder.densityCacheMB, builder.densityCacheEntries);
        this.meshCache = new ChunkCache(builder.meshCacheMB, builder.meshCacheEntries);
        
        // Create subsystems
        this.materializer = new ChunkMaterializer(terrainEngine, workerExecutor, renderExecutor);
        this.worldGenPipeline = new WorldGenPipeline(terrainEngine, materializer, densityCache, meshCache);
        this.densityField = new AdvancedDensityField(terrainEngine);
        this.waterSimulation = new WaterSimulation();
        
        // Start maintenance tasks
        startMaintenance();
    }

    private void startMaintenance() {
        // Periodic cache cleanup
        maintenanceExecutor.scheduleAtFixedRate(this::runMaintenance, 60, 60, TimeUnit.SECONDS);
        
        // Metrics reporting
        maintenanceExecutor.scheduleAtFixedRate(this::reportMetrics, 30, 30, TimeUnit.SECONDS);
    }

    private void runMaintenance() {
        try {
            // Check memory pressure
            long heapUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long heapMax = Runtime.getRuntime().maxMemory();
            double memoryPressure = (double) heapUsed / heapMax;
            
            if (memoryPressure > 0.85) {
                LOGGER.warning("High memory pressure: " + String.format("%.1f", memoryPressure * 100) + "%");
                densityCache.handleMemoryPressure();
                meshCache.handleMemoryPressure();
                densityField.clearCaches();
                System.gc();
            }
            
            // Trim caches if needed
            if (memoryPressure > 0.70) {
                densityCache.trimTo((long) (densityCache.getMemoryUsage() * 0.9));
                meshCache.trimTo((long) (meshCache.getMemoryUsage() * 0.9));
            }
        } catch (Exception e) {
            LOGGER.warning("Maintenance task failed: " + e.getMessage());
        }
    }

    private void reportMetrics() {
        try {
            EngineMetrics.updateMemoryMetrics();
            
            // Report to dashboard
            LocalDashboard.gauge("engine.core.density_cache_hit_rate", densityCache.getHitRate());
            LocalDashboard.gauge("engine.core.mesh_cache_hit_rate", meshCache.getHitRate());
            LocalDashboard.gauge("engine.core.density_cache_mb", densityCache.getMemoryUsage() / (1024.0 * 1024.0));
            LocalDashboard.gauge("engine.core.mesh_cache_mb", meshCache.getMemoryUsage() / (1024.0 * 1024.0));
            LocalDashboard.gauge("engine.core.terrain_avg_us", EngineMetrics.getAvgTerrainGenTimeUs());
            LocalDashboard.gauge("engine.core.water_avg_us", EngineMetrics.getAvgWaterSimTimeUs());
            
            var waterStats = waterSimulation.getStats();
            LocalDashboard.gauge("engine.water.active_cells", waterStats.activeCells());
            LocalDashboard.gauge("engine.water.volume_ml", waterStats.totalWaterVolumeMl());
        } catch (Exception e) {
            LOGGER.warning("Metrics reporting failed: " + e.getMessage());
        }
    }

    /**
     * Start the engine core.
     */
    public synchronized void start() {
        if (running) return;
        running = true;
        LOGGER.info("EngineCore started with seed " + worldSeed + ", revision " + terrainRevision);
        
        // Run initial health checks
        EngineHealthCheck.initDefaultChecks();
        EngineHealthCheck.runOrThrow();
    }

    /**
     * Stop the engine core.
     */
    public synchronized void stop() {
        if (!running) return;
        running = false;
        
        worldGenPipeline.shutdown();
        maintenanceExecutor.shutdown();
        
        if (workerExecutor instanceof java.util.concurrent.ExecutorService es) {
            es.shutdown();
        }
        if (renderExecutor instanceof java.util.concurrent.ExecutorService es) {
            es.shutdown();
        }
        
        densityField.clearCaches();
        densityCache.clear();
        meshCache.clear();
        
        LOGGER.info("EngineCore stopped");
    }

    // ============ High-level API ============

    /**
     * Generate terrain height at world position.
     */
    public double getHeight(double x, double z) {
        return terrainEngine.raw(x, z);
    }

    /**
     * Get full terrain sample (height, water, biome, etc.)
     */
    public TerrainEngine.Sample sample(double x, double z) {
        return terrainEngine.sample(x, z);
    }

    /**
     * Get biome at position.
     */
    public int getBiome(double x, double z) {
        return terrainEngine.biome(x, z);
    }

    /**
     * Get biome at 3D position (includes caves).
     */
    public int getBiome(double x, double y, double z) {
        return terrainEngine.biome(x, y, z);
    }

    /**
     * Check if position is ocean.
     */
    public boolean isOcean(double x, double z) {
        return terrainEngine.ocean(x, z);
    }

    /**
     * Get temperature at position.
     */
    public double getTemperature(double x, double z, double height) {
        return terrainEngine.temperature(x, z, height);
    }

    /**
     * Get rainfall at position.
     */
    public double getRainfall(double x, double z) {
        return terrainEngine.rainfall(x, z);
    }

    /**
     * Get watershed region for waterways.
     */
    public WatershedRegion getWatershed(double x, double z) {
        return terrainEngine.waterways(x, z);
    }

    /**
     * Check for cave at position.
     */
    public boolean hasCave(double x, double y, double z, double surface, int minY) {
        return terrainEngine.cave(x, y, z, surface, minY);
    }

    /**
     * Check for tunnel at position.
     */
    public boolean hasTunnel(double x, double z) {
        return terrainEngine.tunnel(x, z);
    }

    /**
     * Get surface height without ice.
     */
    public double getSurfaceWithoutIce(double x, double z) {
        return terrainEngine.surfaceWithoutIce(x, z);
    }

    /**
     * Generate a chunk asynchronously.
     */
    public java.util.concurrent.CompletableFuture<ChunkMaterializer.MaterializedChunk> generateChunkAsync(ChunkMaterializer.ChunkKey key) {
        return worldGenPipeline.generateChunk(key);
    }

    /**
     * Generate multiple chunks.
     */
    public java.util.concurrent.CompletableFuture<Void> generateChunks(java.lang.Iterable<ChunkMaterializer.ChunkKey> keys,
                                                                        java.util.function.Consumer<ChunkMaterializer.MaterializedChunk> callback) {
        return worldGenPipeline.generateChunks(keys, callback);
    }

    /**
     * Simulate water physics for a chunk.
     */
    public void simulateWater(double dt, java.util.function.DoubleSupplier groundHeight) {
        waterSimulation.tick(dt, groundHeight);
    }

    /**
     * Raycast against terrain surface.
     */
    public AdvancedDensityField.SurfaceHit raycastTerrain(double ox, double oy, double oz,
                                                           double dx, double dy, double dz,
                                                           double maxDist) {
        return densityField.raycast(ox, oy, oz, dx, dy, dz, maxDist);
    }

    /**
     * Compute mesh for a chunk.
     */
    public AdvancedDensityField.MeshData computeChunkMesh(int chunkX, int chunkZ) {
        return densityField.computeMesh(chunkX, chunkZ);
    }

    // ============ Accessors ============

    public TerrainEngine getTerrainEngine() { return terrainEngine; }
    public ChunkMaterializer getMaterializer() { return materializer; }
    public ChunkCache getDensityCache() { return densityCache; }
    public ChunkCache getMeshCache() { return meshCache; }
    public WorldGenPipeline getWorldGenPipeline() { return worldGenPipeline; }
    public AdvancedDensityField getDensityField() { return densityField; }
    public WaterSimulation getWaterSimulation() { return waterSimulation; }
    public boolean isRunning() { return running; }
    public long getWorldSeed() { return worldSeed; }
    public int getTerrainRevision() { return terrainRevision; }
    public TerrainOptions getTerrainOptions() { return terrainOptions; }

    // ============ Builder ============

    public static class Builder {
        private long seed = System.currentTimeMillis();
        private int revision = 7;
        private TerrainOptions options = TerrainOptions.DEFAULT;
        private Executor workerExecutor;
        private Executor renderExecutor;
        private int densityCacheMB = 128;
        private int densityCacheEntries = 4096;
        private int meshCacheMB = 256;
        private int meshCacheEntries = 2048;

        public Builder seed(long seed) { this.seed = seed; return this; }
        public Builder revision(int revision) { this.revision = revision; return this; }
        public Builder options(TerrainOptions options) { this.options = options; return this; }
        public Builder workerExecutor(Executor executor) { this.workerExecutor = executor; return this; }
        public Builder renderExecutor(Executor executor) { this.renderExecutor = executor; return this; }
        public Builder densityCacheMB(int mb) { this.densityCacheMB = mb; return this; }
        public Builder densityCacheEntries(int entries) { this.densityCacheEntries = entries; return this; }
        public Builder meshCacheMB(int mb) { this.meshCacheMB = mb; return this; }
        public Builder meshCacheEntries(int entries) { this.meshCacheEntries = entries; return this; }

        public EngineCore build() {
            return new EngineCore(this);
        }
    }

    private EngineCore() {
        throw new UnsupportedOperationException("Use Builder");
    }
}