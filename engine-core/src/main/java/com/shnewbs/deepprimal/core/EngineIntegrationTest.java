package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Integration test for the complete Deep Primal engine.
 * Validates all subsystems work together correctly.
 */
public final class EngineIntegrationTest {
    private static final Logger LOGGER = Logger.getLogger(EngineIntegrationTest.class.getName());

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting Deep Primal Engine Integration Test...");
        
        // Initialize dashboard
        LocalDashboard.metadata("engine.test", java.util.Map.of("version", "1.0.0"));
        
        // Test 1: Basic engine creation
        testEngineCreation();
        
        // Test 2: Terrain generation
        testTerrainGeneration();
        
        // Test 3: Water simulation
        testWaterSimulation();
        
        // Test 4: Density field
        testDensityField();
        
        // Test 5: Chunk materialization
        testChunkMaterialization();
        
        // Test 6: Caching
        testCaching();
        
        // Test 7: World generation pipeline
        testWorldGenPipeline();
        
        // Test 8: Metrics and health checks
        testMetricsAndHealth();
        
        // Test 9: Upscaling config
        testUpscalingConfig();
        
        // Test 10: Stress test
        stressTest();
        
        LOGGER.info("All integration tests passed!");
        
        // Print final metrics
        LOGGER.info(EngineMetrics.snapshot());
    }

    private static void testEngineCreation() {
        LOGGER.info("Test 1: Engine Creation");
        
        EngineCore core = new EngineCore.Builder()
            .seed(12345L)
            .revision(7)
            .options(TerrainOptions.DEFAULT)
            .densityCacheMB(64)
            .meshCacheMB(128)
            .build();
        
        core.start();
        
        assert core.isRunning() : "Engine should be running";
        assert core.getWorldSeed() == 12345L : "Seed should match";
        assert core.getTerrainRevision() == 7 : "Revision should be 7";
        assert core.getTerrainOptions() == TerrainOptions.DEFAULT : "Options should be default";
        
        core.stop();
        assert !core.isRunning() : "Engine should be stopped";
        
        LOGGER.info("  PASSED");
    }

    private static void testTerrainGeneration() {
        LOGGER.info("Test 2: Terrain Generation");
        
        TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
        
        // Test height at various positions
        double h0 = engine.raw(0, 0);
        double h1 = engine.raw(100, 100);
        double h2 = engine.raw(-500, 500);
        
        assert Double.isFinite(h0) : "Height at 0,0 should be finite";
        assert Double.isFinite(h1) : "Height at 100,100 should be finite";
        assert Double.isFinite(h2) : "Height at -500,500 should be finite";
        
        // Test sample
        TerrainEngine.Sample sample = engine.sample(0, 0);
        assert sample != null : "Sample should not be null";
        assert Double.isFinite(sample.ground()) : "Ground should be finite";
        assert sample.rain() >= 0 && sample.rain() <= 1 : "Rain should be 0-1";
        assert sample.temperature() >= 0 && sample.temperature() <= 1 : "Temp should be 0-1";
        
        // Test biome
        int biome = engine.biome(0, 0);
        assert biome >= 0 : "Biome should be non-negative";
        
        // Test ocean
        boolean isOcean = engine.ocean(0, 0);
        // Just verify it doesn't crash
        
        // Test temperature
        double temp = engine.temperature(0, 0, 100);
        assert temp >= 0 && temp <= 1 : "Temperature should be 0-1";
        
        // Test rainfall
        double rain = engine.rainfall(0, 0);
        assert rain >= 0 && rain <= 1 : "Rainfall should be 0-1";
        
        // Test watershed
        if (engine.revision >= 3) {
            WatershedRegion ws = engine.waterways(0, 0);
            assert ws != null : "Watershed should not be null";
        }
        
        LOGGER.info("  PASSED");
    }

    private static void testWaterSimulation() {
        LOGGER.info("Test 3: Water Simulation");
        
        WaterSimulation sim = new WaterSimulation();
        
        // Add water source
        sim.setWaterLevel(8, 8, 5.0, true);
        
        // Simulate a few ticks
        for (int i = 0; i < 10; i++) {
            sim.tick(1.0, () -> 64.0);
        }
        
        WaterSimulation.Stats stats = sim.getStats();
        assert stats.activeCells() >= 0 : "Active cells should be non-negative";
        assert stats.totalWaterVolumeMl() >= 0 : "Volume should be non-negative";
        
        // Test add/remove water
        sim.addWater(8, 8, 1.0);
        double removed = sim.removeWater(8, 8, 0.5);
        assert removed == 0.5 : "Should remove 0.5 units";
        
        // Test packet encoding
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(144 * 4);
        sim.encodeToPacket(buf, 0, System.currentTimeMillis());
        
        LOGGER.info("  PASSED");
    }

    private static void testDensityField() {
        LOGGER.info("Test 4: Density Field");
        
        TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
        AdvancedDensityField field = new AdvancedDensityField(engine);
        
        // Test sampling
        double d = field.sampleFullRes(0, 100, 0);
        assert Double.isFinite(d) : "Density should be finite";
        
        // Test with derivatives
        AdvancedDensityField.DensitySample sample = field.sampleWithDerivatives(0, 100, 0);
        assert Double.isFinite(sample.density()) : "Density should be finite";
        assert Double.isFinite(sample.dx()) : "Derivative should be finite";
        
        // Test raycast
        AdvancedDensityField.SurfaceHit hit = field.raycast(0, 200, 0, 0, -1, 0, 500);
        // Hit may or may not occur, just verify it doesn't crash
        
        // Test cache stats
        var cacheStats = field.getCacheStats();
        assert cacheStats.chunkCount() >= 0 : "Chunk count should be non-negative";
        
        LOGGER.info("  PASSED");
    }

    private static void testChunkMaterialization() throws Exception {
        LOGGER.info("Test 5: Chunk Materialization");
        
        TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
        var executor = Executors.newFixedThreadPool(2);
        
        ChunkMaterializer materializer = new ChunkMaterializer(engine, executor, executor);
        
        // Test sync materialization
        ChunkMaterializer.ChunkKey key = new ChunkMaterializer.ChunkKey(0, 0, 0);
        ChunkMaterializer.MaterializedChunk chunk = materializer.materializeSync(key);
        
        assert chunk.key().equals(key) : "Key should match";
        assert chunk.mesh() != null : "Mesh should not be null";
        assert chunk.water() != null : "Water data should not be null";
        assert chunk.densityGraph() != null : "Density graph should not be null";
        
        // Test async materialization
        CompletableFuture<ChunkMaterializer.MaterializedChunk> future = 
            materializer.materializeAsync(new ChunkMaterializer.ChunkKey(1, 0, 0));
        ChunkMaterializer.MaterializedChunk asyncChunk = future.get(5, TimeUnit.SECONDS);
        assert asyncChunk != null : "Async chunk should complete";
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        
        LOGGER.info("  PASSED");
    }

    private static void testCaching() {
        LOGGER.info("Test 6: Caching");
        
        ChunkCache cache = new ChunkCache(10, 100); // 10MB, 100 entries
        
        // Test put/get
        cache.put(1, "value1", 100);
        String val = cache.get(1);
        assert "value1".equals(val) : "Should retrieve value";
        
        // Test hit/miss
        cache.get(1); // hit
        cache.get(2); // miss
        double hitRate = cache.getHitRate();
        assert hitRate == 50.0 : "Hit rate should be 50%";
        
        // Test getOrCompute
        String computed = cache.getOrCompute(3, k -> "computed" + k, 100);
        assert "computed3".equals(computed) : "Should compute and cache";
        
        // Test second call uses cache
        String cached = cache.getOrCompute(3, k -> "should_not_run", 100);
        assert "computed3".equals(cached) : "Should use cached value";
        
        // Test eviction
        for (int i = 0; i < 150; i++) {
            cache.put(i, "val" + i, 100);
        }
        assert cache.size() <= 100 : "Should not exceed max entries";
        
        // Test memory pressure
        cache.handleMemoryPressure();
        
        // Test snapshot
        ChunkCache.Snapshot snapshot = ChunkCache.Snapshot.from(cache);
        assert snapshot.entryCount() >= 0 : "Snapshot should have count";
        
        LOGGER.info("  PASSED");
    }

    private static void testWorldGenPipeline() throws Exception {
        LOGGER.info("Test 7: World Generation Pipeline");
        
        TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
        var executor = Executors.newFixedThreadPool(2);
        
        ChunkMaterializer materializer = new ChunkMaterializer(engine, executor, executor);
        ChunkCache densityCache = new ChunkCache(64, 1024);
        ChunkCache meshCache = new ChunkCache(128, 512);
        
        WorldGenPipeline pipeline = new WorldGenPipeline(engine, materializer, densityCache, meshCache);
        
        // Test single chunk
        WorldGenPipeline.ChunkKey key = new WorldGenPipeline.ChunkKey(0, 0, 0);
        CompletableFuture<ChunkMaterializer.MaterializedChunk> future = pipeline.generateChunk(key);
        // Just verify it starts without error
        
        // Test multiple chunks
        java.util.List<WorldGenPipeline.ChunkKey> keys = java.util.List.of(
            new WorldGenPipeline.ChunkKey(0, 0, 0),
            new WorldGenPipeline.ChunkKey(1, 0, 0),
            new WorldGenPipeline.ChunkKey(0, 1, 0)
        );
        
        CompletableFuture<Void> multiFuture = pipeline.generateChunks(keys, chunk -> {
            assert chunk != null : "Chunk should not be null";
        });
        
        multiFuture.get(10, TimeUnit.SECONDS);
        
        pipeline.shutdown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        
        LOGGER.info("  PASSED");
    }

    private static void testMetricsAndHealth() {
        LOGGER.info("Test 8: Metrics and Health Checks");
        
        EngineMetrics.reset();
        
        // Record some metrics
        EngineMetrics.recordTerrainSample(1_000_000); // 1ms
        EngineMetrics.recordWaterSim(500_000); // 0.5ms
        EngineMetrics.CACHE_HITS.increment();
        EngineMetrics.CACHE_HITS.increment();
        EngineMetrics.CACHE_MISSES.increment();
        
        double hitRate = EngineMetrics.getCacheHitRate();
        assert hitRate == 66.66666666666666 : "Hit rate should be ~66.67%";
        
        double avgTerrain = EngineMetrics.getAvgTerrainGenTimeUs();
        assert avgTerrain == 1000.0 : "Avg terrain should be 1000 us";
        
        double avgWater = EngineMetrics.getAvgWaterSimTimeUs();
        assert avgWater == 500.0 : "Avg water should be 500 us";
        
        // Test health checks
        EngineHealthCheck.initDefaultChecks();
        var results = EngineHealthCheck.runAll();
        assert results.isEmpty() : "All health checks should pass: " + results;
        
        // Test validation
        CoreValidation.ValidationReport report = CoreValidation.validateSeed(12345L);
        assert report.passed() : "Seed validation should pass: " + report.issues();
        
        EngineMetrics.reset();
        
        LOGGER.info("  PASSED");
    }

    private static void testUpscalingConfig() {
        LOGGER.info("Test 9: Upscaling Configuration");
        
        // Test FSR quality enums
        for (com.shnewbs.deepprimal.settings.UpscalingConfig.FsrQuality q : 
             com.shnewbs.deepprimal.settings.UpscalingConfig.FsrQuality.values()) {
            assert q.scaleFactor() > 1.0f : "FSR scale factor should be > 1";
            assert q.scaleFactor() <= 2.0f : "FSR scale factor should be <= 2";
        }
        
        // Test DLSS quality enums
        for (com.shnewbs.deepprimal.settings.UpscalingConfig.DlssQuality q : 
             com.shnewbs.deepprimal.settings.UpscalingConfig.DlssQuality.values()) {
            assert q.scaleFactor() > 1.0f : "DLSS scale factor should be > 1";
            assert q.scaleFactor() <= 2.0f : "DLSS scale factor should be <= 2";
        }
        
        // Test mode enum
        for (com.shnewbs.deepprimal.settings.UpscalingConfig.UpscalingMode m : 
             com.shnewbs.deepprimal.settings.UpscalingConfig.UpscalingMode.values()) {
            assert m.name() != null : "Mode should have name";
        }
        
        LOGGER.info("  PASSED");
    }

    private static void stressTest() {
        LOGGER.info("Test 10: Stress Test");
        
        EngineCore core = new EngineCore.Builder()
            .seed(987654321L)
            .revision(7)
            .options(TerrainOptions.DEFAULT)
            .densityCacheMB(256)
            .meshCacheMB(512)
            .build();
        
        core.start();
        
        long start = System.currentTimeMillis();
        int iterations = 1000;
        
        // Stress terrain sampling
        for (int i = 0; i < iterations; i++) {
            double x = (Math.random() - 0.5) * 10000;
            double z = (Math.random() - 0.5) * 10000;
            
            core.getHeight(x, z);
            core.sample(x, z);
            core.getBiome(x, z);
            core.getTemperature(x, z, 100);
            core.getRainfall(x, z);
            core.isOcean(x, z);
        }
        
        long terrainTime = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        
        // Stress water simulation
        WaterSimulation sim = new WaterSimulation();
        for (int i = 0; i < 100; i++) {
            sim.setWaterLevel((int)(Math.random() * 16), (int)(Math.random() * 16), 
                Math.random() * 5, Math.random() > 0.5);
        }
        
        for (int i = 0; i < 1000; i++) {
            sim.tick(1.0, () -> 64.0);
        }
        
        long waterTime = System.currentTimeMillis() - start;
        
        LOGGER.info(String.format("  Terrain: %d ops in %d ms (%.2f us/op)",
            iterations * 6, terrainTime, (terrainTime * 1000.0) / (iterations * 6)));
        LOGGER.info(String.format("  Water: 1000 ticks in %d ms (%.2f us/tick)",
            waterTime, (waterTime * 1000.0) / 1000.0));
        
        core.stop();
        
        LOGGER.info("  PASSED");
    }

    private EngineIntegrationTest() {}
}