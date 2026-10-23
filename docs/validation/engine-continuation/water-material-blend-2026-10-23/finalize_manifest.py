from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';E=P/'docs/validation/engine-continuation/water-material-blend-2026-10-23'
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def sha(b):return hashlib.sha256(b).hexdigest()
(E/'finalize_manifest.py').write_bytes(Path(__file__).read_bytes())
replay=json.loads((E/'offline-audit-replay.json').read_text());assert replay['pass']
st=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text());st['latest_test_summary']['offline_compact_evidence_replay']=True
(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(st,indent=2)+'\n')
su=json.loads((E/'checkpoint-summary.json').read_text());su['offline_compact_evidence_replay']=True;(E/'checkpoint-summary.json').write_text(json.dumps(su,indent=2)+'\n')
(E/'evidence-manifest.json').write_text(json.dumps([{'path':str(p.relative_to(E)),'bytes':p.stat().st_size,'sha256':sha(p.read_bytes())} for p in sorted(E.rglob('*')) if p.is_file() and p.name!='evidence-manifest.json'],indent=2)+'\n')
scope=json.loads((E/'source-scope-audit.json').read_text());paths=scope['production_changed_or_added']+scope['validation_changed_or_added']+['docs/ENGINE-WORK-STATE.json','docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']
texts=[];binary=[];expected=[]
for p in [P/x for x in paths]+[p for p in sorted(E.rglob('*')) if p.is_file()]:
 b=p.read_bytes();rel=str(p.relative_to(P));expected.append({'path':rel,'sha':gs(b),'mode':'100644','type':'blob'})
 if p.suffix in ['.png','.bin','.gz','.webm','.zip']:binary.append({'path':rel,'local_path':str(p),'bytes':len(b),'git_sha':gs(b),'sha256':sha(b)})
 else:texts.append({'path':rel,'type':'blob','mode':'100644','content':b.decode()})
for name,obj in [('checkpoint-text-elements.json',texts),('checkpoint-binary-manifest.json',binary),('checkpoint-expected-entries.json',expected)]: (R/name).write_text(json.dumps(obj))
print(json.dumps({'text_files':len(texts),'text_bytes':sum(len(x['content'].encode()) for x in texts),'binary_files':len(binary),'binary_bytes':sum(x['bytes'] for x in binary),'largest_text':max((len(x['content']),x['path']) for x in texts),'unique_binary':len({x['git_sha'] for x in binary})}))
