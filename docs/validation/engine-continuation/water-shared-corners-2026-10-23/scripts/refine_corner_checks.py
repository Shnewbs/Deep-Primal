from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';D=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'
p=D/'WaterCornerFieldChecks.java';s=p.read_text();(R/'first-WaterCornerFieldChecks.java').write_text(s)
s=s.replace('if(!((Map<?,?>)field(type,"cornerDirections").get(renderer)).isEmpty())throw new AssertionError("Corner history leaked across reset");','try{if(!((Map<?,?>)field(type,"cornerDirections").get(renderer)).isEmpty())throw new AssertionError("Corner history leaked across reset");}catch(NoSuchFieldException legacyBaseline){}')
s=s.replace('inputs.add(session.put(route,1,4,8,axis,0,cell,100,100+age))','inputs.add(session.put(route,1,4,8,axis,0,cell,100+Math.min(age,25),100+age))')
s=s.replace('''    }finally{session.close();}''','''      try {
        var method=session.type.getDeclaredMethod("cornerFlow",long.class,int.class,long.class,int.class);method.setAccessible(true);
        for(int n=0;n<33000;n++)method.invoke(session.renderer,1_000_000L+n,n&15,1_000_000L+n,n&15);
        var history=(Map<?,?>)field(session.type,"cornerDirections").get(session.renderer);
        if(history.size()!=32768||history.containsKey(1_000_000L))throw new AssertionError("Corner history cap/eviction failed");
        System.out.println("WATER_CORNER_HISTORY_CAP_PASS inserted=33000 cap=32768 resetRequired=true");
      } catch(NoSuchMethodException legacyBaseline) {}
    }finally{session.close();}''')
s=s.replace('var rawVertices=new java.io.ByteArrayOutputStream();int[] packedCount={0};','var rawVertices=new java.io.ByteArrayOutputStream();var uploadRows=new ArrayList<Map<String,Object>>();int[] packedCount={0};')
s=s.replace('b.get(0,copy);rawVertices.writeBytes(copy);','b.get(0,copy);int offset=rawVertices.size();rawVertices.writeBytes(copy);\n          var row=new LinkedHashMap<String,Object>();row.put("route",input.route);row.put("side",input.side);row.put("a",input.a);row.put("b",input.b);row.put("axis",input.axis);row.put("origin",input.origin);row.put("cellX",input.cellX);row.put("cellZ",input.cellZ);row.put("time",input.time);row.put("uploaded",input.uploaded);row.put("pageX",input.texture.m30());row.put("pageZ",input.texture.m31());row.put("topVertex",input.topVertex);row.put("count",input.count);row.put("offset",offset);uploadRows.add(row);')
s=s.replace('Files.write(out.resolve("water-corner-upload.bin"),rawVertices.toByteArray());','Files.write(out.resolve("water-corner-upload.bin"),rawVertices.toByteArray());Files.writeString(out.resolve("water-corner-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(uploadRows)+"\\n");')
p.write_text(s)
# Avoid recomputing legacy top UVs that are now replaced by the corner codec.
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();(R/'first-candidate-AnimatedWaterRenderer.java').write_text(s)
s=s.replace('        float phaseU = 0, phaseV = 0;\n','')
start=s.index('            if (face == 0 && shownDirection < 16) {');end=s.index('            if (face == 0) {',start);s=s[:start]+s[end:]
start=s.index('          boolean flowingFace = shownDirection < 16;');end=s.index('          data.putShort((short) Math.round',start)
s=s[:start]+'''          boolean flowingFace = shownDirection < 16;
          boolean side = face != 0;
          if (face == 0) {
            data.putFloat(WaterFlowField.targets(cornerTargets, (cornerAges >>> 24) & 255))
                .putFloat(WaterFlowField.sources(cornerSources, (cornerAges >>> 24) & 255));
          } else {
            var sprite = flowingFace ? flowing : still;
            float scale = flowingFace ? .5f : 1;
            float localU = values[i + 5] * (WaterPatchMesher.direction(values[i + 10]) < 16 ? 2 : 1);
            data.putFloat(sprite.getU(localU * scale)).putFloat(sprite.getV((1 - target) * scale));
          }
'''+s[end:]
s=s.replace('int presentation = side ? 256 | (flowingFace ? 512 : 0) : flowTop ? 1024 : 0;','int presentation = side ? 256 | (flowingFace ? 512 : 0) : 0;');p.write_text(s)
# Keep the actual client FLOW capture test, validating canonical corner metadata now.
p=D/'WaterFlowTextureChecks.java';s=p.read_text();(R/'before-WaterFlowTextureChecks.java').write_text(s)
s=s.replace('private record Sample(int direction,int count,float[] cpu,GpuBuffer read) {}','private record Sample(int direction,int count,float[] cpu,WaterPatchMesher.Snapshot snapshot,GpuBuffer read) {}')
s=s.replace('count,mesh.vertices(),read));','count,mesh.vertices(),s,read));')
start=s.index('                float u=s.cpu[i]-cx');end=s.index('                byte[] rawVertex=',start)
s=s[:start]+'''                if(!WaterCornerPacking.spatial(data,v*36))throw new AssertionError("Actual FLOW top missing corner payload");
                int[] targets=WaterCornerPacking.ids(data,v*36,true);
                for(int n=0;n<4;n++) {
                  // Independent canonical mean of the four captured wet cells.
                  double vx=0,vz=0;int x=cx+(n&1),z=cz+(n>>1);
                  for(int dz=-1;dz<=0;dz++)for(int dx=-1;dx<=0;dx++)if(s.snapshot.water(x+dx,cy,z+dz)>0){int d=s.snapshot.direction(x+dx,cy,z+dz);if(d<16){vx+=(float)Math.cos(d*Math.PI/8);vz+=(float)Math.sin(d*Math.PI/8);}}
                  int expected=vx*vx+vz*vz<1e-12?16:((int)Math.round(Math.atan2(vz,vx)*8/Math.PI)&15);
                  if(targets[n]!=expected)throw new AssertionError("Actual captured FLOW corner direction differs");
                }
'''+s[end:]
start=s.index('                rows.add(Map.of("direction"');end=s.index('\n                matched++;',start)
s=s[:start]+'''                rows.add(Map.of("direction",s.direction,"cornerTargets",targets,"packedU",data.getFloat(v*36+16),"packedV",data.getFloat(v*36+20),"x",data.getFloat(v*36),"y",data.getFloat(v*36+4),"z",data.getFloat(v*36+8)));'''+s[end:];p.write_text(s)
# First baseline only exercises its actual legacy upload reproduction, then stops normally.
p=R/'run_vulkan.py';s=p.read_text();needle="elif mode.startswith('corners'):";s=s.replace(needle,"elif mode=='corners-baseline':\n actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('water-corner-field-check','WATER_CORNER_FIELD_RESULT',0),('vulkan-frame-completion-check','VULKAN_FRAME_COMPLETION_NATIVE_PASS',1),('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',1),('stop',None,0)]\n"+needle);s=s.replace("('water-gpu-reuse-check','WATER_GPU_REUSE_NATIVE_PASS',0),('vulkan-frame-completion-check'","('water-gpu-reuse-check','WATER_GPU_REUSE_NATIVE_PASS',0),('water-flow-texture-check','WATER_FLOW_TEXTURE_NATIVE_PASS',0),('vulkan-frame-completion-check'")
p.write_text(s)
print('refined source and actual-upload/cap/staggered-time checks')
