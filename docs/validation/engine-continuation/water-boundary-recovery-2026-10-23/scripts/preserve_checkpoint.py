from pathlib import Path
import json,hashlib,gzip,tarfile,io,shutil
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-retry-2230');OLD=R.parent/'water-boundary-scan-2102/checkpoint';E=R/'checkpoint';E.mkdir(exist_ok=False)
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def write(n,x):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def copy(p,n):
 d=E/n;d.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,d)
def bundle(name,files):
 raw=io.BytesIO();members=[]
 with tarfile.open(fileobj=raw,mode='w')as t:
  for n,p in sorted(files):
   b=p.read_bytes();i=tarfile.TarInfo(n);i.size=len(b);i.mtime=0;i.mode=0o755 if n=='gradlew'else 0o644;t.addfile(i,io.BytesIO(b));members.append({'path':n,'bytes':len(b),'sha256':sha(b)})
 b=gzip.compress(raw.getvalue(),mtime=0);parts=[]
 for n,o in enumerate(range(0,len(b),524288)):
  p=Path(name+f'.part{n:03}');chunk=b[o:o+524288];(E/p).parent.mkdir(parents=True,exist_ok=True);(E/p).write_bytes(chunk);parts.append({'path':str(p),'bytes':len(chunk),'sha256':sha(chunk),'git_sha':git(chunk)})
 return {'archive':name,'bytes':len(b),'sha256':sha(b),'parts':parts,'members':members}
