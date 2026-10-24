from pathlib import Path
import subprocess,json,hashlib,urllib.request
R=Path(__file__).resolve().parent;T=R/'toolchain';D=T/'graphics-debs';G=T/'graphics-verified'
D.mkdir(exist_ok=False);G.mkdir(exist_ok=False)
with (R/'apt-update.log').open('w') as log:
 subprocess.run(['apt-get','-o','APT::Sandbox::User=root','update'],stdout=log,stderr=subprocess.STDOUT,check=True,timeout=120)
rows=[]
for pin in json.loads((R/'graphics-pins.json').read_text())['packages']:
 raw=subprocess.check_output(['apt-cache','show',pin['name']+'='+pin['version']],text=True).split('\n\n')[0]
 meta=dict(s.split(': ',1) for s in raw.splitlines() if ': ' in s and not s.startswith(' '))
 assert int(meta['Size'])==pin['bytes'] and meta['SHA256']==pin['sha256']
 p=D/Path(meta['Filename']).name;url='https://snapshot.ubuntu.com/ubuntu/20260828T000000Z/'+meta['Filename']
 with urllib.request.urlopen(url,timeout=40) as src,p.open('xb') as out:
  while chunk:=src.read(1024*1024):out.write(chunk)
 assert p.stat().st_size==pin['bytes'] and hashlib.sha256(p.read_bytes()).hexdigest()==pin['sha256'],p
 subprocess.run(['dpkg-deb','-x',str(p),str(G)],check=True)
 rows.append(dict(pin,url=url));print('RESTORED',pin['name'],flush=True)
for i in range(10):
 alias=Path('/tmp/dp'+str(i))
 if not alias.exists() and not alias.is_symlink():alias.symlink_to(G/'usr/bin',target_is_directory=True);break
else:raise AssertionError('No unused private compiler alias')
p=G/'usr/bin/Xvfb';b=p.read_bytes();assert b.count(b'/usr/bin\0')==1
replacement=str(alias).encode()+b'\0';assert len(replacement)==9
out=G/'usr/bin/Xvfb-private';out.write_bytes(b.replace(b'/usr/bin\0',replacement));out.chmod(0o755)
(R/'graphics-restoration.json').write_text(json.dumps({'packages':rows,'private_xvfb_alias':str(alias),'original_xvfb_sha256':hashlib.sha256(b).hexdigest(),'modified_xvfb_sha256':hashlib.sha256(out.read_bytes()).hexdigest()},indent=2)+'\n')
