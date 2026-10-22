"""Accept only focused lifecycle gates and clean native completion; retain broader marker defect."""
from pathlib import Path
import json,hashlib,re,sys,gzip
R=Path(__file__).resolve().parent
names=sys.argv[1:] or ['vulkan-marker-rearm-stack-retry','vulkan-marker-rearm-packaged-sealed-retry','vulkan-marker-unforced-after']
rows=[]
for name in names:
 d=R/name;r=json.loads((d/'result.json').read_text())
 sealed_path=d/'console-final.gz'
 raw=gzip.decompress(sealed_path.read_bytes()) if sealed_path.exists() else (d/'console.log').read_bytes()
 c=raw.decode()
 if sealed_path.exists():
  seal=json.loads((d/'sealed-console.json').read_text());assert seal['stable_samples']>=4,name
  assert hashlib.sha256(sealed_path.read_bytes()).hexdigest()==seal['gzip_sha256'],name
  assert hashlib.sha256(raw).hexdigest()==seal['raw_sha256'] and len(raw)==seal['raw_bytes'],name
 assert r['exit_code']==0 and not r['timeout'] and r['build_successful'],name
 assert hashlib.sha256(raw).hexdigest()==r['console_sha256'],name
 assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed'],name
 assert not re.search(r'VUID-|VULKAN_VALIDATION ERROR|AssertionError|Failed to start FML|Preparing crash report|Exception in thread|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor',c),name
 rearm='rearm' in name
 for marker in ['BACKEND_SELECTION_PASS backend=Vulkan','VULKAN_VALIDATION_ACTIVE true','RESOURCE_RELOAD_PASS','CLIENT_SAVED_DISCONNECT_PASS','VULKAN_RESOURCES_RELEASED buffers=0 images=0','validationErrors=0','Saving chunks for level','All dimensions are saved','BUILD SUCCESSFUL']:
  assert marker in c,(name,marker)
 if rearm:
  assert (d/'startup-rearm.txt').read_text()=='STARTUP_REARM_NATIVE_PASS backend=Vulkan cycles=32 markerCleared=true\n',name
  assert c.count('STARTUP_REARM_NATIVE_PASS backend=Vulkan cycles=32 markerCleared=true')==1,name
 assert (d/'saved-disconnect.txt').read_text()=='CLIENT_SAVED_DISCONNECT_PASS\n',name
 assert (d/'shutdown.txt').read_text()=='VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true followingCompletion=true\n',name
 assert c.count('VULKAN_SHUTDOWN_FAILURE_QUEUED')==1,name
 assert c.rfind('All dimensions are saved')<c.index('CLIENT_SAVED_DISCONNECT_PASS'),name
 observations=(d/'startup-lifecycle.txt').read_text().splitlines();assert all(line in c for line in observations),name
 observed={}
 for line in observations:
  fields=dict(re.findall(r'(\w+)=([^ ]+)',line));observed[fields['label']]=fields
 for label in ['joined','reloaded','before-disconnect','after-disconnect']+(['before-rearm','armed-again','rearm-first-frame'] if rearm else []):assert label in observed,(name,label)
 if rearm:
  assert observed['armed-again']['complete']=='false' and observed['armed-again']['markerPresent']=='true',name
  assert observed['rearm-first-frame']['complete']=='true' and observed['rearm-first-frame']['markerPresent']=='false',name
 else:
  before=json.loads((d/'startup-marker-before.json').read_text());after=json.loads((d/'startup-marker-after.json').read_text())
  assert not before['present'] and not before['explicit_retry'] and not after['present'],name
  assert all(x['markerPresent']=='false' and x['ownerMarkerPresent']=='false' and x['complete']=='true' for x in observed.values()),name
  command=json.loads((d/'command.json').read_text());assert '-PvulkanRetry' not in command,name
  assert not (d/'startup-rearm.txt').exists() and 'STARTUP_REARM_NATIVE_PASS' not in c,name
 if 'packaged' in name:assert all('.jar' in x['owner'] for x in observed.values()),name
 for marker in ['VULKAN_SHADER_CACHE_PASS','VULKAN_BUFFER_QUERY_PASS','VULKAN_DEPTH_READBACK_PASS','VULKAN_TEXTURE_COPY_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1','VULKAN_LIGHTMAP_RANGE']:assert marker in c,(name,marker)
 screenshots=list((d/'screenshots').glob('*.png'));assert screenshots,name
 rows.append({'case':name,'focused_gate_pass':True,'started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'console_sha256':r['console_sha256'],'sealed_console':sealed_path.exists(),'live_console_sha256_at_audit':hashlib.sha256((d/'console.log').read_bytes()).hexdigest(),'scope':'Actual startup owner in a loaded software-Vulkan device; explicit arm/firstFrame replay, not actual device recreation' if rearm else 'Consecutive actual unforced reopen with no explicit arm replay; marker absent at join/reload/disconnect and after exit','cycles':32 if rearm else 0,'observations':observed,'host_marker_before':json.loads((d/'startup-marker-before.json').read_text()),'host_marker_after':json.loads((d/'startup-marker-after.json').read_text()),'new_crashes':0,'save_errors':0,'vuid_errors':0,'validation_errors':0,'final_buffers':0,'final_images':0,'normal_saved_disconnect':True,'owner_gpu_fps_acceptance':False,'shader_output_warnings':c.count('VULKAN_VALIDATION WARNING-Shader-OutputNotConsumed'),'screenshots':[{'path':str(f.relative_to(R)),'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in screenshots]})
report={'all_focused_gates_pass':True,'engine_acceptance':False,'ordinary_marker_reappearance_cause':'unproven; do not attribute to rearm without a corresponding arm call','sessions':rows}
(R/'strict-startup-native-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
