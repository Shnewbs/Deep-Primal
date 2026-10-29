# Expanded replica — docs/renderer-jams/20261027-quad-bounded-upload/TESTS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-quad-bounded-upload/TESTS.md) |
| Source identity | git: docs/renderer-jams/20261027-quad-bounded-upload/TESTS.md |
| Preserved source SHA-256 | `040cc5d2a1992e5eee7dc31d898cca1c238c6e7eea62eea428af2648ec547e7d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Defines an off-by-default 32 KiB canonical-quad splitter that preserves vertex/material/water/index order and payload bytes while rebasing indices. Preflight caps emitted parts per detail level; unsupported shapes pass through. Standard 94,208-byte parts already fit the normal upload allowance.

## Research expansion — gaps and details to deepen

- Smaller parts can increase draw and descriptor overhead without reducing total payload. The experiment needs a complete page-publication and GPU lifetime comparison, not only exact CPU geometry or an assumption that the default path needs splitting.

## Research expansion — proposed contracts

- State the supported topology and immutable-input preconditions, failure-atomic output publication, part-count ceiling, and fallback on unsupported or cancelled work. Measure pending replacement overlap and preserve conservative bounds and transparent ordering.

## Research expansion — acceptance checks

- Retain exact canonical-quad and alpha/index parity checks, then compare default and split modes on identical native Vulkan/OpenGL scenes. Record page latency, actual GPU/CPU tails, draw/descriptor counts, memory peak, and water pixels before considering a default change.

## Research expansion — source anchors

- Reproduction and source identity
- In the isolated evidence workspace (with source/, stubs/, tests/):

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Reproduction and source identity

Pinned renderer code `f569a31cd3e59593c0343a8617fa0934289bb3e9`. Source helper Git blob `770dfde8609da73d3e96bdbfad8d9606cc09efda`. Reproducible test source is `LodMeshPartSplitterChecks.java` here; standalone tests use API-compatible type stubs and are **not** a full Minecraft or native GL/Vulkan validation.

```sh
# In the isolated evidence workspace (with source/, stubs/, tests/):
find source stubs tests -name '*.java' > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodMeshPartSplitterChecks
# LOD_MESH_PART_SPLITTER_PASS assertions=282240 randomizedSingle=1800 randomizedPages=300 optIn32KiB=true
# Mutation: replace 'indices[i] -= firstVertex;' with 'indices[i] -= 0;' in private copy
# Test fails with ArrayIndexOutOfBoundsException, exit 1; not committed.
```

`LodMeshPartSplitterBench.java` is in the external source-evidence archive. It reports a CPU-only Java21 component profile with 400 measured samples and 800 warmups: 94208 source bytes, 3 output parts at 32KiB, p50 34883ns, p95 76335ns, p99 183867ns, maximum 1461268ns. No GPU/whole-frame metric follows.

Toolchain preflight: owner Temurin 25.0.5+8 archive and installation path absent in this run; observed Java/javac 21.0.12.1. No Java25/Gradle/full build, package, Minecraft or GPU execution. 


<!-- END PRESERVED SOURCE -->
