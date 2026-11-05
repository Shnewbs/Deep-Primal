# Expanded replica — docs/renderer-jams/20261026-candidate-runner/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-candidate-runner/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261026-candidate-runner/REVIEW.md |
| Preserved source SHA-256 | `0f799babf8929f4ad644eb13c2115c50cc672132ef85713de76af02e72dd2601` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Introduces a reproducible source-candidate runner using pinned Git blobs, explicit hashes, isolated temporary outputs, and success only after compilation, process exit, and expected marker checks. Negative wrong-hash cases are retained; runtime source and distributable artifacts are unchanged. The self-review validates the harness rather than a new renderer feature.

## Research expansion — gaps and details to deepen

- The runner proves identity and focused execution, but needs a complete environment and dependency receipt to make results comparable across compiler versions and classpaths. A passing marker cannot stand in for product integration or hardware exercise.

## Research expansion — proposed contracts

- Record JDK/compiler arguments, classpath artifact hashes, fixture entry point, enabled assertions, exit status, and source inspection scope. Preserve fail-closed result initialization and path containment; identify stand-ins separately from actual production classes.

## Research expansion — acceptance checks

- Reject changed source bytes, missing classes, disabled required assertions, misleading output markers, and compile/runtime failures. Reproduce a passing run in a fresh isolated output and require a separate exact-artifact native gate for rendering claims.

## Research expansion — source anchors

- Renderer jam 20261026-candidate-runner — self-audit
- Concrete implementation

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `tools/renderer_candidate_checks.py#L1` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-candidate-runner/tools/renderer_candidate_checks.py#L1) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261026-candidate-runner — self-audit

