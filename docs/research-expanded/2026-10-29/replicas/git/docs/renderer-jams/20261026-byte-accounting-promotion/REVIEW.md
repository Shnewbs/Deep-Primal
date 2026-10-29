# Expanded replica — docs/renderer-jams/20261026-byte-accounting-promotion/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-byte-accounting-promotion/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261026-byte-accounting-promotion/REVIEW.md |
| Preserved source SHA-256 | `cce71ac6e747bc9981af3d5d119c122a03d85f097558b6ea21f90215bfd07541` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Promotes the exact byte-accounting candidate after complete source/build and packaging checks. The record distinguishes the final qualified artifact from preliminary contaminated or mutated outputs and reports near-zero query allocations without claiming reduced geometry or GPU memory. Its 35-task build and four headless sessions qualify that historical scope, not later edits.

## Research expansion — gaps and details to deepen

- The valuable addition is a reusable evidence chain connecting source freeze, compiled classes, nested artifacts, and native execution. The historical successful build still does not establish current owner-GPU performance after subsequent renderer changes.

## Research expansion — proposed contracts

- Add a promotion receipt with source/test hashes, build configuration, exact output identities, rejected preliminary artifacts, and gates repeated after mutation. Keep query allocation, payload size, retained memory, and frame latency as separate measurements.

## Research expansion — acceptance checks

- Verify a regenerated artifact matches the frozen source and packaging manifest, rerun actual-class accounting checks, and collect matched target-native frame/allocation captures before presenting the promotion as an FPS improvement.

## Research expansion — source anchors

- Renderer jam 20261026-byte-accounting-promotion — self-audit
- Delivered implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261026-byte-accounting-promotion — self-audit

Final reviewed/pushed runtime **c5abe457c4c7b36a11307128486c240e0b40d508**; initial main **4a8b0fb52e7473a816655096c42f089dd340e735**. Final build execution snapshot **dba66620bc6af6282f721f2cc2ab49085d572657**. Development version **0.0.1-alpha.29-dev.31-renderer.6** (unique build override; Themes stays0.1.0-alpha.8). This is the renderer author's read-only self-audit, not an independent external audit or a whole-engine regrade. All four published source files were read back exactly. Product code stayed frozen after the final packaging correction; no code changes during scored review.

## Delivered implementation

Promoted the exact previously staged LodMesher candidate, SHA2564c8d6b60a71c1021d245df622105b1d1a8a4f0bd0dc58fd2a205f83c9be85eec. Levels.bytes now sums each detail without a Stream.of/flatMap/mapToLong pipeline. RandomAccess lists use indexed traversal; sequential lists retain linear iterator traversal. Counts are recomputed, so mutable-list callers are not given stale cached admission totals. Returned meshes, GPU footprint formula, bounds, geometry, colors/normals/indices, water heights and ownership remain unchanged. Sequential/custom lists are not promised zero allocation.

The new LodByteAccountingChecks is registered in ordinary CoreChecks, not just a private build init. It covers five actual mesher scenes, immutable/ArrayList/sequential/empty and mixed-detail lists, clear/add mutation visibility and500 randomized list cases against an arithmetic oracle using actual color/index lengths. A sequential test list throws on indexed get. It also runs the frozen geometry/bounds/scratch/max-height/cancellation suite.

Pre-delivery inspection found a tracked hidden temporary image entering the nested Themes module, adding7,340,032 uncompressed bytes. Added hidden/rsync-temporary exclusion to Themes processResources and jar, matching the outer bundle's existing policy. The source file is preserved; no authored art, resource ID, notices, UI design or texture is edited. Final normal Themes entry set and every uncompressed entry byte match the prior qualified module exactly. The stronger recursive archive gate rejects hidden entries in all seven archives.

