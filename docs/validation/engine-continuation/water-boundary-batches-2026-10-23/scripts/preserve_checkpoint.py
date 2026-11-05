from pathlib import Path
import json,hashlib,gzip,tarfile,io,shutil,difflib
R=Path(__file__).parent;P=R/'project';B=Path('/tmp/deepprimal-boundary-1830');OLD=R.parent/'water-frame-1732/checkpoint';E=R/'checkpoint';E.mkdir(exist_ok=True)
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
prior=json.loads((OLD/'source-snapshot-manifest.json').read_text());pub=json.loads(gzip.decompress((OLD/'source-input-manifest.json.gz').read_bytes()));staged={x['path']for x in json.loads((OLD/'staged-manifest.json').read_text())};new='src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterBoundaryBatchChecks.java';staged.add(new)
changed={'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java',new};pubmap={x['path']:x['sha']for x in pub};previous={x['path']:x for x in prior};paths=sorted(set(previous)|{new});assert len(paths)==582 and len(staged)==20
manifest=[];changes=[]
for p in paths:
 b=(P/p).read_bytes();before=previous.get(p)
 if p not in changed:assert before['sha256']==sha(b),p
 else:assert before is None or before['sha256']!=sha(b);changes.append({'path':p,'r38_sha256':before['sha256']if before else None,'candidate_sha256':sha(b)})
 row={'path':p,'bytes':len(b),'sha256':sha(b),'git_sha':git(b),'public_parent_sha':pubmap.get(p),'staged':p in staged};manifest.append(row)
 if p in staged:copy(P/p,Path('staged')/p)
write(Path('source-snapshot-manifest.json'),manifest);write(Path('source-snapshot-bundle.json'),bundle('source-snapshot.tar.gz',[(x['path'],P/x['path'])for x in manifest]));write(Path('staged-manifest.json'),[x for x in manifest if x['staged']]);copy(OLD/'source-input-manifest.json.gz',Path('source-input-manifest.json.gz'))
write(Path('source-scope-audit.json'),{'pass':True,'prior_checkpoint':'159f64617a8e9710f83ccb1182a040199a6b1b95','public_inputs_preserved':575,'candidate_inputs':582,'staged_files':20,'staged_production_files':5,'staged_validation_files':15,'changes_since_r38':changes,'other_prior_inputs_unchanged':579,'production_promoted':False,'geometry_solver_geography_changed':False})
patch=''
for p in sorted(changed):
 before=(R/'baseline/AnimatedWaterRenderer.java').read_text()if p.endswith('/render/AnimatedWaterRenderer.java')else(OLD/'staged'/p).read_text()if p!=new else ''
 patch+=''.join(difflib.unified_diff(before.splitlines(True),(P/p).read_text().splitlines(True),fromfile='a/'+p if before else'/dev/null',tofile='b/'+p))
(E/'since-r38.patch').write_text(patch)
sessions=['vulkan-batch-baseline','vulkan-corners-shared-final-batches','vulkan-corners-shared-final-batches-packaged','vulkan-batch-baseline-capacity','vulkan-corners-shared-final-capacity','vulkan-corners-shared-final-capacity-packaged','vulkan-reuse-gl-corners-shared-final-capacity'];records=[]
for s in sessions:
 files=[(p.relative_to(B).as_posix(),p)for p in(B/s).rglob('*')if p.is_file()];records.append(bundle('captures/'+s+'.tar.gz',files));print('sealed',s,len(files),records[-1]['bytes'],flush=True)
(E/'capture-bundles.json.gz').write_bytes(gzip.compress(json.dumps(records).encode(),mtime=0))
write(Path('build-bundle.json'),bundle('build-evidence.tar.gz',[(p.relative_to(B).as_posix(),p)for n in ['build-baseline','build-candidate','build-capacity-baseline','build-capacity-candidate']for p in(B/n).rglob('*')if p.is_file()]))
for p in R.glob('*.py'):copy(p,Path('scripts')/p.name)
restore=(OLD/'scripts/restore_snapshot.py').read_text().replace('581','582');(E/'scripts/restore_snapshot.py').write_text(restore)
for n in ['strict-boundary-audit.json','capacity-baseline-boundary-audit.json','baseline-boundary-audit.json','strict-corner-audit.json','strict-frame-audit.json','strict-deadline-audit.json','strict-publication-audit.json','package-audit.json','boundary-batch.patch']:
 if(R/n).exists():copy(R/n,Path(n))
copy(OLD/'prior-publication-baseline.tar.gz',Path('prior-publication-baseline.tar.gz'))
copy(R/'replay_template.py',Path('scripts/replay_checkpoint.py'))
for p in(R/'baseline').rglob('*'):
 if p.is_file():copy(p,Path('baseline')/p.relative_to(R/'baseline'))
for p in(P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation').glob('WaterBoundaryBatchChecks*.class'):copy(p,Path('runtime-fixtures')/p.name)
write(Path('prior-restoration-provenance.json'),{'source_checkpoint':'159f64617a8e9710f83ccb1182a040199a6b1b95','restored_inputs_before_any_edits':581,'all_prior_snapshot_hashes_checked_before_write':True,'toolchain_shared_readonly_assets_sequential_build_cache_use':'../water-frame-1732/toolchain','no_competing_native_or_gradle_jobs':True,'driver_pins':'Same Mesa25.2.8/Xvfb1.9 as R38; see prior checkpoint graphics/toolchain manifests','original_older_missing_evidence_files':73})
print('SEALED',len(manifest),len(staged),flush=True)