Reviewed production parent `38a140431881cdb49a4287e48fc84a6d904e7e9b`, dev.31. Runtime files/version unchanged. Implemented tool: [renderer_candidate_checks.py](tools/renderer_candidate_checks.py#L1), SHA256 `8218955d1677aaf5467dcf944068d92a60684c631400d71cebfa839a49242c70`. This is the renderer author's read-only self-audit after code completion, not an independent external audit. No tool/candidate code changed during scored review. Exact candidate stays `4c8d6b60a71c1021d245df622105b1d1a8a4f0bd0dc58fd2a205f83c9be85eec`, previously retained in the byte-accounting jam. **No new mod JAR or runtime promotion.**

## Concrete implementation

Added a reusable Python runner for the staged byte-accounting candidate. It verifies Git blob hashes of all75 production core inputs and three fixture sources, requires explicit SHA256 pins for candidate/private check, reconstructs the baseline by class-name-only replacement, and creates a unique tempfile output directory below a caller-selected root. Candidate, baseline and private check are copied into that directory; javac compiles the complete core with the candidate substituted. No cached/product classes on classpath, Gradle daemon, shared output or mutation of source/caches.

The runner requires successful compile and Java exit plus the acceptance marker, checks input identities again after execution, retains exact commands/logs/pass status, and explicitly records full_gradle_build=false, packaged_artifact=false and gpu_validation=false. Inputs escaping the selected checkout root are rejected. Failure retains pass=false rather than success. No automatic process-killing or cleanup of another output. New code is confined to this dedicated renderer docs/tools path.

Three deliberate negative controls pass: wrong candidate SHA, wrong private-check SHA and wrong production Git blob SHA all reject before a work directory is created. The actual caller/check commands are in command.json/result.json. First orchestration attempt failed before compilation because the assembled manifest omitted its tree; initial-setup-stderr.txt is retained. The manifest was repaired from the saved Git tree, and a fresh complete successful run followed. This setup error is not a product pass.

## Fresh execution

Java25.0.4.1, -Xmx512m -Xverify:all, Linux EPYC9V74. Fresh75-source core compile, three freshly Git-pinned fixtures and the new private check pass. Five real mesher scenes compare baseline/candidate geometry and arithmetic; immutable/ArrayList/sequential/empty lists, mutation visibility, ten frozen signed-floor signatures,18 exact part bounds,25 retained serial builds,64 parallel jobs,4096height and cancellation pass. checks.txt contains the complete markers.

Existing ByteAccountingChecks was rerun by the new tool, not newly rewritten. Per fixture:10,000 warm-up calls per variant; five alternating pairs of20,000 calls each. Input/mesh creation excluded; ThreadMXBean calling-thread allocated bytes only; no elapsed benchmark or exclusive host reservation.

Fresh output:
```
ALLOCATION fixture=0 queries=100000 baselineBytes=61600216 candidateBytes=0 meshBytes=1656
ALLOCATION fixture=1 queries=100000 baselineBytes=62400000 candidateBytes=0 meshBytes=108192
ALLOCATION fixture=2 queries=100000 baselineBytes=61600000 candidateBytes=0 meshBytes=57776
ALLOCATION fixture=3 queries=100000 baselineBytes=61600000 candidateBytes=0 meshBytes=81880
ALLOCATION fixture=4 queries=100000 baselineBytes=61600000 candidateBytes=0 meshBytes=4140
```

Thus this execution measured about616–624B/query before and zero on warmed RandomAccess output after. The earlier560–584B result is historical and is not asserted as a constant; JVM execution affects observed totals. Sequential fallback can allocate an iterator. This is repeated CPU budget-query allocation in a staged candidate, not complete mesh build allocation, resident/peak RAM, GPU data, VRAM, frame time or FPS. No deployed performance credit.

## Consumed source, audits and ownership

Read current main, engine/water states, latest renderer candidate review/backlog/claim and recent root/commit records. Core tree remains6b217a520d2707f6bda78d66440e38d8c0018454; architecture3fd014b and renderer diagnostics8323501 are unchanged from the previously read governing documents and were rechecked before integration. No root AGENTS.md exists; inspected core inventory has no new applicable agent file. Existing builder1905 and foreground water are checkpointed, performance measurement released/nonexclusive. No other work-state, schedule, cache/source/process changed; no subagents.

Consumed previous jam's exact source pins and warning: standalone core/allocation checks cannot qualify a JAR. Consumed latest relevant independent audit [1934-e528d06](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/docs/engine-hourly-audits/2026-10-26T1934-e528d06/REVIEW.md): its fresh staged lifecycle/index checks are limited; full Gradle/native capacity gate persists; production and renderer candidate remain unchanged. Its complete engine5.0 grade and earlier dev31 artifact are historical, not rerun/republished here.

Claim covers only docs/renderer-jams/20261026-candidate-runner. Fresh main/claim checks and expected-parent nonforce update preserve concurrent work. Current engine hydrology candidates were neither incorporated nor edited. Toolchain reused read-only; compilation outputs are a unique memory-backed directory, not another job's output.

## Renderer scores — six equal categories

All production scores are explicitly carried from the byte-accounting/jam5 review. Fresh focused tooling does not justify a higher complete renderer grade.

| Category | /10 | Pinned source/test evidence and limits |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259), historical enumeration proof only. No fresh live visible coverage/handoff/travel/cache-race acceptance. |
| Mesh generation/reuse/scheduling | 6 | [Scratch L45](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45); checks.txt fresh exact geometry/ownership regressions. The byte-accounting candidate remains staged and adds no shipped implementation credit. |
| GPU culling/submission/memory | 4 | [Culling L477](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477); no new GPU/submission/buffer/descriptor/VRAM proof, no indirect batching credit. |
| Actual FSR/DLSS implementation | 0 | [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5); providers/dispatch still absent. Spatial linear scaling and frame generation are not counted. |
| Visual/backend/settings compatibility | 4 | [Mesh byte formula L20](https://github.com/Shnewbs/Deep-Primal/blob/38a140431881cdb49a4287e48fc84a6d904e7e9b/engine-core/src/main/java/com/shnewbs/deepprimal/core/HorizonMesh.java#L20), fresh geometry tests in checks.txt. Backend pixels/settings/reload/resize/world-switch/water ordering remain unrun. |
| Validation and measured performance | 4 | [New runner L1](tools/renderer_candidate_checks.py#L1), result.json, manifest.json, checks.txt and negative-controls.json. Fresh isolated compile/provenance/negative controls and narrow allocation only; full mod/platform/frame gates open. |

**Mean22/6=3.67/10; previous3.67; delta0.00.** No external whole-engine regrade and no target>8.5 claim.

## Capacity, artifact and unrun gates

Overlay remained0 bytes free. This run reclaimed only renderer5's completed reproducible root/module build directories and native smoke/cache outputs after checking the prior successful save receipt and exact producer SHA. Source, approved art, raw receipts, staged candidate and test evidence were preserved. No other role's directories/processes were touched. Observed free space remained0 after removal; no cause or recovered-capacity claim is invented.

Memory-backed space allowed small scoped javac/tests, not a full private project/cache/Windows Vulkan bundle/Themes/native workflow. **Full Java25/Gradle/module build not started due capacity. No new JAR, no source/compiled/packaged identity or archive/native/payload acceptance, no Library upload, no release publication.** Older binaries were not renamed/re-uploaded. Standalone core success is never reported as mod build success.

Not run: packaged native source/capture/upload/lifecycle/save/reopen/shutdown; actual OpenGL/Vulkan GPU pixels, client reload/resize/world switch/native handoff/cache races/negative seams/silhouette/lighting/cutout/transparent-water ordering; Windows or AMD8060S; named-device camera/travel/edit scene frame p50/p95/p99, CPU/GPU cull/rebuild/upload/allocation, draw/descriptor counts and resident/pending/cache RAM/VRAM; multiplayer/long soak. Physical /dev/dri is unavailable. No API/camera/resolution/seed/warm game-scene baseline exists in this run.

## Next repair

Restore stable private full-build headroom, re-read main/claims, apply the exact staged candidate and register the private correctness check in normal qualification. Use the retained runner to reproduce scoped gates, then run full Java25/Gradle/modules, package identities/CRCs/payload/notices/fixture exclusion, unique development JAR, native lifecycle and applicable real client/backend gates before promotion/delivery.

Then prioritize camera/travel/edit and live discovery/handoff baselines, bounded mesh/upload reuse and hierarchical coverage/invalidation. Unexplored-terrain delivery still requires an authoritative renderer contract plus coordinated generator/network prerequisites. Full-detail125/LOD192 default512 max remain unchanged; no simulation coupling or slider-only credit. Actual vendor SDK/licensing/provider/render-input/resource-lifetime/hardware integration remains open; frame generation later.

Own claim released at actual receipt boundary; dedicated renderer task remains enabled while this temporary capacity gate is open.


<!-- END PRESERVED SOURCE -->
