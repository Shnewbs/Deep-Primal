# Expanded replica — docs/renderer-jams/20261026-delivered-discovery/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-delivered-discovery/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261026-delivered-discovery/REVIEW.md |
| Preserved source SHA-256 | `0d628c676ed67a55cd7138f013ba512a8f7774e21e20c9ae64b2c044eb2e4257` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Expands delivered-chunk rediscovery beyond the former 32-chunk clipping using a Chebyshev-ring cursor over the effective native radius, bounded at 128 coordinate visits per tick and a 512-entry admission guard. Nonloading checks and native revision precedence are preserved. The minimum enumeration-turn arithmetic is not a wall-clock or rendered-distance guarantee.

## Research expansion — gaps and details to deepen

- Cursor enumeration is not visible readiness. The fixed-center limitation at this historical pin was later addressed by retarget work, but fairness during continuous movement and end-to-end coverage age still need integrated evidence.

## Research expansion — proposed contracts

- Add separate discovered, captured, meshed, GPU-published, and visible counters keyed by world generation and source revision. Link the retarget successor and measure oldest eligible work without treating unavailable authoritative chunks as scheduling failures.

## Research expansion — acceptance checks

- At a 125-chunk radius, verify all 63,001 coordinates are enumerated under bounded turns, then verify actual visible coverage on the native client. Exercise movement, detach, and stale disk results without loading extra chunks.

## Research expansion — source anchors

- Renderer jam 20261026-delivered-discovery — self-audit
- Implemented increment

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261026-delivered-discovery — self-audit

Final reviewed and pushed runtime: `02eb0d8f3739cf036717abf82d03dc7e2a325081`. Initial main: `b198804f19db960fba6efc52a76b18c603d50601`; scoped claim: `31798ec217b1d1f76d1cf014466c606c853846c1`. Build execution source: `b8e69b632d6b45be94b08143d25186f3eee0e942`. Development version: **0.0.1-alpha.29-dev.30-renderer.5**. This is the renderer jam author's read-only self-audit of completed code, not an independent external audit or a whole-engine regrade. Published four-file readback equals reviewed contents. No code changed during the scored review.

## Implemented increment

The actual parent attached the LOD renderer by synchronously checking a square clipped to 32 chunks, even when effective native distance exceeded 32. At radius 125 that checked only 4,225 positions. The new scalar DeliveredChunkScan enumerates the initial effective native radius, bounded by the existing 125 limit, in nondecreasing square/Chebyshev rings. It retains only coordinates and counters, never chunk references, and visits at most 128 coordinates in an active client tick. A rejected admission pauses at that coordinate for retry; zero budget does no work. An unfocused client honors the existing pause-background setting.

Client integration calls only the existing nonloading hasChunk query and dirty capture path. Already-dirty coordinates advance without replacing their revision. New discovery admission stops when the dirty queue reaches 512; the existing 4,096 limit for all other dirty events is unchanged. Native rediscovery creates a newer revision even when disk work or upload is in flight, so the existing revision guards can reject stale persisted results. Reset clears the cursor and progress. Diagnostics expose visited/total/complete; **complete means enumeration finished, not captured, uploaded or visibly ready**.

At radius 125 the coordinate model contains 63,001 positions, requiring at least 493 batches at budget 128 without pressure. These are deterministic counts, not measured game latency or useful-distance acceptance. The starting center/radius are fixed for that attach; later arrivals/travel continue through existing ChunkEvent.Load handling. No distance maximum, host tracking, priority/network transport, terrain generation, simulation, surface-water rendering, shaders or vendor providers changed. No distant unseen terrain is promised.

## Changed source