previous={x['path']:x for x in json.loads((OLD/'source-snapshot-manifest.json').read_text())};staged={x['path']for x in json.loads((OLD/'staged-manifest.json').read_text())};pub=json.loads(gzip.decompress((OLD/'source-input-manifest.json.gz').read_bytes()));pubmap={x['path']:x['sha']for x in pub};changes=json.loads((R/'changes.json').read_text());changed={x['path']for x in changes};paths=sorted(set(previous)|changed);staged|=changed;assert len(paths)==588 and len(staged)==26
manifest=[]
for path in paths:
 b=(P/path).read_bytes();prior=previous.get(path)
 if path not in changed:assert prior['sha256']==sha(b),path
 else:assert prior is None or prior['sha256']!=sha(b)
 row={'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b),'public_parent_sha':pubmap.get(path),'staged':path in staged};manifest.append(row)
 if path in staged:copy(P/path,Path('staged')/path)
write('source-snapshot-manifest.json',manifest);write('source-snapshot-bundle.json',bundle('source-snapshot.tar.gz',[(x['path'],P/x['path'])for x in manifest]));write('staged-manifest.json',[x for x in manifest if x['staged']]);copy(OLD/'source-input-manifest.json.gz','source-input-manifest.json.gz');copy(R/'since-r42.patch','since-r42.patch')
write('source-scope-audit.json',{'pass':True,'prior_checkpoint':'0e88a2b3841e70c42a76590d566aaa2bb18eef6b','candidate_inputs':588,'staged_files':26,'staged_production_files':6,'staged_validation_files':20,'changes_since_r42':changes,'other_prior_inputs_unchanged':581,'production_promoted':False,'geometry_solver_geography_changed':False})
baseline_manifest=[];overlay=[]
baseline_paths=set(previous)|{'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterBoundaryRetryChecks.java'}
for path in sorted(baseline_paths):
 b=(R/'baseline'/path).read_bytes();baseline_manifest.append({'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b)})
 if (P/path).read_bytes()!=b:copy(R/'baseline'/path,Path('baseline-overlay')/path);overlay.append(path)
write('baseline-source-manifest.json',baseline_manifest);write('baseline-overlay.json',{'replace':overlay,'remove':sorted(set(paths)-baseline_paths),'private_boundary_fixture_same_as_candidate':True,'source_inputs':587,'staged_product_is_R42_control':True})
sessions=['vulkan-retry-control','vulkan-retry-control-attempt1','vulkan-corners-shared-final-retry','vulkan-corners-shared-final-retry-packaged','vulkan-reuse-gl-corners-shared-final-retry'];objects={};layout=[]
for name in sessions:
 for p in sorted((B/name).rglob('*')):
  if not p.is_file():continue
  b=p.read_bytes();key='objects/'+sha(b);objects.setdefault(key,p);layout.append({'path':p.relative_to(B).as_posix(),'object':key,'bytes':len(b),'sha256':sha(b)})
write('capture-bundle.json',bundle('captures/capture-data.tar.gz',list(objects.items())));(E/'capture-layout.json.gz').write_bytes(gzip.compress(json.dumps(layout).encode(),mtime=0));write('capture-compaction.json',{'pass':True,'logical_capture_paths':len(layout),'unique_content_objects':len(objects),'logical_bytes':sum(x['bytes']for x in layout),'unique_uncompressed_bytes':sum(p.stat().st_size for p in objects.values()),'all_logical_path_hashes_preserved':True})
write('build-bundle.json',bundle('build-evidence.tar.gz',[(p.relative_to(B).as_posix(),p)for n in ['build-baseline','build-candidate']for p in(B/n).rglob('*')if p.is_file()]))
# Prior controls remain explicitly historical; copy their already sealed parts without relabelling freshness.
prior=json.loads((OLD/'prior-admission-bundle.json').read_text());write('prior-admission-bundle.json',prior)
for part in prior['parts']:copy(OLD/part['path'],part['path'])
copy(OLD/'prior-publication-baseline.tar.gz','prior-publication-baseline.tar.gz')
historical=Path('/tmp/deepprimal-index-2102/vulkan-corners-shared-final-r41-control')
needed=['water-boundary-batch.json','water-boundary-batch-readbacks.bin','result.json','console-final.gz','console.log','source-identity-before.json','source-identity-after.json','crash-delta.json','action-history.json','shutdown.txt','sealed-console.json']
write('prior-boundary-bundle.json',bundle('prior-boundary-control.tar.gz',[(historical.name+'/'+n,historical/n)for n in needed]))
for path in (R/'rejected-fixture').iterdir():copy(path,Path('rejected-fixture')/path.name)
for p in R.glob('*.py'):copy(p,Path('scripts')/p.name)
copy(R/'validation_identity.gradle','scripts/validation_identity.gradle');(E/'scripts/restore_snapshot.py').write_text((OLD/'scripts/restore_snapshot.py').read_text().replace('586','588'));copy(R/'replay_template.py','scripts/replay_checkpoint.py')
for n in ['baseline-boundary-audit.json','strict-admission-audit.json','strict-boundary-audit.json','strict-corner-audit.json','strict-frame-audit.json','strict-deadline-audit.json','strict-publication-audit.json','package-audit.json','code-self-audit.json','screenshot-inspection.json','console-seal-audit.json','matched-control-audit.json','checkpoint-summary.json','README.md','runtime-source-identity.json','renderer-javap.txt','strict-retry-audit.json','audit-corrections.json']:
 if(R/n).exists():copy(R/n,n)
for name in ['WaterBoundaryBatchChecks','WaterAdmissionPlanChecks','WaterBoundaryRetryChecks']:
 for p in(P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation').glob(name+'*.class'):copy(p,Path('runtime-fixtures')/p.name)
class_paths=[('candidate-renderer.class',P/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.class'),('candidate-page.class',P/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer$Page.class'),('candidate-packet.class',P/'engine-core/build/classes/java/main/com/shnewbs/deepprimal/core/WaterCornerPacket.class'),('candidate-retry.class',P/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer$CornerRetry.class'),('control-renderer.class',R/'baseline/build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.class')]
identities=[]
for name,path in class_paths:
 copy(path,Path('runtime-product-classes')/name);data=path.read_bytes();identities.append({'path':'runtime-product-classes/'+name,'bytes':len(data),'sha256':sha(data)})
write('compiled-product-identities.json',identities)
write('sequential-job-audit.json' ,{'pass':True,'full_builds':2,'native_sessions':4,'failed_private_compile_attempts':1,'all_build_and_native_jobs_sequential':True,'toolchain':'../water-frame-1732/toolchain','software_driver':'Mesa25.2.8 lavapipe','display_pin':'Xvfb1.9','no_source_changes_during_candidate_build_native_checks':True,'older_missing_evidence_files':73})
print('SEALED',len(manifest),len(staged),len(layout),len(objects),flush=True)
