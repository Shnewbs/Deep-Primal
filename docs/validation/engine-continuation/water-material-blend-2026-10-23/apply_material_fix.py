from pathlib import Path
import shutil,hashlib
R=Path(__file__).resolve().parent;P=R/'project'
files=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
BASE={'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh': '4e66a6cbe88917da289aa712820d61dd7e281ef6', 'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh': '5e62e48cdf240cf04cee2bd53ede1706ca2f3728', 'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java': '5312e8ef19a2300a23866917cc06b2b4660ee23f', 'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java': '0df1883e900bce457154067cac8c6ccc717f6d7a'}
for f in files:
 b=(P/f).read_bytes();assert hashlib.sha1(b"blob "+str(len(b)).encode()+b"\0"+b).hexdigest()==BASE[f],"Apply only to unchanged parent source: "+f
for f in files:shutil.copyfile(P/f,R/('before-'+Path(f).name))
p=P/files[0];s=p.read_text();s=s.replace('    if (next == 16 || target == 16) {\n      from = target = next;\n      started = now;\n    } else if ((from == target || now - started >= MILLIS) && next != target) {','    if ((from == target || now - started >= MILLIS) && next != target) {').replace('return from < 16 && target < 16 && from != target && age(now) < MILLIS;','return from != target && age(now) < MILLIS;')
p.write_text(s)
p=P/files[1];s=p.read_text().replace('private WaterFlowTransition flow(long key, int direction, long revision) {','private WaterFlowTransition flow(long key, int direction, long revision, int previousDirection) {').replace('if (value == null && direction < 16) {\n      value = new WaterFlowTransition(direction, clock);','if (value == null && (direction < 16 || previousDirection < 16)) {\n      value = new WaterFlowTransition(previousDirection, clock);')
s=s.replace('flowState = face == 0 ? flow(cellKey, direction, faces.revision) : null;', '''flowState = null;
            if (face == 0) {
              // Unchanged still water stays sparse; its accepted snapshot supplies the first endpoint.
              int previousDirection = p.snapshot != null && p.snapshot.water(cx, cy, cz) > 0
                  ? p.snapshot.direction(cx, cy, cz) : direction;
              flowState = flow(cellKey, direction, faces.revision, previousDirection);
            }''')
s=s.replace('boolean blend = face == 0 && shownDirection < 16 && flowState != null && flowState.active(clock);','boolean blend = face == 0 && flowState != null && flowState.active(clock);')
s=s.replace('''          if (flowTop) {
            float a = values[i] - cx - .5f, b = values[i + 2] - cz - .5f;
            textureU = .5f + .5f * (WaterPatchMesher.flowZ(shownDirection) * a - WaterPatchMesher.flowX(shownDirection) * b) + phaseU;
            topV = .5f + .5f * (WaterPatchMesher.flowX(shownDirection) * a + WaterPatchMesher.flowZ(shownDirection) * b);
          }''','''          if (face == 0) {
            if (flowTop) {
              float a = values[i] - cx - .5f, b = values[i + 2] - cz - .5f;
              textureU = .5f + .5f * (WaterPatchMesher.flowZ(shownDirection) * a - WaterPatchMesher.flowX(shownDirection) * b) + phaseU;
              topV = .5f + .5f * (WaterPatchMesher.flowX(shownDirection) * a + WaterPatchMesher.flowZ(shownDirection) * b);
            } else {
              // Raw captures may request flow while the admitted endpoint is still.
              textureU = values[i] - cx;
              topV = values[i + 2] - cz;
            }
          }''')
s=s.replace('p.texture.m20(still.getV(0)).m21(flowing.getV(0));','p.texture.m20(still.getV(0)).m21(flowing.getV(0));\n      p.texture.m13(still.getU(0)).m22(still.getU(1) - still.getU(0));')
assert 'flow(cellKey, direction, faces.revision)' not in s;p.write_text(s)
p=P/files[2];s=p.read_text().replace('flat out vec4 flowBounds;','flat out vec4 flowBounds;\nflat out vec4 previousBounds;')
start=s.index('    flowBounds = (UV2.x & 1024)')
end=s.index('\n    if ((UV2.x & 256)',start)
s=s[:start]+'''    vec4 flowTile = vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2]);
    vec4 stillTile = vec4(TextureMat[1][3], TextureMat[2][0], TextureMat[2][2], TextureMat[1][0]);
    flowBounds = (UV2.x & 1024) != 0 ? flowTile : vec4(0.0);
    previousBounds = vec4(0.0);
    previousWeight = (UV2.y & 32768) != 0
        ? clamp(1.0 - (TextureMat[0][0] + float((UV2.y >> 8) & 127)) / 100.0, 0.0, 1.0) : 0.0;
    previousTexCoord = UV0;
    if (previousWeight > 0.0) {
        int sourceDirection = clamp((UV2.x >> 11) & 31, 0, 16);
        if (sourceDirection < 16) {
            previousBounds = flowTile;
            previousTexCoord = previousFlow(sourceDirection, flowTile);
        } else {
            int cellBits = int(round(Color.a * 255.0));
            vec2 cell = vec2(cellBits & 15, cellBits >> 4);
            previousTexCoord = stillTile.xy + (Position.xz - cell) * stillTile.zw;
        }
    }
''' + s[end:]
p.write_text(s)
p=P/files[3];s=p.read_text().replace('flat in vec4 flowBounds;','flat in vec4 flowBounds;\nflat in vec4 previousBounds;').replace('vec2 oldUV = flowBounds.xy + fract((previousTexCoord - flowBounds.xy) / flowBounds.zw) * flowBounds.zw;', '''vec2 oldUV = previousBounds.z > 0.0
            ? previousBounds.xy + fract((previousTexCoord - previousBounds.xy) / previousBounds.zw) * previousBounds.zw
            : previousTexCoord;''')
p.write_text(s)
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowBlendChecks.java';s=p.read_text().replace('a<16;a++)for(int b=0;b<16;b++)','a<17;a++)for(int b=0;b<17;b++)').replace('int c=(b+1)&15;','int c=(b+1)%17;').replace('t.target(16,250,5);if(t.active(250)||t.target()!=16)throw new AssertionError("still reset");','''t.target(16,250,5);
      if(!t.active(250)||t.target()!=c||t.queued(250)!=(c!=16))throw new AssertionError("material disrupted blend");
      t.target(16,310,6);
      if(t.target()!=16||t.active(310)!=(c!=16))throw new AssertionError("material queued endpoint");''').replace('stillSwitchImmediate=true','stillSwitchBlended=true')
p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionUploadChecks.java';s=p.read_text().replace('getDeclaredMethod("flow",long.class,int.class,long.class)','getDeclaredMethod("flow",long.class,int.class,long.class,int.class)').replace('flowMethod.invoke(renderer,1_000_000L+n,n&15,1000L+n)','flowMethod.invoke(renderer,1_000_000L+n,n&15,1000L+n,n&15)');p.write_text(s)
