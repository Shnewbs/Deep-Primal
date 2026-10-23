"""Verify the complete source base and all candidate bytes before writing any source."""
from pathlib import Path
import gzip,json,hashlib,sys
E=Path(__file__).resolve().parent.parent;P=Path(sys.argv[1]).resolve()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
base=json.loads(gzip.decompress((E/'source-input-manifest.json.gz').read_bytes()));staged=json.loads((E/'staged-restore-manifest.json').read_text())
for r in base:assert (P/r['path']).is_file()and git((P/r['path']).read_bytes())==r['sha'],('base mismatch',r['path'])
for r in staged:
 p=P/r['path'];b=(E/'staged'/r['path']).read_bytes();assert git(b)==r['candidate_sha']and hashlib.sha256(b).hexdigest()==r['sha256']
 assert (p.is_file()and git(p.read_bytes())==r['parent_sha'])if r['parent_sha']else not p.exists(),('candidate parent mismatch',r['path'])
for r in staged:
 p=P/r['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes((E/'staged'/r['path']).read_bytes())
print(json.dumps({'pass':True,'base_inputs':len(base),'staged_files':len(staged),'all_guards_checked_before_writes':True}))
