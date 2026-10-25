package com.shnewbs.deepprimal.validation;

import com.google.gson.GsonBuilder;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterBoundaryIndex;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.client.Minecraft;

/** Read-only sampling of resident top boundaries in a running world, not a GPU pixel test. */
final class WaterLiveContinuityChecks {
  private record Corner(int x2,int y,int z2) {}
  private record Sample(long page,float displayed) {}
  private static Path output;
  private static int ticks;
  private static long comparisons, mismatches, sampledVertices, pausedTicks, withinPageMismatches;
  private static float maximumGap;
  private static long initialWrites,initialBytes;
  private static final List<Long> timings=new ArrayList<>();
  private static final Set<Long> sampledPages=new HashSet<>();
  private static final List<Map<String,Object>> examples=new ArrayList<>();

  private static Field field(Class<?> type,String name)throws ReflectiveOperationException {
    var value=type.getDeclaredField(name);value.setAccessible(true);return value;
  }

  static void start(Minecraft mc,Path destination) {
    if(output!=null)throw new IllegalStateException("Live water probe already running");
    ticks=0;comparisons=0;mismatches=0;sampledVertices=0;pausedTicks=0;withinPageMismatches=0;maximumGap=0;
    sampledPages.clear();examples.clear();output=destination;mc.setScreen(null);
    timings.clear();
    initialWrites=AnimatedWaterRenderer.INSTANCE.heightBoundaryWrites;
    initialBytes=AnimatedWaterRenderer.INSTANCE.heightBoundaryBytes;
    System.out.println("WATER_LIVE_CONTINUITY_STARTED ticks=600 readOnly=true");
  }

  static void tick(Minecraft mc)throws ReflectiveOperationException,java.io.IOException {
    if(output==null||mc.level==null||mc.player==null)return;
    if(mc.isPaused()){pausedTicks++;return;}
    var renderer=AnimatedWaterRenderer.INSTANCE;
    timings.add(renderer.lastHeightBoundaryNanos);
    var type=renderer.getClass();
    long clock=field(type,"clock").getLong(renderer);
    @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);
    var corners=new HashMap<Corner,Sample>();
    for(var entry:pages.entrySet()) {
      var page=entry.getValue();var pt=page.getClass();
      if(clock-field(pt,"used").getLong(page)>100)continue;
      byte[] bytes=(byte[])field(pt,"presentation").get(page);
      if(bytes==null)continue;
      int px=field(pt,"x").getInt(page),py=field(pt,"y").getInt(page),pz=field(pt,"z").getInt(page);
      long uploaded=field(pt,"uploaded").getLong(page);
      var index=(WaterBoundaryIndex)field(pt,"boundaryQuads").get(page);
      var data=ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
      var visited=new HashSet<Integer>();
      var pageCorners=new HashMap<Corner,Float>();
      for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++) {
        if(dx==0&&dz==0)continue;
        for(int at=index.begin(dx,dz);at<index.end(dx,dz);at++) {
          int quad=index.entry(at)>>>12;
          if(!visited.add(quad))continue;
          for(int v=0;v<4;v++) {
            int offset=quad*144+v*36;
            if(!WaterCornerPacket.packed(data,offset)||data.get(offset+32)!=127
                ||WaterCornerPacket.opacityTarget(data,offset)!=127)continue;
            float x=data.getFloat(offset),z=data.getFloat(offset+8);
            if(x!=0&&x!=16&&z!=0&&z!=16)continue;
            int age=(byte)(data.getShort(offset+26)&255);
            float progress=Math.clamp((clock-uploaded-age)/100f,0,1);
            float displayed=data.getFloat(offset+4)+data.getShort(offset+24)/16384f*(1-progress);
            var corner=new Corner(px*2+Math.round(x*2),py+Byte.toUnsignedInt(data.get(offset+35)),
                pz*2+Math.round(z*2));
            sampledPages.add(entry.getKey());sampledVertices++;
            var repeated=pageCorners.putIfAbsent(corner,displayed);
            if(repeated!=null) {
              if(Math.abs(repeated-displayed)>1/16384f+.000001f)withinPageMismatches++;
              continue;
            }
            var previous=corners.putIfAbsent(corner,new Sample(entry.getKey(),displayed));
            if(previous==null)continue;
            comparisons++;
            float gap=Math.abs(previous.displayed-displayed);
            maximumGap=Math.max(maximumGap,gap);
            if(gap>1/16384f+.000001f) {
              mismatches++;
              if(examples.size()<16)examples.add(Map.of("tick",ticks,"clock",clock,
                  "x2",corner.x2,"y",corner.y,"z2",corner.z2,"gap",gap,
                  "pageA",previous.page,"pageB",entry.getKey()));
            }
          }
        }
      }
    }
    if(++ticks<600)return;
    var report=new LinkedHashMap<String,Object>();
    report.put("measurement_complete",comparisons>=100&&sampledPages.size()>=2);
    report.put("backend",com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName());
    report.put("samePageDuplicateMismatches",withinPageMismatches);
    report.put("ticks",ticks);report.put("pausedTicksExcluded",pausedTicks);
    report.put("pages",sampledPages.size());report.put("boundaryVertices",sampledVertices);
    report.put("sharedCornerComparisons",comparisons);report.put("mismatches",mismatches);
    report.put("maximumGapBlocks",maximumGap);report.put("examples",examples);
    timings.sort(Long::compare);
    report.put("heightBoundaryWrites",renderer.heightBoundaryWrites-initialWrites);
    report.put("heightBoundaryBytes",renderer.heightBoundaryBytes-initialBytes);
    report.put("sampledHeightBoundaryP50Millis",timings.get(timings.size()/2)/1_000_000d);
    report.put("sampledHeightBoundaryP95Millis",timings.get(timings.size()*95/100)/1_000_000d);
    report.put("sampledHeightBoundaryP99Millis",timings.get(timings.size()*99/100)/1_000_000d);
    report.put("heightBoundaryLifetimeMaxMillis",renderer.maxHeightBoundaryNanos/1_000_000d);
    report.put("timingScope","Last extraction component duration sampled once per client tick, not all frames or complete frame/tick latency; includes CPU submissions but not GPU completion.");
    report.put("scope","Client-tick samples of visible resident CPU upload mirrors using production shader height arithmetic, opaque horizontal top boundaries only; running-world evidence, not GPU pixels, all frames, sides, opacity transitions or complete flicker acceptance.");
    Files.createDirectories(output);
    Files.writeString(output.resolve("water-live-continuity.json"),
        new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");
    output=null;
    System.out.println("WATER_LIVE_CONTINUITY_COMPLETE ticks="+ticks+" pages="+sampledPages.size()
        +" comparisons="+comparisons+" mismatches="+mismatches+" maximumGap="+maximumGap);
    if(comparisons<100||sampledPages.size()<2)throw new AssertionError("Insufficient live shared-corner coverage");
  }

  private WaterLiveContinuityChecks() {}
}
