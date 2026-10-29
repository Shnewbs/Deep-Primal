# Independent Deep Primal audit — 2026-10-29T1635-a7ade50e

**Pinned current-main source at start:** [`a7ade50ea8aac1458db8f2d6b1461e16967bc00d`](https://github.com/Shnewbs/Deep-Primal/commit/a7ade50ea8aac1458db8f2d6b1461e16967bc00d), version `0.0.1-alpha.29-dev.82-routed-forest.1`. This is independent, read-only audit scope; all runs used local isolated audit resources. Later Builder update `e9d9a5db536dc24105fc44849614388b6ed155ba` hardened two Python runners **after** the pin; this audit does not claim to test that later update.

## Source evidence and findings

* Engine Dev82 [`WatershedRegionPortBridge.java` L269–335](https://github.com/Shnewbs/Deep-Primal/blob/a7ade50ea8aac1458db8f2d6b1461e16967bc00d/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java#L269-L335), Git blob `46fdfaaeea3e741260c77c1001d56041d6ff2c2c`, adds combined observed receiver-forest cycle rejection after collapsed interior routes. The author-reported Java21 bridge test is 2×3,935 assertions, 14 rejected mutation controls; this was **inspected, not independently rerun**. Independent Java21 **algorithm model** (not full production bridge) passed 65,000 randomized functional-graph comparisons with an independent Kahn indegree oracle: **130,004 assertions**.
* Renderer [`LodPageFrustumBounds.java` L20–77](https://github.com/Shnewbs/Deep-Primal/blob/a7ade50ea8aac1458db8f2d6b1461e16967bc00d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L20-L77) / [`LodPartFrustumRuns.java` L18–54](https://github.com/Shnewbs/Deep-Primal/blob/a7ade50ea8aac1458db8f2d6b1461e16967bc00d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPartFrustumRuns.java#L18-L54): pinned Git blobs `e8b289266fb33ed2b10e22acae0ab6bd8d3f893b` and `eea4bb00d9652796426fcc7a966bd6bb002dc6aa` matched extracted renderer-source Git objects. Exact-source helper/stub strict `javac --release 21 -Xlint:all -Werror` passed. Twelve independently repeated Java21 `-ea -Xverify:all` passes (broadphase on/off) totaling **6,606,201 assertions**; the old mutable-live-bounds negative control exited nonzero as intended. Whole `VoxelLodRenderer`/GPU not compiled. The author's synthetic page-publication median ~9.7% slower/+1,120 B per publication remains a **reported**, not independently rebenchmarked or real-frame, measurement.
* Root `docs/ENGINE-WORK-STATE.json` remains dev53; scoped `docs/TERRAIN-BLENDING-WORK-STATE.json` records completed Dev82 with lease released. Physical authoritative water/salt/sediment routing, certified ocean outlets, V8 native registration, save/reopen and real renderer results are still unproven.

## Equal-weight overall grade — historical baseline carried, not a fresh whole-engine regrade

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Tests/docs | 7 |
| Polish/tooling | 4 |
| **Total (Tier 2)** | **50/100 = 5.0/10** |

Target **strictly greater than 8.5/10** remains unmet. No unactivated prototype earns gameplay credit; the renderer's separate author-only six-category 5.80 grade does not replace this ten-category score.

**Not run:** JDK25 absent in current workspace/Library search; complete Java25 Gradle 9.2.1/NeoForge build, qualified JAR/archive class/CRC validation, native save/reopen/shutdown, native water accounting, Windows AMD8060S Vulkan/OpenGL and real GPU/driver timing. Owner's October 27 override assigns complete builds and delivery to the owner. No JAR, release, historical JAR deletion or builder interaction occurred.

**Independent archive:** `DeepPrimal_Dev82_IndependentAudit_20261029T1635_a7ade50e_SourceEvidence.zip`, 15,255 bytes, 24 CRC/SHA256-verified members; SHA256 `6066c08835888593d7303fb4c33041790fc1b02204dde1f12d617be2c3bc2851`; saved in Library `/Deep Primal/Independent Audits/`. It includes logs, source-identity evidence, model and receipt. Next priority: certified global ocean outlets and once-only persistent physical discharge, then native old/new world compatibility and GPU water/LOD acceptance.