from pathlib import Path
import json,gzip,hashlib,struct,sys
def sha(b):return hashlib.sha256(b).hexdigest()
POW=[1,289,83521,24137569]
def uv(v,signed):return 0x40000000|(v&0x7fffff)|((v&0x1f800000)<<1)|(((v&0x20000000)<<2)if signed else 0)
def packet(out,p,pairs,starts):
 a,c,d,e=starts;w1=(pairs>>32)|(a<<1)|(c<<17);w2=(c>>15)|(d<<1)|(e<<17)
 normal=64|(w2>>27)|(((e>>15)&1)<<5)
 struct.pack_into('<II',out,p+16,uv(pairs&0x1fffffff,False),uv((pairs&0xffffffff)>>29|((w1&0x7ffffff)<<3),True))
 old=struct.unpack_from('<H',out,p+26)[0];struct.pack_into('<H',out,p+26,(old&255)|(((w1>>27)|((w2&7)<<5))<<8)&65535)
 old=struct.unpack_from('<H',out,p+28)[0];struct.pack_into('<H',out,p+28,(old&240)|((w2>>19)&15)|(((w2>>3)&255)<<8))
 old=struct.unpack_from('<H',out,p+30)[0];struct.pack_into('<H',out,p+30,(old&240)|((w2>>23)&15)|(((w2>>11)&255)<<8))
 out[p+33]=128|normal
def payload(row):
 initial=bytearray(row['bytes'])
 for q in range(row['quads']):
  for v in range(4):
   p=q*144+v*36;struct.pack_into('<fffI',initial,p,q+(v&1)*.5,q%17+(v>>1)*.25,-q,0x1a234567);struct.pack_into('<H',initial,p+24,1234);struct.pack_into('<H',initial,p+28,176);struct.pack_into('<H',initial,p+30,240);initial[p+32]=127;initial[p+34]=q%16;initial[p+35]=91;packet(initial,p,72*sum(POW),[0]*4)
 assert sha(initial)==row['initialSha256'];expected=bytearray(initial)
 dx,dz=[(1,0),(0,1),(-1,0),(0,-1),(1,1),(-1,1),(1,-1),(-1,-1)][row['axis']]
 touched=set()
 for entry in row['boundaryEntries']:
  q=entry>>12;cx=entry&15;cz=(entry>>4)&15;ns=[]
  for n in range(4):
   x=cx+(n&1);z=cz+(n>>1)
   if dx*16<=x<=dx*16+16 and dz*16<=z<=dz*16+16:ns.append(n)
  if not ns:continue
  touched.add(q);pairs=sum((140 if n in ns else 72)*POW[n]for n in range(4));starts=[100 if n in ns else 0 for n in range(4)]
  for v in range(4):packet(expected,q*144+v*36,pairs,starts)
 return initial,expected,touched
def audit(case,baseline=False):
 r=json.loads((case/'water-boundary-batch.json').read_text());assert r['pass']and r['requireBatches']==(not baseline)and r['differentBytes']==0
 report=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());txt=raw.decode();assert report['exit_code']==0 and report['build_successful']and not report['timeout']and sha(raw)==report['console_sha256']
 for m in ['WATER_BOUNDARY_BATCH_PASS cases=256 readbacks=512 differentBytes=0','CLIENT_SAVED_DISCONNECT_PASS','Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL']:assert m in txt,m
 assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed'];assert all(x['completed']for x in json.loads((case/'action-history.json').read_text()))
 if'BACKEND_SELECTION_PASS backend=Vulkan'in txt:
  for m in ['validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0','VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1']:assert m in txt,m
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 before=json.loads((case/'source-identity-before.json').read_text());after=json.loads((case/'source-identity-after.json').read_text());assert all(before[k]==v for k,v in after.items())
 assert len(r['cases'])==256 and len(r['readbacks'])==512
 data=(case/'water-boundary-batch-readbacks.bin').read_bytes();rows={x['id']:x for x in r['cases']};expected={};normal=[]
 for row in rows.values():
  initial,final,touched=payload(row);accepted=bytearray(initial);ranges=row['ranges'];first=row['firstWriteAttempts'];assert row['idempotentWrites']==0
  assert len(ranges)==first+row['retryWriteAttempts'];assert sum(x['rejected']for x in ranges)==(1 if row['rejectWrite']else 0)
  for n,v in enumerate(ranges):
   a=v['offset'];b=a+v['bytes'];assert a%144==b%144==0 and 0<=a<b<=len(initial)and v['bytes']<=262080
   assert all(q in touched for q in range(a//144,b//144)),'Uploaded an untouched/gap quad'
   if n<first and not v['rejected']:accepted[a:b]=final[a:b]
  assert row['cleanupAfterCall']==(201 if any(not v['rejected']for v in ranges[:first])else 0)and row['cleanupAfterRetry']==201
  if row['rejectWrite']==0:
   assert first==(len(touched) if baseline else ((row['count']+1819)//1820 if row['group']==2048 else row['count']//256 if row['group']==256 else row['count']//16))and row['retryWriteAttempts']==0
   assert sum(v['bytes']for v in ranges)==len(touched)*144;normal.append({'origin':row['origin'],'axis':row['axis'],'group':row['group'],'writes':first,'bytes':len(touched)*144,'quads':len(touched),'sync_cpu_ms':row['syncCpuNanos']/1e6})
  expected[(row['id'],'after-call')]=bytes(accepted);expected[(row['id'],'after-retry')]=bytes(final)
 end=0
 for x in r['readbacks']:
  assert x['offset']==end;n=x['bytes'];cpu=data[end:end+n];gpu=data[end+n:end+2*n];end+=2*n
  assert cpu==gpu==expected[(x['id'],x['phase'])]and x['differentBytes']==0 and sha(cpu)==x['cpuSha256']==x['gpuSha256']
 assert end==len(data)
 return {'case':case.name,'pass':True,'baseline':baseline,'cases':256,'paired_cpu_gpu_reads':512,'raw_readback_bytes':len(data),'raw_sha256':sha(data),'all_bytes_match_independent_packet_and_rejection_oracle':True,'normal_routes':normal,'fixture_class_sha256':r['fixtureClassSha256'],'new_crashes':0,'normal_save_shutdown':True,'scope':'Synchronous private neighbor-range fixture, no full-scene/capchurn/ownerGPU/FPS acceptance'},expected
if __name__=='__main__':
 old,oracle=audit(Path(sys.argv[1]),True);rows=[]
 for p in sys.argv[2:]:
  row,expected=audit(Path(p));assert row['fixture_class_sha256']==old['fixture_class_sha256'];assert all(v==oracle[k]for k,v in expected.items()if k[1]=='after-retry');rows.append(row)
 print(json.dumps({'pass':True,'baseline':old,'candidates':rows,'same_native_fixture_class':True,'intended_final_packets_match_baseline':True},indent=2))
