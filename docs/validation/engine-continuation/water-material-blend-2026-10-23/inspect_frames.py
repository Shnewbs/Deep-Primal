from pathlib import Path
from PIL import Image,ImageDraw
import datetime,json
R=Path(__file__).resolve().parent;B=Path('/tmp/deepprimal-material-blend-0828')
cases=['vulkan-order-material-final','vulkan-order-material-final-packaged','vulkan-reuse-gl-material-final']
for case in cases:
 d=B/case
 if not (d/'result.json').exists() or not (d/'screenshots/order-live-frame-3.png').exists():continue
 sheet=Image.new('RGB',(1152,720),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,profile in enumerate(['','waves-enabled','lod-disabled']):
  for col in range(3):
   f=d/profile/'screenshots'/f'order-live-frame-{col+1}.png'
   im=Image.open(f).convert('RGB');im.thumbnail((384,216));x=col*384;y=row*240
   sheet.paste(im,(x,y+24));draw.text((x+4,y+5),f'{profile or "waves-off-lod-on"} frame {col+1}',fill='white')
 sheet.save(R/(case+'-contact.png'))
if (B/cases[0]/'material-0-0-5.png').exists():
 sheet=Image.new('RGB',(768,328),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,(label,d) in enumerate([('baseline',B/'vulkan-material-baseline'),('candidate',B/cases[0])]):
  for col,phase in enumerate(['old','0ms','25ms','50ms','75ms','100ms']):
   im=Image.open(d/f'material-0-0-{col}.png').convert('RGB').resize((128,128),Image.Resampling.NEAREST);sheet.paste(im,(col*128,row*164+32));draw.text((col*128+3,row*164+5),label+' '+phase,fill='white')
 sheet.save(R/'material-before-after.png')
 sheet=Image.new('RGB',(768,656),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,route in enumerate([0,16,32,48]):
  for col,phase in enumerate(['old','0ms','25ms','50ms','75ms','100ms']):
   im=Image.open(B/cases[0]/f'material-1-{route}-{col}.png').convert('RGB').resize((128,128),Image.Resampling.NEAREST);sheet.paste(im,(col*128,row*164+32));draw.text((col*128+3,row*164+5),f'mip route{route} '+phase,fill='white')
 sheet.save(R/'material-mip-contact.png')
print('completed contact sheets only')
