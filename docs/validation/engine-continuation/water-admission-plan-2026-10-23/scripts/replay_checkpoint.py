"""Hash-checked archived replay; no new native execution."""
from pathlib import Path
import json,sys,hashlib,gzip,tarfile,zipfile,io
from restore_snapshot import read_bundle,restore
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
for p,b in read_bundle(root,json.loads((root/'build-bundle.json').read_text())).items():
 out=dest/'builds'/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);members+=1
names=['vulkan-corners-shared-final-admission','vulkan-corners-shared-final-admission-packaged-verified','vulkan-reuse-gl-corners-shared-final-admission']
base,original=admission(dest/'vulkan-admission-baseline-identity',True);candidates=[]
for n in names:
 r,data=admission(dest/n);assert r['fixture_class_sha256']==base['fixture_class_sha256'];assert all(v==data[k]for k,v in original.items());candidates.append(r)
assert base['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterAdmissionPlanChecks.class').read_bytes())
initial,_=admission(dest/'vulkan-admission-baseline',True,True);assert initial['fixture_class_sha256']==sha((root/'baseline/initial-fixture/WaterAdmissionPlanChecks.class').read_bytes())
rejected=[]
for n in ['vulkan-admission-baseline-mesh','vulkan-admission-baseline-mesh-rebuilt','vulkan-admission-baseline-fresh','vulkan-admission-baseline-no-cache']:
 d=json.loads((dest/n/'water-admission.json').read_text());assert not d.get('farAndBusyGeometryFromRealMesher',False);assert d['fixtureClassSha256']==initial['fixture_class_sha256'];rejected.append(n)
bs=[boundary(dest/n)[0]for n in names];cs=[corner(dest/n)for n in names];ds=[deadline(dest/n)for n in names];fs=[frame(dest/n)for n in names]
historical=dest/'historical';historical.mkdir()
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts;out=historical/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
pubbase,oracle=publication(historical/'vulkan-publication-baseline',True);ps=[]
for n in names:
 rec,data=publication(dest/n);assert all(v==oracle[k]for k,v in data.items()if k[4]=='after-retry');ps.append(rec)
for n in ['build-baseline','build-candidate','build-candidate-package-recovery','compile-validation-fresh']:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())and sha(raw)==r['raw_sha256']
bad_package=read_bundle(root,json.loads((root/'rejected-package-bundle.json').read_text()));members+=len(bad_package)
failed_meta=json.loads(bad_package['audit.json']);failed_name,=[n for n in bad_package if n.endswith('.jar')];failed_bytes=bad_package[failed_name];assert sha(failed_bytes)==failed_meta['archive_sha256']
with zipfile.ZipFile(io.BytesIO(failed_bytes))as z:
 assert z.testzip()is None;n,=[n for n in z.namelist()if n.startswith('META-INF/jarjar/')and'DeepPrimal-GraphicsSettings-'in n]
 with zipfile.ZipFile(io.BytesIO(z.read(n)))as zz:assert zz.testzip()is None and zz.read('META-INF/neoforge.mods.toml')==b''
failed_capture=dest/'vulkan-corners-shared-final-admission-packaged';failed_result=json.loads((failed_capture/'result.json').read_text());assert failed_result['exit_code']!=0 and not failed_result['build_successful']and'Failed to start FML'in gzip.decompress((failed_capture/'console-final.gz').read_bytes()).decode()
restored=restore(root,dest/'source');print(json.dumps({'pass':True,'verified_archived_members':members,'verified_capture_paths':len(layout),'verified_capture_content_objects':len(objects),'restored_source':restored,'admission_baseline':base,'admission_candidates':candidates,'initial_allocation_diagnostic':initial,'rejected_stale_attempts':rejected,'final_boundary_sessions':bs,'final_corner_comparisons':sum(x['comparisons']for x in cs),'final_deadline_sessions':ds,'final_frame_sessions':fs,'final_publication_sessions':ps,'three_full_builds_and_scoped_compile_pass':True,'historical_publication_baseline_not_fresh':True,'failed_package_exact_archive_replayed_and_excluded':True,'new_native_execution':False},indent=2))
