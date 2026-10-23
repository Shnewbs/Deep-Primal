from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';renderer=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';vertex=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh';fragment=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh'
for p in [renderer,vertex]:(R/('height-failed-'+p.name)).write_bytes(p.read_bytes())
s=renderer.read_text();a=s.index('      // Keep nonperiodic side/underside endpoints');b=s.index('      p.texture.m01(',a);s=s[:a]+s[b:];renderer.write_text(s)
s=vertex.read_text();a=s.index('            localUV.y = clamp(');b=s.index('        } else if (sourceDirection < 16)',a)
s=s[:a]+'''            localUV.y -= displacement;
            previousTexCoord = oldTile.xy + localUV * oldTile.zw * oldScale;
            // Negative width marks nonperiodic side bounds for fragment-space clamping.
            previousBounds = vec4(oldTile.xy, -oldTile.z, oldTile.w * oldScale);
''' +s[b:]
s=s.replace('''        float pad = min(TextureMat[2][3], span * 0.5);
        texCoord.y = clamp(UV0.y - displacement * span, start + pad, start + span - pad);''','''        texCoord.y = UV0.y - displacement * span;
        vec4 tile = flowing ? flowTile : stillTile;
        flowBounds = vec4(tile.xy, -tile.z, span);''')
s=s.replace('        float start = flowing ? TextureMat[2][1] : TextureMat[2][0];\n','')
vertex.write_text(s)
b=fragment.read_bytes();parent={x['path']:x['sha'] for x in json.loads((R/'parent-blob-manifest.json').read_text())};assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==parent[str(fragment.relative_to(P))];(R/'before-animated_water.fsh').write_bytes(b)
s=b.decode();s=s.replace('    // Derive LOD from the unwrapped phase', '''    float texelY = 0.5 / float(textureSize(Sampler0, 0).y);
    if (flowBounds.z < 0.0) {
        float pad = min(texelY, flowBounds.w * 0.5);
        uv.y = clamp(uv.y, flowBounds.y + pad, flowBounds.y + flowBounds.w - pad);
    }
    // Derive LOD from the unwrapped phase''').replace('vec4 sampled = flowBounds.z > 0.0 ?', 'vec4 sampled = flowBounds.z != 0.0 ?')
s=s.replace('        sampled = mix(sampled,', '''        if (previousBounds.z < 0.0) {
            float pad = min(texelY, previousBounds.w * 0.5);
            oldUV.y = clamp(oldUV.y, previousBounds.y + pad, previousBounds.y + previousBounds.w - pad);
        }
        sampled = mix(sampled,''')
fragment.write_text(s)
(R/'height-material-reproduction.json').write_text(json.dumps({'case':'vulkan-side-material-baseline-attempt2','production_variant':'vertex-space half-texel guard','fixed_height_controls_pass':True,'new_height_controls':'16 matched physical-pixel routes,4/16 to16/16 height plus material transition','bad_vertices':0,'channels_beyond_tolerance':1536,'max_channel_error':9,'cause':'Clamping vertex endpoint V distorts interpolated UV slope as the quad height changes.','fix':'Keep side UVs and gradients unwrapped through interpolation; clamp current andprevious atlas endpoints perfragment. Reuse existing bounds varyings withnegative width asnonperiodic tag; no new payload or varying.','excluded_earlier_fixture':'attempt1 selected nonexistent extra-route face indices;max255 not a production defect'},indent=2)+'\n')
print('moved side atlas V guard to fragment sampling; removed spare-matrix padding value')
