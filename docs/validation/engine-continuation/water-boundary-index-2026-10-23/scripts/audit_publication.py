from pathlib import Path
import json,hashlib,re,gzip,sys

def raw(p):return p.read_bytes()if p.exists()else gzip.decompress(p.with_name(p.name+'.gz').read_bytes())
def read(p):return json.loads(raw(p))
def sha(b):return hashlib.sha256(b).hexdigest()
def check(case,baseline=False):
 rows=read(case/'water-publication-failure-details.json');data=raw(case/'water-publication-failure-readbacks.bin');assert len(rows)==64;end=bad=0;seen=set();per_case={};mirrors={}
 for r in rows:
  key=(r['path'],r['origin'],r['axis'],r['rejectedWrite'],r['phase'],r['side']);assert key not in seen;seen.add(key);assert r['offset']==end;n=r['bytes'];cpu=data[end:end+n];gpu=data[end+n:end+2*n];end+=n*2;assert len(cpu)==len(gpu)==n
  assert sha(cpu)==r['cpuSha256']and sha(gpu)==r['gpuSha256'];diff=sum(x!=y for x,y in zip(cpu,gpu));assert diff==r['differentBytes'];bad+=diff;per_case[key[:4]]=per_case.get(key[:4],0)+diff;mirrors[key]=cpu
 assert end==len(data)and len(per_case)==16
 if baseline:assert bad==832 and all(v>0 for v in per_case.values())
 else:assert bad==0
 record=raw(case/'water-publication-failure-result.txt').decode();assert'pass='+str(not baseline).lower()in record and'cases=16 readbacks=64 differentBytes='+str(bad)in record
 report=read(case/'result.json');console=gzip.decompress((case/'console-final.gz').read_bytes());text=console.decode();assert report['exit_code']==0 and report['build_successful']and not report['timeout']and sha(console)==report['console_sha256'];assert not read(case/'crash-delta.json')['new_or_changed'];assert all(r['completed']for r in read(case/'action-history.json'))
 for marker in ['Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL']:assert marker in text
 if not baseline:assert'WATER_PUBLICATION_FAILURE_NATIVE_PASS'in text
 for marker in ['AssertionError','Exception in thread','VUID-','Failed to save']:assert marker not in text
 return {'case':case.name,'baseline':baseline,'pass':not baseline,'reproduction_verified':baseline,'cases':16,'readbacks':64,'different_bytes':bad,'raw_bytes':len(data),'raw_sha256':sha(data),'new_crashes':0,'normal_save_shutdown':True},mirrors
if __name__=='__main__':
 baseline,original=check(Path(sys.argv[1]),True);out=[]
 for p in sys.argv[2:]:
  record,mirror=check(Path(p));assert all(v==original[k]for k,v in mirror.items()if k[4]=='after-retry');record['intended_cpu_after_retry_matches_reproduced_baseline']=True
  case=Path(p);b=read(case/'water-retirement-budget.json');assert b['pass']and b['residentBytes']==33554304 and b['pages']==8 and b['turns']==129 and b['rangeWrites']==136 and b['rangeBytes']==33554304 and b['maxRangeBytes']<=262080 and b['differentBytes']==0
  assert sum(r['bytes']for r in b['readbacks'])==33554304 and all(r['cpuSha256']==r['gpuSha256']for r in b['readbacks']);record['real_gpu_retirement']={k:b[k]for k in['pages','residentBytes','turns','rangeWrites','rangeBytes','maxRangeBytes','differentBytes','enqueueCpuMedianNanos','enqueueCpuP95Nanos','enqueueCpuMaxNanos']};record['cpu_enqueue_max_exceeds_2ms']=b['enqueueCpuMaxNanos']>2000000;record['performance_accepted']=False;out.append(record)
 print(json.dumps({'audit_pass':True,'baseline':baseline,'candidate_sessions':out,'scope':'Synchronous pre-backend rejection with explicit manual retry; synthetic GPU residency; no async-device-loss, cohort-atomicity, full-frame/FPS or saturation/churn acceptance'},indent=2))
