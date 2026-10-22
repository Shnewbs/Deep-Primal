from pathlib import Path
import json,hashlib,re
R=Path(__file__).resolve().parent
cases=['vulkan-create-attempt2','vulkan-scene-retry','vulkan-view-final','vulkan-smoke']
rows=[]
for case in cases:
 d=R/case;text=(d/'console.log').read_text();result=json.loads((d/'result.json').read_text())
 assert result['exit_code']==0 and result['build_successful'] and not result['timeout'],case
 if (d/'crash-delta.json').exists():assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed'],case
 else:
  prior=json.loads((R/'vulkan-scene/result.json').read_text())['crash_reports']
  assert not result['crash_reports'] or result['crash_reports']==prior,(case,result['crash_reports'])
 assert 'Preparing crash report' not in text and 'crash report has been saved' not in text,case
 assert 'VULKAN_VALIDATION_ACTIVE true' in text,case
 assert 'VULKAN_RESOURCES_RELEASED buffers=0 images=0' in text,case
 assert 'validationErrors=0' in text,case
 assert not re.search(r'VUID-|AssertionError|Encountered an unexpected exception',text),case
 assert (d/'shutdown.txt').read_text().strip()=='VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true followingCompletion=true',case
 assert text.index('VULKAN_SHUTDOWN_FAILURE_QUEUED')<text.index('VULKAN_RESOURCES_RELEASED buffers=0 images=0')<text.index('BUILD SUCCESSFUL'),case
 if 'create' in case:
  assert 'WORLD_CREATION_JOIN_PASS revision=3 backend=Vulkan' in text
  assert 'WORLD_CREATION_PASS' in (d/'world-creation.txt').read_text()
 else:assert 'BACKEND_SELECTION_PASS backend=Vulkan' in text,case
 if case in ['vulkan-scene-retry','vulkan-view-final']:
  for marker in ['VULKAN_SHADER_CACHE_PASS','VULKAN_BUFFER_QUERY_PASS','VULKAN_STORAGE_BYTES_PASS','VULKAN_DEPTH_READBACK_PASS','VULKAN_TEXTURE_COPY_PASS','VULKAN_STENCIL_PASS format=DEPTH24_STENCIL8 reference=0','VULKAN_STENCIL_PASS format=DEPTH24_STENCIL8 reference=1','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=0','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1']:assert marker in text,(case,marker)
 if case=='vulkan-view-final':
  assert 'RESOURCE_RELOAD_PASS' in text and 'MEMORY_GUARD_CHECKS_PASS' in text
  for name in ['enabled-animated','enabled-fallback','enabled-waves','diagnostic-no-lod','enabled-reloaded']:
   assert 'DEEPPRIMAL_CAPTURE_SAVED '+name in text
   assert (d/'screenshots'/(name+'.png')).is_file()
  for name in ['enabled-animated','enabled-reloaded']:
   obs=(d/(name+'-observation.txt')).read_text()
   assert 'backend=Vulkan animated=true waves=false lod=true ownsWater=true' in obs,obs
   assert int(re.search(r'waterBytes=(\d+)',obs)[1])>0,obs
   assert int(re.search(r'drawnPages=(\d+)',obs)[1])>0,obs
  obs=(d/'enabled-fallback-observation.txt').read_text()
  assert 'backend=Vulkan animated=false waves=false lod=true ownsWater=false waterBytes=0' in obs,obs
 rows.append({'case':case,'acceptance':True,'console_sha256':hashlib.sha256((d/'console.log').read_bytes()).hexdigest(),'shutdown_file_sha256':hashlib.sha256((d/'shutdown.txt').read_bytes()).hexdigest(),'zero_vuid':True,'zero_validation_errors':True,'zero_tracked_buffers_images':True,'shader_output_not_consumed_warnings':text.count('WARNING-Shader-OutputNotConsumed:')})
(R/'strict-vulkan-audit.json').write_text(json.dumps({'software_driver':True,'owner_gpu_fps_acceptance':False,'complete_enabled_view_coverage_acceptance':False,'cases':rows},indent=2)+'\n')
print('STRICT_VULKAN_AUDIT_PASS',len(rows))
