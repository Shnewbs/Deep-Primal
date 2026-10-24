from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';prefix='docs/validation/engine-continuation/water-boundary-packet-2026-10-23/';rows=json.loads((R/'r35-artifact-index.json').read_text());expected={r['path']:r for r in rows}
def sha(b):return hashlib.sha256(b).hexdigest()
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
paths=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
renderer=(P/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.class').read_bytes();ident={p:sha((P/p).read_bytes())for p in paths};before=ident|{'expected_corner_history_field':True,'expected_renderer_class_sha256':sha(renderer)}
provenance={'renderer_class_sha256':sha(renderer),'corner_history_field_present':True,'resident_presentation_mirror_present':True,'animated_water.vsh_sha256':ident[paths[1]],'animated_water.fsh_sha256':ident[paths[2]]}
report=[]
for case in ['vulkan-corners-marker-final','vulkan-corners-marker-final-packaged','vulkan-reuse-gl-corners-marker-final']:
 for name,data in [('source-identity-before.json',before),('source-identity-after.json',ident),('water-corner-runtime-provenance.json',provenance)]:
  path=prefix+case+'/'+name;b=(json.dumps(data,indent=2)+'\n').encode();match=gs(b)==expected[path]['sha'];report.append({'path':path,'original_hash_matched':match,'sha':gs(b)})
  if match:p=R/'recovered-original'/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
(R/'identity-recovery-audit.json').write_text(json.dumps({'records':report,'originals_recovered':sum(r['original_hash_matched']for r in report),'compiled_renderer_sha256':sha(renderer),'scope':'Fresh compilation/source hashes reconstruct original wrappers only when original Git hash matches; no historical native execution relabeled'},indent=2)+'\n');print(json.dumps(report,indent=2))
