#!/usr/bin/env python3
"""Author exact lookup textures, not painted approximations. Python 3 + Pillow."""
import csv
import hashlib
import io
import json
import math
from pathlib import Path
import shutil
from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
PACK = ROOT / 'pack'
CFG = json.loads((HERE / 'palette.json').read_text())
INPUTS = json.loads((HERE / 'engine-inputs.json').read_text())

def rgb(s):
    return tuple(int(s[i:i+2], 16) for i in (0, 2, 4))

def smooth(t):
    return t*t*(3-2*t)

def mix(a, b, t):
    return tuple(x+(y-x)*t for x, y in zip(a,b))

def curve(colors, t):
    knots = CFG['temperature_knots']
    for i in range(len(knots)-1):
        if t <= knots[i+1]:
            w = smooth((t-knots[i])/(knots[i+1]-knots[i]))
            return mix(rgb(colors[i]), rgb(colors[i+1]), w)
    return rgb(colors[-1])

def tint(kind, t, humidity):
    spec = CFG['maps'][kind]
    value = mix(curve(spec['dry'], t), curve(spec['wet'], t), smooth(humidity))
    return tuple(math.floor(x+0.5) for x in value)

def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2)+'\n')

def sample(im, t, h):
    return im.getpixel((int((1-t)*255), int((1-h*t)*255)))

def create_maps():
    maps={}
    for kind in CFG['maps']:
        im=Image.new('RGB',(256,256))
        px=im.load()
        for y in range(256):
            effective_moisture=1-y/255
            for x in range(256):
                t=1-x/255
                h=min(1,effective_moisture/t) if t else 0
                px[x,y]=tint(kind,t,h)
        path=PACK/f'assets/minecraft/textures/colormap/{kind}.png'
        path.parent.mkdir(parents=True,exist_ok=True)
        im.save(path,optimize=True)
        maps[kind]=im
    return maps

def create_water():
    out={}
    for group in CFG['water_groups']:
        for biome in group['biomes']:
            key='minecraft:'+biome
            assert key not in out, key
            data={'color':int(group['color'],16),'fog_color':int(group['fog'],16),'clarity':group['clarity']}
            out[key]=data
            write_json(PACK/f'assets/minecraft/deepprimal/water_palette/{biome}.json',data)
    assert set(out)==set(INPUTS['palette']), 'Engine palette coverage must be exact'
    return out

def font(size,bold=False):
    name='DejaVuSans'+('-Bold' if bold else '')+'.ttf'
    for path in ['/usr/share/fonts/truetype/dejavu/'+name,name]:
        try:
            return ImageFont.truetype(path,size)
        except OSError:
            pass
    return ImageFont.load_default(size=size)

