"""Verify sealed output and explicit acceptance records after launcher completion."""
from pathlib import Path
import json,hashlib,re,sys,gzip
R=Path(__file__).resolve().parent
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
    origins=[l for l in c.splitlines() if 'VALIDATION_CLASS_ORIGIN' in l];assert len(origins)==7,(name,origins)
    if 'packaged' in name:assert all('.jar' in l and '/build/classes/java/main/' not in l for l in origins),(name,origins)
    history=json.loads((d/'action-history.json').read_text())
    required=['backend-check=Vulkan','water-gpu-reuse-check','water-fade-continuity-check','lod-index-merge-check','lod-upload-retry-check','lod-disk-recovery-check','vulkan-frame-completion-check','vulkan-checks','reuse-recovery','reload-check']
    actual=[x['action'] for x in history]
    assert actual in [required,required+['stop']] and all(x['completed'] for x in history),name
    pngs=list((d/'screenshots').glob('*.png'));assert pngs,name
    rows.append({'case':name,'strict_pass':True,'started_utc':r['started_utc'],'finished_utc':r['finished_utc'],'run_directory':r['run_directory'],'class_origins':origins,'console_sha256':r['console_sha256'],'sealed_console_gzip_sha256':seal['gzip_sha256'],'live_console_sha256_at_audit':hashlib.sha256((d/'console.log').read_bytes()).hexdigest(),'eviction_recovery_cycles':4,'fixed_view_idle_calls':16,'freed_capacity_same_view_recovery':True,'actual_ballast_bytes':4193476,'target_page_bytes':1656,'admission_controls':4,'real_chunk_capture_and_store_provenance':True,'queued_edit_guard':True,'frame_completions':8,'open_pass_rejections':8,'unforced_vulkan':True,'marker_before_after_absent':True,'new_crashes':0,'save_errors':0,'vuid_errors':0,'validation_errors':0,'final_buffers':0,'final_images':0,'software_driver':'Mesa llvmpipe 25.2.8','owner_gpu_fps_acceptance':False,'screenshots':[{'path':str(f.relative_to(R)),'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in pngs]})
assert rows,'No cases supplied'
report={'all_focused_gates_pass':True,'engine_acceptance_complete':False,'scope':'Near-capacity same/smaller water updates reuse allocation; blocked oversized update retries after actual ballast release, and separate legal COPY_SRC mesh verifies 288 GPU bytes after partial update. Controlled pages, not full live-scene pixel proof. Also actual water GPU meshes with a controlled presentation clock preserve a dry fade through three empty recaptures, release at deadline and retarget rewet opacity continuously. This is GPU ownership/transition validation, not pixel-complete live-scene flicker acceptance. Also repeats four controlled completed-scan/native-tick cases enforce the 8192-key index bound while preserving queued order/edit guards and nearest remaining slots. This is injected scan completion, not full filesystem discovery or scene coverage. Also repeats real 552-byte partial GPU upload cancelled by an 8-to-4 MiB budget reduction beside 4194304 bytes of actual GPU ballast; bounded idle retry and same-view recovery after ballast closure. Five cancellation guards use an explicit occupancy ledger. Also repeats prior eviction/edit/capture, Vulkan GPU/frame/reload/save/shutdown controls. Isolated renderer; physical travel data is audited separately. Software driver, not owner GPU/FPS or full visual coverage acceptance.','sessions':rows}
(R/'strict-water-gpu-reuse-native-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

