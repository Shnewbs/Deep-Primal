# Renderer jam20261026-lod-reduction — self-audit

Initial base5067254f96bba108ac74cabad226ecd56c307d4a; final water base293516cdb0c62d66ae12a5146e4ac2e6590c45a3; integration parent7525c6ee55fa9c44a7f234e24834fcdf655ceefb; scoped claim9b26b8619b3a44c1542cc10f40a11658c4cd3cf0; frozen reviewed/pushed runtime source5a2131a6309aaf5de6377bef57f433f8f624ef7b. Development version0.0.1-alpha.29-dev.30-renderer.3 is applied by the retained init script. This is the renderer author's self-audit, not the independent engine audit. No product changes during the scored review; checkout provenance correction and final qualification described below do not alter the candidate commit.

## Implementation

LodMesher no longer fills air/zero-color runs into an already zero-initialized dense array. For2×2/4×4 material reduction it stops at the first opaque sample: the existing priority rule can never replace that choice. If no opaque material exists, the first water sample still wins. Zero/air, partial water, palette/material priority, all three detail levels, signed page coordinates, positions, indices, normals, bounds, allocations, page partitioning and actual GPU bytes remain equivalent. No terrain, water simulation, shader, transparency ordering, native handoff, distances, provider or UI change.

LodReductionChecks contains ten frozen baseline signatures covering flat solid, cavity/air gaps, cliff/material changes, partial water8/16 plus later opaque priority and height256, each at floors-64/320 and page(-17,-3). The separate baseline is generated solely by renaming pinned5067254's LodMesher class; it is a private fixture, never packed. Direct candidate-vs-baseline comparisons and frozen signatures include every emitted position/color/normal/index/height and integer bounds at every detail level. Existing exact-part-bounds checks remain applicable.

## Exploratory CPU component measurements

Linux AMD EPYC9V74 virtual host,9 exposed CPUs, Java25.0.4.1 -Xmx512m, shared/nonexclusive host. No /dev/dri device, GPU/API/frame timing, scene camera/resolution or saved-world seed. Deterministic input construction, hashes and setup are excluded. After100 warm-up rounds per variant,31 alternating-order pairs each execute3 whole builds; timings below are medians of three-build mean samples, not per-frame or individual-call latency distributions. ThreadMXBean measures allocation on the calling thread. The initial8-warm-up/11-pair experiment was JIT-sensitive/mixed; its unrejected raw output is retained, not presented as an accepted gain.

| Fixture | Before median ms | Candidate median ms | Median change | Allocation bytes/build (both) |
|---|---:|---:|---:|---:|
| 0 | 0.727023 | 0.685123 | 5.76% | 447080 |
| 1 | 0.626802 | 0.631723 | -0.79% | 573664 |
| 2 | 1.106587 | 1.070626 | 3.25% | 510400 |
| 3 | 1.091594 | 0.997162 | 8.65% | 541488 |
| 4 | 5.424766 | 5.204795 | 4.05% | 790760 |

Positive change means lower component median. Four medians improve about3–9%; cavity is0.8% worse. Tail results are mixed: candidate p99 is worse in four fixtures, better in the cliff fixture. Allocations are unchanged. Raw p50/p95/p99 are retained; shared-host/JIT/GC variability and sample averaging prevent claiming broad latency, FPS, memory or driver gains. This is a work-reduction implementation with equivalent output; named-hardware visual/frame-tail acceptance remains OPEN.

## Six equally weighted renderer categories

All source evidence is pinned to5a2131a6309aaf5de6377bef57f433f8f624ef7b. Prior renderer average3.50/10; this scoped self-audit retains3.50, delta0.00. New CPU checks do not replace missing whole-frame/GPU evidence.

