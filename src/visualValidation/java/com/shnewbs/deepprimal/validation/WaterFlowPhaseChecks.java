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

/** Production shader and phase helper: two neighboring tiles versus one continuous reference; no live-world edits. */
final class WaterFlowPhaseChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private static final int[] DIRECTIONS={0,2,4,6,10,14};
  private static final int[] ORIGINS={15,-1,-16,29999998,-29999999,31};
  private static float phase(boolean u,int direction,int x,int z,boolean reference)throws Exception {
    if(reference) {
      double fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
      double v=.5*(u?fz*x-fx*z:fx*x+fz*z);return (float)(v-Math.floor(v));
    }
    try{return (float)com.shnewbs.deepprimal.core.WaterPatchMesher.class.getMethod(u?"flowUOffset":"flowVOffset",int.class,int.class,int.class).invoke(null,direction,x,z);}
    catch(NoSuchMethodException baseline){return 0;}
  }
  private static WaterGpuMesh quad(int direction,int origin,boolean reference,boolean mip)throws Exception {
    int count=reference?4:8;var b=MemoryUtil.memCalloc(count*36).order(ByteOrder.nativeOrder());
    float fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
    try {
      for(int cell=0;cell<(reference?1:2);cell++)for(int corner=0;corner<4;corner++) {
        float x=cell+((corner==2||corner==3)?(reference?2:1):0),z=(corner==1||corner==2)?1:0;
        float localX=reference?x:x-cell;
        float u=.5f+.5f*(fz*(localX-.5f)-fx*(z-.5f))+phase(true,direction,origin+(reference?0:cell),-17,reference);
        float v=.5f+.5f*(fx*(localX-.5f)+fz*(z-.5f))+phase(false,direction,origin+(reference?0:cell),-17,reference);
        b.putFloat(x-1).putFloat(z*2-1).putFloat(.25f);b.putInt(0xffffffff);
        b.putFloat(mip?u:.125f+u*.5f).putFloat(mip?v:.25f+v*.5f);b.putShort((short)0).putShort((short)0);b.putShort((short)(mip&&reference?0:1024)).putShort((short)0);
        b.put((byte)127).put((byte)127).put((byte)0).put((byte)0);
      }
      b.flip();return new WaterGpuMesh(b,count);
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
    var mipAtlas=device.createTexture("Phase diagnostic mip chain",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,64,64,1,3);var mv=device.createTextureView(mipAtlas);
    for(int level=0;level<3;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(64>>level,64>>level,false)) {
      int size=64>>level;for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixelABGR(x,y,level==1?0xff00ff00:level==2?0xffff0000:0xff000000|((y*4)<<16)|(x*4));
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
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var complete=new AtomicInteger();var pixels=new byte[24][];var meshes=new ArrayList<WaterGpuMesh>();
    for(int i=0;i<24;i++) {
      int which=i;int pair=(i%12)/2;boolean reference=(i&1)==0,mip=i>=12;
      var mesh=quad(DIRECTIONS[pair],ORIGINS[pair],reference,mip);meshes.add(mesh);
      var transform=RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(),new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m10(1).m11(.5f).m20(0).m21(0).m01(mip?0:.125f).m02(mip?1:.5f).m03(mip?0:.25f).m12(mip?1:.5f));
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
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)image.setPixelABGR(x,y,b.getInt((y*32+x)*4));image.writeToFile(out.resolve("flow-phase-"+which+".png"));
          }
        }catch(Exception e){throw new RuntimeException(e);}
        finally {
          read.close();cv.close();dv.close();color.close();depth.close();
          if(complete.incrementAndGet()==24) {
            for(var m:meshes)m.close();projection.close();av.close();lv.close();atlas.close();light.close();mv.close();mipAtlas.close();
            try {
              int differing=0,covered=0,maskDifferences=0,beyondTolerance=0;boolean valid=true;var observations=new ArrayList<Map<String,Object>>();
              for(int p=0;p<12;p++) {
                var a=pixels[p*2];var b=pixels[p*2+1];if(a==null||b==null){valid=false;continue;}int n=0,coverage=0,bad=0,maxError=0,tolerance=p>=6?1:0;
                for(int at=0;at<a.length;at+=4) {
                  if(a[at+3]!=0)coverage++;
                  if(a[at+3]!=b[at+3])maskDifferences++;
                  int error=0;for(int channel=0;channel<4;channel++)error=Math.max(error,Math.abs((a[at+channel]&255)-(b[at+channel]&255)));
                  if(error>0)n++;if(error>tolerance)bad++;maxError=Math.max(maxError,error);
                }
                if(coverage<100)valid=false;differing+=n;covered+=coverage;beyondTolerance+=bad;
                observations.add(Map.of("pair",p,"direction",DIRECTIONS[p%6],"originX",ORIGINS[p%6],"mipControl",p>=6,"differentPixels",n,"coveredPixels",coverage,"maxChannelError",maxError,"beyondTolerancePixels",bad,"tolerance",tolerance));
              }
              boolean pass=valid&&maskDifferences==0&&beyondTolerance==0;String record="WATER_FLOW_PHASE_PIXEL_RESULT pass="+pass+" pairs=12 renders=24 differentPixels="+differing+" beyondTolerancePixels="+beyondTolerance+" coveredPixels="+covered+" geometryMatched="+(maskDifferences==0)+" scope=controlledPhaseHelperAndProductionShaderNotFullStreamAcceptance\n";
              Files.writeString(out.resolve("water-flow-phase-pixels.txt"),record);Files.writeString(out.resolve("water-flow-phase-pixel-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(observations)+"\n");System.out.print(record);
              if(pass){String accepted="WATER_FLOW_PHASE_PIXEL_NATIVE_PASS pairs=12 renders=24 directions=0,2,4,6,10,14 worldPhase=true explicitGradientMipControl=true baseExact=true mipTolerance=1 isolatedGpuReleased=true scope=controlledPhaseHelperAndProductionShaderNotFullStreamAcceptance\n";Files.writeString(out.resolve("water-flow-phase-pass.txt"),accepted);System.out.print(accepted);}
            }catch(Exception e){throw new RuntimeException(e);}
          }
        }
      },0);
    }
  }
  private WaterFlowPhaseChecks() {}
}
