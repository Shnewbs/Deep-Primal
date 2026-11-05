from audit_side_material import audit_side_material
from pathlib import Path
import json,hashlib,gzip,re,struct
from PIL import Image
from audit_material import audit_material



def audit_blend(d,c):
    record=(d/'water-direction-blend-pass.txt').read_text().strip()
    assert record=='WATER_DIRECTION_BLEND_NATIVE_PASS samples=240 renders=336 directions=16 origins=2 mipControls=true oldAndNewEndpoints=true intermediateRGBA=true tolerance=1 isolatedGpuReleased=true scope=controlledProductionShaderNotFullStreamAcceptance'
    assert c.count(record)==1
    j=json.loads((d/'water-direction-blend-details.json').read_text());assert len(j)==240
    for n,row in enumerate(j):
        assert row['maxChannelError']<=1 and row['channelsBeyondTolerance']==0
        assert row['cellX']==row['cellZ']==(0 if n<80 else 15)
    for group in range(48):
        a=Image.open(d/f'direction-blend-{group*7}.png').convert('RGBA');b=Image.open(d/f'direction-blend-{group*7+1}.png').convert('RGBA')
        assert a.size==(32,32) and b.size==(32,32) and a.getchannel('A').getextrema()==(255,255)
        aa=a.tobytes();bb=b.tobytes()
        for phase in range(5):
            cc=Image.open(d/f'direction-blend-{group*7+phase+2}.png').convert('RGBA').tobytes()
            t=phase*.25
            assert max(abs(x-round(y*(1-t)+z*t)) for x,y,z in zip(cc,aa,bb))<=1
    line=(d/'water-direction-upload-pass.txt').read_text().strip()
    assert line=='WATER_DIRECTION_UPLOAD_NATIVE_PASS samples=7 readbackBytes=5040 vertices=140 repeatedUpload=true latestQueued=true staleRevision=true lightPreserved=true historyCap=32768 historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploads' and c.count(line)==1
    data=(d/'water-direction-upload.bin').read_bytes();rows=json.loads((d/'water-direction-upload-details.json').read_text());assert len(data)==5040 and len(rows)==7
    for n,row in enumerate(rows):
        for v in range(20):
            off=(n*20+v)*36;light,sky=struct.unpack_from('<HH',data,off+28)
            assert (light&255)==176 and (sky&255)==240
            active=bool(sky&32768);assert active==(v<4 and row['active'])
            assert data[off+15]==(51 if active else 255)
            if active:assert light>>11==row['source'] and ((sky>>8)&127)==row['age']

def audit_phase(d,c):
    line=(d/'water-flow-phase-pass.txt').read_text().strip()
    assert line=='WATER_FLOW_PHASE_PIXEL_NATIVE_PASS pairs=12 renders=24 directions=0,2,4,6,10,14 worldPhase=true explicitGradientMipControl=true baseExact=true mipTolerance=1 isolatedGpuReleased=true scope=controlledPhaseHelperAndProductionShaderNotFullStreamAcceptance'
    assert c.count(line)==1
    rows=json.loads((d/'water-flow-phase-pixel-details.json').read_text());assert len(rows)==12
    for i,row in enumerate(rows):
        assert row['beyondTolerancePixels']==0 and row['coveredPixels']==1024
        assert row['tolerance']==(0 if i<6 else 1) and row['maxChannelError']<=row['tolerance']
        with Image.open(d/f'flow-phase-{i*2}.png') as a,Image.open(d/f'flow-phase-{i*2+1}.png') as b:
            assert a.size==(32,32) and b.size==(32,32)
            if i<6:assert a.tobytes()==b.tobytes()
            else:assert max(abs(x-y) for x,y in zip(a.tobytes(),b.tobytes()))<=1
            assert a.getchannel('A').getextrema()==(255,255)
            if i<6:assert len(a.convert('RGB').getcolors(2048))>100
            else:assert all(r<=1 and g>=254 and blue<=1 and alpha==255 for r,g,blue,alpha in a.convert('RGBA').get_flattened_data())

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

