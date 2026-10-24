from pathlib import Path
import argparse,json,hashlib,gzip
E=Path(__file__).resolve().parents[1]
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
p=argparse.ArgumentParser(description='Apply only this staged candidate to a separate verified parent checkout. No dependencies or builds run.');p.add_argument('--project',type=Path,required=True);args=p.parse_args();P=args.project.resolve();rows=json.loads((E/'staged-restore-manifest.json').read_text())
m=E/'source-input-manifest.json';inputs=json.loads(m.read_text()if m.exists()else gzip.decompress(m.with_name(m.name+'.gz').read_bytes()))
for r in inputs:
 target=P/r['path'];assert target.is_file()and gs(target.read_bytes())==r['sha'],'Base input mismatch: '+r['path']
for r in rows:
 target=P/r['path'];source=E/'staged'/r['path'];assert gs(source.read_bytes())==r['candidate_sha']
 if r['parent_sha']is None:assert not target.exists(),'New path already exists: '+r['path']
 else:assert target.is_file()and gs(target.read_bytes())==r['parent_sha'],'Parent source mismatch: '+r['path']
for r in rows:
 target=P/r['path'];target.parent.mkdir(parents=True,exist_ok=True);temporary=target.with_name(target.name+'.staged-tmp');temporary.write_bytes((E/'staged'/r['path']).read_bytes());temporary.replace(target)
print(json.dumps({'applied':len(rows),'scope':'isolated staged candidate only;performance acceptance false'}))
