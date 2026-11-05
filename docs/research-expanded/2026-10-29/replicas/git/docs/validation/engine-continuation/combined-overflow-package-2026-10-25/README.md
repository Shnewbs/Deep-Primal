# Expanded replica — docs/validation/engine-continuation/combined-overflow-package-2026-10-25/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/combined-overflow-package-2026-10-25/README.md) |
| Source identity | git: docs/validation/engine-continuation/combined-overflow-package-2026-10-25/README.md |
| Preserved source SHA-256 | `229d44030e9fda2600a4afa19655394f4c0c6e44b92fa769cf827a647647a9d8` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Isolated packaging of the combined ByteLru and density-grid overflow candidates with product classes available to regressions only through the built core JAR. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that archive and compiler-class identity checks qualify that isolated core package, not the full mod, native saved worlds or rendered acceptance. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an explicit product-versus-fixture classpath manifest and a negative shadow-class check for every packaged regression family. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Deliberately shadow a packaged class and require rejection, verify CRCs and product hashes before execution, and preserve both overflow oracle results under the same candidate identity. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Combined overflow candidates: isolated core package gate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
