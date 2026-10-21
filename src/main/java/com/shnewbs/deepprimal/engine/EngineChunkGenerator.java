package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.TerrainEngine;
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

/**
 * Full original Overworld adapter; no NoiseBasedChunkGenerator, native carvers, features or
 * structures.
 */
public final class EngineChunkGenerator extends ChunkGenerator {
  public static final MapCodec<EngineChunkGenerator> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(
                      BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
                      Codec.INT.optionalFieldOf("min_y", -128).forGetter(g -> g.minY),
                      Codec.INT.optionalFieldOf("height", 768).forGetter(g -> g.height))
                  .apply(i, EngineChunkGenerator::new));
  private final int minY, height;
  private volatile TerrainEngine engine = new TerrainEngine(0);

  public EngineChunkGenerator(BiomeSource source, int minY, int height) {
    super(source);
    if (minY % 16 != 0 || height % 16 != 0 || height < 256 || height > 2048)
      throw new IllegalArgumentException("Engine height");
    this.minY = minY;
    this.height = height;
  }

  public TerrainEngine geography() {
    return engine;
  }

  protected MapCodec<? extends ChunkGenerator> codec() {
    return CODEC;
  }

  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> sets, RandomState random, long seed) {
    engine = new TerrainEngine(seed);
    if (biomeSource instanceof RegionBiomeSource b) b.bind(engine);
    return ChunkGeneratorStructureState.createForFlat(
        random, seed, biomeSource, java.util.stream.Stream.empty());
  }

  public int getMinY() {
    return minY;
  }

  public int getGenDepth() {
    return height;
  }

  public int getSeaLevel() {
    return TerrainEngine.SEA;
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

  public void applyBiomeDecoration(WorldGenLevel l, ChunkAccess c, StructureManager m) {}

  public void addDebugScreenInfo(List<String> lines, RandomState s, BlockPos p) {
    var a = engine.sample(p.getX(), p.getZ());
    lines.add(
        "Deep Primal continental_v1 | drainage regions "
            + engine.cachedRegions()
            + " | slope "
            + String.format(java.util.Locale.ROOT, "%.2f", a.slope()));
  }

  public int getBaseHeight(
      int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
    var s = engine.sample(x, z);
    return Math.min(
        level.getMaxY(),
        (int)
                Math.floor(
                    type == Heightmap.Types.OCEAN_FLOOR_WG || type == Heightmap.Types.OCEAN_FLOOR
                        ? s.ground()
                        : Math.max(s.ground(), s.wet() ? Math.ceil(s.water()) - 1 : s.ground()))
            + 1);
  }

  private BlockState solid(TerrainEngine.Sample s, int y) {
    int bed = (int) Math.floor(s.ground());
    if (y < minY + 2) return Blocks.BEDROCK.defaultBlockState();
    if (y < bed - 4)
      return (y < 0
              ? Blocks.DEEPSLATE
              : s.rock() == 0
                  ? Blocks.GRANITE
                  : s.rock() == 1 ? Blocks.ANDESITE : s.rock() == 2 ? Blocks.STONE : Blocks.DIORITE)
          .defaultBlockState();
    if (s.ground() < getSeaLevel() + 4 || s.rain() < .32)
      return (y < bed - 2 ? Blocks.SANDSTONE : Blocks.SAND).defaultBlockState();
    if (s.wet() || s.sediment() > .65)
      return (s.sediment() > .8 ? Blocks.CLAY : Blocks.GRAVEL).defaultBlockState();
    if (s.slope() > .95 || bed > 270)
      return (s.rock() == 0 ? Blocks.GRANITE : Blocks.STONE).defaultBlockState();
    if (s.temperature() < .16 && y == bed) return Blocks.SNOW_BLOCK.defaultBlockState();
    return (y == bed ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState();
  }

  private BlockState at(TerrainEngine.Sample s, int x, int y, int z) {
    if (y <= (int) Math.floor(s.ground()))
      return !s.wet() && engine.cave(x, y, z, s.ground(), minY)
          ? Blocks.AIR.defaultBlockState()
          : solid(s, y);
    if (s.wet() && y < s.water()) {
      int direction =
          Math.abs(s.flowX()) + Math.abs(s.flowZ()) < .01
              ? 16
              : Math.floorMod((int) Math.round(Math.atan2(s.flowZ(), s.flowX()) * 8 / Math.PI), 16);
      return EngineRegistry.WATER_BLOCK
          .get()
          .defaultBlockState()
          .setValue(EngineFluid.UNITS, (int) Math.clamp(Math.round((s.water() - y) * 16), 1, 16))
          .setValue(EngineFluid.FLOW, direction)
          .setValue(EngineFluid.SPEED, (int) Math.clamp(Math.round((s.speed() - .1) * 10), 0, 7));
    }
    return Blocks.AIR.defaultBlockState();
  }

  public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor l, RandomState r) {
    var s = engine.sample(x, z);
    BlockState[] a = new BlockState[l.getHeight()];
    for (int j = 0; j < a.length; j++) a[j] = at(s, x, l.getMinY() + j, z);
    return new NoiseColumn(l.getMinY(), a);
  }

  public CompletableFuture<ChunkAccess> fillFromNoise(
      Blender b, RandomState r, StructureManager m, ChunkAccess c) {
    var pos = new BlockPos.MutableBlockPos();
    var floor = c.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    var top = c.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    int ox = c.getPos().getMinBlockX(), oz = c.getPos().getMinBlockZ();
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        var s = engine.sample(ox + x, oz + z);
        int max =
            Math.min(
                c.getMaxY() - 1,
                (int) Math.ceil(Math.max(s.ground(), s.wet() ? s.water() : s.ground())));
        for (int y = c.getMinY(); y <= max; y++) {
          BlockState state = at(s, ox + x, y, oz + z);
          if (!state.isAir()) {
            c.setBlockState(pos.set(ox + x, y, oz + z), state);
            floor.update(x, y, z, state);
            top.update(x, y, z, state);
          }
        }
      }
    decorate(c, ox, oz);
    return CompletableFuture.completedFuture(c);
  }

  private void decorate(ChunkAccess c, int ox, int oz) {
    for (int gz = Math.floorDiv(oz - 6, 8); gz <= Math.floorDiv(oz + 21, 8); gz++)
      for (int gx = Math.floorDiv(ox - 6, 8); gx <= Math.floorDiv(ox + 21, 8); gx++) {
        int x = gx * 8 + 1 + (int) (engine.noise.hash(gx, gz, 311) * 6),
            z = gz * 8 + 1 + (int) (engine.noise.hash(gx, gz, 312) * 6);
        var s = engine.sample(x, z);
        double density = engine.forest(x, z);
        if (s.wet()
            || s.ground() < getSeaLevel() + 5
            || s.slope() > .8
            || s.temperature() < .1
            || s.rain() < .38
            || engine.noise.hash(gx, gz, 313) > Math.max(0, density - .20) * .66) continue;
        int y = (int) Math.floor(s.ground()) + 1,
            h = 5 + (int) (engine.noise.hash(gx, gz, 314) * 7),
            radius = 2 + (int) (engine.noise.hash(gx, gz, 315) * 2);
        boolean pine = s.temperature() < .36;
        Block log =
            pine
                ? Blocks.SPRUCE_LOG
                : engine.noise.hash(gx, gz, 316) > .75 ? Blocks.BIRCH_LOG : Blocks.OAK_LOG;
        Block leaves =
            pine
                ? Blocks.SPRUCE_LEAVES
                : log == Blocks.BIRCH_LOG ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES;
        for (int dz = -radius; dz <= radius; dz++)
          for (int dx = -radius; dx <= radius; dx++)
            for (int dy = 0; dy <= h + 2; dy++) {
              int wx = x + dx, wz = z + dz;
              if (wx < ox || wz < oz || wx > ox + 15 || wz > oz + 15) continue;
              boolean trunk = dx == 0 && dz == 0 && dy < h;
              double canopy = pine ? Math.max(.5, (h + 2 - dy) * .32) : radius;
              boolean leaf =
                  dy >= (pine ? 3 : h - 3)
                      && dx * dx + dz * dz + (pine ? 0 : Math.pow((dy - h + .5) * .9, 2))
                          <= canopy * canopy;
              var p = new BlockPos(wx, y + dy, wz);
              if (trunk || leaf) {
                if (c.getBlockState(p).isAir())
                  c.setBlockState(
                      p,
                      trunk
                          ? log.defaultBlockState()
                          : leaves
                              .defaultBlockState()
                              .setValue(
                                  net.minecraft.world.level.block.state.properties
                                      .BlockStateProperties.PERSISTENT,
                                  true));
              }
            }
      }
    // Ground cover follows moisture and patch density, rather than one object per regular cell.
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++)
        if (engine.noise.hash(ox + x, oz + z, 401) < .11) {
          var s = engine.sample(ox + x, oz + z);
          int y = (int) Math.floor(s.ground()) + 1;
          var p = new BlockPos(ox + x, y, oz + z);
          if (!s.wet()
              && c.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)
              && c.getBlockState(p).isAir())
            c.setBlockState(p, Blocks.SHORT_GRASS.defaultBlockState());
        }
    Heightmap.primeHeightmaps(
        c, java.util.EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
  }
}
