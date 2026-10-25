package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.DensityField;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

/**
 * World generation pipeline coordinating terrain, water, and biome generation.
 * Designed for parallel execution with proper dependency ordering.
 */
public final class WorldGenPipeline {
    private final TerrainEngine terrainEngine;
    private final ChunkMaterializer materializer;
    private final ChunkCache densityCache;
    private final ChunkCache meshCache;
    private final Executor generationExecutor;
    private final Executor callbackExecutor;
    
    // Pipeline stages
    private final Stage<ChunkKey, DensityGraph> densityStage;
    private final Stage<DensityGraph, ChunkMaterializer.MeshData> meshStage;
    private final Stage<ChunkMaterializer.MeshData, ChunkMaterializer.MaterializedChunk> gpuStage;

    public WorldGenPipeline(TerrainEngine terrainEngine, ChunkMaterializer materializer,
                            ChunkCache densityCache, ChunkCache meshCache) {
        this.terrainEngine = terrainEngine;
        this.materializer = materializer;
        this.densityCache = densityCache;
        this.meshCache = meshCache;
        
        // Dedicated executor for world generation
        this.generationExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "DeepPrimal-WorldGen-" + count++);
                    t.setDaemon(true);
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                }
            }
        );
        
        // Callback executor (can be main thread)
        this.callbackExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DeepPrimal-WorldGen-Callback");
            t.setDaemon(true);
            return t;
        });
        
        // Build pipeline stages
        this.densityStage = new Stage<>("density", generationExecutor, this::generateDensity);
        this.meshStage = new Stage<>("mesh", generationExecutor, this::buildMesh);
        this.gpuStage = new Stage<>("gpu-upload", callbackExecutor, this::uploadToGpu);
    }

    /**
     * Generate a chunk through the full pipeline.
     */
    public CompletableFuture<ChunkMaterializer.MaterializedChunk> generateChunk(ChunkKey key) {
        return densityStage.process(key)
            .thenCompose(density -> meshStage.process(density))
            .thenCompose(mesh -> gpuStage.process(mesh));
    }

    /**
     * Generate multiple chunks in parallel.
     */
    public CompletableFuture<Void> generateChunks(Iterable<ChunkKey> keys, Consumer<ChunkMaterializer.MaterializedChunk> onComplete) {
        var futures = new java.util.ArrayList<CompletableFuture<ChunkMaterializer.MaterializedChunk>>();
        for (ChunkKey key : keys) {
            futures.add(generateChunk(key).thenAccept(onComplete));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private DensityGraph generateDensity(ChunkKey key) {
        return densityCache.getOrCompute(key.asLong(), k -> {
            long start = System.nanoTime();
            try {
                DensityField field = new DensityField(terrainEngine.noise, terrainEngine.options);
                return field.sampleChunk(key.chunkX() * 16, key.chunkZ() * 16);
            } finally {
                EngineMetrics.recordTerrainGenTime(System.nanoTime() - start);
            }
        }, estimateDensitySize());
    }

    private ChunkMaterializer.MeshData buildMesh(DensityGraph density) {
        // Mesh building from density graph
        // This would use marching cubes / dual contouring
        return new ChunkMaterializer.MeshData(new float[0], new int[0], new byte[0], new short[0]);
    }

    private ChunkMaterializer.MaterializedChunk uploadToGpu(ChunkMaterializer.MeshData mesh) {
        // GPU upload - runs on callback executor (render thread)
        return null; // Placeholder
    }

    private long estimateDensitySize() {
        // Rough estimate: 32x32x32 density values * 4 bytes
        return 32 * 32 * 32 * 4;
    }

    public void shutdown() {
        generationExecutor.shutdown();
        callbackExecutor.shutdown();
    }

    /**
     * Pipeline stage with async execution.
     */
    private static class Stage<I, O> {
        private final String name;
        private final Executor executor;
        private final java.util.function.Function<I, O> processor;

        Stage(String name, Executor executor, java.util.function.Function<I, O> processor) {
            this.name = name;
            this.executor = executor;
            this.processor = processor;
        }

        CompletableFuture<O> process(I input) {
            return CompletableFuture.supplyAsync(() -> processor.apply(input), executor);
        }
    }

    public record ChunkKey(int chunkX, int chunkZ, int dimension) {
        public long asLong() {
            return ((long) chunkX << 42) | ((long) (chunkZ & 0x3FFFFF) << 20) | (dimension & 0xFFFFF);
        }
    }
}