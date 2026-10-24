from pathlib import Path
import json,hashlib,gzip,shutil
R=Path(__file__).resolve().parent;C=R/'checkpoint';D=C/'docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22';D.mkdir(parents=True,exist_ok=True)
files=['startup-rearm.patch','startup-source-delta.json','startup-source-extracted.json','before-Startup.java','temporary-startup-trace.patch','baseline-build-audit.json','vulkan-build-audit.json','packaged-module-identity.json','source-scope-audit.json','input-verification.json','strict-startup-native-audit.json','inspected-failures.json','baseline-rearm-crash-excerpt.txt','baseline-rearm-crash-digest.json','tracing-blocker.json','rejected-trace-setup.json','rejected-packaged-log-mutation.json','packaged-unforced-marker-failure.json','check_startup_lifecycle.py','audit_source_scope.py','audit_startup_native.py','audit_vulkan_package.py','run_vulkan.py','prepare_evidence.py']
for name in files:shutil.copyfile(R/name,D/name)
if (R/'rejected-reused-model-marker.json').exists():shutil.copyfile(R/'rejected-reused-model-marker.json',D/'rejected-reused-model-marker.json')
for name in ['vulkan-create','vulkan-marker-rearm-before-retry','vulkan-marker-strace-retry','vulkan-marker-rearm-after-retry','vulkan-marker-rearm-packaged-after-retry','vulkan-marker-packaged-unforced-after']:
 src=R/name;out=D/name;out.mkdir(exist_ok=True)
 for f in src.glob('*.json'):
  if f.name!='live-action.json':shutil.copyfile(f,out/f.name)
 for n in ['startup-lifecycle.txt','world-creation.txt']:
  if (src/n).exists():shutil.copyfile(src/n,out/n)
 text=(src/'console.log').read_text();selected=[x for x in text.splitlines() if any(s in x for s in ['STARTUP_TRACE','STARTUP_LIFECYCLE','AssertionError','VULKAN_TEXTURE_COPY_PASS','VULKAN_ATLAS_RANGE','BUILD SUCCESSFUL','BUILD FAILED','fatal library error','ptrace'])]
 (out/'console-evidence.txt').write_text('\n'.join(selected)+'\n')
manifest=[]
accepted=json.loads((R/'strict-startup-native-audit.json').read_text())['sessions']
for row in accepted:
 name=row['case'];src=R/name;out=D/name;out.mkdir(exist_ok=True)
 for pattern in ['*.json','*.txt','*.gz']:
  for f in src.glob(pattern):
   if f.name not in ['capture-request.txt','live-action.json']:shutil.copyfile(f,out/f.name)
 raw=gzip.decompress((src/'console-final.gz').read_bytes()) if (src/'console-final.gz').exists() else (src/'console.log').read_bytes()
 assert hashlib.sha256(raw).hexdigest()==row['console_sha256'],name
 # Immutable evidence log is canonical sealed bytes; do not substitute a later mutable log.
 (out/'console.log').write_bytes(raw)
 selected=[x for x in raw.decode().splitlines() if any(s in x for s in ['_PASS','STARTUP_LIFECYCLE','VULKAN_ATLAS_RANGE','VULKAN_RESOURCES_RELEASED','VULKAN_OPERATION_COUNTS','VULKAN_VALIDATION_ACTIVE','Saving chunks','All dimensions are saved','BUILD SUCCESSFUL'])]
 (out/'console-evidence.txt').write_text('\n'.join(selected)+'\n')
 for f in (src/'screenshots').glob('*.png'):
  # Keep one actual image for each loaded origin; unforced repeats retain hashes only.
  keep='rearm' in name
  if keep:(out/'screenshots').mkdir(exist_ok=True);shutil.copyfile(f,out/'screenshots'/f.name)
  b=f.read_bytes();manifest.append({'case':name,'path':name+'/screenshots/'+f.name,'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest(),'git_blob_sha':hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest(),'image_preserved':keep,'visually_inspected':keep,'observation':'Upright local terrain/trees visible. This spawn capture does not certify water/LOD coverage, identical-state output or owner GPU/FPS.' if keep else 'Hash retained; repeat image not used for additional visual acceptance.'})
log=R/'fixed-build.log';text=log.read_text();assert 'BUILD SUCCESSFUL in 1m 6s' in text
(D/'build-summary.json').write_text(json.dumps({'scope':'uncached final-source Linux Vulkan full build','date_utc':'2026-10-22','all_25_tasks_executed':True,'full_log_sha256':hashlib.sha256(log.read_bytes()).hexdigest(),'pass_records':[x for x in text.splitlines() if '_PASS' in x or x.startswith('PASS:')],'result':'BUILD SUCCESSFUL in 1m 6s','installer_released':False},indent=2)+'\n')
(D/'screenshot-manifest.json').write_text(json.dumps({'images':manifest,'software_driver':True,'owner_gpu_fps_acceptance':False},indent=2)+'\n')
for name in ['renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/Startup.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WorldCreationChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/StartupLifecycleChecks.java']:
 f=C/name;f.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/'project'/name,f)
print(str(D))
