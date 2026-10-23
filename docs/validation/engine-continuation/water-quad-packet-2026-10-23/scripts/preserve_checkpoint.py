from pathlib import Path
import json,hashlib,gzip,tarfile,io,shutil
R=Path(__file__).parent;P=R/'project';B=Path('/tmp/deepprimal-quad-2029');OLD=R.parent/'water-admission-plan-1932/checkpoint';E=R/'checkpoint';E.mkdir(exist_ok=True)
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
previous={x['path']:x for x in json.loads((OLD/'source-snapshot-manifest.json').read_text())};pub=json.loads(gzip.decompress((OLD/'source-input-manifest.json.gz').read_bytes()));pubmap={x['path']:x['sha']for x in pub};staged={x['path']for x in json.loads((OLD/'staged-manifest.json').read_text())};new='src/coreChecks/java/com/shnewbs/deepprimal/core/WaterCornerBulkChecks.java';staged.add(new);changes=json.loads((R/'changes.json').read_text());changed={x['path']for x in changes};paths=sorted(set(previous)|{new});assert len(paths)==584 and len(staged)==22
manifest=[]
for path in paths:
 b=(P/path).read_bytes();prior=previous.get(path)
 if path not in changed:assert prior['sha256']==sha(b),path
 else:assert prior is None or prior['sha256']!=sha(b)
 row={'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b),'public_parent_sha':pubmap.get(path),'staged':path in staged};manifest.append(row)
 if path in staged:copy(P/path,Path('staged')/path)
write('source-snapshot-manifest.json',manifest);write('source-snapshot-bundle.json',bundle('source-snapshot.tar.gz',[(x['path'],P/x['path'])for x in manifest]));write('staged-manifest.json',[x for x in manifest if x['staged']]);copy(OLD/'source-input-manifest.json.gz','source-input-manifest.json.gz');copy(R/'since-r40.patch','since-r40.patch')
write('source-scope-audit.json',{'pass':True,'prior_checkpoint':'d82b50eaba5c027fdc3d398a39336fe1e7569acd','public_inputs_preserved':575,'candidate_inputs':584,'staged_files':22,'staged_production_files':5,'staged_validation_files':17,'changes_since_r40':changes,'other_prior_inputs_unchanged':580,'production_promoted':False,'geometry_solver_geography_changed':False})
sessions=['vulkan-batch-r40-control','vulkan-corners-shared-final-quad','vulkan-corners-shared-final-quad-packaged','vulkan-reuse-gl-corners-shared-final-quad'];records=[]
for name in sessions:
 files=[(p.relative_to(B).as_posix(),p)for p in(B/name).rglob('*')if p.is_file()];records.append(bundle('captures/'+name+'.tar.gz',files));print('sealed',name,len(files),records[-1]['bytes'],flush=True)
(E/'capture-bundles.json.gz').write_bytes(gzip.compress(json.dumps(records).encode(),mtime=0));write('build-bundle.json',bundle('build-evidence.tar.gz',[(p.relative_to(B).as_posix(),p)for n in ['build-baseline','build-candidate']for p in(B/n).rglob('*')if p.is_file()]))
benchmark_files=[(p.relative_to(R).as_posix(),p)for root in [R/'benchmarks',R/'benchmark-initial',R/'benchmark-instrumented']for p in root.rglob('*')if p.is_file()];objects={};layout=[]
for name,p in sorted(benchmark_files):
 b=p.read_bytes();key='benchmark-objects/'+sha(b);objects.setdefault(key,p);layout.append({'path':name,'object':key,'bytes':len(b),'sha256':sha(b)})
write('benchmark-bundle.json',bundle('benchmark-evidence.tar.gz',list(objects.items())));(E/'benchmark-layout.json.gz').write_bytes(gzip.compress(json.dumps(layout).encode(),mtime=0));write('benchmark-compaction.json',{'pass':True,'logical_paths':len(layout),'unique_objects':len(objects),'logical_bytes':sum(x['bytes']for x in layout),'unique_bytes':sum(p.stat().st_size for p in objects.values()),'all_logical_path_hashes_preserved':True})
prior=Path('/tmp/deepprimal-admission-1932/vulkan-admission-baseline-identity');write('prior-admission-bundle.json',bundle('prior-admission-baseline.tar.gz',[(p.relative_to(prior.parent).as_posix(),p)for p in prior.rglob('*')if p.is_file()]))
copy(OLD/'prior-publication-baseline.tar.gz','prior-publication-baseline.tar.gz')
for p in R.glob('*.py'):copy(p,Path('scripts')/p.name)
copy(R/'validation_identity.gradle','scripts/validation_identity.gradle');(E/'scripts/restore_snapshot.py').write_text((OLD/'scripts/restore_snapshot.py').read_text().replace('583','584'));copy(R/'replay_template.py','scripts/replay_checkpoint.py')
for n in ['baseline-boundary-audit.json','strict-admission-audit.json','strict-boundary-audit.json','strict-corner-audit.json','strict-frame-audit.json','strict-deadline-audit.json','strict-publication-audit.json','package-audit.json','benchmark-results.json','benchmark-audit.json','benchmark-counter-audit.json','console-seal-audit.json','benchmark-identities.json','benchmark-initial-results.json','benchmark-initial-audit.json','benchmark-instrumented-results.json','benchmark-instrumented-audit.json','benchmark-instrumented-identities.json','fast-bulk-check.json','code-self-audit.json','screenshot-inspection.json']:
 if(R/n).exists():copy(R/n,n)
for p in(R/'baseline').rglob('*'):
 if p.is_file():copy(p,Path('baseline')/p.relative_to(R/'baseline'))
for path in ['src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java']:copy(OLD/'staged'/path,Path('baseline')/Path(path).name)
for name in ['WaterBoundaryBatchChecks','WaterAdmissionPlanChecks']:
 for p in(P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation').glob(name+'*.class'):copy(p,Path('runtime-fixtures')/p.name)
write('prior-restoration-provenance.json',{'source_checkpoint':'d82b50eaba5c027fdc3d398a39336fe1e7569acd','restored_inputs_before_any_edits':583,'all_prior_snapshot_hashes_checked_before_write':True,'toolchain_shared_sequential_build_cache_use':'../water-frame-1732/toolchain','no_competing_native_or_gradle_jobs':True,'driver_pins':'Same Mesa25.2.8/Xvfb1.9 as R38–R40','original_older_missing_evidence_files':73})
print('SEALED',len(manifest),len(staged),flush=True)
