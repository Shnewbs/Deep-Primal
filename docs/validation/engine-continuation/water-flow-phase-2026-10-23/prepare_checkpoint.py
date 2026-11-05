from pathlib import Path
import json,hashlib,gzip,shutil,difflib,zipfile,datetime
R=Path(__file__).resolve().parent;P=R/'project'
PREFIX='docs/validation/engine-continuation/water-flow-phase-2026-10-23';E=P/PREFIX;E.mkdir(parents=True,exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def gitsha(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def save(n,x):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def cp(src,dst):
 src=Path(src);dst=E/dst;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
PROD=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java','src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
TESTS=['src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowTextureChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFlowTextureChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFlowPhaseChecks.java']
parent=json.loads((R/'parent-blob-manifest.json').read_text());inputs=[];changed=[]
for row in parent:
 p=P/row['path']
 if row['path'].startswith('docs/') or not p.is_file():continue
 current=gitsha(p.read_bytes());same=current==row['sha']
 inputs.append({'path':row['path'],'parent_git_sha':row['sha'],'current_git_sha':current,'unchanged':same})
 if not same:changed.append(row['path'])
assert len(inputs)==562 and sorted(changed)==sorted(PROD+TESTS[:3]),changed
save('source-scope-audit.json',{'parent':'dad0e5581f183b36dd5ef3490cc3f11e20bf5b1b','guarded_parent':'56a156fc1e7b767e75882c1557693778480b92a9','existing_inputs_checked':562,'unchanged':555,'production_changed':PROD,'validation_changed_or_added':TESTS,'new_files':[TESTS[-1]],'solver_quantity_worldgen_lod_backend_unchanged':True,'installer_release':False})
save('source-input-manifest.json',inputs)
patch=''
for file in PROD:
 before=R/('before-'+Path(file).name);assert gitsha(before.read_bytes())==next(x['sha'] for x in parent if x['path']==file)
 cp(before,'before/'+Path(file).name)
 patch+=''.join(difflib.unified_diff(before.read_text().splitlines(True),(P/file).read_text().splitlines(True),fromfile='a/'+file,tofile='b/'+file))
(E/'production.patch').write_text(patch)
for file in ['candidate-build.log','candidate-build-final.gz','candidate-build-result.json','vulkan-build-audit.json','strict-water-phase-native-audit.json','strict-opengl-phase-audit.json','baseline-source-proof.json','baseline-WaterFlowPhaseChecks.java','initial-WaterFlowPhaseChecks.java','implicit-mip-WaterFlowPhaseChecks.java','implicit-gradient-animated_water.fsh','rejected-vulkan-archive.json','preflight-empty-world-check.json','create_phase_fixture.py','apply_phase_fix.py','add_mip_control.py','finalize_mip_checks.py','build_candidate.py','run_vulkan.py','audit_vulkan_package.py','audit_water_side.py','audit_opengl_side.py','make_frame_video.py','prepare_checkpoint.py']:
 cp(R/file,file)
raw=(R/'candidate-build.log').read_bytes();build=json.loads((R/'candidate-build-result.json').read_text())
assert build['pass'] and raw==gzip.decompress((R/'candidate-build-final.gz').read_bytes()) and sha(raw)==build['raw_sha256']
assert b'WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges=400000 maxCircularError=5.960464477539063E-8' in raw
assert json.loads((R/'strict-water-phase-native-audit.json').read_text())['all_focused_gates_pass']
assert json.loads((R/'strict-opengl-phase-audit.json').read_text())['pass']
outer,=list((P/'build/libs').glob('*-vulkan-linux.jar'));modules=[]
with zipfile.ZipFile(outer) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   b=z.read(n);match=[f for f in P.glob('*/build/libs/*.jar') if f.read_bytes()==b];assert len(match)==1
   modules.append({'nested_path':n,'standalone':str(match[0].relative_to(P)),'bytes':len(b),'sha256':sha(b),'identical':True})
assert len(modules)==5;save('module-identities.json',modules)
for label,d in [('baseline-build','/tmp/deepprimal-phase-baseline-build-0507'),('rejected-missing-helper-build','/tmp/deepprimal-phase-final-build-0507'),('superseded-phase-build','/tmp/deepprimal-phase-clean-build-0507'),('implicit-mip-control-build','/tmp/deepprimal-phase-mip-control-build-0507'),('rejected-truncated-archive-build','/tmp/deepprimal-phase-gradient-final-build-0507')]:
 for n in ['candidate-build.log','candidate-build-final.gz','candidate-build-result.json']:cp(Path(d)/n,label+'/'+n)
save('rejected-and-superseded-summary.json',{'missing_helper_build':'Renderer could not see added core methods; compiled/packaged core lacked them despite source changes. Copied build state suspected, root cause not established; deleting isolated build state restored correct compilation.','truncated_archive_build':'jarJar rejected a 10,192,270-byte Vulkan dependency lacking a ZIP central directory. Header/tail/hash retained; cause unresolved. Clean serial build passes six CRCs and class identities; not claimed as an underlying bug fix.','wrong_world_native':'First attempt used a fresh directory without the named reference world, stayed at DisconnectedScreen and timed out. Excluded. Corrected retry briefly overlapped its final shutdown; neither is timing/performance evidence. Final sessions run sequentially. Missing-world preflight now rejects before launch.','phase_only_prototype':'Six base pixel pairs passed, but diagnostic mip controls later exposed stripes. Superseded; not final acceptance.','implicit_mip_controls':{'different_pixels':1713,'pixels_with_channel_error_above_one':132,'max_channel_error':255,'base_pairs_exact':6,'scope':'Actual shader/mip sampler diagnostic; other one-unit differences reflect rounding at the mip boundary.'}})
BASE=Path('/tmp/deepprimal-flow-phase-0507')
FINAL=['vulkan-order-phase-gradient-final','vulkan-order-phase-gradient-final-packaged','vulkan-reuse-gl-phase-gradient-final']
OLD=['vulkan-phase-baseline','vulkan-phase-baseline-correct','vulkan-phase-baseline-mip-implicit','vulkan-order-phase-final']
for case in OLD+FINAL:
 d=BASE/case
 for f in d.rglob('*'):
  if not f.is_file():continue
  rel=f.relative_to(d)
  if any(s in rel.parts for s in ['isolated-disk-cache','upload-retry-store']):continue
  if 'screenshots' in rel.parts:
   if case in OLD or f.name.startswith('order-live-frame-'):continue
  elif f.suffix not in ['.json','.txt','.gz','.png','.bin','.webm']:continue
  cp(f,case+'/'+str(rel))
 seal=json.loads((d/'sealed-console.json').read_text());b=(d/'console.log').read_bytes()
 assert b==gzip.decompress((d/'console-final.gz').read_bytes()) and sha(b)==seal['raw_sha256']
base=json.loads((BASE/'vulkan-phase-baseline-correct/water-flow-phase-pixel-details.json').read_text());assert sum(x['differentPixels'] for x in base)==6144
observations=[json.loads((BASE/c/'water-flow-phase-pixel-details.json').read_text()) for c in FINAL]
assert all(len(rows)==12 and sum(x['differentPixels'] for x in rows[:6])==0 and sum(x['beyondTolerancePixels'] for x in rows)==0 for rows in observations)
for file in ['phase-before-after.png','mip-before-after.png']+[c+'-contact.png' for c in FINAL]:cp(R/file,file)
summary={'production_files_changed':4,'validation_files_changed_or_added':4,'existing_parent_inputs_checked':562,'unchanged_parent_inputs':555,'core_direction_origin_samples':100000,'core_shared_edge_component_checks':400000,'core_max_circular_phase_error':5.960464477539063e-8,'original_baseline_differing_pixels':6144,'implicit_mip_pixels_beyond_one_channel_unit':132,'implicit_mip_max_channel_error':255,'final_phase_base_exact_pairs':18,'final_mip_pairs':18,'final_phase_controlled_renders':72,'final_phase_base_differing_pixels':0,'final_mip_differing_pixels':sum(x['differentPixels'] for rows in observations for x in rows[6:]),'final_mip_max_channel_error':max(x['maxChannelError'] for rows in observations for x in rows[6:]),'final_phase_pixels_beyond_tolerance':0,'retained_side_exact_pairs':18,'retained_side_upload_readbacks':36,'retained_side_upload_bytes':25920,'retained_flow_direction_gpu_samples':18,'live_lake_frames':27,'wave_lod_profiles':9,'cpu_float_stride':14,'gpu_vertex_bytes':36,'uniform_allocation':'same Matrix4f; added flat vec4 varying and fragment arithmetic are not cost-free','full_clean_serial_build':'pass;25 executed tasks','archive_crcs':6,'class_identities':408,'module_identities':5,'strict_software_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'new_final_crashes_and_save_errors':0,'final_vulkan_validation_errors_and_tracked_gpu_leaks':0,'actual_ocean_wave_geometry_exercised':False,'owner_gpu_fps_acceptance':False,'full_stream_or_direction_retarget_acceptance':False,'installer_released':False}
save('checkpoint-summary.json',summary)
save('screenshot-inspection.json',{'inspected_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sheets':['phase-before-after.png','mip-before-after.png']+[c+'-contact.png' for c in FINAL],'controlled_phase':'Two adjacent tiles now match continuous references. Custom gradient is diagnostic and is not an assertion that the gradient itself is periodic or representative water art.','mip_diagnostic':'Implicit wrapping produces blue coarse-mip spots against near-green hardware-repeat reference. Explicit unwrapped gradients remove large differences; one-unit mip-boundary rounding remains within declared tolerance.','lake':'27 real frames inspected. Water/shore/terrain visible. First development session has a changing far-left terrain silhouette during loading; these are integration/capture evidence, not matched-state or whole-scene coverage/flicker acceptance.','ocean':'Enabled waves have waveBytes=0 at the lake; actual ocean waves remain open.','timing':'Lossless videos preserve RGB pixels exactly; illustrative playback is not measured FPS or independent 60Hz.'})
save('code-self-audit.json',{'scope':'Same-direction flow-top spatial phase and mip selection','phase':'Two world-origin offsets per top quad, reused by its four vertices. Double intermediates; bounded periodic floats. No extra per-cell histories.','geometry_quantity':'Mesh geometry, transitions, water quantity, solver/core transport, saved geography, LOD/backend code unchanged. Core mesher adds only pure numeric phase helpers.','storage':'14 CPU floats,36 GPU vertex bytes and existing Matrix4f allocation unchanged. Flat vec4 adds interstage data; fract/derivatives/textureGrad add GPU arithmetic and require matched profiling.','lighting':'UV2.x bit10 tags flow tops; previous side bits8/9 and native low-byte block light preserved; shader masks before light lookup; skylight unchanged.','sampling':'Wrap in fragment after interpolation, deriving LOD from unwrapped gradients. Default nearest/mipmap path scoped by diagnostic; arbitrary filtering/resource packs not fully accepted.','limits':['Different neighboring flow directions and temporal direction/material retargets remain discontinuous; world anchoring changes retarget phase and needs dedicated continuity work.','Speed-driven advection and sustained streams/ocean/native/LOD boundary coverage remain open.','No new frame-time, FPS, owner GPU, superiority or independent60Hz acceptance.','Preserved unexplained build artifact failures; serial retry is not proven root-cause repair.']})
readme='''# World-anchored flowing-water phase — 2026-10-23

Parent dad0e5581f183b36dd5ef3490cc3f11e20bf5b1b; guarded lease parent 56a156fc1e7b767e75882c1557693778480b92a9. Unreleased continuation on work/engine-after-alpha24. Main/installers and frozen geography remain intact; engine acceptance is incomplete.

## Defects and change

Flowing top UVs restarted in each block. Six controlled neighboring-tile versus continuous-reference comparisons on unchanged production fail with 6,144 differing pixels and matching geometry. Baseline production identities, exact fixture source, raw pixels, complete sealed console and normal saved shutdown are preserved. The reference specifies periodic world phase; the gradient is a diagnostic, not representative water art.

The first spatial-phase prototype passes six base pairs, but self-audit caught a second defect: implicit mip derivatives after fract select coarse mips at diagonal wrap boundaries. Diagnostic hardware-repeat comparisons show 132 pixels beyond a one-unit channel allowance, maximum error255 (1,713 total differences including ordinary one-unit rounding). That candidate is superseded and preserved, not promoted as complete.

Four production files now add bounded world-cell phase helpers, apply them once per top quad, carry a top tag in unused light bits, wrap only at sampling, and derive mip selection from unwrapped UV gradients. Native block light is masked before lookup, skylight and side-height UV morph remain unchanged. CPU stride14 floats, GPU stride36 bytes and uniform allocation remain unchanged. A flat vec4 interstage value and additional arithmetic are real costs, not a performance improvement claim. Quantity, solver, opacity clocks, page bounds, player edits, LOD/backend code and geography remain unchanged.

## Fresh checks

Final clean serial uncached full build passes25 executed tasks and explicit core/geography/hydrology records. All6 archive CRCs,408 compiled class identities and5 nested/standalone module identities match; validation/private-agent/unaccepted-drain payloads are excluded. Core checks cover16 directions,100,000 direction/origin samples and400,000 edge-component comparisons, including negative coordinates, section edges and near30-million coordinates; maximum circular error is5.960464477539063e-8.

Two strict software Vulkan sessions (development and packaged origins) and a scoped OpenGL llvmpipe fallback pass the final source. New controls total18 exactly equal base pixel pairs and18 mip pairs with explicit one-channel-unit tolerance at the mip boundary;72 controlled renders, zero pixels beyond tolerance. Mip pairs are not described as pixel-exact. Prior side-height tests retain18 exact pairs and36 actual upload readbacks/25,920 bytes for repeated uploads/stale revisions/light; real-client FLOW capture and18 GPU direction/material samples before/after reload pass with world phase and top tags. Prior memory-capacity/partial-fade/LOD index/retry/edit/reload/frame/save/shutdown gates remain passing; final Vulkan tracked resources and validation errors are zero. No new final crashes or save errors.

27 actual lake frames in9 wave/LOD profiles were inspected and preserved as lossless RGB VP9 with byte-exact decode verification. First development frames have changing distant-left terrain while loading; they are not matched-state/full-scene coverage or flicker acceptance. Lake waveBytes=0 even with waves enabled, so actual ocean geometry remains untested. Video timing is illustrative, not measured FPS.

Rejected evidence includes a missing-reference-world launch, a compile/artifact method mismatch and a truncated Vulkan dependency archive. Their causes are not established by successful retries. Only isolated build state was discarded; final serial outputs pass independent CRC/byte checks. The native helper now rejects a missing reference world before Gradle/display startup, with a passing preflight check. Reused pinned toolchain and software-driver provenance are linked in the parent checkpoint; no pre-reset measurements are counted as fresh acceptance.

## Continuing work

This fixes spatial phase for equal directions, not complete seamless water. Next smooth temporal direction/material retargeting and different-direction shared edges; world anchoring changes phase on direction changes and needs dedicated continuity checks. Speed-driven advection, sustained actual streams, ocean waves and native/LOD boundary coverage stay open. Added shader/helper work needs matched frame-time/GPU profiling; software-driver checks do not establish owner GPU/FPS or market superiority. Solver substeps are not independent60Hz.

Retain the requirement register: renewable headwater/reservoir and constituent budgets, compatible-liquid composition/volume separation, pressure fanout/p95 and1,024 waiters, beyond8,192-node/key continuation/coarser LOD, physical unload/reopen/input, unexplained marker/reload/atlas/save failures and coherent geology remain unfinished. The staged drain experiment stays separate and unaccepted. Catalog/species stays deferred. No installer is released; this is a continuation checkpoint.
'''
(E/'README.md').write_text(readme)
state=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text())
state.update(status='continuation_permitted',updated_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),worker=None,active_until=None,acceptance_complete=False,latest_continuation_evidence=PREFIX+'/README.md',latest_test_summary=summary,local_validation_environment=str(R),current_gate='Fixed same-direction flow-top tile phase resets and reproduced mip-wrap stripes. Clean serial full build/core/package, two strict software Vulkan sessions and scoped OpenGL pass. Base pixels exact; diagnostic mip controls allow one channel unit rounding. Direction retargets/different-direction edges, full streams/ocean, profiling and broader engine acceptance remain open. No installer.')
state['next_items'][0]='Owner priority seamless flowing water: implement smooth direction/material retargets and different-direction edge continuity; world-anchored phase can change abruptly when direction changes. Same-direction spatial tile phase and explicit-gradient mip stripes now fixed, retaining prior side-height/stale upload continuity and payload size. Then speed-dependent advection, sustained real streams and actual ocean-wave/shared/native/LOD/reload/travel/memory coverage, preserving quantity and edits.'
state['promotion_blockers'].append('World-phase fix has no full temporal/different-direction acceptance. Added flat varying, phase arithmetic and explicit-gradient sampling require matched CPU/GPU frame-time checks; no owner GPU/FPS or complete filtering/resource-pack coverage. New compile/artifact mismatch and truncated archive attempts were excluded; their root causes remain unproven.')
state['latest_graphics_evidence']=[PREFIX+'/'+f for f in ['strict-water-phase-native-audit.json','strict-opengl-phase-audit.json','vulkan-build-audit.json','module-identities.json','source-scope-audit.json','code-self-audit.json','candidate-build-result.json','screenshot-inspection.json','checkpoint-summary.json','rejected-and-superseded-summary.json']]
(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(state,indent=2)+'\n')
for file in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
 p=P/file;p.write_text(p.read_text()+'\n\n## 2026-10-23 — flow-top world phase and mip-wrap audit\n\nFixed same-direction per-block UV resets (6,144 differing baseline pixels) and a superseded implicit-wrap mip defect (132 pixels beyond1/255 tolerance; max255). Four production files preserve14-float CPU/36-byte GPU payload and existing matrix allocation, quantity and frozen geography. Final clean serial25-task build,400,000 shared-edge component checks,6 CRCs/408 class identities/5 module identities, two strict software Vulkan sessions and scoped OpenGL pass.18 base pairs exact;18 diagnostic mip pairs within explicit1/255 rounding tolerance. Retained side/fade/actual GPU upload/flow/LOD/frame/reload/save gates pass.27 lake frames inspected; distant loading changes and waveBytes0 prevent complete scene/ocean acceptance. Failed missing-world/compile/truncated-archive attempts are preserved and excluded; causes not claimed resolved.\n\nEvidence: ['+PREFIX+'/README.md]('+PREFIX.removeprefix('docs/')+'/README.md). Next smooth temporal/different-direction retargets and validate sustained streams; profile added varying/arithmetic. Keep all registered pressure/budget/chemistry/LOD/geology/native blockers open. No installer, owner GPU/FPS, superiority or independent60Hz claim. Lease released; acceptance incomplete.\n')
save('evidence-manifest.json',[{'path':str(p.relative_to(E)),'bytes':p.stat().st_size,'sha256':sha(p.read_bytes())} for p in sorted(E.rglob('*')) if p.is_file() and p.name!='evidence-manifest.json'])
texts=[];binaries=[]
for p in [P/f for f in PROD+TESTS+['docs/ENGINE-WORK-STATE.json','docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']]+[p for p in sorted(E.rglob('*')) if p.is_file()]:
 b=p.read_bytes();rel=str(p.relative_to(P))
 if p.suffix in ['.png','.bin','.gz','.webm']:binaries.append({'path':rel,'local_path':str(p),'bytes':len(b),'git_sha':gitsha(b),'sha256':sha(b)})
 else:texts.append({'path':rel,'type':'blob','mode':'100644','content':b.decode()})
(R/'checkpoint-text-elements.json').write_text(json.dumps(texts));(R/'checkpoint-binary-manifest.json').write_text(json.dumps(binaries,indent=2)+'\n')
print(json.dumps({'text_files':len(texts),'binary_files':len(binaries),'binary_bytes':sum(x['bytes'] for x in binaries),'summary':summary}))
