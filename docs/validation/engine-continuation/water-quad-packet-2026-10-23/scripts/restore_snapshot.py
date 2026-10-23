"""Restore a fully hash-checked 584-input candidate into a new isolated directory."""
from pathlib import Path,PurePosixPath
import json,hashlib,tarfile,io,sys
def sha(b):return hashlib.sha256(b).hexdigest()
def read_bundle(root,record):
 chunks=[]
 for row in record['parts']:
  p=PurePosixPath(row['path']);assert not p.is_absolute()and'..'not in p.parts
  b=(root/str(p)).read_bytes();assert len(b)==row['bytes']and sha(b)==row['sha256'];chunks.append(b)
 b=b''.join(chunks);assert len(b)==record['bytes']and sha(b)==record['sha256'];out={}
 with tarfile.open(fileobj=io.BytesIO(b),mode='r:gz')as t:
  rows=record['members'];members=t.getmembers();assert len(rows)==len(members)
  for row,m in zip(rows,members):
   p=PurePosixPath(m.name);assert m.isfile()and not p.is_absolute()and'..'not in p.parts and m.name==row['path']and m.name not in out
   data=t.extractfile(m).read();assert len(data)==row['bytes']and sha(data)==row['sha256'];out[m.name]=data
 return out
def restore(root,target):
 rows=json.loads((root/'source-snapshot-manifest.json').read_text());data=read_bundle(root,json.loads((root/'source-snapshot-bundle.json').read_text()))
 assert len(data)==len(rows)==584 and set(data)=={x['path']for x in rows}
 for row in rows:
  b=data[row['path']];assert sha(b)==row['sha256']and hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==row['git_sha']
 assert not target.exists(),'Use a new isolated directory; no overwrites permitted'
 target.mkdir(parents=True)
 for p,b in data.items():
  dest=target/p;dest.parent.mkdir(parents=True,exist_ok=True)
  with dest.open('xb')as f:f.write(b)
 (target/'gradlew').chmod(0o755)
 return {'pass':True,'source_inputs':584,'all_archive_member_and_source_hashes_verified_before_writing':True,'target':str(target),'build_or_native_validation_repeated':False}
if __name__=='__main__':print(json.dumps(restore(Path(sys.argv[1]),Path(sys.argv[2])),indent=2))
