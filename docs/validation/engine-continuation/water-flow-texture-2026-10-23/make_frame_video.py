from pathlib import Path
from PIL import Image
import sys,subprocess,hashlib,json
R=Path(__file__).resolve().parent
for case in sys.argv[1:]:
 d=R/case;profiles=[]
 for name in ['', 'waves-enabled','lod-disabled']:
  p=d/name;frames=[p/'screenshots'/('order-live-frame-'+str(i)+'.png') for i in range(1,4)];rows=[];expected=[];size=None
  for f in frames:
   with Image.open(f) as im:
    assert im.getchannel('A').getextrema()==(255,255)
    size=im.size;data=im.convert('RGB').tobytes();expected.append(data)
    rows.append({'png':str(f.relative_to(d)),'png_sha256':hashlib.sha256(f.read_bytes()).hexdigest(),'rgb_sha256':hashlib.sha256(data).hexdigest(),'width':im.width,'height':im.height,'alpha_all_255':True})
  video=p/'order-live-frames.webm'
  cmd=['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-framerate','5','-start_number','1','-i',str(p/'screenshots/order-live-frame-%d.png'),'-frames:v','3','-an','-c:v','libvpx-vp9','-lossless','1','-pix_fmt','gbrp','-colorspace','rgb','-threads','2',str(video)]
  subprocess.run(cmd,check=True,timeout=60)
  decoded=subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-i',str(video),'-frames:v','3','-f','rawvideo','-pix_fmt','rgb24','pipe:1'],check=True,stdout=subprocess.PIPE,timeout=60).stdout
  assert decoded==b''.join(expected),(case,name,'decoded pixels differ')
  record={'profile':name or 'waves-off-lod-on','video':str(video.relative_to(d)),'video_bytes':video.stat().st_size,'video_sha256':hashlib.sha256(video.read_bytes()).hexdigest(),'frames':rows,'decoded_rgb_pixels_bit_exact':True,'playback_fps':5,'real_cadence_source':str((p/'water-page-order-frames.txt').relative_to(d)),'scope':'Three actual consecutive water-stage frames with opaque alpha. VP9 RGB lossless; playback timing is illustrative, not measured FPS or independent simulation cadence. Pixel identity verified against source screenshots.'}
  (p/'frame-video-provenance.json').write_text(json.dumps(record,indent=2)+'\n');profiles.append(record)
  print(case,name or 'base','lossless frames=3 video_bytes='+str(video.stat().st_size))
 (d/'frame-videos.json').write_text(json.dumps(profiles,indent=2)+'\n')
