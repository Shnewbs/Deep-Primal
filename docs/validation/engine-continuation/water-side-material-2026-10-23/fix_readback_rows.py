from pathlib import Path
import json
R=Path(__file__).resolve().parent;p=R/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterSideMaterialChecks.java';s=p.read_text();(R/'gl-row-rejected-WaterSideMaterialChecks.java').write_text(s)
s=s.replace('var device=RenderSystem.getDevice();var encoder=', 'var device=RenderSystem.getDevice();boolean flipReadbackRows=device.getBackendName().equals("OpenGL");\n    Files.writeString(out.resolve("water-side-material-readback-convention.json"),new com.google.gson.Gson().toJson(Map.of("backend",device.getBackendName(),"flipped_raw_rows",flipReadbackRows,"output_row_zero","upper physical Y"))+"\\n");\n    var encoder=')
s=s.replace('b.get(0,data);rgba[p.profile]', 'b.get(0,data);if(flipReadbackRows)flipRows(data,p.size);rgba[p.profile]')
s=s.replace('image.setPixelABGR(x,y,b.getInt((y*p.size+x)*4))','image.setPixelABGR(x,y,ByteBuffer.wrap(data).order(ByteOrder.nativeOrder()).getInt((y*p.size+x)*4))')
at=s.index('  private static Matrix4f view(');s=s[:at]+'''  private static void flipRows(byte[] data,int size) {
    int stride=size*4;
    for(int y=0;y<size/2;y++)for(int x=0;x<stride;x++) {
      int a=y*stride+x,b=(size-1-y)*stride+x;byte value=data[a];data[a]=data[b];data[b]=value;
    }
  }
''' +s[at:];p.write_text(s)
(R/'gl-readback-row-correction.json').write_text(json.dumps({'excluded_case':'vulkan-reuse-gl-side-material-fragment-final','first320_fixed_height_routes_pass':True,'height_control_failure_channels':59392,'max_error':255,'cause':'Raw OpenGL texture readback row0 is lower physicalY; Vulkan row0 isupper. The new lower-quarter geometry crop assumed Vulkan ordering.','correction':'Normalize only this diagnostic pixel readback by the explicit device backend. Record the convention, flip OpenGL rows before bothPNG output andindependentweighted comparisons. Keep actualGPUuploads,views,production andraw vertexbytes unchanged.','revalidation':'Repeat both software Vulkan sessions andOpenGL on exact finalfixture. Earlier Vulkan passes superseded,not added to finalcounts.'},indent=2)+'\n')
p=R/'audit_side_material.py';s=p.read_text();needle=" result=(d/'water-side-material-result.txt').read_text();";s=s.replace(needle," convention=json.loads((d/'water-side-material-readback-convention.json').read_text());assert convention=={'backend':('OpenGL' if 'BACKEND_SELECTION_PASS backend=OpenGL' in c else 'Vulkan'),'flipped_raw_rows':('BACKEND_SELECTION_PASS backend=OpenGL' in c),'output_row_zero':'upper physical Y'}\n"+needle);p.write_text(s)
for n in ['audit_opengl_side.py','rehydrate_evidence.py','verify_archived_evidence.py','inspect_frames.py']:
 p=R/n;s=p.read_text().replace('vulkan-order-side-material-fragment-final','vulkan-order-side-material-normalized-final').replace('vulkan-reuse-gl-side-material-fragment-final','vulkan-reuse-gl-side-material-normalized-final');p.write_text(s)
