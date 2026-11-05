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
  private static WaterGpuMesh quad(int direction,int source,int age,boolean active,int originX,int originZ,boolean mip) {
    var b=MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());float fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
    try {
      for(int i=0;i<4;i++) {
        int cell=originX==0?0:15;
        float x=i==2||i==3?1:0,z=i==1||i==2?1:0;
        float u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f)),v=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f));
        b.putFloat(x+cell).putFloat(1).putFloat(z+cell).putInt(active?((cell|(cell<<4))<<24)|0xffffff:0xffffffff);u+=com.shnewbs.deepprimal.core.WaterPatchMesher.flowUOffset(direction,originX+cell,originZ+cell);v+=com.shnewbs.deepprimal.core.WaterPatchMesher.flowVOffset(direction,originX+cell,originZ+cell);
        b.putFloat(mip?u:.125f+u*.5f).putFloat(mip?v:.25f+v*.5f);
        b.putShort((short)0).putShort((short)0);b.putShort((short)(1024|(active?source<<11:0))).putShort((short)(active?32768|(age<<8):0));
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
    var mipAtlas=device.createTexture("Blend mip diagnostic",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,128,128,1,3);var mv=device.createTextureView(mipAtlas);
    for(int level=0;level<3;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(128>>level,128>>level,false)) {
      int size=128>>level;for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixelABGR(x,y,level==1?0xff00ff00:level==2?0xffff0000:0xff000000|((y*2)<<16)|(x*2));
      encoder.writeToTexture(mipAtlas,image,level,0,0,0,size,size,0,0);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(64,64,false)) {
      for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelABGR(x,y,x>=8&&x<40&&y>=16&&y<48 ? 0xff000000|(((y-16)*8)<<16)|((x-8)*8) : 0xffff00ff);encoder.writeToTexture(atlas,image);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)) {
      for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);
    }
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);
    var projection=device.createBuffer(()->"Side UV identity projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var complete=new AtomicInteger();var pixels=new byte[336][];var meshes=new ArrayList<WaterGpuMesh>();
    for(int i=0;i<336;i++) {
      int which=i,group=i/7,slot=i%7,source=group%16,target=(source+4)&15,originX=group<16?0:29999984,originZ=-originX;boolean mip=group>=32;var mesh=quad(slot==0?source:target,source,slot<2?0:(slot-2)*25,slot>=2,originX,originZ,mip);meshes.add(mesh);
      var view=new Matrix4f().zero().m00(2).m21(2).m30(originX==0?-1:-31).m31(originX==0?-1:-31).m32(.25f).m33(1);
      var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m10(1).m11(.5f).m20(0).m21(0).m01(mip?0:.125f).m02(mip?1:.5f).m03(mip?0:.25f).m12(mip?1:.5f).m30(originX).m31(originZ));
      var color=device.createTexture("Side UV color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,32,32,1,1);
      var depth=device.createTexture("Side UV depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,32,32,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);
      try(var pass=encoder.createRenderPass(()->"Production water side UV",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))) {
        pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",mip?mv:av,mip?RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true):sampler);pass.bindTexture("Sampler2",lv,sampler);mesh.draw(pass);
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
          if(complete.incrementAndGet()==336) {
            for(var m:meshes)m.close();projection.close();av.close();lv.close();atlas.close();light.close();mv.close();mipAtlas.close();
            try {
              int bad=0,max=0;var rows=new ArrayList<Map<String,Object>>();
              for(int sampleGroup=0;sampleGroup<48;sampleGroup++)for(int sample=0;sample<5;sample++) {
                int violations=0,maximum=0;float t=sample*.25f;var candidate=pixels[sampleGroup*7+sample+2];
                for(int at=0;at<4096;at++) {
                  int expected=Math.round((pixels[sampleGroup*7][at]&255)*(1-t)+(pixels[sampleGroup*7+1][at]&255)*t),error=Math.abs((candidate[at]&255)-expected);
                  maximum=Math.max(maximum,error);if(error>1)violations++;
                }
                rows.add(Map.of("sourceDirection",sampleGroup%16,"originX",sampleGroup<16?0:29999984,"cellX",sampleGroup<16?0:15,"cellZ",sampleGroup<16?0:15,"millis",sample*25,"mipControl",sampleGroup>=32,"maxChannelError",maximum,"channelsBeyondTolerance",violations));bad+=violations;max=Math.max(max,maximum);
              }
              String result="WATER_DIRECTION_BLEND_PIXEL_RESULT pass="+(bad==0)+" samples=240 renders=336 channelsBeyondTolerance="+bad+" maxChannelError="+max+" tolerance=1 scope=controlledProductionShaderNotFullStreamAcceptance\n";
              Files.writeString(out.resolve("water-direction-blend-pixels.txt"),result);Files.writeString(out.resolve("water-direction-blend-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");System.out.print(result);
              if(bad==0){String pass="WATER_DIRECTION_BLEND_NATIVE_PASS samples=240 renders=336 directions=16 origins=2 mipControls=true oldAndNewEndpoints=true intermediateRGBA=true tolerance=1 isolatedGpuReleased=true scope=controlledProductionShaderNotFullStreamAcceptance\n";Files.writeString(out.resolve("water-direction-blend-pass.txt"),pass);System.out.print(pass);}
            }catch(Exception e){throw new RuntimeException(e);}
          }
        }
      },0);
    }
  }
  private WaterDirectionBlendChecks() {}
}
