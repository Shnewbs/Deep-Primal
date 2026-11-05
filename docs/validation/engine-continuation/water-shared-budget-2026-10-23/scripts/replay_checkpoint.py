"""Independent archived-data replay; does not claim fresh native execution."""
from pathlib import Path
import json,sys,hashlib,gzip,zipfile,io,tarfile
from restore_snapshot import read_bundle,restore
from audit_frame_retirement import audit as frame
from audit_extraction_deadline import audit as deadline
from audit_corners import audit as corners
from audit_publication import check as publication
def sha(b):return hashlib.sha256(b).hexdigest()
root=Path(sys.argv[1]);dest=Path(sys.argv[2]);assert not dest.exists();dest.mkdir(parents=True)
members=0
for record in json.loads(gzip.decompress((root/'capture-bundles.json.gz').read_bytes())):
 for p,b in read_bundle(root,record).items():
  out=dest/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);members+=1
for key in ['build-bundle.json','rejected-generated-archive-bundle.json']:
 for p,b in read_bundle(root,json.loads((root/key).read_text())).items():
  out=dest/('rejected-generated'if key.startswith('rejected')else'builds')/p;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b);members+=1
final=['vulkan-corners-shared-final','vulkan-corners-shared-final-packaged','vulkan-reuse-gl-corners-shared-final']
c=[corners(dest/n)for n in final]
d0=deadline(dest/'vulkan-deadline-baseline',True);ds=[deadline(dest/n)for n in final];assert all(x['fixture_class_sha256']==d0['fixture_class_sha256']for x in ds)
frames=[frame(dest/n)for n in ['vulkan-frame-retire-steady','vulkan-frame-retire-active']+final+['vulkan-frame-retire-steady-final']]
assert all(x['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterFrameRetirementChecks.class').read_bytes())for x in frames[2:])
assert d0['fixture_class_sha256']==sha((root/'runtime-fixtures/WaterExtractionDeadlineChecks.class').read_bytes())
historical=dest/'historical';historical.mkdir()
with tarfile.open(root/'prior-publication-baseline.tar.gz','r:gz')as t:
 for m in t.getmembers():
  assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts
  out=historical/m.name;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(t.extractfile(m).read())
old,mirrors=publication(historical/'vulkan-publication-baseline',True);pub=[]
for n in final:
 rec,now=publication(dest/n);assert all(v==mirrors[k]for k,v in now.items()if k[4]=='after-retry');pub.append(rec)
for n,expected in [('build',False),('build-retry',True),('candidate-build',True)]:
 p=dest/'builds'/n;r=json.loads((p/'candidate-build-result.json').read_text());raw=gzip.decompress((p/'candidate-build-final.gz').read_bytes());assert r['pass']==expected and sha(raw)==r['raw_sha256']
 if expected:assert all(r['required_records'].values())and r['exit_code']==0
bad=next((dest/'rejected-generated').glob('*_output.jar')).read_bytes();assert len(bad)==3407868 and sha(bad)=='e98889de25ecfc02336eaf36147ac7380121752fef8d9b3c71036c677d094e6b'
try:zipfile.ZipFile(io.BytesIO(bad));raise AssertionError('Invalid archive unexpectedly opened')
except zipfile.BadZipFile:pass
restored=restore(root,dest/'source');report={'pass':True,'archived_member_hashes_verified':members,'source_restore':restored,'corner_comparisons':sum(x['comparisons']for x in c),'deadline_baseline':d0,'deadline_candidates':ds,'frames':frames,'publication_candidates':pub,'historical_publication_baseline_not_fresh':True,'invalid_generated_archive_reproduced':True,'builds_passed':2,'failed_attempts_retained':1,'new_native_execution':False}
print(json.dumps(report,indent=2))
