from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
paths=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
for path in paths:(R/('before-'+Path(path).name)).write_bytes((P/path).read_bytes())
p=P/paths[0];s=p.read_text();at=s.index('  private int flowMaterial;');s=s[:at]+'''  /** Periodic phase at a world cell origin; double intermediates retain distant/negative seams. */
  public static float flowUOffset(int direction, int worldX, int worldZ) {
    if (direction < 0 || direction >= 16) return 0;
    double phase = .5 * ((double) FLOW_Z[direction] * worldX - (double) FLOW_X[direction] * worldZ);
    return (float) (phase - Math.floor(phase));
  }

  public static float flowVOffset(int direction, int worldX, int worldZ) {
    if (direction < 0 || direction >= 16) return 0;
    double phase = .5 * ((double) FLOW_X[direction] * worldX + (double) FLOW_Z[direction] * worldZ);
    return (float) (phase - Math.floor(phase));
  }

'''+s[at:];p.write_text(s)
p=P/paths[1];s=p.read_text().replace('        float[] values = faces.vertices;','        float[] values = faces.vertices;\n        float phaseU = 0, phaseV = 0;');s=s.replace('          if ((v & 3) == 0) {','          if ((v & 3) == 0) {\n            int direction = WaterPatchMesher.direction(values[i + 10]);\n            if (face == 0 && direction < 16) {\n              phaseU = WaterPatchMesher.flowUOffset(direction, p.x + cx, p.z + cz);\n              phaseV = WaterPatchMesher.flowVOffset(direction, p.x + cx, p.z + cz);\n            }')
s=s.replace('          float textureV = side ? (1 - target) * (flowingFace ? .5f : 1) : values[i + 6];\n          data.putFloat(sprite.getU(values[i + 5])).putFloat(sprite.getV(textureV));','''          boolean flowTop = face == 0 && flowingFace;
          float textureU = values[i + 5] + (flowTop ? phaseU : 0);
          float textureV = side ? (1 - target) * (flowingFace ? .5f : 1) : values[i + 6] + (flowTop ? phaseV : 0);
          data.putFloat(sprite.getU(textureU)).putFloat(sprite.getV(textureV));''')
s=s.replace('          int presentation = side ? 256 | (flowingFace ? 512 : 0) : 0;','          int presentation = side ? 256 | (flowingFace ? 512 : 0) : flowTop ? 1024 : 0;')
s=s.replace('      p.texture.m20(still.getV(0)).m21(flowing.getV(0));','''      p.texture.m20(still.getV(0)).m21(flowing.getV(0));
      p.texture.m01(flowing.getU(0)).m02(flowing.getU(1) - flowing.getU(0));
      p.texture.m03(flowing.getV(0)).m12(flowing.getV(1) - flowing.getV(0));''')
s=s.replace('reserve bits 8/9 for side UV mode.','reserve bits 8/9 for side mode and bit 10 for periodic flow tops.')
p.write_text(s)
p=P/paths[2];s=p.read_text().replace('out vec2 texCoord;','out vec2 texCoord;\nflat out vec4 flowBounds;');s=s.replace('    texCoord = UV0;','''    texCoord = UV0;
    flowBounds = (UV2.x & 1024) != 0
        ? vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2])
        : vec4(0.0);''');p.write_text(s)
p=P/paths[3];s=p.read_text().replace('in vec2 texCoord;','in vec2 texCoord;\nflat in vec4 flowBounds;');s=s.replace('    vec4 color = texture(Sampler0, texCoord) * vertexColor;','''    // Keep interpolated world phase unwrapped until sampling; never interpolate a tile seam.
    vec2 uv = flowBounds.z > 0.0
        ? flowBounds.xy + fract((texCoord - flowBounds.xy) / flowBounds.zw) * flowBounds.zw
        : texCoord;
    vec4 color = texture(Sampler0, uv) * vertexColor;''');p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFlowTextureChecks.java';s=p.read_text().replace('                if(Math.abs(data.getFloat(v*36+16)', '''                if(s.direction<16) {
                  eu+=WaterPatchMesher.flowUOffset(s.direction,position.getX(),position.getZ());
                  ev+=WaterPatchMesher.flowVOffset(s.direction,position.getX(),position.getZ());
                  if((data.getShort(v*36+28)&1024)==0)throw new AssertionError("Missing periodic flow-top tag");
                }
                if(Math.abs(data.getFloat(v*36+16)''');p.write_text(s)
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowTextureChecks.java';s=p.read_text().replace('    System.out.println("WATER_FLOW_TEXTURE_CORE_PASS', '''    var random = new java.util.Random(0xFA5E);
    double maxError=0; int edges=0;
    for(int direction=0;direction<16;direction++)for(int sample=0;sample<6250;sample++) {
      int x=sample<8?new int[]{-1,0,15,16,-16,-17,29999998,-29999999}[sample]:random.nextInt(60000000)-30000000;
      int z=random.nextInt(60000000)-30000000;
      double fx=(float)Math.cos(direction*Math.PI/8),fz=(float)Math.sin(direction*Math.PI/8);
      for(boolean alongX:new boolean[]{true,false})for(boolean u:new boolean[]{true,false}) {
        float a=u?WaterPatchMesher.flowUOffset(direction,x,z):WaterPatchMesher.flowVOffset(direction,x,z);
        float b=u?WaterPatchMesher.flowUOffset(direction,x+(alongX?1:0),z+(alongX?0:1)):WaterPatchMesher.flowVOffset(direction,x+(alongX?1:0),z+(alongX?0:1));
        double expected=.5*(u?(alongX?fz:-fx):(alongX?fx:fz));
        double gap=(b-a)-expected;gap-=Math.rint(gap);maxError=Math.max(maxError,Math.abs(gap));
        require(a>=0&&a<=1&&b>=0&&b<=1&&Math.abs(gap)<2e-7,"world phase discontinuity at shared or distant edge");edges++;
      }
    }
    require(WaterPatchMesher.flowUOffset(16,-1,16)==0&&WaterPatchMesher.flowVOffset(16,16,-1)==0,"still material has no flow phase");
    System.out.println("WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges="+edges+" maxCircularError="+maxError+" negativeDistantSectionEdges=true phaseBounded=true");
    System.out.println("WATER_FLOW_TEXTURE_CORE_PASS''');p.write_text(s)
print('focused phase fix applied')
