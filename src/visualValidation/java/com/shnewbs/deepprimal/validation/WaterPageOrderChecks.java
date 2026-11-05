package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.textures.GpuTexture;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Production-pipeline pixel A/B plus live extracted pages under map-order permutations. */
final class WaterPageOrderChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private static volatile boolean active,pixelsReady;
  private static volatile int phase,minVisible;
  private static int stableFrames;
  private static Set<Long> previousKeys=Set.of();
  private static Field pageKey;
  private static final AtomicInteger images=new AtomicInteger();
  private static Path output;
  private static long started;
  private static List<Long> original;
  private static Map<Long,Object> pages;
  private static ContextKey<List<?>> visibleKey;
  private static Field bounds;
  private static final String[] pixels=new String[2];

  private static WaterGpuMesh quad(float depth,boolean red) {
    var b=MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());
    try {
      for(float[] xy:new float[][]{{-.8f,-.8f},{-.8f,.8f},{.8f,.8f},{.8f,-.8f}}) {
        b.putFloat(xy[0]).putFloat(xy[1]).putFloat(depth);
        b.put((byte)(red?255:0)).put((byte)0).put((byte)(red?0:255)).put((byte)255);
        b.putFloat(.5f).putFloat(.5f).putShort((short)0).putShort((short)0).putInt(0);
        b.put((byte)127).put((byte)127).put((byte)0).put((byte)0);
      }
      b.flip();return new WaterGpuMesh(b,4);
    }finally {MemoryUtil.memFree(b);}
  }

  @SuppressWarnings("unchecked")
  static void start(Minecraft mc,Path dir)throws Exception {
    if(active)throw new AssertionError("Page ordering fixture already active");
    output=dir;Files.createDirectories(output);phase=0;stableFrames=0;previousKeys=Set.of();minVisible=Integer.MAX_VALUE;images.set(0);pixelsReady=false;original=null;
    var type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    pages=(Map<Long,Object>)field(type,"pages").get(AnimatedWaterRenderer.INSTANCE);
    visibleKey=(ContextKey<List<?>>)field(type,"VISIBLE").get(null);
    bounds=field(Class.forName(type.getName()+"$Page"),"bounds");
    pageKey=field(Class.forName(type.getName()+"$Page"),"key");
    Files.writeString(output.resolve("water-page-order-state.txt"),
        "WATER_PAGE_ORDER_STATE backend="+RenderSystem.getDevice().getBackendName()
        +" animated="+com.shnewbs.deepprimal.settings.WaterVisualConfig.INTERPOLATE.get()
        +" waves="+com.shnewbs.deepprimal.settings.WaterVisualConfig.WAVES.get()
        +" lod="+com.shnewbs.deepprimal.settings.LodConfig.ENABLED.get()
        +" ownsWater="+AnimatedWaterRenderer.ownsWater()+" stage=AfterTranslucentBlocks\n");
    started=System.nanoTime();active=true;
    pixelAB((RenderPipeline)field(type,"PIPELINE").get(null));
  }

  private static void pixelAB(RenderPipeline pipeline)throws Exception {
    var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();
    var atlas=device.createTexture("Water order pixel atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,1,1,1,1);
    var light=device.createTexture("Water order pixel light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);
    var atlasView=device.createTextureView(atlas);var lightView=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(1,1,false)) {
      image.setPixelABGR(0,0,0x80ffffff);encoder.writeToTexture(atlas,image);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)) {
      for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);
      encoder.writeToTexture(light,image);
    }
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);
    var projection=device.createBuffer(()->"Water order identity projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var near=quad(.25f,true);var far=quad(.75f,false);
    var transform=RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(),new Vector4f(1),new Vector3f(),new Matrix4f().m00(1000));
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.NEAREST);
    var completed=new AtomicInteger();var valid=new boolean[2];
    for(int index=0;index<2;index++) {
      final int which=index;
      var color=device.createTexture("Water order color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,32,32,1,1);
      var depth=device.createTexture("Water order depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,32,32,1,1);
      var cv=device.createTextureView(color);var dv=device.createTextureView(depth);
      try(var pass=encoder.createRenderPass(()->"Production water order A/B",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))) {
        pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);
        pass.bindTexture("Sampler0",atlasView,sampler);pass.bindTexture("Sampler2",lightView,sampler);
        if(which==0){near.draw(pass);far.draw(pass);}else {far.draw(pass);near.draw(pass);}
      }
      var read=device.createBuffer(()->"Water order pixels",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,4096);
      encoder.copyTextureToBuffer(color,read,0,()->{
        try {
          try(var view=device.createCommandEncoder().mapBuffer(read,true,false);
              var image=new com.mojang.blaze3d.platform.NativeImage(32,32,false)) {
            var data=view.data().order(ByteOrder.nativeOrder());int at=(16*32+16)*4;
            int r=data.get(at)&255,g=data.get(at+1)&255,b=data.get(at+2)&255,a=data.get(at+3)&255;
            pixels[which]=r+","+g+","+b+","+a;
            System.out.println("WATER_ORDER_PIXEL_OBSERVED order="+(which==0?"near-first":"far-first")+" rgba="+pixels[which]);
            if(r<100 || (which==0?b>4:b<50))throw new AssertionError("Production water pixel order control differs: "+pixels[which]);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)image.setPixelABGR(x,y,data.getInt((y*32+x)*4));
            image.writeToFile(output.resolve("order-pixels-"+(which==0?"near-first":"far-first")+".png"));
            valid[which]=true;
          }
        }catch(Exception e){throw new RuntimeException(e);}
        finally {
          read.close();cv.close();dv.close();color.close();depth.close();
          if(completed.incrementAndGet()==2) {
            near.close();far.close();projection.close();atlasView.close();lightView.close();atlas.close();light.close();
            if(!valid[0] || !valid[1]){active=false;return;}
            try {Files.writeString(output.resolve("water-order-pixels.txt"),"WATER_ORDER_PIXEL_AB_PASS nearFirst="+pixels[0]+" farFirst="+pixels[1]+" productionPipeline=true scope=twoControlledOverlappingWaterQuads\n");}
            catch(Exception e){throw new RuntimeException(e);}
            pixelsReady=true;
          }
        }
      },0);
    }
  }

  private static double distance(Object page,Vec3 camera) {
    try {return ((AABB)bounds.get(page)).distanceToSqr(camera);}
    catch(Exception e){throw new RuntimeException(e);}
  }
  private static void permute(Vec3 camera,boolean nearFirst) {
    var order=new ArrayList<>(pages.entrySet());order.sort(Comparator.comparingDouble(e->distance(e.getValue(),camera)));
    if(!nearFirst)Collections.reverse(order);pages.clear();for(var e:order)pages.put(e.getKey(),e.getValue());
  }
  private static void restore() {
    if(original==null)return;var order=new LinkedHashMap<Long,Object>();
    for(long key:original)if(pages.containsKey(key))order.put(key,pages.get(key));
    for(var e:pages.entrySet())order.putIfAbsent(e.getKey(),e.getValue());pages.clear();pages.putAll(order);original=null;
  }
  private static synchronized void finish()throws Exception {
    if(!active || phase!=4 || images.get()!=3)return;
    active=false;
    String record="WATER_PAGE_ORDER_NATIVE_PASS liveFrames=3 warmupStableFrames=12 mapPermutations=near-far-near minVisible="+minVisible+" screenshots=3 productionPixelAB=true originalMapOrderRestored=true noWorldEdits=true scope=pageOrderNotFullTransparencyOrFlickerAcceptance\n";
    Files.writeString(output.resolve("water-page-order.txt"),record);System.out.print(record);
  }
  @SubscribeEvent(priority=EventPriority.LOWEST)
  public void frame(RenderLevelStageEvent.AfterTranslucentBlocks event)throws Exception {
    if(!active)return;
    if(System.nanoTime()-started>90_000_000_000L){restore();throw new AssertionError("Water page-order fixture exceeded 90-second bound");}
    if(!pixelsReady || phase==4)return;
    var visible=event.getLevelRenderState().getRenderData(visibleKey);
    if(visible==null || visible.size()<8)return;
    var camera=event.getLevelRenderState().cameraRenderState.pos;
    if(phase==0){
      var keys=new HashSet<Long>();for(var page:visible)keys.add(pageKey.getLong(page));
      stableFrames=keys.equals(previousKeys)?stableFrames+1:1;previousKeys=keys;
      if(stableFrames<12)return;
      original=new ArrayList<>(pages.keySet());permute(camera,true);phase=1;return;
    }
    minVisible=Math.min(minVisible,visible.size());double previous=Double.POSITIVE_INFINITY;
    for(int i=0;i<visible.size();i++) {
      double next=distance(visible.get(i),camera);
      if(next>previous+.00001) {
        System.out.println("WATER_PAGE_ORDER_OBSERVED phase="+phase+" inversion="+i+" previousDistance="+previous+" nextDistance="+next+" visible="+visible.size());
        restore();throw new AssertionError("Live water pages draw nearer geometry before farther translucent pages");
      }
      previous=next;
    }
    final int imageIndex=phase;
    String line="WATER_PAGE_ORDER_FRAME_PASS frame="+phase+" visible="+visible.size()+" nanos="+System.nanoTime()+" gameTime="+Minecraft.getInstance().level.getGameTime()+" loadedChunks="+Minecraft.getInstance().level.getChunkSource().getLoadedChunksCount()+" camera="+camera+" waterBytes="+AnimatedWaterRenderer.INSTANCE.residentBytes+" animationTime="+AnimatedWaterRenderer.INSTANCE.animationTime()
        +" waveBytes="+com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE.residentBytes()
        +" wavePending="+com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE.pendingSamples()
        +" lodActive="+com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.active()
        +" lodBytes="+com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.INSTANCE.residentBytes
        +" lodVisible="+com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.INSTANCE.visibleChunks
        +" lodTriangles="+com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.INSTANCE.drawnTriangles+"\n";
    Files.writeString(output.resolve("water-page-order-frames.txt"),line,StandardOpenOption.CREATE,StandardOpenOption.APPEND);System.out.print(line);
    Screenshot.grab(output.toFile(),"order-live-frame-"+imageIndex+".png",Minecraft.getInstance().getMainRenderTarget(),1,message->{
      try {
        if(!Files.isRegularFile(output.resolve("screenshots/order-live-frame-"+imageIndex+".png")))throw new AssertionError("Page-order frame image not saved");
        images.incrementAndGet();finish();
      }catch(Exception e){throw new RuntimeException(e);}
    });
    if(phase<3)permute(camera,phase==2);else restore();
    phase++;finish();
  }
}
