# Expanded replica — docs/engine-stage2-2026-10-24/runners/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-stage2-2026-10-24/runners/README.md) |
| Source identity | git: docs/engine-stage2-2026-10-24/runners/README.md |
| Preserved source SHA-256 | `ad2d77d5551fd4906012a6795849d1ce0021776267472a45a67f01a9cf025116` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Reproduction”. Its original opening states: “Copy these scripts into a private task root containing density-stage2/ (this checkout), project/ (released alpha.28), toolchain/ (the pinned private JDK25/JDK21/Gradle/cache/compatibility helper) and the previous fresh-compile.gradle. Run build_density.py, verify_density_artifact.py linux, then run_density_native.py candidate-stage2 sequentially. The native runner refuses to overwrite evidence/run directories and requires codec, selected-JAR class identity, normal save and shutdown gates.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityEvidence.java`.

## Research expansion — gaps and details to deepen

- Treat restoration, test harnesses, and output paths as independently validated infrastructure so they cannot manufacture product success or failure.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define terminal process status, complete output set, bounded protocol/deadline behavior, and unconditional failure checks.
- Record environment repairs separately from product changes; retain original failure logs and exact fixture/tool revisions.

## Research expansion — acceptance checks

- Run the harness against deliberately failing, truncated, stale, timed-out, and success-marker-only producers.
- Verify wrong request IDs, malformed frames, dangling output paths, missing required inputs, and cleanup after cancellation.

## Research expansion — source anchors

- Reproduction

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Reproduction

Copy these scripts into a private task root containing `density-stage2/` (this checkout), `project/` (released alpha.28), `toolchain/` (the pinned private JDK25/JDK21/Gradle/cache/compatibility helper) and the previous `fresh-compile.gradle`. Run `build_density.py`, `verify_density_artifact.py linux`, then `run_density_native.py candidate-stage2` sequentially. The native runner refuses to overwrite evidence/run directories and requires codec, selected-JAR class identity, normal save and shutdown gates.

Use `./gradlew :densityBenchmark` in an ordinary Java25 environment. Retain three fresh-JVM timing runs with no competing work. This checkpoint's exact timed source is preserved in `timed-source/DensityBenchmark.java`. The current source adds an `error` argument to run the broader four-seed/16-site approximation diagnostic. To execute that directly, use the compiled core and noiseBenchmark class directories as the Java classpath and invoke `com.shnewbs.deepprimal.benchmark.DensityBenchmark error`. The expanded diagnostic was compiled with javac `-g` after the full build; it does not enter the mod package.

Compile the private `DensityEvidence.java` against core classes, then run `DensityEvidence fingerprint` under ordinary Java, `-Xint` and `-XX:UseAVX=0`. Compare exact output. Its optional CSV preview mode is an exporter, not a claimed executed native or visual test in this checkpoint. Private tools and evidence exporters are never mod payload.


<!-- END PRESERVED SOURCE -->
