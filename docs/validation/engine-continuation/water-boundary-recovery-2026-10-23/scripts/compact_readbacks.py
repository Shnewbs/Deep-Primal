"""Lossless readback-level compaction of this run's unpublished evidence only."""
from pathlib import Path
import json,gzip,hashlib,tarfile,io,shutil
R=Path(__file__).resolve().parent;E=R/'checkpoint';B=Path('/tmp/deepprimal-retry-2230')
def sha(b):return hashlib.sha256(b).hexdigest()
old=json.loads((E/'capture-bundle.json').read_text());prior=json.loads((E/'prior-boundary-bundle.json').read_text());layout=json.loads(gzip.decompress((E/'capture-layout.json.gz').read_bytes()))
logical=[(r,B/r['path'])for r in layout];hist=Path('/tmp/deepprimal-index-2102')
for r in prior['members']:logical.append(({**r,'path':'historical/'+r['path']},hist/r['path']))
objects={};new=[];split=0
for row,p in logical:
 b=p.read_bytes();assert len(b)==row['bytes']and sha(b)==row['sha256'];row={k:v for k,v in row.items()if k!='object'};parts=[]
 if p.name in ['water-boundary-batch-readbacks.bin','water-boundary-retry.bin']:
  meta=p.with_name('water-boundary-batch.json'if p.name=='water-boundary-batch-readbacks.bin'else'water-boundary-retry.json');records=json.loads(meta.read_text())['readbacks'];offset=0
  for r in records:
   assert r['offset']==offset
   for repeat in range(2):
    n=r['bytes'];data=b[offset:offset+n];assert len(data)==n;key='objects/'+sha(data);objects.setdefault(key,(p,offset,n));parts.append({'object':key,'bytes':n});offset+=n
  assert offset==len(b);row['chunks']=parts;split+=1
 else:
  key='objects/'+sha(b);objects.setdefault(key,(p,0,len(b)));row['object']=key
 new.append(row)
raw=io.BytesIO();members=[]
with tarfile.open(fileobj=raw,mode='w')as t:
 for key,(p,offset,n)in sorted(objects.items()):
  with p.open('rb')as f:f.seek(offset);b=f.read(n)
  assert len(b)==n and key=='objects/'+sha(b);info=tarfile.TarInfo(key);info.size=n;info.mtime=0;info.mode=0o644;t.addfile(info,io.BytesIO(b));members.append({'path':key,'bytes':n,'sha256':sha(b)})
compressed=gzip.compress(raw.getvalue(),mtime=0);parts=[]
for n,offset in enumerate(range(0,len(compressed),524288)):
 path='captures/compact-readbacks.tar.gz.part'+str(n).zfill(3);data=compressed[offset:offset+524288];(E/path).write_bytes(data);parts.append({'path':path,'bytes':len(data),'sha256':sha(data),'git_sha':hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()})
record={'archive':'captures/compact-readbacks.tar.gz','bytes':len(compressed),'sha256':sha(compressed),'parts':parts,'members':members}
# Reconstruct every logical file from the new representation before discarding duplicate archives.
for row in new:
 h=hashlib.sha256();size=0
 for item in row.get('chunks',[{'object':row.get('object'),'bytes':row['bytes']}]):
  p,offset,n=objects[item['object']];assert n==item['bytes']
  with p.open('rb')as f:f.seek(offset);data=f.read(n)
  h.update(data);size+=len(data)
 assert size==row['bytes']and h.hexdigest()==row['sha256']
(E/'pre-compaction-manifests.json.gz').write_bytes(gzip.compress(json.dumps({'capture':old,'prior_boundary':prior,'layout':layout}).encode(),mtime=0))
(E/'capture-bundle.json').write_text(json.dumps(record,indent=2)+'\n');(E/'capture-layout.json.gz').write_bytes(gzip.compress(json.dumps(new).encode(),mtime=0))
for record in [old,prior]:
 for part in record['parts']:
  p=Path(part['path']);assert not p.is_absolute()and'..'not in p.parts;(E/p).unlink()
(E/'prior-boundary-bundle.json').unlink()
report={'pass':True,'original_archive_bytes':old['bytes']+prior['bytes'],'compacted_archive_bytes':len(compressed),'logical_paths_preserved':len(new),'unique_content_objects':len(objects),'raw_readback_files_split_losslessly':split,'every_original_logical_file_hash_reconstructed_before_removing_own_unpublished_duplicates':True,'no_build_native_or_timing_execution':True}
(E/'readback-compaction.json').write_text(json.dumps(report,indent=2)+'\n');shutil.copyfile(Path(__file__),E/'scripts/compact_readbacks.py');print(json.dumps(report,indent=2))
# The replay reads both full-file and segmented objects; historical boundary data now shares them.
p=E/'scripts/replay_checkpoint.py';s=p.read_text();s=s.replace(",('prior-boundary-bundle.json','historical')",'')
s=s.replace("b=objects[row['object']];assert len(b)==row['bytes']", "b=b''.join(objects[x['object']]for x in row['chunks'])if 'chunks'in row else objects[row['object']];assert len(b)==row['bytes']")
p.write_text(s);(R/'replay_template.py').write_text(s)
