from pathlib import Path
import json,hashlib,gzip,shutil,difflib,zipfile,datetime
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-material-blend-0828')
PREFIX='docs/validation/engine-continuation/water-material-blend-2026-10-23';E=P/PREFIX;E.mkdir(parents=True,exist_ok=True)
PARENT='80978fac66a0893bcf34b566b619fc01fb9e5f8b';LEASE=json.loads((R/'final-parent.json').read_text())['sha']
def sha(b):return hashlib.sha256(b).hexdigest()
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def save(n,x):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def cp(s,n):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(s,p)
PROD=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
TESTS=['src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowBlendChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionUploadChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterMaterialTransitionChecks.java']
parent=json.loads((R/'parent-blob-manifest.json').read_text());inputs=[];changed=[]
for row in parent:
 p=P/row['path']
 if row['path'].startswith('docs/') or not p.is_file():continue
 current=gs(p.read_bytes());same=current==row['sha'];inputs.append({'path':row['path'],'parent_git_sha':row['sha'],'current_git_sha':current,'unchanged':same})
 if not same:changed.append(row['path'])
assert len(inputs)==567 and sorted(changed)==sorted(PROD+TESTS[:3]),(len(inputs),changed)
save('source-scope-audit.json',{'parent':PARENT,'guarded_parent':LEASE,'existing_inputs_checked':567,'unchanged':560,'production_changed_or_added':PROD,'validation_changed_or_added':TESTS,'new_files':TESTS[3:],'solver_quantity_worldgen_lod_backend_unchanged':True,'installer_release':False})
save('source-input-manifest.json',inputs)
patch=''
for file in PROD:
 b=R/('before-'+Path(file).name);assert gs(b.read_bytes())==next(x['sha'] for x in parent if x['path']==file);cp(b,'before/'+Path(file).name)
 patch+=''.join(difflib.unified_diff(b.read_text().splitlines(True),(P/file).read_text().splitlines(True),fromfile='a/'+file,tofile='b/'+file))
(E/'production.patch').write_text(patch)
files=['vulkan-build-audit.json','strict-water-material-native-audit.json','strict-opengl-material-audit.json','baseline-source-proof.json','baseline-WaterMaterialTransitionChecks.java','baseline-package-audit.json','compile-failed-AnimatedWaterRenderer.java','compile-failed-WaterMaterialTransitionChecks.java','initial-apply_material_fix.py','initial-create_material_fixture.py','apply_material_fix.py','create_material_fixture.py','core-before.json','core-after.json','reproduce_core_switch.py','validation-compile-correction.json','production-compile-correction.json','opacity-auditor-correction.json','pre-failfast-run_vulkan.py','build_candidate.py','run_vulkan.py','audit_vulkan_package.py','audit_water_side.py','audit_opengl_side.py','audit_material.py','make_frame_video.py','inspect_frames.py','rehydrate_evidence.py','verify_archived_evidence.py','prepare_checkpoint.py','final-parent.json']
for f in files:cp(R/f,f)
for label,d in [('', '/tmp/deepprimal-material-correct-build-0828'),('baseline-build','/tmp/deepprimal-material-baseline-build-0828'),('rejected-compile-build','/tmp/deepprimal-material-candidate-build-0828')]:
 for f in ['candidate-build.log','candidate-build-final.gz','candidate-build-result.json']:cp(Path(d)/f,str(Path(label)/f))
