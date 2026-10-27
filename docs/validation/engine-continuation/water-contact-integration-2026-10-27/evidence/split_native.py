"""Convert the private diagnostic to separately loaded immutable modules to avoid JIJ duplication."""
from pathlib import Path
import json,zipfile,hashlib,tomllib
r=Path('/dev/shm/dp-builder2600');jar=r/'contact-diagnostic-private.jar';mods=r/'native-modules';mods.mkdir(exist_ok=True);out=r/'contact-split-private.jar';rows=[]
with zipfile.ZipFile(jar) as src,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as dst:
 for n in src.namelist():
  data=src.read(n)
  if n.startswith('META-INF/jarjar/'):
   if n.endswith('.jar'):
    p=mods/Path(n).name;p.write_bytes(data)
    with zipfile.ZipFile(p) as z:
     if z.testzip() is not None:raise RuntimeError('Module CRC')
     mid=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())['mods'][0]['modId'] if 'META-INF/neoforge.mods.toml' in z.namelist() else None
    rows.append({'path':str(p),'mod_id':mid,'sha256':hashlib.sha256(data).hexdigest(),'bytes':len(data)})
  else:dst.writestr(n,data)
if len(rows)!=6:raise RuntimeError('Expected all six modules')
(r/'split-modules.json').write_text(json.dumps(rows,indent=2)+'\n');jar.unlink();out.rename(jar)
