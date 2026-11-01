# Dev155 — independent Deep Primal audit and repair

**UTC:** 2026-11-01 15:30–15:36. **Prior audit head:** `52fa24e46712db37faa81abd49638e9caf9e5aa8`. **Builder head reviewed:** `e19064d0e9930b2b521646f1ab7c347bea7b270d`. **New Builder commits:** 6. The Builder's bounded writer lease was `completed-released` before the independent correction; no concurrent writer claim was overwritten.

## Builder commit review

Builder Dev154 repaired legacy GPU water-corner conversion: previously malformed five-bit directions 17–31 could exceed a base-289 digit and corrupt adjacent corners. It added `WaterCornerLegacyAdmissionChecks`, registered it in `CoreChecks`, documented its isolated Java21 test evidence, and released the writer claim. The Builder reported 7,102,787 isolated assertions; this audit inspected the source and evidence report but **did not independently rerun that entire suite**.

## New independently reproduced defect and correction

The same class exposed two additional public mutation paths which still admitted invalid directions:

- `WaterCornerPacket.corner(..., to, from, ...)` directly inserted unbounded integer directions into a base-289 corner digit.
- `WaterCornerPacket.patchCorners(..., targets, sources, ...)` inserted five-bit 17–31 directions into base-289 digits without admission.

**Reproduction:** with all corners initially direction pair 1:1, setting corner 0 to 31:31 using the original bulk formula changed neighboring corner 1 from **1:1 to 1:2**. This is a real packet correctness defect for malformed caller inputs, not proof of a live-game occurrence.

**Correction:** `corner` converts out-of-range signed integers to still-water direction ID 16; `patchCorners` caps each five-bit direction at 16 before encoding. Valid 0–16 values remain byte-identical. A new committed regression exercises both APIs, all four corners, all four vertex packets, neighboring-corner isolation, opacity, and direct-vs-bulk packet parity.

- [Production fix](https://github.com/Shnewbs/Deep-Primal/commit/40ac20d444036303cb651dc640a80b91175bd195), blob `e2dad83424bb98a9f133035e42dccbd95f56a44d`.
- [Permanent regression](https://github.com/Shnewbs/Deep-Primal/commit/cf251583c729ff6e17d748d3d8ed67c50253f73e), blob `07f5f5358f0ae6355f0db199e724955bef5d640a`.
- [CoreChecks registration](https://github.com/Shnewbs/Deep-Primal/commit/1be0961e43d62d3052ae479d5d4cc2ab3112429e).

## Independent validation

- `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **PASS**.
- **760,252 assertions** passed on dependency-free exact production packet/encoding/mutation method excerpts, including 10,000 randomized quad mutation cases.
- **3,313,078 independent arithmetic oracle assertions** passed across 100,000 randomized sets and extreme signed direction inputs.
- Original implementation negative control: **FAILED as expected**, exit 1 at `corner target corner=0 vertex=0 check=1`; separate original formula reproduced adjacent-corner carry (1:1 → 1:2).
- Corrected source, test, and CoreChecks registration were re-fetched and verified by GitHub blob SHA.
- **Limit:** isolated Java21 tests on exact relevant source-method excerpts, not compilation of the complete class or a full NeoForge Gradle build; no native shader, saved-world, terrain, or GPU acceptance.

## Build, compatibility, and release gates

| Gate | Status |
|---|---|
| Six Builder commits reviewed | Complete |
| New malformed-corner defect reproduced and repaired | PASS, isolated |
| Regression and registration on `main` | Verified |
| NeoForge target | `26.1.2.122` in `gradle.properties`; not runtime-tested |
| Java/Gradle declared | Java25 toolchain; Gradle 9.2.1 wrapper |
| NeoForge 26.3/26.4 | Not validated; no compatibility claim |
| Native water conservation / V8 saved-world reopen | Not tested |
| Terrain seams / async worldgen | Not tested |
| Vulkan/OpenGL / LOD / upscaling / FPS | Not benchmarked |
| Full Java25 NeoForge playable JAR | BLOCKED |

**Packaging blocker:** isolated runtime has OpenJDK21, no JDK25, no complete private repository checkout, no resolved Gradle/NeoForge cache, and Git CLI cannot resolve github.com. GitHub connector can commit source but cannot substitute for a complete local build. **No playable JAR or JAR SHA was produced; no existing JAR was deleted; no GitHub Actions were invoked.**

**Grades:** scoped public water-corner mutation admission **8.5/10 provisional** (source-level); historical whole-engine baseline **5.0/10**, not freshly regraded. A full engine grade requires real native/runtime measurements.

**Evidence:** `DeepPrimal_Dev155_IndependentAudit_20261101T1530_Evidence.zip`, 7,766 bytes, ZIP integrity verified, SHA256 `06ee92523b1c8d7b7e03eafd5f3489be5f872cc997959e165fe6df74d95e419d`.

**Next:** restore complete checkout, JDK25, Gradle dependencies; build/verify a fresh playable JAR with SHA256; only then retire a superseded audit JAR. Prioritize V8 water conservation/save-reopen, terrain-border and Vulkan/LOD hardware acceptance.
