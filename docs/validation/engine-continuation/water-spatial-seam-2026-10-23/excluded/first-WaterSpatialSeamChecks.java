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


/** Exact shared-edge limits, not a comparison of different adjacent pixel locations.
 * Diagnostic vertices exercise the production phase helper and production shader;
 * this deliberately does not claim actual-upload or complete stream acceptance. */
final class WaterSpatialSeamChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Probe(int group,int a,int b,int axis,int origin,int mode,int side) {}
  private record Read(Probe probe,GpuBuffer read,GpuTexture color,GpuTexture depth,GpuTextureView cv,GpuTextureView dv,WaterGpuMesh mesh) {}
  private static float[] uv(int direction,int x,int z,float u,float v) {
    float fx=com.shnewbs.deepprimal.core.WaterPatchMesher.flowX(direction),fz=com.shnewbs.deepprimal.core.WaterPatchMesher.flowZ(direction);
    return new float[]{.5f+.5f*(fz*(u-.5f)-fx*(v-.5f))+com.shnewbs.deepprimal.core.WaterPatchMesher.flowUOffset(direction,x,z),.5f+.5f*(fx*(u-.5f)+fz*(v-.5f))+com.shnewbs.deepprimal.core.WaterPatchMesher.flowVOffset(direction,x,z)};
  }
  private static float fract(float x){return x-(float)Math.floor(x);}
  private static WaterGpuMesh mesh(Probe p) {
    var data=MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());
    try {
      for(int corner=0;corner<4;corner++) {
        float x=corner>=2?1:0,z=corner==1||corner==2?1:0;float[] v;
        if(p.mode==2) {
          // Uniform direction: independent fidelity gate catches folds/reversal after fract.
          v=uv(p.a,p.origin+1,-p.origin,x,z);
          if(p.side==1){v[0]=fract(v[0]);v[1]=fract(v[1]);}
        } else {
          float t=z;
          var left=uv(p.a,p.origin,-p.origin,p.axis==0?1:t,p.axis==0?t:1);
          var right=uv(p.b,p.origin+(p.axis==0?1:0),-p.origin+(p.axis==1?1:0),p.axis==0?0:t,p.axis==0?t:0);
          v=p.side==0?left:right;
          if(p.mode==1)v=new float[]{(fract(left[0])+fract(right[0]))*.5f,(fract(left[1])+fract(right[1]))*.5f};
        }
        data.putFloat(x*2-1).putFloat(z*2-1).putFloat(.25f).putInt(0xffffffff);
        data.putFloat(.125f+v[0]*.5f).putFloat(.25f+v[1]*.5f);
        data.putShort((short)0).putShort((short)0).putShort((short)1024).putShort((short)0);
        data.put((byte)127).put((byte)127).put((byte)0).put((byte)0);
      }
      data.flip();return new WaterGpuMesh(data,4);
    }finally{MemoryUtil.memFree(data);}
  }
  static void run(Minecraft mc,Path out)throws Exception {
    Files.createDirectories(out);var type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();var pipeline=(RenderPipeline)field(type,"PIPELINE").get(null);
    var atlas=device.createTexture("Spatial phase diagnostic",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,64,64,1,1);
    var light=device.createTexture("Spatial white light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);var av=device.createTextureView(atlas);var lv=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(64,64,false)) {
      for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelABGR(x,y,x>=8&&x<40&&y>=16&&y<48?0xff000000|(((y-16)*8)<<16)|((x-8)*8):0xffff00ff);encoder.writeToTexture(atlas,image);
    }
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)){for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);}
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);var projection=device.createBuffer(()->"Spatial projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var reads=new ArrayList<Read>();var probes=new ArrayList<Probe>();int group=0;
    for(int mode=0;mode<2;mode++)for(int origin:new int[]{0,-16,29999984,-29999984})for(int axis=0;axis<2;axis++)for(int a=0;a<16;a++)for(int b=0;b<16;b++){
      for(int side=0;side<2;side++)probes.add(new Probe(group,a,b,axis,origin,mode,side));group++;
    }
    for(int origin:new int[]{0,-16,29999984,-29999984})for(int a=0;a<16;a++){for(int side=0;side<2;side++)probes.add(new Probe(group,a,a,0,origin,2,side));group++;}
    for(var p:probes) {
      var mesh=mesh(p);var transform=RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(),new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m01(.125f).m02(.5f).m03(.25f).m12(.5f));
      var color=device.createTexture("Spatial color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,8,32,1,1);var depth=device.createTexture("Spatial depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,8,32,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);
      try(var pass=encoder.createRenderPass(()->"Production water shared edge limit",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",av,sampler);pass.bindTexture("Sampler2",lv,sampler);mesh.draw(pass);}
      var read=device.createBuffer(()->"Spatial pixels",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,1024);encoder.copyTextureToBuffer(color,read,0,()->{},0);reads.add(new Read(p,read,color,depth,cv,dv,mesh));
    }
    RenderSystem.queueFencedTask(()->{
      try {
        var raw=new java.io.ByteArrayOutputStream();var rows=new ArrayList<Map<String,Object>>();byte[] previous=null;int stableBad=0,seams=0,candidateBad=0,fidelityBad=0,coverageBad=0,max=0;
        for(var r:reads)try(var mapped=device.createCommandEncoder().mapBuffer(r.read,true,false)) {
          byte[] data=new byte[1024];mapped.data().get(0,data);int offset=raw.size();raw.writeBytes(data);var p=r.probe;
          for(int at=3;at<data.length;at+=4)if((data[at]&255)!=255)coverageBad++;
          if(p.side==0){previous=data;continue;}
          int bad=0,maximum=0,different=0;for(int at=0;at<data.length;at++){int e=Math.abs((data[at]&255)-(previous[at]&255));if(e>8)bad++;if(e>0)different++;maximum=Math.max(maximum,e);}
          if(p.mode==0){if(p.a==p.b&&bad>0)stableBad++;if(p.a!=p.b&&bad>0)seams++;}else if(p.mode==1&&bad>0)candidateBad++;else if(p.mode==2&&bad>0)fidelityBad++;
          max=Math.max(max,maximum);var row=new LinkedHashMap<String,Object>();row.put("group",p.group);row.put("a",p.a);row.put("b",p.b);row.put("axis",p.axis);row.put("origin",p.origin);row.put("mode",p.mode);row.put("right_offset",offset);row.put("left_offset",offset-1024);row.put("differentChannels",different);row.put("channelsBeyondOneTexel",bad);row.put("maxChannelError",maximum);rows.add(row);
          if((p.mode==0&&p.a==0&&p.b==4&&p.origin==0)||(p.mode==1&&p.a==0&&p.b==4&&p.origin==0)||(p.mode==2&&p.a==0&&p.origin==0))for(int side=0;side<2;side++)try(var image=new com.mojang.blaze3d.platform.NativeImage(8,32,false)){
            var b=ByteBuffer.wrap(side==0?previous:data).order(ByteOrder.nativeOrder());for(int y=0;y<32;y++)for(int x=0;x<8;x++)image.setPixelABGR(x,y,b.getInt((y*8+x)*4));image.writeToFile(out.resolve("spatial-"+p.mode+"-"+p.axis+"-"+side+".png"));
          }
        }
        Files.write(out.resolve("water-spatial-pixels.bin"),raw.toByteArray());Files.writeString(out.resolve("water-spatial-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
        boolean pass=stableBad==0&&seams>0&&candidateBad==0&&fidelityBad>0&&coverageBad==0;
        var result=new LinkedHashMap<String,Object>();result.put("audit_pass",pass);result.put("production_spatial_continuity_pass",false);result.put("candidate_promotable",false);result.put("same_direction_failed_routes",stableBad);result.put("different_direction_seam_routes",seams);result.put("naive_shared_uv_failed_edge_routes",candidateBad);result.put("naive_shared_uv_failed_uniform_fidelity_routes",fidelityBad);result.put("coverage_bad_pixels",coverageBad);result.put("draws",reads.size());result.put("comparisons",rows.size());result.put("max_channel_error",max);result.put("same_direction_tolerance",8);result.put("scope","phase-helper and production shader diagnostic; one-texel control tolerance; no actual-upload/full-stream/FPS acceptance");
        Files.writeString(out.resolve("water-spatial-result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");System.out.println("WATER_SPATIAL_RESULT auditPass="+pass+" productionSpatialContinuityPass=false differentDirectionSeams="+seams+" stableFailures="+stableBad+" candidateFidelityFailures="+fidelityBad+" draws="+reads.size());
        if(pass)System.out.println("WATER_SPATIAL_AUDIT_PASS defectReproduced=true naiveUvCandidateRejected=true productionFixPromoted=false");
      }catch(Exception e){throw new RuntimeException(e);}finally{for(var r:reads){r.read.close();r.cv.close();r.dv.close();r.color.close();r.depth.close();r.mesh.close();}projection.close();av.close();lv.close();atlas.close();light.close();}
    });
  }
  private WaterSpatialSeamChecks(){}
}
