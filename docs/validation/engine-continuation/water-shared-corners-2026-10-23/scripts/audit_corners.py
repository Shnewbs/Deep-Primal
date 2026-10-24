"""Independent raw vertex/pixel and sealed native audit. Never infer pass from exit zero."""
from pathlib import Path
import gzip,hashlib,json,math,struct,sys,statistics
def raw(p):return p.read_bytes() if p.exists() else gzip.decompress(p.with_suffix(p.suffix+'.gz').read_bytes())
def read(p):return json.loads(raw(p))
def f32(x):return struct.unpack('<f',struct.pack('<f',x))[0]
def mean(a,b):
    vx=sum(f32(math.cos(d*math.pi/8)) for d in (a,b) if d<16);vz=sum(f32(math.sin(d*math.pi/8)) for d in (a,b) if d<16)
    return 16 if vx*vx+vz*vz<1e-12 else math.floor(math.atan2(vz,vx)*(8/math.pi)+.5)&15
def audit(case,baseline=False):
    result=read(case/'water-corner-result.json');rows=read(case/'water-corner-details.json');pixels=raw(case/'water-corner-pixels.bin');uploads=raw(case/'water-corner-upload.bin');meta=read(case/'water-corner-upload-details.json')
    assert len(meta)==4664 and len(uploads)==5037120
    assert len(rows)==(2468 if baseline else 4936) and len(pixels)==(5890048 if baseline else 7153664)
    kinds={};errors=coverage=0;maximum=0;kind_max={};kind_failed={};offset=0;seen=set()
    for r in rows:
        size=r['width']*r['height']*4;left=r['leftOffset'];right=r['rightOffset'];assert left==offset and right==left+size
        offset=right+size;a=pixels[left:right];b=pixels[right:right+size];assert len(a)==len(b)==size
        es=[abs(x-y) for x,y in zip(a,b)];bad=sum(e>1 for e in es);mx=max(es)
        assert bad==r['channelsBeyondTolerance'] and mx==r['maxChannelError'];errors+=bad;maximum=max(maximum,mx);coverage+=sum(x!=255 for x in a[3::4])+sum(x!=255 for x in b[3::4])
        kind=r['kind'];kinds[kind]=kinds.get(kind,0)+1;kind_max[kind]=max(kind_max.get(kind,0),mx);kind_failed[kind]=kind_failed.get(kind,0)+(bad>0)
        assert (kind,r['route']) not in seen;seen.add((kind,r['route']))
        if not baseline:
            assert bad==0,(r['route'],kind,bad)
            if kind in (2,3):
                for image in (a,b):
                    for at in range(0,size,4):assert all(abs(image[at+c]-[0,255,0,255][c])<=1 for c in range(4)),('wrong diagnostic mip',r['route'],kind,at)
    assert offset==len(pixels) and kinds==({0:2332,1:136} if baseline else {0:2332,2:2332,1:136,3:136})
    assert coverage==0 and result['channels_beyond_tolerance']==errors and result['max_channel_error']==maximum and result['coverage_bad_pixels']==coverage
    assert result['raw_upload_bytes']==len(uploads) and result['pixel_readback_bytes']==len(pixels)
    assert result['draws']==(4936 if baseline else 9872) and result['routes']==2332 and result['uniform_routes']==136
    if baseline:assert result['pass'] is False and result['packed_actual_tops']==0 and kind_failed=={0:2204,1:0} and kind_max[1]==0
    else:assert result['pass'] is True and result['packed_actual_tops']==4664 and result['uniform_mip_routes']==136 and result['mip_edge_routes']==2332 and result['comparisons']==4936
    vertices=0;cursor=0;timing={20:[],40:[]}
    for r in meta:
        assert r['offset']==cursor;cursor+=r['count']*36
        assert r['count'] in (20,40);at=r['offset']+r['topVertex']*36
        if 100<=r['route']<2312:timing[r['count']].append(r['uploadNanos']/1000)
        if baseline:continue
        for v in range(4):
            p=at+v*36;x,y,z=struct.unpack_from('<3f',uploads,p);color=uploads[p+12:p+16];a,b=struct.unpack_from('<2f',uploads,p+16);height,clock,l,sky=struct.unpack_from('<4H',uploads,p+24)
            assert a<-1 and y==1 and color[:3]==b'\xff\xff\xff' and color[3]==r['cellX']|(r['cellZ']<<4)
            assert l&255==176 and sky&255==240 and height==0 and ((clock&255)==0 or (clock&255)>=156)
            to=round(-a)-2;source=round(b);ages=[(clock>>8)&255,(l>>8)&255,(sky>>8)&255,((to>>20)&15)|(((source>>20)&7)<<4)]
            for n in range(4):
                shared=((n&1)==(1-r['side'])) if r['axis']==0 else ((n>>1)==(1-r['side']))
                target=mean(r['a'],r['b']) if shared else (r['a'] if r['side']==0 else r['b'])
                if r['route']<2312:old=target;age=100 if target==16 else 0
                else:
                    old=mean(0,4) if shared else (0 if r['side']==0 else 4)
                    right_boundary=r['side']==1 and (r['cellX']==0 if r['axis']==0 else r['cellZ']==0)
                    started=r['uploaded'] if right_boundary and not shared else 100
                    age=max(0,min(100,r['uploaded']-started))
                expected_source=target if age>=100 else old
                assert (to>>(n*5))&31==target,(r,n,'target',target,(to>>(n*5))&31)
                assert (source>>(n*5))&31==expected_source,(r,n,'source',expected_source,(source>>(n*5))&31)
                assert ages[n]==age,(r,n,'age',age,ages[n])
            assert uploads[p+32:p+34]==bytes([127,127]) and (uploads[p+34]==0 or uploads[p+34]>=156);vertices+=1
    assert cursor==len(uploads)
    before=read(case/'source-identity-before.json');after=read(case/'source-identity-after.json');provenance=read(case/'water-corner-runtime-provenance.json')
    for name,digest in after.items():assert before[name]==digest,('source changed',name)
    assert provenance['renderer_class_sha256']==before['expected_renderer_class_sha256']
    assert provenance['corner_history_field_present']==before['expected_corner_history_field']==(not baseline)
    for shader in ['animated_water.vsh','animated_water.fsh']:assert provenance[shader+'_sha256']==before['graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/'+shader]
    sealed=(case/'console-final.gz').read_bytes();log=gzip.decompress(sealed);proof=read(case/'sealed-console.json');text=log.decode()
    assert hashlib.sha256(log).hexdigest()==proof['raw_sha256'] and hashlib.sha256(sealed).hexdigest()==proof['gzip_sha256'] and proof['stable_samples']>=4 and proof['launcher_exit']==0
    gl='reuse-gl' in case.name
    required=['WATER_CORNER_FIELD_RESULT pass='+str(not baseline).lower(),'Saving players','Saving worlds','All dimensions are saved','BUILD SUCCESSFUL','BACKEND_SELECTION_PASS backend='+('OpenGL' if gl else 'Vulkan')]
    if not baseline:required+=['WATER_CORNER_FIELD_NATIVE_PASS','WATER_CORNER_HISTORY_CAP_PASS inserted=33000 cap=32768','WATER_MATERIAL_RESULT pass=true','WATER_SIDE_MATERIAL_RESULT pass=true','WATER_DIRECTION_UPLOAD_NATIVE_PASS','WATER_SIDE_UV_UPLOAD_NATIVE_PASS','WATER_PARTIAL_FADE_NATIVE_PASS','WATER_GPU_REUSE_NATIVE_PASS','WATER_FLOW_TEXTURE_NATIVE_PASS']
    if gl:required+=['RESOURCE_RELOAD_PASS','WATER_DIRECTION_BLEND_NATIVE_PASS','WATER_FLOW_PHASE_PIXEL_NATIVE_PASS']
    else:required+=['VULKAN_FRAME_COMPLETION_NATIVE_PASS','VULKAN_STENCIL_PASS','validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0'];assert 'VULKAN_SHUTDOWN_CONTINUATION_PASS' in raw(case/'shutdown.txt').decode()
    for marker in required:assert marker in text,marker
    for bad in ['AssertionError','Exception in thread','Failed to save','VUID-']:assert bad not in text,bad
    assert not read(case/'crash-delta.json')['new_or_changed']
    history=read(case/'action-history.json');assert all(x['completed'] for x in history) and len(history)>=5
    if 'packaged' in case.name:
        origins=[s for s in text.splitlines() if 'VALIDATION_CLASS_ORIGIN' in s];assert origins and all('location=file:' in x and '/build/libs/' in x and '.jar' in x and '/build/classes/' not in x for x in origins)
        assert read(case/'module-prelaunch-audit.json')['all_current_compiled_vulkan_classes_match']
    observed={}
    for count,xs in timing.items():
        xs.sort();observed[str(count)]={'samples':len(xs),'median_microseconds':statistics.median(xs),'p95_microseconds':xs[int(len(xs)*.95)]}
    return {'case':case.name,'baseline':baseline,'pass':True,'kind_counts':kinds,'kind_max_channel_error':kind_max,'kind_failed_routes':kind_failed,'channels_beyond_tolerance':errors,'max_channel_error':maximum,'coverage_bad_pixels':coverage,'raw_upload_bytes':len(uploads),'pixel_readback_bytes':len(pixels),'decoded_top_vertices':vertices,'upload_sha256':hashlib.sha256(uploads).hexdigest(),'pixel_sha256':hashlib.sha256(pixels).hexdigest(),'sealed_console_sha256':proof['gzip_sha256'],'loaded_class_and_shader_identities_verified':True,'source_stable':True,'fresh_crashes':0,'normal_save_shutdown':True,'opengl_rows_normalized':gl,'upload_cpu_observations':observed,'performance_acceptance':False,'full_stream_ocean_acceptance':False,'partial_neighbor_request_atomicity':False,'owner_gpu_fps_acceptance':False}
if __name__=='__main__':
    baseline=audit(Path(sys.argv[1]),True);sessions=[audit(Path(x)) for x in sys.argv[2:]];print(json.dumps({'pass':True,'baseline':baseline,'sessions':sessions},indent=2))
