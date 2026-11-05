from pathlib import Path
import json,hashlib,gzip,tarfile,io,shutil,difflib,sys
R=Path(__file__).parent; P=R/'project'; B=Path('/tmp/deepprimal-frame-1732'); E=R/'checkpoint'; E.mkdir(exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def write(p,x):
 p=E/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def cp(p,d):
 d=E/d;d.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,d)
def bundle(name,entries):
 raw=io.BytesIO();members=[]
 with tarfile.open(fileobj=raw,mode='w')as t:
  for n,p in sorted(entries):
   b=p.read_bytes();i=tarfile.TarInfo(n);i.size=len(b);i.mode=0o755 if n.endswith('/gradlew')else 0o644;i.mtime=0;t.addfile(i,io.BytesIO(b));members.append({'path':n,'bytes':len(b),'sha256':sha(b)})
 b=gzip.compress(raw.getvalue(),mtime=0);parts=[]
 for n,o in enumerate(range(0,len(b),524288)):
  p=Path(name+f'.part{n:03}');chunk=b[o:o+524288];(E/p).parent.mkdir(parents=True,exist_ok=True);(E/p).write_bytes(chunk);parts.append({'path':str(p),'bytes':len(chunk),'sha256':sha(chunk),'git_sha':git(chunk)})
 return {'archive':name,'bytes':len(b),'sha256':sha(b),'parts':parts,'members':members}
base=json.loads(gzip.decompress((R/'source-input-manifest.json.gz').read_bytes()));old=json.loads((R/'staged-restore-manifest.json').read_text());staged_paths={x['path']for x in old}
staged_paths.update('src/visualValidation/java/com/shnewbs/deepprimal/validation/'+n+'.java'for n in ['WaterFrameRetirementChecks','WaterExtractionDeadlineChecks'])
assert len(base)==575 and len(staged_paths)==19
base_map={x['path']:x['sha']for x in base};paths=sorted(set(base_map)|staged_paths);assert len(paths)==581
manifest=[]
for p in paths:
 b=(P/p).read_bytes();g=git(b)
 if p not in staged_paths:assert g==base_map[p],p
 manifest.append({'path':p,'bytes':len(b),'sha256':sha(b),'git_sha':g,'public_parent_sha':base_map.get(p),'staged':p in staged_paths})
write(Path('source-snapshot-manifest.json'),manifest)
source=bundle('source-snapshot.tar.gz',[(x['path'],P/x['path'])for x in manifest]);write(Path('source-snapshot-bundle.json'),source)
staged=[]
for x in manifest:
 if x['staged']:cp(P/x['path'],Path('staged')/x['path']);staged.append(x)
write(Path('staged-manifest.json'),staged)
changes=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFrameRetirementChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterExtractionDeadlineChecks.java']
patch='';change_meta=[]
for p in changes:
 prior=R/'staged-r37'/p
 if not prior.exists():prior=R/'staged-r36'/p
 before=prior.read_bytes()if prior.exists()else b'';after=(P/p).read_bytes();assert before!=after
 patch+=''.join(difflib.unified_diff(before.decode().splitlines(True),after.decode().splitlines(True),fromfile='a/'+p if before else '/dev/null',tofile='b/'+p))
 change_meta.append({'path':p,'r37_staged_sha256':sha(before)if before else None,'candidate_sha256':sha(after),'production':p in changes[:1]})
(E/'since-r37.patch').write_text(patch);write(Path('source-scope-audit.json'),{'pass':True,'public_inputs':575,'public_unmodified_except_explicit_staging':True,'snapshot_inputs':581,'staged_files':19,'staged_production_files':5,'staged_validation_files':14,'changed_since_r37':change_meta,'production_promoted':False,'geography_or_solver_changes':False})
sessions=['vulkan-create','vulkan-frame-retire-steady','vulkan-frame-retire-active','vulkan-deadline-baseline','vulkan-corners-shared-final','vulkan-corners-shared-final-packaged','vulkan-reuse-gl-corners-shared-final','vulkan-frame-retire-steady-final']
bundles=[]
for s in sessions:
 files=[(p.relative_to(B).as_posix(),p)for p in(B/s).rglob('*')if p.is_file()]
 bundles.append(bundle('captures/'+s+'.tar.gz',files));print('sealed',s,len(files),bundles[-1]['bytes'],flush=True)
(E/'capture-bundles.json.gz').write_bytes(gzip.compress(json.dumps(bundles).encode(),mtime=0))
builds=bundle('build-evidence.tar.gz',[(p.relative_to(B).as_posix(),p)for n in ['build','build-retry','candidate-build']for p in(B/n).rglob('*')if p.is_file()]);write(Path('build-bundle.json'),builds)
failed=bundle('rejected-generated-archive.tar.gz',[(p.name,p)for p in(R/'rejected-decompile').iterdir()if p.is_file()]);write(Path('rejected-generated-archive-bundle.json'),failed)
for p in R.glob('*.py'):cp(p,Path('scripts')/p.name)
for n in ['strict-corner-audit.json','strict-frame-audit.json','strict-deadline-audit.json','strict-publication-audit.json','frame-steady-audit.json','frame-active-audit.json','frame-steady-final-audit.json','package-audit.json','restoration-audit.json','staged-overlay-manifest.json','staged-restore-manifest.json','graphics-pins-source.json','graphics-pins-current.json','graphics-restoration.json','toolchain-restoration-console.json','generated-archive-failure.json','agent-check.txt','graphics-live-console2.txt','source-input-manifest.json.gz','prior-publication-baseline.tar.gz']:
 if(R/n).exists():cp(R/n,Path(n))
for p in(R/'toolchain').glob('restoration.json'):cp(p,Path('toolchain-restoration.json'))
for n in ['WaterFrameRetirementChecks','WaterExtractionDeadlineChecks']:
 for p in(P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation').glob(n+'*.class'):cp(p,Path('runtime-fixtures')/p.name)
old_frame=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFrameRetirementChecks.java'
before=old_frame.read_text().replace('    result.put("windowFocused", mc.isWindowActive()); result.put("effectiveFpsLimit", mc.getFramerateLimitTracker().getFramerateLimit());\n','')
(E/'initial-frame-fixture-source.java').write_text(before)
write(Path('fixture-variants.json'),{'initial_ordinary_frame_runtime_class_sha256':'9fbece','initial_variant_description':'Initial frame runs preceded addition of windowFocused/effectiveFpsLimit metadata. Source reconstructed by removing that single line; original compiled bytes not retained. Full runtime hash is in each capture JSON.','current_class_sha256':sha((E/'runtime-fixtures/WaterFrameRetirementChecks.class').read_bytes()),'deadline_class_sha256':sha((E/'runtime-fixtures/WaterExtractionDeadlineChecks.class').read_bytes())})
write(Path('screenshot-inspection.json'),{'inspected':['vulkan-corners-shared-final/screenshots/corners-scene-before.png','vulkan-frame-retire-steady-final/screenshots/frame-retire-after.png','vulkan-reuse-gl-corners-shared-final/screenshots/reuse-gl-recovery.png'],'observations':['Lake scene renders in Vulkan and GL controls without a crash screen.','Distant terrain remains visibly cut off; LOD fixture has zero cached/visible tiles, so no LOD coverage acceptance.','Static captures cannot establish absence of temporal flicker; numerical controlled corner/transition oracles are separate.'],'full_scene_accepted':False})
print('CHECKPOINT_SEALED',len(paths),len(staged),sum(x['bytes']for x in bundles),flush=True)