def preview(maps):
    canvas=Image.new('RGB',(1500,1190),'#f1f0e7')
    d=ImageDraw.Draw(canvas)
    d.rectangle((0,0,1500,170),fill='#1c302b')
    d.text((64,38),'DEEP PRIMAL',font=font(42,True),fill='#f2f0df')
    d.text((66,103),'NATURAL COLORMAPS  /  26.1.2  /  v1.0',font=font(21),fill='#b8c5b2')
    for i,(kind,im) in enumerate(maps.items()):
        x=66+i*480
        d.text((x,205),kind.replace('_',' ').upper(),font=font(22,True),fill='#263c31')
        canvas.paste(im.resize((320,320),Image.Resampling.NEAREST),(x,248))
        d.text((x,580),'HOT  →  COLD',font=font(16),fill='#586559')
        d.text((x,608),'Wet above · dry below',font=font(16),fill='#586559')
        labels=[('Boreal',.25,.8),('Temperate',.7,.8),('Dryland',1,.1),('Tropical',.95,.9)]
        for j,(label,t,h) in enumerate(labels):
            sx=x+j*99
            d.rounded_rectangle((sx,650,sx+86,697),radius=5,fill=sample(im,t,h))
            d.text((sx,708),label,font=font(13),fill='#3d4e41')
    d.line((66,758,1434,758),fill='#c5cbbd',width=2)
    d.text((66,785),'WATER + UNDERWATER FOG',font=font(24,True),fill='#263c31')
    groups={g['label']:g for g in CFG['water_groups']}
    labels=['River water','Alpine freshwater','Warm reef water','Deep sea','Peat wetland','Mangrove brackish water','Lush cave pool','Limestone cave pool']
    for i,label in enumerate(labels):
        x=66+(i%4)*350; y=840+(i//4)*128; g=groups[label]
        d.rounded_rectangle((x,y,x+320,y+68),radius=6,fill='#'+g['color'])
        d.rectangle((x+228,y,x+320,y+68),fill='#'+g['fog'])
        d.text((x,y+79),label,font=font(15),fill='#3d4e41')
    d.text((66,1128),'3 exact 256 × 256 lookup textures  ·  40 biome palettes  ·  swatches, not in-game screenshots',font=font(18),fill='#586559')
    path=PACK/'authoring/preview.png'; path.parent.mkdir(parents=True,exist_ok=True)
    canvas.save(path,optimize=True)
    # Resource-pack icon is original geometric artwork, no vanilla texture copy.
    icon=Image.new('RGB',(128,128),'#1c302b'); p=ImageDraw.Draw(icon)
    p.polygon([(0,94),(41,28),(91,94)],fill='#8d9e76')
    p.polygon([(41,28),(61,55),(28,49)],fill='#d7dfcf')
    p.polygon([(52,94),(91,43),(128,94)],fill='#577c60')
    p.polygon([(0,98),(33,88),(64,102),(97,93),(128,100),(128,128),(0,128)],fill='#43858b')
    icon.save(PACK/'pack.png')

def validate(maps,water):
    checks={}
    for kind,im in maps.items():
        path=PACK/f'assets/minecraft/textures/colormap/{kind}.png'
        with Image.open(path) as decoded:
            decoded.load()
            assert decoded.mode=='RGB' and decoded.size==(256,256)
            assert decoded.tobytes()==im.tobytes()
        assert im.getpixel((255,255))==rgb(CFG['maps'][kind]['dry'][0])
        assert im.getpixel((0,255))==rgb(CFG['maps'][kind]['dry'][-1])
        assert im.getpixel((0,0))==rgb(CFG['maps'][kind]['wet'][-1])
        assert all(sum(im.getpixel((x,y)))>0 for y in range(256) for x in range(256))
        max_step=max(abs(a-b) for y in range(256) for x in range(256)
                     for nx,ny in [(min(255,x+1),y),(x,min(255,y+1))]
                     for a,b in zip(im.getpixel((x,y)),im.getpixel((nx,ny))))
        assert max_step<=4, (kind,max_step)
        checks[kind]={'size':[256,256],'mode':'RGB','pixels':65536,'max_adjacent_channel_step':max_step,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
    for key,value in water.items():
        assert set(value)=={'color','fog_color','clarity'}
        assert all(type(value[k]) is int and 0<=value[k]<=0xffffff for k in ('color','fog_color'))
        assert .1<=value['clarity']<=8
    return {'source_commit':INPUTS['source_commit'],'maps':checks,'water_palette_count':len(water),'engine_biome_coverage':'40/40','resource_format':84,'native_client_visual_test':False,'mod_jar_build':False}

def main():
    PACK.mkdir(parents=True,exist_ok=True)
    write_json(PACK/'pack.mcmeta',{'pack':{'description':'Deep Primal | Natural Colormaps v1.0 — grass, foliage and biome water','min_format':84,'max_format':84}})
    maps=create_maps(); water=create_water(); preview(maps)
    authoring=PACK/'authoring'; authoring.mkdir(exist_ok=True)
    for name in ['palette.json','build_colormaps.py','engine-inputs.json','LookupCheck.java']:
        shutil.copyfile(HERE/name,authoring/name)
    readme=HERE/'README.txt'
    if not readme.exists():
        readme=HERE.parent/'README.txt'
    shutil.copyfile(readme,PACK/'README.txt')
    out=io.StringIO(); writer=csv.writer(out)
    writer.writerow(['biome','water_hex','fog_hex','clarity_multiplier'])
    for biome,p in sorted(water.items()):
        writer.writerow([biome,f"#{p['color']:06x}",f"#{p['fog_color']:06x}",p['clarity']])
    (authoring/'water-palettes.csv').write_text(out.getvalue())
    results=validate(maps,water)
    write_json(authoring/'validation.json',results)
    print(json.dumps(results,indent=2))

if __name__=='__main__':
    main()
