from pathlib import Path
import json,shutil,platform,time
r=Path(__file__).resolve().parent
workspace=Path('/workspace/scratch/c748d58856d9')
disk=shutil.disk_usage(workspace);memory=shutil.disk_usage(r)
result={'checked_at_utc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()),
 'workspace_available_bytes':disk.free,'private_memory_available_bytes':memory.free,
 'minimum_isolated_full_qualification_headroom_bytes':2*1024**3,
 'full_build_can_start':max(disk.free,memory.free)>=2*1024**3,
 'full_build_executed':False,
 'full_build_target':'Java25 Gradle9.2.1 --no-daemon --no-build-cache --max-workers=1 --rerun-tasks -PvulkanPlatform=windows :build :visualValidationClasses with all four staged Java files applied in a fresh isolated checkout',
 'reason':'Neither disk nor private memory has enough headroom for full isolated producer/module/archive/native qualification; direct core and adapter checks are not full Gradle or delivery acceptance.',
 'gpu_device_available':Path('/dev/dri').exists(), 'host':platform.platform(),
 'own_cleanup':['renderer-work/gui-themes/build','qualification/native-cache','renderer-work/build/generated'],
 'cleanup_reclaimed_bytes_claimed':0,'cleanup_result':'Directories initially disappeared but reappeared; reported disk availability remained zero. No durable reclamation claimed.'}
(r/'evidence/full-build-preflight.json').write_text(json.dumps(result,indent=2))
print(json.dumps(result,indent=2))
