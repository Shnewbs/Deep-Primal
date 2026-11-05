"""Replay sealed recovery evidence; no new build/native or timing execution."""
from pathlib import Path
import json,sys,hashlib,gzip,tarfile
from restore_snapshot import read_bundle,restore
from audit_retry import audit as retry
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
 p=Path(row['path']);assert not p.is_absolute()and'..'not in p.parts and str(p)not in seen;seen.add(str(p));b=b''.join(objects[x['object']]for x in row['chunks'])if 'chunks'in row else objects[row['object']];assert len(b)==row['bytes']and sha(b)==row['sha256'];out=dest/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
for name,prefix in [('build-bundle.json','builds'),('prior-admission-bundle.json','historical')]:
 record=json.loads((root/name).read_text());data=read_bundle(root,record);members+=len(record['members'])
 for p,b in data.items():out=dest/prefix/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
names=['vulkan-corners-shared-final-idle-retry','vulkan-corners-shared-final-idle-packaged-retry','vulkan-reuse-gl-corners-shared-final-idle-retry'];control='vulkan-frame-retire-steady-r43-control-retry';steady='vulkan-frame-retire-steady-idle-retry'
consoles=[console(dest/n)for n in[control,steady,*names]]+[console(dest/'builds'/n,True)for n in['build-baseline','build-candidate-retry']]
base=frame(dest/control);assert base['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterFrameRetirementChecks.class').read_bytes());steady_result=frame(dest/steady);assert not base['idle_policy']and steady_result['idle_policy']and base['fixture_class_sha256']==steady_result['fixture_class_sha256']
retries=[retry(dest/n)for n in names];assert all(r['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterBoundaryRetryChecks.class').read_bytes())for r in retries)
normalbase,oracle=boundary(dest/'historical/vulkan-corners-shared-final-r41-control');boundaries=[]
for n in names:
 r,data=boundary(dest/n);assert data==oracle and r['fixture_class_sha256']==normalbase['fixture_class_sha256'];boundaries.append(r)
adbase,adexpected=admission(dest/'historical/vulkan-admission-baseline-identity',True);admissions=[]
for n in names:
 r,data=admission(dest/n);assert all(data[k]==v for k,v in adexpected.items());admissions.append(r)
cs=[corner(dest/n)for n in names];ds=[deadline(dest/n)for n in names];fs=[frame(dest/n)for n in names];assert all(r['idle_policy']and r['fixture_class_sha256']==base['fixture_class_sha256']for r in fs)
h=dest/'historical'
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts;out=h/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
pubbase,pubexpected=publication(h/'vulkan-publication-baseline',True);publications=[]
for n in names:
 r,data=publication(dest/n);assert all(v==pubexpected[k]for k,v in data.items()if k[4]=='after-retry');publications.append(r)
for n in['build-baseline','build-candidate-retry']:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())and sha(raw)==r['raw_sha256']
incomplete=dest/'builds/build-candidate';assert not(incomplete/'candidate-build-result.json').exists();partial=(incomplete/'candidate-build.log').read_bytes();assert b'BUILD SUCCESSFUL'not in partial and sha(partial)==json.loads((root/'interrupted-build.json').read_text())['raw_sha256']
identities=json.loads((root/'compiled-product-identities.json').read_text())
for row in identities:
 b=(root/row['path']).read_bytes();assert len(b)==row['bytes']and sha(b)==row['sha256']
runtime=json.loads((root/'runtime-source-identity.json').read_text());assert all(r['runtime_renderer_sha256']==sha((root/'runtime-product-classes/candidate-renderer.class').read_bytes())for r in runtime['candidate_sessions']);assert b'retirementState'in(root/'runtime-product-classes/candidate-packet.class').read_bytes()
restored=restore(root,dest/'source');overlay=json.loads((root/'baseline-overlay.json').read_text());data=read_bundle(root,json.loads((root/'source-snapshot-bundle.json').read_text()))
for p in overlay['remove']:del data[p]
for p in overlay['replace']:data[p]=(root/'baseline-overlay'/p).read_bytes()
rows=json.loads((root/'baseline-source-manifest.json').read_text());assert len(data)==len(rows)==588 and set(data)=={r['path']for r in rows}
for r in rows:assert len(data[r['path']])==r['bytes']and sha(data[r['path']])==r['sha256']
print(json.dumps({'pass':True,'archived_members_verified':members,'logical_capture_paths_verified':len(layout),'unique_content_objects':len(objects),'source_restore':restored,'control_source_inputs':588,'control_reconstruction_verified':True,'fresh_idle_baseline':base,'fresh_steady_candidate':steady_result,'candidate_retry_controls':retries,'candidate_normal_boundaries':boundaries,'candidate_admissions':admissions,'seam_comparisons':sum(r['comparisons']for r in cs),'frame_controls':fs,'deadline_controls':ds,'publication_controls':publications,'raw_sealed_console_identities':consoles,'interrupted_full_build_not_counted':True,'normal_boundary_admission_publication_baselines_historical':True,'new_frame_fixture_outer_and_job_identity_verified':True,'new_build_or_native_execution':False},indent=2))
