from pathlib import Path
import gzip,json,hashlib,tarfile,io,tempfile,importlib.util
E=Path(__file__).resolve().parent.parent
def sha(b):return hashlib.sha256(b).hexdigest()
spec=importlib.util.spec_from_file_location('profile_audit',E/'scripts/audit_retirement_profile.py');audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
with tempfile.TemporaryDirectory()as temp:
 out=Path(temp);bundles=json.loads(gzip.decompress((E/'capture-bundles.json.gz').read_bytes()));results=[];count=0
 for r in bundles:
  parts=[]
  for p in r['parts']:
   b=(E/p['path']).read_bytes();assert len(b)==p['bytes']and sha(b)==p['sha256'];parts.append(b)
  b=b''.join(parts);assert len(b)==r['bytes']and sha(b)==r['sha256']
  with tarfile.open(fileobj=io.BytesIO(b),mode='r:gz')as t:t.extractall(out,filter='data')
  for p in r['files']:
   b=(out/p['path']).read_bytes();assert len(b)==p['bytes']and sha(b)==p['sha256'];count+=1
  results.append(audit.audit(out/r['name']))
 final=[r for r in results if r['case'].endswith('final')];assert len(final)==2 and all(r['validation_fixture_class_sha256']==sha((E/'validation-fixture.class').read_bytes())for r in final)
 print(json.dumps({'pass':True,'archived_files_verified':count,'sessions':len(results),'final_runtime_fixture_identity':True,'independent_steady_packet_hash_oracle':True,'scope':'Replay of retained native output, not a new native execution. Performance remains unaccepted.'},indent=2))
