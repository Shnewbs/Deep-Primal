# Renderer jam 26 — read-only self-audit: page-frustum A/B gate

**Jam:** `renderer-20261029T0555-frustum-ab-gate`.
**Exact immutable candidate source:** [`ec4aab72cddc4c8c80c36384918121c09ebaa710`](https://github.com/Shnewbs/Deep-Primal/commit/ec4aab72cddc4c8c80c36384918121c09ebaa710).
**Preclaim main:** `05c61ec056f96adf2bc196ab5fca729d0d017e0a`; bounded claim `006d06d68de0069bba3c97c50ed01364fab7c357`. Implementation source/test checkpoint `5743b44e2583036b0cc09e3b15588167f195042d`.
**Version:** no bump; engine version remains owned elsewhere. This is a **read-only renderer self-audit**, not a new independent whole-engine evaluation. The exact candidate was frozen during scored review.

## Source changes and consumed findings

Consumed [jam 25](../20261029-page-frustum-broadphase/REVIEW.md) and [independent Dev71](../../engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md). Both left native matched Vulkan/OpenGL coarse-on/off, FPS, VRAM and water-pixel acceptance open. The Dev71 ocean connectivity risk is engine-owned and untouched. `AGENTS.md` was absent; engine/water work states, LOD/drainage architecture, renderer backlog and graphics/upscaling documentation were read.

- [`LodPageFrustumBounds.java` L12–18](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L12-L18), blob `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc`: default-on, startup-read `deepprimal.lod.pageFrustumBroadphase`; only literal `false`, case-insensitive, disables the coarse call.
- [`VoxelLodRenderer.java` L524–588](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L524-L588), blob `1ca2814bc052bf0fab6c12c76b64b41899c142e5`: guarded page-level frustum calls, attempted/passed/rejected counters on completed telemetry extracts, unchanged original ordered per-part selection if coarse disabled or page visible. [L934–936](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L934-L936) resets counters per world.
- [`LodClient.java` L90–100](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L90-L100), blob `29973f705ae4fa7fb0af400fc4344725131e6c04`: honest enabled, attempted, visible and rejected diagnostics and startup/restart scope.
- [Committed isolated test](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/docs/renderer-jams/20261029-frustum-ab-gate/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumOptionChecks.java), blob `7f064130651be66ce7205cbb0db3f5e12b0dec84`, and [usage README](README.md). Prior jam API doubles are referenced, not recopied into production.

No terrain, engine/water work-state, Vulkan provider, native FSR/DLSS, GL fallback, mesh, material, water, render-distance maximum, config serialization, GUI or unrelated scheduler files were changed. An unrelated engine writer inserted its own `docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/LEASE.json` between renderer commits; compare confirms no file collision.

## Executed focused checks

Host Linux OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. The owner-provided JDK25 archive and the previously restored JDK25 directory are **absent** in this workspace. Compiled the exact production helper and committed option test with production-excluded Minecraft mesh/AABB test doubles, then ran:

```sh
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=FALSE -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumBoundsChecks
```

- **PASS:** committed option regression 60,005 assertions each on default/true and 60,003 each on false/FALSE; exact Git blob of compiled helper and committed test matches GitHub.
- **PASS:** extended noncommitted synthetic option fixture 80,006 assertions on default/true/invalid values, 80,004 on false/FALSE. Previously committed page-bounds union/parity regression passed twice, 332,530 assertions each.
- **PASS:** read-only Git-pinned source/caller integration **25/25** checks. All new counters are opt-in telemetry; enabled coarse tested = visible + rejected for completed extracts, disabled coarse calls = zero. Full Minecraft integration is **not** independently compiled here.
- **PASS expected negative:** hardcoding `ENABLED=true` compiles but fails the false-property fixture at assertion 1.

The warm, paired **CPU-only** 4,096-page × six-part synthetic fixture used 201 alternating samples per mode with cached page bounds. Mostly-offscreen p50 fine/coarse was **58,136/15,242 ns** and **58,767/15,212 ns** in two Linux JVM runs. **All-visible regresses:** p50 fine/coarse **72,858/77,695 ns** and **70,084/76,433 ns**. The A/B gate allows the owner to identify which scene mix benefits on actual hardware. These are not native frame/GPU p50/p95/p99, real FPS, VRAM, actual transparent-water pixels or a claim of higher usable distance.

## Six equally weighted renderer grades

| Category | /10 | Exact pinned evidence / missing gate |
|---|---:|---|
| Distance and LOD correctness / usable coverage | **6.55 carried** | [Unchanged native handoff and cached page selection](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L538-L577); no new distance, unseen terrain, real silhouette acceptance |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Prior splitter evidence](../20261028-split-preflight-once/REVIEW.md); no mesh worker geometry change |
| GPU culling/submission/memory | **4.55 carried** | [CPU page-gate only](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L554-L574); no GPU indirect/occlusion, VRAM, driver/pixel gain |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [FSR1 spatial implemented; temporal FSR and DLSS game dispatch missing](../../UPSCALING.md) |
| Visual/backend/settings compatibility | **5.30 (+0.05)** | JVM default-on/off parity, [original ordered fine path](https://github.com/Shnewbs/Deep-Primal/blob/ec4aab72cddc4c8c80c36384918121c09ebaa710/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L559-L577), invalid bounds fallback; no native water/Vulkan/GL frames |
| Validation and measured performance | **5.80 (+0.05)** | Production helper + committed and extended parity, hardcoded-true mutant rejection, 25/25 pinned checks, paired adverse/all-visible CPU cases; no hardware frame/tail/GPU acceptance |
| **Equal-weight mean** | **5.69 (+0.01 displayed)** | `(6.55+6.95+4.55+5.00+5.30+5.80)/6 = 5.6917`; prior renderer jam25 **5.68**. Independent whole-engine carried grade **5.0/10** unchanged. |

## Artifacts, defects and next repair

The source/test evidence ZIP `DeepPrimal_Renderer_PageFrustumAB_20261029_5743b44_SourceEvidence.zip` is **13,671 bytes**, SHA256 `7793b48398ded37269b440dcfe2092f80baeef33e8f94fabd7a381bf9acd9dcd`, **16 entries**, outer ZIP CRC and every SHA256 manifest entry verified. Library upload succeeded to `/Deep Primal/Renderer Jams/`. This is source/test evidence, **not a JAR**, and has no platform/native payload.

**Unrun owner gates:** complete Java25 Gradle9.2.1/NeoForge/Minecraft build and JAR/compiled-class/nested CRC/release verification, Windows Vulkan/OpenGL/RTX native execution, device FPS, frame p50/p95/p99, VRAM/RAM/draw/descriptor counts, native culling pixels and transparent-water, resource reload, resize, world switch, save/reopen/shutdown, FSR temporal and DLSS Super Resolution. The October 27 owner override assigns these to the owner; no hosted Actions, CurseForge, native GPU acceptance or fresh JAR is claimed.

**Next:** same scene, camera, world, FOV, full-detail/vertical/LOD setting and warm/cold state with coarse enabled/default versus `-Ddeepprimal.lod.pageFrustumBroadphase=false` on **both** Vulkan and OpenGL. Capture page attempted/accepted/rejected, fine tests, CPU/GPU frame p50/p95/p99, draw/VRAM and water pixels including an all-visible adverse scene. Then tune any proven culling regression or move to measured submission/upscaler blockers. Preserve the separate engine/water builder and independent auditor.
