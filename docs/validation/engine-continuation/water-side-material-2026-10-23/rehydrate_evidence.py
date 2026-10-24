"""Restore archived pixels/logs for offline strict audits; no game/world mutation."""
from pathlib import Path
import zipfile,gzip,json,subprocess,hashlib,sys
from PIL import Image
R=Path(__file__).resolve().parent
CASES=['vulkan-order-side-material-normalized-final','vulkan-order-side-material-normalized-final-packaged','vulkan-reuse-gl-side-material-normalized-final']
if '--all' in sys.argv:CASES=[p.name.removesuffix('-controlled-pixels.zip') for p in sorted(R.glob('*-controlled-pixels.zip'))]
for case in CASES:
 d=R/case
 for record in json.loads((R/'compressed-data-manifest.json').read_text()):
  if not record['path'].startswith(case+'/'):continue
  encoded=(R/record['gzip_path']).read_bytes();assert hashlib.sha256(encoded).hexdigest()==record['gzip_sha256']
  raw=gzip.decompress(encoded);assert len(raw)==record['bytes'] and hashlib.sha256(raw).hexdigest()==record['sha256']
  target=R/record['path'];target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
 with zipfile.ZipFile(R/(case+'-controlled-pixels.zip')) as z:
  index=json.loads(z.read('manifest.json'))
  assert z.testzip() is None
  for name,digest in index.items():
   assert Path(name).name==name and name.endswith('.png')
   b=z.read('pixels/'+digest+'.png');assert hashlib.sha256(b).hexdigest()==digest
   (d/name).write_bytes(b)
 raw=gzip.decompress((d/'console-final.gz').read_bytes());seal=json.loads((d/'sealed-console.json').read_text())
 assert hashlib.sha256(raw).hexdigest()==seal['raw_sha256']
 (d/'console.log').write_bytes(raw)
 if not (d/'frame-video-provenance.json').exists():continue
 for p in [d,d/'waves-enabled',d/'lod-disabled']:
  record=json.loads((p/'frame-video-provenance.json').read_text())
  video=d/record['video'];assert hashlib.sha256(video.read_bytes()).hexdigest()==record['video_sha256']
  raw=subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-i',str(video),'-frames:v','3','-f','rawvideo','-pix_fmt','rgb24','pipe:1'],check=True,capture_output=True,timeout=30).stdout
  at=0
  for row in record['frames']:
   n=row['width']*row['height']*3;b=raw[at:at+n];at+=n
   assert hashlib.sha256(b).hexdigest()==row['rgb_sha256']
   target=d/row['png'];target.parent.mkdir(parents=True,exist_ok=True)
   Image.frombytes('RGB',(row['width'],row['height']),b).convert('RGBA').save(target)
  assert at==len(raw)
 print(case,'restored sealed log, controlled PNGs and9 byte-exact RGB frames; PNG compression may differ')
