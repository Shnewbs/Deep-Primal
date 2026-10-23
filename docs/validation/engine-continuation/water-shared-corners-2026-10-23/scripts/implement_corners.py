from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
paths=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
for name in paths:
 p=R/'before'/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes((P/name).read_bytes())
core=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowField.java'
core.write_text('''package com.shnewbs.deepprimal.core;

/** Canonical visual direction at one global water corner. Never changes quantity. */
public final class WaterFlowField {
  public static int direction(WaterPatchMesher.Snapshot s, int x, int y, int z) {
    if (s == null) return -1;
    double vx = 0, vz = 0; boolean wet = false;
    for (int dz = -1; dz <= 0; dz++) for (int dx = -1; dx <= 0; dx++) {
      if (s.water(x + dx, y, z + dz) <= 0) continue;
      wet = true; int d = s.direction(x + dx, y, z + dz);
      if (d < 16) { vx += WaterPatchMesher.flowX(d); vz += WaterPatchMesher.flowZ(d); }
    }
    if (!wet) return -1;
    if (vx * vx + vz * vz < 1e-12) return 16;
    return (int) Math.round(Math.atan2(vz, vx) * (8 / Math.PI)) & 15;
  }
  /** Four 5-bit IDs plus the low/high four/three bits of the fourth age.
   * The valid ID range leaves every encoded integer strictly below 2^24. */
  public static float targets(int directions, int fourthAge) {
    return -(directions | ((fourthAge & 15) << 20)) - 2f;
  }
  public static float sources(int directions, int fourthAge) {
    return directions | ((fourthAge >>> 4) << 20);
  }
  private WaterFlowField() {}
}
''')
p=P/paths[0];s=p.read_text();s=s.replace('import com.shnewbs.deepprimal.core.WaterPatchMesher;','import com.shnewbs.deepprimal.core.WaterPatchMesher;\nimport com.shnewbs.deepprimal.core.WaterFlowField;')
needle='  private final ArrayList<Future<Ready>> work = new ArrayList<>();';assert needle in s
s=s.replace(needle,'  private final Long2ObjectLinkedOpenHashMap<WaterFlowTransition> cornerDirections = new Long2ObjectLinkedOpenHashMap<>();\n'+needle)
at=s.index('  private void upload(Ready ready)')
s=s[:at]+'''  private WaterFlowTransition cornerFlow(long key, int direction, long revision, int previous) {
    var value = cornerDirections.getAndMoveToLast(key);
    if (value == null && (direction < 16 || previous < 16)) {
      value = new WaterFlowTransition(previous, clock);
      cornerDirections.put(key, value);
    }
    if (value != null) value.target(direction, clock, revision);
    while (cornerDirections.size() > MAX_HISTORY) cornerDirections.removeFirst();
    return value;
  }

'''+s[at:]
s=s.replace('        WaterFlowTransition flowState = null;','        WaterFlowTransition flowState = null;\n        int cornerTargets = 0, cornerSources = 0, cornerAges = 0;')
needle='''            opacity =
                transition(alpha.get(face), cellKey, values[i + 12], values[i + 13]);'''
assert needle in s
s=s.replace(needle,'''            if (face == 0) {
              cornerTargets = cornerSources = cornerAges = 0;
              for (int n = 0; n < 4; n++) {
                int x = cx + (n & 1), z = cz + (n >> 1);
                int directionAtCorner = WaterFlowField.direction(ready.snapshot, x, cy, z);
                int old = WaterFlowField.direction(p.snapshot, x, cy, z);
                if (directionAtCorner < 0) directionAtCorner = old < 0 ? direction : old;
                if (old < 0) old = directionAtCorner;
                var corner = cornerFlow(BlockPos.asLong(p.x + x, p.y + cy, p.z + z), directionAtCorner, faces.revision, old);
                int to = corner == null ? directionAtCorner : corner.target();
                int from = corner == null ? to : corner.source();
                int age = corner == null ? 100 : corner.age(clock);
                cornerTargets |= to << (n * 5); cornerSources |= from << (n * 5);
                cornerAges |= age << (n * 8);
                if (corner != null && corner.queued(clock)) {
                  animating = true;
                  remaining = Math.max(remaining, 100 - age);
                }
              }
            }
'''+needle)
s=s.replace('.put((byte) (blend ? cx | (cz << 4) : 255));','.put((byte) (face == 0 || blend ? cx | (cz << 4) : 255));')
s=s.replace('''          data.putFloat(sprite.getU(textureU)).putFloat(sprite.getV(textureV));
          data.putShort((short) Math.round(Math.clamp(from - target, -1, 1) * 16384))
              .putShort((short) offset);''','''          if (face == 0) {
            data.putFloat(WaterFlowField.targets(cornerTargets, (cornerAges >>> 24) & 255))
                .putFloat(WaterFlowField.sources(cornerSources, (cornerAges >>> 24) & 255));
          } else data.putFloat(sprite.getU(textureU)).putFloat(sprite.getV(textureV));
          data.putShort((short) Math.round(Math.clamp(from - target, -1, 1) * 16384))
              .putShort((short) (face == 0 ? (offset & 255) | ((cornerAges & 255) << 8) : offset));''')
s=s.replace('''          data.putShort((short) ((light & 255) | presentation | source)).putShort((short) (((light >>> 16) & 255) | age));''','''          data.putShort((short) ((light & 255) | (face == 0 ? ((cornerAges >>> 8) & 255) << 8 : presentation | source)))
              .putShort((short) (((light >>> 16) & 255) | (face == 0 ? ((cornerAges >>> 16) & 255) << 8 : age)));''')
