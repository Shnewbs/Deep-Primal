"""Hash-checked archive replay, without fresh build/native or benchmark execution."""
from pathlib import Path
import json,sys,hashlib,gzip,tarfile,subprocess
from restore_snapshot import read_bundle,restore
from audit_console import audit as console
from audit_admission import audit as admission
from audit_boundary_batches import audit as boundary
from audit_frame_retirement import audit as frame
from audit_extraction_deadline import audit as deadline
from audit_corners import audit as corner
from audit_publication import check as publication
def sha(b):return hashlib.sha256(b).hexdigest()
root=Path(sys.argv[1]);dest=Path(sys.argv[2]);assert not dest.exists();dest.mkdir(parents=True);members=0;objects={}
for record in json.loads(gzip.decompress((root/'capture-bundles.json.gz').read_bytes())):
 objects.update(read_bundle(root,record));members+=len(record['members'])
layout=json.loads(gzip.decompress((root/'capture-layout.json.gz').read_bytes()));seen=set()
for row in layout:
 p=Path(row['path']);assert not p.is_absolute()and'..'not in p.parts and str(p)not in seen;seen.add(str(p));b=objects[row['object']];assert len(b)==row['bytes']and sha(b)==row['sha256'];out=dest/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
for name,prefix in [('build-bundle.json','builds'),('benchmark-bundle.json',''),('prior-admission-bundle.json','historical')]:
 record=json.loads((root/name).read_text());data=read_bundle(root,record);members+=len(record['members'])
 if name=='benchmark-bundle.json':
  for row in json.loads(gzip.decompress((root/'benchmark-layout.json.gz').read_bytes())):
   path=Path(row['path']);assert not path.is_absolute()and'..'not in path.parts;b=data[row['object']];assert len(b)==row['bytes']and sha(b)==row['sha256'];out=dest/path;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
 else:
  for p,b in data.items():
   out=dest/prefix/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
names=['vulkan-corners-shared-final-quad','vulkan-corners-shared-final-quad-packaged','vulkan-reuse-gl-corners-shared-final-quad']
console_rows=[console(dest/n)for n in ['vulkan-batch-r40-control',*names]]+[console(dest/'builds'/n,True)for n in ['build-baseline','build-candidate']]
base,original=boundary(dest/'vulkan-batch-r40-control');boundaries=[]
for n in names:
 r,data=boundary(dest/n);assert r['fixture_class_sha256']==base['fixture_class_sha256'];assert data==original;boundaries.append(r)
assert base['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterBoundaryBatchChecks.class').read_bytes())
adbase,adoriginal=admission(dest/'historical/vulkan-admission-baseline-identity',True);admissions=[]
for n in names:
 r,data=admission(dest/n);assert r['fixture_class_sha256']==adbase['fixture_class_sha256'];assert all(v==data[k]for k,v in adoriginal.items());admissions.append(r)
assert adbase['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterAdmissionPlanChecks.class').read_bytes())
cs=[corner(dest/n)for n in names];ds=[deadline(dest/n)for n in names];fs=[frame(dest/n)for n in names]
h=dest/'historical'
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts;out=h/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
pubbase,oracle=publication(h/'vulkan-publication-baseline',True);ps=[]
for n in names:
 rec,data=publication(dest/n);assert all(v==oracle[k]for k,v in data.items()if k[4]=='after-retry');ps.append(rec)
for n in ['build-baseline','build-candidate']:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())and sha(raw)==r['raw_sha256']
identities=json.loads((root/'benchmark-identities.json').read_text());records=json.loads((root/'benchmark-results.json').read_text());(dest/'benchmark-results.json').write_bytes((root/'benchmark-results.json').read_bytes())
for run in records:
 variant=run['variant'];r=dest/'benchmarks'/('run-'+str(run['run'])+'-'+variant);assert json.loads((r/'result.json').read_text())==run
 for p,digest in identities[variant]['all_core_class_sha256'].items():assert sha((dest/'benchmarks'/(variant+'-core-classes')/p).read_bytes())==digest
 assert run['identity_before']==identities[variant]
a=subprocess.run([sys.executable,str(root/'scripts/audit_benchmark.py'),str(dest)],capture_output=True,text=True);assert a.returncode==0,a.stderr;benchmark=json.loads(a.stdout);assert benchmark['pass'];(dest/'benchmark-replay.json').write_text(a.stdout)
restored=restore(root,dest/'source');print(json.dumps({'pass':True,'verified_archived_members':members,'verified_capture_paths':len(layout),'verified_capture_content_objects':len(objects),'restored_source':restored,'baseline_boundary_fresh_native':base,'candidate_boundaries':boundaries,'candidate_admissions':admissions,'final_corner_comparisons':sum(x['comparisons']for x in cs),'final_frame_controls':fs,'final_deadline_controls':ds,'final_publication_sessions':ps,'two_full_builds_pass':True,'raw_sealed_console_sessions':console_rows,'benchmark_primary_pinned_class_bytes_and_independent_payloads_verified':True,'benchmark':benchmark,'historical_admission_and_publication_baselines_not_fresh':True,'new_native_or_benchmark_execution':False},indent=2))
