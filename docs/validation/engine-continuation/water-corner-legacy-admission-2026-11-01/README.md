# Dev154 — legacy water corner packet admission

**UTC:** 2026-11-01 15:05. **Consumed independent audit:** `docs/engine-hourly-audits/2026-11-01T1430-dev153/REVIEW.md` (source head `52fa24e46712db37faa81abd49638e9caf9e5aa8`). **Writer scope:** `WaterCornerPacket.fromLegacy`, one new core regression, registration, this report and bounded lease only. No other engine, renderer or auditor files modified.

## Confirmed issue and change

The legacy GPU corner conversion read each direction from a five-bit lane (0..31) and directly constructed base-289 digits as `target*17 + source`. Invalid directions 17..31 can produce digits larger than 288, carrying into a neighboring corner and changing its decoded direction. Nonfinite legacy float packets could likewise decode arbitrary directions. The new admission clamps invalid lanes to still-water ID 16; if either legacy float packet is nonfinite, all four pairs become still-to-still. For valid 0..16 directions, the original exact packet encoding is preserved. The change does **not** modify water volume, world saves, native Vulkan shader code or water animation timing.

## Local verification (isolated Java21)

- `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **PASS**.
- **7,102,787 assertions** passed across 100,000 valid packets, 100,000 randomized five-bit malformed packets, 60 targeted invalid lane cases, and six nonfinite input cases (NaN, ±Infinity on each input).
- Valid packet output is byte-identical to the original conversion oracle; direction/age, nonmaterial bytes and opacity are checked.
- Negative control with the original conversion method **FAILED as expected**, exit 1: `invalid target or corner carry at 0 target=25 check=3500001`.
- Committed regression Git blob: `e3cad80ce745ca8dd25b6e5a26fc256f54ea9380` (byte-identical to the local tested test file).
- Production source Git blob after patch: `c35b0849354b124e3e58caa09695d0b91c060c3a`.
- Local evidence ZIP: `DeepPrimal_dev154_legacy_corner_admission_evidence.zip`; ZIP integrity checked; SHA256 `806b59718d6ce1a78532d807fefa8e3b0183f7f9db458004ee48b0ff99d8f1ae`.

**Scope limitation:** local production codec is a dependency-free extraction, not the complete `WaterCornerPacket.java` class. This is an isolated Java source regression, **not** a complete Gradle/NeoForge, native shader, GPU or live-world test. The committed full source and test were re-fetched from GitHub for blob verification.

## Build and acceptance

- Local runtime has Java21, not Java25; the complete private repository checkout, Gradle/NeoForge caches and native GPU test target are unavailable. Local Git cannot resolve GitHub DNS; connector source writes work.
- No complete NeoForge JAR was built. **No playable JAR or JAR SHA256 can be claimed.** Existing qualified JARs were not touched or deleted.
- No GitHub Actions used. Independent historical whole-engine grade remains **5.0/10**; this targeted test does not regrade the engine.
- Next: restore Java25/complete offline Gradle checkout and build a qualified JAR; then native V8 water save/reopen conservation, terrain-border and Vulkan/LOD hardware acceptance.
