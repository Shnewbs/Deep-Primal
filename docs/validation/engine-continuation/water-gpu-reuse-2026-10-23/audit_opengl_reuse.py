from pathlib import Path
import json,hashlib,gzip,re
R=Path(__file__).resolve().parent;d=R/'vulkan-reuse-gl-final2';r=json.loads((d/'result.json').read_text());seal=json.loads((d/'sealed-console.json').read_text());z=(d/'console-final.gz').read_bytes();b=gzip.decompress(z);c=b.decode()
assert b==(d/'console.log').read_bytes() and hashlib.sha256(b).hexdigest()==r['console_sha256']==seal['raw_sha256']
assert hashlib.sha256(z).hexdigest()==seal['gzip_sha256'] and seal['stable_samples']>=4
assert r['exit_code']==0 and not r['timeout'] and r['build_successful']
assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed']
assert not re.search(r'AssertionError|Exception in thread|Preparing crash report|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor|GL_INVALID_|OpenGL debug message',c)
reuse='WATER_GPU_REUSE_NATIVE_PASS targetBytes=46080 ballastBytes=33508224 updates=16 smallerAndRegrowth=true boundedGrowthRetry=true actualGpuPayloadBytes=288 isolatedGpuReleased=true scope=controlledGpuPagesNotLiveSceneFlickerAcceptance\n'
assert (d/'water-gpu-reuse.txt').read_text()==reuse and c.count(reuse.strip())==1
for file in ['water-fade-continuity.txt','saved-disconnect.txt']:
 text=(d/file).read_text();assert text.strip() in c
for text in ['BACKEND_SELECTION_PASS backend=OpenGL','WATER_GPU_REUSE_OBSERVED targetBytes=46080 ballastBytes=33508224 resident=33554304 accepted=true','WATER_GPU_REUSE_CAPACITY_PASS updates=16 smaller=true regrowth=true oversizedRetry=true closerCoveragePreserved=true','RESOURCE_RELOAD_PASS','All dimensions are saved','CLIENT_SAVED_DISCONNECT_PASS','BUILD SUCCESSFUL']:
 assert text in c,text
assert c.index('All dimensions are saved')<c.index('CLIENT_SAVED_DISCONNECT_PASS')
assert '-PgraphicsBackend=opengl' in json.loads((d/'command.json').read_text())
for f in ['startup-marker-before.json','startup-marker-after.json']:assert not json.loads((d/f).read_text())['present']
actions=[a['action'] for a in json.loads((d/'action-history.json').read_text())];required=['backend-check=OpenGL','water-gpu-reuse-check','water-fade-continuity-check','reuse-gl-recovery','reload-check'];assert actions in [required,required+['stop']]
assert len(list((d/'screenshots').glob('*.png')))==1
report={'case':d.name,'pass':True,'backend':'OpenGL software llvmpipe','scope':'Shared water buffer update API, actual near-capacity allocations, same/smaller/regrowth/bounded growth retry, 288-byte fenced GPU readback, prior fade checks, reload/capture/normal save. Not full OpenGL performance or visual acceptance.','started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'console_sha256':r['console_sha256'],'sealed_console_gzip_sha256':seal['gzip_sha256'],'new_crashes':0,'save_errors':0,'owner_gpu_fps_acceptance':False}
(R/'strict-opengl-reuse-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
