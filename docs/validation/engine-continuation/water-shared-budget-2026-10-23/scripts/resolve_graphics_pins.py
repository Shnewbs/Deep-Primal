from pathlib import Path
import urllib.request,lzma,json,hashlib,re,time
R=Path(__file__).resolve().parent;wanted={p['name']for p in json.loads((R/'graphics-pins-source.json').read_text())['packages']};found={};indexes=[]
for suite in ['noble','noble-updates']:
 for component in ['main','universe']:
  url=f'https://archive.ubuntu.com/ubuntu/dists/{suite}/{component}/binary-amd64/Packages.xz'
  with urllib.request.urlopen(url,timeout=30)as response:b=response.read(40000000)
  text=lzma.decompress(b).decode();indexes.append({'url':url,'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest()})
  for block in text.split('\n\n'):
   first=block.split('\n',1)[0]
   if not first.startswith('Package: ')or first[9:]not in wanted:continue
   values={line.split(': ',1)[0]:line.split(': ',1)[1]for line in block.splitlines()if ': 'in line and not line.startswith(' ')}
   found[values['Package']]={'name':values['Package'],'version':values['Version'],'bytes':int(values['Size']),'sha256':values['SHA256'],'url':'https://archive.ubuntu.com/ubuntu/'+values['Filename'],'metadata_source':url}
assert set(found)==wanted,(wanted-set(found))
(R/'graphics-pins-current.json').write_text(json.dumps({'packages':list(found.values()),'metadata_indexes':indexes,'old_snapshot_error':'HTTPS CONNECT returned403; original xserver binaries absent from live mirror. Resolve current official Noble package metadata, no old-driver comparative claim.'},indent=2)+'\n')
print([(p['name'],p['version'])for p in found.values()])
