from pathlib import Path
import json, hashlib, shutil, gzip, difflib, zipfile, datetime, re

R=Path(__file__).resolve().parent
P=R/'project'
prefix='docs/validation/engine-continuation/water-side-uv-2026-10-23'
E=P/prefix
E.mkdir(parents=True,exist_ok=True)
def sha(data): return hashlib.sha256(data).hexdigest()
def gitsha(data): return hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()
def save(name,obj): (E/name).write_text(json.dumps(obj,indent=2)+'\n')
def cp(src,dst=None):
    src=R/src;dest=E/(dst or src.name);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dest)
prod=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh']
validation=['src/visualValidation/java/com/shnewbs/deepprimal/validation/'+n+'.java' for n in ['ClientCapture','WaterSideUvChecks','WaterSideUvUploadChecks']]
inputs=json.loads((R/'recovered-inputs.json').read_text())
changes=[];manifest=[]
for row in inputs:
    original=row['content'].encode();assert gitsha(original)==row['sha'],row['path']
    current=(P/row['path']).read_bytes();changed=current!=original
    if changed:changes.append(row['path'])
    manifest.append({'path':row['path'],'parent_git_sha':row['sha'],'current_git_sha':gitsha(current),'unchanged':not changed})
assert sorted(changes)==sorted(prod+[validation[0]]),changes
wrapper=P/'gradle/wrapper/gradle-wrapper.jar'
tree=json.loads((R/'parent-tree.json').read_text()); entries=tree['tree'] if isinstance(tree,dict) else tree
wrapperrow=next(e for e in entries if e['path']=='gradle/wrapper/gradle-wrapper.jar')
assert gitsha(wrapper.read_bytes())==wrapperrow['sha']
manifest.append({'path':wrapperrow['path'],'parent_git_sha':wrapperrow['sha'],'current_git_sha':wrapperrow['sha'],'unchanged':True})
save('restored-input-manifest.json',manifest)
save('source-scope-audit.json',{'parent_commit':'d2f96e2dfadc06beef0c661bbda74324a5c43423','lease_parent':'979a5e796d2f9fa861cd9da5d045bf8fdbb6079f','restored_parent_inputs':len(manifest),'unchanged_parent_inputs':sum(e['unchanged'] for e in manifest),'changed_existing_inputs':changes,'new_validation_inputs':validation[1:],'production_changed':prod,'unaccepted_solver_unchanged':True,'geography_unchanged':True,'no_installer_release':True})
patch=''
for file,before in zip(prod,['before-AnimatedWaterRenderer.java','before-animated_water.vsh']):
    cp(before,'before/'+Path(file).name)
    patch+=''.join(difflib.unified_diff((R/before).read_text().splitlines(True),(P/file).read_text().splitlines(True),fromfile='a/'+file,tofile='b/'+file))
(E/'production.patch').write_text(patch)
for file in ['candidate-build.log','candidate-build-result.json','candidate-build-final.gz','vulkan-build-audit.json','strict-water-side-native-audit.json','strict-opengl-side-audit.json','side-uv-math-checks.json','baseline-source-verification.json','baseline-WaterSideUvChecks.java','check_side_uv_math.py','audit_water_side.py','audit_opengl_side.py','audit_vulkan_package.py','make_frame_video.py','build_candidate.py','run_vulkan.py','restore_graphics.py','graphics-pins-source.json','graphics-restoration.json','restore_validation_toolchain.py']:
    cp(file)
cp('toolchain/restoration.json','toolchain-restoration.json')
cp('toolchain/checked-agent/neoform-process-fallback.checks.json','neoform-process-fallback.checks.json')
build=json.loads((R/'candidate-build-result.json').read_text());assert build['pass'] and all(build['required_records'].values())
raw=(R/'candidate-build.log').read_bytes();assert raw==gzip.decompress((R/'candidate-build-final.gz').read_bytes())==Path('/tmp/deepprimal-side-build-final-0433/candidate-build.log').read_bytes()
assert sha(raw)==build['raw_sha256']
assert json.loads((R/'strict-water-side-native-audit.json').read_text())['all_focused_gates_pass']
assert json.loads((R/'strict-opengl-side-audit.json').read_text())['pass']
outer,=list((P/'build/libs').glob('*-vulkan-linux.jar'));modules=[]
with zipfile.ZipFile(outer) as z:
    for nested in [n for n in z.namelist() if n.startswith('META-INF/jarjar/') and n.endswith('.jar')]:
        data=z.read(nested);matching=[f for f in P.glob('*/build/libs/*.jar') if f.read_bytes()==data];assert len(matching)==1,nested
        modules.append({'nested_path':nested,'standalone':str(matching[0].relative_to(P)),'sha256':sha(data),'bytes':len(data),'identical':True})
