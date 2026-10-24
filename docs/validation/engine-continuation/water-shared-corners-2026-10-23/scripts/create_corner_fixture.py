from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';D=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'
header=(D/'WaterMaterialTransitionChecks.java').read_text().split('/** Actual production uploads')[0]
body=r'''
/** Actual-upload edge limits. Geometry is reprojected only for an identical-point comparison;
 * this is not a claim of full scene/stream/FPS or partial neighbor-upload atomicity. */
final class WaterCornerFieldChecks {
  private static Field field(Class<?> c,String n)throws Exception{var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Input(int route,int side,int a,int b,int axis,int origin,int cellX,int cellZ,long time,long uploaded,int topVertex,int count,Matrix4f texture,GpuBuffer read){}
  private record Pixels(Input in,GpuBuffer read,GpuTexture color,GpuTexture depth,GpuTextureView cv,GpuTextureView dv,WaterGpuMesh mesh){}
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
    void reset()throws Exception{type.getMethod("reset").invoke(renderer);local.clear();revision=0;}
    Input put(int route,int side,int a,int b,int axis,int origin,int cell,long time,long drawTime)throws Exception{
      int leftX=origin+cell,leftZ=-origin+cell,wx=leftX+(side==1&&axis==0?1:0),wz=leftZ+(side==1&&axis==1?1:0),px=Math.floorDiv(wx,16)*16,pz=Math.floorDiv(wz,16)*16;
      long key=SectionPos.asLong(px>>4,0,pz>>4);Object page=local.get(key);boolean fresh=page==null;
      if(fresh){page=pc.newInstance(key);local.put(key,page);((Map<Long,Object>)field(type,"pages").get(renderer)).put(key,page);}
      var s=snapshot(px,pz,leftX,leftZ,axis,a,b);var mesh=WaterPatchMesher.build(s,s,true);field(type,"clock").setLong(renderer,time);field(pt,"changed").setBoolean(page,false);
      upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);var vf=field(WaterGpuMesh.class,"vertices");
      if(fresh){((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Corner actual readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,gpu.bytes));upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));}
      int targetCell=(wx-px)|((wz-pz)<<4),top=-1;
      for(int v=0;v<mesh.count();v+=4){int at=v*14;if((int)mesh.vertices()[at+9]==targetCell&&WaterPatchMesher.face(mesh.vertices()[at+10])==0){top=v;break;}}
      if(top<0)throw new AssertionError("Missing actual top");
      int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;var read=RenderSystem.getDevice().createBuffer(()->"Corner actual upload readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vf.get(gpu)).slice(0,count*36L),read.slice());
      return new Input(route,side,a,b,axis,origin,wx-px,wz-pz,drawTime,time,top,count,new Matrix4f((Matrix4f)field(pt,"texture").get(page)).m00(drawTime-time),read);
    }
    public void close()throws Exception{reset();if(!((Map<?,?>)field(type,"cornerDirections").get(renderer)).isEmpty())throw new AssertionError("Corner history leaked across reset");((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();}
  }
  static void run(Minecraft mc,Path out)throws Exception{
    Files.createDirectories(out);System.out.println("VALIDATION_CLASS_ORIGIN class="+AnimatedWaterRenderer.class.getName()+" location="+AnimatedWaterRenderer.class.getProtectionDomain().getCodeSource().getLocation());
    var device=RenderSystem.getDevice();var encoder=device.createCommandEncoder();var pipeline=(RenderPipeline)field(AnimatedWaterRenderer.class,"PIPELINE").get(null);
    var real=(TextureAtlas)mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);var flow=real.getSprite(Identifier.withDefaultNamespace("block/water_flow"));var still=real.getSprite(Identifier.withDefaultNamespace("block/water_still"));
    var actual=mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getTexture();int width=actual.getWidth(0),height=actual.getHeight(0);
    var atlas=device.createTexture("Corner diagnostic atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,1);var av=device.createTextureView(atlas);
    try(var image=new com.mojang.blaze3d.platform.NativeImage(width,height,false)){
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)image.setPixelABGR(x,y,0xffff00ff);
      for(var sprite:List.of(flow,still)){int x0=Math.round(sprite.getU(0)*width),y0=Math.round(sprite.getV(0)*height),w=Math.round((sprite.getU(1)-sprite.getU(0))*width),h=Math.round((sprite.getV(1)-sprite.getV(0))*height);for(int y=0;y<h;y++)for(int x=0;x<w;x++)image.setPixelABGR(x0+x,y0+y,0xff000000|((32+y*160/h)<<8)|(sprite==flow?(64+x*160/w)<<16:64+x*160/w));}
      encoder.writeToTexture(atlas,image);
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
        inputs.add(session.put(route,0,4,8,axis,0,cell,100,100+age));inputs.add(session.put(route,1,4,8,axis,0,cell,100,100+age));route++;
      }
    }finally{session.close();}
    final int routes=route;
    RenderSystem.queueFencedTask(()->{
      var pixels=new ArrayList<Pixels>();var rawVertices=new java.io.ByteArrayOutputStream();int[] packedCount={0};
      try{
        for(var input:inputs)try(var mapped=device.createCommandEncoder().mapBuffer(input.read,true,false)){
          var b=mapped.data().order(ByteOrder.nativeOrder());byte[] copy=new byte[input.count*36];b.get(0,copy);rawVertices.writeBytes(copy);
          int first=input.topVertex*36;boolean spatial=WaterCornerPacking.spatial(b,first);if(spatial)packedCount[0]++;
          var data=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh mesh;
          try{
            for(int n=0;n<4;n++){
              boolean high=n==1||n==2;int sourceCorner=input.axis==0?(input.side==0?(high?2:3):(high?1:0)):(input.side==0?(high?2:1):(high?3:0));
              byte[] vertex=new byte[36];b.get(first+sourceCorner*36,vertex);int at=data.position();data.put(vertex);
              float along=high?1:0;float x=input.cellX+(input.axis==0?(input.side==0?1:0):along),z=input.cellZ+(input.axis==0?along:(input.side==0?1:0));
              data.putFloat(at,x).putFloat(at+4,(n>=2?1:-1)).putFloat(at+8,z);data.putShort(at+24,(short)0);
            }
            data.flip();mesh=new WaterGpuMesh(data,4);
          }finally{MemoryUtil.memFree(data);}
          var view=new Matrix4f().zero().m10(1).m32(.25f).m33(1);if(input.axis==0)view.m21(2).m31(-input.cellZ*2-1);else view.m01(2).m31(-input.cellX*2-1);
          var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),input.texture);
          var color=device.createTexture("Corner edge color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,8,32,1,1);var depth=device.createTexture("Corner edge depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,8,32,1,1);var cv=device.createTextureView(color);var dv=device.createTextureView(depth);var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
          try(var pass=encoder.createRenderPass(()->"Actual uploaded water corner edge",cv,OptionalInt.of(0),dv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",av,sampler);pass.bindTexture("Sampler2",lv,sampler);mesh.draw(pass);}
          var read=device.createBuffer(()->"Corner edge pixel readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,1024);encoder.copyTextureToBuffer(color,read,0,()->{},0);pixels.add(new Pixels(input,read,color,depth,cv,dv,mesh));
        }
        Files.write(out.resolve("water-corner-upload.bin"),rawVertices.toByteArray());
      }catch(Exception e){throw new RuntimeException(e);}finally{for(var i:inputs)i.read.close();}
      RenderSystem.queueFencedTask(()->{
        try{
          var raw=new java.io.ByteArrayOutputStream();var rows=new ArrayList<Map<String,Object>>();byte[] previous=null;int bad=0,coverage=0,max=0;
          for(var p:pixels)try(var mapped=device.createCommandEncoder().mapBuffer(p.read,true,false)){
            byte[] bytes=new byte[1024];mapped.data().get(0,bytes);raw.writeBytes(bytes);for(int at=3;at<bytes.length;at+=4)if((bytes[at]&255)!=255)coverage++;
            if(p.in.side==0){previous=bytes;continue;}
            int errors=0,maximum=0;for(int at=0;at<bytes.length;at++){int e=Math.abs((bytes[at]&255)-(previous[at]&255));if(e>1)errors++;maximum=Math.max(maximum,e);}bad+=errors;max=Math.max(max,maximum);
            rows.add(Map.of("route",p.in.route,"a",p.in.a,"b",p.in.b,"axis",p.in.axis,"origin",p.in.origin,"cell",p.in.cellX,"time",p.in.time,"maxChannelError",maximum,"channelsBeyondTolerance",errors));
            if(p.in.route==4||p.in.route==68||p.in.route==routes-3)for(int side=0;side<2;side++)try(var image=new com.mojang.blaze3d.platform.NativeImage(8,32,false)){var b=ByteBuffer.wrap(side==0?previous:bytes).order(ByteOrder.nativeOrder());for(int y=0;y<32;y++)for(int x=0;x<8;x++)image.setPixelABGR(x,y,b.getInt((y*8+x)*4));image.writeToFile(out.resolve("corner-"+p.in.route+"-"+side+".png"));}
          }
          Files.write(out.resolve("water-corner-pixels.bin"),raw.toByteArray());Files.writeString(out.resolve("water-corner-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
          boolean pass=bad==0&&coverage==0&&packedCount[0]==inputs.size();var result=new LinkedHashMap<String,Object>();result.put("pass",pass);result.put("routes",routes);result.put("draws",pixels.size());result.put("packed_actual_tops",packedCount[0]);result.put("channels_beyond_tolerance",bad);result.put("max_channel_error",max);result.put("coverage_bad_pixels",coverage);result.put("tolerance",1);result.put("raw_upload_bytes",rawVertices.size());result.put("pixel_readback_bytes",raw.size());result.put("scope","actual uploaded corner payload reprojected at equal shared-edge positions; settled section halos and matched temporal requests; no full scene/stream or partial neighbor-request atomicity");Files.writeString(out.resolve("water-corner-result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
          System.out.println("WATER_CORNER_FIELD_RESULT pass="+pass+" routes="+routes+" channelsBeyondTolerance="+bad+" maxChannelError="+max+" packedTops="+packedCount[0]);if(pass)System.out.println("WATER_CORNER_FIELD_NATIVE_PASS actualUploads=true sectionHaloEdges=true matchedTemporal=true vertexStride=36");
        }catch(Exception e){throw new RuntimeException(e);}finally{for(var p:pixels){p.read.close();p.cv.close();p.dv.close();p.color.close();p.depth.close();p.mesh.close();}projection.close();av.close();lv.close();atlas.close();light.close();}
      });
    });
  }
  private WaterCornerFieldChecks(){}
}
'''
(D/'WaterCornerFieldChecks.java').write_text(header+body)
p=D/'ClientCapture.java';s=p.read_text().replace('if (action.equals("water-spatial-seam-check"))','if (action.equals("water-corner-field-check")) { WaterCornerFieldChecks.run(mc, dir); return; }\n      if (action.equals("water-spatial-seam-check"))');p.write_text(s)
p=R/'run_vulkan.py';s=p.read_text();needle="elif mode.startswith('spatial'):";s=s.replace(needle,"elif mode.startswith('corners'):\n actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('water-corner-field-check','WATER_CORNER_FIELD_RESULT',0),('water-material-check','WATER_MATERIAL_RESULT',0),('water-side-material-check','WATER_SIDE_MATERIAL_RESULT',0),('water-direction-upload-check','WATER_DIRECTION_UPLOAD_NATIVE_PASS',0),('water-side-uv-upload-check','WATER_SIDE_UV_UPLOAD_NATIVE_PASS',0),('water-partial-fade-check','WATER_PARTIAL_FADE_NATIVE_PASS',0),('water-gpu-reuse-check','WATER_GPU_REUSE_NATIVE_PASS',0),('vulkan-frame-completion-check','VULKAN_FRAME_COMPLETION_NATIVE_PASS',1),('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',1),('stop',None,0)]\n"+needle);p.write_text(s)
print('created actual-upload shared-edge GPU fixture')
