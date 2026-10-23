"""Independently recompute every retained diagnostic pixel comparison, fail closed."""
from pathlib import Path
import gzip,hashlib,json,sys
def content(p):
    return p.read_bytes() if p.exists() else gzip.decompress(p.with_suffix(p.suffix+'.gz').read_bytes())
def audit(case):
    rows=json.loads(content(case/'water-spatial-details.json'));raw=content(case/'water-spatial-pixels.bin')
    result=json.loads(content(case/'water-spatial-result.json'))
    assert len(rows)==6208 and len(raw)==12416*1024,(len(rows),len(raw))
    counts={0:0,1:0,2:0,3:0};same_failed=seams=candidate_failed=fidelity_failed=blend_failed=blend_uniform_failed=0
    strict_control_max=blend_control_max=0;baseline={};max_error=0;coverage=0
    for i,row in enumerate(rows):
        assert row['group']==i and row['left_offset']==i*2048 and row['right_offset']==i*2048+1024
        a=raw[i*2048:i*2048+1024];b=raw[i*2048+1024:(i+1)*2048];errors=[abs(x-y) for x,y in zip(a,b)];bad=sum(e>8 for e in errors);mx=max(errors)
        assert row['maxChannelError']==mx and row['channelsBeyondOneTexel']==bad and row['differentChannels']==sum(e>0 for e in errors)
        coverage+=sum(x!=255 for x in a[3::4])+sum(x!=255 for x in b[3::4]);max_error=max(max_error,mx)
        mode=row['mode'];counts[mode]+=1;key=(row['origin'],row['axis'],row['a'])
        if mode==0:
            if row['a']==row['b']:
                same_failed+=bad>0;strict_control_max=max(strict_control_max,mx);baseline[key]=(a,b)
            else:seams+=bad>0
        elif mode==1:candidate_failed+=bad>0
        elif mode==2:fidelity_failed+=bad>0
        else:
            blend_failed+=bad>0
            if row['a']==row['b']:
                control=baseline[key]
                for image,reference in zip((a,b),control):
                    e=max(abs(x-y) for x,y in zip(image,reference));blend_control_max=max(blend_control_max,e);blend_uniform_failed+=e>8
    assert counts=={0:2048,1:2048,2:64,3:2048},counts
    assert same_failed==0 and seams==1920 and candidate_failed==0 and fidelity_failed>0 and blend_failed==0 and blend_uniform_failed==0 and coverage==0
    # Stronger independent acceptance than the native one-texel diagnostic gate:
    # all controls and staged edge blends must actually be byte-exact.
    assert strict_control_max==0 and blend_control_max==0
    assert all(r['maxChannelError']==0 for r in rows if r['mode'] in (1,3))
    expected={'audit_pass':True,'production_spatial_continuity_pass':False,'candidate_promotable':False,'same_direction_failed_routes':same_failed,'different_direction_seam_routes':seams,'naive_shared_uv_failed_edge_routes':candidate_failed,'naive_shared_uv_failed_uniform_fidelity_routes':fidelity_failed,'shared_sample_blend_failed_edges':blend_failed,'shared_sample_blend_uniform_fidelity_failures':blend_uniform_failed,'coverage_bad_pixels':coverage,'draws':12416,'comparisons':6208,'max_channel_error':max_error,'same_direction_tolerance':8}
    for k,v in expected.items():assert result[k]==v,(k,result[k],v)
    sealed=case/'console-final.gz';log=gzip.decompress(sealed.read_bytes());proof=json.loads((case/'sealed-console.json').read_text())
    assert hashlib.sha256(log).hexdigest()==proof['raw_sha256'] and hashlib.sha256(sealed.read_bytes()).hexdigest()==proof['gzip_sha256'] and proof['stable_samples']>=4 and proof['launcher_exit']==0
    text=log.decode();required=['WATER_SPATIAL_AUDIT_PASS defectReproduced=true naiveUvCandidateRejected=true productionFixPromoted=false','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS','WATER_DIRECTION_UPLOAD_NATIVE_PASS','VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS','Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0']
    for marker in required:assert marker in text,marker
    for bad in ['Exception in thread','AssertionError','WATER_SPATIAL_RESULT auditPass=false','Failed to save','VUID-']:assert bad not in text,bad
    assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed']
    assert 'VULKAN_SHUTDOWN_CONTINUATION_PASS' in (case/'shutdown.txt').read_text()
    history=json.loads((case/'action-history.json').read_text());assert len(history)==7 and all(x['completed'] for x in history),history
    if 'packaged' in case.name:
        origins=[line for line in text.splitlines() if 'VALIDATION_CLASS_ORIGIN' in line]
        assert len(origins)==4 and all('location=file:' in x and '/build/libs/' in x and '.jar' in x and 'build/classes/' not in x for x in origins),origins
        assert all('-vulkan-linux.jar' in x for x in origins if 'class=com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer ' in x)
        assert any('class=net.vulkanmod.render.engine.VkCommandEncoder ' in x and '/renderer-vulkan/build/libs/DeepPrimal-Vulkan-' in x for x in origins)
        assert json.loads((case/'module-prelaunch-audit.json').read_text())['all_current_compiled_vulkan_classes_match']
    return dict(case=case.name,pass_=True,software_vulkan_only=True,production_fix_promoted=False,counts=expected,strict_same_direction_max_channel_error=strict_control_max,shared_sample_blend_uniform_max_channel_error=blend_control_max,raw_pixels_sha256=hashlib.sha256(raw).hexdigest(),details_sha256=hashlib.sha256(content(case/'water-spatial-details.json')).hexdigest(),sealed_console_sha256=proof['gzip_sha256'],fresh_crashes=0,normal_save_shutdown=True,explicit_records=required,owner_gpu_fps_acceptance=False,actual_stream_or_upload_acceptance=False)
if __name__=='__main__':
    results=[audit(Path(x)) for x in sys.argv[1:]];print(json.dumps({'pass':True,'sessions':results},indent=2))
