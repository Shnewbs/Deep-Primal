from pathlib import Path
import urllib.request,json,hashlib,subprocess,time
R=Path(__file__).resolve().parent;T=R/'toolchain';D=T/'graphics-debs';G=T/'graphics-verified'
D.mkdir(exist_ok=False);G.mkdir(exist_ok=False);rows=[];started=time.monotonic()
for pin in json.loads((R/'graphics-pins-source.json').read_text())['packages']:
    p=D/Path(pin['url']).name
    with urllib.request.urlopen(pin['url'],timeout=40) as response,p.open('xb') as out:
        size=0
        while chunk:=response.read(1024*1024):
            size+=len(chunk)
            if size>pin['bytes'] or time.monotonic()-started>300:raise ValueError('Restore bound')
            out.write(chunk)
    assert p.stat().st_size==pin['bytes'] and hashlib.sha256(p.read_bytes()).hexdigest()==pin['sha256']
    subprocess.run(['dpkg-deb','-x',str(p),str(G)],check=True)
    rows.append(pin);print('RESTORED',pin['name'],flush=True)
for i in range(10):
    alias=Path('/tmp/dp'+str(i))
    if not alias.exists() and not alias.is_symlink():alias.symlink_to(G/'usr/bin',target_is_directory=True);break
else:raise AssertionError('No unused private compiler alias')
p=G/'usr/bin/Xvfb';b=p.read_bytes();assert b.count(b'/usr/bin\0')==1
replacement=str(alias).encode()+b'\0';assert len(replacement)==9
out=G/'usr/bin/Xvfb-private';out.write_bytes(b.replace(b'/usr/bin\0',replacement));out.chmod(0o755)
(R/'graphics-restoration.json').write_text(json.dumps({'packages':rows,'private_xvfb_alias':str(alias),'original_xvfb_sha256':hashlib.sha256(b).hexdigest(),'modified_xvfb_sha256':hashlib.sha256(out.read_bytes()).hexdigest()},indent=2)+'\n')
