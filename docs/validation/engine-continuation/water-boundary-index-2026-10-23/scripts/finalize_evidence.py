from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parent;E=R/'checkpoint';r=json.loads((R/'replay-console.json').read_text());assert r['pass']and r['restored_source']['source_inputs']==586
shutil.copyfile(R/'replay-console.json',E/'offline-audit-replay.json')
s=json.loads((E/'checkpoint-summary.json').read_text());s.update({'archived_members_replayed':r['verified_archived_members'],'logical_capture_paths_replayed':r['verified_capture_paths'],'unique_capture_objects':r['verified_capture_content_objects'],'baseline584_source_reconstruction_verified':True,'offline_replay_pass':True})
for p in [E/'checkpoint-summary.json',R/'checkpoint-summary.json']:p.write_text(json.dumps(s,indent=2)+'\n')
for p in [E/'README.md',R/'README.md']:
 text=p.read_text();text+='\nOffline replay passes: '+str(r['verified_archived_members'])+'physical capture/build/historical-admission archive members and'+str(r['verified_capture_paths'])+'logical capture paths are hash-checked, alongside586candidate inputs and584reconstructed control inputs. No build/native execution or new timing is performed by replay.\n';p.write_text(text)
shutil.copyfile(Path(__file__),E/'scripts/finalize_evidence.py')
rows=[]
for p in sorted(E.rglob('*')):
 if not p.is_file():continue
 b=p.read_bytes();assert len(b)<=524288,(p,len(b));rows.append({'path':p.relative_to(E).as_posix(),'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest(),'git_sha':hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()})
(R/'upload-manifest.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps({'pass':True,'files':len(rows),'bytes':sum(x['bytes']for x in rows),'archived_members_replayed':r['verified_archived_members'],'logical_capture_paths':r['verified_capture_paths'],'source_inputs':586,'control_inputs':584},indent=2))
