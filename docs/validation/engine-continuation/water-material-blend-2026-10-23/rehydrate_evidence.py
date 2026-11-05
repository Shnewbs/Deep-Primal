"""Restore archived pixels/logs for offline strict audits; no game/world mutation."""
from pathlib import Path
import zipfile,gzip,json,subprocess,hashlib
from PIL import Image
R=Path(__file__).resolve().parent
CASES=['vulkan-order-material-final','vulkan-order-material-final-packaged','vulkan-reuse-gl-material-final']
for case in CASES:
 d=R/case
 with zipfile.ZipFile(R/(case+'-controlled-pixels.zip')) as z:
  assert all(Path(n).name==n and n.endswith('.png') for n in z.namelist())
  z.extractall(d)
 raw=gzip.decompress((d/'console-final.gz').read_bytes());seal=json.loads((d/'sealed-console.json').read_text())
 assert hashlib.sha256(raw).hexdigest()==seal['raw_sha256']
 (d/'console.log').write_bytes(raw)
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
