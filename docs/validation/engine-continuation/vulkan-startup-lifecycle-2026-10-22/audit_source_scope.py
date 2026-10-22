from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';inputs=json.loads((R/'input-verification.json').read_text());changed=[];same=0
for row in inputs['files']:
 f=P/row['path'];b=f.read_bytes();sha=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
 if sha==row['sha']:same+=1
 else:changed.append({'path':row['path'],'before_blob':row['sha'],'after_blob':sha,'sha256':hashlib.sha256(b).hexdigest()})
assert {x['path'] for x in changed}=={'renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/Startup.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WorldCreationChecks.java'}
prod=[x for x in changed if '/src/main/' in x['path']];assert len(prod)==1
b=(P/prod[0]['path']).read_bytes();assert b'STARTUP_TRACE' not in b and b'complete = false;' in b
new=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/StartupLifecycleChecks.java'
checks={'WaterRuntime':'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java','WaterTransport':'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java','VkCommandEncoder':'renderer-vulkan/src/main/java/net/vulkanmod/render/engine/VkCommandEncoder.java','VoxelLodRenderer':'lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java'}
package=json.loads((R/'vulkan-build-audit.json').read_text());assert package['changed_source_sha256']=={x['path']:x['sha256'] for x in prod}
model=json.loads((R/'startup-source-extracted.json').read_text());assert all(x['source_sha256']==prod[0]['sha256'] for x in model if x['variant']=='after')
result={'source_commit':inputs['source_commit'],'original_build_inputs':len(inputs['files']),'unchanged':same,'changed':changed,'new_validation_source':{'path':str(new.relative_to(P)),'sha256':hashlib.sha256(new.read_bytes()).hexdigest()},'production_changes':1,'temporary_trace_removed':True,'prior_fix_and_water_hashes':{name:hashlib.sha256((P/path).read_bytes()).hexdigest() for name,path in checks.items()},'water_quantity_player_edit_geography_code_unchanged':True,'performance_scope':'One completion reset per arm; no new frame-loop logic/I/O or measured FPS claim','ordinary_marker_reappearance_cause':'Not established: trace shows successful deletion then later presence without another owner arm before the explicit test.'}
(R/'source-scope-audit.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
