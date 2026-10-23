from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent
P=R/'project';PREFIX='docs/validation/engine-continuation/water-direction-blend-2026-10-23';E=P/PREFIX
def sha(b):return hashlib.sha256(b).hexdigest()
def gitsha(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
prior=json.loads((R/'checkpoint-text-elements.json').read_text());source=[x['path'] for x in prior if not x['path'].startswith(PREFIX+'/')]
(E/'evidence-manifest.json').write_text(json.dumps([{'path':str(p.relative_to(E)),'bytes':p.stat().st_size,'sha256':sha(p.read_bytes())} for p in sorted(E.rglob('*')) if p.is_file() and p.name!='evidence-manifest.json'],indent=2)+'\n')
texts=[];binaries=[];expected=[]
for p in [P/f for f in source]+[p for p in sorted(E.rglob('*')) if p.is_file()]:
 b=p.read_bytes();rel=str(p.relative_to(P));expected.append({'path':rel,'sha':gitsha(b),'mode':'100644','type':'blob'})
 if p.suffix in ['.png','.bin','.gz','.webm','.zip']:binaries.append({'path':rel,'local_path':str(p),'bytes':len(b),'git_sha':gitsha(b),'sha256':sha(b)})
 else:texts.append({'path':rel,'type':'blob','mode':'100644','content':b.decode()})
assert len(expected)==len(set(x['path'] for x in expected))
(R/'checkpoint-text-elements.json').write_text(json.dumps(texts));(R/'checkpoint-binary-manifest.json').write_text(json.dumps(binaries,indent=2)+'\n');(R/'checkpoint-expected-entries.json').write_text(json.dumps(expected))
print(json.dumps({'texts':len(texts),'binaries':len(binaries),'bytes':sum(x['bytes'] for x in binaries),'total':len(expected)}))
