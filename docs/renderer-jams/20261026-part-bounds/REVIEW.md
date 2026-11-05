# Renderer jam 20261026-part-bounds — self-audit

Source parent d6e32501827565239220d61078f6d9c41ebf70fd; claim commit c48a8a21f0783e4c6dfc8f9336ee9040532122ec; exact reviewed implementation snapshot 7772c36daf3fd6cd6a90bea95484272eaf1c603f. Testing version 0.0.1-alpha.29-dev.28-water.2-renderer.1 is supplied by the retained build init script. Final integration/receipt SHA is the commit containing this report; runtime snapshot remains 7772c36. Review is read-only after the two implementation files were frozen. This is the renderer author's self-audit, not the separate independent engine audit.

## Change and evidence

LodMesher.Builder initialized every part at the chunk floor and retained cumulative height extrema after flush. Independently culled parts therefore included empty floor space and earlier parts' vertices. Initialize/reset to empty extrema; actual emitted vertices establish floor/ceil bounds. Geometry, winding, indices, normals, colors, partial-water heights, page coordinates and buffer sizes are unchanged. VoxelLodRenderer consumes these bounds directly at lines592–599 and tests them at486; no simulation or geography changes.

The real ColumnarChunk fixture covers negative page coordinates, floors -64/0/320, all three resolutions, gaps, two solid materials and 8/16-unit water, with multipart output. Baseline strict check fails (18 loose parts). Final check:18 parts,0 loose bounds. A conservative vertical-slab oracle rejects162 cases versus18 before, with no false rejection; this is a synthetic visibility count, NOT measured frame savings. Matched geometry/material/normal/index SHA256 is 3b6ae8b901558f5dcdb8bcc86f9d632c015d10e991c15fd01dd701d74e18f612. The first fixture attempt did not split coarse output; it was corrected before final baseline/candidate runs. Direct javac must use -g to compare bytecode with Gradle; failed default/-parameters comparisons were tooling flag mismatches, not accepted package results.

## Six-category renderer rubric

Every source link is pinned to the reviewed snapshot. First dedicated renderer jam; prior renderer average/delta: not available. No whole-engine grade changes.

| Category | /10 | Evidence and limitation |
|---|---:|---|
| Distance/LOD correctness and useful coverage | 4 | [VoxelLodRenderer L445](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L445): native handoff, error/hysteresis and per-part culling; fresh bounds regression passes. Explored-only coverage, page seams and live acceptance remain open. |
| Mesh generation/reuse/scheduling | 5 | [LodMesher L221](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L221): bounded4096-vertex parts, material/fluid-preserving greedy faces, now exact per-part bounds; geometry digest unchanged. No shipping meshoptimizer, measured reuse improvement or broader meshing benchmark. |
| GPU culling/submission/memory | 4 | [VoxelLodRenderer L518](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L518): bounded eviction/upload paths; bounds consumed at595. No hierarchy, GPU indirect implementation or fresh GPU gains. drawMultipleIndexed at681 is not credited as GPU multi-draw indirect. |
| Actual FSR/DLSS implementation | 0 | [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5): requests still fall back to native. No provider/dispatch integrated. Basic spatial scaling and frame generation earn no vendor-upscaling credit. |
| Visual/backend/settings compatibility | 4 | [VoxelLodRenderer L635](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L635): common GPU interface retained; settings and water vertex bytes unchanged. Current Windows/native/OpenGL/Vulkan pixels, reload/resize/world-switch/save/reopen/shutdown unrun. Historical receipts are not rerun evidence. |
| Validation and measured performance | 4 | [LodPartBoundsChecks L37](https://github.com/Shnewbs/Deep-Primal/blob/7772c36daf3fd6cd6a90bea95484272eaf1c603f/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodPartBoundsChecks.java#L37): regression and extracted-package execution pass; build/package results are attached. No camera/travel/edit frame p50/p95/p99, GPU timestamps, allocation/upload tails, RAM/VRAM baseline or named-device acceptance. |

**Average21/6 =3.50/10. Target >8.5 not demonstrated.** Scores assess existing renderer foundations plus this bounded correction, not just the defect's repair.

## Coordination, delivery and next repair

Consumed latest engine audit docs/engine-hourly-audits/2026-10-26T1033-4a583db/REVIEW.md: ordinary precipitation still throws for supported saturated constituent state. Preserve that failing gate and leave production repair to the water builder. No prior dedicated renderer audit exists. Root AGENTS.md and dedicated renderer state/ledger did not exist; module trees contain no applicable AGENTS.md. Engine/water leases were checkpointed; latest recorded measurement was completed functional-only. Scoped renderer claim is released at integration. No other automation or writer's work was changed; no subagents used.

Source restoration compared846 retained inputs with pinned Git blob identities, zero mismatches, before modification; the two changed files were then read back from7772c36 with exact content equality. Separate project/cache/output; immutable retained toolchain reused read-only. Host Linux AMD EPYC9V74,9 CPUs; no /dev/dri device. No active GPU timing reservation was taken. Seed/camera/resolution/API/warm status are inapplicable to the non-timed synthetic mesh check and are not invented.

New JAR retained locally for evidence, withheld from testing delivery/publication because the combined source has a confirmed essential native precipitation crash. That failure is carried from the pinned independent audit, not freshly rerun here. No Library upload, GitHub release or CurseForge release was attempted for a known unqualified installer. Do not treat archive success as native acceptance. No older artifact is relabeled.

Next renderer priority: instrument reproducible camera/travel/edit frame/culling/rebuild/upload and memory baselines, then differential full-frustum/live reload tests for these tighter bounds. Continue bounded mesh/visibility work before expanding usable distance or integrating actual vendor providers. Full-detail cap125 and explored LOD default192/max512 remain unchanged; no unseen-terrain delivery is claimed. FSR/DLSS require actual SDK integration and hardware gates in a later jam.

## Final checks on the frozen candidate

Full Java25.0.4.1 / Gradle9.2.1 build exits0:36/36 tasks executed,5m29s including full core/cache/ground/module checks, visualValidationClasses and rendererPartBoundsChecks. Exact command and console are retained. Seven recursive CRC/duplicate checks pass; 588 product classes match fresh compiler output; 10 directly compiled core classes match with javac -g; 215 fixture classes excluded. Packaged actual-core code-source assertions and bounds regression pass. Windows DLL-only payload, OpenGL fallback classes, approved art/palettes, Themes and notices checks pass. No producer byte change after successful build and final package validation.

Retained artifact size 125291158 bytes; SHA256 `5236d13c114cd4ffdd74b6b042c30182fc370a07030873a3b2153aea1c5b8476`. Final producer completion does not remove the inherited native crash gate. Current candidate native/GPU lifecycle tests are NOT RUN; no FPS claim.

Integration recheck: builder-20261026T1100 now holds an active functional-build/native reservation until11:45Z for precipitation admission, explicitly nonexclusive for benchmarks. This renderer uses isolated outputs, makes no timings and changes no water/builder files. The inspected final compare contains only the two claimed renderer source/test files and this jam's dedicated documentation. Preserve the builder's active state verbatim.
