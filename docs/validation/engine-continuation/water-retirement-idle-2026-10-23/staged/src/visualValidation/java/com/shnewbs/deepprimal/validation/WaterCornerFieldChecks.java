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


/** Actual-upload edge limits. Geometry is reprojected only for an identical-point comparison;
 * this is not a claim of full scene/stream/FPS or partial neighbor-upload atomicity. */
final class WaterCornerFieldChecks {
  private static Field field(Class<?> c,String n)throws Exception{var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Input(int route,int side,int a,int b,int axis,int origin,int cellX,int cellZ,long time,long uploaded,long uploadNanos,int topVertex,int count,Matrix4f texture,GpuBuffer read){}
  private record Pixels(Input in,int kind,int endpoint,int width,int height,GpuBuffer read,GpuTexture color,GpuTexture depth,GpuTextureView cv,GpuTextureView dv,WaterGpuMesh mesh){}
  private static WaterPatchMesher.Snapshot snapshot(int px,int pz,int leftX,int leftZ,int axis,int a,int b){
    byte[] u=new byte[WaterPatchMesher.CELLS],f=new byte[u.length];Arrays.fill(f,(byte)16);int[] l=new int[u.length],colors=new int[256];Arrays.fill(colors,0xffffffff);
    for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++){
      int wx=px+x,wz=pz+z,index=WaterPatchMesher.index(x,0,z);
      if(wx==leftX&&wz==leftZ){u[index]=16;f[index]=(byte)a;l[index]=0x00f000b0;}
      if(wx==leftX+(axis==0?1:0)&&wz==leftZ+(axis==1?1:0)){u[index]=16;f[index]=(byte)b;l[index]=0x00f000b0;}
    }
    return new WaterPatchMesher.Snapshot(u,new byte[u.length],l,colors,f);
  }
  private static final class Uploads implements AutoCloseable {
    final Class<?> type=AnimatedWaterRenderer.class,pt=Class.forName(type.getName()+"$Page"),rt=Class.forName(type.getName()+"$Ready");
    final Object renderer;final Constructor<?> pc,ready;final Method upload;long revision;
    final Map<Long,Object> local=new HashMap<>();
    Uploads()throws Exception{
      var c=type.getDeclaredConstructor();c.setAccessible(true);renderer=c.newInstance();pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
      ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    }
    void reset()throws Exception{
      long bytes=0;boolean mirrors=true;
      try{for(Object page:local.values()){byte[] b=(byte[])field(pt,"presentation").get(page);if(b!=null)bytes+=b.length;}}catch(NoSuchFieldException baseline){mirrors=false;}
      if(mirrors&&(bytes>field(type,"residentBytes").getLong(renderer)||bytes>32L*1024*1024))throw new AssertionError("CPU mirror exceeds resident GPU cap");
      type.getMethod("reset").invoke(renderer);
      if(mirrors)for(Object page:local.values())if(field(pt,"presentation").get(page)!=null)throw new AssertionError("CPU mirror leaked across reset");
      local.clear();revision=0;
    }
    Input put(int route,int side,int a,int b,int axis,int origin,int cell,long time,long drawTime)throws Exception{
      int leftX=origin+cell,leftZ=-origin+cell,wx=leftX+(side==1&&axis==0?1:0),wz=leftZ+(side==1&&axis==1?1:0),px=Math.floorDiv(wx,16)*16,pz=Math.floorDiv(wz,16)*16;
      long key=SectionPos.asLong(px>>4,0,pz>>4);Object page=local.get(key);boolean fresh=page==null;
      if(fresh){page=pc.newInstance(key);local.put(key,page);((Map<Long,Object>)field(type,"pages").get(renderer)).put(key,page);}
      var s=snapshot(px,pz,leftX,leftZ,axis,a,b);var mesh=WaterPatchMesher.build(s,s,true);field(type,"clock").setLong(renderer,time);field(pt,"changed").setBoolean(page,false);
      long started=System.nanoTime();
      upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));
      long uploadNanos=System.nanoTime()-started;
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);var vf=field(WaterGpuMesh.class,"vertices");
      if(fresh){((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Corner actual readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,gpu.bytes));started=System.nanoTime();upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));uploadNanos=System.nanoTime()-started;}
      int targetCell=(wx-px)|((wz-pz)<<4),top=-1;
      for(int v=0;v<mesh.count();v+=4){int at=v*14;if((int)mesh.vertices()[at+9]==targetCell&&WaterPatchMesher.face(mesh.vertices()[at+10])==0){top=v;break;}}
      if(top<0)throw new AssertionError("Missing actual top");
      int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;var read=RenderSystem.getDevice().createBuffer(()->"Corner actual upload readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vf.get(gpu)).slice(0,count*36L),read.slice());
      return new Input(route,side,a,b,axis,origin,wx-px,wz-pz,drawTime,time,uploadNanos,top,count,new Matrix4f((Matrix4f)field(pt,"texture").get(page)).m00(drawTime-time),read);
    }
    Input resident(Input old,int a,int b,long drawTime)throws Exception {
      long key=SectionPos.asLong(((int)old.texture.m30())>>4,0,((int)old.texture.m31())>>4);Object page=local.get(key);
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;
      var read=RenderSystem.getDevice().createBuffer(()->"Unrepacked resident neighbor",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);
      RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)field(WaterGpuMesh.class,"vertices").get(gpu)).slice(0,count*36L),read.slice());
      long uploaded=field(pt,"uploaded").getLong(page);
      return new Input(old.route,old.side,a,b,old.axis,old.origin,old.cellX,old.cellZ,drawTime,uploaded,0,old.topVertex,count,new Matrix4f((Matrix4f)field(pt,"texture").get(page)).m00(drawTime-uploaded),read);
    }
    public void close()throws Exception{reset();try{if(!((Map<?,?>)field(type,"cornerDirections").get(renderer)).isEmpty())throw new AssertionError("Corner history leaked across reset");}catch(NoSuchFieldException legacyBaseline){}((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();}
  }
  static void run(Minecraft mc,Path out)throws Exception{
    Files.createDirectories(out);System.out.println("VALIDATION_CLASS_ORIGIN class="+AnimatedWaterRenderer.class.getName()+" location="+AnimatedWaterRenderer.class.getProtectionDomain().getCodeSource().getLocation());
    var provenance=new LinkedHashMap<String,Object>();
    try(var stream=AnimatedWaterRenderer.class.getResourceAsStream("AnimatedWaterRenderer.class")){provenance.put("renderer_class_sha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}
    boolean hasCorners;try{field(AnimatedWaterRenderer.class,"cornerDirections");hasCorners=true;}catch(NoSuchFieldException baseline){hasCorners=false;}
    provenance.put("corner_history_field_present",hasCorners);
    boolean hasMirror;try{field(Class.forName(AnimatedWaterRenderer.class.getName()+"$Page"),"presentation");hasMirror=true;}catch(NoSuchFieldException baseline){hasMirror=false;}
    provenance.put("resident_presentation_mirror_present",hasMirror);
    for(String shader:List.of("animated_water.vsh","animated_water.fsh"))try(var stream=mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("deepprimal_graphics","shaders/core/"+shader)).orElseThrow().open()){
      provenance.put(shader+"_sha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
    }
    Files.writeString(out.resolve("water-corner-runtime-provenance.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(provenance)+"\n");
    var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();var pipeline=(RenderPipeline)field(AnimatedWaterRenderer.class,"PIPELINE").get(null);
    var real=(TextureAtlas)mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);var flow=real.getSprite(Identifier.withDefaultNamespace("block/water_flow"));var still=real.getSprite(Identifier.withDefaultNamespace("block/water_still"));
    var actual=mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getTexture();int width=actual.getWidth(0),height=actual.getHeight(0);
    var atlas=device.createTexture("Corner diagnostic atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,1);var av=device.createTextureView(atlas);
    var mip=device.createTexture("Corner mip atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,3);var mv=device.createTextureView(mip);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(width,height,false)){
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)image.setPixelABGR(x,y,0xffff00ff);
      for(var sprite:List.of(flow,still)){int x0=Math.round(sprite.getU(0)*width),y0=Math.round(sprite.getV(0)*height),w=Math.round((sprite.getU(1)-sprite.getU(0))*width),h=Math.round((sprite.getV(1)-sprite.getV(0))*height);for(int y=0;y<h;y++)for(int x=0;x<w;x++)image.setPixelABGR(x0+x,y0+y,0xff000000|((32+y*160/h)<<8)|(sprite==flow?(64+x*160/w)<<16:64+x*160/w));}
      encoder.writeToTexture(atlas,image);encoder.writeToTexture(mip,image);
    }
    for(int level=1;level<=2;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(width>>level,height>>level,false)){
      for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)image.setPixelABGR(x,y,level==1?0xff00ff00:0xffff0000);
      encoder.writeToTexture(mip,image,level,0,0,0,image.getWidth(),image.getHeight(),0,0);
    }
    var light=device.createTexture("Corner white light",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,16,16,1,1);var lv=device.createTextureView(light);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(16,16,false)){for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xffffffff);encoder.writeToTexture(light,image);}
    var identity=MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());new Matrix4f().get(0,identity);var projection=device.createBuffer(()->"Corner projection",GpuBuffer.USAGE_UNIFORM,identity);MemoryUtil.memFree(identity);
    var inputs=new ArrayList<Input>();var session=new Uploads();int route=0;
    try{
      for(int origin:new int[]{0,29999968})for(int cell:new int[]{3,15})for(int axis=0;axis<2;axis++)for(int a=0;a<17;a++)for(int b=0;b<17;b++){
        session.reset();inputs.add(session.put(route,0,a,b,axis,origin,cell,0,0));inputs.add(session.put(route,1,a,b,axis,origin,cell,0,0));route++;
      }
      // Matched neighbor captures and staggered uploads preserve their shared clock.
      // A neighbor not yet repacked for a NEW request is explicitly outside this gate.
      for(int cell:new int[]{3,15})for(int axis=0;axis<2;axis++)for(int age:new int[]{0,25,50,75,100}){
        session.reset();session.put(-1,0,0,4,axis,0,cell,0,0).read.close();session.put(-1,1,0,4,axis,0,cell,0,0).read.close();
        inputs.add(session.put(route,0,4,8,axis,0,cell,100,100+age));inputs.add(session.put(route,1,4,8,axis,0,cell,100+Math.min(age,25),100+age));route++;
      }
      // New request arrives on only one page: read the resident neighbor without a capture/upload request.
      for(int origin:new int[]{0,29999968})for(int cell:new int[]{3,15})for(int axis=0;axis<2;axis++)for(int age:new int[]{0,25,50,75,100})for(int first=0;first<2;first++) {
        session.reset();var oldLeft=session.put(route,0,0,4,axis,origin,cell,0,0);var oldRight=session.put(route,1,0,4,axis,origin,cell,0,0);
        oldLeft.read.close();oldRight.read.close();
        long started=origin==0?100:65520;
        var changed=session.put(route,first,4,8,axis,origin,cell,started,started+age);
        var resident=session.resident(first==0?oldRight:oldLeft,4,8,started+age);
        if(first==0){inputs.add(changed);inputs.add(resident);}else{inputs.add(resident);inputs.add(changed);}route++;
      }
      // Retire expired real GPU packets, then sample beyond the 16-bit wrap period.
      for(int origin:new int[]{0,29999968})for(int axis=0;axis<2;axis++)for(int first=0;first<2;first++){
        session.reset();var left=session.put(route,0,0,4,axis,origin,15,0,0);var right=session.put(route,1,0,4,axis,origin,15,0,0);left.read.close();right.read.close();
        session.put(route,first,4,8,axis,origin,15,65520,65520).read.close();
        field(session.type,"clock").setLong(session.renderer,65621);
        var retire=session.type.getDeclaredMethod("retirePresentation");retire.setAccessible(true);retire.invoke(session.renderer);
        inputs.add(session.resident(left,4,8,131106));inputs.add(session.resident(right,4,8,131106));route++;
      }
      try {
        var method=session.type.getDeclaredMethod("cornerFlow",long.class,int.class,long.class,int.class);method.setAccessible(true);
        for(int n=0;n<33000;n++)method.invoke(session.renderer,1_000_000L+n,n&15,1_000_000L+n,n&15);
        var history=(Map<?,?>)field(session.type,"cornerDirections").get(session.renderer);
        if(history.size()!=32768||history.containsKey(1_000_000L))throw new AssertionError("Corner history cap/eviction failed");
        System.out.println("WATER_CORNER_HISTORY_CAP_PASS inserted=33000 cap=32768 resetRequired=true");
      } catch(NoSuchMethodException legacyBaseline) {}
    }finally{session.close();}
    final int routes=route;
    RenderSystem.queueFencedTask(()->{
      var pixels=new ArrayList<Pixels>();var uniformPixels=new ArrayList<Pixels>();var mipPixels=new ArrayList<Pixels>();var rawVertices=new java.io.ByteArrayOutputStream();var uploadRows=new ArrayList<Map<String,Object>>();int[] packedCount={0};
      try{
        for(var input:inputs)try(var mapped=device.createCommandEncoder().mapBuffer(input.read,true,false)){
          var b=mapped.data().order(ByteOrder.nativeOrder());byte[] copy=new byte[input.count*36];b.get(0,copy);int offset=rawVertices.size();rawVertices.writeBytes(copy);
          var row=new LinkedHashMap<String,Object>();row.put("route",input.route);row.put("side",input.side);row.put("a",input.a);row.put("b",input.b);row.put("axis",input.axis);row.put("origin",input.origin);row.put("cellX",input.cellX);row.put("cellZ",input.cellZ);row.put("time",input.time);row.put("uploaded",input.uploaded);row.put("pageX",input.texture.m30());row.put("pageZ",input.texture.m31());row.put("uploadNanos",input.uploadNanos);row.put("topVertex",input.topVertex);row.put("count",input.count);row.put("offset",offset);uploadRows.add(row);
          int first=input.topVertex*36;boolean spatial=WaterCornerPacking.spatial(b,first);if(spatial)packedCount[0]++;
          var data=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh mesh;byte[] probeBytes=new byte[144];
          try{
            for(int n=0;n<4;n++){
              boolean high=n==1||n==2;int sourceCorner=input.axis==0?(input.side==0?(high?2:3):(high?1:0)):(input.side==0?(high?2:1):(high?3:0));
              byte[] vertex=new byte[36];b.get(first+sourceCorner*36,vertex);int at=data.position();data.put(vertex);
              float along=high?1:0;float x=input.cellX+(input.axis==0?(input.side==0?1:0):along),z=input.cellZ+(input.axis==0?along:(input.side==0?1:0));
              data.putFloat(at,x).putFloat(at+4,(n>=2?1:-1)).putFloat(at+8,z);data.putShort(at+24,(short)0);
            }
            data.flip();data.get(0,probeBytes);mesh=new WaterGpuMesh(data,4);
          }finally{MemoryUtil.memFree(data);}
          var view=new Matrix4f().zero().m10(1).m32(.25f).m33(1);if(input.axis==0)view.m21(2).m31(-input.cellZ*2-1);else view.m01(2).m31(-input.cellX*2-1);
          var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),input.texture);
          var color=device.createTexture("Corner edge color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,8,32,1,1);var depth=device.createTexture("Corner edge depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,8,32,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
          try(var pass=encoder.createRenderPass(()->"Actual uploaded water corner edge",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",av,sampler);pass.bindTexture("Sampler2",lv,sampler);mesh.draw(pass);}
          var read=device.createBuffer(()->"Corner edge pixel readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,1024);encoder.copyTextureToBuffer(color,read,0,()->{},0);pixels.add(new Pixels(input,0,input.side,8,32,read,color,depth,cv,dv,mesh));
          var mipData=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh mipMesh;
          try{mipData.put(probeBytes).flip();mipMesh=new WaterGpuMesh(mipData,4);}finally{MemoryUtil.memFree(mipData);}
          var mipColor=device.createTexture("Corner mip edge color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,8,8,1,1);var mipDepth=device.createTexture("Corner mip edge depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,8,8,1,1);var mcv=device.createTextureView(mipColor);var mdv=device.createTextureView(mipDepth);
          try(var pass=encoder.createRenderPass(()->"Actual uploaded water mip edge",mcv,OptionalInt.of(0),mdv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",mv,RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true));pass.bindTexture("Sampler2",lv,sampler);mipMesh.draw(pass);}
          var mr=device.createBuffer(()->"Corner mip edge readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,256);encoder.copyTextureToBuffer(mipColor,mr,0,()->{},0);mipPixels.add(new Pixels(input,2,input.side,8,8,mr,mipColor,mipDepth,mcv,mdv,mipMesh));
          if(input.a==input.b && input.side==0 && input.route<2312)for(int profile=0;profile<2;profile++)for(int reference=0;reference<2;reference++) {
            int wholeSize=profile==0?32:8;
            var whole=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh uniform;
            try {
              for(int n=0;n<4;n++) {
                byte[] vertex=new byte[36];b.get(first+n*36,vertex);int at=whole.position();whole.put(vertex);
                if(reference==1) {
                  float x=b.getFloat(first+n*36)-input.cellX,z=b.getFloat(first+n*36+8)-input.cellZ,u=x,v=z;
                  if(input.a<16) {
                    float fx=(float)Math.cos(input.a*Math.PI/8),fz=(float)Math.sin(input.a*Math.PI/8);
                    int wx=(int)input.texture.m30()+input.cellX,wz=(int)input.texture.m31()+input.cellZ;
                    u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f))+WaterPatchMesher.flowUOffset(input.a,wx,wz);
                    v=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f))+WaterPatchMesher.flowVOffset(input.a,wx,wz);
                  }
                  var sprite=input.a<16?flow:still;
                  whole.put(at+15,(byte)255);whole.put(at+33,(byte)WaterCornerPacking.opacityTarget(b,first+n*36));whole.putFloat(at+16,sprite.getU(u));whole.putFloat(at+20,sprite.getV(v));
                  whole.putShort(at+26,(short)WaterCornerPacking.heightOffset(b,first+n*36));whole.putShort(at+28,(short)(WaterCornerPacking.light(b,first+n*36,28)|(input.a<16?1024:0)));whole.putShort(at+30,(short)WaterCornerPacking.light(b,first+n*36,30));
                }
              }
              whole.flip();uniform=new WaterGpuMesh(whole,4);
            }finally{MemoryUtil.memFree(whole);}
            var wholeView=new Matrix4f().zero().m00(2).m21(2).m30(-input.cellX*2-1).m31(-input.cellZ*2-1).m32(.25f).m33(1);
            var wholeTransform=RenderSystem.getDynamicUniforms().writeTransform(wholeView,new Vector4f(1),new Vector3f(),input.texture);
            var wholeColor=device.createTexture("Corner uniform color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,wholeSize,wholeSize,1,1);var wholeDepth=device.createTexture("Corner uniform depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,wholeSize,wholeSize,1,1);var wcv=device.createTextureView(wholeColor);var wdv=device.createTextureView(wholeDepth);
            try(var pass=encoder.createRenderPass(()->"Actual corner uniform-flow fidelity",wcv,OptionalInt.of(0),wdv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",wholeTransform);pass.bindTexture("Sampler0",profile==0?av:mv,profile==0?sampler:RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true));pass.bindTexture("Sampler2",lv,sampler);uniform.draw(pass);}
            var wr=device.createBuffer(()->"Corner uniform pixel readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,wholeSize*wholeSize*4);encoder.copyTextureToBuffer(wholeColor,wr,0,()->{},0);uniformPixels.add(new Pixels(input,profile==0?1:3,reference,wholeSize,wholeSize,wr,wholeColor,wholeDepth,wcv,wdv,uniform));
          }

        }
        pixels.addAll(mipPixels);pixels.addAll(uniformPixels);
        Files.write(out.resolve("water-corner-upload.bin"),rawVertices.toByteArray());Files.writeString(out.resolve("water-corner-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(uploadRows)+"\n");
      }catch(Exception e){throw new RuntimeException(e);}finally{for(var i:inputs)i.read.close();}
      RenderSystem.queueFencedTask(()->{
        try{
          var raw=new java.io.ByteArrayOutputStream();var rows=new ArrayList<Map<String,Object>>();byte[] previous=null;int previousOffset=0,bad=0,coverage=0,max=0,uniformRoutes=0,uniformMipRoutes=0;
          for(var p:pixels)try(var mapped=device.createCommandEncoder().mapBuffer(p.read,true,false)){
            byte[] bytes=new byte[p.width*p.height*4];mapped.data().get(0,bytes);if(device.getBackendName().equals("OpenGL")){byte[] ordered=bytes.clone();for(int y=0;y<p.height;y++)System.arraycopy(bytes,(p.height-1-y)*p.width*4,ordered,y*p.width*4,p.width*4);bytes=ordered;}int pixelOffset=raw.size();raw.writeBytes(bytes);for(int at=3;at<bytes.length;at+=4)if((bytes[at]&255)!=255)coverage++;
            if(p.endpoint==0){previous=bytes;previousOffset=pixelOffset;continue;}if(p.kind==1)uniformRoutes++;if(p.kind==3)uniformMipRoutes++;
            int errors=0,maximum=0;for(int at=0;at<bytes.length;at++){int e=Math.abs((bytes[at]&255)-(previous[at]&255));if(e>1)errors++;maximum=Math.max(maximum,e);}bad+=errors;max=Math.max(max,maximum);
            var row=new LinkedHashMap<String,Object>();row.put("route",p.in.route);row.put("kind",p.kind);row.put("a",p.in.a);row.put("b",p.in.b);row.put("axis",p.in.axis);row.put("origin",p.in.origin);row.put("cellX",p.in.cellX);row.put("cellZ",p.in.cellZ);row.put("time",p.in.time);row.put("width",p.width);row.put("height",p.height);row.put("leftOffset",previousOffset);row.put("rightOffset",pixelOffset);row.put("maxChannelError",maximum);row.put("channelsBeyondTolerance",errors);rows.add(row);
            if((p.kind==0&&(p.in.route==4||p.in.route==68||p.in.route==routes-3))||(p.kind==1&&p.in.a==4&&p.in.origin==0&&p.in.cellX==3&&p.in.axis==0))for(int side=0;side<2;side++)try(var image=new com.mojang.blaze3d.platform.NativeImage(p.width,p.height,false)){var b=ByteBuffer.wrap(side==0?previous:bytes).order(ByteOrder.nativeOrder());for(int y=0;y<p.height;y++)for(int x=0;x<p.width;x++)image.setPixelABGR(x,y,b.getInt((y*p.width+x)*4));image.writeToFile(out.resolve("corner-"+p.kind+"-"+p.in.route+"-"+side+".png"));}
          }
          Files.write(out.resolve("water-corner-pixels.bin"),raw.toByteArray());Files.writeString(out.resolve("water-corner-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
          boolean pass=bad==0&&coverage==0&&packedCount[0]==inputs.size();var result=new LinkedHashMap<String,Object>();result.put("pass",pass);result.put("routes",routes);result.put("uniform_routes",uniformRoutes);result.put("uniform_mip_routes",uniformMipRoutes);result.put("mip_edge_routes",routes);result.put("comparisons",rows.size());result.put("draws",pixels.size());result.put("packed_actual_tops",packedCount[0]);result.put("channels_beyond_tolerance",bad);result.put("max_channel_error",max);result.put("coverage_bad_pixels",coverage);result.put("tolerance",1);result.put("readback_row_zero","upper physical Y");result.put("opengl_rows_flipped",device.getBackendName().equals("OpenGL"));result.put("raw_upload_bytes",rawVertices.size());result.put("pixel_readback_bytes",raw.size());result.put("scope","actual uploaded corner payload reprojected at equal shared-edge positions; settled section halos and matched temporal requests; staggered new-request resident-neighbor publication included; no full scene/stream/cap-churn acceptance");Files.writeString(out.resolve("water-corner-result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
          System.out.println("WATER_CORNER_FIELD_RESULT pass="+pass+" routes="+routes+" channelsBeyondTolerance="+bad+" maxChannelError="+max+" packedTops="+packedCount[0]);if(pass)System.out.println("WATER_CORNER_FIELD_NATIVE_PASS actualUploads=true sectionHaloEdges=true matchedTemporal=true vertexStride=36");
        }catch(Exception e){throw new RuntimeException(e);}finally{for(var p:pixels){p.read.close();p.cv.close();p.dv.close();p.color.close();p.depth.close();p.mesh.close();}projection.close();av.close();mv.close();lv.close();atlas.close();mip.close();light.close();}
      });
    });
  }
  private WaterCornerFieldChecks(){}
}
