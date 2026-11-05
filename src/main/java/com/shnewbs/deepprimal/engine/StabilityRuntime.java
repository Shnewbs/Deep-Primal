package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Edit-triggered local cave-ins. No surface collapse, fluid overwrite, forced chunk loads or global scan. */
public final class StabilityRuntime {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLED;
  static {var b=new ModConfigSpec.Builder();ENABLED=b.comment("Server-authoritative local cave-ins in V6 worlds. Protects twelve surface blocks; at most eight queued checks per tick.").define("undergroundCaveIns",true);SPEC=b.build();}
  private static final int LIMIT=256,CHECKS=8,DROP=64,COVER=12;
  public static final class Pending extends SavedData {
    final LinkedHashSet<Long> positions=new LinkedHashSet<>();
    Pending(){}
    Pending(List<Long> points){if(points.size()>LIMIT)throw new IllegalArgumentException("Stability queue cap");positions.addAll(points);}
    static final Codec<Pending> CODEC=Codec.LONG.listOf().xmap(Pending::new,p->List.copyOf(p.positions));
    static final SavedDataType<Pending> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("deepprimal","stability_v1"),Pending::new,CODEC);
    void add(BlockPos p){if(positions.size()<LIMIT&&positions.add(p.asLong()))setDirty();}
  }
  private static boolean loaded(ServerLevel l,BlockPos p){return l.getChunkSource().getChunkNow(Math.floorDiv(p.getX(),16),Math.floorDiv(p.getZ(),16))!=null;}
  private static int strength(BlockState s){
    if(s.is(Blocks.SANDSTONE)||s.is(Blocks.TUFF))return 2;
    if(s.is(Blocks.CALCITE))return 3;
    if(s.is(Blocks.GRANITE)||s.is(Blocks.ANDESITE)||s.is(Blocks.DIORITE)||s.is(Blocks.BASALT)||s.is(Blocks.DEEPSLATE)||s.is(Blocks.STONE))return 4;
    return 0; // Crafted supports, inventories and ore blocks are never converted into falling rock.
  }
  /** Bounded transaction: the same solid lands on a verified solid floor without consuming water. */
  public static boolean collapse(ServerLevel l,BlockPos p) {
    if(!loaded(l,p)||!(l.getChunkSource().getGenerator() instanceof EngineChunkGenerator g)||g.geography().revision<6)return false;
    if(p.getY()>=Math.min(g.geography().column(p.getX(),p.getZ()).ground(),l.getHeight(Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ()))-COVER)return false;
    var original=l.getBlockState(p);int span=strength(original);if(span==0||!l.getBlockState(p.below()).isAir())return false;
    for(Direction d:Direction.Plane.HORIZONTAL){var q=p.relative(d);if(!loaded(l,q))return false;if(l.getBlockState(q).is(EngineRegistry.WATER_BLOCK.get()))span=Math.max(1,span-1);}
    for(Direction d:Direction.Plane.HORIZONTAL)for(int n=1;n<=span;n++) {
      var q=p.relative(d,n);if(!loaded(l,q))return false;
      if(l.getBlockState(q).isAir())break;
      if(l.getBlockState(q).isSolidRender()&&l.getBlockState(q.below()).isSolidRender()&&l.getBlockState(q.below(2)).isSolidRender())return false;
    }
    BlockPos landing=null;
    for(int n=1;n<=DROP;n++) {
      var q=p.below(n);if(q.getY()<=l.getMinY()+2)return false;
      var s=l.getBlockState(q);
      if(!s.isAir()) {if(s.isSolidRender()&&n>1)landing=q.above();break;}
    }
    if(landing==null)return false; // Water and unsupported/deep drops arrest this local event.
    if(!HydrologyRuntime.write(l,landing,original))return false;
    if(!HydrologyRuntime.write(l,p,Blocks.AIR.defaultBlockState())) {HydrologyRuntime.write(l,landing,Blocks.AIR.defaultBlockState());return false;}
    WaterRuntime.activate(l,p);WaterRuntime.activate(l,landing);return true;
  }
  @SubscribeEvent public void broken(BreakBlockEvent e) {
    if(e.isCanceled()||!ENABLED.get()||!(e.getLevel() instanceof ServerLevel l)||!(l.getChunkSource().getGenerator() instanceof EngineChunkGenerator g)||g.geography().revision<6)return;
    var pending=l.getDataStorage().computeIfAbsent(Pending.TYPE);
    for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)pending.add(e.getPos().offset(dx,1,dz));
  }
  @SubscribeEvent public void tick(LevelTickEvent.Post e) {
    if(!ENABLED.get()||!(e.getLevel() instanceof ServerLevel l)||!(l.getChunkSource().getGenerator() instanceof EngineChunkGenerator g)||g.geography().revision<6)return;
    var pending=l.getDataStorage().computeIfAbsent(Pending.TYPE);int size=Math.min(CHECKS,pending.positions.size());
    for(int n=0;n<size;n++) {
      var it=pending.positions.iterator();long key=it.next();it.remove();var p=BlockPos.of(key);
      if(!loaded(l,p)){pending.positions.add(key);continue;}
      pending.setDirty();
      if(collapse(l,p)){pending.add(p.above());for(Direction d:Direction.Plane.HORIZONTAL)pending.add(p.relative(d));}
    }
  }
}
