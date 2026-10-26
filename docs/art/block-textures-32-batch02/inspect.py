from pathlib import Path
import json,hashlib,statistics
from PIL import Image
root=Path(__file__).parent
m=json.loads((root/'manifest.json').read_text())
config=json.loads((root/'assets.json').read_text())
assert sorted(a['id'] for a in m['assets'])==sorted(a['id'] for a in config['assets']),'incomplete or unexpected export set'
results=[]
seen=set()
for a in m['assets']:
 p=root/a['tile']
 im=Image.open(p)
 assert im.size==(32,32),(a['id'],'size')
 assert im.mode in ('RGB','RGBA'),(a['id'],'mode')
 rgba=im.convert('RGBA')
 rgb=list(rgba.get_flattened_data() if hasattr(rgba,'get_flattened_data') else rgba.getdata())
 assert all(r==g==b for r,g,b,alpha in rgb),(a['id'],'not neutral')
 assert a['sha256']==hashlib.sha256(p.read_bytes()).hexdigest(),(a['id'],'hash')
 master=root/a['master']
 if master.exists():assert a['master_sha256']==hashlib.sha256(master.read_bytes()).hexdigest(),(a['id'],'master hash')
 assert a['sha256'] not in seen,(a['id'],'duplicate')
 seen.add(a['sha256'])
 opaque=sum(px[3]==255 for px in rgb)
 if a['alpha_mode']=='binary_cutout':
  assert all(px[3] in (0,255) for px in rgb),(a['id'],'nonbinary alpha')
  assert 10<opaque<1000,(a['id'],'missing silhouette/alpha')
 else:assert opaque==1024,(a['id'],'unexpected alpha')
 get=lambda x,y: rgb[y*32+x][0]
 interior=[abs(get(x,y)-get(x+1,y)) for y in range(32) for x in range(31)]+[abs(get(x,y)-get(x,y+1)) for y in range(31) for x in range(32)]
 seam_x=statistics.mean(abs(get(31,y)-get(0,y)) for y in range(32))
 seam_y=statistics.mean(abs(get(x,31)-get(x,0)) for x in range(32))
 interior_mean=statistics.mean(interior)
 ratio=max(seam_x,seam_y)/max(interior_mean,1)
 values=[px[0] for px in rgb if px[3]]
 results.append({'id':a['id'],'dimensions_and_channels':True,'distinct_export':True,'opaque_texels':opaque,'grayscale_range':[min(values),max(values)],'interior_mean_step':round(interior_mean,3),'wrap_x_mean_step':round(seam_x,3),'wrap_y_mean_step':round(seam_y,3),'max_wrap_to_interior_ratio':round(ratio,3),'repeat_attention':a['repeat']=='both_axes' and ratio>2.5})
out={'assets_checked':len(results),'pass_dimensions_channels_alpha_hashes':True,'repeat_note':'Wrap statistics are screening data, not proof of seamless artwork; inspect actual tiled proofs. No native render validation claimed.','assets':results}
(root/'validation.json').write_text(json.dumps(out,indent=2)+'\n')
print(json.dumps({'checked':len(results),'attention':[a['id'] for a in results if a['repeat_attention']]}))

