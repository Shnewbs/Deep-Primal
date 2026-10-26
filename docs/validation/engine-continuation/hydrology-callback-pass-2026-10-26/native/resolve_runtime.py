from pathlib import Path
import json,zipfile,hashlib
N=Path(__file__).resolve().parent;R=Path('/workspace/scratch/a22609fabda0');cache=R/'builder1700/gradle-cache'
manifest=cache/'caches/neoformruntime/artifacts/minecraft_26.1.2_version_manifest.json'
userdev=next(cache.rglob('neoforge-26.1.2.122-userdev.jar'))
with zipfile.ZipFile(userdev) as z:config=json.loads(z.read('config.json'))
coords={':'.join(v['name'].split(':')[:2]):v['name'].split(':') for v in json.loads(manifest.read_text())['libraries']}
for name in config['libraries']:coords[':'.join(name.split(':')[:2])]=name.split(':')
allcp=Path('/dev/shm/dp-builder1905-oaqp2q1h/classpath.txt').read_text().strip().split(':');chosen=[allcp[2].replace('26.1.2.122.jar','26.1.2.122-merged.jar')]
for p in allcp[3:]:
 bits=Path(p).parts
 if 'files-2.1' not in bits:continue
 i=bits.index('files-2.1');group,artifact,version=bits[i+1:i+4];c=coords.get(group+':'+artifact)
 if c and c[2]==version:chosen.append(p)
chosen.append(str(next(cache.rglob('neoforge-26.1.2.122-universal.jar'))))
(N/'classpath.txt').write_text(':'.join(chosen))
(N/'runtime-resolution.json').write_text(json.dumps({'selection':'Minecraft26.1.2 version manifest plus NeoForge26.1.2.122 userdev runtime libraries; userdev version wins','manifest_sha256':hashlib.sha256(manifest.read_bytes()).hexdigest(),'userdev_sha256':hashlib.sha256(userdev.read_bytes()).hexdigest(),'classpath_sha256':{p:hashlib.sha256(Path(p).read_bytes()).hexdigest() for p in chosen}},indent=2))
print('RESOLVED_NATIVE_CLASSPATH',len(chosen))