build=E/'candidate-build.log';res=json.loads((E/'candidate-build-result.json').read_text());assert res['pass'] and build.read_bytes()==gzip.decompress((E/'candidate-build-final.gz').read_bytes()) and sha(build.read_bytes())==res['raw_sha256']
assert b'WATER_FLOW_BLEND_CORE_PASS pairs=272' in build.read_bytes()
audit=json.loads((R/'vulkan-build-audit.json').read_text());assert json.loads((R/'strict-water-material-native-audit.json').read_text())['all_focused_gates_pass'] and json.loads((R/'strict-opengl-material-audit.json').read_text())['pass']
outer,=list((P/'build/libs').glob('*-vulkan-linux.jar'));mods=[]
with zipfile.ZipFile(outer) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   b=z.read(n);match=[f for f in P.glob('*/build/libs/*.jar') if f.read_bytes()==b];assert len(match)==1;mods.append({'nested_path':n,'standalone':str(match[0].relative_to(P)),'bytes':len(b),'sha256':sha(b),'identical':True})
assert len(mods)==5;save('module-identities.json',mods)
FINAL=['vulkan-order-material-final','vulkan-order-material-final-packaged','vulkan-reuse-gl-material-final'];OLD=['vulkan-material-compile-failure','vulkan-material-baseline']
for case in OLD+FINAL:
 d=B/case
 with zipfile.ZipFile(E/(case+'-controlled-pixels.zip'),'w',zipfile.ZIP_DEFLATED) as z:
  for f in sorted(d.glob('*.png')):z.write(f,f.name)
 for f in d.rglob('*'):
  if not f.is_file():continue
  rel=f.relative_to(d)
  if any(s in rel.parts for s in ['isolated-disk-cache','upload-retry-store']):continue
  if f.suffix=='.png':
   if rel.parent==Path('.') or case in OLD or f.name.startswith('order-live-frame-'):continue
  elif f.suffix not in ['.json','.txt','.gz','.bin','.webm']:continue
  cp(f,case+'/'+str(rel))
 seal=json.loads((d/'sealed-console.json').read_text());raw=(d/'console.log').read_bytes();assert raw==gzip.decompress((d/'console-final.gz').read_bytes()) and sha(raw)==seal['raw_sha256']
