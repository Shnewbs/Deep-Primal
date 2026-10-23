from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';V=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'
s=(V/'WaterSideUvChecks.java').read_text().replace('WaterSideUvChecks','WaterDirectionBlendChecks')
a=s.index('  private static WaterGpuMesh quad(');b=s.index('  static void run(',a)
s=s[:a]+'''  private static WaterGpuMesh quad(int direction,int age,boolean active) {
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
'''+s[b:]
s=s.replace('var pixels=new byte[12][]','var pixels=new byte[7][]').replace('for(int i=0;i<12;i++)','for(int i=0;i<7;i++)')
a=s.index('      int which=i;');b=s.index('      var color=',a)
s=s[:a]+'''      int which=i;var mesh=quad(i==0?0:4,i<2?0:(i-2)*25,i>=2);meshes.add(mesh);
      var view=new Matrix4f().zero().m00(2).m21(2).m30(-1).m31(-1).m32(.25f).m33(1);
      var transform=RenderSystem.getDynamicUniforms().writeTransform(view,new Vector4f(1),new Vector3f(),new Matrix4f().m00(0).m10(1).m11(.5f).m20(0).m21(0).m01(.125f).m02(.5f).m03(.25f).m12(.5f).m30(0).m31(0));
'''+s[b:]
s=s.replace('16,64,1,1','64,64,1,1').replace('NativeImage(16,64,false)','NativeImage(64,64,false)')
s=s.replace('for(int y=0;y<64;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xff000000|((y*4)<<16)|(x*16));encoder.writeToTexture(atlas,image);','for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelABGR(x,y,x>=8&&x<40&&y>=16&&y<48 ? 0xff000000|(((y-16)*8)<<16)|((x-8)*8) : 0xffff00ff);encoder.writeToTexture(atlas,image);')
s=s.replace('side-uv-','direction-blend-').replace('complete.incrementAndGet()==12','complete.incrementAndGet()==7')
a=s.index('              int differing=');b=s.index('            }catch(Exception e)',a)
s=s[:a]+'''              int bad=0,max=0;var rows=new ArrayList<Map<String,Object>>();
              for(int sample=0;sample<5;sample++) {
                int violations=0,maximum=0;float t=sample*.25f;var candidate=pixels[sample+2];
                for(int at=0;at<4096;at++) {
                  int expected=Math.round((pixels[0][at]&255)*(1-t)+(pixels[1][at]&255)*t),error=Math.abs((candidate[at]&255)-expected);
                  maximum=Math.max(maximum,error);if(error>1)violations++;
                }
                rows.add(Map.of("millis",sample*25,"maxChannelError",maximum,"channelsBeyondTolerance",violations));bad+=violations;max=Math.max(max,maximum);
              }
              String result="WATER_DIRECTION_BLEND_PIXEL_RESULT pass="+(bad==0)+" samples=5 renders=7 channelsBeyondTolerance="+bad+" maxChannelError="+max+" tolerance=1 scope=controlledProductionShaderNotFullStreamAcceptance\\n";
              Files.writeString(out.resolve("water-direction-blend-pixels.txt"),result);Files.writeString(out.resolve("water-direction-blend-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\\n");System.out.print(result);
              if(bad==0){String pass="WATER_DIRECTION_BLEND_NATIVE_PASS samples=5 renders=7 oldAndNewEndpoints=true intermediateRGBA=true tolerance=1 isolatedGpuReleased=true scope=controlledProductionShaderNotFullStreamAcceptance\\n";Files.writeString(out.resolve("water-direction-blend-pass.txt"),pass);System.out.print(pass);}
'''+s[b:]
(V/'WaterDirectionBlendChecks.java').write_text(s)
p=V/'ClientCapture.java';s=p.read_text().replace('      if (action.equals("water-flow-phase-check")) {','      if (action.equals("water-direction-blend-check")) {\n        WaterDirectionBlendChecks.run(mc, dir);\n        return;\n      }\n      if (action.equals("water-flow-phase-check")) {');p.write_text(s)
p=R/'run_vulkan.py';s=p.read_text().replace("elif mode.startswith('phase-baseline'):","elif mode.startswith('blend-baseline'):\n actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('water-direction-blend-check','WATER_DIRECTION_BLEND_PIXEL_RESULT',0),('stop',None,0)]\nelif mode.startswith('phase-baseline'):")
s=s.replace("('water-flow-phase-check','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS',0)","('water-direction-blend-check','WATER_DIRECTION_BLEND_NATIVE_PASS',0),('water-flow-phase-check','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS',0)");p.write_text(s)
print('direction blend pixel fixture added, production unchanged')
