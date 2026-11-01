# Dev156 — water corner read-predicate admission

**UTC:** 2026-11-01 16:03–16:07. **Audit consumed:** `docs/engine-hourly-audits/2026-11-01T1530-dev155/REVIEW.md`. **Writer scope:** `WaterCornerPacket` predicates, one new CoreChecks regression, this report and the bounded writer lease. No changes to physical fluid, native Vulkan, terrain geography, saved formats, upscaling or renderer-owned files.

## Reproduced defect and correction

Dev155 correctly sanitized invalid water corner directions 17–31 to still water (ID 16) in public packet writes. But `WaterCornerPacket.changedCorners()` and `matches()` still compared raw malformed directions against stored normalized directions. Repeated malformed requests could therefore report a changed corner even when the stored corner was already the correct still-water state, causing avoidable packet/GPU publication work.

- `changedCorners()` now clamps five-bit directions 17–31 to 16 before comparing; `patchCorners()` already used that rule.
- `matches()` now maps out-of-range signed integer directions to 16 before comparing; `corner()` already used that rule.
- Ordinary directions 0–16 retain the existing comparison logic. The 100 ms expiration/retirement semantics are unchanged.
- This is a correctness and potential unnecessary-work fix. **No live frame-time/FPS improvement has been measured.**

## Reproducible local validation

- `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **PASS**.
- **4,307,250 assertions passed**, across 50,000 malformed randomized quads, 50,000 valid randomized quads, 60 targeted invalid five-bit lane cases and 28 signed scalar extremes.
- Assertions cover read-only predicate identity, independently calculated changed masks, four-corner/four-vertex target/source isolation, opacity preservation, scalar admission, stable repeated requests and existing expired-transition exceptions.
- Two original-method negative controls **failed as expected**: unnormalized bulk predicate at assertion 87; unnormalized scalar predicate at assertion 21096.
- Committed regression `WaterCornerPredicateAdmissionChecks.java` Git blob `a58c8bedaae9a8dff3ba81b85a5c9318bbf840eb`, identical to locally tested file.
- Committed production `WaterCornerPacket.java` Git blob `336d0e83b9a150286c040a8780b787e192143d27`; `CoreChecks` registration Git blob `555a6b93a205d3c0d8ddf695fb2206da604eebd2`.
- Local evidence ZIP `DeepPrimal_dev156_corner_predicate_evidence.zip` SHA-256 `6a400eb4fbb4bf4afe5b007f87ab6c491966d16dd7128b2581fb96982b371dea`, archive integrity checked.

**Evidence boundary:** the local compiled production codec is a dependency-free source excerpt based on the prior dev154 evidence and the relevant current production methods, not the entire `WaterCornerPacket.java` or NeoForge mod. The committed test itself was verified byte-identical against the locally tested source. This does not qualify a playable JAR.

## Build and remaining gates

The local runtime has Java21 but not Java25, a complete private repository checkout or resolved Gradle/NeoForge dependencies; direct GitHub DNS resolution fails. The connected GitHub integration can publish and re-fetch source but cannot replace a full local build. No GitHub Actions were used. **No playable JAR or JAR SHA-256 was produced; previous qualified JARs were not deleted.**

Historical whole-engine grade remains **5.0/10**; this isolated regression does not constitute a regrade. Next priorities: restore full Java25/Gradle build and qualify a JAR, then native V8 water conservation/save/reopen, terrain-border checks and Vulkan/LOD hardware acceptance.
