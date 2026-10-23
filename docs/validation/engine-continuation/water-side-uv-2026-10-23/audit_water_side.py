"""Verify sealed output and explicit acceptance records after launcher completion."""
from pathlib import Path
import json,hashlib,re,sys,gzip,struct
R=Path(__file__).resolve().parent
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
            assert raw[i*36+15]==255
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


rows=[]
for name in sys.argv[1:]:
    d=R/name;r=json.loads((d/'result.json').read_text());seal=json.loads((d/'sealed-console.json').read_text())
    compressed=(d/'console-final.gz').read_bytes();raw=gzip.decompress(compressed);c=raw.decode()
    assert (d/'console.log').read_bytes()==raw, (name,'live console differs from producer seal')
    assert seal['stable_samples']>=4 and hashlib.sha256(compressed).hexdigest()==seal['gzip_sha256'],name
    assert hashlib.sha256(raw).hexdigest()==seal['raw_sha256']==r['console_sha256'] and len(raw)==seal['raw_bytes'],name
    assert r['exit_code']==0 and not r['timeout'] and r['build_successful'],name
    assert not json.loads((d/'crash-delta.json').read_text())['new_or_changed'],name
    assert not re.search(r'VUID-|VULKAN_VALIDATION ERROR|AssertionError|Failed to start FML|Preparing crash report|Exception in thread|Failed to save chunk|Exception stopping the server|Could not schedule ConsecutiveExecutor',c),name
    for marker in ['BACKEND_SELECTION_PASS backend=Vulkan','VULKAN_VALIDATION_ACTIVE true','VULKAN_RESOURCES_RELEASED buffers=0 images=0','validationErrors=0','RESOURCE_RELOAD_PASS','Saving chunks for level','All dimensions are saved','BUILD SUCCESSFUL','LOD_FIXED_VIEW_CAPACITY_PASS idleCalls=16 noDiskWorkUnderPressure=true freedCapacityRecovery=true actualGpuBallast=true','LOD_CAPACITY_RECOVERY_OBSERVED pageBytes=1656 ballastBytes=4193476 fixedView=true recovered=true','LOD_CAPTURE_PERSISTENCE_PASS realNativeChunk=true storedRevisionMatches=true','LOD_QUEUED_EDIT_GUARD_PASS dirtyDisplacement=true staleReadRejected=true metadataReleased=true']:
        assert marker in c,(name,marker)
    for control in ['dirty','newer-revision','not-persisted','persistence-disabled','obsolete-session']:
        assert c.count('LOD_UPLOAD_RETRY_GUARD_PASS control='+control)==1,(name,control)
    for control in ['dirty','newer-revision','not-persisted','persistence-disabled']:
        assert c.count('LOD_EVICTION_ADMISSION_PASS control='+control)==1,(name,control)
    for cycle in range(4):assert c.count('LOD_EVICTION_RECOVERY_OBSERVED cycle='+str(cycle)+' indexed=true resident=0')==1,(name,cycle)
    assert 'WATER_PARTIAL_CLEANUP_PASS steadyVertices=20 retainedCpuBytes=0 arraysReused=true originalRetainedBytes=1120' in c,name
    audit_order(d,c,'Vulkan')
    audit_flow(d,c)
    audit_side(d,c)
    partial=(d/'water-partial-fade.txt').read_text()
    assert partial=='WATER_PARTIAL_FADE_NATIVE_PASS samples=6 retainedVertices=40 readbackBytes=8640 originalDeadline=201 rewetDeadline=251 sameAllocation=true completedPartialReleased=true retainedArraysReused=true cpuBytes=1120 isolatedGpuReleased=true scope=controlledPageGpuGeometryOpacityNotLiveScenePixels\n' and c.count(partial.strip())==1,name
    assert 'WATER_PARTIAL_FADE_OBSERVED vertices=40 elapsed=25 originalVertices=40 originalDeadline=201' in c,name
    assert 'WATER_PARTIAL_FADE_LIFETIME_PASS retainedVertices=40 originalDeadline=201 rewetDeadline=251 samples=6 sameAllocation=true' in c,name
    assert c.count('WATER_PARTIAL_GPU_SAMPLE_PASS')==6,name
    reuse=(d/'water-gpu-reuse.txt').read_text()
    assert reuse=='WATER_GPU_REUSE_NATIVE_PASS targetBytes=46080 ballastBytes=33508224 updates=16 smallerAndRegrowth=true boundedGrowthRetry=true actualGpuPayloadBytes=288 isolatedGpuReleased=true scope=controlledGpuPagesNotLiveSceneFlickerAcceptance\n' and c.count(reuse.strip())==1,name
    assert 'WATER_GPU_REUSE_OBSERVED targetBytes=46080 ballastBytes=33508224 resident=33554304 accepted=true' in c,name
    assert 'WATER_GPU_REUSE_CAPACITY_PASS updates=16 smaller=true regrowth=true oversizedRetry=true closerCoveragePreserved=true' in c,name
    fade=(d/'water-fade-continuity.txt').read_text()
    assert re.fullmatch(r'WATER_FADE_CONTINUITY_NATIVE_PASS retainedBytes=[1-9][0-9]* repeatedEmptyUpdates=3 fadeMillis=100 noExtension=true rewetContinuous=true newerEditGuard=true isolatedGpuReleased=true scope=actualWaterGpuMeshesControlledClockNotCompleteSceneFlickerAcceptance\n',fade) and c.count(fade.strip())==1,name
    assert 'WATER_DRY_FADE_OBSERVED elapsed=25 remaining=76 retained=true' in c and 'WATER_DRY_FADE_LIFETIME_PASS repeatedEmptyUpdates=3 deadline=201 noExtension=true released=true' in c and 'WATER_REWET_CONTINUITY_PASS displayedAtRetarget=0.5 target=1 newerEditGuard=true' in c,name
    for filename,marker in [('lod-index-merge.txt','LOD_INDEX_MERGE_NATIVE_PASS cases=4 indexBound=8192 existingOrderAndEditGuards=true nearestRemainingSlots=true completedScanReleased=true scope=controlledScanFutureActualNativeTick'),('lod-upload-retry.txt','LOD_UPLOAD_RETRY_NATIVE_PASS partialBytes=552 budgetReducedMiB=8-to-4 actualBallastBytes=4194304 blockedCalls=16 restored=true guards=5 isolatedGpuReleased=true'),('lod-disk-recovery.txt','LOD_DISK_RECOVERY_NATIVE_PASS backend=Vulkan retainedAfterOutside=1 diskReads=6 uploadedChunks=6 restored=true evictionCycles=4 fixedViewIdleCalls=16 freedCapacityRecovery=true actualGpuBallast=true editPersistenceControls=4 nativeCapturePersisted=true queuedEditGuard=true isolatedGpuReleased=true scope=isolatedRendererRealStoreGpu'),('frame-completion.txt','VULKAN_FRAME_COMPLETION_NATIVE_PASS iterations=8 mappedBytes=128 openPassRejections=8 backend=Vulkan'),('saved-disconnect.txt','CLIENT_SAVED_DISCONNECT_PASS'),('shutdown.txt','VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true followingCompletion=true')]:
        assert (d/filename).read_text()==marker+'\n',(name,filename)
        assert c.count(marker)==1 if filename!='shutdown.txt' else c.count('VULKAN_SHUTDOWN_FAILURE_QUEUED')==1,(name,filename)
    assert c.rfind('All dimensions are saved')<c.index('CLIENT_SAVED_DISCONNECT_PASS'),name
    for marker in ['VULKAN_SHADER_CACHE_PASS','VULKAN_BUFFER_QUERY_PASS bytes=16','VULKAN_STORAGE_BYTES_PASS','VULKAN_DEPTH_READBACK_PASS','VULKAN_TEXTURE_COPY_PASS','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1','VULKAN_LIGHTMAP_RANGE']:
        assert marker in c,(name,marker)
    observations=(d/'startup-lifecycle.txt').read_text().splitlines()
    assert {re.search(r'label=([^ ]+)',x).group(1) for x in observations}=={'before-disconnect','after-disconnect'},name
    assert all(x in c and 'markerPresent=false' in x and 'ownerMarkerPresent=false' in x and 'complete=true' in x for x in observations),name
    before=json.loads((d/'startup-marker-before.json').read_text());after=json.loads((d/'startup-marker-after.json').read_text())
    assert not before['present'] and not before['explicit_retry'] and not after['present'],name
    assert '-PvulkanRetry' not in json.loads((d/'command.json').read_text()),name
    origins=[l for l in c.splitlines() if 'VALIDATION_CLASS_ORIGIN' in l];assert len(origins)==15,(name,origins)
    if 'packaged' in name:assert all('.jar' in l and '/build/classes/java/main/' not in l for l in origins),(name,origins)
    history=json.loads((d/'action-history.json').read_text())
    required=['backend-check=Vulkan','water-view=true,false,true','audit-lake-camera','water-side-uv-check','water-side-uv-upload-check','water-flow-texture-check','water-page-order-check','water-view=true,true,true','water-page-order-check=waves-enabled','water-view=true,false,false','water-page-order-check=lod-disabled','water-view=true,false,true','water-partial-fade-check','water-gpu-reuse-check','water-fade-continuity-check','lod-index-merge-check','lod-upload-retry-check','lod-disk-recovery-check','vulkan-frame-completion-check','vulkan-checks','order-recovery','reload-check','water-flow-texture-check=after-reload']
    actual=[x['action'] for x in history]
    assert actual in [required,required+['stop']] and all(x['completed'] for x in history),name
    pngs=list((d/'screenshots').glob('*.png'));assert pngs,name
    rows.append({'case':name,'strict_pass':True,'started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'run_directory':r['run_directory'],'class_origins':origins,'console_sha256':r['console_sha256'],'sealed_console_gzip_sha256':seal['gzip_sha256'],'live_console_sha256_at_audit':hashlib.sha256((d/'console.log').read_bytes()).hexdigest(),'eviction_recovery_cycles':4,'fixed_view_idle_calls':16,'freed_capacity_same_view_recovery':True,'actual_ballast_bytes':4193476,'target_page_bytes':1656,'admission_controls':4,'real_chunk_capture_and_store_provenance':True,'queued_edit_guard':True,'frame_completions':8,'open_pass_rejections':8,'unforced_vulkan':True,'marker_before_after_absent':True,'new_crashes':0,'save_errors':0,'vuid_errors':0,'validation_errors':0,'final_buffers':0,'final_images':0,'software_driver':'Mesa llvmpipe 25.2.8','owner_gpu_fps_acceptance':False,'screenshots':[{'path':str(f.relative_to(R)),'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in pngs]})
