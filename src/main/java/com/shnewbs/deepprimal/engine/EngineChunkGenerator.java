package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.core.Geology;
import com.shnewbs.deepprimal.core.DepositField;
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
  public static final MapCodec<EngineChunkGenerator> CODEC = codecFor(1);
  public static final MapCodec<EngineChunkGenerator> CODEC_V2 = codecFor(2);
  public static final MapCodec<EngineChunkGenerator> CODEC_V3 = codecFor(3);
  public static final MapCodec<EngineChunkGenerator> CODEC_V4 = codecFor(4);
  public static final MapCodec<EngineChunkGenerator> CODEC_V5 = codecFor(5);

  public static final MapCodec<EngineChunkGenerator> CODEC_V6 = codecFor(6);
  private static MapCodec<EngineChunkGenerator> codecFor(int revision) {
    if(revision==6) return RecordCodecBuilder.mapCodec(i->i.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter((EngineChunkGenerator g)->g.biomeSource),
        Codec.INT.optionalFieldOf("min_y",-128).forGetter(g->g.minY),
        Codec.INT.optionalFieldOf("height",768).forGetter(g->g.height),
        TerrainProfile.CODEC.optionalFieldOf("terrain").forGetter(g->Optional.of(g.profile))
    ).apply(i,(source,min,height,profile)->new EngineChunkGenerator(source,min,height,6,profile.orElseGet(TerrainConfig::creationProfile))));
    return RecordCodecBuilder.mapCodec(
        i ->
            i.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
                    Codec.INT.optionalFieldOf("min_y", -128).forGetter(g -> g.minY),
                    Codec.INT.optionalFieldOf("height", 768).forGetter(g -> g.height))
                .apply(
                    i,
                    (source, min, height) ->
                        new EngineChunkGenerator(source, min, height, revision)));
  }

  private final int minY, height, revision;
  private volatile TerrainEngine engine;
  private final TerrainProfile profile;
  private final List<BlockState> orePalette;
  private volatile DepositPlan depositPlan;

  public EngineChunkGenerator(BiomeSource source, int minY, int height) {
    this(source, minY, height, 1);
  }

  private EngineChunkGenerator(BiomeSource source, int minY, int height, int revision) {
    this(source,minY,height,revision,null);
  }
  private EngineChunkGenerator(BiomeSource source, int minY, int height, int revision,TerrainProfile profile) {
    super(source);
    this.profile=profile;
    orePalette=profile==null?List.of():profile.palette();
    this.revision = revision;
    engine = new TerrainEngine(0, revision, profile==null?TerrainOptions.DEFAULT:profile.options());
    if(profile!=null)depositPlan=profile.plan(engine.noise);
    if (minY % 16 != 0 || height % 16 != 0 || height < 256 || height > 2048)
      throw new IllegalArgumentException("Engine height");
    this.minY = minY;
    this.height = height;
  }

  public TerrainEngine geography() {
    return engine;
  }

  protected MapCodec<? extends ChunkGenerator> codec() {
    return switch (revision) {
      case 1 -> CODEC;
      case 2 -> CODEC_V2;
      case 3 -> CODEC_V3;
      case 4 -> CODEC_V4;
      case 5 -> CODEC_V5;
      default -> CODEC_V6;
    };
  }

  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> sets, RandomState random, long seed) {
    engine = new TerrainEngine(seed, revision, profile==null?TerrainOptions.DEFAULT:profile.options());
    if(profile!=null)depositPlan=profile.plan(engine.noise);
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
    var a = surfaceSample(p.getX(), p.getZ());
    lines.add(
        "Deep Primal continental_v"
            + revision
            + " | drainage regions "
            + engine.cachedRegions()
            + " | slope "
            + String.format(java.util.Locale.ROOT, "%.2f", a.slope()));
  }

  public int getBaseHeight(
      int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
    var s = engine.column(x, z);
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
    if (revision >= 3) return surfaceV3(s, y, bed);
    if (s.ground() < getSeaLevel() + 4 || s.rain() < .32)
      return (y < bed - 2 ? Blocks.SANDSTONE : Blocks.SAND).defaultBlockState();
    if (s.wet() || s.sediment() > .65)
      return (s.sediment() > .8 ? Blocks.CLAY : Blocks.GRAVEL).defaultBlockState();
    if (s.slope() > .95 || bed > 270)
      return (s.rock() == 0 ? Blocks.GRANITE : Blocks.STONE).defaultBlockState();
    if (s.temperature() < .16 && y == bed) return Blocks.SNOW_BLOCK.defaultBlockState();
    return (y == bed ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState();
  }

  private BlockState solidV4(TerrainEngine.Sample s, Geology.Column g, DepositField.Column deposits, int y) {
    int bed = (int) Math.floor(s.ground());
    if (y < minY + 2) return Blocks.BEDROCK.defaultBlockState();
    if (y > bed - g.soilDepth()) return surfaceV3(s, y, bed);
    var ore = deposits == null ? g.ore(y, bed) : deposits.ore(y, bed);
    boolean deep = y < 0;
    if (ore != Geology.Ore.NONE) return switch (ore) {
      case COAL -> (deep ? Blocks.DEEPSLATE_COAL_ORE : Blocks.COAL_ORE).defaultBlockState();
      case COPPER -> (deep ? Blocks.DEEPSLATE_COPPER_ORE : Blocks.COPPER_ORE).defaultBlockState();
      case IRON -> (deep ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE).defaultBlockState();
      case GOLD -> (deep ? Blocks.DEEPSLATE_GOLD_ORE : Blocks.GOLD_ORE).defaultBlockState();
      case REDSTONE -> (deep ? Blocks.DEEPSLATE_REDSTONE_ORE : Blocks.REDSTONE_ORE).defaultBlockState();
      case LAPIS -> (deep ? Blocks.DEEPSLATE_LAPIS_ORE : Blocks.LAPIS_ORE).defaultBlockState();
      case DIAMOND -> (deep ? Blocks.DEEPSLATE_DIAMOND_ORE : Blocks.DIAMOND_ORE).defaultBlockState();
      case EMERALD -> (deep ? Blocks.DEEPSLATE_EMERALD_ORE : Blocks.EMERALD_ORE).defaultBlockState();
      default -> throw new AssertionError(ore);
    };
    return switch (g.rock(y)) {
      case GRANITE -> Blocks.GRANITE.defaultBlockState();
      case ANDESITE -> Blocks.ANDESITE.defaultBlockState();
      case DIORITE -> Blocks.DIORITE.defaultBlockState();
      case BASALT -> Blocks.BASALT.defaultBlockState();
      case TUFF -> Blocks.TUFF.defaultBlockState();
      case SANDSTONE -> Blocks.SANDSTONE.defaultBlockState();
      case CALCITE -> Blocks.CALCITE.defaultBlockState();
      case DEEPSLATE -> Blocks.DEEPSLATE.defaultBlockState();
    };
  }


  public TerrainProfile terrainProfile() { return profile; }
  private static BlockState rock(Geology.Rock rock) {
    return (switch(rock) {
      case GRANITE -> Blocks.GRANITE;case ANDESITE -> Blocks.ANDESITE;case DIORITE -> Blocks.DIORITE;
      case BASALT -> Blocks.BASALT;case TUFF -> Blocks.TUFF;case SANDSTONE -> Blocks.SANDSTONE;
      case CALCITE -> Blocks.CALCITE;case DEEPSLATE -> Blocks.DEEPSLATE;
    }).defaultBlockState();
  }
  private BlockState water(TerrainEngine.Sample s,int y,boolean ocean) {
    int direction=Math.abs(s.flowX())+Math.abs(s.flowZ())<.01?16:Math.floorMod((int)Math.round(Math.atan2(s.flowZ(),s.flowX())*8/Math.PI),16);
    return EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineWaterBlock.OCEAN,ocean)
        .setValue(EngineFluid.UNITS,(int)Math.clamp(Math.round((s.water()-y)*16),1,16)).setValue(EngineFluid.FLOW,direction)
        .setValue(EngineFluid.SPEED,(int)Math.clamp(Math.round((s.speed()-.1)*10),0,7));
  }
  private final class V6Column {
    final int x,z,bed,iceBase;final boolean tunnel,ocean;
    final TerrainEngine.Sample s,hydraulic;final Geology.Column geology;
    final SoilProfile soil;final CaveNetwork.Column cave;final DepositPlan.Column deposits;
    V6Column(int x,int z,TerrainEngine.Sample s,DepositPlan.Chunk deposits) {
      this.x=x;this.z=z;this.s=s;bed=(int)Math.floor(s.ground());
      hydraulic=engine.hydraulicColumn(x,z);double uncut=engine.raw(x,z);
      tunnel=engine.tunnel(x,z);ocean=engine.ocean(x,z);
      iceBase=(int)Math.floor(engine.surfaceWithoutIce(x,z));
      geology=new Geology(engine.noise).column(x,z,s);soil=SoilProfile.at(s,geology);
      this.deposits=deposits.column(x,z,geology,iceBase);
      double roof=Math.min(iceBase,hydraulic.ground());
      cave=profile.options().caves()?new CaveNetwork(engine.noise).column(x,z,roof,minY,minY+height-1,geology):null;
    }
    BlockState at(int y) {
      if(y<minY+2)return Blocks.BEDROCK.defaultBlockState();
      if(tunnel && y>Math.floor(hydraulic.ground()) && y<=Math.ceil(hydraulic.water())+2)
        return y<hydraulic.water()?water(hydraulic,y,false):Blocks.AIR.defaultBlockState();
      if(y<=bed) {
        if(y>iceBase)return (y==bed?Blocks.SNOW_BLOCK:y<bed-4?Blocks.BLUE_ICE:Blocks.PACKED_ICE).defaultBlockState();
        if(cave!=null && cave.contains(y))
          return cave.flooded(y)?EngineRegistry.WATER_BLOCK.get().defaultBlockState():Blocks.AIR.defaultBlockState();
        int depth=bed-y;
        if(depth<soil.depth()) {
          if(s.wet())return (soil.kind()==SoilProfile.Kind.CLAY?Blocks.CLAY:soil.kind()==SoilProfile.Kind.GRAVEL?Blocks.GRAVEL:Blocks.SAND).defaultBlockState();
          if(soil.kind()==SoilProfile.Kind.SAND)return (depth<soil.topsoil()+soil.subsoil()?Blocks.SAND:Blocks.SANDSTONE).defaultBlockState();
          if(depth<soil.topsoil())return (depth==0?(soil.kind()==SoilProfile.Kind.PODZOL?Blocks.PODZOL:soil.kind()==SoilProfile.Kind.FROZEN?Blocks.SNOW_BLOCK:Blocks.GRASS_BLOCK):Blocks.DIRT).defaultBlockState();
          if(depth<soil.topsoil()+soil.subsoil())return (soil.kind()==SoilProfile.Kind.CLAY?Blocks.CLAY:Blocks.DIRT).defaultBlockState();
          return (geology.rock(y)==Geology.Rock.SANDSTONE?Blocks.SANDSTONE:Blocks.GRAVEL).defaultBlockState();
        }
        int ore=deposits.at(y);
        return ore<0?rock(geology.rock(y)):orePalette.get(ore);
      }
      return s.wet()&&y<s.water()?water(s,y,ocean):Blocks.AIR.defaultBlockState();
    }
  }
  /** Canonical anchors are evaluated in each intersected chunk; writes remain local and one-time. */
  private void decorateDebris(ChunkAccess chunk,int ox,int oz) {
    for(int gx=Math.floorDiv(ox-8,32);gx<=Math.floorDiv(ox+23,32);gx++)
      for(int gz=Math.floorDiv(oz-8,32);gz<=Math.floorDiv(oz+23,32);gz++) {
        if(engine.noise.hash(gx,gz,3300)>.25)continue;
        int x=gx*32+8+(int)(engine.noise.hash(gx,gz,3301)*16),z=gz*32+8+(int)(engine.noise.hash(gx,gz,3302)*16);
        var s=engine.sample(x,z);
        if(!s.wet()||s.speed()==0||s.rain()<.5||s.temperature()<.25)continue;
        boolean east=Math.abs(s.flowZ())>Math.abs(s.flowX());int dx=east?1:0,dz=east?0:1;
        int left=0,right=0,y=(int)Math.floor(s.water())-1;
        for(int n=1;n<=8;n++) {var a=engine.column(x-dx*n,z-dz*n);if(!a.wet()&&a.ground()>=y){left=n;break;}}
        for(int n=1;n<=8;n++) {var a=engine.column(x+dx*n,z+dz*n);if(!a.wet()&&a.ground()>=y){right=n;break;}}
        if(left==0||right==0||left+right>13)continue;
        for(int n=-left;n<=right;n++) {
          int wx=x+dx*n,wz=z+dz*n;if(wx<ox||wz<oz||wx>ox+15||wz>oz+15)continue;
          var p=new BlockPos(wx,y,wz);var old=chunk.getBlockState(p);
          if(old.is(EngineRegistry.WATER_BLOCK.get()))chunk.setBlockState(p,Blocks.OAK_LOG.defaultBlockState()
              .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,east?Direction.Axis.X:Direction.Axis.Z));
        }
      }
    for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
      int wx=ox+x,wz=oz+z;if(engine.noise.hash(wx,wz,3310)>.008)continue;
      var s=surfaceSample(wx,wz);if(s.wet()||s.slope()<.6||s.slope()>1.8||s.temperature()<.12)continue;
      var p=new BlockPos(wx,(int)Math.floor(s.ground())+1,wz);
      if(chunk.getBlockState(p).isAir())chunk.setBlockState(p,rock(new Geology(engine.noise).column(wx,wz,s).rock(p.getY()-2)));
    }
    Heightmap.primeHeightmaps(chunk,java.util.EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG,Heightmap.Types.OCEAN_FLOOR_WG));
  }

  private BlockState surfaceV3(TerrainEngine.Sample s, int y, int bed) {
    if (s.sediment() > .65 && s.slope() > 1.15 && bed <= 270)
      return Blocks.GRAVEL.defaultBlockState();
    if (s.slope() > 1.4 || bed > 270)
      return (s.rock() == 0 ? Blocks.GRANITE : Blocks.STONE).defaultBlockState();
    if (s.ground() < getSeaLevel() + 4 || s.rain() < .32)
      return (y < bed - 2 ? Blocks.SANDSTONE : Blocks.SAND).defaultBlockState();
    if (s.wet()) {
      if (s.speed() > .28 || s.slope() > .35) return Blocks.GRAVEL.defaultBlockState();
      return (s.rain() > .72 && s.sediment() > .8 ? Blocks.CLAY : Blocks.DIRT).defaultBlockState();
    }
    if (s.temperature() < .16 && y == bed) return Blocks.SNOW_BLOCK.defaultBlockState();
    if (s.sediment() > .9 && s.slope() > .8 && s.rain() < .45)
      return Blocks.GRAVEL.defaultBlockState();
    return (y == bed ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState();
  }

  private CaveColumn caves(TerrainEngine.Sample s, int x, int z, int maxY) {
    return !s.wet() && (revision < 3 || s.sediment() < .05)
        ? new CaveColumn(engine.noise, x, z, s.ground(), minY, maxY)
        : null;
  }

  private BlockState at(TerrainEngine.Sample s, CaveColumn caves, Geology.Column geology, DepositField.Column deposits, boolean ocean, int y) {
    if (y <= (int) Math.floor(s.ground()))
      return caves != null && caves.contains(y) ? Blocks.AIR.defaultBlockState() : geology == null ? solid(s, y) : solidV4(s, geology, deposits, y);
    if (s.wet() && y < s.water()) {
      int direction =
          Math.abs(s.flowX()) + Math.abs(s.flowZ()) < .01
              ? 16
              : Math.floorMod((int) Math.round(Math.atan2(s.flowZ(), s.flowX()) * 8 / Math.PI), 16);
      return EngineRegistry.WATER_BLOCK
          .get()
          .defaultBlockState()
          .setValue(EngineWaterBlock.OCEAN, ocean)
          .setValue(EngineFluid.UNITS, (int) Math.clamp(Math.round((s.water() - y) * 16), 1, 16))
          .setValue(EngineFluid.FLOW, direction)
          .setValue(EngineFluid.SPEED, (int) Math.clamp(Math.round((s.speed() - .1) * 10), 0, 7));
    }
    return Blocks.AIR.defaultBlockState();
  }

  public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor l, RandomState r) {
    var s = surfaceSample(x, z);
    if(revision>=6) {
      var deposit=depositPlan.chunk(Math.floorDiv(x,16)*16,Math.floorDiv(z,16)*16,(px,pz)->engine.column(px,pz).ground());
      var column=new V6Column(x,z,s,deposit);
      var states=new BlockState[l.getHeight()];
      for(int i=0;i<states.length;i++)states[i]=column.at(l.getMinY()+i);
      return new NoiseColumn(l.getMinY(),states);
    }
    var caves = caves(s, x, z, l.getMaxY() - 1);
    var geology = revision >= 4 ? new Geology(engine.noise).column(x, z, s) : null;
    var deposits = revision >= 5 ? new DepositField(engine.noise).column(x, z, geology) : null;
    boolean ocean = revision >= 4 && engine.ocean(x, z);
    BlockState[] a = new BlockState[l.getHeight()];
    for (int j = 0; j < a.length; j++) a[j] = at(s, caves, geology, deposits, ocean, l.getMinY() + j);
    return new NoiseColumn(l.getMinY(), a);
  }

  private static TerrainEngine.Sample shapedSlope(
      TerrainEngine.Sample center,
      TerrainEngine.Sample west,
      TerrainEngine.Sample east,
      TerrainEngine.Sample north,
      TerrainEngine.Sample south) {
    return center.withSlope(
        Math.hypot(east.ground() - west.ground(), south.ground() - north.ground()) * .5);
  }

  private TerrainEngine.Sample surfaceSample(int x, int z) {
    var center = engine.column(x, z);
    return revision < 3
        ? center
        : shapedSlope(
            center,
            engine.column(x - 1, z),
            engine.column(x + 1, z),
            engine.column(x, z - 1),
            engine.column(x, z + 1));
  }

  private TerrainEngine.Sample[] columns(int ox, int oz) {
    var result = new TerrainEngine.Sample[256];
    if (revision < 3) {
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) result[z * 16 + x] = engine.column(ox + x, oz + z);
      return result;
    }
    var halo = new TerrainEngine.Sample[18 * 18];
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) halo[(z + 1) * 18 + x + 1] = engine.column(ox + x, oz + z);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        int i = (z + 1) * 18 + x + 1;
        result[z * 16 + x] =
            shapedSlope(halo[i], halo[i - 1], halo[i + 1], halo[i - 18], halo[i + 18]);
      }
    return result;
  }

  public CompletableFuture<ChunkAccess> fillFromNoise(
      Blender b, RandomState r, StructureManager m, ChunkAccess c) {
    int ox = c.getPos().getMinBlockX(), oz = c.getPos().getMinBlockZ();
    var columns = columns(ox, oz);
    var depositField = revision == 5 ? new DepositField(engine.noise) : null;
    var authored = revision >= 6 ? depositPlan.chunk(ox,oz,(x,z)->engine.column(x,z).ground()) : null;
    var pos = new BlockPos.MutableBlockPos();
    var floor = c.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    var top = c.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        var s = columns[z * 16 + x];
        int max =
            Math.min(
                c.getMaxY() - 1,
                (int) Math.ceil(Math.max(s.ground(), s.wet() ? s.water() : s.ground())));
        var detailed=revision>=6?new V6Column(ox+x,oz+z,s,authored):null;
        var caves = revision<6?caves(s, ox + x, oz + z, max):null;
        var geology = revision >= 4 ? new Geology(engine.noise).column(ox + x, oz + z, s) : null;
        var deposits = depositField == null ? null : depositField.column(ox + x, oz + z, geology);
        boolean ocean = revision >= 4 && engine.ocean(ox + x, oz + z);
        for (int y = c.getMinY(); y <= max; y++) {
          BlockState state = detailed==null?at(s,caves,geology,deposits,ocean,y):detailed.at(y);
          if (!state.isAir()) {
            c.setBlockState(pos.set(ox + x, y, oz + z), state);
            floor.update(x, y, z, state);
            top.update(x, y, z, state);
          }
        }
      }
    decorate(c, ox, oz, columns);
    if(revision>=6 && profile.options().debris())decorateDebris(c,ox,oz);
    return CompletableFuture.completedFuture(c);
  }

  private TerrainEngine.Sample column(
      TerrainEngine.Sample[] columns, int ox, int oz, int x, int z) {
    int dx = x - ox, dz = z - oz;
    return dx >= 0 && dx < 16 && dz >= 0 && dz < 16 ? columns[dz * 16 + dx] : engine.column(x, z);
  }

  private void decorate(ChunkAccess c, int ox, int oz, TerrainEngine.Sample[] columns) {
    for (int gz = Math.floorDiv(oz - 6, 8); gz <= Math.floorDiv(oz + 21, 8); gz++)
      for (int gx = Math.floorDiv(ox - 6, 8); gx <= Math.floorDiv(ox + 21, 8); gx++) {
        int x = gx * 8 + 1 + (int) (engine.noise.hash(gx, gz, 311) * 6),
            z = gz * 8 + 1 + (int) (engine.noise.hash(gx, gz, 312) * 6);
        int reach = revision >= 3 ? 4 : 3;
        if (x + reach < ox || z + reach < oz || x - reach > ox + 15 || z - reach > oz + 15)
          continue;
        var s = column(columns, ox, oz, x, z);
        if (revision >= 3 && (x < ox || z < oz || x > ox + 15 || z > oz + 15))
          s = surfaceSample(x, z);
        double density = engine.forest(x, z);
        if (s.wet()
            || s.ground() < getSeaLevel() + 5
            || s.slope() > .8
            || s.temperature() < .1
            || s.rain() < .38
            || engine.noise.hash(gx, gz, 313) > Math.max(0, density - .20) * .66) continue;
        if(revision>=6 && engine.landscape.ice(x,z,s)>0)continue;
        boolean pine = s.temperature() < .36;
        int y = (int) Math.floor(s.ground()) + 1,
            h =
                revision >= 3
                    ? (pine ? 8 : 5) + (int) (engine.noise.hash(gx, gz, 314) * 5)
                    : 5 + (int) (engine.noise.hash(gx, gz, 314) * 7),
            radius =
                revision >= 3
                    ? 3 + (int) (engine.noise.hash(gx, gz, 315) * 2)
                    : 2 + (int) (engine.noise.hash(gx, gz, 315) * 2);
        if (revision >= 2) {
          boolean supported = true;
          for (int dz = -radius; dz <= radius && supported; dz++)
            for (int dx = -radius; dx <= radius; dx++) {
              var neighbor = column(columns, ox, oz, x + dx, z + dz);
              if ((revision >= 3 && neighbor.sediment() > .85)
                  || neighbor.wet()
                  || Math.abs(neighbor.ground() - s.ground()) > 2) {
                supported = false;
                break;
              }
            }
          if (!supported) continue;
        }
        Block log =
            pine
                ? Blocks.SPRUCE_LOG
                : engine.noise.hash(gx, gz, 316) > .75 ? Blocks.BIRCH_LOG : Blocks.OAK_LOG;
        Block leaves =
            pine
                ? Blocks.SPRUCE_LEAVES
                : log == Blocks.BIRCH_LOG ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES;
        if(revision>=6) {
          int biome=engine.biome(x,z);
          if(biome==8) {log=Blocks.ACACIA_LOG;leaves=Blocks.ACACIA_LEAVES;}
          else if(biome==10||biome==11||biome==12) {log=Blocks.JUNGLE_LOG;leaves=Blocks.JUNGLE_LEAVES;}
          else if(biome==13) {log=Blocks.DARK_OAK_LOG;leaves=Blocks.DARK_OAK_LEAVES;}
          else if(biome==22) {log=Blocks.CHERRY_LOG;leaves=Blocks.CHERRY_LEAVES;}
          else if(biome==28) {log=Blocks.MANGROVE_LOG;leaves=Blocks.MANGROVE_LEAVES;}
        }
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
              if (revision >= 3) {
                double crownY = (dy - h + .5) / (pine ? 1 : 3.3),
                    jitter = .88 + engine.noise.hash(wx, wz, 317 + dy) * .12;
                double spread =
                    pine ? Math.max(.35, radius * (h + 2. - dy) / (h - 1.)) : radius * jitter;
                leaf =
                    dy >= (pine ? 3 : h - 4)
                        && (pine
                            ? dx * dx + dz * dz <= spread * spread
                            : (dx * dx + dz * dz) / (spread * spread) + crownY * crownY <= 1);
              }
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
          var s = column(columns, ox, oz, ox + x, oz + z);
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
