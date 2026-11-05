from pathlib import Path
from PIL import Image,ImageDraw
import datetime,json
R=Path(__file__).resolve().parent;B=Path('/tmp/deepprimal-direction-blend-0645')
cases=['vulkan-order-blend-cell-final','vulkan-order-blend-cell-final-packaged','vulkan-reuse-gl-blend-final']
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
if (B/cases[0]/'direction-blend-335.png').exists():
 sheet=Image.new('RGB',(896,328),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,(name,d) in enumerate([('baseline',B/'vulkan-blend-baseline'),('blend',B/cases[0])]):
  for col,label in enumerate(['old','target','0ms','25ms','50ms','75ms','100ms']):
   im=Image.open(d/f'direction-blend-{col}.png').convert('RGB').resize((128,128),Image.Resampling.NEAREST)
   sheet.paste(im,(col*128,row*164+32));draw.text((col*128+3,row*164+5),name+' '+label,fill='white')
 sheet.save(R/'blend-before-after.png')
 sheet=Image.new('RGB',(896,656),'#171a1e');draw=ImageDraw.Draw(sheet)
 for row,g in enumerate([32,34,36,38]):
  for col,label in enumerate(['old','target','0ms','25ms','50ms','75ms','100ms']):
   im=Image.open(B/cases[0]/f'direction-blend-{g*7+col}.png').convert('RGB').resize((128,128),Image.Resampling.NEAREST)
   sheet.paste(im,(col*128,row*164+32));draw.text((col*128+3,row*164+5),f'mip dir{g%16} '+label,fill='white')
 sheet.save(R/'blend-mip-contact.png')
print('contact sheets generated for completed sessions only')