assert rows,'No cases supplied'
report={'all_focused_gates_pass':True,'engine_acceptance_complete':False,'scope':'Side UV morph fix: six exact production-pipeline image pairs at matched displayed heights, plus12 actual upload readbacks/8640bytes checking side/still/flow flags, light coordinates, repeated uploads and stale revisions. Also retains prior flow direction fix: real client capture and six production GPU UV samples per session, before/after resource reload, restore the source cell and preserve geometry/quantity. Selected actual GPU vertex bytes and expected atlas UVs retained. Existing production page sort survives three live map-order permutations in three wave/LOD profiles, with nine real frame captures per session and production-pipeline two-layer pixel A/B. Baseline changing a real source cell FLOW produced identical UVs; candidate carries flow and selects/rotates water_flow. This does not establish per-primitive order, matched physical state, full live-flow flicker or coverage acceptance. Also mixed wet/dry page preserves 40 actual GPU vertices through recapture and original fade/rewet deadlines; six GPU readbacks check 8640 bytes of geometry and shader-input opacity timing, not scene pixels. Retained CPU floats remain 1120 bytes with packed face/flow metadata, keep original capture revision, and are discarded on rewet. Also near-capacity same/smaller water updates reuse allocation; blocked oversized update retries after actual ballast release, and separate legal COPY_SRC mesh verifies 288 GPU bytes after partial update. Controlled pages, not full live-scene pixel proof. Also actual water GPU meshes with a controlled presentation clock preserve a dry fade through three empty recaptures, release at deadline and retarget rewet opacity continuously. This is GPU ownership/transition validation, not pixel-complete live-scene flicker acceptance. Also repeats four controlled completed-scan/native-tick cases enforce the 8192-key index bound while preserving queued order/edit guards and nearest remaining slots. This is injected scan completion, not full filesystem discovery or scene coverage. Also repeats real 552-byte partial GPU upload cancelled by an 8-to-4 MiB budget reduction beside 4194304 bytes of actual GPU ballast; bounded idle retry and same-view recovery after ballast closure. Five cancellation guards use an explicit occupancy ledger. Also repeats prior eviction/edit/capture, Vulkan GPU/frame/reload/save/shutdown controls. Isolated renderer; physical travel data is audited separately. Software driver, not owner GPU/FPS or full visual coverage acceptance.','sessions':rows}
(R/'strict-water-side-native-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