def audit_flow(d,c):
    assert c.count('WATER_FLOW_TEXTURE_NATIVE_PASS')==2
    assert c.count('WATER_FLOW_CAPTURE_OBSERVED topUvEqual=false sourceDirections=0,4,16 actualCapture=true clientStateRestored=true unitsUnchanged=true')==2
    for label in ['', 'after-reload']:
        p=d/label;record=(p/'water-flow-texture.txt').read_text()
        m=re.fullmatch(r'WATER_FLOW_TEXTURE_NATIVE_PASS directions=0,4,16 samples=3 topVertices=([0-9]+) readbackBytes=([0-9]+) actualCapture=true geometryAndQuantityUnchanged=true clientStateRestored=true isolatedGpuReleased=true scope=actualCapturedCellAndGpuUvsNotLiveFlowFlickerAcceptance\n',record)
        assert m and int(m[1])>=12 and 0<int(m[2])<=3*33554432 and record.strip() in c
        raw=(p/'selected-flow-gpu-vertices.bin').read_bytes();j=json.loads((p/'selected-flow-gpu-uv.json').read_text());rows=j['vertices']
        assert len(raw)==36*int(m[1])==36*len(rows) and j['vertexStrideBytes']==36 and j['byteOrder']=='LITTLE_ENDIAN'
        assert 'deepprimal:engine_water' in j['sourceRestored'] or 'deepprimal:water' in j['sourceRestored'],j['sourceRestored']
        counts={}
        for i,row in enumerate(rows):
            direction=row['direction'];counts[direction]=counts.get(direction,0)+1
            assert direction in [0,4,16] and row['sprite']==('water_still' if direction==16 else 'water_flow')
            u,v=struct.unpack_from('<ff',raw,i*36+16)
            assert abs(u-row['u'])<1e-7 and abs(v-row['v'])<1e-7 and abs(u-row['expectedU'])<=1e-6 and abs(v-row['expectedV'])<=1e-6
            lm,sk=struct.unpack_from('<HH',raw,i*36+28)
            if sk&32768:
                assert (direction==4 and lm>>11==0) or (direction==16 and lm>>11==4)
                quad=rows[(i//4)*4:(i//4+1)*4]
                cx=int(min(v['x'] for v in quad));cz=int(min(v['z'] for v in quad))
                assert raw[i*36+15]==(cx|(cz<<4))
            else:assert raw[i*36+15]==255
        assert set(counts)=={0,4,16} and min(counts.values())>=4


def audit_side(d,c):
    from PIL import Image
    record=(d/'water-side-uv-pixels.txt').read_text()
    assert record=='WATER_SIDE_UV_PIXEL_RESULT pass=true pairs=6 renders=12 differentPixels=0 coveredPixels=1152 geometryMatched=true scope=controlledProductionShaderNotLiveStreamAcceptance\n'
    assert c.count(record.strip())==1 and c.count('WATER_SIDE_UV_PIXEL_NATIVE_PASS')==1
    details=json.loads((d/'water-side-uv-pixel-details.json').read_text());assert len(details)==6
    for i,row in enumerate(details):
        assert row['pair']==i and row['differentPixels']==0 and row['coveredPixels']==[128,192,256][i%3]
        assert row['flowing']==(i>=3) and row['elapsedMillis']==(i%3)*50
        with Image.open(d/('side-uv-'+str(i*2)+'.png')) as a,Image.open(d/('side-uv-'+str(i*2+1)+'.png')) as b:
            assert a.size==b.size==(32,32) and a.convert('RGBA').tobytes()==b.convert('RGBA').tobytes()
    expected='WATER_SIDE_UV_UPLOAD_NATIVE_PASS samples=12 readbackBytes=8640 sideVertices=192 surfaceVertices=48 stillAndFlowing=true repeatedUpload=true staleRevision=true lightPreserved=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploads\n'
    assert (d/'water-side-uv-upload-pass.txt').read_text()==expected and c.count(expected.strip())==1
    data=(d/'water-side-uv-upload.bin').read_bytes();rows=json.loads((d/'water-side-uv-upload-details.json').read_text());assert len(data)==8640 and len(rows)==12
    for n,row in enumerate(rows):
        assert row['direction']==([16]*6+[0]*6)[n] and row['time']==[0,100,125,150,200,225][n%6] and row['stale']==(n%6==5)
        span=row['vSpan'];start=row['v0'];assert 0<span<1 and 0<=start<1 and start+span<=1.000001
        for v in range(20):
            at=(n*20+v)*36
            y=struct.unpack_from('<f',data,at+4)[0];uv=struct.unpack_from('<f',data,at+20)[0]
            delta,offset,light,sky=struct.unpack_from('<hhHH',data,at+24);delta/=16384
            is_side=v>=4;assert bool(light&256)==is_side and bool(light&512)==(is_side and row['direction']==0) and light&255==176 and sky==240
            if is_side:
                t=max(0,min(1,(row['time']-row['uploaded']-offset)/100))
                shown=y+delta*(1-t);actual=max(start,min(start+span,uv-delta*(1-t)*span));want=start+max(0,min(1,1-shown))*span
                assert abs(actual-want)<=1e-6
                if row['stale'] and y>0:assert abs(y-1)<1e-6


R=Path(__file__).resolve().parent;d=R/'vulkan-reuse-gl-side-material-normalized-final';r=json.loads((d/'result.json').read_text());seal=json.loads((d/'sealed-console.json').read_text());z=(d/'console-final.gz').read_bytes();b=gzip.decompress(z);c=b.decode()
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
actions=[a['action'] for a in json.loads((d/'action-history.json').read_text())];required=['backend-check=OpenGL','water-view=true,false,true','audit-lake-camera','water-side-material-check','water-material-check','water-direction-blend-check','water-direction-upload-check','water-flow-phase-check','water-side-uv-check','water-side-uv-upload-check','water-flow-texture-check','water-page-order-check','water-view=true,true,true','water-page-order-check=waves-enabled','water-view=true,false,false','water-page-order-check=lod-disabled','water-view=true,false,true','water-partial-fade-check','water-gpu-reuse-check','water-fade-continuity-check','reuse-gl-recovery','reload-check','water-flow-texture-check=after-reload'];assert actions in [required,required+['stop']]
assert len(list((d/'screenshots').glob('*.png')))==4
audit_order(d,c,'OpenGL')
audit_flow(d,c)
material=audit_material(d,c)
side_material=audit_side_material(d,c)
audit_blend(d,c)
audit_phase(d,c)
audit_side(d,c)
report={'case':d.name,'side_material':side_material,'material':material,'pass':True,'backend':'OpenGL software llvmpipe','scope':'New side/underside still/flow blend:336 routes,4000 actual-upload shader draws,3360 weighted RGBA checks,2009 actual GPU readbacks/1735776 bytes including16 simultaneous4-to16 height/material routes; original deadlines, atlas V guards and sparse history. Retained top still/flow blend:64 near/far routes,768 actual-upload shader renders,640 weighted RGBA samples,393 actual GPU readbacks/282960 bytes,queued/repeated/stale/deadline/sparse-still checks. Retained prior regressions: Flow-direction single-draw blend,336 shader renders and7 real uploads with bounded history, queued/stale/reupload checks. Six exact periodic top-phase pairs plus six diagnostic mip controls with one-channel-unit rounding allowance across negative/distant/section origins. Side UV morph pixels/actual upload packing, plus real capture and before/after-reload actual GPU flow UV fix, plus page order and actual frames in three wave/LOD profiles, production-pipeline pixel A/B; not full scene acceptance. Also mixed-page fade retention, six GPU readbacks of geometry/original opacity timing, and shared water buffer update API, actual near-capacity allocations, same/smaller/regrowth/bounded growth retry, 288-byte fenced GPU readback, prior fade checks, reload/capture/normal save. Not full OpenGL performance or visual acceptance.','started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'console_sha256':r['console_sha256'],'sealed_console_gzip_sha256':seal['gzip_sha256'],'new_crashes':0,'save_errors':0,'owner_gpu_fps_acceptance':False}
(R/'strict-opengl-side-material-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
