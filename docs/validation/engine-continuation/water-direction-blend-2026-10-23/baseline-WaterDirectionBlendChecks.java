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
final class WaterDirectionBlendChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private static WaterGpuMesh quad(int direction,int age,boolean active) {
    var b=MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());float fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
    try {
      for(int i=0;i<4;i++) {
        float x=i==2||i==3?1:0,z=i==1||i==2?1:0;
        float u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f)),v=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f));
        b.putFloat(x).putFloat(1).putFloat(z).putInt(0xffffffff);b.putFloat(.125f+u*.5f).putFloat(.25f+v*.5f);
        b.putShort((short)0).putShort((short)0);b.putShort((short)1024).putShort((short)(active?32768|(age<<8):0));
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
    var atlas=device.createTexture("Side UV gradient",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,64,64,1,1);
    var light=device.createTexture("Side UV white light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);
    var av=device.createTextureView(atlas);var lv=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(64,64,false)) {
      for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelABGR(x,y,x>=8&&x<40&&y>=16&&y<48 ? 0xff000000|(((y-16)*8)<<16)|((x-8)*8) : 0xffff00ff);encoder.writeToTexture(atlas,image);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)) {
      for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);
    }
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);
    var projection=device.createBuffer(()->"Side UV identity projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var complete=new AtomicInteger();var pixels=new byte[7][];var meshes=new ArrayList<WaterGpuMesh>();
    for(int i=0;i<7;i++) {
      int which=i;var mesh=quad(i==0?0:4,i<2?0:(i-2)*25,i>=2);meshes.add(mesh);
      var view=new Matrix4f().zero().m00(2).m21(2).m30(-1).m31(-1).m32(.25f).m33(1);
      var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m10(1).m11(.5f).m20(0).m21(0).m01(.125f).m02(.5f).m03(.25f).m12(.5f).m30(0).m31(0));
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
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)image.setPixelABGR(x,y,b.getInt((y*32+x)*4));image.writeToFile(out.resolve("direction-blend-"+which+".png"));
          }
        }catch(Exception e){throw new RuntimeException(e);}
        finally {
          read.close();cv.close();dv.close();color.close();depth.close();
          if(complete.incrementAndGet()==7) {
            for(var m:meshes)m.close();projection.close();av.close();lv.close();atlas.close();light.close();
            try {
              int bad=0,max=0;var rows=new ArrayList<Map<String,Object>>();
              for(int sample=0;sample<5;sample++) {
                int violations=0,maximum=0;float t=sample*.25f;var candidate=pixels[sample+2];
                for(int at=0;at<4096;at++) {
                  int expected=Math.round((pixels[0][at]&255)*(1-t)+(pixels[1][at]&255)*t),error=Math.abs((candidate[at]&255)-expected);
                  maximum=Math.max(maximum,error);if(error>1)violations++;
                }
                rows.add(Map.of("millis",sample*25,"maxChannelError",maximum,"channelsBeyondTolerance",violations));bad+=violations;max=Math.max(max,maximum);
              }
              String result="WATER_DIRECTION_BLEND_PIXEL_RESULT pass="+(bad==0)+" samples=5 renders=7 channelsBeyondTolerance="+bad+" maxChannelError="+max+" tolerance=1 scope=controlledProductionShaderNotFullStreamAcceptance\n";
              Files.writeString(out.resolve("water-direction-blend-pixels.txt"),result);Files.writeString(out.resolve("water-direction-blend-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");System.out.print(result);
              if(bad==0){String pass="WATER_DIRECTION_BLEND_NATIVE_PASS samples=5 renders=7 oldAndNewEndpoints=true intermediateRGBA=true tolerance=1 isolatedGpuReleased=true scope=controlledProductionShaderNotFullStreamAcceptance\n";Files.writeString(out.resolve("water-direction-blend-pass.txt"),pass);System.out.print(pass);}
            }catch(Exception e){throw new RuntimeException(e);}
          }
        }
      },0);
    }
  }
  private WaterDirectionBlendChecks() {}
}
