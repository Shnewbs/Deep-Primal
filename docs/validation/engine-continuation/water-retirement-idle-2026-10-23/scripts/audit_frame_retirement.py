from pathlib import Path
import hashlib,json,gzip,struct,math,collections,sys
def sha(b):return hashlib.sha256(b).hexdigest()
def expected_vertex():
 pairs=sum(108*289**n for n in range(4));a=pairs&0xffffffff;b=pairs>>32
 def uv(v,signed):return 0x40000000|(v&0x7fffff)|((v&0x1f800000)<<1)|(((v&0x20000000)<<2)if signed else 0)
 out=bytearray(36);struct.pack_into('<II',out,16,uv(a&0x1fffffff,False),uv((a>>29)|((b&0x7ffffff)<<3),True));struct.pack_into('<H',out,26,(b>>27)<<8);out[33]=192;return bytes(out)
def audit(case):
 data=json.loads((case/'water-frame-retirement.json').read_text());r=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());text=raw.decode();assert sha(raw)==r['console_sha256']and r['exit_code']==0 and not r['timeout']and r['build_successful']
 for marker in ['WATER_FRAME_RETIREMENT_PASS','CLIENT_SAVED_DISCONNECT_PASS','All dimensions are saved','BUILD SUCCESSFUL']:assert marker in text,marker
 assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed'];assert all(a['completed']for a in json.loads((case/'action-history.json').read_text()))
 if'BACKEND_SELECTION_PASS backend=Vulkan'in text:
  for marker in ['VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0']:assert marker in text,marker
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 assert data['pass']and data['differentBytes']==0 and data['pages']==2048 and data['residentBytes']==33554304 and data['turns']==145 and data['sweepTurns']==129 and data['idleTurns']==16
 moving=data['moving'];assert data['rangeWrites']==(2175 if moving else 0)and data['rangeBytes']==(33554304 if moving else 0)and data['maxRangeBytes']==(16416 if moving else 0)
 rows=data['samples'];assert len(rows)==145
 ids=[x['frame']for x in rows];assert len(set(ids))==145 and all(b==a+1 for a,b in zip(ids,ids[1:]))
 assert sum(x['rangeWrites']for x in rows)==data['rangeWrites']and sum(x['rangeBytes']for x in rows)==data['rangeBytes']
 for x in rows:
  assert 0<=x['rangeWrites']<=32 and 0<=x['rangeBytes']<=262080
  assert 0<=x['backendWriteNanos']+x['backendEncoderNanos']<=x['retireCpuNanos']<=x['frameCpuNanos']<=x['frameIntervalNanos']
 for key in ['retireCpuNanos','frameCpuNanos','frameIntervalNanos']:
  values=sorted(x[key]for x in rows);assert data[key+'Median']==values[72]and data[key+'P95']==values[math.ceil(145*.95)-1]and data[key+'Max']==values[-1]
 sizes=[(113+(n<1592))*144 for n in range(2048)];assert collections.Counter(x['bytes']for x in data['readbacks'])==collections.Counter(sizes)
 vertex=expected_vertex()
 for x in data['readbacks']:
  expected=sha(vertex*(x['bytes']//36));assert x['cpuSha256']==x['gpuSha256']==expected
 events=data['stagingEvents'];assert sum(e['predictedWrap']for e in events)==data['stagingWrapPredictions']
 for e in events:
  assert e['predictedWrap']==(e['bytes']>e['stagingCapacity']-((e['stagingUsedBefore']+3)&~3))
  assert e['stagingUsedAfter']==(e['bytes']if e['predictedWrap']else((e['stagingUsedBefore']+3)&~3)+e['bytes'])
 idle=rows[129:];candidate=data['idlePolicy'];assert len(idle)==16 and all(x['idle']==1 and x['rangeWrites']==x['rangeBytes']==0 for x in idle)
 assert all(x['queueBefore']==x['queueAfter']==(0 if candidate else 2048)for x in idle)
 assert data['queueAfterIdle']==(0 if candidate else 2048)
 assert all(x['queueBefore']>=x['queueAfter']for x in rows[:129])
 phases={x['phase']:x for x in data['lifecycle']};assert len(phases)==11
 assert phases['partial-sweep']['cursor']==262080 and phases['partial-sweep']['queueSize']==1
 for name in ['behind-cursor-expired','normal-publication-expired','retry-expired','recovered-retirement']:
  assert phases[name]['queueSize']==(0 if candidate else 1)
 for name in ['normal-publication-wake','live-pair-retained','retry-wake','rejected-retirement']:
  assert phases[name]['queueSize']==1
 assert phases['rejected-retirement']['cursor']==0 and data['lifecyclePublicationAndRetirementRejectionsPreserved']and data['resetQueueEmpty']
 from audit_boundary_batches import packet,POW
 raw=bytearray(393120)
 for q in range(0,len(raw),144):
  to,start=(9,7000)if q==0 else(7,5000)if q==144 else(8,6000)if q==288 else(6,0)
  for v in range(4):packet(raw,q+v*36,(to*17+to)*sum(POW),[start]*4)
 cpu=(case/'water-idle-lifecycle-cpu.bin').read_bytes();gpu=(case/'water-idle-lifecycle-gpu.bin').read_bytes()
 assert cpu==gpu==raw and sha(cpu)==data['lifecycleCpuSha256']==data['lifecycleGpuSha256']and data['lifecycleBytes']==len(raw)
 idle_ns=sorted(x['retireCpuNanos']for x in idle)
 before=json.loads((case/'source-identity-before.json').read_text());after=json.loads((case/'source-identity-after.json').read_text());assert all(before[k]==v for k,v in after.items())
 if'packaged'in case.name:
  origins=[x for x in text.splitlines()if'VALIDATION_CLASS_ORIGIN'in x];assert origins and all('/build/libs/'in x and'.jar'in x for x in origins)
 return {'case':case.name,'pass':True,'moving':moving,'distinct_rendered_frames':145,'range_writes':data['rangeWrites'],'range_bytes':data['rangeBytes'],'maximum_writes_per_frame':max(x['rangeWrites']for x in rows),'maximum_bytes_per_frame':max(x['rangeBytes']for x in rows),'staging_wrap_predictions':data['stagingWrapPredictions'],'retire_cpu_p95_ms':data['retireCpuNanosP95']/1e6,'retire_cpu_max_ms':data['retireCpuNanosMax']/1e6,'frame_cpu_p95_ms':data['frameCpuNanosP95']/1e6,'frame_cpu_max_ms':data['frameCpuNanosMax']/1e6,'frame_interval_p95_ms':data['frameIntervalNanosP95']/1e6,'frame_interval_max_ms':data['frameIntervalNanosMax']/1e6,'idle_policy':candidate,'idle_frames':16,'idle_queue':data['queueAfterIdle'],'idle_cpu_median_ms':idle_ns[8]/1e6,'idle_cpu_max_ms':idle_ns[-1]/1e6,'lifecycle_independent_packet_pass':True,'fixture_class_sha256':data['fixtureClassSha256'],'all_gpu_hashes_match_independent_packet':True,'normal_save_shutdown':True,'new_crashes':0,'scope':'Private non-drawn pages under real rendered-frame scheduling; no admission/churn/full-scene/ownerGPU/FPS acceptance'}
if __name__=='__main__':print(json.dumps({'pass':True,'sessions':[audit(Path(p))for p in sys.argv[1:]]},indent=2))
