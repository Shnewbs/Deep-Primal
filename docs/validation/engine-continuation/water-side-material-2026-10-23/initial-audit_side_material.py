"""Independent raw-vertex and weighted-pixel verification for actual uploaded material draws."""
from pathlib import Path
import json,struct,math
from PIL import Image
LINE='WATER_SIDE_MATERIAL_NATIVE_PASS routes=320 renders=3840 samples=3200 readbacks=1929 readbackBytes=1666656 stillToFlow=true flowToStill=true mipControls=true nearFarCells=true verticalSidesAndUnderside=true repeatedQueuedStale=true stillHistorySparse=true lightPreserved=true historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionSideUploadsAndShaderNotFullStreamAcceptance'
def f32(x):return struct.unpack('<f',struct.pack('<f',x))[0]
def audit_side_material(d,c):
 assert (d/'water-side-material-pass.txt').read_text().strip()==LINE and c.count(LINE)==1
 result=(d/'water-side-material-result.txt').read_text();assert 'pass=true ' in result and 'badVertices=0 ' in result and 'channelsBeyondTolerance=0 ' in result and 'stillHistoryViolations=0 ' in result
 raw=(d/'water-side-material-upload.bin').read_bytes();rows=json.loads((d/'water-side-material-upload-details.json').read_text());tiles=json.loads((d/'water-side-material-atlas.json').read_text())
 assert len(raw)==1666656 and len(rows)==1929
 for n,r in enumerate(rows):
  assert r['offset']==n*864
  if r['route']<320:
   route=r['route'];slot=r['slot'];a=16 if route%64%32<16 else route%16;b=route%16 if route%64%32<16 else 16
   assert r['source']==a and r['target']==(a if slot==0 else b) and r['active']==(0<slot<5) and r['cell']==(3 if route%64<32 else 15) and r['origin']==(0 if route%64<32 else 29999984)
  if r['route']==320 and r['slot'] in [3,5,6]:assert r['cleanup']==(201 if r['slot']==3 else 301)
  for v in range(24):
   at=n*864+v*36;l,sky=struct.unpack_from('<HH',raw,at+28);active=bool(sky&32768);want=r['active']
   assert l&255==176 and sky&255==240 and active==want
   assert raw[at+15]==((r['cell']|(r['cell']<<4)) if want else 255)
   if active:assert l>>11==r['source'] and (sky>>8)&127==r['age']
   face=v//4
   x,y,z=struct.unpack_from('<fff',raw,at);x-=r['cell'];z-=r['cell']
   if face==0:
    assert y==1;u=x;w=z
    if r['target']<16:
     a=r['target']*math.pi/8;fx=f32(math.cos(a));fz=f32(math.sin(a))
     xx=r['origin']+r['cell'];zz=-r['origin']+r['cell'];pu=.5*(fz*xx-fx*zz);pv=.5*(fx*xx+fz*zz)
     u=.5+.5*(fz*(x-.5)-fx*(z-.5))+f32(pu-math.floor(pu));w=.5+.5*(fx*(x-.5)+fz*(z-.5))+f32(pv-math.floor(pv))
   else:
    scale=.5 if r['target']<16 else 1;u=(z if face in [2,4] else x)*scale;w=(1-y)*scale
   t=tiles['still' if r['target']==16 else 'flow'];actualU,actualV=struct.unpack_from('<ff',raw,at+16)
   assert abs(actualU-(t['u0']+u*t['us']))<=1e-6 and abs(actualV-(t['v0']+w*t['vs']))<=1e-6
   assert raw[at+32]==127 and raw[at+33]==127 and raw[at+35]==0
   signed=raw[at+34] if raw[at+34]<128 else raw[at+34]-256
   assert signed==-min(r['time'],100)
 details=json.loads((d/'water-side-material-pixel-details.json').read_text());assert len(details)==3200
 for r in details:assert r['maxChannelError']<=1 and r['channelsBeyondTolerance']==0
 for profile in range(2):
  for route in range(320):
   a=Image.open(d/f'side-material-{profile}-{route}-0.png').convert('RGBA');b=Image.open(d/f'side-material-{profile}-{route}-5.png').convert('RGBA')
   size=32 if profile==0 else 8;assert a.size==b.size==(size,size)
   if profile==0:assert a.tobytes()!=b.tobytes() and len(a.getcolors(2048))>1
    assert all(not (r>1 and bl>1) and alpha==255 for r,g,bl,alpha in a.getdata()),('atlas bleed',route,0)
    assert all(not (r>1 and bl>1) and alpha==255 for r,g,bl,alpha in b.getdata()),('atlas bleed',route,5)
   if profile==1:
    shade=229 if route//64 in [0,2] else 204 if route//64 in [1,3] else 127
    assert all(abs(g-shade)<=1 and r<=1 and bl<=1 and alpha==255 for r,g,bl,alpha in a.getdata())
    assert all(abs(g-shade)<=1 and r<=1 and bl<=1 and alpha==255 for r,g,bl,alpha in b.getdata())
   aa=a.tobytes();bb=b.tobytes()
   for phase in range(5):
    image=Image.open(d/f'side-material-{profile}-{route}-{phase+1}.png').convert('RGBA');cc=image.tobytes();t=phase*.25
    assert image.getchannel('A').getextrema()==(255,255)
    assert max(abs(x-round(y*(1-t)+z*t)) for x,y,z in zip(cc,aa,bb))<=1
 return {'routes':320,'shader_renders':3840,'weighted_RGBA_samples':3200,'actual_upload_readbacks':1929,'readback_bytes':1666656,'pass':True}
