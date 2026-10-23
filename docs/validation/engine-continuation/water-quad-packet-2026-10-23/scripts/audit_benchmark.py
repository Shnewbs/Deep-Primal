from pathlib import Path
import json,hashlib,struct,sys
from audit_corners import decode
R=Path(sys.argv[1])if len(sys.argv)>1 else Path(__file__).parent
records=json.loads((R/'benchmark-results.json').read_text());powers=[1,289,83521,24137569]
def uv(v,signed):return 0x40000000|(v&0x7fffff)|((v&0x1f800000)<<1)|(((v&0x20000000)<<2)if signed else 0)
def pack(b,p,pairs,starts,opaque):
 a,c,d,e=starts;w1=(pairs>>32)|(a<<1)|(c<<17);w2=(c>>15)|(d<<1)|(e<<17);normal=(64 if opaque else 0)|(w2>>27)|(((e>>15)&1)<<5)
 first=uv(pairs&0x1fffffff,False)|(0x80000000 if normal==0 else 0);second=uv((pairs&0xffffffff)>>29|((w1&0x7ffffff)<<3),True)
 struct.pack_into('<II',b,p+16,first,second);old=struct.unpack_from('<H',b,p+26)[0];struct.pack_into('<H',b,p+26,(old&255)|(((w1>>27)|((w2&7)<<5))<<8)&65535)
 old=struct.unpack_from('<H',b,p+28)[0];struct.pack_into('<H',b,p+28,(old&240)|((w2>>19)&15)|(((w2>>3)&255)<<8));old=struct.unpack_from('<H',b,p+30)[0];struct.pack_into('<H',b,p+30,(old&240)|((w2>>23)&15)|(((w2>>11)&255)<<8));b[p+33]=128|normal
expected={};routes=[]
for run in records:
 root=R/'benchmarks'/('run-'+str(run['run'])+'-'+run['variant'])
 for row in run['rows']:
  mask=row['mask'];initial=(root/f'initial-{mask}.bin').read_bytes();final=(root/f'final-{mask}.bin').read_bytes();oracle=bytearray(initial);assert len(initial)==len(final)==2048*144
  samples=sorted(row['sample_nanos']);assert len(samples)==31 and row['median_nanos']==samples[15]and row['p95_nanos']==samples[29]
  rounds=row['samples']+row['warmup_rounds'];passes=row['passes_per_sample'];now=65520+(rounds-1)*passes+passes-1;start=(now-25)&65535
  assert row['declared_corner_updates']==2048*rounds*passes*(2 if mask==3 else 6)
  # Declared work count follows fixed source loops; no per-update instrumentation is timed.
  target=[8 if mask>>n&1 else 4 for n in range(4)];source=[4]*4;starts=[start if mask>>n&1 else 0 for n in range(4)];pairs=sum((target[n]*17+source[n])*powers[n]for n in range(4))
  for q in range(2048):
   for v in range(4):
    at=q*144+v*36;assert decode(initial,at)==([4]*4,[4]*4,[0]*4);pack(oracle,at,pairs,starts,v%2==0);assert decode(final,at)==(target,source,starts)
  assert final==oracle and hashlib.sha256(final).hexdigest()==row['sha256'];assert expected.setdefault(mask,final)==final
  routes.append({'run':run['run'],'variant':run['variant'],'mask':mask,'median_packet_cpu_ms_per_16_passes':row['median_nanos']/1e6,'p95_packet_cpu_ms_per_16_passes':row['p95_nanos']/1e6,'payload_sha256':row['sha256']})
report={'pass':True,'independent_byte_oracle':True,'all_variants_identical_final_packets':True,'runs':4,'order':['scalar','bulk','bulk','scalar'],'quads_per_pass':2048,'passes_per_sample':16,'warmup_rounds':16,'samples_per_mask_per_run':31,'per_update_counter_removed_from_timed_paths':True,'two_masks':routes,'scope':'Standalone packet CPU arithmetic only; separate JVM warmups and sampled elapsed times are diagnostic, not GPU publication, full-neighbor scan, frame/FPS or owner-GPU acceptance.'}
print(json.dumps(report,indent=2))
