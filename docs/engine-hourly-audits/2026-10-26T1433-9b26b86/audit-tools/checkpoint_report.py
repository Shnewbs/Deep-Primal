from pathlib import Path
import json,hashlib
r=Path(__file__).resolve().parent;e=r/'evidence';p=r/'project';pin='9b26b8619b3a44c1542cc10f40a11658c4cd3cf0';old=json.loads((e/'last-qualified-delivery.json').read_text());parent=json.loads((e/'publication-parent.json').read_text())
s=json.loads((e/'source-identity.json').read_text())
for f in s['files']:assert hashlib.sha256((p/f['path']).read_bytes()).hexdigest()==f['sha256'],f['path']
rows=[('Terrain',5,'src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java',34,'V1–V7 registered; V8 density prototypes do not provide accepted native saved topology.'),('Biomes',4,'engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java',76,'Sharp climate/height thresholds persist; no weighted ecotone surface/vegetation acceptance.'),('Hydrology/caves',6,'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterAccount.java',73,'Prior precipitation/receiver repairs unchanged. Full loaded/unloaded ownership, edits and broad cave/water acceptance remain open.'),('Features/structures',4,'src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java',115,'Deterministic deposit features exist; generic structure spacing/exclusion pipeline remains incomplete.'),('Performance/threading',3,'src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java',423,'Native fill remains synchronous before completedFuture; TerrainEngine.java228–250 builds regions under synchronization. No current performance run.'),('Determinism/stability',7,'src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java',260,'Prior scoped persistence passes remain historical. Current broad replay/soak/cross-machine proof absent.'),('Architecture',6,'engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java',22,'Bounded/coalesced lifecycle APIs exist; native generator coupling and world water ownership remain incomplete.'),('Extensibility',4,'src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java',54,'Saved deposit/options codec validates definitions; full generation-stage/profile authoring and extension acceptance remains incomplete.'),('Tests/docs',7,'src/coreChecks/java/com/shnewbs/deepprimal/core/RollingTimingChecks.java',6,'Existing mathematical/native regression infrastructure is unchanged. No new executable tests run in this deferred audit.'),('Polish/tooling',4,'tools/server_smoke.py',17,'Strict save/shutdown/error precedence tooling retained. No current platform, natural-scene, UI or authoring acceptance.')]
g={'source_sha':pin,'total':50,'average':5.0,'tier':'2/5','fresh_complete_regrade':False,'scores':{x[0]:x[1] for x in rows},'status':'all scores carried; static preflight only; full build/native gates deferred','threshold_demonstrated':False};(e/'grade.json').write_text(json.dumps(g,indent=2))
t=f'''# Independent engine audit checkpoint — 9b26b86

**Current build incomplete: audit compilation/native/benchmarks deferred under an active CPU microbenchmark claim. No new JAR exists and none is presented as newly successful.**

[Last qualified historical Windows development JAR — source2067e27]({old['library_url']}). Its exact source is2067e273e85fb4f80530c86a7c238106cbbb4cdf, version0.0.1-alpha.29-dev.29-audit-20261026T1339-2067e27, size{old['size']}bytes, SHA256 `{old['sha256']}`. This is the prior audit's artifact, not a build from this audit pin. It was already saved successfully; no duplicate upload/old-binary relabel occurs.

## Current pin and timing decision

Pinned main at start: `{pin}`. Final pre-publication main read: `{parent['sha']}`. No production/test/build-source changes since prior auditor pin2067e27: all nine selected build-module root Git tree hashes and top-level build inputs are unchanged. Commits add audit evidence, root work-state updates and the renderer's work claim. Its new LodMesher/LodReductionChecks work is not committed production at the pin and earns no feature/performance credit. No staged R57/R60/R61/R62 or hydrology deadline/wrap candidate is promoted.

Root builder1425 is active on an explicitly nonexclusive functional build in builder0731 resources. Separate renderer claim renderer-20261026-lod-reduction is active, expires2026-10-26T15:12:02.153Z and says "Isolated CPU component microbenchmark only; no frame/GPU benchmark reservation". This is not a claim of an exclusive frame/GPU reservation, nor proof that a timed JVM is running at every observation. It does identify active CPU measurement work. Independent mutable checkouts/cache/output are available, but independent physical CPU isolation cannot be established on the shared host. Auditor conservatively defers only its own heavy work to avoid contaminating component timings, as the owner requested. Neither builder nor renderer is paused, interrupted, killed, reset, edited or required to stop. This is a temporary timing/resource deferral, not the superseded foreground report-only policy or a permanent access blocker.

Current snapshot/restoration/output are preserved for continuation. Planned unique version0.0.1-alpha.29-dev.29-audit-20261026T1433-9b26b86 is **not built**. Independent auditor receipt is checkpointed and does not hold/take over a builder lease or impose a stop gate. Re-read main and claims before resuming; if source changes, repin/reconcile and never overlay unaccepted staging. No background build is left running and no continuous execution between runs is implied.

## Ten equal categories — carried baseline

**50/100, mean5.0/10, Tier2/5. All ten scores carried; not a fresh complete regrade.** Original rubric is docs/engine-review-2026-10-24/REVIEW.md. Static source inspection does not establish current runtime acceptance. Last full accepted baseline remains50/100. The strictly-greater-than8.5 target is not demonstrated, so the existing auditor remains enabled. The renderer author's separate rubric is not substituted.

| Category | /10 | Status | Pinned source evidence / remaining gate |
|---|---:|---|---|
'''
for name,score,path,line,why in rows:t+=f'| {name} | {score} | carried | [L{line}](https://github.com/Shnewbs/Deep-Primal/blob/{pin}/{path}#L{line}): {why} |\n'
t+=f'''
## Passed now; explicitly not run

Static preflight only: main pin and all required policies/work-state read; work-state validator passes;{s['verified_inputs']}restored source/resource/doc inputs verified against recorded identities, current unchanged build-module trees and freshly retrieved changed work-state files;74core production/60private core sources present; five pinned retained restoration archives and private compatibility-agent hashes verified. Tools remain outside mod payload. Source excerpts inspect registration, heightfield/native fill, climate thresholds, caves, profile codec, bounded pipeline and module structure. Water foreground claim is checkpointed. All prior exported JARs, receipts and immutable toolchains retained.

To recover headroom, removed only five older completed auditor-owned local project copies. Their committed source remains in Git, exact input identities and reports remain retained, and the last audit checkout supplies current Git-verified restoration bytes. No other role's checkout/cache/process changed. Completed prior auditor's cache moved into this audit's unique path, with no shared mutable producer. Source/approved assets match pinned module blobs; frozen V1–V7, saved profiles, finite quantities, player edits and Deep Primal identity unchanged.

Latest completed evidence inspected: prior [2067e27 audit](https://github.com/Shnewbs/Deep-Primal/blob/de95ee27dece40a76b15dd1e3a30a657eca50997/docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md) and the committed survey/clock/precipitation/renderer receipts referenced there. Prior35-task build,590class/144core/218fixture identities, recursive CRC/resource checks and ten Linux native sessions belong to that historical JAR. They were **not rerun now**. Likewise prior survey38/158case, tick216/1266case, arithmetic1200000 and targeted staged API compilation are historical, not new current test executions. No credit for in-progress functional work or unpublished microbenchmark results.

Unfinished essential gates, in order: full Java25/Gradle compilation/build/tests; independent full Java25 core suite; outer/nested CRC/duplicates/platform payload/resource/notices/private-fixture checks; source/compiled/packaged identities; ten appropriate exported-package native fresh/reopen/restart/save-flush/shutdown sessions; final producer/export byte equality/SHA256/size; save and link a newly qualified JAR. None of these gates is marked failed or passed in this audit; status is deferred/unrun. Current fresh JAR cannot be claimed.

Also unrun/unavailable: Windows/AMD8060S native execution, actual OpenGL/Vulkan client/GPU/pixels, controlled latency/frame/IO/VRAM benchmarks, extended travel/edit/unload/multiplayer soak, cross-machine replay, complete water ledger and registered V8 topology/materialization. Historical Linux smoke is not Windows acceptance. No GitHub Release/CurseForge/stable promotion.

## Next priorities

First resume this auditor's fresh build/package/native/export gates when active CPU measurement work ends, without stopping any owner. Builder's next implementation priorities remain native qualification of staged stream per-column/wrap deadlines with finite source quotas and restart; loaded/unloaded single-owner water accounting/edits/catch-up; registered V8 saved integration; ecotones/features; natural cliff/LOD temporal coverage and named-hardware tail/frame datasets. Production still lacks the per-column stream deadline check and general native V8 registration. No new implementation or fresh whole-engine grade is claimed here.
'''
(e/'REVIEW.md').write_text(t)
prefix='docs/engine-hourly-audits/2026-10-26T1433-9b26b86/';items=[]
for f in sorted(e.iterdir()):
 if f.is_file() and f.suffix in ['.json','.txt','.md']:
  content=f.read_text()
  if f.suffix=='.json':content=json.dumps(json.loads(content),separators=(',',':'))+'\n'
  items.append({'path':prefix+f.name,'mode':'100644','type':'blob','content':content})
for name in ['init.py','run_build.py','run_checks.py','validate_package.py','packaged_regressions.py','finish_gates.py','post_native_verify.py','source-excerpts.py','native_smoke.py','aquifer_native_smoke.py','native-memory.init.gradle','strict-compile.init.gradle','audit-version.gradle','rolling_package.py','precipitation-native/run_native.py','receiver-native/run_native.py','checkpoint_report.py']:
 f=r/name;items.append({'path':prefix+'audit-tools/'+name,'mode':'100644','type':'blob','content':f.read_text()})
groups=[];current=[];size=0
for x in items:
 n=len(json.dumps(x))
 if size+n>50000 and current:groups.append(current);current=[];size=0
 current.append(x);size+=n
if current:groups.append(current)
for i,group in enumerate(groups):(r/('publication-'+str(i)+'.json')).write_text(json.dumps(group,separators=(',',':')))
(r/'publication-summary.json').write_text(json.dumps({'files':len(items),'groups':len(groups),'bytes':sum(len(x['content']) for x in items)}));print(json.dumps({'files':len(items),'groups':len(groups),'bytes':sum(len(x['content']) for x in items)}))
