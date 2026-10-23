from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';V=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'
s=(V/'WaterSideUvUploadChecks.java').read_text().replace('WaterSideUvUploadChecks','WaterDirectionUploadChecks')
s=s.replace('private record Sample(int direction,long time,long uploaded,boolean stale,int count,Matrix4f texture,GpuBuffer read)', 'private record Sample(int direction,int source,int age,boolean active,long time,int count,GpuBuffer read)')
a=s.index('      for(int direction:new int[]{16,0})');b=s.index('      RenderSystem.queueFencedTask',a)
s=s[:a]+'''      long session=field(type,"session").getLong(renderer);var page=pc.newInstance(0L);
      @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(0L,page);
      var full=snapshot(16,0);field(type,"clock").setLong(renderer,0);field(pt,"changed").setBoolean(page,false);upload.invoke(renderer,ready.newInstance(session,1L,page,full,WaterPatchMesher.build(full,full,true)));
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);long capacity=gpu.bytes;var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Direction readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,capacity));
      int[] requested={0,4,4,8,8,8,0},targets={0,4,4,4,8,8,8},sources={0,0,0,0,4,4,4},ages={0,0,25,50,0,25,50};long[] times={0,100,125,150,200,225,250};
      for(int n=0;n<times.length;n++) {
        var next=snapshot(16,requested[n]);field(type,"clock").setLong(renderer,times[n]);field(pt,"changed").setBoolean(page,false);
        upload.invoke(renderer,ready.newInstance(session,n==6?0L:n+2L,page,next,WaterPatchMesher.build(next,next,true)));
        gpu=(WaterGpuMesh)field(pt,"gpu").get(page);int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;if(gpu.bytes!=capacity||count!=20)throw new AssertionError("Direction blend changed allocation or coverage");
        var read=RenderSystem.getDevice().createBuffer(()->"Direction vertex readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);
        RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vf.get(gpu)).slice(0,count*36L),read.slice());
        samples.add(new Sample(targets[n],sources[n],ages[n],n!=0,times[n],count,read));
      }
      var flowMethod=type.getDeclaredMethod("flow",long.class,int.class,long.class);flowMethod.setAccessible(true);
      for(int n=0;n<33000;n++)flowMethod.invoke(renderer,1_000_000L+n,n&15,1000L+n);
      var history=(Map<?,?>)field(type,"flowDirections").get(renderer);if(history.size()!=32768||history.containsKey(1_000_000L))throw new AssertionError("Direction history is unbounded or not FIFO-LRU evicted");
''' +s[b:]
a=s.index('        var rows=new ArrayList');b=s.index('      });queued=true;',a)
s=s[:a]+'''        var rows=new ArrayList<Map<String,Object>>();var raw=new java.io.ByteArrayOutputStream();int vertices=0;
        try {
          for(var sample:samples)try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(sample.read,true,false)) {
            var b=mapped.data().order(ByteOrder.nativeOrder());
            for(int v=0;v<sample.count;v++) {
              int at=v*36,light=b.getShort(at+28)&65535,sky=b.getShort(at+30)&65535;
              if((light&255)!=176||(sky&255)!=240)throw new AssertionError("Direction metadata corrupted light");
              boolean active=(sky&32768)!=0;
              if(active!=(v<4&&sample.active))throw new AssertionError("Direction blend activation differs");
              if(v<4) {
                if(active&&((light>>>11)!=sample.source||((sky>>>8)&127)!=sample.age))throw new AssertionError("Source direction/age reset or stale target admitted");
                float x=b.getFloat(at)-3,z=b.getFloat(at+8)-3;double angle=sample.direction*Math.PI/8;
                float fx=(float)Math.cos(angle),fz=(float)Math.sin(angle);
                float u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f))+WaterPatchMesher.flowUOffset(sample.direction,3,3);
                float w=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f))+WaterPatchMesher.flowVOffset(sample.direction,3,3);
                var atlas=(net.minecraft.client.renderer.texture.TextureAtlas)mc.getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
                var sprite=atlas.getSprite(net.minecraft.resources.Identifier.withDefaultNamespace("block/water_flow"));
                if(Math.abs(b.getFloat(at+16)-sprite.getU(u))>1e-6||Math.abs(b.getFloat(at+20)-sprite.getV(w))>1e-6)throw new AssertionError("Pending or stale request changed admitted UV target");
              }
              byte[] bytes=new byte[36];b.duplicate().position(at).get(bytes);raw.writeBytes(bytes);vertices++;
            }
            rows.add(Map.of("time",sample.time,"source",sample.source,"target",sample.direction,"age",sample.age,"active",sample.active,"vertices",sample.count));
          }
          Files.write(out.resolve("water-direction-upload.bin"),raw.toByteArray());Files.writeString(out.resolve("water-direction-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\\n");
          String record="WATER_DIRECTION_UPLOAD_NATIVE_PASS samples=7 readbackBytes=5040 vertices="+vertices+" repeatedUpload=true latestQueued=true staleRevision=true lightPreserved=true historyCap=32768 historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploads\\n";
          Files.writeString(out.resolve("water-direction-upload-pass.txt"),record);System.out.print(record);
        }catch(Exception e){throw new RuntimeException(e);}finally {for(var sample:samples)sample.read.close();}
''' +s[b:]
# Existing finally resets and shuts down the isolated worker before fenced callback.
s=s.replace('type.getMethod("reset").invoke(renderer);((ExecutorService)', 'type.getMethod("reset").invoke(renderer);if(!((Map<?,?>)field(type,"flowDirections").get(renderer)).isEmpty())throw new AssertionError("History survived reset");((ExecutorService)')
(V/'WaterDirectionUploadChecks.java').write_text(s)
p=V/'ClientCapture.java';s=p.read_text().replace('      if (action.equals("water-direction-blend-check")) {','      if (action.equals("water-direction-upload-check")) {\n        WaterDirectionUploadChecks.run(mc, dir);\n        return;\n      }\n      if (action.equals("water-direction-blend-check")) {');p.write_text(s)
p=R/'run_vulkan.py';s=p.read_text().replace("('water-direction-blend-check','WATER_DIRECTION_BLEND_NATIVE_PASS',0)","('water-direction-blend-check','WATER_DIRECTION_BLEND_NATIVE_PASS',0),('water-direction-upload-check','WATER_DIRECTION_UPLOAD_NATIVE_PASS',0)");p.write_text(s)
print('production-upload, queued/stale and bounded history checks added')
