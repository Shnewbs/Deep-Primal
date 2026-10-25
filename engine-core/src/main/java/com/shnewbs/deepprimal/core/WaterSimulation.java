package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * Finite water simulation with pressure-based flow.
 * Deterministic, frame-rate independent, and suitable for multi-threaded execution.
 */
public final class WaterSimulation {
    public static final int CHUNK_SIZE = 16;
    public static final int CELL_COUNT = CHUNK_SIZE * CHUNK_SIZE;
    
    // Physics constants
    public static final double GRAVITY = 9.81;
    public static final double WATER_DENSITY = 1000.0; // kg/m^3
    public static final double MAX_FLOW_SPEED = 2.0; // m/tick
    public static final double MIN_FLOW_THRESHOLD = 0.01;
    public static final double EVAPORATION_RATE = 0.001; // per tick
    public static final double INFILTRATION_RATE = 0.0005; // per tick
    
    // Simulation state
    private final double[] waterLevel; // height in blocks
    private final double[] flowX;
    private final double[] flowZ;
    private final double[] pressure;
    private final boolean[] isSource;
    private final byte[] material; // 0=air, 1=water, 2=lava, etc.
    
    // Boundaries
    private final double[] neighborLevel; // 4 neighbors: -x, +x, -z, +z
    private final boolean[] hasNeighbor;
    
    // Statistics
    public final AtomicLong simulationTimeNs = new AtomicLong(0);
    public final AtomicLong totalWaterVolume = new AtomicLong(0);
    public final AtomicLong activeCells = new AtomicLong(0);

    public WaterSimulation() {
        this.waterLevel = new double[CELL_COUNT];
        this.flowX = new double[CELL_COUNT];
        this.flowZ = new double[CELL_COUNT];
        this.pressure = new double[CELL_COUNT];
        this.isSource = new boolean[CELL_COUNT];
        this.material = new byte[CELL_COUNT];
        this.neighborLevel = new double[4];
        this.hasNeighbor = new boolean[4];
        
        Arrays.fill(waterLevel, 0.0);
        Arrays.fill(flowX, 0.0);
        Arrays.fill(flowZ, 0.0);
        Arrays.fill(pressure, 0.0);
    }

    /**
     * Simulate one tick of water physics.
     * @param dt Time delta in ticks (normally 1.0)
     * @param getGroundHeight Function to query ground height at (x, z)
     */
    public void tick(double dt, DoubleSupplier getGroundHeight) {
        long start = System.nanoTime();
        
        // Reset flow accumulators
        Arrays.fill(flowX, 0.0);
        Arrays.fill(flowZ, 0.0);
        
        // Compute pressure for each cell
        computePressure(getGroundHeight);
        
        // Compute flows between cells
        computeFlows(dt);
        
        // Apply flows
        applyFlows(dt);
        
        // Handle evaporation and infiltration
        applyEnvironmentalLosses(dt);
        
        // Update statistics
        updateStatistics();
        
        simulationTimeNs.addAndGet(System.nanoTime() - start);
    }

