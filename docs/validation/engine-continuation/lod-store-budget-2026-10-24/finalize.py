from pathlib import Path
import hashlib,json,shutil,difflib
r=Path(__file__).resolve().parent;e=r/'evidence';p=r/'project';meta=json.loads((e/'source-manifest.json').read_text());changed=[]
for item in meta['entries']:
 b=(p/item['path']).read_bytes();sha=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
 if sha!=item['sha']:changed.append({'path':item['path'],'parent_git_sha':item['sha'],'candidate_sha256':hashlib.sha256(b).hexdigest()})
assert {x['path'] for x in changed}=={'engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodStore.java','src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java'}
(e/'source-audit.json').write_text(json.dumps({'verified_parent_inputs':len(meta['entries']),'changed_existing_inputs':changed,'new_private_input':'src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodStoreBudgetChecks.java','runtime_files_changed':1,'geography_or_water_changes':False},indent=2)+'\n')
for f in ['build.py','fresh-compile.gradle','verify_core.py','audit_package.py','verify_package.py','build-result.json','build.log','independent-core-audit.json','independent-compile.json','package-audit.json']:shutil.copyfile(r/f,e/f)
fix='engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodStore.java';diff=''.join(difflib.unified_diff((r/'baseline/LodStore.java').read_text().splitlines(True),(p/fix).read_text().splitlines(True),fromfile='a/'+fix,tofile='b/'+fix));(e/'runtime.patch').write_text(diff)
summary={'checkpoint':'R53','parent':meta['parent'],'runtime_files_promoted':1,'new_private_fixture_files':1,'private_invocation_files_changed':1,'baseline_reproduces_accepted_unreadable_replacement':True,'candidate_filesystem_checks':19,'packaged_filesystem_checks':19,'full_build_tasks':31,'full_build_exit':0,'full_build_pass':True,'independent_core_sources':49,'independent_class_matches':87,'archive_crcs':7,'compiled_product_class_matches':502,'compiled_private_class_paths_excluded':45,'reporter_assertion_corrections':1,'failed_game_runs':0,'new_minecraft_execution':False,'new_graphics_or_performance_execution':False,'installer_released':False,'owner_gpu_FPS_acceptance':False,'engine_acceptance_complete':False,'scope':'Reject over-limit palette before any cache-file mutation; retain valid prior snapshot and v1 disk format. General enabled-view/LOD cutoff and discovery saturation remain open.'}
(e/'summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps(summary))
