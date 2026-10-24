from pathlib import Path
import gzip,json,hashlib,struct,sys

def raw(p):return p.read_bytes()if p.exists()else gzip.decompress(p.with_name(p.name+'.gz').read_bytes())
def read(p):return json.loads(raw(p))
def sha(b):return hashlib.sha256(b).hexdigest()
def decode(b,p):
 x,y=struct.unpack_from('<2I',b,p+16);h,c,l,sky=struct.unpack_from('<4H',b,p+24);op=b[p+33]
 assert op&128 and x&0x40800000==0x40000000 and y&0x40800000==0x40000000
 a=(x&0x7fffff)|((x>>1)&0x1f800000);cuv=(y&0x7fffff)|((y>>1)&0x1f800000)|((y>>2)&0x20000000)
 words=[(a|((cuv&7)<<29))&0xffffffff,((cuv>>3)|((c>>8&31)<<27))&0xffffffff,((c>>13&7)|((l>>8&255)<<3)|((sky>>8&255)<<11)|((l&15)<<19)|((sky&15)<<23)|((op&31)<<27))&0xffffffff,(op>>5)&1]
 data=sum(w<<(32*n)for n,w in enumerate(words));pairs=data&((1<<33)-1)
 targets=[pairs//289**n%289//17 for n in range(4)];sources=[pairs//289**n%289%17 for n in range(4)];starts=[data>>(33+16*n)&65535 for n in range(4)]
 return targets,sources,starts

def audit(case,historical=False):
 result=read(case/'water-corner-result.json');rows=read(case/'water-corner-details.json');pixels=raw(case/'water-corner-pixels.bin');uploads=raw(case/'water-corner-upload.bin');meta=read(case/'water-corner-upload-details.json')
 assert len(rows)==5112 and len(meta)==4840 and len(uploads)==5221440 and len(pixels)==7378944
 end=bad=maximum=coverage=decoded=shared_checks=0;kinds={};seen=set()
 for r in rows:
  size=r['width']*r['height']*4;a=r['leftOffset'];b=r['rightOffset'];assert a==end and b==a+size;end=b+size;left=pixels[a:b];right=pixels[b:end];errors=[abs(x-y)for x,y in zip(left,right)];n=sum(e>1 for e in errors);m=max(errors)
  assert n==r['channelsBeyondTolerance']and m==r['maxChannelError'];bad+=n;maximum=max(maximum,m);coverage+=sum(v!=255 for v in left[3::4])+sum(v!=255 for v in right[3::4]);key=r['kind'],r['route'];assert key not in seen;seen.add(key);kinds[r['kind']]=kinds.get(r['kind'],0)+1
  if r['kind']in(2,3):
   for image in(left,right):
    for p in range(0,len(image),4):assert all(abs(image[p+c]-[0,255,0,255][c])<=1 for c in range(4))
 assert end==len(pixels)and kinds=={0:2420,2:2420,1:136,3:136}and bad==coverage==0 and maximum<=1
 assert result['pass']and result['channels_beyond_tolerance']==bad and result['max_channel_error']==maximum and result['coverage_bad_pixels']==coverage
 cursor=0
 for r in meta:
  assert r['offset']==cursor;cursor+=r['count']*36;assert r['count']in(20,40);first=r['offset']+r['topVertex']*36
  for v in range(4):
   p=first+v*36;to,fr,starts=decode(uploads,p);h,c,l,sky=struct.unpack_from('<4H',uploads,p+24)
   assert h==0 and l&240==176 and sky&240==240 and(c&255==0 or c&255>=156)
   assert uploads[p+12:p+15]==b'\xff\xff\xff'and uploads[p+15]==r['cellX']|(r['cellZ']<<4)and uploads[p+32]==127 and uploads[p+33]&64 and uploads[p+35]==0
   assert all(0<=t<=16 and 0<=f<=16 for t,f in zip(to,fr));ages=[min(100,(r['time']-s)&65535)for s in starts]
   if 2332<=r['route']<2412:
    first_side=(r['route']-2332)%2;updated=r['count']==40 or r['side']==first_side
    for n in range(4):
     shared=((n&1)==1-r['side'])if r['axis']==0 else((n>>1)==1-r['side']);target=6 if shared else((4 if r['side']==0 else 8)if updated else(0 if r['side']==0 else 4));source=(2 if shared else(0 if r['side']==0 else 4))if(updated or shared)else target
     assert to[n]==target and fr[n]==source
     expected_age=min(100,r['time']-(100 if r['origin']==0 else 65520))if(updated or shared)else 100
     assert fr[n]==to[n]or ages[n]==expected_age;shared_checks+=int(shared)
   if r['route']>=2412:
    first_side=(r['route']-2412)%2
    for n in range(4):
     shared=((n&1)==1-r['side'])if r['axis']==0 else((n>>1)==1-r['side']);target=6 if shared else((4 if r['side']==0 else 8)if r['side']==first_side else(0 if r['side']==0 else 4));assert to[n]==fr[n]==target;shared_checks+=int(shared)
   decoded+=1
 assert cursor==len(uploads)
 log=gzip.decompress((case/'console-final.gz').read_bytes());text=log.decode();report=read(case/'result.json');assert report['exit_code']==0 and not report['timeout']and report['build_successful']and report['console_sha256']==sha(log)
 gl='reuse-gl'in case.name
 markers=['WATER_CORNER_FIELD_NATIVE_PASS','WATER_MATERIAL_NATIVE_PASS','WATER_SIDE_MATERIAL_NATIVE_PASS','WATER_DIRECTION_UPLOAD_NATIVE_PASS','WATER_SIDE_UV_UPLOAD_NATIVE_PASS','WATER_FLOW_TEXTURE_NATIVE_PASS','WATER_PARTIAL_FADE_NATIVE_PASS','WATER_GPU_REUSE_NATIVE_PASS','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS','WATER_DIRECTION_BLEND_NATIVE_PASS','WATER_SIDE_UV_PIXEL_NATIVE_PASS','WATER_FADE_CONTINUITY_NATIVE_PASS','Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL','BACKEND_SELECTION_PASS backend='+('OpenGL'if gl else'Vulkan')]
 for m in markers:assert m in text,m
 for m in ['AssertionError','Exception in thread','Failed to save','VUID-']:assert m not in text,m
 if gl:assert'RESOURCE_RELOAD_PASS'in text
 else:
  for m in ['VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0']:assert m in text
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in raw(case/'shutdown.txt').decode()
 before=read(case/'source-identity-before.json');after=read(case/'source-identity-after.json');provenance=read(case/'water-corner-runtime-provenance.json');assert all(before[k]==v for k,v in after.items());assert provenance['renderer_class_sha256']==before['expected_renderer_class_sha256']
 for n in['animated_water.vsh','animated_water.fsh']:assert provenance[n+'_sha256']==before['graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/'+n]
 assert not read(case/'crash-delta.json')['new_or_changed'];actions=case/'action-history.json';action_available=actions.exists()
 if action_available:assert all(a['completed']for a in read(actions))
 elif not historical:raise AssertionError('Fresh native action history missing')
 if 'packaged'in case.name:
  origins=[l for l in text.splitlines()if'VALIDATION_CLASS_ORIGIN'in l];assert origins and all('/build/libs/'in l and'.jar'in l and'/build/classes/'not in l for l in origins)
 return{'case':case.name,'audit_pass':True,'historical_replay':historical,'fresh_native':not historical,'comparisons':len(rows),'draws':result['draws'],'decoded_top_vertices':decoded,'shared_corner_checks':shared_checks,'channels_beyond_1':bad,'maximum_channel_error':maximum,'uncovered_pixels':coverage,'pixel_sha256':sha(pixels),'vertex_sha256':sha(uploads),'source_class_shader_identity':True,'normal_save_shutdown':True,'fresh_crash_delta':0,'action_history_available':action_available,'scope':'Controlled actual uploaded payload reprojection; no full-stream/owner-GPU acceptance'}
if __name__=='__main__':
 hist='--historical'in sys.argv;paths=[p for p in sys.argv[1:]if p!='--historical'];print(json.dumps({'pass':True,'sessions':[audit(Path(p),hist)for p in paths]},indent=2))
