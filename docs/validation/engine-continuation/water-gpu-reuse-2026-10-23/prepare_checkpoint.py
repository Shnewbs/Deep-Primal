from pathlib import Path
import json,hashlib,gzip,shutil,zipfile,difflib,datetime
R=Path(__file__).resolve().parent;P=R/'project';C=R/'checkpoint';rel='docs/validation/engine-continuation/water-gpu-reuse-2026-10-23';E=C/rel;E.mkdir(parents=True,exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
audit=json.loads((R/'strict-water-gpu-reuse-native-audit.json').read_text());assert audit['all_focused_gates_pass'] and len(audit['sessions'])==2
gl=json.loads((R/'strict-opengl-reuse-audit.json').read_text());assert gl['pass']
build=json.loads((R/'candidate-build-result.json').read_text());b=gzip.decompress((R/'candidate-build-final.gz').read_bytes());assert build['pass'] and sha(b)==build['raw_sha256'] and b==(R/'candidate-build.log').read_bytes()
for marker in ['WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40','WATER_SURFACE_SHAPE_PASS','HYDROLOGY_PASS randomizedTransfers=500000']:assert marker in b.decode()
package=json.loads((R/'vulkan-build-audit.json').read_text());first=json.loads((R/'first-package-audit.json').read_text());assert package['jar_sha256']==first['jar_sha256'] and sum(package['compiled_class_byte_matches'].values())==407
baseline=json.loads((R/'source-baseline.json').read_text());tree={e['path']:e['sha'] for e in json.loads((R/'parent-tree.json').read_text())['tree'] if e['type']=='blob'}
changed=[]
for path in baseline['verified']:
 b=(P/path).read_bytes();git=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
 if git!=tree[path]:changed.append({'path':path,'before_git_blob_sha':tree[path],'after_git_blob_sha':git,'sha256':sha(b)})
prod='src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';gfx='graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/WaterGpuMesh.java';hook='src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java';new='src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterGpuReuseChecks.java'
assert {e['path'] for e in changed}=={prod,gfx,hook}
scope={'parent':baseline['commit'],'parent_inputs_checked':512,'parent_inputs_unchanged':509,'changed':changed,'new_validation_file':new,'production_files_changed':2,'validation_files_changed_or_added':2,'authoritative_water_runtime_core_lod_vulkan_and_frozen_geography_unchanged':True}
(R/'source-scope-audit.json').write_text(json.dumps(scope,indent=2)+'\n')
patch=''
for path,name in [(prod,'AnimatedWaterRenderer'),(gfx,'WaterGpuMesh')]:patch+=''.join(difflib.unified_diff((R/('before-'+name+'.java')).read_text().splitlines(True),(P/path).read_text().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
(R/'water-gpu-reuse.patch').write_text(patch)
outer=P/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-linux.jar';assert sha(outer.read_bytes())==package['jar_sha256'];identities=[]
with zipfile.ZipFile(outer) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   payload=z.read(n);basename=Path(n).name.removeprefix('com.shnewbs.deepprimal.');f,=[p for p in P.glob('*/build/libs/*.jar') if p.name==basename];assert f.read_bytes()==payload;identities.append({'nested':n,'sha256':sha(payload),'standalone_matches':True})
assert len(identities)==5
(R/'module-identities.json').write_text(json.dumps(identities,indent=2)+'\n')
for path in [e['path'] for e in changed]+[new]:
 d=C/path;d.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(P/path,d)
for name in ['source-scope-audit.json','source-baseline.json','before-AnimatedWaterRenderer.java','before-WaterGpuMesh.java','water-gpu-reuse.patch','candidate-build-result.json','candidate-build-final.gz','vulkan-build-audit.json','module-identities.json','strict-water-gpu-reuse-native-audit.json','strict-opengl-reuse-audit.json','run_vulkan.py','audit_water_reuse.py','audit_opengl_reuse.py','audit_vulkan_package.py','build_candidate.py','prepare_checkpoint.py']:
 shutil.copyfile(R/name,E/name)
binary=[str((E/'candidate-build-final.gz').relative_to(C))];screens=[]
for name in ['vulkan-reuse-baseline']+[s['case'] for s in audit['sessions']]+[gl['case']]:
 src=R/name;dst=E/name;dst.mkdir(exist_ok=True)
 for n in ['result.json','command.json','sealed-console.json','crash-delta.json','action-history.json','startup-marker-before.json','startup-marker-after.json','module-prelaunch-audit.json','startup-lifecycle.txt','water-gpu-reuse.txt','water-fade-continuity.txt','lod-index-merge.txt','lod-upload-retry.txt','lod-disk-recovery.txt','frame-completion.txt','saved-disconnect.txt','shutdown.txt','console-final.gz']:
  f=src/n
  if f.exists():shutil.copyfile(f,dst/n)
 raw=gzip.decompress((src/'console-final.gz').read_bytes());assert sha(raw)==json.loads((src/'result.json').read_text())['console_sha256'];binary.append(str((dst/'console-final.gz').relative_to(C)))
 (dst/'selected-records.txt').write_text('\n'.join(l for l in raw.decode().splitlines() if any(k in l for k in ['_PASS','WATER_GPU_REUSE','AssertionError','VALIDATION_CLASS_ORIGIN','VULKAN_RESOURCES_RELEASED','VULKAN_OPERATION_COUNTS','BUILD SUCCESSFUL','BUILD FAILED']))+'\n')
 if name!='vulkan-reuse-baseline':
  assert raw==(src/'console.log').read_bytes()
  for f in (src/'screenshots').glob('*.png'):
   d=dst/'screenshots'/f.name;d.parent.mkdir(exist_ok=True);shutil.copyfile(f,d);binary.append(str(d.relative_to(C)));screens.append({'path':str(d.relative_to(C)),'sha256':sha(f.read_bytes()),'inspected':True,'scope':'Actual active lake scene; not a video/readback of the isolated memory-pressure fixture, matched equal-state comparison or complete flicker acceptance.'})
delta=json.loads((R/'vulkan-reuse-baseline/crash-delta.json').read_text())['new_or_changed'];assert len(delta)==1
name,digest=next(iter(delta.items()));crash=Path('/tmp/deepprimal-travel-correct-0035/crash-reports')/name;assert sha(crash.read_bytes())==digest
(E/'vulkan-reuse-baseline/crash-excerpt.txt').write_text('\n'.join(crash.read_text().splitlines()[:30])+'\n')
# Exclude the fallback's inherited Vulkan-only shutdown injection despite Gradle zero.
src=Path('/tmp/deepprimal-water-reuse-0135/vulkan-reuse-gl-final');dst=E/'rejected-opengl-harness';dst.mkdir(exist_ok=True)
for n in ['result.json','command.json','sealed-console.json','crash-delta.json','console-final.gz','action-history.json']:
 shutil.copyfile(src/n,dst/n)
raw=gzip.decompress((src/'console-final.gz').read_bytes());assert b'Shutdown fixture requires actual Vulkan' in raw and json.loads((src/'result.json').read_text())['exit_code']==0
binary.append(str((dst/'console-final.gz').relative_to(C)))
(dst/'rejection.json').write_text(json.dumps({'accepted':False,'reason':'OpenGL harness inherited DEEPPRIMAL_SHUTDOWN_RECORD and triggered a Vulkan-only stopping assertion. Gradle exit zero is insufficient. Scoped reuse/readback/fade/reload/save records precede failure; this session is excluded from all passing gates.','correction':'run_vulkan.py removes Vulkan shutdown injection for reuse-gl mode; a fresh separate session must pass strict audit.','console_sha256':sha(raw)},indent=2)+'\n')
(dst/'selected-failure.txt').write_text('\n'.join(l for l in raw.decode().splitlines() if any(k in l for k in ['Shutdown fixture requires','AssertionError','BUILD SUCCESSFUL','CLIENT_SAVED_DISCONNECT_PASS']))+'\n')
(E/'screenshot-inspection.json').write_text(json.dumps(screens,indent=2)+'\n')
(E/'README.md').write_text('''# Water GPU updates under memory pressure — 2026-10-23

Continues `653c5bcd` on the unreleased working branch, following the owner's seamless-flow priority. This is a demonstrated stalled-update fix, not complete live-scene flicker acceptance. Two production files change: the water renderer and the shared graphics module's water GPU mesh. Authoritative water quantities/solver, core, LOD, Vulkan backend and published V1/V2/V3 geography remain byte-identical. Main and supplied installers remain unchanged.

## Reproduction and independent fix

The baseline's upload admission charges the new water mesh while retaining its old allocation. A same-sized update can fail at the 32 MiB budget when all other pages are closer or busy, leaving the existing water geometry stale and repeatedly retrying. The native fixture creates a real 46,080-byte water page and 33,508,224 bytes of actual GPU ballast in a closer page. Resident payload is 33,554,304 bytes, 128 below the cap. The next same-size update is rejected: `accepted=false`, followed by the expected assertion `Near-capacity same-size water update stalled or displaced closer coverage`. The baseline crash, explicit failure and sealed logs are retained; it is not counted as passing validation. Controlled pages are not drawn into the lake screenshot.

The renderer now reuses a page's GPU allocation when the new data fits. WaterGpuMesh creates an updatable vertex buffer, validates quad count/byte length/capacity, writes through the shared CommandEncoder API, then updates the index count. Full allocation capacity remains charged after smaller updates, allowing safe regrowth without understating resident memory. Oversized replacements retain the previous admission/closer-page protections and retry path; this does not allocate old+new beyond the existing payload budget or steal closer coverage. The cap is owned water vertex payload, not a total VRAM guarantee: staging, deferred frees and driver overhead are separate. Existing Vulkan transfer barriers handle prior reads and following vertex reads; the backend itself is unchanged. The OpenGL fallback uses the same public GPU API.

Native candidates execute 16 alternating real same-sized water uploads at the near-capacity occupancy, verify the same GPU object and unchanged resident capacity/closer ballast, then shrink and regrow. An oversized update stays pending with old coverage, and succeeds after actual ballast is released. A separate test-only mesh adds legal COPY_SRC solely for readback: updating its first 144 bytes preserves the remaining 144 bytes, with all 288 GPU bytes checked after a fence. Invalid count/byte shape is rejected before writing. This readback uses the production update method but a test-only readable-source buffer; it does not certify the displayed river pixels. All isolated GPU/worker ownership is released.

## Fresh checks and limits

512 parent input paths are Git-blob verified; final scope is 509 unchanged, two production files plus one validation hook changed and one new opt-in fixture. Full sealed uncached Linux Vulkan/core/module/geography build passes 25 executed tasks. Existing water/core checks pass 10,000 animation retargets, 40 shared edges, vertical/page shape controls and 500,000 conservative transfers. Six outer/nested archive CRCs, 407 compiled-class matches and five standalone/nested module identities pass; no validation/helper payload leaks. Development JAR SHA: JAR_SHA (unchanged through native validation; no installer release).

Two strict development/packaged software-Vulkan sessions pass memory-pressure reuse, GPU payload, earlier dry-fade/deadline/rewet/edit protection, index bound, LOD retry/eviction/edit/capture, frame/device/reload/normal-save/shutdown gates. Explicit pass records and producer-sealed/live hashes are required in addition to Gradle exit; both sessions have no new crash/save/VUID/validation errors and zero final tracked buffers/images. A third scoped OpenGL software-driver session passes water reuse/payload/fade, reload, capture and normal save; it is fallback coverage, not a full backend comparison. Vulkan remains the primary reference API. All screenshots are inspected as limited still-scene evidence; fine repeated water patterns/darker bands and repetitive terraces remain visible. No temporal/no-flicker conclusion follows from them.

The first OpenGL attempt was rejected despite Gradle exit zero: it inherited a Vulkan-only shutdown injection and logged `Shutdown fixture requires actual Vulkan`. This is a harness configuration failure, not evidence of a water/GPU implementation failure. Its sealed output and rejection reason are preserved. The corrected runner removes only that Vulkan-specific injection in OpenGL mode, and the separate final fallback session must pass the strict audit.

No independent 60 Hz, owner-GPU/FPS, market-superiority or complete enabled-view claim. Existing pressure p95/waiters/8192-node residency, regional water budgets, geology, spatial/paged/coarser LOD, actual player-input/server physical unload/reopen, scene overlaps/gaps and marker/reload/atlas/direct-stop failures remain open. Staged retained-drain source stays separate. Catalog/species stays deferred.

## Next work

Capture live-flow frame/coverage sequences across adjacent horizontal/vertical pages and native/animated/LOD handoffs, including dry/rewet, reload/travel and real memory pressure. Trace stable flow UV/timebase and remove only reproduced discontinuities while preserving water quantity and player edits. Isolated buffer reuse closes one cause of stale geometry; the engine acceptance register is not complete.

Reproduce in an isolated project/toolchain with build_candidate.py and audit_vulkan_package.py. Run run_vulkan.py reuse-final and reuse-final-packaged sequentially with private DEEPPRIMAL_CAPTURE_ROOT and disposable DEEPPRIMAL_NATIVE_RUN_DIR; symlink captures beside scripts for audit_water_reuse.py. Run reuse-gl-final and audit_opengl_reuse.py for the fallback. Restoring both before-source files reproduces the baseline assertion before the candidate-only payload checks. Respect the work-state lease.
'''.replace('JAR_SHA',package['jar_sha256']))
section='''\n\n## Water GPU update continuity under memory pressure — 2026-10-23

[Source and explicit before/after evidence](validation/engine-continuation/water-gpu-reuse-2026-10-23/README.md) fix a reproduced same-size water upload stall. A real 46,080-byte page beside 33,508,224 GPU ballast bytes cannot replace itself under the baseline's 32 MiB admission check, despite requiring no larger final allocation. The renderer now updates the existing GPU allocation when data fits. The shared graphics mesh retains full capacity accounting and updates the draw count; larger updates keep the bounded admission/retry and closer-page coverage protections.

Full sealed uncached build/core/geography, six CRCs/407 class matches/five module identities, two strict development/packaged Vulkan sessions and one scoped OpenGL fallback session pass. Native checks cover 16 near-capacity updates, shrink/regrowth, bounded oversized retry, 288 GPU bytes read back, and prior fade/LOD/GPU/frame/reload/save/shutdown protections. The fixture uses actual GPU allocations but controlled isolated pages; images do not prove live-flow temporal continuity or full coverage. Two production files change; water solver/core/LOD/Vulkan backend/frozen geography remain identical. Main/installers are unchanged; no owner GPU/FPS/independent 60 Hz or market-superiority claim.

Next establish actual live-flow frames/coverage and adjacent-page/native/LOD continuity with dry/rewet/reload/travel/low memory; flow UV/timebase remains open. Keep spatial/coarser LOD, pressure performance/waiter/residency, regional water budgets/geology and existing native failures open. Catalog/species remains deferred. Lease released, acceptance incomplete.
'''
for path in ['docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md']:
 d=C/path;d.parent.mkdir(parents=True,exist_ok=True);d.write_text((P/path).read_text()+section)
s=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text());s.update(status='continuation_permitted',worker=None,active_until=None,updated_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),acceptance_complete=False,unreleased_changes=True,latest_continuation_evidence=rel+'/README.md',local_validation_environment=str(R))
s['current_gate']='Reproduced and fixed same-size water upload stalls under the 32 MiB payload cap using in-place GPU reuse with full capacity accounting. Full uncached build, two strict Vulkan sessions and a scoped OpenGL fallback pass real allocations/288-byte readback, 16 updates, shrink/regrowth and bounded growth retry. Controlled pages do not prove live-scene seamless flow; no installer release.'
s['next_items'][0]='Owner priority: seamless-looking flowing water without visual flickers. Dry-fade deletion and near-capacity same-size GPU upload stalls are fixed. Capture matched live-flow temporal/coverage sequences and shared horizontal/vertical/native/animated/LOD handoffs under dry/rewet, reload/travel and memory pressure. Trace flow UV/timebase before making new changes. Preserve finite water quantity, bounded work and player edits.'
s['latest_test_summary']={'fresh_production_files_changed':2,'validation_files_changed_or_added':2,'parent_inputs_checked':512,'parent_inputs_unchanged':509,'native_baseline':'expected same-size upload rejection/assertion beside actual GPU ballast','target_gpu_bytes':46080,'ballast_gpu_bytes':33508224,'resident_bytes':33554304,'payload_budget_bytes':33554432,'same_size_reuse_updates':16,'smaller_regrowth_and_bounded_growth_retry':True,'separate_test_mesh_readback_bytes':288,'retained_allocation_capacity_accounted':True,'sealed_uncached_linux_vulkan_build':'pass;25 executed tasks','core_retargets':10000,'core_shared_edge_samples':40,'core_randomized_conservative_transfers':500000,'archive_crc_count':6,'compiled_class_matches':407,'module_identities':5,'strict_final_vulkan_sessions':2,'scoped_opengl_fallback_sessions':1,'final_vulkan_buffers_images_validation_errors':0,'new_candidate_crashes_and_save_errors':0,'full_live_flow_flicker_acceptance':False,'owner_gpu_fps_acceptance':False,'authoritative_water_core_lod_vulkan_geography':'unchanged','installer_released':False}
s['latest_graphics_evidence']=[rel+'/'+n for n in ['strict-water-gpu-reuse-native-audit.json','strict-opengl-reuse-audit.json','vulkan-build-audit.json','source-scope-audit.json','candidate-build-result.json','screenshot-inspection.json']]
(C/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(s,indent=2)+'\n')
manifest=[{'path':str(f.relative_to(C)),'bytes':f.stat().st_size,'sha256':sha(f.read_bytes())} for f in E.rglob('*') if f.is_file() and f.name!='evidence-manifest.json'];(E/'evidence-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
(R/'checkpoint-binaries.json').write_text(json.dumps(binary,indent=2)+'\n');elements=[{'path':str(f.relative_to(C)),'mode':'100644','type':'blob','content':f.read_text()} for f in C.rglob('*') if f.is_file() and str(f.relative_to(C)) not in binary];(R/'checkpoint-text-elements.json').write_text(json.dumps(elements));print(json.dumps({'files':len(elements)+len(binary),'binary':len(binary),'lease_release_staged':True,'jar_sha256':package['jar_sha256']}))
