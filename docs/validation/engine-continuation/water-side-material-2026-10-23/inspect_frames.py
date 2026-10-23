from pathlib import Path
from PIL import Image,ImageDraw
import datetime,json
R=Path(__file__).resolve().parent;B=Path('/tmp/deepprimal-side-material-0930')
cases=['vulkan-order-side-material-normalized-final','vulkan-order-side-material-normalized-final-packaged','vulkan-reuse-gl-side-material-normalized-final']
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

for label,case in [('baseline','vulkan-side-material-baseline'),('candidate',cases[0])]:
 d=B/case
 if not (d/'side-material-0-0-5.png').exists():continue
 sheet=Image.new('RGB',(768,800),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,route in enumerate([0,64,128,192,256]):
  for col,phase in enumerate(['old','0ms','25ms','50ms','75ms','100ms']):
   im=Image.open(d/f'side-material-0-{route}-{col}.png').convert('RGB').resize((128,128),Image.Resampling.NEAREST);sheet.paste(im,(col*128,row*160+25));draw.text((col*128+3,row*160+5),f'face{row+1} '+phase,fill='white')
 sheet.save(R/(label+'-side-contact.png'))
d=B/cases[0]
if (d/'side-material-0-320-5.png').exists():
 sheet=Image.new('RGBA',(640,640),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,extra in enumerate([0,4,8,12]):
  for col in range(5):
   im=Image.open(d/f'side-material-0-{320+extra}-{col+1}.png').convert('RGBA').resize((128,128),Image.Resampling.NEAREST);sheet.alpha_composite(im,(col*128,row*160+25));draw.text((col*128+3,row*160+5),f'rising side{row+1} {col*25}ms',fill='white')
 sheet.convert('RGB').save(R/'height-material-contact.png')
print('completed available contact sheets')
