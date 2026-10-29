# Expanded replica — docs/engine-review-2026-10-24/runners/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-review-2026-10-24/runners/README.md) |
| Source identity | git: docs/engine-review-2026-10-24/runners/README.md |
| Preserved source SHA-256 | `23886d32c7480b1793c5d85ce625b167088f53f6ae58ae08620ce77b5d0193d2` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Exact isolated audit runners”. Its original opening states: “These scripts retain the executed commands. Copy them into a task root with engine-review/ (the development checkout), project/ (the alpha.28 checkout containing its accepted Linux JAR), toolchain/ (the pinned JDK25/JDK21/Gradle/cache/compatibility helper), and fresh-compile.gradle from the previous release's validation runners. They intentionally create new evidence/native-run directories and refuse to overwrite prior runs.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Preserve the measurement boundary and avoid promoting component allocation or microbenchmark results into full-engine performance claims.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Record workload, source identities, cold/warm/cached state, process forks, output equality, actual device/backend, and instrumented-versus-release mode.
- Publish raw distributions and separate temporary allocation, resident memory, GPU residency, throughput, and end-to-end latency.

## Research expansion — acceptance checks

- Repeat matched baseline/candidate workloads with alternating order and checksums; investigate differences before interpreting timing.
- Measure the actual native load-to-ready or frame path and report p50/p95/p99 with sample counts and failures.

## Research expansion — source anchors

- Exact isolated audit runners

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Exact isolated audit runners

These scripts retain the executed commands. Copy them into a task root with `engine-review/` (the development checkout), `project/` (the alpha.28 checkout containing its accepted Linux JAR), `toolchain/` (the pinned JDK25/JDK21/Gradle/cache/compatibility helper), and `fresh-compile.gradle` from the previous release's validation runners. They intentionally create new evidence/native-run directories and refuse to overwrite prior runs.

Run `build_review.py`, then `run_worldgen_benchmark.py baseline-1`, `baseline-2`, `candidate-1`, `baseline-profile` sequentially. Run `run_noise_benchmark.py` after all native producers finish. The profile run uses JFR and is separately attributed. No competing build or timed workload should run during a benchmark. The reproduction uses the repo's current-proxy helper for this managed environment; normal setups can use the Gradle commands in the parent README. No credentials or tool binaries are embedded.


<!-- END PRESERVED SOURCE -->
