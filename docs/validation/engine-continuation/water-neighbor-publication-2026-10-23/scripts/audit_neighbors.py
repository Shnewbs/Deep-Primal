from pathlib import Path
import json,gzip,hashlib,struct,sys,statistics,re

def raw(p):return p.read_bytes() if p.exists() else gzip.decompress(p.with_name(p.name+'.gz').read_bytes())
def read(p):return json.loads(raw(p))
def audit(case,baseline):
 result=read(case/'water-corner-result.json');rows=read(case/'water-corner-details.json');pixels=raw(case/'water-corner-pixels.bin');uploads=raw(case/'water-corner-upload.bin');meta=read(case/'water-corner-upload-details.json')
 assert len(meta)==4824 and len(uploads)==5209920 and len(rows)==5096 and len(pixels)==7358464
 errors=coverage=maximum=offset=0;kinds={};failed=[];seen=set()
 for r in rows:
  size=r['width']*r['height']*4;lo=r['leftOffset'];hi=r['rightOffset'];assert lo==offset and hi==lo+size;offset=hi+size;a=pixels[lo:hi];b=pixels[hi:offset];es=[abs(x-y) for x,y in zip(a,b)];bad=sum(x>1 for x in es);mx=max(es)
  assert bad==r['channelsBeyondTolerance'] and mx==r['maxChannelError'];errors+=bad;maximum=max(maximum,mx);coverage+=sum(x!=255 for x in a[3::4])+sum(x!=255 for x in b[3::4]);kinds[r['kind']]=kinds.get(r['kind'],0)+1
  assert(r['kind'],r['route'])not in seen;seen.add((r['kind'],r['route']))
  if bad:failed.append({'kind':r['kind'],'route':r['route'],'channels':bad,'max':mx})
  if r['kind']in(2,3):
   for image in(a,b):
    for at in range(0,size,4):assert all(abs(image[at+c]-[0,255,0,255][c])<=1 for c in range(4))
 assert offset==len(pixels) and kinds=={0:2412,2:2412,1:136,3:136} and coverage==0
 assert result['channels_beyond_tolerance']==errors and result['max_channel_error']==maximum and result['coverage_bad_pixels']==coverage and result['packed_actual_tops']==4824
 if baseline:assert result['pass']is False and len(failed)==32 and errors==15280 and maximum==120 and all(r['kind']==0 and r['route']>=2332 for r in failed)
 else:assert result['pass']is True and not failed
 cursor=0;decoded=0;sharedChecks=0;timing={20:[],40:[]}
 for r in meta:
  assert r['offset']==cursor;cursor+=r['count']*36;assert r['count']in(20,40)
  if 100<=r['route']<2312:timing[r['count']].append(r['uploadNanos']/1000)
  first=r['offset']+r['topVertex']*36
  for v in range(4):
   p=first+v*36;u,w=struct.unpack_from('<2f',uploads,p+16);assert u<-1
   to=round(-u)-2;fr=round(w);h,c,l,sky=struct.unpack_from('<4H',uploads,p+24);ages=[c>>8,l>>8,sky>>8,((to>>20)&15)|(((fr>>20)&7)<<4)]
   assert h==0 and l&255==176 and sky&255==240 and (c&255==0 or c&255>=156)
   assert uploads[p+12:p+15]==b'\xff\xff\xff' and uploads[p+15]==r['cellX']|(r['cellZ']<<4);assert uploads[p+32:p+34]==bytes([127,127]) and (uploads[p+34]==0 or uploads[p+34]>=156)
   if not baseline:assert uploads[p+35]==0
   for n in range(4):assert((to>>(n*5))&31)<=16 and((fr>>(n*5))&31)<=16 and 0<=ages[n]<=100
   if r['route']>=2332:
    firstSide=(r['route']-2332)%2;samePage=r['count']==40
    for n in range(4):
     shared=((n&1)==(1-r['side']))if r['axis']==0 else((n>>1)==(1-r['side']))
     updated=samePage or r['side']==firstSide
     target=(6 if updated or not baseline else 2)if shared else((4 if r['side']==0 else 8)if updated else(0 if r['side']==0 else 4))
     old=(2 if shared else(0 if r['side']==0 else 4));source=old if(updated or(shared and not baseline))else target
     assert(to>>(n*5))&31==target,(r,n,'target',target)
     assert(fr>>(n*5))&31==source,(r,n,'source',source)
     ageAtDraw=min(100,ages[n]+r['time']-r['uploaded'])
     expectedAge=min(100,r['time']-(100 if r['origin']==0 else 20000))if(updated or(shared and not baseline))else 100
     assert ageAtDraw==expectedAge,(r,n,'age',ageAtDraw,expectedAge)
     if shared:sharedChecks+=1
   decoded+=1
 assert cursor==len(uploads)
 before=read(case/'source-identity-before.json');after=read(case/'source-identity-after.json');provenance=read(case/'water-corner-runtime-provenance.json');assert all(before[k]==v for k,v in after.items());assert provenance['renderer_class_sha256']==before['expected_renderer_class_sha256'];assert provenance['resident_presentation_mirror_present']==(not baseline);assert provenance['corner_history_field_present']
 for n in ['animated_water.vsh','animated_water.fsh']:assert provenance[n+'_sha256']==before['graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/'+n]
 log=raw(case/'console-final.gz');log=gzip.decompress(log);text=log.decode();seal=read(case/'sealed-console.json');assert hashlib.sha256(log).hexdigest()==seal['raw_sha256'] and hashlib.sha256(raw(case/'console-final.gz')).hexdigest()==seal['gzip_sha256'] and seal['launcher_exit']==0 and seal['stable_samples']>=4
 gl='reuse-gl'in case.name;required=['WATER_CORNER_FIELD_RESULT pass='+str(not baseline).lower(),'Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL','BACKEND_SELECTION_PASS backend='+('OpenGL'if gl else'Vulkan'),'WATER_MATERIAL_RESULT pass=true','WATER_SIDE_MATERIAL_RESULT pass=true','WATER_DIRECTION_UPLOAD_NATIVE_PASS','WATER_SIDE_UV_UPLOAD_NATIVE_PASS','WATER_FLOW_TEXTURE_NATIVE_PASS','WATER_PARTIAL_FADE_NATIVE_PASS','WATER_GPU_REUSE_NATIVE_PASS']
 if not baseline:required+=['WATER_CORNER_FIELD_NATIVE_PASS','WATER_CORNER_HISTORY_CAP_PASS inserted=33000 cap=32768']
 if gl:required+=['RESOURCE_RELOAD_PASS','WATER_DIRECTION_BLEND_NATIVE_PASS','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS']
 else:required+=['VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0'];assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in raw(case/'shutdown.txt').decode()
 for m in required:assert m in text,m
 for m in ['AssertionError','Exception in thread','Failed to save','VUID-']:assert m not in text,m
 assert not read(case/'crash-delta.json')['new_or_changed'];assert all(x['completed']for x in read(case/'action-history.json'))
 if 'packaged'in case.name:
  origin=[l for l in text.splitlines()if'VALIDATION_CLASS_ORIGIN'in l];assert origin and all('/build/libs/'in l and'.jar'in l and'/build/classes/'not in l for l in origin);assert read(case/'module-prelaunch-audit.json')['all_current_compiled_vulkan_classes_match']
 obs={}
 for count,xs in timing.items():xs.sort();obs[str(count)]={'samples':len(xs),'median_microseconds':statistics.median(xs),'p95_microseconds':xs[int(len(xs)*.95)]}
 return{'case':case.name,'baseline':baseline,'audit_pass':True,'functional_pass':not baseline,'comparisons':len(rows),'draws':result['draws'],'failed_pairs':failed,'channels_beyond_1':errors,'max_channel_error':maximum,'coverage_bad_pixels':coverage,'decoded_top_vertices':decoded,'new_request_shared_corner_checks':sharedChecks,'source_class_shader_identity':True,'normal_save_shutdown':True,'fresh_crashes':0,'vertex_bytes':len(uploads),'pixel_bytes':len(pixels),'pixel_sha256':hashlib.sha256(pixels).hexdigest(),'vertex_sha256':hashlib.sha256(uploads).hexdigest(),'tiny_upload_cpu_observations':obs,'full_scene_performance_acceptance':False}
if __name__=='__main__':print(json.dumps({'pass':True,'baseline':audit(Path(sys.argv[1]),True),'candidate_sessions':[audit(Path(p),False)for p in sys.argv[2:]]},indent=2))
