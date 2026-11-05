package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Private packaged-server oracle; never included in the exported mod. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class DepositNativeChecks {
  public DepositNativeChecks() { NeoForge.EVENT_BUS.register(this); }
  private static final int[][] SITES = {{-255,-10},{-254,-10},{-255,-9},{-254,-9},
      {-2,-2},{-1,-2},{0,0},{1,0},{-75,-32},{-74,-32},{3,-32},{4,-32}};
  private static final Map<BlockState, byte[]> BYTES = new HashMap<>();
  private static byte[] bytes(BlockState s) {
    return BYTES.computeIfAbsent(s, k -> (k.toString()+"\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
  private static String hex(byte[] b) { return HexFormat.of().formatHex(b); }
  static EngineChunkGenerator generator(ServerLevel l, JsonObject template, int rev, long seed) {
    var json=template.deepCopy(); json.addProperty("type", "deepprimal:continental_v"+rev);
    if(rev!=7){json.remove("material_revision");json.remove("channel_revision");} // saved revisions are V7-only
    // V1-V5 codecs predate saved terrain profiles and cannot round-trip this field.
    if(rev<6)json.remove("terrain");
    var ops=l.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var g=(EngineChunkGenerator)ChunkGenerator.CODEC.parse(ops,json).getOrThrow();
    var encoded=ChunkGenerator.CODEC.encodeStart(ops,g).getOrThrow();
    if (!encoded.equals(json) || g.geography().revision!=rev) throw new AssertionError("codec revision "+rev);
    g.createState(l.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),l.getChunkSource().randomState(),seed);
    return g;
  }
  static ProtoChunk chunk(ServerLevel l, EngineChunkGenerator g, int[] at) {
    var c=new ProtoChunk(new ChunkPos(at[0],at[1]),UpgradeData.EMPTY,l,PalettedContainerFactory.create(l.registryAccess()),null);
    g.fillFromNoise(Blender.empty(),l.getChunkSource().randomState(),l.structureManager(),c).join();
    return c;
  }
  static String digest(ProtoChunk c) throws Exception {
    var hash=MessageDigest.getInstance("SHA-256");
    for(var section:c.getSections()) for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++)
      hash.update(bytes(section.getBlockState(x,y,z)));
    return hex(hash.digest());
  }
  static Geology.Ore oracle(TerrainNoise n, Geology.Column host,int x,int y,int z,int bed) {
    if(y>=bed-5)return Geology.Ore.NONE;
    int gx=Math.floorDiv(x,32),gz=Math.floorDiv(z,32);
    for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
      int cx=gx+dx,cz=gz+dz;
      double r=3+n.hash(cx,cz,1814)*5;
      double u=(x-((long)cx*32+8+n.hash(cx,cz,1810)*16))/(r*1.7);
      double v=(z-((long)cz*32+8+n.hash(cx,cz,1811)*16))/r;
      double w=(y-(-96+n.hash(cx,cz,1812)*352))/(r*.45);
      if(u*u+v*v+w*w>1)continue;
      var h=host.rock(y); int type=(int)(n.hash(cx,cz,1813)*8);
      boolean eligible=switch(type) {
        case 0 -> h==Geology.Rock.SANDSTONE||h==Geology.Rock.TUFF;
        case 1 -> h==Geology.Rock.ANDESITE||h==Geology.Rock.CALCITE;
        case 2 -> true;
        case 3 -> h==Geology.Rock.GRANITE||h==Geology.Rock.DIORITE;
        case 4 -> h==Geology.Rock.BASALT||h==Geology.Rock.GRANITE;
        case 5 -> h==Geology.Rock.CALCITE;
        case 6 -> h==Geology.Rock.BASALT||h==Geology.Rock.DEEPSLATE;
        default -> h==Geology.Rock.CALCITE||h==Geology.Rock.GRANITE;
      };
      if(eligible)return Geology.Ore.values()[type+1];
    }
    return Geology.Ore.NONE;
  }
  static BlockState material(Geology.Ore ore,Geology.Rock rock,int y) {
    if(ore!=Geology.Ore.NONE) {
      Block b=switch(ore) {
        case COAL -> y<0?Blocks.DEEPSLATE_COAL_ORE:Blocks.COAL_ORE;
        case COPPER -> y<0?Blocks.DEEPSLATE_COPPER_ORE:Blocks.COPPER_ORE;
        case IRON -> y<0?Blocks.DEEPSLATE_IRON_ORE:Blocks.IRON_ORE;
        case GOLD -> y<0?Blocks.DEEPSLATE_GOLD_ORE:Blocks.GOLD_ORE;
        case REDSTONE -> y<0?Blocks.DEEPSLATE_REDSTONE_ORE:Blocks.REDSTONE_ORE;
        case LAPIS -> y<0?Blocks.DEEPSLATE_LAPIS_ORE:Blocks.LAPIS_ORE;
        case DIAMOND -> y<0?Blocks.DEEPSLATE_DIAMOND_ORE:Blocks.DIAMOND_ORE;
        case EMERALD -> y<0?Blocks.DEEPSLATE_EMERALD_ORE:Blocks.EMERALD_ORE;
        default -> throw new AssertionError();
      }; return b.defaultBlockState();
    }
    return (switch(rock) {
      case GRANITE -> Blocks.GRANITE; case ANDESITE -> Blocks.ANDESITE; case DIORITE -> Blocks.DIORITE;
      case BASALT -> Blocks.BASALT; case TUFF -> Blocks.TUFF; case SANDSTONE -> Blocks.SANDSTONE;
      case CALCITE -> Blocks.CALCITE; case DEEPSLATE -> Blocks.DEEPSLATE;
    }).defaultBlockState();
  }
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception {
    String output=System.getenv("DEEPPRIMAL_DEPOSIT_RECORD"); if(output==null)return;
    var l=event.getServer().overworld(); var ops=l.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var template=ChunkGenerator.CODEC.encodeStart(ops,l.getChunkSource().getGenerator()).getOrThrow().getAsJsonObject();
    String savedRevision=System.getenv("DEEPPRIMAL_SAVED_REVISION");
    boolean reopen="reopen".equals(System.getenv("DEEPPRIMAL_DEPOSIT_MODE"));
    var marker=new BlockPos(9,500,9);
    if(savedRevision!=null) {
      int expected=Integer.parseInt(savedRevision);
      if(((EngineChunkGenerator)l.getChunkSource().getGenerator()).geography().revision!=expected)
        throw new AssertionError("world lost saved revision "+expected+" "+template);
      if(reopen&&!l.getBlockState(marker).is(Blocks.GOLD_BLOCK))throw new AssertionError("edit lost on reopen");
    }
    int last=Integer.parseInt(System.getenv().getOrDefault("DEEPPRIMAL_DEPOSIT_MAX_REVISION","5"));
    var result=new JsonObject();
    result.addProperty("fixture_schema",2);result.addProperty("sites_count",SITES.length);
    try(var in=DepositNativeChecks.class.getResourceAsStream("DepositNativeChecks.class")){result.addProperty("fixture_class_sha256",hex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));} var hashes=new JsonObject(); var density=new JsonObject();
    long baseChecks=0,oracleChecks=0,changed=0,protectedChecks=0,orderChecks=0;
    long caveAirChecks=0,waterChecks=0,soilChecks=0,bedrockChecks=0;
    String firstChanged=null;
    long started=System.nanoTime();
    for(long seed:new long[]{0,20261020}) {
      List<ProtoChunk> v4=new ArrayList<>();
      for(int rev=1;rev<=last;rev++) {
        var g=generator(l,template,rev,seed);var keyHashes=new ArrayList<String>();
        var counts=new long[Geology.Ore.values().length];
        for(int i=0;i<SITES.length;i++) {
          var c=chunk(l,g,SITES[i]);String hash=digest(c);keyHashes.add(hash);
          hashes.addProperty(seed+":"+rev+":"+i,hash); if(rev==4)v4.add(c);
          var p=new BlockPos.MutableBlockPos();
          for(int z=c.getPos().getMinBlockZ();z<=c.getPos().getMaxBlockZ();z++)
            for(int x=c.getPos().getMinBlockX();x<=c.getPos().getMaxBlockX();x++) {
              var s=g.geography().column(x,z);
              if(rev>=3) s=s.withSlope(Math.hypot(g.geography().column(x+1,z).ground()-g.geography().column(x-1,z).ground(),g.geography().column(x,z+1).ground()-g.geography().column(x,z-1).ground())*.5);
              int bed=(int)Math.floor(s.ground());
              var col=g.getBaseColumn(x,z,l,l.getChunkSource().randomState());
              var host=rev==5?new Geology(g.geography().noise).column(x,z,s):null;
              for(int y=l.getMinY();y<l.getMaxY();y++) {
                var state=c.getBlockState(p.set(x,y,z));
                if(rev>=4) { String name=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                  if(name.startsWith("deepslate_"))name=name.substring(10);
                  if(name.endsWith("_ore")) counts[Geology.Ore.valueOf(name.substring(0,name.length()-4).toUpperCase(java.util.Locale.ROOT)).ordinal()]++;
                }
                // Native decoration lives above bed; compare all raw terrain and water.
                if(y<=Math.max(bed,Math.ceil(s.wet()?s.water():s.ground())-1)) {
                  if(!state.equals(col.getBlock(y)))throw new AssertionError("base/chunk "+rev+" "+p);
                  baseChecks++;
                }
                if(rev!=5)continue;
                var old=v4.get(i).getBlockState(p);
                if(!state.equals(old)){changed++;if(firstChanged==null)firstChanged="seed="+seed+" "+p+" old="+old+" new="+state;}
                if(old.isAir()||!old.getFluidState().isEmpty()||y<l.getMinY()+2||y>bed-host.soilDepth()) {
                  if(!state.equals(old))throw new AssertionError("cave/water/soil changed "+p);
                  protectedChecks++;
                  if(old.isAir()&&y<=bed)caveAirChecks++;
                  if(!old.getFluidState().isEmpty())waterChecks++;
                  if(y>bed-host.soilDepth()&&y<=bed)soilChecks++;
                  if(y<l.getMinY()+2)bedrockChecks++;
                  continue;
                }
                var ore=oracle(g.geography().noise,host,x,y,z,bed);
                if(!state.equals(material(ore,host.rock(y),y)))throw new AssertionError("native oracle "+p+" "+ore+" "+state);
                oracleChecks++;
              }
            }
        }
        density.add(seed+":"+rev,new Gson().toJsonTree(counts));
        if(rev==5) {
          var reverse=generator(l,template,rev,seed);
          for(int i=SITES.length-1;i>=0;i--) {
            if(!digest(chunk(l,reverse,SITES[i])).equals(keyHashes.get(i)))throw new AssertionError("order "+i);
            orderChecks++;
          }
        }
      }
    }
    if(last==5&&(changed==0||oracleChecks==0||orderChecks!=2*SITES.length))throw new AssertionError("empty coverage");
    long fullChunkChecks=0;
    if(savedRevision!=null) {
      var worldGenerator=(EngineChunkGenerator)l.getChunkSource().getGenerator();
      var pos=new BlockPos.MutableBlockPos();
      for(int[] site:new int[][]{{-2,-2},{-1,-2},{4,-32}}) {
        var expected=chunk(l,worldGenerator,site);var actual=l.getChunk(site[0],site[1]);
        for(int z=actual.getPos().getMinBlockZ();z<=actual.getPos().getMaxBlockZ();z++)
          for(int x=actual.getPos().getMinBlockX();x<=actual.getPos().getMaxBlockX();x++) {
            int bed=(int)Math.floor(worldGenerator.geography().column(x,z).ground());
            for(int y=l.getMinY();y<bed-8;y++) {
              if(!actual.getBlockState(pos.set(x,y,z)).equals(expected.getBlockState(pos)))throw new AssertionError("FULL/save chunk "+pos);
              fullChunkChecks++;
            }
          }
      }
    }
    result.addProperty("full_saved_chunk_blocks",fullChunkChecks);
    if(savedRevision!=null&&!reopen) l.setBlock(marker,Blocks.GOLD_BLOCK.defaultBlockState(),3);
    result.addProperty("saved_revision",((EngineChunkGenerator)l.getChunkSource().getGenerator()).geography().revision);
    result.addProperty("reopen_edit_verified",reopen&&l.getBlockState(marker).is(Blocks.GOLD_BLOCK));
    result.addProperty("pass",true);result.add("hashes",hashes);result.add("density",density);
    result.addProperty("base_column_blocks",baseChecks);result.addProperty("oracle_blocks",oracleChecks);
    result.addProperty("changed_material_blocks",changed);result.addProperty("first_changed",firstChanged);result.addProperty("protected_blocks",protectedChecks);
    result.addProperty("cave_air_blocks",caveAirChecks);result.addProperty("water_blocks",waterChecks);
    result.addProperty("soil_band_blocks",soilChecks);result.addProperty("bedrock_band_blocks",bedrockChecks);
    result.addProperty("reversed_chunks",orderChecks);result.addProperty("elapsed_ms",(System.nanoTime()-started)/1e6);
    result.addProperty("root_origin",EngineChunkGenerator.class.getProtectionDomain().getCodeSource().getLocation().toString());
    result.addProperty("core_origin",Geology.class.getProtectionDomain().getCodeSource().getLocation().toString());
    Files.writeString(Path.of(output),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    System.out.println("DEPOSIT_NATIVE_PASS maxRevision="+last+" changed="+changed+" oracle="+oracleChecks);
  }
}
