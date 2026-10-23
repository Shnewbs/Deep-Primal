from pathlib import Path
import json,gzip,hashlib,sys
from audit_frame_retirement import expected_vertex
def sha(b):return hashlib.sha256(b).hexdigest()
def audit(case,baseline=False):
 r=json.loads((case/'water-deadline-result.json').read_text());report=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());text=raw.decode();assert report['exit_code']==0 and report['build_successful']and not report['timeout']and sha(raw)==report['console_sha256']
 for record in ['WATER_DEADLINE_RESULT','CLIENT_SAVED_DISCONNECT_PASS','Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL']:assert record in text,record
 if not baseline:assert'WATER_DEADLINE_NATIVE_PASS cases=3 pendingFutureProbes=0 retirementPreserved=true'in text
 assert r['sharedBudget']==(not baseline)and r['expectedShared']==(not baseline)and r['differentBytes']==0
 assert len(r['cases'])==len(r['readbacks'])==3
 for row,delay in zip(r['cases'],[8,12,20]):
  assert row['delayMillis']==delay and row['observedDelayNanos']>=7000000 and row['extractNanos']>=row['observedDelayNanos']
  assert row['retirementWrites']==1 and row['pendingFutureProbesAfterRetirement']==(4 if baseline else 0)
 b=(case/'water-deadline-readbacks.bin').read_bytes();expected=expected_vertex()*4;assert b==expected*6 and len(b)==864
 assert all(x['bytes']==144 and x['gpuSha256']==x['cpuSha256']==sha(expected)for x in r['readbacks'])
 assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed'];assert all(x['completed']for x in json.loads((case/'action-history.json').read_text()))
 if'BACKEND_SELECTION_PASS backend=Vulkan'in text:
  for marker in ['validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0','VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1']:assert marker in text,marker
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 for marker in ['AssertionError','Exception in thread','VUID-','Failed to save']:assert marker not in text,marker
 return {'case':case.name,'baseline':baseline,'budget_regression_reproduced':baseline,'shared_admission_deadline_pass':not baseline,'controlled_delays_ms':[8,12,20],'retirement_writes':3,'pending_probes_after_retirement':[x['pendingFutureProbesAfterRetirement']for x in r['cases']],'different_bytes':0,'raw_readback_sha256':sha(b),'fixture_class_sha256':r['fixtureClassSha256'],'new_crashes':0,'normal_save_shutdown':True,'scope':'Controlled injected retirement stalls verify optional-work admission; not a hard total extraction bound or ordinary latency benchmark'}
if __name__=='__main__':
 baseline=audit(Path(sys.argv[1]),True);candidates=[audit(Path(p))for p in sys.argv[2:]];assert all(x['fixture_class_sha256']==baseline['fixture_class_sha256']for x in candidates)
 print(json.dumps({'pass':True,'baseline':baseline,'candidate_sessions':candidates,'same_native_fixture_class':True},indent=2))
