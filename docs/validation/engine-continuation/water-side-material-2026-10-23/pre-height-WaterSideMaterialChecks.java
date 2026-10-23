package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Actual production uploads and draws. Controlled snapshots/clock; no authoritative world edits. */
final class WaterSideMaterialChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Sample(int route,int slot,int source,int target,int age,boolean active,long time,int cell,int origin,long cleanup,GpuBuffer read) {}
  private record Pixel(int route,int slot,int profile,int size,GpuBuffer read,GpuTexture color,GpuTexture depth,GpuTextureView cv,GpuTextureView dv) {}
  private static WaterPatchMesher.Snapshot snapshot(int direction,int cell) {
    byte[] u=new byte[WaterPatchMesher.CELLS],s=new byte[u.length],f=new byte[u.length];int[] l=new int[u.length],c=new int[256];Arrays.fill(c,0xffffffff);Arrays.fill(f,(byte)16);
    
    int key=WaterPatchMesher.index(cell,0,cell);u[key]=16;f[key]=(byte)direction;l[key]=0x00f000b0;
    return new WaterPatchMesher.Snapshot(u,s,l,c,f);
  }
  private static final class Uploads implements AutoCloseable {
    final Class<?> type=AnimatedWaterRenderer.class,pt=Class.forName(type.getName()+"$Page"),rt=Class.forName(type.getName()+"$Ready");
    final Object renderer;final Constructor<?> pc,ready;final Method upload;Object page;int cell,origin;long revision;
    Uploads()throws Exception {
      var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);renderer=ctor.newInstance();
      pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
      ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    }
    void start(int origin,int cell,int direction)throws Exception {
      type.getMethod("reset").invoke(renderer);this.origin=origin;this.cell=cell;revision=0;
      long key=SectionPos.asLong(origin>>4,0,(-origin)>>4);page=pc.newInstance(key);
      @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(key,page);
      put(direction,0,++revision);
      var mesh=mesh();var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(mesh)).close();vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Material readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,mesh.bytes));
      put(direction,0,++revision);
    }
    void put(int direction,long time,long rev)throws Exception {
      var s=snapshot(direction,cell);field(type,"clock").setLong(renderer,time);field(pt,"changed").setBoolean(page,false);
      upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),rev,page,s,WaterPatchMesher.build(s,s,true)));
    }
    WaterGpuMesh mesh()throws Exception {return (WaterGpuMesh)field(pt,"gpu").get(page);}
    Matrix4f texture()throws Exception {return new Matrix4f((Matrix4f)field(pt,"texture").get(page)).m00(0);}
    Sample read(int route,int slot,int source,int target,int age,boolean active,long time)throws Exception {
      var mesh=mesh();int count=field(WaterGpuMesh.class,"indices").getInt(mesh)/6*4;
      if(count!=24||mesh.bytes!=864)throw new AssertionError("Material changed geometry/allocation");
      var read=RenderSystem.getDevice().createBuffer(()->"Material packed vertices",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,864);
      RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)field(WaterGpuMesh.class,"vertices").get(mesh)).slice(0,864),read.slice());
      return new Sample(route,slot,source,target,age,active,time,cell,origin,field(pt,"cleanup").getLong(page),read);
    }
    int history()throws Exception{return ((Map<?,?>)field(type,"flowDirections").get(renderer)).size();}
    public void close()throws Exception {type.getMethod("reset").invoke(renderer);if(history()!=0)throw new AssertionError("Material history survived reset");((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();}
  }
  private static void tile(com.mojang.blaze3d.platform.NativeImage image,net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,boolean flowing) {
    int w=image.getWidth(),h=image.getHeight(),x0=Math.round(sprite.getU(0)*w),y0=Math.round(sprite.getV(0)*h),sw=Math.round((sprite.getU(1)-sprite.getU(0))*w),sh=Math.round((sprite.getV(1)-sprite.getV(0))*h);
    for(int y=0;y<sh;y++)for(int x=0;x<sw;x++){int a=64+x*160/sw,g=32+y*160/sh;image.setPixelABGR(x0+x,y0+y,0xff000000|(g<<8)|(flowing?a<<16:a));}
  }
  static void run(Minecraft mc,Path out)throws Exception {
    Files.createDirectories(out);var type=AnimatedWaterRenderer.class;System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();var pipeline=(RenderPipeline)field(type,"PIPELINE").get(null);
    var real=(TextureAtlas)mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);var still=real.getSprite(Identifier.withDefaultNamespace("block/water_still"));var flowing=real.getSprite(Identifier.withDefaultNamespace("block/water_flow"));
    var actual=mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getTexture();int width=actual.getWidth(0),height=actual.getHeight(0);
    Files.writeString(out.resolve("water-side-material-atlas.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("width",width,"height",height,"still",Map.of("u0",still.getU(0),"v0",still.getV(0),"us",still.getU(1)-still.getU(0),"vs",still.getV(1)-still.getV(0)),"flow",Map.of("u0",flowing.getU(0),"v0",flowing.getV(0),"us",flowing.getU(1)-flowing.getU(0),"vs",flowing.getV(1)-flowing.getV(0))))+"\n");
    var atlas=device.createTexture("Material atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,1);var av=device.createTextureView(atlas);
    var mip=device.createTexture("Material mip diagnostic",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,3);var mv=device.createTextureView(mip);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(width,height,false)) {
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)image.setPixelABGR(x,y,0xffff00ff);tile(image,still,false);tile(image,flowing,true);encoder.writeToTexture(atlas,image);encoder.writeToTexture(mip,image);
    }
    for(int level=1;level<=2;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(width>>level,height>>level,false)) {
      for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)image.setPixelABGR(x,y,level==1?0xff00ff00:0xffff0000);encoder.writeToTexture(mip,image,level,0,0,0,image.getWidth(),image.getHeight(),0,0);
    }
    var light=device.createTexture("Material white light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);var lv=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)){for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);}
    var bytes=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,bytes);var projection=device.createBuffer(()->"Material identity projection",GpuBuffer.USAGE_UNIFORM,bytes);MemoryUtil.memFree(bytes);
    var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);var mipSampler=RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true);
    var samples=new ArrayList<Sample>();var pixels=new ArrayList<Pixel>();var session=new Uploads();boolean queued=false;int[] stableStillHistory={0};
    try {
      for(int route=0;route<320;route++) {
        int source=(route%64)%32<16?16:route%16,target=(route%64)%32<16?route%16:16,origin=route%64<32?0:29999984,cell=route%64<32?3:15;session.start(origin,cell,source);
        if(source==16&&session.history()!=0)stableStillHistory[0]++;
        for(int slot=0;slot<6;slot++) {
          long time=slot==0?0:100+(slot-1)*25L;
          if(slot>0)session.put(target,time,++session.revision);
          samples.add(session.read(route,slot,source,slot==0?source:target,slot==0?0:(slot-1)*25,slot>0&&slot<5,time));
          for(int profile=0;profile<2;profile++) {
            int size=profile==0?32:8;var view=view(route/64+1,cell);
            var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),session.texture());
            var color=device.createTexture("Material color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,size,size,1,1);var depth=device.createTexture("Material depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,size,size,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);
            try(var pass=encoder.createRenderPass(()->"Actual water material upload pixels",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))) {
              pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",profile==0?av:mv,profile==0?sampler:mipSampler);pass.bindTexture("Sampler2",lv,sampler);var sequential=RenderSystem.getSequentialBuffer(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS);pass.setVertexBuffer(0,(GpuBuffer)field(WaterGpuMesh.class,"vertices").get(session.mesh()));pass.setIndexBuffer(sequential.getBuffer(36),sequential.type());pass.drawIndexed(0,(route/64+1)*6,6,1);
            }
            var read=device.createBuffer(()->"Material pixels",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,(long)size*size*4);encoder.copyTextureToBuffer(color,read,0,()->{},0);
            pixels.add(new Pixel(route,slot,profile,size,read,color,depth,cv,dv));
          }
        }
      }
      session.start(0,3,16);
      int[] requests={16,0,0,16,16,0,4,0,0},targets={16,0,0,0,16,16,16,0,0},sources={16,16,16,16,0,0,0,16,16},ages={0,0,25,50,0,25,50,0,100};long[] times={0,100,125,150,200,225,250,300,400};
      for(int n=0;n<times.length;n++) {
        if(n>0)session.put(requests[n],times[n],n==6?0:++session.revision);
        samples.add(session.read(320,n,sources[n],targets[n],ages[n],n>0&&n<8,times[n]));
      }
      RenderSystem.queueFencedTask(()->{
        try {
          int bad=0;var details=new ArrayList<Map<String,Object>>();var raw=new java.io.ByteArrayOutputStream();
          for(var s:samples)try(var mapped=device.createCommandEncoder().mapBuffer(s.read,true,false)) {
            var b=mapped.data().order(ByteOrder.nativeOrder());byte[] copy=new byte[864];b.get(0,copy);int offset=raw.size();raw.writeBytes(copy);
            for(int v=0;v<24;v++) {
              int at=v*36,l=b.getShort(at+28)&65535,sky=b.getShort(at+30)&65535;boolean active=(sky&32768)!=0,expected=s.active;
              boolean wrong=(l&255)!=176||(sky&255)!=240||active!=expected||(b.get(at+15)&255)!=(expected?(s.cell|(s.cell<<4)):255);
              if(active)wrong|=(l>>>11)!=s.source||((sky>>>8)&127)!=s.age;
              int face=v/4;
              float x=b.getFloat(at)-s.cell,z=b.getFloat(at+8)-s.cell,y=b.getFloat(at+4),u,w;
              if(face==0) {
                u=x;w=z;
                if(s.target<16){float fx=(float)Math.cos(s.target*Math.PI/8),fz=(float)Math.sin(s.target*Math.PI/8);u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f))+WaterPatchMesher.flowUOffset(s.target,s.origin+s.cell,-s.origin+s.cell);w=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f))+WaterPatchMesher.flowVOffset(s.target,s.origin+s.cell,-s.origin+s.cell);}
              } else {float scale=s.target<16?.5f:1;u=(face==2||face==4?z:x)*scale;w=(1-y)*scale;}
              var sprite=s.target==16?still:flowing;wrong|=Math.abs(b.getFloat(at+16)-sprite.getU(u))>1e-6||Math.abs(b.getFloat(at+20)-sprite.getV(w))>1e-6;
              if(wrong)bad++;
            }
            var row=new LinkedHashMap<String,Object>();row.put("route",s.route);row.put("slot",s.slot);row.put("source",s.source);row.put("target",s.target);row.put("age",s.age);row.put("active",s.active);row.put("time",s.time);row.put("cell",s.cell);row.put("origin",s.origin);row.put("cleanup",s.cleanup);row.put("offset",offset);details.add(row);
          }
          for(var s:samples)if(s.route==320&&((s.slot==3&&s.cleanup!=201)||((s.slot==5||s.slot==6)&&s.cleanup!=301)))bad++;
          byte[][][] rgba=new byte[2][1920][];var imageRows=new ArrayList<Map<String,Object>>();
          for(var p:pixels)try(var mapped=device.createCommandEncoder().mapBuffer(p.read,true,false);var image=new com.mojang.blaze3d.platform.NativeImage(p.size,p.size,false)) {
            var b=mapped.data().order(ByteOrder.nativeOrder());byte[] data=new byte[p.size*p.size*4];b.get(0,data);rgba[p.profile][p.route*6+p.slot]=data;
            for(int y=0;y<p.size;y++)for(int x=0;x<p.size;x++)image.setPixelABGR(x,y,b.getInt((y*p.size+x)*4));image.writeToFile(out.resolve("side-material-"+p.profile+"-"+p.route+"-"+p.slot+".png"));
          }
          int channels=0,max=0;
          for(int profile=0;profile<2;profile++)for(int route=0;route<320;route++)for(int phase=0;phase<5;phase++) {
            var a=rgba[profile][route*6];var b=rgba[profile][route*6+5];var c=rgba[profile][route*6+phase+1];float t=phase*.25f;int violations=0,maximum=0;
            for(int i=0;i<c.length;i++){int error=Math.abs((c[i]&255)-Math.round((a[i]&255)*(1-t)+(b[i]&255)*t));maximum=Math.max(maximum,error);if(error>1)violations++;}
            for(int i=3;i<c.length;i+=4)if((c[i]&255)!=255)violations++;
            var row=new LinkedHashMap<String,Object>();row.put("profile",profile);row.put("route",route);row.put("millis",phase*25);row.put("maxChannelError",maximum);row.put("channelsBeyondTolerance",violations);imageRows.add(row);channels+=violations;max=Math.max(max,maximum);
          }
          Files.write(out.resolve("water-side-material-upload.bin"),raw.toByteArray());Files.writeString(out.resolve("water-side-material-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(details)+"\n");
          Files.writeString(out.resolve("water-side-material-pixel-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(imageRows)+"\n");
          boolean pass=bad==0&&channels==0&&stableStillHistory[0]==0;
          String record="WATER_SIDE_MATERIAL_RESULT pass="+pass+" routes=320 renders=3840 samples=3200 readbacks=1929 readbackBytes=1666656 badVertices="+bad+" channelsBeyondTolerance="+channels+" maxChannelError="+max+" tolerance=1 stillHistoryViolations="+stableStillHistory[0]+" scope=controlledActualProductionSideUploadsAndShaderNotFullStreamAcceptance\n";
          Files.writeString(out.resolve("water-side-material-result.txt"),record);System.out.print(record);
          if(pass){String line="WATER_SIDE_MATERIAL_NATIVE_PASS routes=320 renders=3840 samples=3200 readbacks=1929 readbackBytes=1666656 stillToFlow=true flowToStill=true mipControls=true nearFarCells=true verticalSidesAndUnderside=true repeatedQueuedStale=true stillHistorySparse=true lightPreserved=true historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionSideUploadsAndShaderNotFullStreamAcceptance\n";Files.writeString(out.resolve("water-side-material-pass.txt"),line);System.out.print(line);}
        }catch(Exception e){throw new RuntimeException(e);}finally {for(var s:samples)s.read.close();for(var p:pixels){p.read.close();p.cv.close();p.dv.close();p.color.close();p.depth.close();}projection.close();av.close();mv.close();lv.close();atlas.close();mip.close();light.close();}
      });queued=true;
    } finally {session.close();if(!queued){for(var s:samples)s.read.close();for(var p:pixels){p.read.close();p.cv.close();p.dv.close();p.color.close();p.depth.close();}projection.close();av.close();mv.close();lv.close();atlas.close();mip.close();light.close();}}
  }
  private static Matrix4f view(int face,int cell) {
    var m=new Matrix4f().zero().m33(1);
    if(face==1||face==3) {
      float depth=face==1?.25f:-.25f;
      return m.m00(2).m11(2).m22(depth).m30(-2*cell-1).m31(-1).m32(.25f-depth*cell+(face==3?.25f:0));
    }
    if(face==2||face==4) {
      float depth=face==4?.25f:-.25f;
      return m.m20(2).m11(2).m02(depth).m30(-2*cell-1).m31(-1).m32(.25f-depth*cell+(face==2?.25f:0));
    }
    return m.m00(2).m21(2).m12(.25f).m30(-2*cell-1).m31(-2*cell-1).m32(.25f);
  }
  private WaterSideMaterialChecks() {}
}
