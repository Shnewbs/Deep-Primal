from pathlib import Path
import json,gzip,hashlib,sys
def sha(b):return hashlib.sha256(b).hexdigest()
def distance(bounds,camera):
 return sum(max(bounds[n]-camera[n],0,camera[n]-bounds[n+3])**2 for n in range(3))
def audit(case,baseline=False,initial=False):
 d=json.loads((case/'water-admission.json').read_text());r=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());txt=raw.decode()
 assert r['exit_code']==0 and not r['timeout']and r['build_successful']and sha(raw)==r['console_sha256'];assert d['fixtureExecutionPass']and d['requirePlan']==(not baseline)and d['differentBytes']==0
 if not initial:
  assert d['farAndBusyGeometryFromRealMesher']
  for phase in ['AFTER_COMPILER','BEFORE_NATIVE']:assert 'ADMISSION_CLASS_'+phase+' '+d['fixtureClassSha256']in txt
 for m in ['WATER_ADMISSION_NATIVE_PASS cases=16','CLIENT_SAVED_DISCONNECT_PASS','Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL']:assert m in txt,m
 assert all(x['completed']for x in json.loads((case/'action-history.json').read_text()));assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed']
 if'BACKEND_SELECTION_PASS backend=Vulkan'in txt:
  for m in ['validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0','VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1']:assert m in txt,m
  assert'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 if 'packaged'in case.name:
  package=json.loads((case/'validated-package-identity.json').read_text());assert package['unchanged']and package['before_sha256']==package['after_sha256']and package['root_jar_and_jarJar_excluded_after_audited_full_build']
 assert len(d['cases'])==16;rows={x['id']:x for x in d['cases']};normal=[];denied=lost=unneeded=0
 for row in rows.values():
  before={x['label']:x for x in row['before']};after={x['label']:x for x in row['after']};assert set(before)==set(after)
  assert all(x['present']and x['bytes']==x['actualBufferBytes']for x in before.values());resident=sum(x['bytes']for x in before.values());assert resident==row['beforeResidentBytes']and row['cap']==33554432 and 0<=row['cap']-resident<144
  camera=row['camera'];keep=distance(before['target']['bounds'],camera);required=resident+row['incomingBytes']-row['cap'];assert required>0
  candidates=[x for x in before.values()if x['label']!='target'and not x['busy']and distance(x['bounds'],camera)>keep and(baseline or x['bytes']>0)]
  candidates.sort(key=lambda x:distance(x['bounds'],camera),reverse=True)
  sufficient=sum(x['bytes']for x in candidates)>=required;removed=[];reclaimed=0
  if baseline or sufficient:
   for x in candidates:
    removed.append(x['label']);reclaimed+=x['bytes']
    if reclaimed>=required:break
  observed=[x['label']for x in after.values()if not x['present']];assert set(observed)==set(removed),(case.name,row['id'],observed,removed)
  assert row['accepted']==sufficient and row['oldTargetClosed']==sufficient and row['uploadsDelta']==int(sufficient)and row['retryRequested']==(not sufficient)
  expected_resident=resident-reclaimed+(row['incomingBytes']-row['targetOldBytes']if sufficient else 0)
  assert expected_resident==row['afterResidentBytes']==sum(x['bytes']for x in after.values()if x['present'])<=row['cap']
  assert after['near']['present']and after['busy']['present']and after['target']['present'];assert after['target']['bytes']==(row['incomingBytes']if sufficient else row['targetOldBytes'])
  if not sufficient:
   denied+=1;lost+=sum(before[n]['bytes']>0 for n in removed);assert row['deniedPreserved']==(not removed)
  if'placeholder'in removed:unneeded+=1
  normal.append({'id':row['id'],'mode':row['mode'],'axis':row['axis'],'origin':row['origin'],'incoming_bytes':row['incomingBytes'],'accepted':sufficient,'expected_evictions':removed,'actual_evictions':observed,'after_resident_bytes':expected_resident,'upload_cpu_ms':row['uploadCpuNanos']/1e6})
 assert denied==8 and d['budgetDeniedCoveragePreserved']==(not baseline)
 if baseline:assert lost==16 and unneeded==16
 else:assert lost==unneeded==0
 data=(case/'water-admission-readbacks.bin').read_bytes();end=0;packets={};expected_payloads={}
 for x in d['readbacks']:
  assert x['offset']==end;n=x['bytes'];cpu=data[end:end+n];gpu=data[end+n:end+2*n];end+=2*n
  assert cpu==gpu and len(cpu)==n and x['differentBytes']==0 and sha(cpu)==x['cpuSha256']==x['gpuSha256'];key=(x['id'],x['page'],x['phase']);assert key not in packets;packets[key]=cpu
  if x['page']=='near':assert cpu==bytes([90])*n
 for row in rows.values():
  a=packets[(row['id'],'target','before')];b=packets[(row['id'],'target','after')];assert len(a)==row['targetOldBytes'];assert len(b)==(row['incomingBytes']if row['accepted']else len(a))
  if not row['accepted']:assert a==b
  if not initial:
   for x in d['readbacks']:
    if x['id']==row['id']and x['page'].startswith('far')and x['bytes']>0:
     label=x['page'];initial_size=next(v['bytes']for v in row['before']if v['label']==label)
     if initial_size==row['targetOldBytes']:
      reference=a[x['bufferOffset']:x['bufferOffset']+x['bytes']];assert packets[(row['id'],label,x['phase'])]==reference
 assert end==len(data)
 before=json.loads((case/'source-identity-before.json').read_text());after=json.loads((case/'source-identity-after.json').read_text());assert all(before[k]==v for k,v in after.items())
 return {'case':case.name,'pass':True,'baseline':baseline,'initial_allocation_only_variant':initial,'cases':16,'nominal_budget_denials':8,'lost_resident_meshes_on_budget_denial':lost,'unnecessary_placeholder_evictions':unneeded,'paired_cpu_gpu_reads':len(d['readbacks']),'different_bytes':0,'raw_readback_bytes':len(data),'raw_sha256':sha(data),'fixture_class_sha256':d['fixtureClassSha256'],'independent_farthest_first_budget_and_survivor_oracle':True,'all_target_and_survivor_gpu_checks_pass':True,'normal_save_shutdown':True,'new_crashes':0,'routes':normal,'scope':'Private real-mesher production upload/eviction with actual32MiB residency; no discovery/travel/visible-scene, allocationfailure atomicity, broad memory/FPS acceptance'},packets
if __name__=='__main__':
 old,payloads=audit(Path(sys.argv[1]),True);out=[]
 for p in sys.argv[2:]:
  row,packets=audit(Path(p));assert row['fixture_class_sha256']==old['fixture_class_sha256']
  assert all(v==packets[k]for k,v in payloads.items()),'Shared before/after/survivor packet bytes changed'
  out.append(row)
 print(json.dumps({'pass':True,'baseline':old,'candidates':out,'same_native_fixture_class':True,'shared_before_after_and_survivor_packets_match_baseline':True},indent=2))
