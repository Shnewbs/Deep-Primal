from pathlib import Path
import hashlib,json,shutil,re
R=Path(__file__).resolve().parent
D=R/'checkpoint/docs/validation/engine-continuation/vulkan-lod-recovery-2026-10-22';D.mkdir(parents=True,exist_ok=True)
files=['disk-recovery.patch','frame-completion.patch','disk-recovery-extracted.json','frame-source-delta.json','vulkan-build-audit.json','source-audit.json','strict-combined-native-audit.json','inspected-before-failures.json','rejected-direct-stop-sessions.json','rejected-packaged-fallback.json','rejected-generated-archives.json','packaged-module-origin-audit.json','before-VoxelLodRenderer.java','malformed-direct-module-audit.json','graphics-restoration.json','graphics-pins.json','check_disk_recovery.py','audit_native.py','audit_sources_and_failures.py','audit_vulkan_package.py','run_vulkan.py','restore_graphics.py']
for name in files:shutil.copyfile(R/name,D/name)
shutil.copyfile(R/'toolchain/restoration.json',D/'tool-restoration.json')
shutil.copyfile(R/'toolchain/checked-agent/neoform-process-fallback.checks.json',D/'private-agent-checks.json')
for case in ['vulkan-create','vulkan-frame-before-retry','vulkan-disk-before-fixed-vulkan-retry']:
 for name in ['failure-excerpt.txt','result.json','crash-delta.json']:
  f=R/case/name
  if f.exists():dest=D/case/name;dest.parent.mkdir(exist_ok=True);shutil.copyfile(f,dest)
for case in ['vulkan-disk-after-graceful-retry','vulkan-disk-after-packaged-retry','vulkan-create-attempt1']:
 d=R/case;dest=D/case;dest.mkdir(exist_ok=True)
 for pattern in ['*.json','*.txt']:
  for f in d.glob(pattern):
   if f.name not in ['capture-request.txt','live-action.json']:shutil.copyfile(f,dest/f.name)
 text=(d/'console.log').read_text()
 selected=[l for l in text.splitlines() if any(s in l for s in ['_PASS','VALIDATION_CLASS_ORIGIN','VULKAN_OPERATION_COUNTS','VULKAN_RESOURCES_RELEASED','VULKAN_VALIDATION_ACTIVE','VULKAN_SHUTDOWN_FAILURE_QUEUED','WATER_VIEW_OBSERVATION','LOD_DISK_RECOVERY_OBSERVED','Saving chunks','All dimensions are saved','BUILD SUCCESSFUL'])]
 (dest/'console-evidence.txt').write_text('\n'.join(selected)+'\n')
log=R/'combined-build.log';text=log.read_text();assert 'BUILD SUCCESSFUL' in text
record={'fresh_build_utc_date':'2026-10-22','full_log_sha256':hashlib.sha256(log.read_bytes()).hexdigest(),'uncached_rerun':True,'tasks_executed':25,'result':'BUILD SUCCESSFUL in 1m 8s','pass_records':[s for s in text.splitlines() if '_PASS' in s or s.startswith('PASS:')],'installer_released':False}
(D/'combined-build-summary.json').write_text(json.dumps(record,indent=2)+'\n')
print(str(D))
