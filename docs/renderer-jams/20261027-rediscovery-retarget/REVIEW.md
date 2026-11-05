# Renderer jam 20261027-rediscovery-retarget — read-only self-audit

Reviewed **exact staged commit [3bae9d401019563f138739fc244e78ffb0c67749](https://github.com/Shnewbs/Deep-Primal/commit/3bae9d401019563f138739fc244e78ffb0c67749)** after candidate implementation and component testing. Initial main `2ae5e5f406e9a9d91aeb0d689772cbd35396524c`; scoped claim `c0a3a6eeef2dfb12bda2dbb60fa507b8ecff254b`. This is the **renderer author's read-only self-audit**, not a separate external whole-engine audit. None of the reviewed candidate code or tests was edited during scoring. The only subsequent commit is this review/receipt update.

## Exact reviewed increment and safety

Read-back verification matched *all seven* source/check/script/evidence Git blob IDs recorded in `test-results.json`. The staged [retarget helper](candidate/DeliveredChunkScan.java) applies configuration/radius changes, after-completion travel and bounded in-progress Chebyshev displacement; using signed-long differences avoids integer overflow. It adds no work to the original visitor, changes no geometry, and allocates no new collection per check. The [client tick patch](candidate/VoxelLodRenderer.patch) deliberately retains max-native render distance, bounded 128-visit tick admission, pressure retry and dirty queue, while restarting the cheap scan cursor where needed. It remains a patch **not merged into the production renderer**. Continuous movement can repeatedly re-target and delay distant enumeration; chunk-load callback provides a separate partial route, but actual long-travel coverage is unverified.

Pinned baseline evidence: [native tick discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/3bae9d401019563f138739fc244e78ffb0c67749/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259) initialized `deliveredScan` only when null. New candidate [shouldRetarget](candidate/DeliveredChunkScan.java) adds the missing decision; no value is claimed from increasing any setting maximum. Existing `20261027-admission-gate` owns a separate unpromoted `VoxelLodRenderer` candidate, requiring a future fresh-parent integration, and active `docs/GRAPHICS-UPSCALING-WORK-STATE.json` owns FSR/DLSS settings/providers. This jam touched neither live owner and changed **only** its own dedicated docs directory.

## Reproducible evidence and restrictions

Private Linux container: OpenJDK21.0.12.1. Commands and source identity in [run_checks.sh](run_checks.sh) and [test-results.json](test-results.json); local full-output excerpts below.

1. `javac --release 21 -d classes DistancePolicy.java candidate/DeliveredChunkScan.java checks/DeliveredRetargetChecks.java`, then `java -ea -Xverify:all ...DeliveredRetargetChecks`: **1056 checks pass across nine complete scans**, signed coordinate extremes, zero/large radius, recenter threshold and pressure pause.
2. Same candidate plus original `DeliveredChunkScanChecks`: **108,999 coordinates** visited; **92,064 beyond radius32**, bounded scan and original negative-coordinate/no-duplicate/pressure-retry suite pass.
3. Compile and run a separate deliberately broken helper that omits the `done()` retarget rule: compiled successfully but **expected rejection exit 1** with `AssertionError: completed move +1:16`. This was not a product check failure.
4. `patch --dry-run` and `git apply --check` on an exact native-tick position/context reconstruction: pass. **Do not mislabel this as an integration against a complete checked-out repository**; run the committed `run_checks.sh` from a genuine worktree before any promotion.
5. Exact local tested source/fixture/patch/runner Git object hashes agree with remotely staged objects; fresh fetch from the review pin confirms no bytes drift.

**Not run:** any Java25 full Gradle root/module build or complete engine suite, native NeoForge class/linkage, full renderer JAR producer, nested archive and compiled/source identity checks, loader/native resource and fixture exclusions, client Vulkan/OpenGL pixel/reload/resize/world-switch/save/reopen/shutdown acceptance, Windows/AMD8060S/NVIDIA tests, real camera/travel/LOD visual coverage, measured p50/p95/p99 CPU/GPU/draw/descriptor/VRAM/FPS. The current separate container has no private complete source checkout, Java25 or renderer dependencies; a pure JVM component pass cannot certify them. No new version/JAR/download/GitHub release or CurseForge publication.

## Six equally weighted scores (all carried, zero shipping credit)

| Category | /10 | Reviewed evidence and limitations |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | Staged [candidate retarget](candidate/DeliveredChunkScan.java) and 1056 new checks; production [discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/3bae9d401019563f138739fc244e78ffb0c67749/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259) unchanged, visible coverage/unseen-authority untested. |
| Mesh generation/reuse/scheduling | 6 | [Existing LodMesher traversal](https://github.com/Shnewbs/Deep-Primal/blob/3bae9d401019563f138739fc244e78ffb0c67749/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16); geometry untouched, prior allocation findings carried, no new matched mesh measurement. |
| GPU culling/submission/memory | 4 | [Existing VoxelLodRenderer](https://github.com/Shnewbs/Deep-Primal/blob/3bae9d401019563f138739fc244e78ffb0c67749/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java); no GPU/arena/MDI/VRAM proof. |
| Actual FSR/DLSS | 0 | [Existing vendor policy](https://github.com/Shnewbs/Deep-Primal/blob/3bae9d401019563f138739fc244e78ffb0c67749/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java); other worker owns vendor implementation, no SDK dispatch validated here. |
| Visual/backend/settings compatibility | 4 | Native effective radius read in the [proposed client tick patch](candidate/VoxelLodRenderer.patch), no packaged API, Vulkan/GL water/transparency or lifecycle validation. |
| Validation and measured performance | 4 | [Component checks and negative control](checks/DeliveredRetargetChecks.java), no Java25/full build, live scene or measured GPU/frame distributions. |

**Total 22/60, average 3.67/10, previous 3.67/10, delta 0.00.** All carried: no fresh complete renderer score uplift. The last independent whole-engine baseline **5.0/10** is unchanged, not regraded. Goal above 8.5 remains open.

## Next repair

1. Preserve separate upscaling owner; reconcile released `20261027-admission-gate` changes with this patch under a fresh nonoverlapping claim and pinned main.
2. Full Java25 Gradle/core/modules and actual-API admission checks, final artifact/package/identity gates, native client world-switch/travel/cold-warm camera GPU evidence; only then guarded direct-main promotion and a genuinely new development JAR.
3. Evaluate hysteresis/retarget starvation, signed edge and arbitrary native distances, accurate explored/unknown authority and GPU handoff; frame tail and resident/pending memory before any improvement claim. Existing independent auditor remains separate.

Owner scope and default art/water rules preserved. The narrow staging claim is released in the accompanying receipt. The renderer task remains ongoing until canceled.