    private void computePressure(DoubleSupplier getGroundHeight) {
        int index = 0;
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++, index++) {
                if (material[index] != 1) { // Not water
                    pressure[index] = 0.0;
                    continue;
                }
                
                double level = waterLevel[index];
                if (level <= 0.0) {
                    pressure[index] = 0.0;
                    continue;
                }
                
                // Pressure = rho * g * h (hydrostatic)
                // Plus dynamic pressure from flow
                double groundHeight = getGroundHeight.getAsDouble(); // Would need x,z params
                double depth = level - groundHeight;
                pressure[index] = WATER_DENSITY * GRAVITY * Math.max(0, depth);
                
                // Add dynamic pressure
                double flowSpeed = Math.hypot(flowX[index], flowZ[index]);
                pressure[index] += 0.5 * WATER_DENSITY * flowSpeed * flowSpeed;
            }
        }
    }

    private void computeFlows(double dt) {
        // Internal flows (cell to cell within chunk)
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                int idx = z * CHUNK_SIZE + x;
                if (material[idx] != 1 || waterLevel[idx] <= MIN_FLOW_THRESHOLD) continue;
                
                // Flow to +X neighbor
                if (x < CHUNK_SIZE - 1) {
                    int idxX = z * CHUNK_SIZE + (x + 1);
                    computeFlow(idx, idxX, 1.0, 0.0, dt);
                }
                
                // Flow to +Z neighbor
                if (z < CHUNK_SIZE - 1) {
                    int idxZ = (z + 1) * CHUNK_SIZE + x;
                    computeFlow(idx, idxZ, 0.0, 1.0, dt);
                }
            }
        }
        
        // Boundary flows (to/from neighboring chunks)
        // This would be handled by the chunk boundary system
    }

    private void computeFlow(int fromIdx, int toIdx, double dirX, double dirZ, double dt) {
        if (material[toIdx] != 1) return; // Can't flow into non-water
        
        double pressureDiff = pressure[fromIdx] - pressure[toIdx];
        if (Math.abs(pressureDiff) < MIN_FLOW_THRESHOLD) return;
        
        // Flow rate proportional to pressure difference
        double flowRate = pressureDiff * 0.001 * dt; // Tuned coefficient
        
        // Limit by available water
        double availableWater = waterLevel[fromIdx] * 0.1; // Max 10% per tick
        flowRate = Math.min(flowRate, availableWater);
        
        // Limit by max speed
        double maxFlow = MAX_FLOW_SPEED * dt;
        flowRate = Math.min(flowRate, maxFlow);
        
        // Accumulate flow
        flowX[fromIdx] -= flowRate * dirX;
        flowZ[fromIdx] -= flowRate * dirZ;
        flowX[toIdx] += flowRate * dirX;
        flowZ[toIdx] += flowRate * dirZ;
    }

    private void applyFlows(double dt) {
        for (int i = 0; i < CELL_COUNT; i++) {
            if (material[i] != 1) continue;
            
            double netFlowX = flowX[i];
            double netFlowZ = flowZ[i];
            
            // Distribute outflow to neighbors (simplified)
            // In reality, this would be a proper divergence calculation
            waterLevel[i] -= (Math.abs(netFlowX) + Math.abs(netFlowZ)) * 0.5;
            waterLevel[i] = Math.max(0, waterLevel[i]);
        }
    }

    private void applyEnvironmentalLosses(double dt) {
        for (int i = 0; i < CELL_COUNT; i++) {
            if (material[i] != 1) continue;
            
            double level = waterLevel[i];
            if (level <= 0) continue;
            
            // Evaporation
            double evaporation = level * EVAPORATION_RATE * dt;
            
            // Infiltration (into ground)
            double infiltration = level * INFILTRATION_RATE * dt;
            
            waterLevel[i] = Math.max(0, level - evaporation - infiltration);
        }
    }

    private void updateStatistics() {
        long volume = 0;
        long active = 0;
        for (int i = 0; i < CELL_COUNT; i++) {
            if (material[i] == 1 && waterLevel[i] > 0) {
                volume += (long) (waterLevel[i] * 1000); // mL
                active++;
            }
        }
        totalWaterVolume.set(volume);
        activeCells.set(active);
    }

    /**
     * Set water level at a cell (for source blocks, rain, etc.)
     */
    public void setWaterLevel(int x, int z, double level, boolean source) {
        int idx = z * CHUNK_SIZE + x;
        waterLevel[idx] = Math.max(0, level);
        isSource[idx] = source;
        material[idx] = 1;
    }

    /**
     * Add water to a cell (rain, bucket, etc.)
     */
    public void addWater(int x, int z, double amount) {
        int idx = z * CHUNK_SIZE + x;
        waterLevel[idx] += amount;
        material[idx] = 1;
    }

    /**
     * Remove water from a cell (bucket, pump, etc.)
     */
    public double removeWater(int x, int z, double amount) {
        int idx = z * CHUNK_SIZE + x;
        double available = waterLevel[idx];
        double removed = Math.min(amount, available);
        waterLevel[idx] -= removed;
        if (waterLevel[idx] <= 0) {
            waterLevel[idx] = 0;
            material[idx] = 0;
            isSource[idx] = false;
        }
        return removed;
    }

    public double getWaterLevel(int x, int z) {
        return waterLevel[z * CHUNK_SIZE + x];
    }

    public double getFlowX(int x, int z) {
        return flowX[z * CHUNK_SIZE + x];
    }

    public double getFlowZ(int x, int z) {
        return flowZ[z * CHUNK_SIZE + x];
    }

    public double getPressure(int x, int z) {
        return pressure[z * CHUNK_SIZE + x];
    }

    public boolean isSource(int x, int z) {
        return isSource[z * CHUNK_SIZE + x];
    }

    public void setMaterial(int x, int z, byte materialType) {
        material[z * CHUNK_SIZE + x] = materialType;
    }

    public byte getMaterial(int x, int z) {
        return material[z * CHUNK_SIZE + x];
    }

    /**
     * Serialize to WaterCornerPacket format for network/renderer.
     */
    public void encodeToPacket(java.nio.ByteBuffer buffer, int offset, long timestamp) {
        // Pack 4 corners (simplified - real implementation would encode all cells)
        // This is a placeholder for the actual packet encoding
        for (int corner = 0; corner < 4; corner++) {
            int x = (corner % 2) * (CHUNK_SIZE - 1);
            int z = (corner / 2) * (CHUNK_SIZE - 1);
            int idx = z * CHUNK_SIZE + x;
            
            int target = material[idx] == 1 ? 1 : 0;
            int source = isSource[idx] ? 1 : 0;
            int age = (int) (timestamp & 0xFFFF);
            
            WaterCornerPacket.write(buffer, offset + corner * 36,
                0, target, source, age, age, false);
        }
    }

    /**
     * Create a WaterSimulation from chunk data.
     */
    public static WaterSimulation fromChunkData(double[] waterLevels, boolean[] sources, byte[] materials) {
        WaterSimulation sim = new WaterSimulation();
        for (int i = 0; i < CELL_COUNT; i++) {
            sim.waterLevel[i] = waterLevels[i];
            sim.isSource[i] = sources[i];
            sim.material[i] = materials[i];
        }
        return sim;
    }

    /**
     * Get simulation statistics.
     */
    public record Stats(
        long simulationTimeUs,
        long totalWaterVolumeMl,
        long activeCells,
        double avgWaterLevel
    ) {}

    public Stats getStats() {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < CELL_COUNT; i++) {
            if (material[i] == 1 && waterLevel[i] > 0) {
                sum += waterLevel[i];
                count++;
            }
        }
        return new Stats(
            simulationTimeNs.get() / 1000,
            totalWaterVolume.get(),
            activeCells.get(),
            count > 0 ? sum / count : 0
        );
    }

    public void resetStats() {
        simulationTimeNs.set(0);
        totalWaterVolume.set(0);
        activeCells.set(0);
    }
}