# Combined overflow candidates: isolated core package gate

Source: `ec434fa97b790bd2ac1677184882c5f281666e42` (dev.21 plus concurrent texture candidates preserved).
Consumed audit: `docs/engine-hourly-audits/2026-10-25T2155-19491a1/REVIEW.md`, pinned to `19491a10187e679eebf5731aa6fc3bd8079ce786`.
Last external score: 50/100, unchanged; no full regrade.

## Actual change

New reusable `run_package_checks.py` compiles product core separately from all private fixtures, builds a core-only JAR, checks its CRC and every packaged class against compiler output, rejects fixture leakage, and executes both overflow regressions and the complete CoreChecks suite with product classes available only through that JAR. New `PackagedOverflowChecks.java` verifies both candidate classes' actual code-source location before invoking the regressions. An intentionally shadowed baseline ByteLru is rejected before tests, proving that this location guard can fail.

No new runtime fix is claimed: this combines the existing unpromoted ByteLru and finite DensityGrid repairs unchanged. All 111 current production/check input files match Git blob identities in inputs.json. Four candidate/fixture files separately match their current committed Git blob identities in overlays.json. The two staged repairs replace their production counterparts only in this isolated compile. R57/R60/R61/R62 overlays remain excluded. Production code/version, frozen geography, owner art, quantities and saved profiles are unchanged.

## Reproduction

Use a checkout of the source SHA, a Java25 JDK, a nonexistent output directory, and Linux/POSIX. Copy the four files named by overlays.json beside this script from their committed paths; verify their Git blob hashes. Then:

```sh
python run_package_checks.py /path/to/pinned-checkout /path/to/jdk25 /path/to/new-output
```

The script verifies all 111 input hashes before compilation. The candidate core JAR is a private JVM validation artifact, **not an installable Deep Primal JAR**; it contains neither Minecraft adapter/modules nor platform dependencies. It must not be uploaded as an owner mod build or substituted into an old release. The evidence result identifies its bytes and SHA256 for reproduction only.

## Checks and limits

See result.json, regression.log, core.log and shadow-rejection.log for recorded outcomes. Both regressions run against the same compiled/packaged candidate core: 3,379,936 ByteLru oracle assertions and 11,728 finite-grid checks. The deliberately shadowed ByteLru run is an expected failure, not a candidate failure. Class hashes and archive CRC are checked before the JVM runs; the output archive has no concurrent producer. Incidental core timing lines are not controlled benchmarks.

The previous independent audit's full dev.21 Gradle/native passes apply only to its pinned unmodified runtime; they do not qualify these overlays. No full Gradle/mod-module/adapter compilation, nested mod-archive qualification, native Minecraft save/reopen/shutdown, graphics, controlled performance or Windows/AMD8060S test ran here. No installable testing JAR or release was produced. Zero free overlay storage and approximately 100MB free shared memory prevent restoring an independent full build workspace without touching other writers' resources.

Next: restore independent full-build capacity; apply both candidates with a version beyond the then-current production version, integrate their regressions into the build, and run full module/package/native qualification before promotion. Then resume registered V8 topology/profile/water integration, opt-in channel4 footprint/flow/visual coverage and water/LOD performance gates. This core package checkpoint does not close those gates.

Coordination: Git writer lease expired, timing reservation completed, no Java/Gradle process observed at acquisition. Personal Context yielded no reliable live writer claim; auditor run records show completion at 22:11:35Z. A bounded main writer lease was acquired using an expected-parent update. Work used an isolated memory-backed directory; other checkouts/caches/outputs were not edited. The new texture commit is preserved byte-for-byte.