s=s.replace('    flowDirections.clear();','    flowDirections.clear();\n    cornerDirections.clear();');p.write_text(s)
# Reuse one coefficient table in both shader stages, avoiding independently drifting copies.
v=P/paths[1];vs=v.read_text();start=vs.index('// Q25 coefficients');end=vs.index('vec2 previousFlow',start);table=vs[start:end]
inc=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/include/water_flow.glsl';inc.parent.mkdir(parents=True,exist_ok=True);inc.write_text(table)
vs=vs[:start]+'#moj_import <deepprimal_graphics:water_flow.glsl>\n'+vs[end:]
vs=vs.replace('flat out float previousWeight;','flat out float previousWeight;\nflat out ivec4 waterCorners;')
vs=vs.replace('''    float t = clamp((TextureMat[0][0] - float(UV1.y)) / 100.0, 0.0, 1.0);''','''    bool spatial = UV0.x < -1.0;
    int heightOffset = spatial ? (UV1.y & 255) : UV1.y;
    if (spatial && heightOffset > 127) heightOffset -= 256;
    float t = clamp((TextureMat[0][0] - float(heightOffset)) / 100.0, 0.0, 1.0);''')
vs=vs.replace('if ((UV2.y & 32768) != 0) tint.a = 1.0;','if (spatial || (UV2.y & 32768) != 0) tint.a = 1.0;')
needle='''    flowBounds = (UV2.x & 1024) != 0 ? flowTile : vec4(0.0);''';assert needle in vs
vs=vs.replace(needle,'''    waterCorners = ivec4(0);
    if (spatial) {
        int to = int(round(-UV0.x)) - 2, from = int(round(UV0.y));
        int fourthAge = ((to >> 20) & 15) | (((from >> 20) & 7) << 4);
        int ages = ((UV1.y >> 8) & 255) | (((UV2.x >> 8) & 255) << 8)
                 | (((UV2.y >> 8) & 255) << 16) | (fourthAge << 24);
        int cellBits = int(round(Color.a * 255.0));
        waterCorners = ivec4(to & 1048575, from & 1048575, ages, cellBits);
        flowBounds = flowTile; previousBounds = stillTile;
        texCoord = Position.xz - vec2(cellBits & 15, cellBits >> 4);
        previousTexCoord = vec2(0.0); previousWeight = -1.0;
        return;
    }
'''+needle);v.write_text(vs)
f=P/paths[2];fs=f.read_text();fs=fs.replace('#moj_import <minecraft:fog.glsl>','#moj_import <minecraft:fog.glsl>\n#moj_import <minecraft:dynamictransforms.glsl>\n#moj_import <deepprimal_graphics:water_flow.glsl>')
fs=fs.replace('flat in float previousWeight;','flat in float previousWeight;\nflat in ivec4 waterCorners;')
at=fs.index('void main()')
fs=fs[:at]+'''vec4 spatialSample(int direction, vec2 local, vec2 dx, vec2 dy) {
    if (direction == 16) {
        vec2 uv = previousBounds.xy + fract(local) * previousBounds.zw;
        return textureGrad(Sampler0, uv, dx * previousBounds.zw, dy * previousBounds.zw);
    }
    ivec2 c = FLOW_Q25[direction];
    ivec2 cell = ivec2(waterCorners.w & 15, waterCorners.w >> 4);
    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]) + cell);
    uvec2 phase = uvec2(uint(c.y) * world.x - uint(c.x) * world.y,
                       uint(c.x) * world.x + uint(c.y) * world.y) & uvec2(67108863u);
    vec2 flow = vec2(c) / 33554432.0, p = local - vec2(0.5);
    vec2 uv = vec2(0.5) + 0.5 * vec2(flow.y*p.x-flow.x*p.y, flow.x*p.x+flow.y*p.y) + vec2(phase)/67108864.0;
    vec2 ux = 0.5 * vec2(flow.y*dx.x-flow.x*dx.y, flow.x*dx.x+flow.y*dx.y);
    vec2 uy = 0.5 * vec2(flow.y*dy.x-flow.x*dy.y, flow.x*dy.x+flow.y*dy.y);
    return textureGrad(Sampler0, flowBounds.xy + fract(uv) * flowBounds.zw, ux * flowBounds.zw, uy * flowBounds.zw);
}
vec4 spatialColor(vec2 local, vec2 dx, vec2 dy) {
    int to = waterCorners.x, from = waterCorners.y;
    // Uniform settled surfaces retain the original single texture evaluation.
    if (to == from && to == ((to & 31) * 33825))
        return spatialSample(to & 31, local, dx, dy);
    vec2 w = clamp(local, vec2(0.0), vec2(1.0));
    vec4 result = vec4(0.0);
    for (int n = 0; n < 4; n++) {
        int target = (to >> (n*5)) & 31, source = (from >> (n*5)) & 31;
        float age = TextureMat[0][0] + float((waterCorners.z >> (n*8)) & 255);
        float weight = ((n&1)==0 ? 1.0-w.x : w.x) * ((n&2)==0 ? 1.0-w.y : w.y);
        if (weight > 0.0) {
            vec4 sampled = spatialSample(target, local, dx, dy);
            if (source != target && age < 100.0)
                sampled = mix(spatialSample(source, local, dx, dy), sampled, clamp(age/100.0,0.0,1.0));
            result += sampled * weight;
        }
    }
    return result;
}
'''+fs[at:]
fs=fs.replace('''    vec4 sampled = flowBounds.z != 0.0 ? textureGrad(Sampler0, uv, dx, dy) : texture(Sampler0, uv);''','''    vec4 sampled = previousWeight < 0.0 ? spatialColor(texCoord, dx, dy)
        : flowBounds.z != 0.0 ? textureGrad(Sampler0, uv, dx, dy) : texture(Sampler0, uv);''')
f.write_text(fs)
print('implemented shared-corner renderer and shader path at 36-byte stride')
