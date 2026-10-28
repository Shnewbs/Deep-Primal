# Independent Dev61 source audit — 2026-10-28T19:30Z

**Pinned source:** `2a341f90b6aaff2a253a50f8f792b24a47cc80fc` (7 commits after previously audited `7b854edd9c32c8d20d621495d4a935a5e6418946`).
**Scope:** read-only auditor; no production source changes, builder lease, shared checkout, timed GPU measurements, release, or JAR.

## New source since previous audit

- Builder committed opt-in `TerrainShapeV8.definitionReliefV3` and `V8GeneratorSnapshot.newReliefV3` at `0c0e97d7c8194962dd8d273135b05de64dfd5956`; Dev60 relief V2 and frozen V1/V7 geography are unchanged. V3 is not a registered native generator.
- Renderer committed best-effort future cancellation, disk-index reset, worker shutdown and cached GPU closes at `969abe129b94df7734afe61bc3b5d76f5155bf13`; device-loss remains untested.
- Builder's own source-only test record: 17,022,648 Java21 assertions; renderer's own source-model record: 1,664 checks. Reviewed, not independently rerun as full suites.

## Independently run

Linux Java21 strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` in separate scratch resources; current `TerrainShapeV8.java` was reconstructed from the GitHub patch and independently Git-blob verified as `7c518f9dbef345fa74051356aad4fbd52fda4834` (9,028 bytes). Prior Dev60 baseline source blob `17c856a1272336b611b40a3922f8e148adaa54f4`; GPU cleanup helper blob `9756d3b42b232a90b2c5af4c3686205dd2c48279`.

- **127,493 independent coastal assertions passed:** 21,209 protected columns, 3,857 transition columns, 734 inland. V3 had zero class changes at `continentalness <= 0.08`, while V2 had 654 class changes against V1 in this exact sampled protected band. Verified bit-exact V1 protection, bit-exact V2 parity at `continentalness >= 0.16`, previous sea-plane witness recovery, and deterministic graph identities.
- **Important residual finding:** In the transition `0.08 < continentalness < 0.16`, the V3 sample still has **two** V1 sea-plane classification changes: seed 0, x=-2006 z=-2282, continentalness=.119837908, V1 height=94.722346158, V3=100.639544783; seed 922337203685, x=9459 z=9273, continentalness=.125662178, V1=95.500805606, V3=103.902156699; sea plane=96. These are source-level changes, **not demonstrated native flooding or a proven ocean-mask disagreement**. Do not describe the entire shoulder as a protected coastline.
- **29,041 independent GPU cleanup-helper assertions passed**, 83,817 close callbacks including 21,039 injected failures. VoxelLodRenderer reset order inspected at lines 833–884; full class compile/native GPU reset not run.
- **Negative mutation:** changing the guard start from .08 to 0 compiled but was rejected by the coastal oracle on its first protected witness, as intended.

**Independent total:** 156,534 passed assertions. Reproducible ZIP: `DeepPrimal_Dev61_IndependentAudit_20261028T1930_2a341f9_SourceEvidence.zip`, 27,254 bytes, 17 CRC-verified entries, SHA256 `00d985565d35a6032568a27e9f8544099b4a016c17099cee43bdd02d841143fc`.

## Whole-engine scoring (historical full baseline carried; NOT fresh regrade)

| Category | Score /10 |
| --- | ---: |
| Terrain realism | 5 |
| Biome blending | 4 |
| Hydrology and caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Tests/benchmarks/docs | 7 |
| Polish/tooling | 4 |
| **Total / average / tier** | **50/100; 5.0/10; Tier 2** |

**Threshold strictly >8.5 is unproven.** No new playable V8 or rendering capability credit from opt-in graphs or unused temporal SDKs.

## Gates not run / next priorities

Java25 toolchain and owner archive absent in this run; complete private checkout and DNS unavailable in scratch container. Under October 27 owner-build override, **no Gradle/NeoForge, build/JAR, outer/nested archive CRC, native Minecraft save/reopen/shutdown, Windows/AMD8060S Vulkan/OpenGL, timing/VRAM, or real ocean/river/water-constituent acceptance**. No new qualified JAR, stable release, CurseForge/GitHub Release or retention mutation.

Next: reconcile the two transition-band sea-plane changes with the authoritative native ocean mask and routed hydrology; native saved-authoritative V8 registration; finite-water save/unload/reopen conservation; real LOD fault cleanup and Vulkan frame-time telemetry. The Dev61 terrain scoped lease was released; renderer claim released; no builder resources touched. Keep independent auditor enabled.
