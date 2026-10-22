"""Strictly audit completed sessions; launcher success alone is insufficient."""
from pathlib import Path
import json,hashlib,re,sys
R=Path(__file__).resolve().parent
cases=sys.argv[1:] or ['vulkan-disk-after-graceful-retry','vulkan-disk-after-packaged-retry']
records=[]
for name in cases:
 d=R/name;report=json.loads((d/'result.json').read_text());console=(d/'console.log').read_text()
 assert report['exit_code']==0 and not report['timeout'] and report['build_successful'],name
 assert hashlib.sha256((d/'console.log').read_bytes()).hexdigest()==report['console_sha256'],name
 assert 'BUILD SUCCESSFUL' in console and 'BACKEND_SELECTION_PASS backend=Vulkan' in console,name
 assert 'VULKAN_VALIDATION_ACTIVE true' in console,name
 assert not re.search(r'VUID-|VULKAN_VALIDATION ERROR|AssertionError|Failed to start FML|Preparing crash report|Exception in thread|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor',console),name
 assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed'],name
 for marker in ['VULKAN_RESOURCES_RELEASED buffers=0 images=0','validationErrors=0','RESOURCE_RELOAD_PASS','Saving chunks for level','All dimensions are saved']:
  assert marker in console,(name,marker)
 for filename,marker in [('frame-completion.txt','VULKAN_FRAME_COMPLETION_NATIVE_PASS iterations=8 mappedBytes=128 openPassRejections=8 backend=Vulkan'),('shutdown.txt','VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true followingCompletion=true')]+([('lod-disk-recovery.txt','LOD_DISK_RECOVERY_NATIVE_PASS backend=Vulkan retainedAfterOutside=1 diskReads=1 uploadedChunks=1 restored=true isolatedGpuReleased=true')] if 'disk' in name else []):
  text=(d/filename).read_text();assert text.count(marker)==1 and (console.count(marker)==1 if filename!='shutdown.txt' else console.count('VULKAN_SHUTDOWN_FAILURE_QUEUED')==1),(name,filename)
 assert (d/'saved-disconnect.txt').read_text()=='CLIENT_SAVED_DISCONNECT_PASS\n' and console.count('CLIENT_SAVED_DISCONNECT_PASS')==1,name
 assert console.rfind('All dimensions are saved')<console.index('CLIENT_SAVED_DISCONNECT_PASS'),name
 for marker in ['VULKAN_SHADER_CACHE_PASS','VULKAN_BUFFER_QUERY_PASS bytes=16','VULKAN_STORAGE_BYTES_PASS','VULKAN_DEPTH_READBACK_PASS','VULKAN_TEXTURE_COPY_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1','VULKAN_LIGHTMAP_RANGE']:
  assert marker in console,(name,marker)
 histories=json.loads((d/'action-history.json').read_text());assert histories and all(x['completed'] for x in histories),name
 origins=[l for l in console.splitlines() if 'VALIDATION_CLASS_ORIGIN' in l]
 assert len(origins)==2,name
 if 'packaged' in name:assert all('.jar' in l and '/build/classes/java/main/' not in l for l in origins),(name,origins)
 pngs=list((d/'screenshots').glob('*.png'));assert pngs,name
 records.append({'case':name,'started_utc':report.get('started_utc'),'finished_utc':report.get('finished_utc'),'strict_pass':True,'class_origins':origins,'startup_marker_before':json.loads((d/'startup-marker-before.json').read_text()),'startup_marker_after':json.loads((d/'startup-marker-after.json').read_text()),'elapsed_seconds':report['elapsed_seconds'],'console_sha256':report['console_sha256'],'explicit_regression_records_match_console':True,'normal_world_save':True,'forced_shutdown_record_file_pass':True,'new_crashes':0,'vuid_errors':0,'validation_errors':0,'final_buffers':0,'final_images':0,'nonfatal_shader_output_warnings':console.count('VULKAN_VALIDATION WARNING-Shader-OutputNotConsumed'),'screenshots':[{'path':str(f.relative_to(R)),'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in pngs],'software_driver':'Mesa llvmpipe 25.2.8','owner_gpu_fps_acceptance':False})
result={'scope':'native Vulkan disk-store/upload and fenced completion fixtures; direct camera-position selection is not full physical teleport coverage','sessions':records,'all_pass':True}
(R/'strict-combined-native-audit.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
