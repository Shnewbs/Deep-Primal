"""Verify split archive hashes and replay independent CPU/pixel/readback oracles."""
from pathlib import Path
import json,hashlib,tarfile,io,sys,importlib.util,tempfile,gzip
E=Path(__file__).resolve().parent.parent
def sha(b):return hashlib.sha256(b).hexdigest()
def module(n):
 s=importlib.util.spec_from_file_location(n,E/'scripts'/(n+'.py'));m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
with tempfile.TemporaryDirectory()as temp:
 out=Path(temp);bundles=json.loads(gzip.decompress((E/'capture-bundles.json.gz').read_bytes()));count=0
 for r in bundles:
  parts=[]
  for p in r['parts']:
   b=(E/p['path']).read_bytes();assert len(b)==p['bytes']and sha(b)==p['sha256'];parts.append(b)
  b=b''.join(parts);assert len(b)==r['bytes']and sha(b)==r['sha256']
  with tarfile.open(fileobj=io.BytesIO(b),mode='r:gz')as t:t.extractall(out,filter='data')
  for f in r['files']:
   b=(out/f['path']).read_bytes();assert len(b)==f['bytes']and sha(b)==f['sha256'];count+=1
 names=['vulkan-corners-publication-final','vulkan-corners-publication-final-packaged','vulkan-reuse-gl-corners-publication-final'];c=module('audit_corners');p=module('audit_publication')
 corners=[c.audit(out/n)for n in names];baseline=p.check(out/'vulkan-publication-baseline',True)
 # The complete publication CLI also compares repaired payloads to the reproduced target.
 import subprocess
 publication=json.loads(subprocess.check_output([sys.executable,str(E/'scripts/audit_publication.py'),str(out/'vulkan-publication-baseline'),*[str(out/n)for n in names]]))
 for name in ['baseline-build','candidate-build']:
  r=json.loads((out/name/'candidate-build-result.json').read_text());assert r['pass']and r['exit_code']==0 and all(r['required_records'].values())
 print(json.dumps({'pass':True,'archived_files_verified':count,'corner_sessions':len(corners),'corner_comparisons':sum(r['comparisons']for r in corners),'publication_audit':publication,'scope':'Replay of retained fresh native output, not another native execution.'},indent=2))
