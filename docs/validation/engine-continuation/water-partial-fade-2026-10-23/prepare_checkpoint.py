from pathlib import Path
import json,hashlib,gzip,shutil,zipfile,difflib,datetime
R=Path(__file__).resolve().parent;P=R/'project';C=R/'checkpoint';rel='docs/validation/engine-continuation/water-partial-fade-2026-10-23';E=C/rel;E.mkdir(parents=True,exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def copy(src,dst):dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
audit=json.loads((R/'strict-water-partial-native-audit.json').read_text());assert audit['all_focused_gates_pass'] and len(audit['sessions'])==2
gl=json.loads((R/'strict-opengl-reuse-audit.json').read_text());assert gl['pass']
build=json.loads((R/'candidate-build-result.json').read_text());b=gzip.decompress((R/'candidate-build-final.gz').read_bytes());assert build['pass'] and sha(b)==build['raw_sha256'] and b==(R/'candidate-build.log').read_bytes()
for text in ['WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40','HYDROLOGY_PASS randomizedTransfers=500000','25 actionable tasks: 25 executed']:assert text in b.decode()
package=json.loads((R/'vulkan-build-audit.json').read_text());first=json.loads((R/'first-package-audit.json').read_text());assert package['jar_sha256']==first['jar_sha256'] and sum(package['compiled_class_byte_matches'].values())==408
base=json.loads((R/'source-baseline.json').read_text());tree={e['path']:e['sha'] for e in json.loads((R/'parent-tree.json').read_text())['tree'] if e['type']=='blob'}
changed=[]
for path in base['verified']:
 b=(P/path).read_bytes();git=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
 if git!=tree[path]:changed.append({'path':path,'before_git_blob_sha':tree[path],'after_git_blob_sha':git,'sha256':sha(b)})
prod='src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';shader='graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh';hook='src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java';new='src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterPartialFadeChecks.java'
assert {e['path'] for e in changed}=={prod,shader,hook}
scope={'parent':base['commit'],'parent_inputs_checked':513,'parent_inputs_unchanged':510,'changed':changed,'new_validation_file':new,'production_files_changed':2,'validation_files_changed_or_added':2,'authoritative_water_runtime_core_lod_vulkan_and_frozen_geography_unchanged':True,'owner_requirement_register_changed':'docs/WATER-BUDGET-REQUIREMENTS.md'}
(R/'source-scope-audit.json').write_text(json.dumps(scope,indent=2)+'\n')
patch=''
for path,before in [(prod,'before-AnimatedWaterRenderer.java'),(shader,'before-animated_water.vsh')]:patch+=''.join(difflib.unified_diff((R/before).read_text().splitlines(True),(P/path).read_text().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
(R/'water-partial-fade.patch').write_text(patch)
outer=P/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-linux.jar';assert sha(outer.read_bytes())==package['jar_sha256'];identities=[]
with zipfile.ZipFile(outer) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   data=z.read(n);name=Path(n).name.removeprefix('com.shnewbs.deepprimal.');f,=[p for p in P.glob('*/build/libs/*.jar') if p.name==name];assert f.read_bytes()==data;identities.append({'nested':n,'sha256':sha(data),'standalone_matches':True})
assert len(identities)==5;(R/'module-identities.json').write_text(json.dumps(identities,indent=2)+'\n')
for path in [prod,shader,hook,new,'docs/WATER-BUDGET-REQUIREMENTS.md']:copy(P/path,C/path)
for name in ['source-scope-audit.json','source-baseline.json','before-AnimatedWaterRenderer.java','before-animated_water.vsh','water-partial-fade.patch','candidate-build-result.json','candidate-build-final.gz','vulkan-build-audit.json','module-identities.json','strict-water-partial-native-audit.json','strict-opengl-reuse-audit.json','run_vulkan.py','audit_water_partial.py','audit_opengl_partial.py','audit_vulkan_package.py','build_candidate.py','prepare_checkpoint.py']:copy(R/name,E/name)
binary=[str((E/'candidate-build-final.gz').relative_to(C))];screens=[]
cases=['vulkan-partial-baseline','vulkan-partial-baseline2']+[s['case'] for s in audit['sessions']]+[gl['case']]
for name in cases:
 src=R/name;dst=E/name;dst.mkdir(exist_ok=True)
 for n in ['result.json','command.json','sealed-console.json','crash-delta.json','action-history.json','startup-marker-before.json','startup-marker-after.json','module-prelaunch-audit.json','startup-lifecycle.txt','water-partial-fade.txt','water-gpu-reuse.txt','water-fade-continuity.txt','lod-index-merge.txt','lod-upload-retry.txt','lod-disk-recovery.txt','frame-completion.txt','saved-disconnect.txt','shutdown.txt','console-final.gz','live-action.json']:
  if (src/n).exists():copy(src/n,dst/n)
 raw=gzip.decompress((src/'console-final.gz').read_bytes());result=json.loads((src/'result.json').read_text());assert sha(raw)==result['console_sha256'];binary.append(str((dst/'console-final.gz').relative_to(C)))
 (dst/'selected-records.txt').write_text('\n'.join(l for l in raw.decode().splitlines() if any(k in l for k in ['_PASS','WATER_PARTIAL','AssertionError','VALIDATION_CLASS_ORIGIN','VULKAN_RESOURCES_RELEASED','VULKAN_OPERATION_COUNTS','BUILD SUCCESSFUL','BUILD FAILED']))+'\n')
 if 'baseline' not in name:
  assert raw==(src/'console.log').read_bytes()
  for f in (src/'screenshots').glob('*.png'):
   d=dst/'screenshots'/f.name;copy(f,d);binary.append(str(d.relative_to(C)));screens.append({'path':str(d.relative_to(C)),'sha256':sha(f.read_bytes()),'inspected':True,'scope':'Actual active lake still shows fine repeated water patterns/darker bands and terraced terrain. Not a temporal sequence or image of the isolated partial-fade fixture; no full flicker acceptance.'})
 elif name=='vulkan-partial-baseline':
  assert result['timeout'] and b'WATER_PARTIAL_FADE_OBSERVED' not in raw
  (dst/'rejection.json').write_text(json.dumps({'accepted':False,'reason':'Bounded 120-second first action timeout before the new fixture; no assertion, save or shutdown pass. This does not reproduce the mixed-page defect. Cause unresolved; unchanged unforced retry is audited separately.'},indent=2)+'\n')
 else:
  assert not result['timeout'] and b'Mixed wet/dry page discarded fading faces before their deadline' in raw and b'WATER_PARTIAL_FADE_OBSERVED vertices=20 elapsed=25' in raw
  delta=json.loads((src/'crash-delta.json').read_text())['new_or_changed'];assert len(delta)==1;filename,digest=next(iter(delta.items()));f=Path('/tmp/deepprimal-travel-correct-0035/crash-reports')/filename;assert sha(f.read_bytes())==digest
  (dst/'crash-excerpt.txt').write_text('\n'.join(f.read_text().splitlines()[:30])+'\n')
(E/'screenshot-inspection.json').write_text(json.dumps(screens,indent=2)+'\n')
(E/'README.md').write_text('''# Mixed wet/dry page fade continuity — 2026-10-23

Continues `c2294b4b` on the working branch. The owner asked for seamless water and continued actual implementation/self-audit. This checkpoint fixes another demonstrated premature-disappearance path and records the owner's headwater/composition clarification. It does not finish the engine or implement chemistry/reservoirs. Main/installers, authoritative water quantity/solver, engine core, LOD, Vulkan backend and published V1/V2/V3 geography remain unchanged.

## Reproduction

An isolated production renderer uploads two wet patches in one page: 40 real GPU vertices (1,440-byte capacity). One patch dries at controlled t=100 ms; the other stays wet. At t=125 another coherent worker mesh sees only the remaining wet patch. The baseline rebuild replaces the GPU draw count with 20 vertices, deleting the first patch 25 ms into its 100 ms fade. Explicit native assertion/crash context are retained. A preceding first-action timeout never entered this fixture and is excluded; it proves nothing about this defect. Both outputs are sealed and preserved.

The independently authored fix retains only absent face groups whose opacity is still fading toward zero. Current/rewet geometry immediately replaces matching groups. Retained vertices keep their original capture revision, so they cannot masquerade as a new height observation and override newer shared-corner targets. Retention stays within admitted GPU vertex geometry; release/reset and completed fades clear history. Arrays are immutable and reused on subsequent recaptures to avoid copying unchanged dying faces. The focused fixture holds five face groups / 1,120 CPU float bytes, and verifies original revision, array identity, rewet cancellation and completed partial cleanup while the second patch remains visible. The general bound follows admitted vertex capacity (float payload is 56/36 times its GPU vertex payload, plus bounded per-face record/map overhead); this is not a total process/VRAM budget or a frame-time guarantee.

Opacity formerly restarted its 100 ms interval every upload. The GPU format's unused signed-normal third byte now carries the original start offset (-100..0 ms); the shader evaluates it against elapsed page-upload time. Alpha endpoints retain existing 1/127 quantization. Cleanup uses the actual remaining transition duration, preserving the original deadline through repeated captures. Height interpolation and the vertex format size remain unchanged; no water is added, removed or frozen.

## Fresh validation

The native candidate retains all 40 vertices at t=125. Six fenced GPU readbacks inspect 8,640 bytes: the drying patch has opacity .75 and .5; rewetting begins at .5 and advances through .625, .875 and 1 while the other patch stays opaque. Deadlines remain t=201/t=251 rather than extending on every rebuild. A second completed partial fade releases its CPU history/draw faces while retaining the other wet patch and same allocation. Readback uses a test-only legal COPY_SRC buffer on the real page's production GPU update path. Opacity is evaluated from the actual packed shader inputs, not sampled from displayed pixels; these controlled snapshots/time do not prove live-scene temporal coverage. Real scene screenshots still have repeated patterns/bands and cannot establish absence of flicker.

513 parent input paths are Git-blob verified; final comparison is 510 unchanged, two production renderer/shader files plus one validation hook changed and one opt-in fixture added. Full sealed uncached Linux Vulkan/core/module/geography build passes 25 executed tasks, including 10,000 animation retargets/40 shared-edge samples and 500,000 conservative transfers. Six outer/nested CRCs, 408 compiled-class matches, exact shader resource identity and five nested/standalone module identities pass. JAR SHA: JAR_SHA. No installer is released.

Two final strict development/packaged software-Vulkan sessions pass partial fades/readbacks/CPU cleanup and all prior water reuse/dry-fade, LOD retry/eviction/index/edit/capture, Vulkan device/frame/reload/normal-save/shutdown controls, with live/sealed hash identity, no new crashes/save/VUID/validation errors and zero final tracked GPU buffers/images. A separate scoped software OpenGL fallback session repeats partial/readback/reuse/fade/reload/save checks; Vulkan stays primary. Earlier candidate sessions preceded the final immutable-array/cleanup assertions and are not substituted for final-source validation.

## Owner clarification and next implementation

The adopted [water requirements](../../../WATER-BUDGET-REQUIREMENTS.md) now explicitly place metered stream supply at headwaters and separate chemistry from physical water volume. Compatible river/ocean water shares the volume solver; contact-only chemistry changes no cell levels, and actual flow advects conserved constituents. Stable average sea level still requires an accounted ocean exchange. Other liquids need miscibility/layering/reaction rules. Sparse versioned cell/chunk composition is planned; separate fluid IDs do not implement it automatically. These are recorded requirements, not shipped reservoir/composition behavior.

Next capture actual live-flow frame/coverage sequences across shared horizontal/vertical page and native/animated/LOD handoffs, including reload/travel/memory pressure, and trace flow UV/timebase. Keep regional reservoirs/constituents, pressure p95/waiter/residency, spatial/paged/coarser LOD, player-input/server physical unload/reopen and coherent geology open. Catalog/species stays deferred. Prior marker/console writer, atlas/reload/direct-stop failures and this first-action timeout remain unresolved. No independent 60 Hz, owner GPU/FPS, market-superiority or complete visual acceptance claim.

Reproduce with isolated project/toolchain using build_candidate.py and audit_vulkan_package.py, then run_vulkan.py partial-finished and partial-finished-packaged sequentially with private DEEPPRIMAL_CAPTURE_ROOT and disposable DEEPPRIMAL_NATIVE_RUN_DIR. Symlink captures beside the scripts for audit_water_partial.py; run reuse-gl-partial-final with audit_opengl_partial.py for fallback. Restore before-source renderer/shader to reproduce the baseline assertion. Respect the work lease.
'''.replace('JAR_SHA',package['jar_sha256']))
section='''\n\n## Mixed wet/dry page water continuity and headwater/composition direction — 2026-10-23

[Source, explicit baseline and scoped GPU checks](validation/engine-continuation/water-partial-fade-2026-10-23/README.md) fix a mixed-page fade cut: a second capture removed 20 of 40 water vertices only 25 ms into a 100 ms fade while another patch stayed wet. Retain absent still-dying face groups with their original capture revision, reuse immutable arrays, and encode original opacity timing in the existing vertex byte. Rewetting replaces old faces; finished fades release history; cleanup deadlines no longer restart on each rebuild.

Full sealed uncached build/core/geography, six CRCs/408 class matches/exact shader resource/five module identities, two final strict development/packaged Vulkan sessions and scoped OpenGL fallback pass. Six GPU readbacks cover 8,640 bytes and expected fade/rewet progression; CPU history is 1,120 bytes in the fixture, arrays are reused, and completed partial cleanup retains steady water. Prior water/LOD/GPU/frame/reload/save/shutdown protections remain passing. A first baseline startup/action timeout is excluded; its cause is unresolved. Controlled GPU geometry/shader-input checks are not live-scene pixel continuity acceptance. Main/installers, water solver/core/LOD/Vulkan backend and frozen geography remain unchanged.

The owner clarified headwater source outlets and composition mixing. docs/WATER-BUDGET-REQUIREMENTS.md now requires metered headwater supply, shared compatible river/ocean physical volume, conservative constituent mixing/advection, no level rise from chemistry alone, accounted ocean discharge and per-family mixing/layering/reaction rules. Sparse persisted chemistry/reservoirs remain unimplemented. No hidden drain for real inflow or source multiplication.

Next actual live-flow temporal/coverage checks and adjacent-page/native/LOD/UV work; retain water budget/chemistry, pressure continuation/performance, spatial/coarser LOD, geology and previous native blockers. Catalog/species stays deferred. No owner GPU/FPS/independent 60 Hz claim. Acceptance remains incomplete; lease released.
'''
for path in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
 d=C/path;d.parent.mkdir(parents=True,exist_ok=True);d.write_text((P/path).read_text()+section)
s=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text());s.update(status='continuation_permitted',worker=None,active_until=None,updated_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),acceptance_complete=False,unreleased_changes=True,latest_continuation_evidence=rel+'/README.md',local_validation_environment=str(R))
s['current_gate']='Mixed wet/dry page fade cut reproduced (40-to-20 vertices at25ms) and fixed with retained original-revision faces, reused CPU arrays and original GPU opacity deadlines. Full uncached build, final two strict Vulkan sessions and scoped OpenGL fallback pass six readbacks totaling8640 bytes, cleanup and prior safety gates. Owner headwater/chemistry requirements recorded, not implemented. Live-scene flicker acceptance remains open; no installer release.'
s['next_items'][0]='Owner priority: seamless flowing water. Fully/partly dry fade removal, repeated opacity deadline resets and near-capacity same-size upload stalls now have focused fixes. Capture actual matched live-flow frames/coverage and adjacent horizontal/vertical/native/animated/LOD handoffs under dry/rewet/reload/travel/memory pressure; trace flow UV/timebase. Preserve quantity, bounds and player edits.'
for i,item in enumerate(s['next_items']):
 if item.startswith('Implement explicit renewable'):s['next_items'][i]=item+' Owner clarified metered headwater source outlets and shared compatible river/ocean chemistry/volume separation; implement sparse conserved constituent state, mixing/advection and explicit per-liquid compatibility under docs/WATER-BUDGET-REQUIREMENTS.md.'
s['latest_test_summary']={'fresh_production_files_changed':2,'validation_files_changed_or_added':2,'parent_inputs_checked':513,'parent_inputs_unchanged':510,'native_baseline':'explicit mixed-page 40-to-20 vertex removal at25ms, expected assertion','initial_baseline_action_timeout_excluded':True,'gpu_samples':6,'gpu_readback_bytes':8640,'retained_vertices':40,'steady_vertices_after_partial_cleanup':20,'retained_cpu_float_bytes':1120,'immutable_arrays_reused':True,'original_capture_revision_preserved':True,'dry_and_rewet_deadlines_preserved':[201,251],'sealed_uncached_linux_vulkan_build':'pass;25 executed tasks','core_retargets':10000,'core_shared_edge_samples':40,'core_randomized_conservative_transfers':500000,'archive_crc_count':6,'compiled_class_matches':408,'exact_shader_resource_identity':True,'module_identities':5,'strict_final_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'final_vulkan_buffers_images_validation_errors':0,'new_candidate_crashes_and_save_errors':0,'full_live_flow_pixel_acceptance':False,'owner_gpu_fps_acceptance':False,'authoritative_water_core_lod_vulkan_geography':'unchanged','headwater_and_chemistry_implementation':'requirements recorded, implementation open','installer_released':False}
s['latest_graphics_evidence']=[rel+'/'+n for n in ['strict-water-partial-native-audit.json','strict-opengl-reuse-audit.json','vulkan-build-audit.json','source-scope-audit.json','candidate-build-result.json','screenshot-inspection.json']]
(C/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(s,indent=2)+'\n')
manifest=[{'path':str(f.relative_to(C)),'bytes':f.stat().st_size,'sha256':sha(f.read_bytes())} for f in E.rglob('*') if f.is_file() and f.name!='evidence-manifest.json'];(E/'evidence-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
(R/'checkpoint-binaries.json').write_text(json.dumps(binary,indent=2)+'\n');elements=[{'path':str(f.relative_to(C)),'mode':'100644','type':'blob','content':f.read_text()} for f in C.rglob('*') if f.is_file() and str(f.relative_to(C)) not in binary];(R/'checkpoint-text-elements.json').write_text(json.dumps(elements));print(json.dumps({'files':len(elements)+len(binary),'binary':len(binary),'lease_release_staged':True,'jar_sha256':package['jar_sha256']}))
