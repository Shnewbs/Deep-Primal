# Expanded replica — docs/renderer-jams/20261026-candidate-runner/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-candidate-runner/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-candidate-runner/BACKLOG.md |
| Preserved source SHA-256 | `a98180a8167402637b757b8a548701abcabad7235e7f831daeee587ee8d16af3` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Introduces a reproducible source-candidate runner using pinned Git blobs, explicit hashes, isolated temporary outputs, and success only after compilation, process exit, and expected marker checks. Negative wrong-hash cases are retained; runtime source and distributable artifacts are unchanged. Use the runner for a defined current candidate and link its later integration receipt; avoid repeating source-only evidence as release qualification.

## Research expansion — gaps and details to deepen

- The runner proves identity and focused execution, but needs a complete environment and dependency receipt to make results comparable across compiler versions and classpaths. A passing marker cannot stand in for product integration or hardware exercise.

## Research expansion — proposed contracts

- Record JDK/compiler arguments, classpath artifact hashes, fixture entry point, enabled assertions, exit status, and source inspection scope. Preserve fail-closed result initialization and path containment; identify stand-ins separately from actual production classes.

## Research expansion — acceptance checks

- Reject changed source bytes, missing classes, disabled required assertions, misleading output markers, and compile/runtime failures. Reproduce a passing run in a fresh isolated output and require a separate exact-artifact native gate for rendering claims.

## Research expansion — source anchors

- Renderer backlog after candidate-runner jam

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after candidate-runner jam
Production parent 38a140431881cdb49a4287e48fc84a6d904e7e9b; runtime dev.31 unchanged. New runner SHA256 8218955d1677aaf5467dcf944068d92a60684c631400d71cebfa839a49242c70; candidate remains4c8d6b60a71c1021d245df622105b1d1a8a4f0bd0dc58fd2a205f83c9be85eec.
1. Restore independent full-build capacity; run retained tool with pinned manifest/candidate/check, integrate candidate only after normal Gradle/module/package/native qualification.
2. GPU camera/travel/edit and live discovery/capture/upload readiness, native handoff/cache/edit races, lifecycle and water ordering.
3. Full CPU/GPU frame/pipeline/memory baseline; current fresh allocation is616–624B/query to zero in staged fixtures, not shipped FPS/RAM/VRAM credit.
4. Bounded mesh/upload reuse, hierarchical visibility/refinement/coverage and streaming/invalidation.
5. Unseen-terrain delivery contract and generator/network coordination; independent persisted/effective full/vertical/LOD/simulation budget controls.
6. Official FSR/DLSS provider/SDK/licensing/render inputs/lifetimes/hardware validation; frame generation distinct and later.
All scores carried [4,6,4,0,4,4],mean3.67,delta0. No new binary. Consume REVIEW and result.json before next attempt.


<!-- END PRESERVED SOURCE -->
