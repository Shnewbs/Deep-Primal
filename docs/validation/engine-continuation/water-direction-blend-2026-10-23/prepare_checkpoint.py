from pathlib import Path
import json,hashlib,gzip,shutil,difflib,zipfile,datetime,io
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-direction-blend-0645')
PREFIX='docs/validation/engine-continuation/water-direction-blend-2026-10-23';E=P/PREFIX;E.mkdir(parents=True,exist_ok=True)
PARENT='8baffc9e89697d0abeec11345091b3f9ef95f78e'
LEASE=json.loads((R/'final-parent.json').read_text())['sha']
def sha(b):return hashlib.sha256(b).hexdigest()
def gitsha(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def save(n,x):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def cp(src,dst):
 src=Path(src);dst=E/dst;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
PROD=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh','engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java']
TESTS=['src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowBlendChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionBlendChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionUploadChecks.java']
parent=json.loads((R/'parent-blob-manifest.json').read_text());inputs=[];changed=[]
for row in parent:
 p=P/row['path']
 if row['path'].startswith('docs/') or not p.is_file():continue
 current=gitsha(p.read_bytes());same=current==row['sha'];inputs.append({'path':row['path'],'parent_git_sha':row['sha'],'current_git_sha':current,'unchanged':same})
 if not same:changed.append(row['path'])
assert len(inputs)==563 and sorted(changed)==sorted(PROD[:4]+TESTS[:2]),changed
save('source-scope-audit.json',{'parent':PARENT,'guarded_parent':LEASE,'existing_inputs_checked':563,'unchanged':557,'production_changed_or_added':PROD,'validation_changed_or_added':TESTS,'new_files':[PROD[-1]]+TESTS[2:],'solver_quantity_worldgen_lod_backend_unchanged':True,'installer_release':False})
save('source-input-manifest.json',inputs)
patch=''
for file in PROD:
 if file==PROD[-1]:before=''
 else:
  b=R/('before-'+Path(file).name);assert gitsha(b.read_bytes())==next(x['sha'] for x in parent if x['path']==file);cp(b,'before/'+Path(file).name);before=b.read_text()
 patch+=''.join(difflib.unified_diff(before.splitlines(True),(P/file).read_text().splitlines(True),fromfile='a/'+file,tofile='b/'+file))
(E/'production.patch').write_text(patch)
for file in ['vulkan-build-audit.json','strict-water-blend-native-audit.json','strict-opengl-blend-audit.json','baseline-source-proof.json','baseline-WaterDirectionBlendChecks.java','pre-mip-WaterDirectionBlendChecks.java','pre-mip-animated_water.fsh','create_blend_fixture.py','apply_blend_fix.py','create_upload_fixture.py','add_blend_mip_checks.py','refine_cell_phase.py','fix_initial_transition.py','initial-transition-reproduction.json','build_candidate.py','run_vulkan.py','audit_vulkan_package.py','audit_water_side.py','audit_opengl_side.py','make_frame_video.py','prepare_checkpoint.py','final-parent.json','rejected-final-archive.json','superseded-flow-auditor.json','inspect_frames.py','reserved-name-animated_water.vsh','glsl-portability-fix.json','pre-portability-vulkan-build-audit.json','rehydrate_evidence.py','global-output-animated_water.vsh','global-output-vulkan-build-audit.json','glsl-output-warning-fix.json']:
 cp(R/file,file)
buildroot=Path('/tmp/deepprimal-blend-explicit-final-build-0645')
for file in ['candidate-build.log','candidate-build-final.gz','candidate-build-result.json']:cp(buildroot/file,file)
raw=(buildroot/'candidate-build.log').read_bytes();build=json.loads((buildroot/'candidate-build-result.json').read_text())
assert build['pass'] and raw==gzip.decompress((buildroot/'candidate-build-final.gz').read_bytes()) and sha(raw)==build['raw_sha256']
assert b'WATER_FLOW_BLEND_CORE_PASS pairs=240' in raw and b'earlyFirstRetarget=true' in raw
assert json.loads((R/'strict-water-blend-native-audit.json').read_text())['all_focused_gates_pass']
assert json.loads((R/'strict-opengl-blend-audit.json').read_text())['pass']
outer,=list((P/'build/libs').glob('*-vulkan-linux.jar'));modules=[]
with zipfile.ZipFile(outer) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   b=z.read(n);match=[f for f in P.glob('*/build/libs/*.jar') if f.read_bytes()==b];assert len(match)==1
   modules.append({'nested_path':n,'standalone':str(match[0].relative_to(P)),'bytes':len(b),'sha256':sha(b),'identical':True})
assert len(modules)==5;save('module-identities.json',modules)
for label,d in [('baseline-build','/tmp/deepprimal-blend-baseline-build-0645'),('superseded-first-build','/tmp/deepprimal-blend-full-final-build-0645'),('superseded-cell-build','/tmp/deepprimal-blend-cell-final-build-0645'),('rejected-truncated-outer-build','/tmp/deepprimal-blend-accepted-build-0645'),('superseded-pre-portability-build','/tmp/deepprimal-blend-accepted-retry-build-0645'),('superseded-global-output-build','/tmp/deepprimal-blend-portable-final-build-0645')]:
 for n in ['candidate-build.log','candidate-build-final.gz','candidate-build-result.json']:cp(Path(d)/n,label+'/'+n)
FINAL=['vulkan-order-blend-cell-final','vulkan-order-blend-cell-final-packaged','vulkan-reuse-gl-blend-final']
OLD=['vulkan-blend-baseline','vulkan-order-blend-final','vulkan-order-blend-checked-final','vulkan-order-blend-cell-pre-portability','vulkan-order-blend-cell-pre-portability-packaged','vulkan-reuse-gl-reserved-name-failure','vulkan-order-blend-global-output','vulkan-order-blend-global-output-packaged','vulkan-reuse-gl-global-output-warning']
for case in OLD+FINAL:
 d=B/case
 # Keep a compact lossless pixel archive rather than hundreds of individual Git objects.
 images=sorted([f for f in d.glob('*.png')])
 with zipfile.ZipFile(E/(case+'-controlled-pixels.zip'),'w',zipfile.ZIP_DEFLATED) as z:
  for f in images:z.write(f,f.name)
 for f in d.rglob('*'):
  if not f.is_file():continue
  rel=f.relative_to(d)
  if case in OLD and (f.suffix=='.webm' or f.name in ['frame-video-provenance.json','frame-videos.json']):continue
  if any(s in rel.parts for s in ['isolated-disk-cache','upload-retry-store']):continue
  if f.suffix=='.png':
   if rel.parent==Path('.') or case in OLD or f.name.startswith('order-live-frame-'):continue
  elif f.suffix not in ['.json','.txt','.gz','.bin','.webm']:continue
  cp(f,case+'/'+str(rel))
 seal=json.loads((d/'sealed-console.json').read_text());b=(d/'console.log').read_bytes()
 assert b==gzip.decompress((d/'console-final.gz').read_bytes()) and sha(b)==seal['raw_sha256']
save('rejected-and-superseded-summary.json',{'baseline':'Unchanged production fails controlled 100ms direction interpolation:7680 channels beyond tolerance,max120; explicit failure and normal shutdown preserved.','validation_compile_failure':'First expanded fixture redeclared loop variable group in callback. Renamed sampleGroup; failed compileVisualValidation excluded. Full build does not include visual fixture compilation.','superseded_native':'224-render candidate passes16 directions at2 section origins but lacks diagnostic mips and cell15 coverage. Preserved, not counted as final acceptance.','early_retarget':'Self-audit found first change within100ms of creation postponed despite no active blend. Standalone failing reproduction preserved; initial idle endpoint may now admit immediately.','truncated_outer_archive':'Gradle success produced outer archive7438052 bytes without central directory. All5 standalone modules CRC passed. Native preflight rejected before launch. Only isolated archive outputs removed; full serial retry required. Cause unresolved; final CRC/class audit mandatory.','superseded_flow_auditor':'An old invariant assumed constant Color.a255. Replaced with exact active/source/cell packing check from actual positions; inactive255 still required. Preserved raw evidence unchanged.','glsl_portability':'OpenGL actual client crashed before controls: PACKED_TOK syntax error from reserved variable packed. Identifier renamed cellBits;both Vulkan and OpenGL final gates rerun. Passing pre-rename Vulkan runs excluded from final counts.','glsl_output_warning':'Renamed shader renders correctly but Mesa warns flowBounds global output used uninitialized in helper. Strict audit rejects it. Tile bounds now explicit helper input;all native gates rerun.','final_only':FINAL})
obs=[json.loads((B/c/'water-direction-blend-details.json').read_text()) for c in FINAL]
assert all(len(a)==240 and all(x['channelsBeyondTolerance']==0 for x in a) for a in obs)
audit=json.loads((R/'vulkan-build-audit.json').read_text())
summary={'production_files_changed_or_added':5,'validation_files_changed_or_added':5,'existing_parent_inputs_checked':563,'unchanged_parent_inputs':557,'core_ordered_direction_pairs':240,'final_controlled_shader_renders':1008,'final_RGBA_blend_samples':720,'directions':16,'origins':'near0,far+29999984/-29999984;farcell15','mip_control_groups_per_session':16,'max_channel_error':max(x['maxChannelError'] for a in obs for x in a),'channels_beyond_tolerance':0,'channel_tolerance':1,'final_upload_readbacks':21,'final_upload_readback_bytes':15120,'history_cap':32768,'cpu_float_stride':14,'gpu_vertex_stride_bytes':36,'new_gpu_work':'second texture fetch during active100ms blend;extra varyings and integer phase;unprofiled','full_clean_serial_build':'pass;25 executed tasks','archive_crcs':len(audit['archives_crc_pass']),'class_identities':sum(audit['compiled_class_byte_matches'].values()),'module_identities':5,'strict_software_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'retained_side_exact_pairs':18,'retained_phase_pairs':36,'live_lake_frames':27,'actual_ocean_wave_geometry_exercised':False,'owner_gpu_fps_acceptance':False,'full_stream_acceptance':False,'installer_released':False}
save('checkpoint-summary.json',summary)
for f in ['blend-before-after.png','blend-mip-contact.png']+[c+'-contact.png' for c in FINAL]:cp(R/f,f)
save('screenshot-inspection.json',json.loads((R/'screenshot-inspection.json').read_text()))
save('code-self-audit.json',{'scope':'Temporal flow-direction presentation only','transition':'Two stable endpoints plus one latest request. Never chain weighted endpoint histories. Stale revision rejected; repeated upload keeps start; queue admitted after current100ms ends. Idle first retarget immediate. Still/flow sprite switch remains immediate.','payload':'CPU14floats,GPU36bytes unchanged. Lowbytes of block/skylight preserved. UV2.x bits11-15 source;UV2.y bit15 active andbits8-14 age. Active-top Color.a carries cell x/z;shader restores tint alpha before applying existing opacity Normal fields.','phase':'Q25 modular uint arithmetic reconstructs old direction world phase;cell-local origin avoids loss of page-local precision near world limits. Prior explicit unwrapped textureGrad retained for both endpoints, with gradients outside conditional.','bounds':'History LRU capped32768 and reset. Existing bounded cleanup admission schedules latest pending target; eviction may still expose hard switches and needs larger scene coverage.','unchanged':['Water solver/quantity','geography V1/V2/V3','player edits','LOD and Vulkan/OpenGL backend code','side geometry and height/opacity transition logic'],'costs':['Additional bounded per-cell history','extra interstage values','second texture fetch while blending','integer phase arithmetic'],'limits':['Still/flow and material changes remain abrupt','Different-direction spatial shared edges remain open','No speed-dependent UV advection','Long stream/ocean/native/LOD scenes and cap churn not fully accepted','Pixel atlas is opaque;alpha-varying resource packs are not comprehensively accepted','Development Vulkan frames show rectangular distant LOD/horizon cutoffs;cause unresolved and full enabled-view coverage remains open','No matched profiling,ownerGPU/FPS or independent60Hz acceptance']})
readme=f"""# Flow-direction crossfade — 2026-10-23

Source parent {PARENT}; guarded parent {LEASE}. Unreleased continuation on work/engine-after-alpha24. Main/installers and frozen geography remain intact; engine acceptance remains incomplete.

## Reproduction and implementation

Unchanged production jumps between flow-direction UVs. The controlled five-phase test fails with7,680 channels beyond1/255 tolerance and maximum120. Baseline source identities, exact fixture, lossless pixels, explicit failure, sealed console and saved shutdown are retained. This is a controlled shader defect, not a claim to have reproduced every live-stream flicker.

Five production files add a100ms single-draw texture crossfade with two fixed endpoints and one latest pending direction. Repeated uploads preserve elapsed age; older revisions cannot replace admitted target UVs; a new request waits for the current blend to finish. First retarget while idle starts immediately, including within100ms of creation; self-audit reproduced and fixed that candidate-only edge case before acceptance. Still/flow material switching remains immediate.

The shader blends sampled RGBA in one fragment instead of layering translucent meshes. Both endpoints use unwrapped gradients computed outside conditional flow. Q25 integer modular world phase and active-top packed cell coordinates retain per-cell precision at far section edges. Color alpha is restored before existing opacity interpolation; light lowbytes are masked/preserved. No water quantity, solver, geography, edits, LOD/backend or height/opacity clock changes. CPU stride14floats,GPU stride36bytes remain unchanged.

A32768-entry bounded LRU history is real additional CPU memory; the extra varyings, integer math and second texture fetch during blends are real GPU costs. No performance improvement is claimed.

## Fresh checks and exclusions

Clean serial uncached full build passes25 executed tasks and explicit core/geography/hydrology records, including240 ordered direction pairs, repeated uploads, latest queue, stale guards, initial early retarget and bounded endpoints. All6 archive CRCs,409 compiled-class identities and5 nested module identities match. Private agent, validation fixtures and staged unaccepted drain payload remain excluded.

Two strict software Vulkan sessions (development and packaged origins), then scoped OpenGL llvmpipe fallback pass. Across them:1008 actual production-shader renders,720 weighted RGBA comparisons covering16 directions at near and far section/cell15 origins plus diagnostic mip levels; max error{summary['max_channel_error']}/255, zero channels beyond1/255.21 actual production upload readbacks (15120 bytes) verify source/target/age, alpha-cell packing, native light, repeated/queued/stale uploads,32768 cap and reset. Prior side-height/phase/real-client FLOW capture/GPU reuse/partial fade/LOD index/retry/edit/frame/reload/save/shutdown gates pass. Final Vulkan validation errors and tracked GPU resources are zero; no new final crash/save failures.

27 actual lake frames in9 wave/LOD profiles were inspected and stored in lossless RGB VP9 with exact decoded pixel checks. The development Vulkan sheet shows rectangular distant horizon/LOD cutoffs with LOD enabled;disabled-LOD frames differ. Their cause is not established and full enabled-view coverage remains open. These are integration evidence, not complete matched-stream/flicker acceptance. Lake waveBytes remains0 even enabled: actual ocean waves remain open. Videos use illustrative playback, not measured FPS.

The outer archive of a Gradle-successful build was truncated (7438052 bytes,no central directory); native preflight rejected it before launch. Its hash/header/tail and all module CRC observations are preserved. Only isolated archive outputs were removed before a full serial retry; this does not establish the underlying cause. OpenGL caught a real GLSL330 portability defect: variable packed is reserved; Vulkan conversion had accepted it. The crash and shader source are preserved. Renaming it cellBits corrects compilation; both Vulkan sessions and OpenGL were rerun on that exact corrected source. Earlier passing Vulkan sessions are superseded. The strict GL audit then caught a global-output initialization warning despite passing pixels. Tile bounds are now an explicit helper input, and all native gates were rerun; the warning is not suppressed. The initial expanded visual fixture failed compilation due to a callback variable name; that run is preserved and excluded. A passing224-render prototype was superseded by cell15/mip controls. The initial core-transition candidate failed the standalone early-retarget reproduction and was corrected. A retained flow auditor initially assumed Color.a always255. It correctly rejected the new packed active-blend value; the corrected audit derives the cell from actual quad positions and checks the active/source metadata, preserving255 for inactive vertices. Raw shader pixels separately check full opacity. Full build alone does not compile native visual fixtures. Sealed raw/gzip hashes, crash deltas and explicit pass records govern acceptance; Gradle exit0 alone is insufficient. Toolchain is reused from pinned prior checkpoints; current native evidence is fresh, pre-reset observations are historical.

## Reproduction and continuing work

Run python rehydrate_evidence.py from this evidence directory, then python audit_water_side.py vulkan-order-blend-cell-final vulkan-order-blend-cell-final-packaged and python audit_opengl_side.py. The helper extracts controlled PNGs, verifies/inflates sealed logs and reconstructs opaque lake PNGs from lossless videos, checking every decoded RGB hash. PNG compression may differ from original files; RGB pixels are exact. The original PNG hashes remain recorded for provenance. Native runs need the pinned toolchain and disposable Vulkan-primary-20261022 saved world, with DEEPPRIMAL_NATIVE_RUN_DIR set; missing-world preflight rejects before launching. See command/result/module/class/seal records for exact inputs. Source and compact evidence only are preserved; no installer is published.

Next smooth still/flow and material transitions, different-direction shared edges and sustained real stream/ocean coverage; then profile history/cap churn and shader cost on matched scenes. Speed-driven advection, full enabled-view/native/LOD coverage and owner GPU/FPS remain unaccepted. Solver substeps are not independent60Hz.

Keep all existing registered water budgets/headwater outlets/chemistry-volume separation, pressure fanout/p95 and1024 waiters, beyond8192-node/key continuation/coarser LOD, physical unload/reopen/input, marker/reload/atlas/direct-stop-save failures and coherent geology requirements open. Catalog/species stays deferred; the drain experiment stays separate. This is a continuation checkpoint, not engine completion.
"""
(E/'README.md').write_text(readme)
state=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text())
state.update(status='continuation_permitted',updated_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),worker=None,active_until=None,acceptance_complete=False,latest_continuation_evidence=PREFIX+'/README.md',latest_test_summary=summary,local_validation_environment=str(R),current_gate='Flowing-direction100ms single-draw texture blend passes full build/core/package,two strict software Vulkan and scoped GL;336 shader renders and7 actual uploads each. Still/flow/material transitions,different-direction spatial edges,sustained streams/ocean,profiling and broader engine remain unfinished. No installer.')
state['next_items'][0]='Owner priority seamless water: smooth still/flow and other material transitions, different-direction shared edges; validate sustained actual streams/ocean and history-cap churn with matched profiling. Direction-only100ms fixed-endpoint/latest-request blend now passes controlled near/farcell15/mip pixels and actual repeated/queued/stale GPU upload checks. Preserve quantity,edits and bounded work; speed advection and full native/LOD coverage remain open.'
state['next_items'][1]='Reproduce and fix the rectangular distant horizon/LOD cutoffs observed in current development Vulkan lake captures, using matched view and load state. '+state['next_items'][1]
state['promotion_blockers'].append('Current development Vulkan lake screenshots show rectangular distant horizon/LOD cutoffs with LOD enabled;cause unresolved. Fixed-camera visible-page counts do not establish full enabled-view coverage.')
state['promotion_blockers'].append('Direction-only crossfade adds bounded32768 history, varyings/integer phase and second texture fetch during blends; cost unprofiled. Still/flow/material and spatial different-direction transitions,history cap churn,long streams/ocean and owner GPU/FPS not accepted.')
state['latest_graphics_evidence']=[PREFIX+'/'+f for f in ['README.md','strict-water-blend-native-audit.json','strict-opengl-blend-audit.json','vulkan-build-audit.json','module-identities.json','source-scope-audit.json','code-self-audit.json','candidate-build-result.json','screenshot-inspection.json','checkpoint-summary.json','rejected-and-superseded-summary.json']]
(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(state,indent=2)+'\n')
for file in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
 p=P/file;p.write_text(p.read_text()+'\n\n## 2026-10-23 — direction-only water crossfade\n\nReproduced abrupt direction sampling (baseline7680 channels beyond1/255,max120), then added100ms fixed-endpoint single-draw RGBA blend with latest pending target, preserved age/stale guards, bounded32768 history, exact distant cell phase and preserved light/opacity/payload sizes. Self-audit caught initial-idle retarget delay, fixture compile error and GLSL330 reserved-name crash; corrected, preserved and excluded. A truncated Gradle-successful outer archive was also rejected; its cause remains unresolved. Clean serial25-task build,240 ordered direction pairs,6 CRCs/409 class identities/5 modules pass. Two strict software Vulkan and scoped OpenGL pass1008 controlled renders/720 blend comparisons within1/255,21 actual upload readbacks/15120 bytes,plus prior water/LOD/frame/reload/save/shutdown gates.27 lake frames inspected;waves0 bytes.\n\nEvidence: ['+PREFIX+'/README.md]('+PREFIX.removeprefix('docs/')+'/README.md). Still/flow/material and spatial different-direction continuity,sustained streams/ocean,history cap churn and matched profiling remain open. Preserve registered pressure/water budget/chemistry/geology/LOD/native blockers. No owner GPU/FPS,superiority,independent60Hz or installer claim. Lease released;acceptance incomplete.\n')
save('evidence-manifest.json',[{'path':str(p.relative_to(E)),'bytes':p.stat().st_size,'sha256':sha(p.read_bytes())} for p in sorted(E.rglob('*')) if p.is_file() and p.name!='evidence-manifest.json'])
texts=[];binaries=[]
for p in [P/f for f in PROD+TESTS+['docs/ENGINE-WORK-STATE.json','docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']]+[p for p in sorted(E.rglob('*')) if p.is_file()]:
 b=p.read_bytes();rel=str(p.relative_to(P))
 if p.suffix in ['.png','.bin','.gz','.webm','.zip']:binaries.append({'path':rel,'local_path':str(p),'bytes':len(b),'git_sha':gitsha(b),'sha256':sha(b)})
 else:texts.append({'path':rel,'type':'blob','mode':'100644','content':b.decode()})
(R/'checkpoint-text-elements.json').write_text(json.dumps(texts));(R/'checkpoint-binary-manifest.json').write_text(json.dumps(binaries,indent=2)+'\n')
print(json.dumps({'text_files':len(texts),'binary_files':len(binaries),'binary_bytes':sum(x['bytes'] for x in binaries),'summary':summary}))
