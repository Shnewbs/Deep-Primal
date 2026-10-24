from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';prod=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh'];parent={x['path']:x['sha'] for x in json.loads((R/'parent-blob-manifest.json').read_text())}
for name in prod:
 b=(P/name).read_bytes();gs=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest();assert gs==parent[name];(R/('before-'+Path(name).name)).write_bytes(b)
p=P/prod[0];s=p.read_text();a=s.index('            flowState = null;');b=s.index('            shownDirection',a)
s=s[:a]+'''            // All faces share the cell's admitted material and original transition clock.
            // Unchanged still water stays sparse; the accepted snapshot seeds its first change.
            int previousDirection = p.snapshot != null && p.snapshot.water(cx, cy, cz) > 0
                ? p.snapshot.direction(cx, cy, cz) : direction;
            flowState = flow(cellKey, direction, faces.revision, previousDirection);
''' +s[b:]
s=s.replace('boolean blend = face == 0 && flowState != null && flowState.active(clock);','''boolean blend = flowState != null && flowState.active(clock)
              && (face == 0 || (flowState.source() < 16) != (shownDirection < 16));''')
s=s.replace('// Alpha transitions live in Normal: reuse constant color alpha for exact cell-local UVs.','// Opacity lives in Normal; active faces reuse constant color alpha for cell-local UVs.')
s=s.replace('boolean flowingFace = (face == 0 ? shownDirection : WaterPatchMesher.direction(values[i + 10])) < 16;','boolean flowingFace = shownDirection < 16;').replace('boolean side = face >= 1 && face <= 4;','boolean side = face != 0;')
s=s.replace('float textureU = values[i + 5] + (flowTop ? phaseU : 0);','''float textureU = values[i + 5] + (flowTop ? phaseU : 0);
          if (side) {
            // Raw meshing may contain a queued/stale material. Recover its cell-local U
            // before applying the admitted sprite's full/half-tile scale.
            float localU = values[i + 5] * (WaterPatchMesher.direction(values[i + 10]) < 16 ? 2 : 1);
            textureU = localU * (flowingFace ? .5f : 1);
          }''')
p.write_text(s)
p=P/prod[1];s=p.read_text().replace('// Active top blends reuse the otherwise constant color alpha for cell coordinates.','// Active material blends reuse the otherwise constant color alpha for cell coordinates.')
a=s.index('        if (sourceDirection < 16) {');b=s.index('\n    }\n\n    if ((UV2.x & 256)',a)
s=s[:a]+'''        if ((UV2.x & 256) != 0) {
            bool currentFlow = (UV2.x & 512) != 0;
            bool oldFlow = sourceDirection < 16;
            vec4 currentTile = currentFlow ? flowTile : stillTile;
            vec4 oldTile = oldFlow ? flowTile : stillTile;
            float currentScale = currentFlow ? 0.5 : 1.0;
            float oldScale = oldFlow ? 0.5 : 1.0;
            // Convert admitted UVs to cell-local coordinates, then apply the same
            // displayed height displacement to both material endpoints.
            vec2 localUV = (UV0 - currentTile.xy) / (currentTile.zw * currentScale);
            localUV.y = clamp(localUV.y - displacement, 0.0, 1.0);
            previousTexCoord = oldTile.xy + localUV * oldTile.zw * oldScale;
        } else if (sourceDirection < 16) {
            previousBounds = flowTile;
            previousTexCoord = previousFlow(sourceDirection, flowTile);
        } else {
            int cellBits = int(round(Color.a * 255.0));
            vec2 cell = vec2(cellBits & 15, cellBits >> 4);
            previousTexCoord = stillTile.xy + (Position.xz - cell) * stillTile.zw;
        }''' +s[b:]
p.write_text(s)
# Retained top-material test now verifies the extra side metadata explicitly.
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterMaterialTransitionChecks.java';s=p.read_text().replace('expected=v<4&&s.active','expected=s.active').replace('if(active&&v<4)','if(active)');p.write_text(s)
print('applied two production files; retained material metadata expectation extended to sides')