Actual source changes:
- [LodMesher.java L16](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16): bounded list traversal for byte admission.
- [CoreChecks.java L17](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L17): normal-suite registration.
- [LodByteAccountingChecks.java L19](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodByteAccountingChecks.java#L19): arithmetic/list/geometry/ownership regressions.
- [gui-themes/build.gradle L36](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/gui-themes/build.gradle#L36): resource filter; jar filter atL47.

No render/simulation distance maximum, native tracking/networking, terrain generation, finite-water simulation, materials, transparency pass, shaders, vendor SDK or frame-generation change. Existing native125 and explored LOD192 default/512 maximum remain.

## Fresh matched allocation evidence — actual product core

PackagedByteChecks asserts LodMesher's code source is the extracted nested product JAR. The production JAR precedes all private fixture directories on the classpath. Baseline is exact initial-main LodMesher with only class/constructor renaming. Both variants run the real mesher on identical immutable inputs and preserve geometry signatures.

Java25.0.4.1, -Xmx512m -Xverify:all; Linux AMD EPYC9V74 virtual host,9 exposed CPUs. Per fixture:10,000 warm-up queries per variant, then five alternating pairs of20,000 queries per variant. ThreadMXBean records calling-thread allocated bytes; input/mesh creation excluded and results consumed through a volatile sink. No exclusive host reservation or elapsed-time benchmark.

| Fixture | Baseline B /100,000 queries | Candidate B /100,000 queries | Mesh GPU bytes, unchanged |
|---|---:|---:|---:|
| 0 | 59,650,184 | 120 | 1,656 |
| 1 | 56,800,000 | 0 | 108,192 |
| 2 | 56,000,000 | 0 | 57,776 |
| 3 | 56,000,000 | 0 | 81,880 |
| 4 | 58,240,000 | 0 | 4,140 |

Observed baseline560–597B/query, candidate0–0.0012B/query in this warmed RandomAccess corpus. The first candidate batch has120 total bytes; do not round it into an exact universal zero guarantee. Prior staged560–584/616–624 results are historical, not this run's measurements. This demonstrates reduced query allocation only: no whole-mesh allocation, returned/resident RAM, GPU bytes, VRAM, upload, frame-time, FPS or usable-distance gain. Earlier jam4's30–49% scratch result is carried, not rerun as an allocation experiment here.

Raw data: packaged-byte-checks.txt and packaged-check-command.json; private baseline/check/wrapper sources retained under tools/probe-src.

## Final qualification and failure discipline

Final full Java25/Gradle9.2.1 build passes with --rerun-tasks: **35 executed tasks**,305.704s. Includes complete core/cache/material/module checks and visualValidation compilation. The final client console captured only startup lines; authoritative final-daemon.txt records BUILD SUCCESSFUL in5m5s,35/35 executed and the complete core markers. build-result.json confirms producer exit0. Normal Gradle deprecation/compiler warnings remain. The final daemon log also retains a shutdown registry/file-lock-listener warning after successful task completion. Producer exit0, complete task/core markers and all later archive/native identity gates are required; this cleanup warning is not relabeled as a failed core check or claimed resolved.

Fresh independent javac -g: **139 sources** (75 production core +64 private fixtures); full -Xverify:all CoreChecks passes. New byte checks pass against direct and actual extracted-product classes. Ten frozen signatures,18-part bounds,25 later serial builds,64 parallel jobs,4096height and cancellation checks pass. No final test failures.

Seven outer/nested CRC/no-duplicate gates pass. **592** compiled product/package comparisons, **146** independent core/product comparisons and **224** excluded private classes pass. Two Windows AMD64PE DLLs, no Linux/macOS Vulkan payload, OpenGL fallback classes, exact module/Vulkan notices, **2,508** Themes resource hashes,43 palettes,11 approved ground images and148 generated soils pass. Development asset-generator classes are excluded. Final Themes normal resources/classes/metadata are byte-identical to the prior qualified module.

Four final packaged Linux dedicated-server sessions pass: default fresh/reopen with saved channel2; opt-in fresh/reopen with channel5 retained after disabling the creation toggle. Seed20261020, view/simulation2, private ports26231/26232 and fresh unique world directories. Require terrain/deposit baseline replay, actual loaded class identities, soil/source/allowance/codec markers, acknowledged save-all flush, failure precedence, ordered shutdown and exit0. All six nested modules are selected from the actual exported root in every final session. This bounded regression is not an independent ten-session engine audit or client rendering proof.

Preliminary evidence is explicitly invalidated:
1. Initial source6a948fd full build passed, but the archive contained the hidden temporary image.
2. Packaging was changed while a preliminary own smoke run was still active. Its final byte-identity guard correctly rejected the changed artifact; the preliminary harness exited1. That run is not counted as final qualification and no intermediate binary was delivered.
3. After source freeze, repeated the full forced build, independent compilation/core suite, recursive package/class/resource checks, actual-product allocation/geometry checks and native matrix. A fresh native setup first stopped with FileExistsError before launching a server; first-final-native-setup-failure.txt is retained. Switched the private harness to unique tempfile world directories, then all four final sessions completed.
All final consumer processes completed before byte verification/save. No product edits after freeze. Current SHA still matches producer/export after all native consumers and successful saving.

## Six equally weighted renderer scores

The broader category numbers are explicitly **carried unchanged** from the previous renderer3.67 review, with fresh scoped implementation/check evidence below. Shipping this narrow optimization and correcting packaging do not establish broad GPU/coverage/provider acceptance.

| Category | /10 | Pinned evidence and missing gates |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259); prior bounded enumeration result carried. New exact mesh regressions in packaged-byte-checks.txt preserve sampled geometry only. No live discovery readiness/cache-race/handoff, unseen delivery or higher useful-distance acceptance. |
| Mesh generation/reuse/scheduling | 6 | [Byte traversal L16](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16), [oracle/list checks L19](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodByteAccountingChecks.java#L19), fresh actual-product query allocations above. Existing build-local scratch/geometry ownership retained. No across-edit reuse, native meshoptimizer shipping or measured scheduling/frame gain. |
| GPU culling/submission/memory | 4 | [Culling L477](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477), [ordinary indexed draw L121](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L121). No GPU code/layout improvement, real multi-draw indirect, fence-safe arena or VRAM measurement. CPU query allocation is not GPU memory savings. |
| Actual FSR/DLSS implementation | 0 | [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5). Providers/dispatch remain absent; requests fall back to native. Linear spatial scaling and frame generation do not count as FSR/DLSS integration. No SDK/license/hardware acceptance. |
| Visual/backend/settings compatibility | 4 | [Nested resource filter L36](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/gui-themes/build.gradle#L36), package.json/post-native-identity.json, exact normal Themes bytes and fresh geometry/water-related core regressions. No UI/art/settings redesign. OpenGL/Vulkan client pixels/reload/resize/world-switch and transparent-water ordering remain unrun. |
| Validation and measured performance | 4 | [Normal check registration L17](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L17), final-daemon.txt, independent-core-result.json, packaged-byte-checks.txt, package.json and final native results. Full build/product provenance/four server sessions pass; named-device camera/travel/edit/full-frame/GPU/RAM/VRAM baselines still absent. |

**Mean22/6=3.67/10; previous3.67; change0.00.** No whole-engine external grade change, independent external audit claim or target>8.5 acceptance.

## Source reconstruction, consumed audits and integration

Storage recovered, but the earlier local checkout/cache/toolchain had been removed. Fresh Git clone was unavailable without credentials. Reconstructed an isolated checkout by copying only **862 exact Git-blob-verified tracked build inputs** from retained read-only source, never another writer's mutable output/cache. Root/module source trees, latest main, engine/water state, claims, recent commits and measurement records were read; governing architecture/renderer roadmap hashes unchanged and read; no applicable AGENTS.md found. Required art validation/profile JSON fetched at exact source pin. Pinned JDK25/JDK21/Gradle/NeoForm/ASM archives downloaded into a private toolchain and size/SHA verified; the private fallback agent was built/tested from verified source. No borrowed mutable cache. Themes source archive was inspected but added0files; Git files were not overwritten.

Consumed prior byte-accounting and candidate-runner reviews: maintain mutable/sequential semantics, true product code-source checks and no full-JAR claim from standalone probes. This run closes their full build/package/native delivery gate for this increment. Consumed independent2232-4a8b0fb audit: carried external5.0, unchanged production/staged hydrology distinction, current native/package checks and explicit real-client limits. Its results are historical context, not fresh tests by this renderer jam. General builder tool recovery and water scopes were preserved; no staged hydrology promotion or broad work-state edit.

Runtime reparented onto fresh080bfb69 to preserve concurrent documentation plus two uncompiled restoration-tool changes. All production module/src/root build trees are identical to tested dba6662; the actually used proxy/server/compatibility tool blobs match too. Only inherited docs and unused restore-tool/test code differ; these are not compiled/packaged or called during this qualification. rebase-input-identity.json records exact proof. No unnecessary fresh full build is claimed for that reparenting. Four pushed file contents were read back exactly after guarded nonforce expected-parent integration.

Scoped claim initially covered the three renderer/core-check files and this dedicated path; it was explicitly extended to gui-themes/build.gradle before that packaging correction, after confirming no competing active scope. General builder remained functional-only/nonexclusive and owned restoration tools; water was checkpointed. No other automation or feature state changed; no agents/messages/process kills/pauses/force pushes/shared mutable build outputs.

## Artifact and remaining gates

[Download renderer.6 development testing JAR](https://chatgpt.com/library?file_id=libfile_37bc578622e48191999c4426e7a21247&account_id=personal).
**125,296,494 bytes**, SHA256 `d3bb2d886010e5b621a3d6907e07fee4d701dea0deeab4fa9bce4378c4412095`. Final source c5abe457c4c7b36a11307128486c240e0b40d508; build-execution source dba6662 with proven identical runtime/build inputs. Fresh unique renderer.6 producer; saved successfully, identity metadata applied, post-save producer/export bytes equal. No earlier/superseded binary relabeled. No release-write/upload tool or gh executable available; no GitHub Release, stable or CurseForge publication.

Unrun: physical /dev/dri GPU, Windows/AMD8060S, actual OpenGL/Vulkan client scenes, backend pixels, resource reload/resize/world switch/client save-reopen/shutdown, cache/edit/travel/native-handoff races, silhouettes/signed seams/lighting/cutouts/transparent-water temporal ordering; long multiplayer/travel/edit/unload soak. Linux dedicated server success is not Windows or GPU acceptance.

No new camera/resolution/API/warm game-scene baseline; host.json marks these null. Full-frame CPU/GPU p50/p95/p99, cull/rebuild/upload/descriptor/draw counts, resident/pending/cache RAM and GPU memory remain incomplete. Do not reuse raw diagnostic timing printed by broad core tests as a controlled renderer benchmark.

Next priority: reproduce live discovery/capture/upload readiness and native handoff under travel/edit/cache pressure on a GPU client, capture complete cold/warm camera/travel/edit baselines, then continue bounded reuse/upload and hierarchical visibility/refinement/coverage. An authoritative unexplored-terrain delivery contract and generator/network prerequisites remain open. Independent persisted requested/effective full/vertical/LOD/simulation budgets must precede any higher native cap; merely raising maxima earns no credit. Real official FSR/DLSS provider/input/resource-lifetime/licensing/hardware integration remains open; frame generation later and separate.

Own claim released at the final receipt boundary. Dedicated renderer work remains enabled; finite runs do not imply uninterrupted execution.


<!-- END PRESERVED SOURCE -->
