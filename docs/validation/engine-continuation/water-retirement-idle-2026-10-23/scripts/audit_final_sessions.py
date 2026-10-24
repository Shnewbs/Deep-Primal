from pathlib import Path
import json,sys,tarfile
R=Path(__file__).parent;B=Path('/tmp/deepprimal-idle-2328');sys.path.insert(0,str(R))
from audit_admission import audit as admission
from audit_boundary_batches import audit as boundary
from audit_frame_retirement import audit as frame
from audit_extraction_deadline import audit as deadline
from audit_corners import audit as corner
from audit_publication import check as publication
names=sys.argv[1:];baseline_boundary,expected_boundary=boundary(Path('/tmp/deepprimal-index-2102/vulkan-corners-shared-final-r41-control'));base,original=admission(Path('/tmp/deepprimal-admission-1932/vulkan-admission-baseline-identity'),True);rows=[];boundaries=[]
for n in names:
 r,data=admission(B/n);assert r['fixture_class_sha256']==base['fixture_class_sha256'];assert all(v==data[k]for k,v in original.items());rows.append(r)
 r,data=boundary(B/n);assert r['fixture_class_sha256']==baseline_boundary['fixture_class_sha256'];assert data==expected_boundary;boundaries.append(r)
(R/'strict-admission-audit.json').write_text(json.dumps({'pass':True,'baseline_historical_r40':True,'baseline':base,'candidates':rows,'same_native_fixture_class':True,'shared_before_after_and_survivor_packets_match_baseline':True},indent=2)+'\n')
(R/'strict-boundary-audit.json').write_text(json.dumps({'pass':True,'baseline_historical_r42':baseline_boundary,'sessions':boundaries,'all_before_after_call_and_retry_packets_match_baseline':True},indent=2)+'\n')
for name,fn in [('corner',corner),('frame',frame),('deadline',deadline)]:
 (R/('strict-'+name+'-audit.json')).write_text(json.dumps({'pass':True,'sessions':[fn(B/n)for n in names]},indent=2)+'\n')
h=B/'historical-publication'
if not h.exists():
 h.mkdir()
 with tarfile.open(R.parent/'water-admission-plan-1932/checkpoint/prior-publication-baseline.tar.gz','r:gz')as t:
  for m in t.getmembers():
   assert m.isfile()and not Path(m.name).is_absolute()and'..'not in Path(m.name).parts;p=h/m.name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(t.extractfile(m).read())
baseline,oracle=publication(h/'vulkan-publication-baseline',True);rows=[]
for n in names:
 r,data=publication(B/n);assert all(v==oracle[k]for k,v in data.items()if k[4]=='after-retry');rows.append(r)
(R/'strict-publication-audit.json').write_text(json.dumps({'pass':True,'baseline_historical':True,'sessions':rows},indent=2)+'\n');print('FINAL_SESSION_AUDITS_PASS',len(names))
