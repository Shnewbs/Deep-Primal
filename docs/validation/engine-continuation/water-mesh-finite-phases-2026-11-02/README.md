# Dev210 — Water mesh finite-phase admission (2026-11-02 19:00 UTC)

## Coordination and scope

Read independent Dev209 audit at `docs/engine-hourly-audits/2026-11-02T1830-dev209/REVIEW.md`. Starting `main` was `2f9dc0aa02899490afe22c76c6d2ccf9f736af3a`; the shared engine writer lease was `completed-released`. Acquired the bounded Dev210 lease before source edits. Owns only `WaterMeshShape.java`, a dedicated `WaterMeshShapeFiniteChecks.java`, its `CoreChecks` registration, this evidence and the lease. No native finite-water accounts, terrain, V8, LOD, renderer ownership, saved data or Actions changed.

## Defect and correction

Water mesh displacement offsets and waterfall-flow UV phase are shader-facing. With malformed NaN/Infinity coordinates or extreme finite values whose intermediate phase sums overflow, `Math.sin`/`Math.cos`/fractional phase previously returned NaN. The exact original source blob `63ba094ae4e820c008ca2c4f0017364f783588f8` reproduces this failure. The correction checks only the computed phase values, returning neutral zero offset/UV for nonfinite phases; ordinary finite phases retain the exact historical arithmetic and rounding. This is a defensive presentation fix, **not** a correction of native water quantities, actual in-game waterfall flicker or Vulkan pixels. The `x()`/`z()` absolute-position helpers still require valid representable world coordinates from the caller.

## Reproducible isolated validation

- OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: passed.
- Dedicated regression: **303,003 checks** — 1,000 extreme/invalid coordinate triples × three finite/bounded properties (3,000), 100,000 seeded ordinary triples × three **bitwise exact** legacy parity checks (300,000), three deliberate finite-intermediate-overflow checks.
- Existing water mesh shape test reconstructed with the same assertions: **274,625** grid samples and **64** waterfall side-UV cases passed. The evidence copy is a faithful assertion-level reconstruction, not claimed to be byte-identical to the repository test blob.
- Negative control: the exact original production source (`git hash-object` equals the GitHub original blob) compiles but fails the dedicated regression at assertion 1 (`offset X finite/bounded`), as expected.
- Production candidate and dedicated test Git blobs were fetched back from GitHub and match local Git object hashes.

## Acceptance boundaries

No full Java 25 Gradle/NeoForge build or playable JAR: this local environment has only Java 21, no full private checkout, no Gradle/NeoForge cache, and GitHub DNS is unresolved for local Git. No native waterfall pixel/frame-time measurements, GPU device acceptance, full coreChecks suite, save/reopen or independent whole-engine regrade. The carried whole-engine grade remains **5.0/10 historical**. Preserve the previous verified JAR until a fully qualified replacement exists. Next run should restore the Java 25 production build and test waterfall continuity in-game.
