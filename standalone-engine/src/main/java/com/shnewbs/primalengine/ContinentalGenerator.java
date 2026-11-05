package com.shnewbs.primalengine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.primalengine.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/** Own column construction and decoration. Minecraft supplies chunk storage and scheduling only. */
public final class ContinentalGenerator extends ChunkGenerator {
  public static final MapCodec<ContinentalGenerator> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource))
                  .apply(i, ContinentalGenerator::new));
  private volatile ContinentalWorld world = new ContinentalWorld(0);
  private volatile SeedField scatter = new SeedField(0);

  public ContinentalGenerator(BiomeSource biomes) {
    super(biomes);
  }

  public ContinentalWorld geography() {
    return world;
  }

  protected MapCodec<? extends ChunkGenerator> codec() {
    return CODEC;
  }

  @Override
  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> sets, RandomState random, long seed) {
    world = new ContinentalWorld(seed);
    scatter = new SeedField(seed);
    if (biomeSource instanceof RegionalBiomes b) b.bind(world);
    return ChunkGeneratorStructureState.createForFlat(
        random, seed, biomeSource, java.util.stream.Stream.empty());
  }

  public int getMinY() {
    return ContinentalWorld.MIN_Y;
  }

  public int getGenDepth() {
    return ContinentalWorld.HEIGHT;
  }

  public int getSeaLevel() {
    return ContinentalWorld.SEA;
  }

  public void applyCarvers(
      WorldGenRegion r,
      long seed,
      RandomState s,
      BiomeManager b,
      StructureManager m,
      ChunkAccess c) {}

  public void buildSurface(WorldGenRegion r, StructureManager m, RandomState s, ChunkAccess c) {}

  public void spawnOriginalMobs(WorldGenRegion r) {}

  @Override
  public void applyBiomeDecoration(WorldGenLevel l, ChunkAccess c, StructureManager m) {}

  public int getBaseHeight(
      int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state) {
    var s = world.sample(x, z);
    return Math.min(
        level.getMaxY(),
        (int)
                Math.floor(
                    type == Heightmap.Types.OCEAN_FLOOR_WG || type == Heightmap.Types.OCEAN_FLOOR
                        ? s.ground()
                        : Math.max(
                            s.ground(),
                            Double.isNaN(s.water()) ? s.ground() : Math.ceil(s.water()) - 1))
            + 1);
  }

  private BlockState at(ContinentalWorld.Sample s, int y) {
    int bed = (int) Math.floor(s.ground());
    if (y <= getMinY() + 1) return Blocks.BEDROCK.defaultBlockState();
    if (y <= bed) {
      if (y < bed - 4) return (y < 0 ? Blocks.DEEPSLATE : Blocks.STONE).defaultBlockState();
      if (s.ocean() || bed <= getSeaLevel() + 3 || s.rain() < .4)
        return Blocks.SAND.defaultBlockState();
      if (!Double.isNaN(s.water())) return Blocks.GRAVEL.defaultBlockState();
      if (bed > 300) return Blocks.STONE.defaultBlockState();
      return (y == bed ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState();
    }
    if (!Double.isNaN(s.water()) && y < s.water()) {
      int units = (int) Math.clamp(Math.round((s.water() - y) * 16), 1, 16);
      int direction =
          Math.abs(s.flowX()) + Math.abs(s.flowZ()) < .01
              ? 16
              : Math.floorMod((int) Math.round(Math.atan2(s.flowZ(), s.flowX()) * 8 / Math.PI), 16);
      return PrimalEngine.WATER_BLOCK
          .get()
          .defaultBlockState()
          .setValue(EngineFluid.UNITS, units)
          .setValue(EngineFluid.FLOW, direction);
    }
    return Blocks.AIR.defaultBlockState();
  }

  public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState state) {
    var s = world.sample(x, z);
    BlockState[] blocks = new BlockState[level.getHeight()];
    for (int j = 0; j < blocks.length; j++) blocks[j] = at(s, level.getMinY() + j);
    return new NoiseColumn(level.getMinY(), blocks);
  }

  public CompletableFuture<ChunkAccess> fillFromNoise(
      Blender ignored, RandomState rs, StructureManager manager, ChunkAccess chunk) {
    var p = new BlockPos.MutableBlockPos();
    var floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    var surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    int ox = chunk.getPos().getMinBlockX(), oz = chunk.getPos().getMinBlockZ();
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        var s = world.sample(ox + x, oz + z);
        int max =
            (int) Math.ceil(Math.max(s.ground(), Double.isNaN(s.water()) ? s.ground() : s.water()));
        for (int y = chunk.getMinY(); y <= Math.min(max, chunk.getMaxY() - 1); y++) {
          BlockState b = at(s, y);
          if (b.isAir()) continue;
          chunk.setBlockState(p.set(x, y, z), b);
          floor.update(x, y, z, b);
          surface.update(x, y, z, b);
        }
      }
    decorate(chunk, ox, oz);
    return CompletableFuture.completedFuture(chunk);
  }

  private void decorate(ChunkAccess chunk, int ox, int oz) {
    // Each chunk reconstructs the intersecting part of nearby trees, never writing into neighbors.
    for (int gz = Math.floorDiv(oz - 4, 24); gz <= Math.floorDiv(oz + 19, 24); gz++)
      for (int gx = Math.floorDiv(ox - 4, 24); gx <= Math.floorDiv(ox + 19, 24); gx++) {
        int x = gx * 24 + 4 + (int) (scatter.at(gx, gz, 201) * 16),
            z = gz * 24 + 4 + (int) (scatter.at(gx, gz, 202) * 16);
        var s = world.sample(x, z);
        if (s.ocean() || !Double.isNaN(s.water()) || s.rain() < .55 || s.ground() > 300) continue;
        int y = (int) Math.floor(s.ground()) + 1;
        if (Math.abs(world.sample(x + 3, z).ground() - s.ground()) > 2
            || Math.abs(world.sample(x, z + 3).ground() - s.ground()) > 2) continue;
        for (int dz = -3; dz <= 3; dz++)
          for (int dx = -3; dx <= 3; dx++)
            for (int dy = 2; dy <= 7; dy++) {
              if (x + dx < ox || x + dx > ox + 15 || z + dz < oz || z + dz > oz + 15) continue;
              var pos = new BlockPos(x + dx, y + dy, z + dz);
              boolean trunk = dx == 0 && dz == 0 && dy < 6;
              boolean leaf = dx * dx + dz * dz + Math.pow((dy - 4.5) * 1.3, 2) < 11;
              if (chunk.getBlockState(pos).isAir() && (trunk || leaf))
                chunk.setBlockState(
                    pos,
                    (trunk
                        ? Blocks.OAK_LOG.defaultBlockState()
                        : Blocks.OAK_LEAVES
                            .defaultBlockState()
                            .setValue(
                                net.minecraft.world.level.block.state.properties
                                    .BlockStateProperties.PERSISTENT,
                                true)));
            }
        if (x >= ox && x <= ox + 15 && z >= oz && z <= oz + 15)
          for (int dy = 0; dy < 2; dy++)
            chunk.setBlockState(new BlockPos(x, y + dy, z), Blocks.OAK_LOG.defaultBlockState());
      }
  }

  public void addDebugScreenInfo(List<String> lines, RandomState rs, BlockPos p) {
    var s = world.sample(p.getX(), p.getZ());
    lines.add(
        "Primal Engine v1: own continental generator; bed="
            + (int) s.ground()
            + " water="
            + s.water());
  }
}
