from pathlib import Path
import math,struct
R=Path(__file__).resolve().parent;P=R/'project';C=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core'
files=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
for n in files:(R/('before-'+Path(n).name)).write_bytes((P/n).read_bytes())
(C/'WaterFlowTransition.java').write_text('''package com.shnewbs.deepprimal.core;

/** Two texture endpoints and one latest request. Never retarget an unfinished blend. */
public final class WaterFlowTransition {
  public static final int MILLIS = 100;
  private int from, target, pending;
  private long started, revision = Long.MIN_VALUE;
  public WaterFlowTransition(int initial, long now) {
    from = target = pending = initial;
    started = now;
  }
  public void target(int next, long now, long version) {
    if (version < revision) return;
    revision = version;
    pending = next;
    if (next == 16 || target == 16) {
      from = target = next;
      started = now;
    } else if (now - started >= MILLIS && next != target) {
      from = target;
      target = next;
      started = now;
    }
  }
  public int target() { return target; }
  public int source() { return from; }
  public int age(long now) { return (int)Math.clamp(now - started, 0, MILLIS); }
  public boolean active(long now) { return from < 16 && target < 16 && from != target && age(now) < MILLIS; }
  public boolean queued(long now) { return active(now) && pending != target; }
}
''')
p=C/'WaterPatchMesher.java';s=p.read_text().replace('  private int flowMaterial;','  public static float flowX(int direction) { return FLOW_X[direction]; }\n  public static float flowZ(int direction) { return FLOW_Z[direction]; }\n\n  private int flowMaterial;');p.write_text(s)
p=P/files[1];s=p.read_text().replace('import com.shnewbs.deepprimal.core.WaterPatchMesher;','import com.shnewbs.deepprimal.core.WaterPatchMesher;\nimport com.shnewbs.deepprimal.core.WaterFlowTransition;')
s=s.replace('  private final ArrayList<Future<Ready>> work', '  private final Long2ObjectLinkedOpenHashMap<WaterFlowTransition> flowDirections = new Long2ObjectLinkedOpenHashMap<>();\n  private final ArrayList<Future<Ready>> work')
at=s.index('  private void upload(Ready ready)');s=s[:at]+'''  private WaterFlowTransition flow(long key, int direction, long revision) {
    var value = flowDirections.getAndMoveToLast(key);
    if (value == null && direction < 16) {
      value = new WaterFlowTransition(direction, clock);
      flowDirections.put(key, value);
    }
    if (value != null) value.target(direction, clock, revision);
    while (flowDirections.size() > MAX_HISTORY) flowDirections.removeFirst();
    return value;
  }

'''+s[at:]
s=s.replace('        float phaseU = 0, phaseV = 0;','        float phaseU = 0, phaseV = 0;\n        int shownDirection = 16;\n        WaterFlowTransition flowState = null;')
s=s.replace('            int direction = WaterPatchMesher.direction(values[i + 10]);\n            if (face == 0 && direction < 16) {','''            int direction = WaterPatchMesher.direction(values[i + 10]);
            flowState = face == 0 ? flow(cellKey, direction, faces.revision) : null;
            shownDirection = flowState == null ? direction : flowState.target();
            if (flowState != null && flowState.queued(clock)) {
              animating = true;
              remaining = Math.max(remaining, WaterFlowTransition.MILLIS - flowState.age(clock));
            }
            if (face == 0 && shownDirection < 16) {
              direction = shownDirection;''')
s=s.replace('          float textureU = values[i + 5] + (flowTop ? phaseU : 0);','''          float textureU = values[i + 5] + (flowTop ? phaseU : 0);
          float topV = values[i + 6];
          if (flowTop) {
            float a = values[i] - cx - .5f, b = values[i + 2] - cz - .5f;
            textureU = .5f + .5f * (WaterPatchMesher.flowZ(shownDirection) * a - WaterPatchMesher.flowX(shownDirection) * b) + phaseU;
            topV = .5f + .5f * (WaterPatchMesher.flowX(shownDirection) * a + WaterPatchMesher.flowZ(shownDirection) * b);
          }''')
s=s.replace(': values[i + 6] + (flowTop ? phaseV : 0);',': topV + (flowTop ? phaseV : 0);')
s=s.replace('          data.putShort((short) ((light & 255) | presentation)).putShort((short) (light >>> 16));','''          boolean blend = flowTop && flowState != null && flowState.active(clock);
          int source = blend ? flowState.source() << 11 : 0;
          int age = blend ? 32768 | (flowState.age(clock) << 8) : 0;
          data.putShort((short) ((light & 255) | presentation | source)).putShort((short) (((light >>> 16) & 255) | age));''')
