from pathlib import Path
import json,hashlib,gzip,re
from PIL import Image

def audit_order(d,c,backend):
    assert c.count('WATER_PAGE_ORDER_NATIVE_PASS')==3 and c.count('WATER_PAGE_ORDER_FRAME_PASS')==9
    assert c.count('WATER_ORDER_PIXEL_OBSERVED order=near-first rgba=128,0,0,128')==3
    assert c.count('WATER_ORDER_PIXEL_OBSERVED order=far-first rgba=128,0,64,192')==3
    for name,waves,lod in [('',False,True),('waves-enabled',True,True),('lod-disabled',False,False)]:
        p=d/name;record=(p/'water-page-order.txt').read_text()
        match=re.fullmatch(r'WATER_PAGE_ORDER_NATIVE_PASS liveFrames=3 warmupStableFrames=12 mapPermutations=near-far-near minVisible=([0-9]+) screenshots=3 productionPixelAB=true originalMapOrderRestored=true noWorldEdits=true scope=pageOrderNotFullTransparencyOrFlickerAcceptance\n',record)
        assert match and int(match[1])>=8 and record.strip() in c
        assert (p/'water-page-order-state.txt').read_text()==f'WATER_PAGE_ORDER_STATE backend={backend} animated=true waves={str(waves).lower()} lod={str(lod).lower()} ownsWater=true stage=AfterTranslucentBlocks\n'
        lines=(p/'water-page-order-frames.txt').read_text().splitlines();assert len(lines)==3
        cameras=[];times=[]
        for i,line in enumerate(lines,1):
            assert f'frame={i} ' in line and line in c
            cameras.append(re.search(r'camera=(.*?) waterBytes=',line)[1]);times.append(int(re.search(r'nanos=([0-9]+)',line)[1]))
            assert 0<int(re.search(r'waterBytes=([0-9]+)',line)[1])<=33554432
            assert int(re.search(r'loadedChunks=([0-9]+)',line)[1])>0
            assert re.search(r'waveBytes=([0-9]+)',line) is not None
            assert ('lodActive='+str(lod).lower()) in line
            if not lod: assert 'lodBytes=0 lodVisible=0' in line
        assert len(set(cameras))==1 and times[0]<times[1]<times[2]
        for n,rgba in [('near-first',(128,0,0,128)),('far-first',(128,0,64,192))]:
            with Image.open(p/f'order-pixels-{n}.png') as im:assert im.size==(32,32) and im.getpixel((16,16))==rgba
        for i in range(1,4):
            with Image.open(p/f'screenshots/order-live-frame-{i}.png') as im:assert im.size==(854,480)
        assert (p/'water-order-pixels.txt').read_text()=='WATER_ORDER_PIXEL_AB_PASS nearFirst=128,0,0,128 farFirst=128,0,64,192 productionPipeline=true scope=twoControlledOverlappingWaterQuads\n'

R=Path(__file__).resolve().parent;d=R/'vulkan-reuse-gl-order-validated';r=json.loads((d/'result.json').read_text());seal=json.loads((d/'sealed-console.json').read_text());z=(d/'console-final.gz').read_bytes();b=gzip.decompress(z);c=b.decode()
assert b==(d/'console.log').read_bytes() and hashlib.sha256(b).hexdigest()==r['console_sha256']==seal['raw_sha256']
assert hashlib.sha256(z).hexdigest()==seal['gzip_sha256'] and seal['stable_samples']>=4
assert r['exit_code']==0 and not r['timeout'] and r['build_successful']
assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed']
assert not re.search(r'AssertionError|Exception in thread|Preparing crash report|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor|GL_INVALID_|OpenGL debug message',c)
reuse='WATER_GPU_REUSE_NATIVE_PASS targetBytes=46080 ballastBytes=33508224 updates=16 smallerAndRegrowth=true boundedGrowthRetry=true actualGpuPayloadBytes=288 isolatedGpuReleased=true scope=controlledGpuPagesNotLiveSceneFlickerAcceptance\n'
assert (d/'water-partial-fade.txt').read_text().strip() in c and c.count('WATER_PARTIAL_GPU_SAMPLE_PASS')==6
assert (d/'water-gpu-reuse.txt').read_text()==reuse and c.count(reuse.strip())==1
for file in ['water-fade-continuity.txt','saved-disconnect.txt']:
 text=(d/file).read_text();assert text.strip() in c
for text in ['BACKEND_SELECTION_PASS backend=OpenGL','WATER_GPU_REUSE_OBSERVED targetBytes=46080 ballastBytes=33508224 resident=33554304 accepted=true','WATER_GPU_REUSE_CAPACITY_PASS updates=16 smaller=true regrowth=true oversizedRetry=true closerCoveragePreserved=true','RESOURCE_RELOAD_PASS','All dimensions are saved','CLIENT_SAVED_DISCONNECT_PASS','BUILD SUCCESSFUL']:
 assert text in c,text
assert c.index('All dimensions are saved')<c.index('CLIENT_SAVED_DISCONNECT_PASS')
assert '-PgraphicsBackend=opengl' in json.loads((d/'command.json').read_text())
for f in ['startup-marker-before.json','startup-marker-after.json']:assert not json.loads((d/f).read_text())['present']
actions=[a['action'] for a in json.loads((d/'action-history.json').read_text())];required=['backend-check=OpenGL','water-view=true,false,true','audit-lake-camera','water-page-order-check','water-view=true,true,true','water-page-order-check=waves-enabled','water-view=true,false,false','water-page-order-check=lod-disabled','water-view=true,false,true','water-partial-fade-check','water-gpu-reuse-check','water-fade-continuity-check','reuse-gl-recovery','reload-check'];assert actions in [required,required+['stop']]
assert len(list((d/'screenshots').glob('*.png')))==4
audit_order(d,c,'OpenGL')
report={'case':d.name,'pass':True,'backend':'OpenGL software llvmpipe','scope':'Validation-only page order and actual frames in three wave/LOD profiles, production-pipeline pixel A/B; no production fix or full scene acceptance. Also mixed-page fade retention, six GPU readbacks of geometry/original opacity timing, and shared water buffer update API, actual near-capacity allocations, same/smaller/regrowth/bounded growth retry, 288-byte fenced GPU readback, prior fade checks, reload/capture/normal save. Not full OpenGL performance or visual acceptance.','started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'console_sha256':r['console_sha256'],'sealed_console_gzip_sha256':seal['gzip_sha256'],'new_crashes':0,'save_errors':0,'owner_gpu_fps_acceptance':False}
(R/'strict-opengl-reuse-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
