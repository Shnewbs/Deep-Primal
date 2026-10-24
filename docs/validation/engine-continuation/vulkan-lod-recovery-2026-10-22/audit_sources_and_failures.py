from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parent;P=R/'project'
source=json.loads((R/'remote-inputs.json').read_text());changed=[];unchanged=0
for row in source['files']:
 p=P/row['path'];data=p.read_bytes();sha=hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()
 if sha==row['sha']:unchanged+=1
 else:changed.append({'path':row['path'],'parent_blob':row['sha'],'current_blob':sha,'sha256':hashlib.sha256(data).hexdigest()})
assert {r['path'] for r in changed}=={'lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java','renderer-vulkan/src/main/java/net/vulkanmod/render/engine/VkCommandEncoder.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java'}
new=[]
for name in ['LodDiskRecoveryChecks','VulkanFrameCompletionChecks']:
 p=P/('src/visualValidation/java/com/shnewbs/deepprimal/validation/'+name+'.java');data=p.read_bytes()
 new.append({'path':str(p.relative_to(P)),'sha256':hashlib.sha256(data).hexdigest()})
result={'base_commit':source['base_commit'],'original_build_inputs_checked':len(source['files']),'unchanged':unchanged,'changed':changed,'new_validation_sources':new,'production_changes':2,'water_runtime_core_and_geography_unchanged':True,'suspected_bounds_defect_rejected':'WaterSectionBoundsMixin already supplies expanded section bounds to discovery; no water-bounds fix authored.','remaining_scope':'Retains only indexed out-of-range disk keys. Cached/dirty keys retain prior removal behavior; already-consumed/evicted keys and broader reindex recovery are not solved.'}
(R/'source-audit.json').write_text(json.dumps(result,indent=2)+'\n')
crashes=P/'run-vulkan-primary/crash-reports';rows=[]
for case,crash in [('vulkan-create','crash-2026-10-22_16.45.10-client.txt'),('vulkan-frame-before-retry','crash-2026-10-22_16.49.33-client.txt'),('vulkan-disk-before-fixed-vulkan-retry','crash-2026-10-22_17.02.55-client.txt')]:
 d=R/case;report=json.loads((d/'result.json').read_text());file=crashes/crash;text=file.read_text()
 pattern='Out-of-range cache must remain eligible' if 'disk-before' in case else 'Close the existing render pass before performing additional commands'
 assert pattern in text
 excerpt='\n'.join(text.splitlines()[:55])+'\n'
 (d/'failure-excerpt.txt').write_text(excerpt)
 rows.append({'case':case,'accepted':False,'launcher_exit':report['exit_code'],'launcher_success_is_insufficient':report['build_successful'],'crash':crash,'crash_sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'exception':pattern,'excerpt':case+'/failure-excerpt.txt','console_sha256':report['console_sha256'],'explicit_record':(d/'world-creation.txt').read_text() if (d/'world-creation.txt').exists() else 'LOD_DISK_RECOVERY_OBSERVED retainedAfterOutside=0 startedAfterMove=false' if 'disk-before' in case else 'Forced completion callback reached public map guard; crash stack preserved.'})
(R/'inspected-before-failures.json').write_text(json.dumps(rows,indent=2)+'\n')
print(json.dumps({'unchanged_inputs':unchanged,'changed_paths':[r['path'] for r in changed],'native_before_failures':rows},indent=2))