s=s.replace('      p.texture.m03(flowing.getV(0)).m12(flowing.getV(1) - flowing.getV(0));','      p.texture.m03(flowing.getV(0)).m12(flowing.getV(1) - flowing.getV(0));\n      p.texture.m30(p.x).m31(p.z);')
s=s.replace('    alpha.forEach(Map::clear);','    alpha.forEach(Map::clear);\n    flowDirections.clear();');p.write_text(s)
coeff=[]
for d in range(16):
 def f(v):return struct.unpack('f',struct.pack('f',v))[0]
 coeff.append((round(f(math.cos(d*math.pi/8))*33554432),round(f(math.sin(d*math.pi/8))*33554432)))
table=',\n    '.join('ivec2(%d, %d)'%c for c in coeff)
p=P/files[2];s=p.read_text().replace('flat out vec4 flowBounds;','flat out vec4 flowBounds;\nout vec2 previousTexCoord;\nflat out float previousWeight;')
at=s.index('void main()')
s=s[:at]+'''// Q25 coefficients keep world-origin phase exact using uint modular arithmetic.
const ivec2 FLOW_Q25[16] = ivec2[16](
    '''+table+''');
vec2 previousFlow(int direction) {
    ivec2 c = FLOW_Q25[direction];
    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]));
    uvec2 phase = uvec2(uint(c.y) * world.x - uint(c.x) * world.y,
                       uint(c.x) * world.x + uint(c.y) * world.y) & uvec2(67108863u);
    vec2 f = vec2(c) / 33554432.0, local = Position.xz - vec2(0.5);
    vec2 uv = vec2(0.5) + 0.5 * vec2(f.y * local.x - f.x * local.y, f.x * local.x + f.y * local.y)
              + vec2(phase) / 67108864.0;
    return flowBounds.xy + uv * flowBounds.zw;
}
'''+s[at:]
s=s.replace('ivec2(UV2.x & 255, UV2.y)', 'ivec2(UV2.x & 255, UV2.y & 255)')
needle='        : vec4(0.0);';s=s.replace(needle,needle+'''
    previousWeight = (UV2.y & 32768) != 0
        ? clamp(1.0 - (TextureMat[0][0] + float((UV2.y >> 8) & 127)) / 100.0, 0.0, 1.0) : 0.0;
    previousTexCoord = UV0;
    if (previousWeight > 0.0) previousTexCoord = previousFlow(clamp((UV2.x >> 11) & 31, 0, 15));
''');p.write_text(s)
p=P/files[3];s=p.read_text().replace('flat in vec4 flowBounds;','flat in vec4 flowBounds;\nin vec2 previousTexCoord;\nflat in float previousWeight;')
s=s.replace('    vec4 color = sampled * vertexColor;','''    if (previousWeight > 0.0) {
        vec2 oldUV = flowBounds.xy + fract((previousTexCoord - flowBounds.xy) / flowBounds.zw) * flowBounds.zw;
        sampled = mix(sampled, textureGrad(Sampler0, oldUV, dFdx(previousTexCoord), dFdy(previousTexCoord)), previousWeight);
    }
    vec4 color = sampled * vertexColor;''');p.write_text(s)
T=P/'src/coreChecks/java/com/shnewbs/deepprimal/core'
(T/'WaterFlowBlendChecks.java').write_text('''package com.shnewbs.deepprimal.core;
final class WaterFlowBlendChecks {
  static void run() {
    int cases=0;
    for(int a=0;a<16;a++)for(int b=0;b<16;b++)if(a!=b) {
      var t=new WaterFlowTransition(a,0);t.target(b,100,1);
      if(t.source()!=a||t.target()!=b||!t.active(100))throw new AssertionError("initial blend");
      t.target(b,125,2);if(t.age(125)!=25)throw new AssertionError("reupload restarted blend");
      int c=(b+1)&15;t.target(c,150,3);
      if(t.target()!=b||!t.queued(150)||t.age(150)!=50)throw new AssertionError("rapid request disrupted blend");
      t.target(a,175,0);if(t.target()!=b||!t.queued(175))throw new AssertionError("stale revision admitted");
      t.target(c,210,4);if(t.source()!=b||t.target()!=c||t.age(210)!=0)throw new AssertionError("queued endpoint");
      t.target(16,250,5);if(t.active(250)||t.target()!=16)throw new AssertionError("still reset");
      cases++;
    }
    System.out.println("WATER_FLOW_BLEND_CORE_PASS pairs="+cases+" repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchImmediate=true");
  }
}
''')
p=T/'CoreChecks.java';s=p.read_text().replace('    WaterFlowTextureChecks.run();','    WaterFlowTextureChecks.run();\n    WaterFlowBlendChecks.run();');p.write_text(s)
print('bounded flow-direction blend applied')
