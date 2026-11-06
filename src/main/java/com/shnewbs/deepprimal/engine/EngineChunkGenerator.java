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
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
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
  private static final TagKey<Biome> RIPARIAN_MOSAIC = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "riparian_mosaic"));
  private static final TagKey<Biome> WATER_EDGE_VEGETATION = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "water_edge_vegetation"));
  private static final TagKey<Biome> WATER_ROOTED_TREES = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "water_rooted_trees"));
  private static final TagKey<Biome> WATER_EMERGENT_PLANTS = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "water_emergent_plants"));
  private static final TagKey<Biome> WATER_SUBMERGED_PLANTS = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "water_submerged_plants"));
  private static final TagKey<Biome> WATER_FLOATING_PLANTS = TagKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", "water_floating_plants"));
  public static final MapCodec<EngineChunkGenerator> CODEC = codecFor(1);
  public static final MapCodec<EngineChunkGenerator> CODEC_V2 = codecFor(2);
  public static final MapCodec<EngineChunkGenerator> CODEC_V3 = codecFor(3);
  public static final MapCodec<EngineChunkGenerator> CODEC_V4 = codecFor(4);
  public static final MapCodec<EngineChunkGenerator> CODEC_V5 = codecFor(5);

  public static final MapCodec<EngineChunkGenerator> CODEC_V6 = codecFor(6);
  public static final MapCodec<EngineChunkGenerator> CODEC_V7 = codecFor(7);
  private static MapCodec<EngineChunkGenerator> codecFor(int revision) {
    if(revision>=6) return RecordCodecBuilder.mapCodec(i->i.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter((EngineChunkGenerator g)->g.biomeSource),
        Codec.INT.optionalFieldOf("min_y",-128).forGetter(g->g.minY),
        Codec.INT.optionalFieldOf("height",768).forGetter(g->g.height),
        TerrainProfile.CODEC.optionalFieldOf("terrain").forGetter(g->Optional.of(g.profile)),
        Codec.intRange(0,6).optionalFieldOf("material_revision",0).forGetter(g->g.materialRevision),
        Codec.intRange(0,10).optionalFieldOf("channel_revision",0).forGetter(g->g.channelRevision)
    ).apply(i,(source,min,height,profile,materials,channels)->new EngineChunkGenerator(source,min,height,revision,
        profile.orElseGet(TerrainConfig::creationProfile),materials,channels)));
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
  private final int materialRevision, channelRevision;
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
    this(source,minY,height,revision,profile,0,0);
  }
  private EngineChunkGenerator(BiomeSource source, int minY, int height, int revision,
      TerrainProfile profile,int materialRevision,int channelRevision) {
    super(source);
    if(materialRevision<0||materialRevision>6||(materialRevision!=0&&revision!=7))
      throw new IllegalArgumentException("Material revision requires V7 geography");
    this.materialRevision=materialRevision;
    this.channelRevision=channelRevision;
    this.profile=profile;
    orePalette=profile==null?List.of():profile.palette();
    this.revision = revision;
    engine = new TerrainEngine(0, revision, profile==null?TerrainOptions.DEFAULT:profile.options(), channelRevision);
    if(profile!=null)depositPlan=profile.plan(engine.noise);
    if (minY % 16 != 0 || height % 16 != 0 || height < 256 || height > 2048)
      throw new IllegalArgumentException("Engine height");
    this.minY = minY;
    this.height = height;
  }

  public TerrainEngine geography() {
    return engine;
  }
  public int materialRevision() { return materialRevision; }
  public int channelRevision() { return channelRevision; }

  protected MapCodec<? extends ChunkGenerator> codec() {
    return switch (revision) {
      case 1 -> CODEC;
      case 2 -> CODEC_V2;
      case 3 -> CODEC_V3;
      case 4 -> CODEC_V4;
      case 5 -> CODEC_V5;
      case 6 -> CODEC_V6;
      default -> CODEC_V7;
    };
  }

  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> sets, RandomState random, long seed) {
    engine = new TerrainEngine(seed, revision, profile==null?TerrainOptions.DEFAULT:profile.options(), channelRevision);
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
    final int x,z,bed,iceBase,biome;final boolean tunnel,ocean;
    final TerrainEngine.Sample s,hydraulic;final Geology.Column geology;
    final Geology geologyModel;
    final Geology.OutcropColumn outcrop;
    final SoilProfile soil;final CaveNetwork.Column cave;final DepositPlan.Column deposits;
    final SoilTexture texture;
    final double shoreSandWeight;
    final NaturalShore.Bed bedFacies;
    V6Column(int x,int z,TerrainEngine.Sample s,DepositPlan.Chunk deposits) {
      this.x=x;this.z=z;this.s=s;bed=(int)Math.floor(s.ground());
      bedFacies = channelRevision >= 9
          && (s.wet() || NaturalShore.exposedBank(engine.noise, x, z, s))
          ? NaturalShore.bed(engine.noise, x, z, s) : null;
      biome=revision>=7?engine.biome(x,z):-1;
      hydraulic=engine.hydraulicColumn(x,z);double uncut=engine.raw(x,z);
      tunnel=engine.tunnel(x,z);ocean=engine.ocean(x,z);
      iceBase=(int)Math.floor(engine.surfaceWithoutIce(x,z));
      geologyModel=new Geology(engine.noise);geology=geologyModel.column(x,z,s);
      soil=materialRevision>=6?SoilProfile.atCoherentShore(engine.noise,x,z,s,geology)
          :materialRevision>=2?SoilProfile.at(engine.noise,x,z,s,geology):SoilProfile.at(s,geology);
      outcrop=materialRevision>=5
          ?geologyModel.outcropColumnPreservingIntrusions(x,z,geology)
          :materialRevision>=3?geologyModel.outcropColumn(x,z,geology):null;
      texture=materialRevision>=1?SoilTexture.at(engine.noise,x,z,s,geology):null;
      shoreSandWeight=materialRevision>=6?SoilProfile.shoreSandWeight(s):0;
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
          var original=legacySoil(depth,y);
          return materialRevision>=1?soilMaterial(original):original;
        }
        int ore=deposits.at(y);
        if (ore >= 0) return orePalette.get(ore);
        boolean layered = materialRevision == 3
            || materialRevision >= 4 && Geology.isMountainOutcrop(bed, s.slope(), y);
        // Mountain strata banding on flanks (material revision 6+)
        Geology.Rock host;
        if (materialRevision >= 6 && Geology.isMountainStrataZone(bed, s.slope(), y)) {
          host = geologyModel.mountainStrataRock(x, z, geology, y);
        } else {
          host = layered ? outcrop.rock(y) : geology.rock(y);
        }
        return rock(host);
      }
      return s.wet()&&y<s.water()?water(s,y,ocean):Blocks.AIR.defaultBlockState();
    }

    private BlockState legacySoil(int depth,int y) {
      if (bedFacies != null && (s.wet() || depth < 2)) {
        return switch (bedFacies) {
          case GRAVEL -> Blocks.GRAVEL.defaultBlockState();
          case SAND -> GroundMaterials.soil(SoilTexture.SAND, GroundMaterials.Cover.BARE).defaultBlockState();
          case SILT -> GroundMaterials.soil(SoilTexture.SILT, GroundMaterials.Cover.BARE).defaultBlockState();
          case CLAY -> GroundMaterials.soil(SoilTexture.CLAY, GroundMaterials.Cover.BARE).defaultBlockState();
        };
      }
      if(s.wet())return (soil.kind()==SoilProfile.Kind.CLAY?Blocks.CLAY:soil.kind()==SoilProfile.Kind.GRAVEL?Blocks.GRAVEL:Blocks.SAND).defaultBlockState();
      if(biome==9)return (depth==0&&s.slope()<.45?Blocks.RED_SAND:
          Math.floorMod(y,12)<3?Blocks.ORANGE_TERRACOTTA:Math.floorMod(y,12)<5?Blocks.YELLOW_TERRACOTTA:Blocks.TERRACOTTA).defaultBlockState();
      if((biome==6||biome==28)&&depth>0)return Blocks.MUD.defaultBlockState();
      if(biome==13&&depth==0)return Blocks.PODZOL.defaultBlockState();
      if(soil.kind()==SoilProfile.Kind.SAND)return (depth<soil.topsoil()+soil.subsoil()?Blocks.SAND:Blocks.SANDSTONE).defaultBlockState();
      if(soil.kind()==SoilProfile.Kind.SANDY_LOAM) {
        if(depth<soil.topsoil())return (depth==0?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState();
        if(depth<soil.topsoil()+soil.subsoil())return Blocks.DIRT.defaultBlockState();
        return Blocks.SANDSTONE.defaultBlockState();
      }
      if(soil.kind()==SoilProfile.Kind.LOAMY_SAND) {
        if(depth<soil.topsoil())return (depth==0?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState();
        if(depth<soil.topsoil()+soil.subsoil())return (depth<soil.topsoil()+1?Blocks.DIRT:Blocks.SAND).defaultBlockState();
        return Blocks.SANDSTONE.defaultBlockState();
      }
      if(depth<soil.topsoil())return (depth==0?(soil.kind()==SoilProfile.Kind.PODZOL?Blocks.PODZOL:soil.kind()==SoilProfile.Kind.FROZEN?Blocks.SNOW_BLOCK:Blocks.GRASS_BLOCK):Blocks.DIRT).defaultBlockState();
      if(depth<soil.topsoil()+soil.subsoil())return (soil.kind()==SoilProfile.Kind.CLAY?Blocks.CLAY:Blocks.DIRT).defaultBlockState();
      return (geology.rock(y)==Geology.Rock.SANDSTONE?Blocks.SANDSTONE:Blocks.GRAVEL).defaultBlockState();
    }

    private BlockState soilMaterial(BlockState original) {
      if(!(original.is(Blocks.DIRT)||original.is(Blocks.GRASS_BLOCK)||original.is(Blocks.PODZOL)
          ||original.is(Blocks.MUD)||original.is(Blocks.CLAY)||original.is(Blocks.SAND)||original.is(Blocks.RED_SAND)))
        return original;
      SoilTexture selected;
      if (original.is(Blocks.SAND) || original.is(Blocks.RED_SAND)) {
        selected = SoilTexture.SAND;
      } else if (original.is(Blocks.CLAY)) {
        selected = SoilTexture.CLAY;
      } else if (materialRevision >= 6 && shoreSandWeight > .15 && shoreSandWeight < .85) {
        // Grass-to-sand blending: use intermediate soil textures in the ecotone
        if (shoreSandWeight < .35) {
          selected = SoilTexture.LOAMY_SAND;  // More grass-like
        } else if (shoreSandWeight < .65) {
          selected = SoilTexture.SANDY_LOAM;  // Balanced blend
        } else {
          selected = SoilTexture.LOAMY_SAND;  // More sand-like
        }
      } else {
        selected = texture;
      }
      var cover=original.is(Blocks.GRASS_BLOCK)?GroundMaterials.Cover.GRASS
          :original.is(Blocks.PODZOL)?GroundMaterials.Cover.PODZOL:GroundMaterials.Cover.BARE;
      return GroundMaterials.soil(selected,cover).defaultBlockState();
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
        int biome = revision>=7?engine.biome(x,z):-1;
        double chance=revision>=7?Vegetation.treeChance(biome,density):Math.max(0,density-.20)*.66;
        boolean naturalBank = channelRevision >= 9 && !s.wet() && s.sediment() > .55
            && s.rain() >= .38 && chance > 0 && nearWater(x, z, 6);
        if (naturalBank)
          chance = NaturalShore.treeChance(biome, density,
              engine.noise.field(x, z, 32, 94506) * .5 + .5);
        var biomeHolder =
            channelRevision >= 8
                    && x >= ox
                    && x <= ox + 15
                    && z >= oz
                    && z <= oz + 15
                ? c.getNoiseBiome(
                    x >> 2, Math.floorDiv((int) Math.floor(s.ground()), 4), z >> 2)
                : null;
        boolean waterRooted = biomeHolder != null && biomeHolder.is(WATER_ROOTED_TREES)
            && nearWater(x, z, 3);
        if (s.wet()
            || s.ground() < (waterRooted ? getSeaLevel() - 1
                : naturalBank ? getSeaLevel() + .35 : getSeaLevel() + 5)
            || s.slope() > (revision>=7?1.5:.8)
            || s.temperature() < .1
            || s.rain() < .38
            || channelRevision >= 8 && biomeHolder != null
                && biomeHolder.is(WATER_ROOTED_TREES) && !waterRooted
            || engine.noise.hash(gx, gz, 313) > chance) continue;
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
          int supportRadius=waterRooted?0:revision>=7?1:radius;
          for (int dz = -supportRadius; dz <= supportRadius && supported; dz++)
            for (int dx = -supportRadius; dx <= supportRadius; dx++) {
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
        if(waterRooted && biome==28) {
          log=Blocks.MANGROVE_LOG;leaves=Blocks.MANGROVE_LEAVES;
        } else if(revision>=6) {
          if(revision<7)biome=engine.biome(x,z);
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
      for (int x = 0; x < 16; x++) {
        int wx = ox + x, wz = oz + z;
        var s = column(columns, ox, oz, wx, wz);
        var biomeHolder =
            channelRevision >= 8
                ? c.getNoiseBiome(
                    wx >> 2, Math.floorDiv((int) Math.floor(s.ground()), 4), wz >> 2)
                : null;
        boolean naturalPlants = channelRevision >= 9 && !s.wet() && s.sediment() > .55 && biomeHolder != null
            && biomeHolder.is(RIPARIAN_MOSAIC) && nearWater(wx, wz, 5);
        boolean shorePlants =
            !naturalPlants && biomeHolder != null
                && biomeHolder.is(WATER_EDGE_VEGETATION)
                && nearWater(wx, wz, 3);
        boolean emergentPlants =
            channelRevision >= 8
                && biomeHolder != null
                && biomeHolder.is(WATER_EMERGENT_PLANTS);
        double coverDraw =
            materialRevision >= 6
                ? engine.noise.field(wx, wz, 24, 4401) * .5 + .5
                : engine.noise.hash(wx, wz, 401);
        int y = (int) Math.floor(s.ground()) + 1;
        if (emergentPlants
            && engine.noise.hash(wx, wz, 4412) < .018
            && !s.wet()
            && s.slope() < .45
            && s.temperature() > .2
            && emergentWaterSupport(wx, y, wz)) {
          var plant = new BlockPos(wx, y, wz);
          var support = c.getBlockState(plant.below());
          if (c.getBlockState(plant).isAir()
              && (support.is(net.minecraft.tags.BlockTags.DIRT)
                  || support.is(net.minecraft.tags.BlockTags.SAND))) {
            int height = 1 + (int) (engine.noise.hash(wx, wz, 4410) * 3);
            for (int dy = 0; dy < height && y + dy < c.getMaxY(); dy++) {
              var segment = new BlockPos(wx, y + dy, wz);
              if (!c.getBlockState(segment).isAir()) break;
              c.setBlockState(segment, Blocks.SUGAR_CANE.defaultBlockState());
            }
          }
        }

        // Submerged plants (seagrass, kelp) in tagged water biomes
        boolean submergedPlants =
            channelRevision >= 8
                && biomeHolder != null
                && biomeHolder.is(WATER_SUBMERGED_PLANTS);
        if (submergedPlants && s.wet() && s.water() > y && engine.noise.hash(wx, wz, 4413) < .025) {
          var plantPos = new BlockPos(wx, y, wz);
          var support = c.getBlockState(plantPos.below());
          if (c.getBlockState(plantPos).is(Blocks.WATER)
              && (support.is(Blocks.SAND) || support.is(Blocks.GRAVEL)
                  || support.is(Blocks.DIRT) || support.is(Blocks.CLAY)
                  || support.getBlock() instanceof GroundMaterials.SoilBlock)) {
            // Seagrass in shallow water, kelp in deeper water
            int waterDepth = (int) (s.water() - y);
            if (waterDepth >= 1 && waterDepth <= 4) {
              c.setBlockState(plantPos, Blocks.SEAGRASS.defaultBlockState());
            } else if (waterDepth > 4 && waterDepth <= 12 && engine.noise.hash(wx, wz, 4414) < .4) {
              int kelpHeight = 2 + (int) (engine.noise.hash(wx, wz, 4415) * (waterDepth - 2));
              for (int dy = 0; dy < kelpHeight && y + dy < s.water(); dy++) {
                var kelpPos = new BlockPos(wx, y + dy, wz);
                if (!c.getBlockState(kelpPos).is(Blocks.WATER)) break;
                c.setBlockState(kelpPos, Blocks.KELP.defaultBlockState());
              }
              if (y + kelpHeight < s.water()) {
                c.setBlockState(new BlockPos(wx, y + kelpHeight, wz), Blocks.KELP_PLANT.defaultBlockState());
              }
            }
          }
        }

        // Floating plants (lily pads) in tagged water biomes
        boolean floatingPlants =
            channelRevision >= 8
                && biomeHolder != null
                && biomeHolder.is(WATER_FLOATING_PLANTS);
        if (floatingPlants && s.wet() && s.water() == y && engine.noise.hash(wx, wz, 4416) < .015) {
          var plantPos = new BlockPos(wx, y, wz);
          if (c.getBlockState(plantPos).is(Blocks.WATER)
              && c.getBlockState(plantPos.above()).isAir()) {
            c.setBlockState(plantPos.above(), Blocks.LILY_PAD.defaultBlockState());
          }
        }

        // Water-rooted trees with mangrove-style prop roots in tagged biomes
        boolean waterRootedTrees =
            channelRevision >= 8
                && biomeHolder != null
                && biomeHolder.is(WATER_ROOTED_TREES)
                && nearWater(wx, wz, 4);
        if (waterRootedTrees && s.wet() && s.water() > y && engine.noise.hash(wx, wz, 4417) < .008) {
          var basePos = new BlockPos(wx, y, wz);
          var support = c.getBlockState(basePos.below());
          // Place mangrove propagules/roots in shallow water near shore
          if (c.getBlockState(basePos).is(Blocks.WATER)
              && (support.is(Blocks.SAND) || support.is(Blocks.GRAVEL)
                  || support.is(Blocks.DIRT) || support.is(Blocks.CLAY)
                  || support.is(Blocks.MUD)
                  || support.getBlock() instanceof GroundMaterials.SoilBlock)) {
            int waterDepth = (int) (s.water() - y);
            if (waterDepth >= 1 && waterDepth <= 3) {
              // Place mangrove propagule at water surface
              c.setBlockState(basePos.above(), Blocks.MANGROVE_PROPAGULE.defaultBlockState()
                  .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HANGING, false));
              // Add prop roots extending down into water
              for (int dy = 1; dy <= waterDepth; dy++) {
                var rootPos = new BlockPos(wx, y + dy, wz);
                if (!c.getBlockState(rootPos).is(Blocks.WATER)) break;
                if (engine.noise.hash(wx, wz, 4418 + dy) < .6) {
                  c.setBlockState(rootPos, Blocks.MANGROVE_ROOTS.defaultBlockState()
                      .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true));
                }
              }
            }
          }
        }

        double coverChance = naturalPlants
            ? NaturalShore.coverChance(engine.noise.field(wx, wz, 18, 94507) * .5 + .5)
            : shorePlants ? .42 : .11;
        if (coverDraw < coverChance) {
          var p = new BlockPos(wx, y, wz);
          if (!s.wet()
              && (c.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)
                  || c.getBlockState(p.below()).getBlock() instanceof GroundMaterials.SoilBlock soil
                      && soil.cover()==GroundMaterials.Cover.GRASS)
              && c.getBlockState(p).isAir()) {
            Block cover=Blocks.SHORT_GRASS;
            if(revision>=7) {
              int biome=engine.biome(ox+x,oz+z);double pick=engine.noise.hash(ox+x,oz+z,4401);
              if(biome==4||biome==15||biome==16||biome==10||biome==11)cover=Blocks.FERN;
              else if(biome==21||biome==22)cover=pick<.32?Blocks.DANDELION:pick<.54?Blocks.POPPY:pick<.68?Blocks.OXEYE_DAISY:Blocks.SHORT_GRASS;
              else if(biome==8&&pick<.30)cover=Blocks.DEAD_BUSH;
            }
            c.setBlockState(p, cover.defaultBlockState());
          } else if(revision>=7&&!s.wet()&&c.getBlockState(p).isAir()
              && (c.getBlockState(p.below()).is(Blocks.SAND)||c.getBlockState(p.below()).is(Blocks.RED_SAND)
                  || c.getBlockState(p.below()).getBlock() instanceof GroundMaterials.SoilBlock soil
                      && soil.texture()==SoilTexture.SAND)
              && s.ground()>getSeaLevel()+6 && s.rain()<.32)
            c.setBlockState(p,Blocks.DEAD_BUSH.defaultBlockState());
        }
      }
    Heightmap.primeHeightmaps(
        c, java.util.EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
  }

  private boolean nearWater(int x,int z,int radius) {
    for(int r=1;r<=radius;r++)
      for(int n=-r;n<=r;n++) {
        if(engine.column(x+n,z-r).wet()||engine.column(x+n,z+r).wet()
            ||engine.column(x-r,z+n).wet()||engine.column(x+r,z+n).wet())return true;
      }
    return false;
  }

  private boolean emergentWaterSupport(int x, int y, int z) {
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      int wx = x + direction.getStepX(), wz = z + direction.getStepZ();
      var neighbor = engine.column(wx, wz);
      if (neighbor.wet() && y > Math.floor(neighbor.ground()) && y < neighbor.water())
        return true;
    }
    return false;
  }
}
