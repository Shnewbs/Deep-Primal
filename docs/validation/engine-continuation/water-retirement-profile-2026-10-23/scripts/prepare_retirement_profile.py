from pathlib import Path
import json,hashlib,gzip,shutil,difflib,tarfile,io
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-retire-profile-1618');E=R/'profile-checkpoint';E.mkdir(exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def write(p,d):
 p=E/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n')
prior=json.loads((R/'checkpoint/staged-restore-manifest.json').read_text());pm={r['path']:r for r in prior};changed=[];patch=[]
for r in prior:
 b=(P/r['path']).read_bytes()
 if git(b)!=r['candidate_sha']:changed.append(r['path'])
assert set(changed)=={'src/visualValidation/java/com/shnewbs/deepprimal/validation/'+n+'.java'for n in ['ClientCapture','WaterRetirementBudgetChecks']}
overlay=[]
for path in changed:
 b=(P/path).read_bytes();out=E/'staged'/path;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);overlay.append({'path':path,'parent_sha':pm[path]['candidate_sha'],'candidate_sha':git(b),'sha256':sha(b)})
 original=(R/'before-cap-profile'/Path(path).name).read_text();patch.extend(difflib.unified_diff(original.splitlines(True),b.decode().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
(E/'fixture-cleanup.patch').write_text(''.join(patch));write(Path('staged-overlay-manifest.json'),overlay)
write(Path('source-scope-audit.json'),{'pass':True,'changed_since_publication_checkpoint':changed,'production_files_changed_since_publication_checkpoint':0,'full_candidate_staged_files':17,'full_candidate_production_files':5,'full_candidate_validation_files':12,'public_source_promoted':False,'prior_source_evidence':'../water-publication-commit-2026-10-23','all_other_staged_source_hashes_unchanged':True})
for n in ['audit_retirement_profile.py','run_retirement_profile.py','profile_retirement_fixture.py','profile_staging_occupancy.py','prepare_retirement_profile.py']:
 out=E/'scripts'/n;out.parent.mkdir(exist_ok=True);shutil.copyfile(R/n,out)
for n in ['WaterRetirementBudgetChecks-profile-v1.java','WaterRetirementBudgetChecks-profile-v2.java','WaterRetirementBudgetChecks-profile-v3.java']:
 out=E/'diagnostic-variants'/n;out.parent.mkdir(exist_ok=True);shutil.copyfile(R/'before-cap-profile'/n,out)
for n in ['strict-retirement-profile-audit.json','retirement-profile-source-identities.json']:
 shutil.copyfile(R/n,E/n)
shutil.copyfile(R/'package-audit.json',E/'unchanged-production-package-audit.json')
cls=P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation/WaterRetirementBudgetChecks.class';shutil.copyfile(cls,E/'validation-fixture.class')
bundles=[]
for d in sorted(B.iterdir()):
 if not d.is_dir()or not(d/'result.json').exists():continue
 stream=io.BytesIO();members=[]
 with tarfile.open(fileobj=stream,mode='w:gz',compresslevel=9)as t:
  for p in sorted(d.rglob('*')):
   if p.is_file():
    b=p.read_bytes();rel=str(p.relative_to(B));members.append({'path':rel,'bytes':len(b),'sha256':sha(b)});t.add(p,arcname=rel,recursive=False)
 blob=stream.getvalue();parts=[]
 for i,at in enumerate(range(0,len(blob),524288)):
  part=blob[at:at+524288];path=Path('captures')/(d.name+'.tar.gz.part%03d'%i);out=E/path;out.parent.mkdir(exist_ok=True);out.write_bytes(part);parts.append({'path':str(path),'bytes':len(part),'sha256':sha(part)})
 bundles.append({'name':d.name,'bytes':len(blob),'sha256':sha(blob),'parts':parts,'files':members})
(E/'capture-bundles.json.gz').write_bytes(gzip.compress(json.dumps(bundles,indent=2).encode(),mtime=0))
finals=[json.loads((B/n/'water-retirement-budget.json').read_text())for n in ['vulkan-retire-profile-cap-single-final','vulkan-retire-profile-gl-final']]
for d in finals:assert d['validationFixtureClassSha256']==sha(cls.read_bytes())
write(Path('runtime-fixture-identity.json'),{'pass':True,'compiled_fixture_sha256':sha(cls.read_bytes()),'runtime_final_fixture_sha256':[d['validationFixtureClassSha256']for d in finals],'final_fixture_source_sha256':sha((P/changed[1]if changed[1].endswith('WaterRetirementBudgetChecks.java')else P/changed[0]).read_bytes()),'prior_variant_runtime_class_hash_not_captured':True,'validation_class_packaged_in_released_installer':False})
backend=P/'renderer-vulkan/src/main/java/net/vulkanmod/vulkan/memory/buffer/StagingBuffer.java'
write(Path('staging-source-audit.json'),{'source':str(backend.relative_to(P)),'source_sha256':sha(backend.read_bytes()),'source_git_sha':git(backend.read_bytes()),'modified':False,'observed_duplicate_setup_used_bytes':67108608,'staging_capacity_bytes':67108864,'first_write_bytes':16416,'observed_after_first_write_used_bytes':16416,'backend_first_write_ns':56726756,'source_path':'copyBuffer detects insufficient remaining space, calls submitUploads, submits commands, flushes and waits for fences before reset.','inference':'Observed occupancy/reset follows the overflow branch containing synchronous fence waiting. Backend timing includes all work and possible concurrent GC; exclusive fence duration is not measured. This does not establish the cause of every earlier outlier or normal-frame behavior.','fixed_fixture_setup_vertex_upload_bytes':33849216,'remaining_single_setup_burst_wrap_turn':126})
print(json.dumps({'files':len([p for p in E.rglob('*')if p.is_file()]),'bundle_bytes':sum(x['bytes']for x in bundles),'sessions':len(bundles),'members':sum(len(x['files'])for x in bundles),'source_delta':changed}))
