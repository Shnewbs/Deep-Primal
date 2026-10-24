from pathlib import Path
import json,hashlib,gzip,struct,sys,math,collections
def sha(b):return hashlib.sha256(b).hexdigest()
def packed_vertex():
 # Independent expected post-retirement packet: all four directions are steady6,
 # timestamp0, opaque, geometry/color/light initially zero. No Java codec is called.
 pairs=sum(108*289**n for n in range(4));a=pairs&0xffffffff;b=pairs>>32
 def pack(v,signed):return 0x40000000|(v&0x7fffff)|((v&0x1f800000)<<1)|(((v&0x20000000)<<2)if signed else 0)
 out=bytearray(36);struct.pack_into('<II',out,16,pack(a&0x1fffffff,False),pack((a>>29)|((b&0x7ffffff)<<3),True));struct.pack_into('<H',out,26,(b>>27)<<8);out[33]=192;return bytes(out)
def audit(case):
 r=json.loads((case/'water-retirement-budget.json').read_text());result=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());text=raw.decode();assert sha(raw)==result['console_sha256'];assert result['exit_code']==0 and not result['timeout']and result['build_successful']
 for record in ['WATER_RETIREMENT_BUDGET_NATIVE_PASS','CLIENT_SAVED_DISCONNECT_PASS','All dimensions are saved','BUILD SUCCESSFUL']:
  assert record in text,record
 if 'Vulkan'in text.split('BACKEND_SELECTION_PASS backend=')[-1].splitlines()[0]:
  for record in ['VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0']:assert record in text,record
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed'];assert all(x['completed']for x in json.loads((case/'action-history.json').read_text()))
 assert r['pass']and r['differentBytes']==0 and r['residentBytes']==33554304 and r['turns']==129 and r['rangeBytes']==33554304
 n=r['pages'];assert n in(8,2048);quads=33554304//144;base,extra=divmod(quads,n);sizes=[(base+(i<extra))*144 for i in range(n)];boundaries=set();prefix=0
 for size in sizes[:-1]:prefix+=size;boundaries.add(prefix)
 cuts=set(range(262080,33554304,262080));expected_writes=1+len(boundaries|cuts);assert r['rangeWrites']==expected_writes
 expected_counts=collections.Counter(sizes);assert collections.Counter(x['bytes']for x in r['readbacks'])==expected_counts
 v=packed_vertex()
 for row in r['readbacks']:
  expected=sha(v*(row['bytes']//36));assert row['cpuSha256']==row['gpuSha256']==expected
 rows=r['turnSamples'];assert len(rows)==129 and [x['turn']for x in rows]==list(range(129));times=sorted(x['elapsedNanos']for x in rows)
 assert r['enqueueCpuMedianNanos']==times[64]and r['enqueueCpuP95Nanos']==times[math.ceil(129*.95)-1]and r['enqueueCpuMaxNanos']==times[-1]
 assert sum(x['rangeWrites']for x in rows)==expected_writes and sum(x['rangeBytes']for x in rows)==33554304
 assert sum(x['backendWriteNanos']for x in rows)==r['backendWriteTotalNanos']and sum(x['backendEncoderNanos']for x in rows)==r['backendEncoderTotalNanos']
 for row in rows:
  assert 0<=row['rangeBytes']<=262080 and 0<=row['rangeWrites']<=32 and row['otherCpuNanos']>=0
  assert row['elapsedNanos']==row['backendWriteNanos']+row['backendEncoderNanos']+row['otherCpuNanos']
 events=r.get('stagingWriteEvents',[]);wraps=[e for e in events if e['predictedWrap']]
 if 'stagingWrapPredictions'in r:
  assert len(wraps)==r['stagingWrapPredictions']
  for e in events:
   assert e['predictedWrap']==(e['bytes']>e['stagingCapacity']-((e['stagingUsedBefore']+3)&~3))
   assert e['stagingUsedAfter']==e['bytes']if e['predictedWrap']else e['stagingUsedAfter']==((e['stagingUsedBefore']+3)&~3)+e['bytes']
 return {'case':case.name,'pass':True,'validation_fixture_class_sha256':r.get('validationFixtureClassSha256'),'validation_fixture_class_origin':r.get('validationFixtureClassOrigin'),'setup_vertex_upload_bytes':r.get('setupVertexUploadBytes'),'pages':n,'gpu_bytes':33554304,'turns':129,'range_writes':expected_writes,'maximum_writes_per_turn':max(x['rangeWrites']for x in rows),'p95_ms':r['enqueueCpuP95Nanos']/1e6,'max_ms':r['enqueueCpuMaxNanos']/1e6,'worst_turn':max(rows,key=lambda x:x['elapsedNanos']),'wraps':wraps,'duplicate_full_setup_uploads':r.get('duplicateFullSetupUploads',True),'all_readback_hashes_match_independent_codec':True,'normal_save_shutdown':True,'new_crashes':0,'performance_accepted':False,'scope':'Instrumented diagnostic burst, not normal-frame/churn/FPS acceptance'}
if __name__=='__main__':print(json.dumps({'pass':True,'sessions':[audit(Path(p))for p in sys.argv[1:]]},indent=2))
