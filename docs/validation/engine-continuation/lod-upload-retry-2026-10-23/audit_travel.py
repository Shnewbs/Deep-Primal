from pathlib import Path
import json,hashlib,gzip,re,sys
R=Path(__file__).resolve().parent
rows=[]
for case in sys.argv[1:]:
 d=R/case;r=json.loads((d/'result.json').read_text());s=json.loads((d/'sealed-console.json').read_text());b=(d/'console-final.gz').read_bytes();raw=gzip.decompress(b);c=raw.decode()
 assert hashlib.sha256(b).hexdigest()==s['gzip_sha256']
 assert hashlib.sha256(raw).hexdigest()==s['raw_sha256']==r['console_sha256']
 assert (d/'console.log').read_bytes()==raw and s['stable_samples']>=4
 assert r['exit_code']==0 and r['build_successful'] and not r['timeout']
 assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed']
 assert not re.search(r'VUID-|VULKAN_VALIDATION ERROR|AssertionError|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor|Exception in thread',c)
 record=(d/'lod-travel-pass.txt').read_text();assert c.count(record.strip())==1
 for token in ['authoritativeTeleports=3','clientUnload=true','serverBlockEdits=2','diskStateMatches=true','reload=true'] :assert token in record
 for token in ['BACKEND_SELECTION_PASS backend=Vulkan','VULKAN_VALIDATION_ACTIVE true','VULKAN_RESOURCES_RELEASED buffers=0 images=0','validationErrors=0','VULKAN_SHADER_CACHE_PASS','VULKAN_BUFFER_QUERY_PASS bytes=16','VULKAN_DEPTH_READBACK_PASS','VULKAN_TEXTURE_COPY_PASS','VULKAN_STENCIL_PASS','VULKAN_LIGHTMAP_RANGE','VULKAN_SHUTDOWN_FAILURE_QUEUED','All dimensions are saved','CLIENT_SAVED_DISCONNECT_PASS','BUILD SUCCESSFUL']:assert token in c,(case,token)
 for f,recordMarker in [('saved-disconnect.txt','CLIENT_SAVED_DISCONNECT_PASS'),('shutdown.txt','VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true followingCompletion=true'),('frame-completion.txt','VULKAN_FRAME_COMPLETION_NATIVE_PASS iterations=8 mappedBytes=128 openPassRejections=8 backend=Vulkan')]:assert (d/f).read_text()==recordMarker+'\n'
 assert c.index('LOD_TRAVEL_NATIVE_PASS')<c.rfind('All dimensions are saved')<c.index('CLIENT_SAVED_DISCONNECT_PASS')<c.index('VULKAN_RESOURCES_RELEASED')<c.index('BUILD SUCCESSFUL')
 assert not json.loads((d/'startup-marker-before.json').read_text())['present'] and not json.loads((d/'startup-marker-after.json').read_text())['present']
 assert '-PvulkanRetry' not in json.loads((d/'command.json').read_text())
 origins=[line for line in c.splitlines() if 'VALIDATION_CLASS_ORIGIN' in line]
 if 'packaged' in case:assert len(origins)==2 and all('.jar' in line and '/build/classes/java/main/' not in line for line in origins)
 phases=re.findall(r'^LOD_TRAVEL_PHASE phase=(\d+)$',c,re.M);assert phases==list(map(str,range(7)))
 history=[e['action'] for e in json.loads((d/'action-history.json').read_text())]
 required=['backend-check=Vulkan','lod-travel-check','travel-recovery','vulkan-frame-completion-check','vulkan-checks'];assert history in [required,required+['stop'],required[:2]+['audit-lake-camera','water-view-observe=travel-final']+required[2:],required[:2]+['audit-lake-camera','water-view-observe=travel-final']+required[2:]+['stop']]
 fields=dict(re.findall(r'(samples|peakLodBytes|peakWaterBytes|diskReadsDelta|capturesDelta)=(\d+)',record));fields={k:int(v) for k,v in fields.items()}
 assert 0<fields['peakLodBytes']<=4194304 and 0<fields['peakWaterBytes']<=32*1048576 and fields['capturesDelta']>0
 pngs=list((d/'screenshots').glob('*.png'));assert pngs
 rows.append({'case':case,'strict_pass':True,'record':record.strip(),'metrics':fields,'utc':[r['started_utc'],r['finished_utc']],'console_sha256':s['raw_sha256'],'compressed_console_sha256':s['gzip_sha256'],'marker_absent_before_after':True,'zero_errors_resources':True,'software_driver':True,'owner_gpu_fps_acceptance':False,'screenshots':[{'path':str(p.relative_to(R)),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in pngs]})
report={'scope':'Actual server commands and ordinary authoritative setBlock edits delivered to a native client, observed client unload, persisted snapshot state and resource reload under 4 MiB real-scene LOD payload pressure. Not simulated mouse/block-item input, server physical unload acceptance, pixel-complete visual coverage, regional mass accounting or owner GPU/FPS acceptance.','sessions':rows}
(R/'strict-travel-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