assert len(modules)==5
save('module-identities.json',modules)
for file in ['baseline-incomplete-candidate-build-final.gz','baseline-incomplete-candidate-build-result.json','baseline-incomplete-candidate-build.log']:
    cp(file,'rejected-capture/'+file)
daemon=(R/'baseline-daemon-complete.log').read_bytes()
selected=[s for s in daemon.decode().splitlines() if re.search(r'(_PASS|BUILD SUCCESSFUL|25 actionable tasks)',s)]
(E/'rejected-capture/daemon-selected-build-markers.txt').write_text('\n'.join(selected)+'\n')
save('rejected-capture-summary.json',{'classification':'Incomplete client console; Gradle exit zero alone is rejected','full_daemon_sha256':sha(daemon),'selected_daemon_markers':len(selected),'client_bytes':1284,'explicit_client_required_records':False,'native_acceptance':False,'root_cause':'unresolved','final_uncached_candidate_build':'explicit complete sealed records pass'})
cases=['vulkan-side-baseline','vulkan-order-side-final','vulkan-order-side-final-packaged','vulkan-reuse-gl-side-final']
for case in cases:
    d=R/case
    for src in d.rglob('*'):
        if not src.is_file():continue
        rel=src.relative_to(d)
        if 'screenshots' in rel.parts:
            # Preserve actual three-frame pixels losslessly in verified RGB VP9 videos.
            if src.name.startswith('order-live-frame-'):continue
        elif src.suffix not in ['.json','.txt','.gz','.bin','.png','.webm']:continue
        if any(s in rel.parts for s in ['isolated-disk-cache','upload-retry-store']):continue
        cp(str(src.relative_to(R)),case+'/'+str(rel))
    seal=json.loads((d/'sealed-console.json').read_text());console=(d/'console.log').read_bytes()
    assert sha(console)==seal['raw_sha256'] and console==gzip.decompress((d/'console-final.gz').read_bytes())
