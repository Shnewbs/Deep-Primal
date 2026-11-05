"""Independent raw-vertex and weighted-pixel verification for actual uploaded material draws."""
from pathlib import Path
import json,struct,math
from PIL import Image
LINE='WATER_MATERIAL_NATIVE_PASS routes=64 renders=768 samples=640 readbacks=393 readbackBytes=282960 stillToFlow=true flowToStill=true mipControls=true nearFarCells=true repeatedQueuedStale=true stillHistorySparse=true lightPreserved=true historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploadsAndShaderNotFullStreamAcceptance'
def f32(x):return struct.unpack('<f',struct.pack('<f',x))[0]
def audit_material(d,c):
 assert (d/'water-material-pass.txt').read_text().strip()==LINE and c.count(LINE)==1
 result=(d/'water-material-result.txt').read_text();assert 'pass=true ' in result and 'badVertices=0 ' in result and 'channelsBeyondTolerance=0 ' in result and 'stillHistoryViolations=0 ' in result
 raw=(d/'water-material-upload.bin').read_bytes();rows=json.loads((d/'water-material-upload-details.json').read_text());tiles=json.loads((d/'water-material-atlas.json').read_text())
 assert len(raw)==282960 and len(rows)==393
 for n,r in enumerate(rows):
  assert r['offset']==n*720
  if r['route']<64:
   route=r['route'];slot=r['slot'];a=16 if route%32<16 else route%16;b=route%16 if route%32<16 else 16
   assert r['source']==a and r['target']==(a if slot==0 else b) and r['active']==(0<slot<5) and r['cell']==(3 if route<32 else 15) and r['origin']==(0 if route<32 else 29999984)
  if r['route']==64 and r['slot'] in [3,5,6]:assert r['cleanup']==(201 if r['slot']==3 else 301)
  for v in range(20):
   at=n*720+v*36;l,sky=struct.unpack_from('<HH',raw,at+28);active=bool(sky&32768);want=v<4 and r['active']
   assert l&255==176 and sky&255==240 and active==want
   assert raw[at+15]==((r['cell']|(r['cell']<<4)) if want else 255)
   if active:assert l>>11==r['source'] and (sky>>8)&127==r['age']
   if v<4:
    x,y,z=struct.unpack_from('<fff',raw,at);assert y==1
    x-=r['cell'];z-=r['cell'];u=x;w=z
    if r['target']<16:
     a=r['target']*math.pi/8;fx=f32(math.cos(a));fz=f32(math.sin(a))
     xx=r['origin']+r['cell'];zz=-r['origin']+r['cell'];pu=.5*(fz*xx-fx*zz);pv=.5*(fx*xx+fz*zz)
     u=.5+.5*(fz*(x-.5)-fx*(z-.5))+f32(pu-math.floor(pu));w=.5+.5*(fx*(x-.5)+fz*(z-.5))+f32(pv-math.floor(pv))
    t=tiles['still' if r['target']==16 else 'flow'];actualU,actualV=struct.unpack_from('<ff',raw,at+16)
    assert abs(actualU-(t['u0']+u*t['us']))<=1e-6 and abs(actualV-(t['v0']+w*t['vs']))<=1e-6
    assert raw[at+32]==127 and raw[at+33]==127 and raw[at+35]==0
    signed=raw[at+34] if raw[at+34]<128 else raw[at+34]-256
    assert signed==-min(r['time'],100)
 details=json.loads((d/'water-material-pixel-details.json').read_text());assert len(details)==640
 for r in details:assert r['maxChannelError']<=1 and r['channelsBeyondTolerance']==0
 for profile in range(2):
  for route in range(64):
   a=Image.open(d/f'material-{profile}-{route}-0.png').convert('RGBA');b=Image.open(d/f'material-{profile}-{route}-5.png').convert('RGBA')
   size=32 if profile==0 else 8;assert a.size==b.size==(size,size)
   if profile==0:assert a.tobytes()!=b.tobytes() and len(a.getcolors(2048))>16
   if profile==1:
    assert all(g>=254 and r<=1 and bl<=1 and alpha==255 for r,g,bl,alpha in a.getdata())
    assert all(g>=254 and r<=1 and bl<=1 and alpha==255 for r,g,bl,alpha in b.getdata())
   aa=a.tobytes();bb=b.tobytes()
   for phase in range(5):
    image=Image.open(d/f'material-{profile}-{route}-{phase+1}.png').convert('RGBA');cc=image.tobytes();t=phase*.25
    assert image.getchannel('A').getextrema()==(255,255)
    assert max(abs(x-round(y*(1-t)+z*t)) for x,y,z in zip(cc,aa,bb))<=1
 return {'routes':64,'shader_renders':768,'weighted_RGBA_samples':640,'actual_upload_readbacks':393,'readback_bytes':282960,'pass':True}