| Category | /10 | Review scope | Evidence and remaining gates |
|---|---:|---|---|
| Distance/LOD correctness and usable coverage | 4 | carried, scoped output checks | [VoxelLodRenderer L466](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L466): explored-only native handoff/error selection unchanged; ten exact mesh signatures. No unseen delivery or live distance/seam acceptance. |
| Mesh generation/reuse/scheduling | 5 | fresh scoped reduction review | [LodMesher L36](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L36): avoids zero writes and unneeded footprint samples, material priority unchanged. Matched CPU results are modest/mixed and do not close reuse/scheduling/meshoptimizer gates. |
| GPU culling/submission/memory | 4 | carried | [VoxelLodRenderer L580](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L580): bounded uploads/eviction and existing CPU telemetry unchanged. No GPU hierarchy, actual indirect batching, arena or driver measurement added. |
| Actual FSR/DLSS implementation | 0 | carried absence, source-root verified | [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5): no vendor provider/dispatch; native fallback. Spatial scaling and frame generation receive no credit. |
| Visual/backend/settings compatibility | 4 | carried; scoped byte equivalence | [LodReductionChecks L30](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodReductionChecks.java#L30): full mesh byte/attribute signatures unchanged. No new OpenGL/Vulkan pixels, native handoff, reload/resize/world-switch or named-device acceptance. |
| Validation and measured performance | 4 | fresh focused regressions/component probe | [LodReductionChecks L52](https://github.com/Shnewbs/Deep-Primal/blob/5a2131a6309aaf5de6377bef57f433f8f624ef7b/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodReductionChecks.java#L52): frozen before/after hashes and real-mesher benchmark. Final build/package/native records follow. Mixed tails/no full-frame measurements prevent a score increase. |

**21/6 =3.50/10; >8.5 not demonstrated. Whole-engine external grade unchanged.**

## Coordination and provenance

Consumed previous renderer jam2 REVIEW/BACKLOG, current engine/water states, architecture/roadmap and latest audit docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md. The independent audit retains5.0/10, ten native sessions and unpromoted hydrology scheduler candidates; no scheduler/chemistry takeover. Its prior packaged RollingTiming/LOD-bounds results are carried evidence, not this jam's fresh GPU checks. Main since jam2 changed only the docs root, verified by complete root-tree hashes rather than truncated compare-file inference; no new applicable AGENTS.md or production deltas. The active builder1425 reservation is a separate nonexclusive functional hydrology build. Scoped renderer ownership excludes its files, caches, outputs and work state. No other schedule changes or subagents.

Own completed renderer dependency cache was moved to this isolated run, not shared with an active job. Only completed own reproducible build/native outputs are reclaimed; saved artifacts, source, approved art and receipts retained. Immutable toolchain reused read-only. A preflight found one extra trailing newline in six previously restored files. The first full-build attempt/package result is invalidated for exact-source provenance, even though whitespace did not change product logic; correct the checkout and repeat all final full-build/package gates. No rejected binary is delivered.

Unrun: actual camera/travel/edit frame p50/p95/p99, CPU/GPU allocation/upload/draw/descriptor/RAM/VRAM baseline, OpenGL/Vulkan client/pixels/resource reload/resize/world-switch/client save-reopen/shutdown, Windows/AMD8060S execution, long multiplayer/travel/edit soak and vendor SDK integration. Linux dedicated-server sessions only qualify server lifecycle. No speed/quality superiority or continuous execution claim.

Next: investigate mixed CPU tails under controlled named hardware; validate culling and telemetry in real camera/travel/edit scenes; complete rebuild/allocation/descriptor/full-frame/GPU memory instrumentation. Continue mesh reuse and conservative hierarchy/streaming before expanding tracking-safe distances or actual vendor providers. FSR/DLSS and frame generation remain unimplemented; their absence is not hidden by native fallback.

Qualification reset: main promoted dev.30 at293516cdb0c62d66ae12a5146e4ac2e6590c45a3 while the dev29 candidate was building. That candidate was not integrated or delivered. Only this run's superseded build/descendants were stopped. Adopt the exact two changed inputs (HydrologyRuntime.java and gradle.properties), preserve all other owner files, rebase the same renderer changes on293516c and repeat complete build/package/native checks. The final scored snapshot is3b1de33; scoped review repeats after these gates. Renderer benchmark source is unchanged between the candidates.

Own cache recovery: the superseded build left journal-owner metadata that collided with namespace PID51. A final-build attempt failed before compilation waiting for that private cache lock. After the own build/session had exited, an exclusive nonblocking OS byte-range lock on the entire journal file succeeded; only then was that stale own lock unlinked. Other jobs/caches were untouched. The current full qualification is a new attempt; stale-lock and superseded outputs are retained separately and not counted as passes. Initial scored drafts are superseded; repeat the source/compatibility review against the final dev30 snapshot after all final gates.

## Final scored snapshot and qualification

The final read-only review was repeated against pushed5a2131a6309aaf5de6377bef57f433f8f624ef7b after all code work and final checks. No product edits during this final scored review. Full build execution snapshot3b1de33ebc3e09d9528bfe3f0ff972518579eb1a and pushed5a2131a have identical complete production module trees and root build inputs. Only documentation differs: retained water ownership, staged uncompiled hydrology recharge evidence and dev30 delivery receipts. Those are not approved-art inputs or Gradle product sources. The exact root-tree and documentation-delta proof is retained; no newer production fix was silently included and no extra full build is claimed for docs-only metadata. The artifact is from new renderer3 product inputs identical to the pushed snapshot, not a relabeled renderer2 binary.

Final Java25.0.4.1/Gradle9.2.1 full available build exits0 in5m38s,36/36 tasks executed, including full core/cache/resources/module boundaries, visualValidationClasses and rendererReductionChecks. All850 retained unchanged input blobs match pinned sources; the two renderer files match the committed candidate. The first whitespace-drift build, superseded dev29 attempt and stale-cache startup failure are not counted as final passes.

Seven outer/nested CRC/no-duplicate checks pass;590 production compiled/package identities,10 direct core class identities and219 fixture exclusions pass. Windows Vulkan DLL-only payload/OpenGL fallback,2508Themes resources,43palettes,11approved ground images,148generated soil resources and exact notices pass. Actual package-core code-source guards, ten new frozen signatures and the existing18-part exact-bounds checks pass with zero loose bounds and unchanged geometry digest. Baseline/probe classes remain outside the product archive.

Two sequential packaged Linux native default-profile sessions pass fresh/reopen, actual loaded-class hashes, frozen terrain/deposit replay, soil/source checks, explicit RCON save flush, ordered clean shutdown and exit0. These are server checks, not renderer GPU/client acceptance or the independent auditor's ten-session matrix. No final functional/package/native failures. Final producer/export SHA256 equal after native completion and after saving.

[Download dev.30 renderer.3 development testing JAR](https://chatgpt.com/library?file_id=libfile_abf1214694f48191a9de1ec96dd4632b&account_id=personal). Size125294519bytes; SHA256`9fcca4f2693c0e509440708cbd7133ed647b71d3287ae43d25e6f9e665086375`. Saved successfully with original-file identity metadata applied. No connected release-write/upload publisher or gh executable was available; no GitHub Release, CurseForge or stable promotion. Optional GPU/named-hardware gates remain explicitly unrun.

After archive/class/resource verification and both native runs completed, only this run's reproducible theme build output was reclaimed for upload space. Sources, approved art, exact raw evidence and both final JAR copies remain. Scoped ownership is released at this receipt commit; dedicated rendering work remains enabled, other feature schedules untouched.