baseline=json.loads((R/cases[0]/'water-side-uv-pixel-details.json').read_text())
assert sum(x['differentPixels'] for x in baseline)==640
assert 'pass=false' in (R/cases[0]/'water-side-uv-pixels.txt').read_text()
final=[json.loads((R/c/'water-side-uv-pixel-details.json').read_text()) for c in cases[1:]]
assert all(len(rows)==6 and sum(x['differentPixels'] for x in rows)==0 for rows in final)
for file in ['side-uv-before-after.png']+[c+'-contact.png' for c in cases[1:]]:cp(file)
save('screenshot-inspection.json',{'inspected_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'contact_sheets':['side-uv-before-after.png']+[c+'-contact.png' for c in cases[1:]],'controlled_before_after':'Baseline first two matched-height still-side pairs visibly shift UV despite identical silhouette. Candidate pairs have the same gradient; settled pair already matched on baseline. Flow-side numeric differences also preserved in all twelve raw images.','live_27_frames':'Lake, water grid, native shore and distant terrain remain visible across all three profiles and three sessions. No blank capture observed. Camera is fixed; a short lake sequence cannot establish smooth moving streams or diagonal/primitive transparency.','wave_geometry':'waveBytes=0 despite enabled option; actual ocean waves untested','owner_gpu_acceptance':False,'full_flow_flicker_acceptance':False})
save('code-self-audit.json',{'production_files':2,'cpu_float_stride':14,'gpu_vertex_bytes':36,'uniform_allocation':'Existing Matrix4f reused; m10/m11 span and m20/m21 origin; m00 remains elapsed clock','admitted_height':'CPU side UV uses shared target after stale revision guard, not stale mesh target','shader':'Side V follows exact geometry displacement, clamps to corresponding still/full or flowing/half sprite range','lighting':'Native block light low byte preserved; reserved UV2.x bits8/9 masked before light lookup; skylight unchanged','clock_and_opacity':'Original 100ms height and opacity clocks/packed offsets unchanged','scope_limits':['No direction/material transition smoothing','No speed-dependent texture advection','No whole-stream, ocean-wave, diagonal seam, per-primitive transparency or owner GPU/FPS acceptance','Double precision randomized algebra is not GPU float/performance evidence','No volume, simulation frequency, solver, LOD or terrain changes'],'new_test_files':validation[1:],'fixture_isolation':'Controlled buffers/atlas/targets released; isolated renderer worker shut down; no authoritative world edits from new side fixtures'})
summary={'fresh_production_files_changed':2,'validation_files_changed_or_added':3,'parent_inputs_checked':560,'parent_inputs_unchanged':557,'baseline_differing_pixels':640,'baseline_geometry_matched':True,'candidate_exact_pixel_pairs':18,'candidate_controlled_renders':36,'candidate_differing_pixels':0,'actual_upload_readbacks':36,'actual_upload_readback_bytes':25920,'side_vertices_checked':576,'top_vertices_checked':144,'repeated_upload_stale_revision_light_checks':True,'retained_flow_gpu_uv_samples':18,'cpu_vertex_float_stride':14,'gpu_vertex_bytes':36,'actual_live_frames':27,'view_profiles':9,'actual_ocean_wave_geometry_exercised':False,'full_live_flow_pixel_acceptance':False,'sealed_uncached_linux_vulkan_build':'pass;25 executed tasks','archive_crc_count':6,'compiled_class_matches':408,'module_identities':5,'strict_final_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'new_final_crashes_and_save_errors':0,'owner_gpu_fps_acceptance':False,'headwater_and_chemistry_implementation':'requirements recorded; implementation open','installer_released':False}
save('checkpoint-summary.json',summary)
readme='''# Water side UV continuity — 2026-10-23

Parent d2f96e2dfadc06beef0c661bbda74324a5c43423; guarded lease parent 979a5e796d2f9fa861cd9da5d045bf8fdbb6079f. Unreleased continuation on work/engine-after-alpha24. Main and supplied installers remain intact. Engine acceptance is incomplete.

## Reproduction and focused change

The existing height morph moves geometry over 100 ms but immediately switches side texture V to the new target. The production shader, unchanged from the parent, produces 640 differing pixels across six controlled matched-height pairs with identical geometry. The settled pairs match; this is a real texture-displacement defect, not a silhouette difference or screenshot timing claim. Baseline raw images, complete sealed console, explicit failing result, unchanged-production source proof and exact pixel fixture are preserved. The expected baseline failure is not a client crash or acceptance pass.

Two production files change: renderer upload derives side V from the admitted shared height (including rejected stale captures), encodes side/flow mode in the unused high bits of native block-light coordinates, and carries sprite bounds in existing matrix storage. The shader masks those bits before light sampling and moves/clamps side V with the same displacement used by geometry. CPU stride remains 14 floats; GPU stride remains 36 bytes; uniform allocation remains the existing matrix. Top UV selection, light values, height/opacity timing, quantity, solver, worker bounds, LOD and frozen geography are preserved. No installer is released.

## Fresh validation

Full uncached Linux Vulkan build: 25 executed tasks, explicit build/core/terrain/hydrology/water records, sealed raw/gzip identity. All six archive CRCs, 408 compiled classes and five nested/standalone module identities match; validation and private compatibility-agent payloads are absent. Pinned toolchain and Mesa/Xvfb were restored and hash checked after workspace loss; old pre-reset measurements are historical.

Two strict software Vulkan sessions (including packaged class origins) and one scoped OpenGL llvmpipe fallback session pass explicit records, reload, normal save/shutdown, sealed log/crash checks and prior water/LOD/buffer lifetime regressions. Each passes six exact matched-height pixel pairs for still/flowing sides at 0/50/100 ms and twelve actual production uploads, including repeated retargets, stale revisions and native light coordinates. Totals: 18 exact pairs/36 controlled renders, zero differing pixels; 36 fenced upload readbacks/25,920 bytes. Prior real-client FLOW capture and eighteen GPU direction/material UV samples before/after reload also pass. Controlled pixel tests use the real production pipeline and gradient atlas; upload tests independently decode actual production buffers and real nonzero atlas bounds. Neither alone proves full live-stream acceptance.

Twenty-seven actual lake frames in nine wave/LOD profiles were inspected; lossless RGB VP9 videos decode byte-for-byte to their source frame pixels. Playback is illustrative, not measured FPS. Water and terrain remain present. Enabled-wave lake runs have waveBytes=0; actual ocean waves remain untested. Page order and two-layer pixel controls retain their explicit passes, without establishing per-primitive transparency.

The first restored build exited zero but its client console lacked all required completion records. It is explicitly rejected under rejected-capture/, with selected daemon records and full daemon hash. A later complete build passes; that does not explain the missing client tail. No new final crashes, save errors, Vulkan validation errors or tracked buffer/image leaks were observed. The 200,000-case CPU algebra check is double precision, not native shader or performance evidence.

## Remaining work and continuation

Next: smooth direction/material retargeting, then diagonal seam and longer moving-stream coverage, speed-dependent advection, shared/native/LOD transitions and actual ocean-wave coverage. Preserve bounded storage and quantity; do not hide abrupt directional UV changes behind the height fix. Source/headwater reservoirs, compatible-liquid chemistry/mixing and conserved constituent budgets are requirements only. Prior staged retained-drain pressure experiments remain separate and unaccepted: fanout latency, 1024 waiters, larger-region/residency limits and broad output/memory acceptance remain open. Spatial/coarser LOD discovery beyond the 8192-key cap, physical input/server unload/reopen, unexplained reload/atlas/marker/save failures, coherent geology and owner-GPU/frame-time/market comparisons remain unresolved. Catalog/species work stays deferred. Solver substeps are not independent 60 Hz simulation.

The source, reproducible fixtures, strict audit scripts, compact logs, raw controlled pixels/readbacks and exact live-frame videos are preserved here for review. A successful checkpoint is a continuation point, not engine completion.
'''
(E/'README.md').write_text(readme)
state=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text())
now=datetime.datetime.now(datetime.timezone.utc).isoformat()
state.update(status='continuation_permitted',updated_at=now,active_until=None,worker=None,acceptance_complete=False,current_gate='Fixed reproduced side UV snap during height morph and rejected stale mesh admission; six exact pixel pairs and twelve real production upload readbacks per session pass on two strict software Vulkan sessions plus OpenGL fallback. Full build/package/core pass; direction retargets, long streams/ocean and owner GPU acceptance remain open. No installer.',latest_continuation_evidence=prefix+'/README.md',latest_test_summary=summary,local_validation_environment=str(R))
state['next_items'][0]='Owner priority seamless flowing water: smooth direction/material retargeting and diagonal tile seams; speed-dependent advection remains open. Side UV height-morph and stale-admission mismatch now fixed without vertex/uniform allocation growth; captured FLOW/material selection previously fixed. Validate longer actual moving streams and real ocean-wave geometry (lake waveBytes0) across shared horizontal/vertical/native/LOD/reload/travel/memory pressure, retaining bounds and edits.'
state['latest_graphics_evidence']=[prefix+'/'+f for f in ['strict-water-side-native-audit.json','strict-opengl-side-audit.json','vulkan-build-audit.json','module-identities.json','source-scope-audit.json','code-self-audit.json','candidate-build-result.json','screenshot-inspection.json','checkpoint-summary.json']]
(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(state,indent=2)+'\n')
for file in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
    p=P/file
    p.write_text(p.read_text()+'\n\n## 2026-10-23 — side UV height continuity checkpoint\n\nReproduced 640 differing production-shader pixels at matched displayed heights; fixed side UV motion and stale-admitted-height coherence in two production files, preserving 14-float CPU/36-byte GPU payloads, existing matrix allocation, light, clocks, quantity and frozen geography. Full uncached 25-task build, six CRCs/408 class identities/five module identities, two strict software Vulkan sessions and one scoped OpenGL fallback pass. Eighteen exact pixel pairs now have zero differences; 36 real upload readbacks/25,920 bytes cover repeated retargets, stale revisions and light. Twenty-seven short lake frames inspected; actual ocean geometry remains untested. First incomplete build console is explicitly rejected despite exit zero. No installer; no owner GPU/FPS or whole-stream acceptance.\n\nEvidence: ['+prefix+'/README.md]('+prefix.removeprefix('docs/')+'/README.md). Next implement smooth direction/material retargeting and validate real streams/diagonal seams; retain all existing solver/budget/LOD/geology and marker/reload blockers. Lease released, acceptance incomplete.\n')
save('evidence-manifest.json',[{'path':str(p.relative_to(E)),'bytes':p.stat().st_size,'sha256':sha(p.read_bytes())} for p in sorted(E.rglob('*')) if p.is_file() and p.name!='evidence-manifest.json'])
texts=[];binaries=[]
paths=[P/f for f in prod+validation+['docs/ENGINE-WORK-STATE.json','docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']]+[p for p in sorted(E.rglob('*')) if p.is_file()]
for p in paths:
    data=p.read_bytes();rel=str(p.relative_to(P));row={'path':rel,'mode':'100644','type':'blob'}
    if p.suffix in ['.png','.bin','.gz','.webm']:
        binaries.append({'path':rel,'local_path':str(p),'bytes':len(data),'sha256':sha(data),'git_sha':gitsha(data)})
    else:row['content']=data.decode();texts.append(row)
(R/'checkpoint-text-elements.json').write_text(json.dumps(texts))
(R/'checkpoint-binary-manifest.json').write_text(json.dumps(binaries,indent=2)+'\n')
print(json.dumps({'text_files':len(texts),'binary_files':len(binaries),'binary_bytes':sum(x['bytes'] for x in binaries),'production_changes':len(prod),'validation_changes':len(validation),'summary':summary}))