save('rejected-and-superseded-summary.json',{'unchanged_production_native':'Explicit FAIL despite exit0/normal saved stop:1055 bad vertices,774022 channels beyond tolerance,max219. Exact fixture,source proof,raw vertices and lossless pixels preserved.','unchanged_production_core':'32 still/flow switches fail;patched32 pass.','validation_compile_failure':'org.joml wildcard import made Math ambiguous. Explicit imports corrected before baseline native run. No launched game;not acceptance.','production_compile_failure':'Candidate called nonexistent snapshot.flow accessor. Corrected to existing direction accessor;full serial uncached build rerun. Failed source/build retained.','opacity_auditor_correction':'Original auditor assumed opacity timing byte0 throughout constantopacity test. Existing original-start clock reaches-100;exact -min(time,100) now required with127/127 opacity endpoints,pad0 andopaque pixels. Raw evidence unchanged;no production suppression.','final_only':FINAL})
summary={'production_files_changed_or_added':4,'validation_files_changed_or_added':4,'existing_parent_inputs_checked':567,'unchanged_parent_inputs':560,'core_ordered_direction_pairs':272,'material_routes_per_session':64,'final_material_shader_draws':2304,'final_material_RGBA_comparisons':1920,'final_material_upload_readbacks':1179,'final_material_readback_bytes':848880,'max_channel_error':1,'channels_beyond_tolerance':0,'channel_tolerance':1,'still_history_violations':0,'retained_direction_shader_draws':1008,'retained_direction_RGBA_comparisons':720,'retained_direction_upload_readbacks':21,'history_cap':32768,'cpu_float_stride':14,'gpu_vertex_stride_bytes':36,'new_gpu_work':'additional flat vec4 previous sprite bounds;16 bytes varying;unprofiled. Existing two samples during blends andbounded history retained. Two spare matrix values reused;vertex allocation unchanged.','full_clean_serial_build':'pass;25 executed tasks','archive_crcs':len(audit['archives_crc_pass']),'class_identities':sum(audit['compiled_class_byte_matches'].values()),'module_identities':5,'strict_software_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'retained_side_exact_pairs':18,'retained_phase_pairs':36,'live_lake_frames':27,'actual_ocean_wave_geometry_exercised':False,'owner_gpu_fps_acceptance':False,'full_stream_acceptance':False,'installer_released':False,'observed_lod_horizon_cutoffs':True,'new_final_crashes_and_save_errors':0,'final_vulkan_validation_errors_and_tracked_gpu_leaks':0}
save('checkpoint-summary.json',summary)
for f in ['material-before-after.png','material-mip-contact.png']+[c+'-contact.png' for c in FINAL]:cp(R/f,f)
inspection={'inspected':FINAL,'contact_sheets':3,'live_lake_frames':27,'material_contact_sheets':['material-before-after.png','material-mip-contact.png'],'observations':['Controlled baseline jumps to new sprite;candidate source/quarter/half/threequarter/target changes gradually. Mip controls stay green;no adjacent atlas tile bleed in fixture.','Near lake remains visible with stable bank/underwater geometry in these short consecutiveframes. Distant rectangular horizon/LOD cutoffs remain visible with LOD enabled;disabled view differs. Cause and completecoverage unresolved.','Actual ocean waveBytes0 even enabled;no oceanwave or sustained streamflicker acceptance.','Lake videos losslessRGB with decodedbyte equality;illustrative5fps not timing acceptance.'],'full_visual_acceptance':False}
save('screenshot-inspection.json',inspection)
save('code-self-audit.json',{'scope':'Still/flow top-surface sprite transition;side-face material switches remain immediate.','transition':'Same two fixed endpoints and latest pending request over100ms;code16 now participates. Stale target cannot replace admitted still UV;still cell-local coordinates reconstructed.','sparse_history':'Initial stable still water creates no history;previous accepted nonempty snapshot direction seeds firstchange. Same32768-entry bound andreset.','sprites':'Current andprevious endpoint carry independent atlas tile bounds. Prior flow usesQ25 worldphase;prior still usescell-local phase. Gradients computed unwrapped outsideconditional;single-fragment RGBA crossfade.','payload':'CPU14floats,GPU36bytes unchanged. Source16 in highmetadata bits with lowlight176/240 preserved;activecellalpha restored tofull before opacity. Two spare TextureMat values reused. New flatvec4 previousBounds is16-byte varying cost.','unchanged':['WaterRuntime/WaterTransport','WaterPatchMesher quantity/geometry','player edits','frozen V1/V2/V3 geography','LOD and Vulkan/OpenGL backend code','original opacity andheight timing'],'limits':['Side-face material/tint/other liquid families unresolved','Different-direction spatialedges unresolved','No speed-driven advection','Longstreams,ocean,capchurn andmatchedprofiling open','Opaque diagnostic atlas;arbitraryalpha/resourcepacks notaccepted','LOD horizoncutoffs unresolved','No ownerGPU/FPS/60Hz/superiority acceptance']})
(E/'README.md').write_text(f'''# Still/flow surface crossfade — 2026-10-23

Source parent {PARENT}; guarded parent {LEASE}. Unreleased continuation on work/engine-after-alpha24. Main, installers and frozen geography remain intact. Engine acceptance remains incomplete.

## Reproduction and implementation

Unchanged production switches abruptly between still and flowing top sprites. A standalone core reproduction fails all 32 switches. The actual production upload/shader baseline exits zero and saves normally, but explicitly fails: 1,055 bad vertices, 774,022 channels beyond 1/255 tolerance, maximum error 219. The exact fixture, source identities, raw vertex bytes, lossless PNG controls and sealed console are retained. Gradle success alone is insufficient.

Four production files extend the existing 100 ms blend through direction 16 (still). Two fixed endpoints and one latest pending request avoid chains of blended history. The previous accepted nonempty snapshot supplies the first endpoint; unchanged still cells create no history. Queued and stale requests preserve admitted target UVs, including reconstructed cell-local still coordinates. Each shader endpoint carries independent sprite bounds. Flow keeps the existing Q25 world phase; still keeps cell-local phase. Unwrapped gradients remain outside conditionals, and sampled RGBA blends in one fragment.

The 32,768-entry history cap, CPU 14-float/GPU 36-byte strides and original opacity/height timing remain unchanged. Two spare texture-matrix values supply still bounds. A new flat vec4 adds 16 bytes of interstage values; its cost is unprofiled. Solver, water quantity, mesher geometry, player edits, geography and LOD/backend source are unchanged. No performance improvement is claimed.

## Fresh validation

The full serial uncached build passes all 25 tasks and explicit core, geography and hydrology records, including 272 ordered direction pairs and repeated, queued, stale and early-idle cases. All six archive CRCs, 409 compiled-class identities and five nested/standalone module identities match. Validation fixtures, the private build agent and the unaccepted drain experiment remain excluded from the payload.

Two strict software Vulkan sessions (development and packaged class origins), followed by scoped OpenGL software fallback, pass. Each checks 64 still-to-flow/flow-to-still routes at near cell 3 and far cell 15, origins ±29,999,984, with 768 actual production-upload shader draws, 640 weighted RGBA comparisons and 393 actual GPU upload readbacks. Across three sessions: 2,304 draws, 1,920 comparisons and 1,179 readbacks (848,880 bytes), maximum error 1/255, zero channels beyond tolerance, bad vertices or sparse-still-history violations.

The independent Python audit rereads raw bytes, derives light/source/age/UV/opacity expectations from positions and actual sprite bounds, and recalculates weighted PNG pixels. Queued reversals, stale rejection and cleanup deadlines are checked. Retained direction, phase, mip, side, real capture/FLOW, GPU reuse, partial fade, LOD index/retry/edit, frame, reload, save and shutdown gates pass. Final Vulkan validation errors and tracked resources are zero; there are no new final crash or save failures. Environmental sound-device errors are not suppressed.

All 27 lake frames across nine wave/LOD profiles were inspected and stored in lossless RGB VP9 with exact decoded-byte checks. Rectangular distant horizon/LOD cutoffs remain visible; their cause is unresolved. Wave geometry remains zero bytes even when enabled, so actual ocean waves remain untested. Short lake sequences and the opaque diagnostic atlas do not establish complete stream, flicker, transparent resource-pack or enabled-view acceptance. Video playback at five frames per second is illustrative, not measured FPS.

The first visual fixture failed compilation because wildcard JOML imports made Math ambiguous; explicit imports fixed it before the native baseline. The first candidate build called nonexistent snapshot.flow; this was corrected to the existing direction accessor before the full build and all final native runs. Failed sources and logs are preserved and excluded. The initial independent opacity auditor expected a zero timing byte, while the existing original-start clock reaches -100 even with constant opacity. The corrected audit demands exact -min(time,100), 127/127 endpoints, zero padding and opaque rendered pixels. Raw evidence and production were unchanged for that correction. The failing baseline confirms the gate detects the defect.

## Replay and remaining work

Run `python verify_archived_evidence.py` for CPU-only replay in a temporary copy. It extracts controlled PNGs, verifies sealed gzip logs, restores 27 frames from lossless videos with RGB hash checks, and reruns both strict auditors. PNG compression may differ; decoded pixels must match exactly. For extraction in place, run `python rehydrate_evidence.py`, then `python audit_water_side.py vulkan-order-material-final vulkan-order-material-final-packaged` and `python audit_opengl_side.py`.

Native runs require the recorded pinned toolchain and disposable Vulkan-primary-20261022 saved world through DEEPPRIMAL_NATIVE_RUN_DIR. Current measurements are fresh; pre-reset observations remain historical. Only source and compact evidence are preserved; no installer is published.

Next implement and validate side-face still/flow/material continuity, then different-direction spatial edges, sustained actual streams/ocean, history-cap churn and matched CPU/GPU profiling. Speed-driven advection, other liquid families/tints, owner GPU/FPS and full enabled-view acceptance remain open. Solver substeps are not independent 60 Hz simulation.

Retain registered headwater outlets, regional reservoir budgets and compatible chemistry/volume separation; pressure fanout/p95, 1,024 waiters and beyond-8,192 continuation; coarser LOD, physical unload/input, marker/reload/atlas/save investigations and coherent geology. Catalog/species remains deferred, and the unaccepted drain experiment stays separate. This checkpoint is a continuation point, not engine completion.
''')
state=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text());state.update(status='continuation_permitted',updated_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),worker=None,active_until=None,acceptance_complete=False,latest_continuation_evidence=PREFIX+'/README.md',latest_test_summary=summary,local_validation_environment=str(R),current_gate='Still/flow top-surface 100 ms blend passes full build, 272 core pairs, package checks, two strict software Vulkan sessions and scoped GL: 2304 actual-upload shader draws, 1920 RGBA checks and 1179 readbacks. Side-face material/tint, spatial direction edges, long streams/ocean, profiling and broader engine acceptance remain open. No installer.')
state['next_items'][0]='Owner seamless-water priority: implement side-face still/flow/material continuity, then different-direction spatial shared edges, sustained actual streams/ocean and history-cap churn with matched profiling. Top still/flow 100 ms blending now passes 64 near/far routes per session with actual production uploads and shader pixel/mip checks under two software Vulkan and scoped GL sessions. Preserve quantity, edits and sparse bounded history; speed advection, tints, other liquids and full native/LOD acceptance remain open.'
state['promotion_blockers'][-1]='Top direction and still/flow crossfades pass controlled actual-upload near/far pixel/mip gates. The bounded 32768-entry history, second sample and added 16-byte previousBounds varying remain unprofiled. Side-face material/tint, spatial different-direction continuity, cap churn, long streams/ocean, arbitrary alpha packs and owner GPU/FPS remain unaccepted.'
state['latest_graphics_evidence']=[PREFIX+'/'+f for f in ['README.md','strict-water-material-native-audit.json','strict-opengl-material-audit.json','vulkan-build-audit.json','module-identities.json','source-scope-audit.json','code-self-audit.json','candidate-build-result.json','screenshot-inspection.json','checkpoint-summary.json','rejected-and-superseded-summary.json','offline-audit-replay.json']]
(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(state,indent=2)+'\n')
for file in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
 p=P/file;marker='\n\n## 2026-10-23 — still/flow top-surface continuity';prior=p.read_text().split(marker)[0]
 p.write_text(prior+marker+f'''

Four production files extend the 100 ms fixed-endpoint texture blend through still code 16, with sparse previous-snapshot admission, independent sprite bounds, queued/stale guards and unchanged 14-float/36-byte vertices. The unchanged-production baseline explicitly fails despite normal exit/save: 1055 bad vertices, 774022 channels beyond 1/255, maximum 219. Full serial 25-task build, 272 core pairs, six CRCs, 409 class identities and five module identities pass. Two strict software Vulkan sessions and scoped GL pass 2304 actual-upload draws, 1920 weighted RGBA checks and 1179 GPU readbacks (848880 bytes), maximum 1/255 and zero violations. Retained water/LOD/reload/save gates pass. The failed fixture-import and production-accessor builds plus an opacity-auditor correction are preserved and excluded.

Evidence: [still/flow checkpoint](validation/engine-continuation/water-material-blend-2026-10-23/README.md). All 27 lake frames inspected; distant LOD cutoffs remain and wave bytes are zero. Next: side-face material/tint transitions, spatial edges, long streams/ocean, cap churn and matched profiling. Preserve registered water budgets/chemistry, pressure, geology and LOD/native blockers. Added flat vec4 cost is unprofiled. No owner GPU/FPS, independent 60 Hz, superiority or installer claim. Lease released; engine acceptance incomplete.
''')
print(json.dumps({'prepared':str(E),'summary':summary}))
