from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
v=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh'
s=v.read_text().replace('uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]));','int packed = int(round(Color.a * 255.0));\n    ivec2 cell = ivec2(packed & 15, packed >> 4);\n    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]) + cell);').replace('local = Position.xz - vec2(0.5);','local = Position.xz - vec2(cell) - vec2(0.5);')
s=s.replace('vertexColor = Color * ColorModulator', '// Active top blends reuse the otherwise constant color alpha for cell coordinates.\n    vec4 tint = Color;\n    if ((UV2.y & 32768) != 0) tint.a = 1.0;\n    vertexColor = tint * ColorModulator')
v.write_text(s)
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text()
s=s.replace('data.put((byte) (color >> 16)).put((byte) (color >> 8)).put((byte) color).put((byte) 255);','boolean blend = face == 0 && shownDirection < 16 && flowState != null && flowState.active(clock);\n          // Alpha transitions live in Normal: reuse constant color alpha for exact cell-local UVs.\n          data.put((byte) (color >> 16)).put((byte) (color >> 8)).put((byte) color)\n              .put((byte) (blend ? cx | (cz << 4) : 255));')
s=s.replace('          boolean blend = flowTop && flowState != null && flowState.active(clock);\n','')
p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionBlendChecks.java';s=p.read_text()
s=s.replace('float x=i==2||i==3?1:0,z=i==1||i==2?1:0;', 'int cell=originX==0?0:15;\n        float x=i==2||i==3?1:0,z=i==1||i==2?1:0;')
s=s.replace('b.putFloat(x).putFloat(1).putFloat(z).putInt(0xffffffff);','b.putFloat(x+cell).putFloat(1).putFloat(z+cell).putInt(active?((cell|(cell<<4))<<24)|0xffffff:0xffffffff);')
s=s.replace('flowUOffset(direction,originX,originZ)', 'flowUOffset(direction,originX+cell,originZ+cell)').replace('flowVOffset(direction,originX,originZ)','flowVOffset(direction,originX+cell,originZ+cell)')
s=s.replace('.m30(-1).m31(-1).m32', '.m30(originX==0?-1:-31).m31(originX==0?-1:-31).m32')
p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionUploadChecks.java';s=p.read_text().replace('boolean active=(sky&32768)!=0;', 'boolean active=(sky&32768)!=0;\n              if((b.get(at+15)&255)!=(active?51:255))throw new AssertionError("Cell phase packing corrupted color alpha");')
p.write_text(s)
for name in ['audit_water_side.py','audit_opengl_side.py']:
 p=R/name;s=p.read_text().replace("if active:assert light>>11==row['source'] and ((sky>>8)&127)==row['age']", "assert data[off+15]==(51 if active else 255)\n            if active:assert light>>11==row['source'] and ((sky>>8)&127)==row['age']")
 p.write_text(s)
