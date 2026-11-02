# Dev192 — legacy native-water volume admission

**UTC cycle:** 2026-11-02 10:00. **Pinned source:** `970466f6e0f4752a213fc7f274fbc4f9694fdc6e` before the scoped writer claim `c8e19538d2041455544a30f5c9d94f515e2dc010`. **Consumed independent audit:** `docs/engine-hourly-audits/2026-11-02T0930-dev191/REVIEW.md`.

## Change and risk

The legacy `FluidVolume` 8-unit transfer helpers (`down`, `sideways`, `pressure`) previously accepted negative or over-capacity source/target amounts. A malformed amount could produce a transfer larger than one native water cell, or silently mask corrupt water state. Add shared fail-closed range admission at all three public transfer entry points. This does not change any valid 0..8-unit transfer or saved-world format. The newer 16-unit `WaterTransport` and the independently audited `WaterSimulation` are untouched. No in-game manifestation of this defect has been established; this is an explicit API-boundary hardening correction.

## Executed verification

- Java 21 `javac --release 21 -Xlint:all -Werror` compiled the exact proposed production class and the new independent test class. `java -ea -Xverify:all` passed **376,053 assertions**.
- The test covers every 0..8 donor/receiver pair, eight adversarial elevation endpoints in both positions, and **120,000 seeded full-int-coordinate pressure cases**. An independent `BigInteger` oracle verifies exact head arithmetic, bounded transfers and conservation.
- Seven malformed native-volume pairs, including negative and signed-int extremes, are rejected by each transfer entry point.
- Original production blob `65ad5c85173976553caf3029826b90edc3fc0fda` compiled with the new regression but failed on the first invalid pair (`-1,0`), exit code 1. All preceding valid-range cases passed.
- `HydrologyChecks.run()` registers the new regression, making it reachable from `CoreChecks` on a complete checkout.
- Proposed production SHA-256 `6943e27f56d69c2694926dd76b1c990f8c2b8ee79cecc4ab7af4140c651a7f5a`; test SHA-256 `12f656e436dc76c3bc0640960601b9e8fd3a201ce06e465315ca561da118836c`.

## Missing gates

No full Java 25 Gradle/NeoForge build, packaged client/server smoke, native water gameplay replay, renderer/Vulkan/LOD benchmarks or qualified JAR. The execution environment has Java 21 only, no complete private checkout or cached Gradle/NeoForge dependencies, and local GitHub DNS resolution is unavailable. GitHub Actions were not used. Preserve the previous successful JAR until a verified replacement exists. Whole-engine historical grade remains **5.0/10**, not regraded.

**Next:** restore a complete Java 25 build environment; run full `CoreChecks`, native water transfer/save/reopen acceptance, then benchmark any admission overhead and continue outstanding water/LOD/Vulkan gates.
