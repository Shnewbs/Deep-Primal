from pathlib import Path
import json,hashlib,sys
E=Path(__file__).resolve().parent.parent;P=Path(sys.argv[1]).resolve();prior=json.loads(Path(sys.argv[2]).read_text());overlay=json.loads((E/'staged-overlay-manifest.json').read_text())
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
for r in prior:assert git((P/r['path']).read_bytes())==r['candidate_sha'],('prior staged source mismatch',r['path'])
for r in overlay:
 b=(E/'staged'/r['path']).read_bytes();assert git(b)==r['candidate_sha']and hashlib.sha256(b).hexdigest()==r['sha256'];assert git((P/r['path']).read_bytes())==r['parent_sha']
for r in overlay:(P/r['path']).write_bytes((E/'staged'/r['path']).read_bytes())
print(json.dumps({'pass':True,'prior_staged_files_verified':len(prior),'validation_overlay_files':len(overlay),'all_guards_checked_before_writes':True}))
