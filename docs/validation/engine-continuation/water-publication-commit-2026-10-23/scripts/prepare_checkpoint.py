from pathlib import Path
import json,hashlib,gzip,shutil,difflib,tarfile,io
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-publication-1529');E=R/'checkpoint';E.mkdir(exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def write(p,d):
 p=E/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n')
parents=json.loads((R/'parent-inputs.json').read_text());old=json.loads((R/'staged-parent-inputs.json').read_text());paths=[x['path']for x in old]+['src/visualValidation/java/com/shnewbs/deepprimal/validation/'+n+'.java'for n in ['ClientCapture','WaterPublicationFailureChecks','WaterRetirementBudgetChecks']]
pm={x['path']:x['sha']for x in parents};manifest=[];patch=[];delta=[]
for path in paths:
 b=(P/path).read_bytes();original=R/'original-public'/path
 if path in pm:assert original.exists()and git(original.read_bytes())==pm[path],path
 dst=E/'staged'/path;dst.parent.mkdir(parents=True,exist_ok=True);dst.write_bytes(b)
 manifest.append({'path':path,'parent_sha':pm.get(path),'candidate_sha':git(b),'sha256':sha(b)})
 patch.extend(difflib.unified_diff(original.read_text().splitlines(True)if original.exists()else [],b.decode().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
 before=next((x for x in old if x['path']==path),None)
 if before is None or before['candidate_sha']!=git(b):delta.append(path)
assert len(manifest)==17 and len(delta)==6,(len(manifest),delta)
write(Path('staged-restore-manifest.json'),manifest);(E/'staged-fix.patch').write_text(''.join(patch));write(Path('source-scope-audit.json'),{'public_inputs':len(parents),'staged_files':17,'staged_production_files':5,'staged_validation_files':12,'changes_since_prior_checkpoint':delta,'production_changes_promoted':0,'source_base':'f85fc8a565a64d1ed7789f830193f1b7aa614bba','prior_checkpoint':'91c8b9488fd617c6cc9705cce490b503fc3c6058'})
shutil.copyfile(R/'source-input-manifest.json.gz',E/'source-input-manifest.json.gz')
for n in ['strict-corner-audit.json','strict-publication-audit.json','package-audit.json','rejected-jar-observations.json','historical-replay-audit.json','identity-recovery-audit.json','more-wrapper-recovery-audit.json','graphics-restoration.json','toolchain-restoration-console.json','agent-check.txt']:
 shutil.copyfile(R/n,E/n)
scripts=['audit_corners.py','audit_publication.py','audit_package.py','run_vulkan.py','build_candidate.py','rebuild_package.py','restore_graphics.py','restore_validation_toolchain.py','recover_identity_wrappers.py','recover_more_wrappers.py','prepare_checkpoint.py']
for n in scripts:
 d=E/'scripts'/n;d.parent.mkdir(exist_ok=True);shutil.copyfile(R/n,d)
cases=['baseline-build','candidate-build','package-rebuild','vulkan-create','vulkan-publication-baseline','vulkan-publication-candidate','vulkan-publication-candidate-attempt1','vulkan-corners-publication-final','vulkan-corners-publication-final-packaged','vulkan-reuse-gl-corners-publication-final']
bundles=[]
for name in cases:
 d=B/name;raw=io.BytesIO();files=[]
 with tarfile.open(fileobj=raw,mode='w:gz',compresslevel=9)as t:
  for p in sorted(d.rglob('*')):
   if p.is_file():
    rel=str(p.relative_to(B));b=p.read_bytes();files.append({'path':rel,'bytes':len(b),'sha256':sha(b)});t.add(p,arcname=rel,recursive=False)
 b=raw.getvalue();parts=[]
 for i,at in enumerate(range(0,len(b),524288)):
  part=b[at:at+524288];path=Path('captures')/(name+'.tar.gz.part%03d'%i);out=E/path;out.parent.mkdir(exist_ok=True);out.write_bytes(part);parts.append({'path':str(path),'bytes':len(part),'sha256':sha(part)})
 bundles.append({'name':name,'bytes':len(b),'sha256':sha(b),'parts':parts,'files':files})
 # Directly reviewable compact authoritative summaries, without duplicating raw payloads.
 for n in ['result.json','candidate-build-result.json','water-publication-failure-result.txt','water-retirement-budget.json','water-corner-result.json','crash-delta.json','shutdown.txt','water-corner-pass.txt']:
  p=d/n
  if p.exists():out=E/'summaries'/name/n;out.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,out)
write(Path('capture-bundles.json'),bundles)
write(Path('screenshot-inspection.json'),{'inspected':['vulkan-corners-publication-final-packaged/screenshots/corners-scene-before.png','vulkan-reuse-gl-corners-publication-final/screenshots/reuse-gl-recovery.png'],'findings':'Both show loaded lake/shore terrain with visible submerged terraces and textured water. Single still frames cannot establish seamless long-stream motion or full enabled-view coverage. Distant horizon/LOD acceptance remains open; these views do not resolve the previously observed cutoff. Different resolutions are not a matched performance comparison.','full_scene_accepted':False})
write(Path('recovery-status.json'),{'prior_original_evidence_recovered':305,'original_missing_wrappers_recovered_exact_this_checkpoint':30,'prior_missing_originals_remaining':73,'historical_replay_is_fresh_native':False,'historical_replayed_sessions':3,'historical_missing_action_histories':3,'one_additional_old_png_archive_unserved_by_contents_api':{'git_sha':'5420a42870a8df8efb93ee0cef9ae10f3cf2e815','bytes':5281477},'new_bundles_max_part_bytes':524288,'new_build_native_environment_restored':True})
summary={'pass':True,'staged_only':True,'acceptance_complete':False,'code_changes_since_prior_checkpoint':delta,'defect':'A rejected synchronous GPU range write poisoned the accepted CPU mirror; explicit retry then skipped repair.','fix':'Prepare transfer bytes separately; commit CPU mirror and retirement cursor/deadline only after successful synchronous range submission.','baseline_cases':16,'baseline_differing_cpu_gpu_bytes':832,'candidate_cases':48,'candidate_cpu_gpu_readbacks':192,'candidate_differing_bytes':0,'corner_comparisons':15336,'corner_draws':30672,'new_core_cases':210000,'fresh_full_build_tasks':25,'gpu_retirement_each':{'pages':8,'bytes':33554304,'turns':129,'writes':136,'max_write_bytes':262080,'differing_bytes':0},'cpu_enqueue_p95_ms':[0.750307,1.182097,1.367506],'cpu_enqueue_max_ms':[46.477484,35.977936,3.777809],'performance_accepted':False,'owner_gpu_fps_accepted':False,'package_crcs':6,'package_class_identities':411,'discarded_truncated_jar':True,'installer_released':False,'scope':'Software drivers, controlled synchronous pre-backend rejection/manual retry, eight private synthetic GPU pages. No async device loss, full-cohort atomicity, automatic retry policy, normal-frame saturation/churn, full-stream/ocean or owner GPU acceptance.'}
write(Path('checkpoint-summary.json'),summary)
print(json.dumps({'files':len(list(E.rglob('*'))),'bundle_bytes':sum(x['bytes']for x in bundles),'bundle_parts':sum(len(x['parts'])for x in bundles),'delta':delta}))
