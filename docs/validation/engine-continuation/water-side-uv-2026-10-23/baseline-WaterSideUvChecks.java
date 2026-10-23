package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Actual production shader pixels at matched displayed heights, with controlled water inputs. */
final class WaterSideUvChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private static WaterGpuMesh quad(float target,float from,int offset,boolean flowing) {
    var b=MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());
    try {
      int[] moving={0,0,1,1};float[] x={-.5f,.5f,.5f,-.5f};
      for(int i=0;i<4;i++) {
        float h=moving[i]==0?0:target;
        b.putFloat(x[i]).putFloat(h-.5f).putFloat(.25f);
        b.putInt(0xffffffff);b.putFloat(i==0||i==3?0:1).putFloat((1-h)*(flowing?.5f:1));
        b.putShort((short)(moving[i]==0?0:Math.round((from-target)*16384))).putShort((short)offset);
        b.putShort((short)(256|(flowing?512:0))).putShort((short)0);
        b.put((byte)127).put((byte)127).put((byte)0).put((byte)0);
      }
      b.flip();return new WaterGpuMesh(b,4);
    }finally {MemoryUtil.memFree(b);}
  }
  static void run(Minecraft mc,Path out)throws Exception {
    Files.createDirectories(out);
    var type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var pipeline=(RenderPipeline)field(type,"PIPELINE").get(null);var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();
    var atlas=device.createTexture("Side UV gradient",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,64,1,1);
    var light=device.createTexture("Side UV white light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);
    var av=device.createTextureView(atlas);var lv=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,64,false)) {
      for(int y=0;y<64;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xff000000|((y*4)<<16)|(x*16));encoder.writeToTexture(atlas,image);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)) {
      for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);
    }
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);
    var projection=device.createBuffer(()->"Side UV identity projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var complete=new AtomicInteger();var pixels=new byte[12][];var meshes=new ArrayList<WaterGpuMesh>();
    for(int i=0;i<12;i++) {
      int which=i;boolean flowing=i>=6;int phase=(i%6)/2;boolean reference=(i&1)==0;
      float displayed=.5f+phase*.25f,target=reference?displayed:1,from=reference?displayed:.5f;int offset=reference?0:-phase*50;
      var mesh=quad(target,from,offset,flowing);meshes.add(mesh);
      var transform=RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(),new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m10(1).m11(.5f).m20(0).m21(0));
      var color=device.createTexture("Side UV color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,32,32,1,1);
      var depth=device.createTexture("Side UV depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,32,32,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);
      try(var pass=encoder.createRenderPass(()->"Production water side UV",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))) {
        pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",av,sampler);pass.bindTexture("Sampler2",lv,sampler);mesh.draw(pass);
      }
      var read=device.createBuffer(()->"Side UV pixels",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,4096);
      encoder.copyTextureToBuffer(color,read,0,()->{
        try {
          try(var mapped=device.createCommandEncoder().mapBuffer(read,true,false);var image=new com.mojang.blaze3d.platform.NativeImage(32,32,false)) {
            var b=mapped.data().order(ByteOrder.nativeOrder());pixels[which]=new byte[4096];b.get(0,pixels[which]);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)image.setPixelABGR(x,y,b.getInt((y*32+x)*4));image.writeToFile(out.resolve("side-uv-"+which+".png"));
          }
        }catch(Exception e){throw new RuntimeException(e);}
        finally {
          read.close();cv.close();dv.close();color.close();depth.close();
          if(complete.incrementAndGet()==12) {
            for(var m:meshes)m.close();projection.close();av.close();lv.close();atlas.close();light.close();
            try {
              int differing=0,covered=0,maskDifferences=0;boolean valid=true;var observations=new ArrayList<Map<String,Object>>();
              for(int p=0;p<6;p++) {
                var a=pixels[p*2];var b=pixels[p*2+1];if(a==null||b==null){valid=false;continue;}int n=0,coverage=0;
                for(int at=0;at<a.length;at+=4) {
                  if(a[at+3]!=0)coverage++;
                  if(a[at+3]!=b[at+3])maskDifferences++;
                  if(a[at]!=b[at]||a[at+1]!=b[at+1]||a[at+2]!=b[at+2]||a[at+3]!=b[at+3])n++;
                }
                if(coverage<100)valid=false;differing+=n;covered+=coverage;
                observations.add(Map.of("pair",p,"flowing",p>=3,"elapsedMillis",(p%3)*50,"differentPixels",n,"coveredPixels",coverage));
              }
              boolean pass=valid&&maskDifferences==0&&differing==0;String record="WATER_SIDE_UV_PIXEL_RESULT pass="+pass+" pairs=6 renders=12 differentPixels="+differing+" coveredPixels="+covered+" geometryMatched="+(maskDifferences==0)+" scope=controlledProductionShaderNotLiveStreamAcceptance\n";
              Files.writeString(out.resolve("water-side-uv-pixels.txt"),record);Files.writeString(out.resolve("water-side-uv-pixel-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(observations)+"\n");System.out.print(record);
              if(pass){String accepted="WATER_SIDE_UV_PIXEL_NATIVE_PASS pairs=6 renders=12 stillAndFlowing=true times=0,50,100 exactPixels=true isolatedGpuReleased=true scope=controlledProductionShaderNotLiveStreamAcceptance\n";Files.writeString(out.resolve("water-side-uv-pass.txt"),accepted);System.out.print(accepted);}
            }catch(Exception e){throw new RuntimeException(e);}
          }
        }
      },0);
    }
  }
  private WaterSideUvChecks() {}
}
