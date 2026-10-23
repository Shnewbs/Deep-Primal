"""Hash-checked archived replay; no new native execution."""
from pathlib import Path
import json,sys,hashlib,gzip,tarfile,importlib.util
from restore_snapshot import read_bundle,restore
from audit_boundary_batches import audit as boundary
from audit_frame_retirement import audit as frame
from audit_extraction_deadline import audit as deadline
from audit_corners import audit as corner
from audit_publication import check as publication
def sha(b):return hashlib.sha256(b).hexdigest()
root=Path(sys.argv[1]);dest=Path(sys.argv[2]);assert not dest.exists();dest.mkdir(parents=True);members=0
for record in json.loads(gzip.decompress((root/'capture-bundles.json.gz').read_bytes())):
 for p,b in read_bundle(root,record).items():
  out=dest/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);members+=1
for p,b in read_bundle(root,json.loads((root/'build-bundle.json').read_text())).items():
 out=dest/'builds'/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);members+=1
names=['vulkan-corners-shared-final-capacity','vulkan-corners-shared-final-capacity-packaged','vulkan-reuse-gl-corners-shared-final-capacity']
base,original=boundary(dest/'vulkan-batch-baseline-capacity',True);candidates=[]
for n in names:
 r,data=boundary(dest/n);assert r['fixture_class_sha256']==base['fixture_class_sha256'];assert all(v==original[k]for k,v in data.items()if k[1]=='after-retry');candidates.append(r)
assert base['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterBoundaryBatchChecks.class').read_bytes())
spec=importlib.util.spec_from_file_location('initial_boundary_oracle',root/'baseline/audit_boundary_batches.py');old=importlib.util.module_from_spec(spec);spec.loader.exec_module(old)
diagbase,diagfinal=old.audit(dest/'vulkan-batch-baseline',True);diagnostics=[]
for n in ['vulkan-corners-shared-final-batches','vulkan-corners-shared-final-batches-packaged']:
 r,data=old.audit(dest/n);assert r['fixture_class_sha256']==diagbase['fixture_class_sha256'];assert all(v==diagfinal[k]for k,v in data.items()if k[1]=='after-retry');diagnostics.append(r)
assert diagbase['fixture_class_sha256']==sha((root/'baseline/runtime-fixtures/WaterBoundaryBatchChecks.class').read_bytes())
cs=[corner(dest/n)for n in names];ds=[deadline(dest/n)for n in names];fs=[frame(dest/n)for n in names]
historical=dest/'historical';historical.mkdir()
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts
  out=historical/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
pubbase,oracle=publication(historical/'vulkan-publication-baseline',True);ps=[]
for n in names:
 rec,data=publication(dest/n);assert all(v==oracle[k]for k,v in data.items()if k[4]=='after-retry');ps.append(rec)
for n in ['build-baseline','build-candidate','build-capacity-baseline','build-capacity-candidate']:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())and sha(raw)==r['raw_sha256']
restored=restore(root,dest/'source');print(json.dumps({'pass':True,'verified_archived_members':members,'restored_source':restored,'expanded_baseline':base,'expanded_candidates':candidates,'initial_diagnostic_baseline':diagbase,'initial_diagnostic_candidates':diagnostics,'fixture_variants_preserved_separately':True,'final_corner_comparisons':sum(x['comparisons']for x in cs),'final_deadline_sessions':ds,'final_frame_sessions':fs,'final_publication_sessions':ps,'four_full_builds_pass':True,'historical_publication_baseline_not_fresh':True,'new_native_execution':False},indent=2))
