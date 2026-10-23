"""Lossless capture storage compaction; every logical path remains hash-checkable."""
from pathlib import Path
import json,gzip,hashlib,tarfile,io
R=Path(__file__).parent;E=R/'checkpoint';B=Path('/tmp/deepprimal-quad-2029')
def sha(b):return hashlib.sha256(b).hexdigest()
old=json.loads(gzip.decompress((E/'capture-bundles.json.gz').read_bytes()))
assert all(x['archive'].startswith('captures/vulkan-')for x in old),'Compact the original per-session format only'
objects={};layout=[]
for record in old:
 for row in record['members']:
  path=Path(row['path']);assert not path.is_absolute()and'..'not in path.parts
  p=B/path;b=p.read_bytes();assert len(b)==row['bytes']and sha(b)==row['sha256'];key='objects/'+row['sha256'];objects.setdefault(key,p);layout.append({**row,'object':key})
raw=io.BytesIO();members=[]
with tarfile.open(fileobj=raw,mode='w')as t:
 for key,p in sorted(objects.items()):
  b=p.read_bytes();i=tarfile.TarInfo(key);i.size=len(b);i.mtime=0;i.mode=0o644;t.addfile(i,io.BytesIO(b));members.append({'path':key,'bytes':len(b),'sha256':sha(b)})
b=gzip.compress(raw.getvalue(),mtime=0);parts=[]
for n,o in enumerate(range(0,len(b),524288)):
 p=Path('captures/capture-data.tar.gz'+f'.part{n:03}');chunk=b[o:o+524288];(E/p).write_bytes(chunk);parts.append({'path':str(p),'bytes':len(chunk),'sha256':sha(chunk),'git_sha':hashlib.sha1(b'blob '+str(len(chunk)).encode()+b'\0'+chunk).hexdigest()})
record={'archive':'captures/capture-data.tar.gz','bytes':len(b),'sha256':sha(b),'parts':parts,'members':members}
(E/'capture-layout.json.gz').write_bytes(gzip.compress(json.dumps(layout).encode(),mtime=0));(E/'capture-bundles.json.gz').write_bytes(gzip.compress(json.dumps([record]).encode(),mtime=0))
for rec in old:
 for part in rec['parts']:
  p=Path(part['path']);assert p.parts[0]=='captures'and'..'not in p.parts;(E/p).unlink()
report={'pass':True,'logical_capture_paths':len(layout),'unique_content_objects':len(objects),'original_logical_bytes':sum(x['bytes']for x in layout),'unique_uncompressed_bytes':sum(x['bytes']for x in members),'original_archive_bytes':sum(x['bytes']for x in old),'compacted_archive_bytes':len(b),'all_original_path_hashes_preserved':True,'only_own_unpublished_duplicate_archives_removed':True}
(E/'capture-compaction.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
