from pathlib import Path
import json,subprocess,zipfile,io,hashlib,gzip,time
R=Path(__file__).resolve().parent;P=R/'project';O=Path('/tmp/deepprimal-publication-1529/package-rebuild');O.mkdir(exist_ok=False);cmd=json.loads(Path('/tmp/deepprimal-publication-1529/candidate-build/candidate-build-result.json').read_text())['command'];cmd[-1]='jar'
with(O/'console.log').open('wb')as f:r=subprocess.run(cmd,cwd=P,stdout=f,stderr=subprocess.STDOUT,timeout=400)
raw=(O/'console.log').read_bytes();(O/'console-final.gz').write_bytes(gzip.compress(raw,mtime=0));jar=next((P/'build/libs').glob('*-vulkan-linux.jar'));b=jar.read_bytes();labels=[]
def audit(data,label):
 with zipfile.ZipFile(io.BytesIO(data))as z:
  assert z.testzip()is None;labels.append(label)
  for n in z.namelist():
   if n.startswith('META-INF/jarjar/')and n.endswith('.jar'):audit(z.read(n),n)
assert r.returncode==0;audit(b,jar.name);assert len(labels)==6;(R/'accepted-build.jar').write_bytes(b)
record={'pass':True,'exit_code':r.returncode,'command':cmd,'raw_sha256':hashlib.sha256(raw).hexdigest(),'archive_sha256':hashlib.sha256(b).hexdigest(),'archive_bytes':len(b),'archive_crcs':labels,'prior_truncated_archive_excluded':True};(O/'result.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record))
