"""Recheck sealed source/capture archives. Performs no build, native launch or new timing."""
from pathlib import Path
import json,sys,hashlib,gzip,tarfile
from restore_snapshot import read_bundle,restore
from audit_console import audit as console
from audit_admission import audit as admission
from audit_boundary_batches import audit as boundary
from audit_frame_retirement import audit as frame
from audit_extraction_deadline import audit as deadline
from audit_corners import audit as corner
from audit_publication import check as publication
def sha(b):return hashlib.sha256(b).hexdigest()
root=Path(sys.argv[1]);dest=Path(sys.argv[2]);assert not dest.exists();dest.mkdir(parents=True);members=0
record=json.loads((root/'capture-bundle.json').read_text());objects=read_bundle(root,record);members+=len(record['members']);layout=json.loads(gzip.decompress((root/'capture-layout.json.gz').read_bytes()));seen=set()
for row in layout:
 p=Path(row['path']);assert not p.is_absolute()and'..'not in p.parts and str(p)not in seen;seen.add(str(p));b=objects[row['object']];assert len(b)==row['bytes']and sha(b)==row['sha256'];out=dest/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
for name,prefix in [('build-bundle.json','builds'),('prior-admission-bundle.json','historical')]:
 record=json.loads((root/name).read_text());data=read_bundle(root,record);members+=len(record['members'])
 for p,b in data.items():out=dest/prefix/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
names=['vulkan-corners-shared-final-index','vulkan-corners-shared-final-index-packaged','vulkan-reuse-gl-corners-shared-final-index'];warm='vulkan-corners-shared-final-r41-control';cold='vulkan-batch-r41-control'
console_rows=[console(dest/n)for n in [cold,warm,*names]]+[console(dest/'builds'/n,True)for n in ['build-baseline','build-candidate']]
base,original=boundary(dest/warm);coldbase,colddata=boundary(dest/cold);assert colddata==original and coldbase['fixture_class_sha256']==base['fixture_class_sha256'];boundaries=[]
for n in names:
 r,data=boundary(dest/n);assert r['fixture_class_sha256']==base['fixture_class_sha256'];assert data==original;boundaries.append(r)
assert base['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterBoundaryBatchChecks.class').read_bytes())
adbase,adoriginal=admission(dest/'historical/vulkan-admission-baseline-identity',True);admissions=[]
for n in [warm,*names]:
 r,data=admission(dest/n);assert r['fixture_class_sha256']==adbase['fixture_class_sha256'];assert all(v==data[k]for k,v in adoriginal.items());admissions.append(r)
assert adbase['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterAdmissionPlanChecks.class').read_bytes())
cs=[corner(dest/n)for n in [warm,*names]];ds=[deadline(dest/n)for n in [warm,*names]];fs=[frame(dest/n)for n in [warm,*names]]
h=dest/'historical'
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts;out=h/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
pubbase,oracle=publication(h/'vulkan-publication-baseline',True);ps=[]
for n in [warm,*names]:
 rec,data=publication(dest/n);assert all(v==oracle[k]for k,v in data.items()if k[4]=='after-retry');ps.append(rec)
for n in ['build-baseline','build-candidate']:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())and sha(raw)==r['raw_sha256']
identities=json.loads((root/'compiled-product-identities.json').read_text())
for row in identities:
 data=(root/row['path']).read_bytes();assert len(data)==row['bytes']and sha(data)==row['sha256']
runtime=json.loads((root/'runtime-source-identity.json').read_text());runtime_rows=runtime['runtime_renderer_class_identities'];assert runtime_rows[0]['renderer_runtime_sha256']==sha((root/'runtime-product-classes/control-renderer.class').read_bytes());assert all(x['renderer_runtime_sha256']==sha((root/'runtime-product-classes/candidate-renderer.class').read_bytes())for x in runtime_rows[1:]);assert runtime['renderer_disassembly_sha256']==sha((root/'renderer-javap.txt').read_bytes())
restored=restore(root,dest/'source');overlay=json.loads((root/'baseline-overlay.json').read_text());baseline={p:b for p,b in read_bundle(root,json.loads((root/'source-snapshot-bundle.json').read_text())).items()}
for p in overlay['remove']:del baseline[p]
for p in overlay['replace']:baseline[p]=(root/'baseline-overlay'/p).read_bytes()
rows=json.loads((root/'baseline-source-manifest.json').read_text());assert set(baseline)=={x['path']for x in rows}and len(rows)==584
for row in rows:assert len(baseline[row['path']])==row['bytes']and sha(baseline[row['path']])==row['sha256']
print(json.dumps({'pass':True,'verified_archived_members':members,'verified_capture_paths':len(layout),'verified_capture_content_objects':len(objects),'restored_source':restored,'baseline584_source_reconstruction_verified':True,'cold_boundary_control':coldbase,'baseline_full_suite_boundary':base,'candidate_boundaries':boundaries,'all_call_and_retry_packets_equal_fresh_controls':True,'candidate_and_control_admissions':admissions,'final_corner_comparisons_including_control':sum(x['comparisons']for x in cs),'final_frame_controls':fs,'final_deadline_controls':ds,'final_publication_sessions':ps,'two_full_builds_pass':True,'raw_sealed_console_sessions':console_rows,'historical_admission_and_publication_baselines_not_fresh':True,'new_build_or_native_execution':False},indent=2))
