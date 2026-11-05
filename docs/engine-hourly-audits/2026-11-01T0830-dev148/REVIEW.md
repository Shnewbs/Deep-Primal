# Deep Primal Dev148 — independent audit and water quantity admission repair

Date: 2026-11-01T08:30Z. Previous independent audit head: `2edb3bb2a270c216e60cdb2fb4e36ad24b914a1a`. Builder head reviewed: `541be8540579ebea7b616fb9e0e74773ac48c2d8`. No GitHub Actions.

## Seven new Builder commits reviewed

- `809d010b`: scoped horizon writer claim.
- `335e4cd1`: reject negative horizon radii and nonfinite camera coordinates.
- `7e77b530`: independent rectangle oracle and negative control.
- `5f91350e`: register FarTerrain regression in CoreChecks.
- `d46b00d0`: standalone strict Java21 horizon runner.
- `bf89a755`: baseline reproduction, test evidence and build gates.
- `541be854`: release scoped writer claim.

The production `FarTerrain.java` blob is `fd1a247028c9a4fda99422515438027b809ea2eb` (SHA-256 `a85ba6a3ee5dabf8b8a3b4e1b8a9e006d8750c4f799ef513076dae4bcad7431a`). Independent Java21 tests passed **500,034 assertions**, including 500,000 randomized nearest-point rectangle oracle comparisons, signed tile-key boundaries, negative radii, and NaN/Infinity inputs. The uncorrected baseline fails the negative-radius test. No additional horizon defect was confirmed.

## Confirmed nonoverlapping water defect

`WaterPatchMesher.Snapshot.water()` returned the raw signed byte, without enforcing the authoritative 0..16 water-unit domain (`WaterTransport.CAPACITY`). Invalid captured values (e.g. 17, 255) could be interpreted as nonzero water or negative quantities by the mesher, risking phantom faces and malformed surface heights. The original exact-method stub failed at raw byte 17.

Fix: normalize raw unsigned values >16 to zero at the Snapshot read boundary, leaving valid values and captured arrays unchanged. This is independent of the Builder's released FarTerrain scope and complements Dev147's flow-direction validation.

- Production repair: `351242db1eb1e67421b8dbeb6595140810866800`.
- Permanent `WaterSnapshotQuantityChecks`: `125c8f0fbfa8db74359fa090d086f3f29c6bcd38`.
- CoreChecks registration: `f96c3a2f9d1cf1ea593741fd79ea8f95bd340d16`.
- Corrected production blob: `c12da5b8b2870120a9396824889408642d3c0af3`.
- Permanent test blob: `6871d0a040d5aa158e11dd424e04c160e3ae9bac`.
- CoreChecks blob: `8bd23ef76abd732a46cae70213826ba9460c8969`.

Strict isolated Java21 compilation with `javac --release 21 -Xlint:all -Werror` and execution with `java -ea -Xverify:all` passed **402,578 water quantity assertions**, including all 256 byte values at five positions, 200,000 seeded cases, source-array preservation, valid-volume identity, and legacy null-flow behavior. The original method fails at raw byte 17. Total isolated assertions this audit: **902,612**.

**Evidence scope:** The water quantity test uses an exact-method API-equivalent Snapshot stub plus the WaterTransport capacity constant, not a full production WaterPatchMesher compile. The FarTerrain source and permanent water regression file were independently Git-blob matched. Full CoreChecks, NeoForge, water simulation/persistence, GPU renderer and runtime FPS tests were not executed.

## Build and NeoForge acceptance

`gradle.properties` pins `neo_version=26.1.2.122` and `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`. Mod metadata requires Minecraft `[26.1.2]` and NeoForge `[26.1.2.122,26.1.3)`. Gradle wrapper 9.2.1; Java toolchain 25. The local environment provides Java21 only, no complete private checkout, no cached Gradle distribution/dependencies, no GitHub DNS through Git CLI, and no native GPU validation target. Consequently no full Gradle build, playable JAR, JAR SHA, or 26.3/26.4 compatibility claim. No superseded JAR deleted.

## Provisional grades

- Builder's FarTerrain admission correction: **8.5/10** (independent source-only verification).
- Water snapshot quantity boundary: **8.0/10** (strict isolated validation, full compile pending).
- Whole engine: **5.0/10 historical baseline**, not freshly regraded; no native acceptance.

## Evidence and next steps

Local evidence archive: `DeepPrimal_Dev148_IndependentAudit_20261101T0830_Evidence.zip`; SHA-256 `6f1f023f8392dbc272b57986cbe56565608a7fcf2ddd9054cf715f51a0d91b9b`. Contains independent sources, baseline failures, strict compilation/verification logs and build-gate diagnostics.

Next: restore complete Java25 Gradle/NeoForge checkout and build; verify a playable JAR and SHA before deleting any predecessor; run native finite-water conservation and V8 save/reopen tests; validate terrain seams and Vulkan/OpenGL/LOD performance.
