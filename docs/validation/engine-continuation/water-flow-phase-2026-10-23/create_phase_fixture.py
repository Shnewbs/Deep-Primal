from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';V=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'
s=(V/'WaterSideUvChecks.java').read_text().replace('WaterSideUvChecks','WaterFlowPhaseChecks')
a=s.index('  private static WaterGpuMesh quad(');b=s.index('  static void run(',a)
s=s[:a]+'''  private static final int[] DIRECTIONS={0,2,4,6,10,14};
  private static final int[] ORIGINS={15,-1,-16,29999998,-29999999,31};
  private static float phase(boolean u,int direction,int x,int z,boolean reference)throws Exception {
    if(reference) {
      double fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
      double v=.5*(u?fz*x-fx*z:fx*x+fz*z);return (float)(v-Math.floor(v));
    }
    try{return (float)com.shnewbs.deepprimal.core.WaterPatchMesher.class.getMethod(u?"flowUOffset":"flowVOffset",int.class,int.class,int.class).invoke(null,direction,x,z);}
    catch(NoSuchMethodException baseline){return 0;}
  }
  private static WaterGpuMesh quad(int direction,int origin,boolean reference)throws Exception {
    int count=reference?4:8;var b=MemoryUtil.memCalloc(count*36).order(ByteOrder.nativeOrder());
    float fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
    try {
      for(int cell=0;cell<(reference?1:2);cell++)for(int corner=0;corner<4;corner++) {
        float x=cell+((corner==2||corner==3)?(reference?2:1):0),z=(corner==1||corner==2)?1:0;
        float localX=reference?x:x-cell;
        float u=.5f+.5f*(fz*(localX-.5f)-fx*(z-.5f))+phase(true,direction,origin+(reference?0:cell),-17,reference);
        float v=.5f+.5f*(fx*(localX-.5f)+fz*(z-.5f))+phase(false,direction,origin+(reference?0:cell),-17,reference);
        b.putFloat(x-1).putFloat(z*2-1).putFloat(.25f);b.putInt(0xffffffff);
        b.putFloat(.125f+u*.5f).putFloat(.25f+v*.5f);b.putShort((short)0).putShort((short)0);b.putShort((short)1024).putShort((short)0);
        b.put((byte)127).put((byte)127).put((byte)0).put((byte)0);
      }
      b.flip();return new WaterGpuMesh(b,count);
    }finally {MemoryUtil.memFree(b);}
  }
''' +s[b:]
s=s.replace('16,64,1,1','64,64,1,1').replace('NativeImage(16,64,false)','NativeImage(64,64,false)')
s=s.replace('for(int y=0;y<64;y++)for(int x=0;x<16;x++)image.setPixelABGR(x,y,0xff000000|((y*4)<<16)|(x*16));encoder.writeToTexture(atlas,image);','for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelABGR(x,y,x>=8&&x<40&&y>=16&&y<48 ? 0xff000000|(((y-16)*8)<<16)|((x-8)*8) : 0xffff00ff);encoder.writeToTexture(atlas,image);')
a=s.index('      int which=i;');b=s.index('      var transform=',a)
s=s[:a]+'''      int which=i;int pair=i/2;boolean reference=(i&1)==0;
      var mesh=quad(DIRECTIONS[pair],ORIGINS[pair],reference);meshes.add(mesh);
'''+s[b:]
s=s.replace('.m20(0).m21(0)', '.m20(0).m21(0).m01(.125f).m02(.5f).m03(.25f).m12(.5f)')
s=s.replace('side-uv-', 'flow-phase-').replace('WATER_SIDE_UV_PIXEL','WATER_FLOW_PHASE_PIXEL').replace('water-side-uv','water-flow-phase')
s=s.replace('"flowing",p>=3,"elapsedMillis",(p%3)*50', '"direction",DIRECTIONS[p],"originX",ORIGINS[p]')
s=s.replace('stillAndFlowing=true times=0,50,100','directions=0,2,4,6,10,14 worldPhase=true')
s=s.replace('scope=controlledProductionShaderNotLiveStreamAcceptance','scope=controlledPhaseHelperAndProductionShaderNotFullStreamAcceptance')
s=s.replace('Actual production shader pixels at matched displayed heights, with controlled water inputs.', 'Production shader and phase helper: two neighboring tiles versus one continuous reference; no live-world edits.')
(V/'WaterFlowPhaseChecks.java').write_text(s)
p=V/'ClientCapture.java';s=p.read_text().replace('      if (action.equals("water-side-uv-check")) {','      if (action.equals("water-flow-phase-check")) {\n        WaterFlowPhaseChecks.run(mc, dir);\n        return;\n      }\n      if (action.equals("water-side-uv-check")) {');p.write_text(s)
p=R/'run_vulkan.py';s=p.read_text();s=s.replace("elif mode.startswith('side-baseline'):","elif mode.startswith('phase-baseline'):\n actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('water-flow-phase-check','WATER_FLOW_PHASE_PIXEL_RESULT',0),('stop',None,0)]\nelif mode.startswith('side-baseline'):")
s=s.replace("('water-side-uv-check','WATER_SIDE_UV_PIXEL_NATIVE_PASS',0)","('water-flow-phase-check','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS',0),('water-side-uv-check','WATER_SIDE_UV_PIXEL_NATIVE_PASS',0)")
p.write_text(s)
print('phase pixel fixture added; production unchanged')
