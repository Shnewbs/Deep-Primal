from pathlib import Path
import urllib.request,urllib.error,json,hashlib,subprocess,time
R=Path(__file__).resolve().parent;T=R/'toolchain';D=T/'graphics-debs-live2';G=T/'graphics-verified';D.mkdir(exist_ok=False);assert not any(G.iterdir());old=json.loads((R/'graphics-pins-source.json').read_text())['packages'];current={p['name']:p for p in json.loads((R/'graphics-pins-current.json').read_text())['packages']};rows=[]
for pin in old:
 canonical='https://archive.ubuntu.com/ubuntu/pool/'+pin['url'].split('/pool/',1)[1];chosen=dict(pin,url=canonical)
 try:response=urllib.request.urlopen(canonical,timeout=30)
 except urllib.error.HTTPError as e:
  if e.code!=404:raise
  chosen=current[pin['name']];response=urllib.request.urlopen(chosen['url'],timeout=30)
 p=D/Path(chosen['url']).name
 with response,p.open('xb')as out:
  count=0
  while chunk:=response.read(1024*1024):
   count+=len(chunk);assert count<=chosen['bytes'];out.write(chunk)
 assert p.stat().st_size==chosen['bytes']and hashlib.sha256(p.read_bytes()).hexdigest()==chosen['sha256']
 subprocess.run(['dpkg-deb','-x',str(p),str(G)],check=True);rows.append(dict(chosen,verified=True,changed_from_prior=chosen['sha256']!=pin['sha256']));print('RESTORED',chosen['name'],chosen['version'],flush=True)
for i in range(10):
 alias=Path('/tmp/dp'+str(i))
 if not alias.exists()and not alias.is_symlink():alias.symlink_to(G/'usr/bin',target_is_directory=True);break
else:raise AssertionError('No private compiler alias')
p=G/'usr/bin/Xvfb';b=p.read_bytes();assert b.count(b'/usr/bin\0')==1;replacement=str(alias).encode()+b'\0';assert len(replacement)==9;q=G/'usr/bin/Xvfb-private';q.write_bytes(b.replace(b'/usr/bin\0',replacement));q.chmod(0o755)
(R/'graphics-restoration.json').write_text(json.dumps({'packages':rows,'private_xvfb_alias':str(alias),'original_xvfb_sha256':hashlib.sha256(b).hexdigest(),'modified_xvfb_sha256':hashlib.sha256(q.read_bytes()).hexdigest(),'scope':'Current official Ubuntu metadata fallback only for removed old versions; private display changes must be distinguished from prior captures.'},indent=2)+'\n')