- [DeliveredChunkScan.java L15](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L15): bounded coordinates, scalar ring cursor, pausable drain.
- [VoxelLodRenderer.java L259](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259): bounded attach discovery, focus/admission/revision handling; reset at L753.
- [LodClient.java L53](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L53): enumeration diagnostics.
- [DeliveredChunkScanChecks.java L9](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScanChecks.java#L9): Cartesian-set oracle independent of ring traversal, uniqueness/order/completion, 128 bound, pause/retry/zero/exception controls, signed coordinates, radius and overflow rejection.

All other changes are this dedicated jam's receipts/tools. The build-only renderer version and test-task registration are retained in tools/renderer-version.gradle; Themes retains 0.1.0-alpha.8.

## Consumed findings and final-snapshot discipline

Consumed jam4's self-audit/backlog: retain exact geometry/output ownership; no broad FPS or useful-distance credit from component probes; obtain live client/camera evidence before claiming acceptance. The current attach-time clipping is a concrete source correctness gap that can be repaired while those hardware gates remain open. Meshoptimizer remains development-only and does not ship as a new dependency.

Read the latest relevant independent engine audit, docs/engine-hourly-audits/2026-10-26T1627-b198804/REVIEW.md, at af8e1c1e501d474138b4ec6a072c71a8020e9f79. Its scoped allocation result and native matrix are historical carried evidence, not fresh jam5 tests; its full-engine 5.0 rubric is unchanged. Its recharge/allowance defects remain with the engine owner. Its nested-module/native payload verification informed the final archive and selected-module checks here.

Two pre-scored reviews were invalidated: the first candidate omitted the unfocused discovery guard; the second skipped working/pending keys and could miss authoritative rediscovery of stale disk data. Both corrections are documented in correction receipts. The final full 36-task build, archive/class/resource checks, extracted-product discovery test and two packaged native sessions were repeated on b8e69b6 after the last correction. Intermediate d6750d3 had been checkpointed on main; final 02eb0d8 supersedes it. Earlier package/native results in superseded-cache-guard are not used as final qualification.

Main advanced with audit and engine-claim documentation while qualification ran. Rebased final code onto fresh af8e1c1, with all 21 non-doc root entries identical to tested b8e69b6 and only docs different. The intervening docs are audit/ownership records, not build/art inputs. See rebase-build-input-identity.json. Four pushed source files were read back exactly. Runtime was integrated by nonforce expected-parent update after fresh main and ownership checks. No stale forced update.

## Six equally weighted renderer categories

| Category | /10 | Fresh versus carried evidence and limits |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | Fresh bounded enumeration implementation at [L259](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259), independent coordinate oracle at [checks L9](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScanChecks.java#L9), discovery-checks.txt and packaged-discovery.txt. Corrects the source cap without increasing a slider; real client coverage, handoff/seams, hysteresis and unseen delivery are unverified. Grade carried unchanged. |
| Mesh generation/reuse/scheduling | 6 | Carried jam4 implementation at [LodMesher L45](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45), unchanged production module inputs except new helper. Prior 30–49% component allocation result is explicitly historical, not rerun this jam. New admission is bounded; no measured scheduling throughput, across-edit reuse or shipped meshoptimizer credit. |
| GPU culling/submission/memory | 4 | Carried bounded upload/culling at [VoxelLodRenderer L477](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477), and ordinary [drawIndexed L121](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L121). package.json verifies compiled identities only. No GPU execution, arena/descriptor improvement, real multi-draw indirect or VRAM measurements. |
| Actual FSR/DLSS implementation | 0 | Carried absence rechecked at [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5) and [UpscalingConfig L12](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/UpscalingConfig.java#L12). No vendor provider/dispatch. Native fallback remains; Vulkan linear whole-frame scaling and frame generation earn no FSR/DLSS credit. |
| Visual/backend/settings compatibility | 4 | Carried broader grade; fresh [focus guard L265](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L265), [reset L753](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L753), package/resource/native receipts. No mesh/surface/settings/art changes. Archive verification and Linux server lifecycle do not exercise OpenGL/Vulkan client reload/resize/world switch or water transparency pixels. |
| Validation and measured performance | 4 | Fresh [checks L16](https://github.com/Shnewbs/Deep-Primal/blob/02eb0d8f3739cf036717abf82d03dc7e2a325081/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScanChecks.java#L16), build.txt/result, package.json, packaged-discovery.txt, default-native-result.json and post-native-identity.json. Functional coordinate counts, full build and two server sessions pass; no fresh timing/allocation/upload/frame/VRAM or named-hardware camera/travel/edit baseline. Grade unchanged. |

**Average: 22/6 = 3.67/10. Previous jam4: 22/6 = 3.67. Change: 0.00.** The source coverage fix is useful, but unrun client gates prevent higher usable-distance or performance credit. Target above 8.5 remains open. The independent engine grade is not modified.

## Final checks and delivery

Full Java 25.0.4.1 / Gradle 9.2.1 build completed successfully in 322.02 seconds: **36 tasks executed with --rerun-tasks**, including core/cache/material/module gates, visualValidationClasses and injected rendererDiscoveryChecks. Deprecated Gradle features remain a warning. The exact argv is build-command.json, producer result is build-result.json, and tools/COMMANDS.txt records the additional invocations.

Direct and actual extracted-product helper checks pass: 108,999 oracle coordinates, 92,064 beyond radius 32 across the corpus; all traversal bounds, uniqueness, square-ring ordering, negative-coordinate, retry/zero/exception and overflow cases pass. The wrapper asserts the production class originates from the extracted nested core JAR, not a private fixture. Source-identity.json records 851 unchanged Git-verified inputs and four owned source changes.

Seven outer/nested CRC and no-duplicate checks pass; **592** compiled product classes, **3** independently compiled core classes and **221** excluded private fixture classes pass. Full identity hashes are in class-identity.json. All six selected nested module filenames appear in both packaged native sessions. Two Vulkan Windows AMD64 PE DLLs, no Linux/macOS Vulkan payload, OpenGL fallback classes, exact notices, 2,508 Themes resources, 43 palettes, 11 approved ground images and 148 generated soils pass.

Two sequential actual packaged **Linux dedicated-server** sessions pass fresh/reopen with saved channel revision 2, terrain/deposit baseline replay, actual loaded class hashes, soil/source markers, explicit RCON save-flush acknowledgment, failure precedence, ordered shutdown and exit 0. This is a bounded preservation regression, not a fresh independent ten-session engine audit and not client rendering execution.

[Download renderer.5 development testing JAR](https://chatgpt.com/library?file_id=libfile_da696bd6b3908191aa5c2eeb5f81ae2d&account_id=personal).
Size: **125,296,418 bytes**. SHA256: `04d26d3a47348cde3ca23be97b1d0caf5a7e65d6fcf2c12b074fb0f029476b2b`. Unique final producer, exact final source inputs above; producer/export equality verified after native testing and again after save. Upload succeeded and local file identity metadata was applied. No earlier or intermediate JAR is presented as this build. No authorized GitHub release/upload action or gh executable was available; no GitHub Release, stable or CurseForge publication.

## Remaining gates and next repair

No /dev/dri device is exposed on this Linux EPYC 9V74 host. **Not run:** real client discovered coverage at radius 33/96/125, cache-in-flight/native-edit races under a running client, OpenGL/Vulkan GPU pixels, reload/resize/world switch/client save-reopen/shutdown, Windows or AMD8060S execution, negative-coordinate visual seams, silhouettes/cutouts/lighting/transparent-water ordering, player travel/edit images, multiplayer/long soak. Neither server sessions nor standalone CPU tests establish these results.

No fresh matched frame/rebuild/allocation/upload/culling/submission timing, draw/descriptor count, resident/pending/cache RAM or GPU-memory baseline is available. Camera/resolution/API are null in host.json; no FPS or speed gain is claimed. The coordinate counts are exact workload bounds only.

Next priority: instrument and validate live discovery/capture/upload readiness and native handoff under travel/edit/cache races on a GPU client, then capture camera/travel/edit baselines with host/seed/camera/resolution/distance/warmness/water/LOD context. Continue bounded work/reuse and hierarchical coverage before larger-distance admission. Document an authoritative unseen-terrain delivery contract and coordinate generator/network prerequisites without expanding this feature's scope. Later FSR/DLSS work still needs current official SDK/licensing/packaging review, actual render inputs/providers/resource lifetimes and supported-hardware/API validation. Frame generation is a distinct later feature.

## Ownership boundary

Read main, engine/water state, architecture, graphics/LOD roadmaps, renderer receipts/backlog, recent commits, scheduler/lease records and measurement state before editing; refreshed state again before integration. No applicable new AGENTS.md was found in inspected root/module trees. Engine builder1700's active qualification and foreground water scope were preserved. Both performance reservations were functional/nonexclusive; no timed benchmark ran here. Only this renderer's four source files and dedicated receipt path were claimed. Private checkout/cache/output; toolchain read-only; no other process paused/killed and no shared mutable output used. Other work-state and schedules were not edited; no subagents.

Scoped claim is completed/released with the final receipt. Dedicated renderer work remains enabled. Finite completed work does not imply continuous execution.


<!-- END PRESERVED SOURCE -->
