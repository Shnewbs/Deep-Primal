from pathlib import Path
import json,hashlib,ast,shutil
r=Path(__file__).resolve().parent;e=r/'evidence';p=r/'project';pin='6bd85c6647a9b9f256df5ae2103589bbc8347161'
def rd(n):return json.loads((e/n).read_text())
i=rd('independent-core-result.json');f=rd('focused-checks.json');assert i['pass'] and f['pass'] and rd('harness-failure-controls.json')['pass'];s=rd('source-identity.json')
for x in s['files']:assert hashlib.sha256((p/x['path']).read_bytes()).hexdigest()==x['sha256'],x['path']
s['verified_after_tests']=True;(e/'source-identity.json').write_text(json.dumps(s,indent=2))
ident=[]
for file in (p/'engine-core/build/classes/java/main').rglob('*.class'):
 rel=file.relative_to(p/'engine-core/build/classes/java/main');h=hashlib.sha256(file.read_bytes()).hexdigest();assert h==hashlib.sha256((r/'independent-core'/rel).read_bytes()).hexdigest();ident.append({'class':str(rel),'compiled_sha256':h,'independent_sha256':h})
(e/'partial-core-identity.json').write_text(json.dumps({'pass':True,'source_sha':pin,'classes':ident,'packaged':False},indent=2))
h=rd('host.json');h['workload']='Failed four-task Gradle attempt; full independent core; direct LOD/split checks; deterministic scheduling and injected JVM-failure controls. Zero native sessions, no controlled benchmark.';(e/'host.json').write_text(json.dumps(h,indent=2))
prior=ast.parse((r.parent/'audit-2067e27-20261026T1339/make_report.py').read_text());rows=next(ast.literal_eval(n.value) for n in prior.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='rows' for t in n.targets))
rows[2]=('Hydrology/caves',6,'src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java',451,'Per-column/wrap deadline fix promoted; exact accounting core passes. Busy source loop still starves regular recharge in deterministic reproduction; native water ledger/cave acceptance open.')
rows[4]=('Performance/threading',3,'engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java',36,'Less zero-fill/footprint work with exact output; inspected component medians/tails are mixed. No fresh benchmark, whole-chunk/frame/hardware budget acceptance.')
rows[3]=('Features/structures',4,'src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java',115,'Current direct deposit regressions pass; native replay is historical. Generic spacing/exclusion and structures remain incomplete.')
rows[5]=('Determinism/stability',7,'src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java',260,'Current exact core/accounting checks pass; no current native replay or cross-machine/soak qualification. Prior native passes remain historical.')
rows[6]=('Architecture',6,'engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java',22,'Current direct core lifecycle controls pass; prior module-boundary passes are historical. Native coupling and world-water ownership remain incomplete.')
rows[9]=('Polish/tooling',4,'tools/server_smoke.py',17,'Strict save/shutdown harness and approved source resources preserved; current package/client/platform/tooling acceptance unrun.')
rows[8]=('Tests/docs',7,'src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodReductionChecks.java',30,'Full independent core, ten old/current mesh signatures and strict negative harness controls pass; current full Gradle/package/native gates incomplete.')
g={'source_sha':pin,'total':50,'average':5.0,'tier':'2/5','fresh_complete_regrade':False,'scores':{x[0]:x[1] for x in rows},'status':'all scores carried; fresh scoped core/method checks only; current full build failed, retry storage-blocked','threshold_demonstrated':False};(e/'grade.json').write_text(json.dumps(g,indent=2));disk=shutil.disk_usage('.').free
text=f'''# Independent audit — 6bd85c6 / failed build, scoped checks passed

**No new audit JAR.** Full Gradle build failed before Themes compilation because the auditor setup left its build directory as a dangling shared-memory output link. Path corrected to an ordinary private disk directory; only{disk}bytes of disk headroom remained at reporting. A complete retry, installer/export and native qualification cannot safely fit. Initial failed log/receipt and corrected-path state retained. No failed binary ships, no old build is relabeled new, no stable/CurseForge/GitHub Release promotion.

[Latest owner-qualified dev.30 renderer.3 development JAR](https://chatgpt.com/library?file_id=libfile_abf1214694f48191a9de1ec96dd4632b&account_id=personal). This is the renderer owner's previously qualified artifact, **not produced or independently archive/native-requalified by this audit**. Source5a2131a6309aaf5de6377bef57f433f8f624ef7b; size125294519bytes; SHA256 `9fcca4f2693c0e509440708cbd7133ed647b71d3287ae43d25e6f9e665086375`. Complete selected module/root build-input Git trees match the current audit pin, with its distinct renderer.3 version init. Owner reports36-task full build, recursive package checks and two Linux server sessions after correcting prior whitespace/provenance/cache failures. Those are inspected historical owner evidence, not current auditor passes. No duplicate Library upload.

[Last independently qualified auditor JAR, older source2067e27](https://chatgpt.com/library?file_id=libfile_0b07185d35548191b187382a12ad02da&account_id=personal). Its125294742bytes/SHA256 ba71f7937145171558326a3ebdca332d85f04b89b33a2ec24a77e4bf8a0b5c56 belong to the previous audit; it predates both current production repairs.

## Identity, coordination and actual changes

Pinned main at start: `{pin}`. Independent audit checkout/cache/output and own receipt; immutable toolchain reused read-only. Required engine policies/work-state/rubric and completed builder/renderer receipts inspected. Renderer CPU measurement claim completed; root builder1510 checkpointed, explicitly nonexclusive. No active exclusive timing reservation prevented this attempt. Timing reservations were re-read before the build. No builder checkout/cache/lease/process was edited, paused, killed, reset or required to stop. Only this auditor's unused package waiter was stopped through its own exec session after build failure, exit130; no background build remains. Existing auditor stays enabled because this is a temporary storage limitation and >8.5 has not been demonstrated.

Since prior pin9b26b86, committed HydrologyRuntime checks the shared2ms deadline before every source-column admission; all five survey/tick nanoTime comparisons use signed subtraction for wrap safety. Source headwater order/quota/quantities remain unchanged. Version advances todev.30. LodMesher skips zero writes into already-zero arrays and stops material reduction at the first opaque sample; air/water/material priority and all mesh data remain equivalent in scoped tests. New private LodReductionChecks is committed. No R57/R60/R61/R62 or newer recharge-reservation prototype is promoted. Frozen V1–V7 geography, saved profiles, quantities, owner edits, approved art, Deep Primal identity and separate graphics/LOD/Vulkan modules preserved.

## Ten equal categories

**50/100, average5.0/10, Tier2/5. All scores carried; not a fresh complete regrade.** Scoped fixes/probes do not close the original whole-engine rubric. Last complete accepted baseline remains50/100. Renderer author's separate3.50/10 self-score is not substituted. No unused/staged library earns gameplay credit.

| Category | /10 | Status | Pinned implementation / actual evidence |
|---|---:|---|---|
'''
for name,score,path,line,why in rows:text+=f'| {name} | {score} | carried | [L{line}](https://github.com/Shnewbs/Deep-Primal/blob/{pin}/{path}#L{line}): {why} |\n'
text+=f'''
## Fresh checks passed

Full independent Java25 javac -g compiles{i['production_sources']}production+{i['test_sources']}private core sources (135 total). Complete -Xverify:all CoreChecks exits0 with retained raw log/SHA. Python12state+16shutdown+12RCON tests pass normal/-O/-OO. All{len(ident)}Gradle-compiled core classes from the partial build match independently compiled current source; these are **not packaged** class identities.

Direct new LodReductionChecks passes all ten frozen mesh signatures (five scenes, floors-64/320, all three detail levels). An independent old-code comparison renames exact pinned2067e27 LodMesher only, compiles it outside mod sources and proves all ten baseline/current signatures equal, covering positions/colors/normals/indices/heights/bounds, partial water and opaque priority. Existing18-part exact-bounds and80000split-admission controls also pass. No baseline/probe files are added to mod source/payload. No GPU/client or current package execution is inferred.

Fresh current-production exact tick extraction with deterministic platform/time doubles passes3360boundary cases+1000multi-page cases per variant, including signed clock boundaries, stable headwater ties, unloaded/stale rejection and zero regular breadth. Under the100-tick/100microsecond callback exhaustion reproduction, current production admits **zero regular visits**; staged500microsecond reservation admits500. This is a scheduler starvation reproduction, not native water loss or actual latency. Candidate stays outside production and is not packaged. A single callback/survey/sort can consume the reservation; general fairness remains unproven.

Fresh strict harness failure controls inject JVM exit23: normal/-O/-OO each return failure and write no passing receipt. The previously committed Python-assert defect could falsely accept under optimized Python; its historical failed controls and corrected source were inspected, not concealed. Genuine method tests run normally here; expected negative controls are not engine failures. Author's full candidate targeted API compilation and other optimized genuine runs are historical evidence, not new full/native qualification.

Current source/resource/doc restoration has{s['verified_inputs']}recorded input identities, rechecked after tests; changed files retrieved from exact Git pin. Five retained pinned tool archives and private compatibility-agent hashes verified. Source excerpts and complete changed src/core trees inspected; other selected module root hashes unchanged. No new applicable AGENTS.md in selected trees.

## Failure, omitted gates and recovery

Observed full-build failure:4tasks executed, exit1, gui-themes:createMinecraftArtifacts cannot create its parent build directory. The prepared RAM output target became unavailable; a dangling link remained when Gradle checked it. The path was corrected afterward. Shared filesystem headroom fell from roughly1GB before launch to roughly300MB during checks; no attribution to a particular other job is inferred. Retry is deferred for space, not falsely claimed successful. Neither the full module/resource test graph nor a producer/exported JAR finished.

Independent source/class tests do not substitute for full Gradle. **Not run for a new audit JAR:** remaining full build/module/resource gates; current outer/nested CRC/duplicates, platform/native payload, complete Themes/notices and private-fixture exclusion; complete product compiled/packaged identities; packaged native source/quota/weather/save/reopen/shutdown; final exported SHA256/size/equality; new Library delivery. No new artifact checksum or download is invented. Planned version0.0.1-alpha.29-dev.30-audit-20261026T1533-6bd85c6 remains unbuilt.

Also unrun/unavailable: Windows/AMD8060S execution, OpenGL/Vulkan client pixels/reload/reset, named-hardware chunk/tick/frame/IO/VRAM measurements, long travel/edit/unload/multiplayer soak, cross-machine identity, complete world-water ledger and registered V8 saved topology/materialization. Linux owner smoke is not Windows acceptance. No controlled benchmark was run by this auditor. Inspected renderer CPU component medians improved3–9% in four fixtures, regressed0.8% in one; p99 worsened in four, allocation unchanged, shared host/JIT/GC limits. No FPS/whole-frame/VRAM superiority claimed; FSR/DLSS providers remain absent.

Storage recovery touched only completed auditor-owned copies/intermediates: exact earlier local successful JAR copies were checksum-matched to successful durable Library receipts before removal (durable files untouched); current last independent2067e27 local JAR retained. Old failed4a binary/extracted core and failure diagnostics remain. Older completed local project copies pruned after restoring current Git-verified inputs; their committed source/evidence and immutable tools remain. Audit cache is uniquely owned. No cleanup of builder/renderer/video resources. The abandoned RAM setup and failed attempt are retained as diagnostics. Do not require another owner to stop for build room.

Next: recover enough private output/export/native headroom, re-read main/claims, reconcile or repin if needed, rerun full current build and complete package/native gates before delivering a new auditor JAR. Then qualify staged regular-recharge reservation with actual finite source/quota/weather/restart cases; continue loaded/unloaded single-owner accounting, V8 saved integration, ecotones/features and natural cliff/LOD/hardware acceptance. Current starvation evidence is not closed by production per-column deadline checks.
'''
parent=rd('publication-parent.json');text+=f"\nMain re-read before report publication: `{parent['sha']}`. Audit source remains `{pin}`; the linked owner JAR is pinned5a2131a, the older independent JAR2067e27. No newer builder changes are silently included in a new audit artifact.\n";(e/'REVIEW.md').write_text(text)
prefix='docs/engine-hourly-audits/2026-10-26T1533-6bd85c6/';items=[]
for file in sorted(e.iterdir()):
 if file.is_file() and file.suffix in ['.json','.txt','.md']:
  content=file.read_text()
  if file.suffix=='.json':content=json.dumps(json.loads(content),separators=(',',':'))+'\n'
  items.append({'path':prefix+file.name,'mode':'100644','type':'blob','content':content})
for name in ['init.py','run_build.py','run_checks.py','finish_gates.py','validate_package.py','packaged_regressions.py','post_native_verify.py','rolling_package.py','source-excerpts.py','native_smoke.py','aquifer_native_smoke.py','native-memory.init.gradle','strict-compile.init.gradle','audit-version.gradle','report_partial.py','probes/probe.py','probes/reproduce-admission.py','probes/failure_control.py','probes/CompareLod.java']:
 file=r/name;items.append({'path':prefix+'audit-tools/'+name,'mode':'100644','type':'blob','content':file.read_text()})
groups=[];group=[];size=0
for x in items:
 n=len(json.dumps(x))
 if group and size+n>55000:groups.append(group);group=[];size=0
 group.append(x);size+=n
if group:groups.append(group)
for idx,group in enumerate(groups):(r/('publication-'+str(idx)+'.json')).write_text(json.dumps(group,separators=(',',':')))
summary={'files':len(items),'groups':len(groups),'bytes':sum(len(x['content']) for x in items)};(r/'publication-summary.json').write_text(json.dumps(summary));print(json.dumps(summary))
