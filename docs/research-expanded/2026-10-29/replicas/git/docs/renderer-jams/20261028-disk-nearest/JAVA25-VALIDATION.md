# Expanded replica — docs/renderer-jams/20261028-disk-nearest/JAVA25-VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-nearest/JAVA25-VALIDATION.md) |
| Source identity | git: docs/renderer-jams/20261028-disk-nearest/JAVA25-VALIDATION.md |
| Preserved source SHA-256 | `a59585e5b4d5db7bca6c7caa36cfcd27f4899d874a3e45edd8ae26c24ffcae30` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Selects the closest 8,192 canonical persisted pages with exact signed-coordinate ranking instead of keeping the first directory entries. The Java25 addendum reruns the same pinned scanner source and records a warm-directory CPU diagnostic without changing product code.

## Research expansion — gaps and details to deepen

- This historical all-in-one scan is superseded by the cursor and immutable-admission repair. A stronger compiler run does not repair an integration contract outside the scanner, and warm-directory latency cannot be compared directly with unrelated Java21 runs or game frames.

## Research expansion — proposed contracts

- Add explicit superseding links and separate helper correctness, renderer consumption, incremental work, and native visible restoration. Record filesystem/cache condition, directory size, retained cap, JDK, and exact source/test hashes for every scanner measurement.

## Research expansion — acceptance checks

- Retain 9,006 real-directory assertions under both compiler targets, then verify actual immutable result admission and bounded cursor integration. Exercise late nearby files and fixed-camera refresh in the native artifact before claiming useful far-terrain recovery.

## Research expansion — source anchors

- Java25 focused scanner regression

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Java25 focused scanner regression

Source code remains pinned to [89b8bb0](https://github.com/Shnewbs/Deep-Primal/commit/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb) without product changes. After the initial renderer self-audit, the owner's Temurin 25.0.5+8 Linux archive was materialized from the Library into this jam's isolated workspace, verified against the expected archive root and safely extracted. java and javac report version 25.0.5+8.

Both --release 21 and --release 25 compilation with -Xlint:all -Werror passed. Both executable scanner regression tests passed 9,006 real-directory assertions. A separate 21-run warm-directory Java25 diagnostic (8,206 filenames, at most 8,192 retained) reported p50 8.237ms, p95 13.074ms and p99 13.456ms. No native game, current Gradle project, JAR, GPU/fence, full-frame or other hardware acceptance ran, so no new performance grade.

**Library artifact:** DeepPrimal_Renderer_DiskNearest_Java25_20261028_89b8bb0_Evidence.zip (2,348 bytes; SHA256 40e0d5e67c003689593c5d5776c0a5523d5ec36de0cd47415a43ed4f4aa4615b; 7 CRC-valid entries), Library file libfile_307174058df08191b9e22fa49ae86f80. The exact current-source helper/test Git blobs are unchanged and appear in the primary evidence ZIP.

Owner still handles whole Java25 Gradle, Minecraft, native Vulkan/OpenGL, Windows AMD and distribution. The dedicated renderer schedule and separate engine/auditor scopes are unchanged.


<!-- END PRESERVED SOURCE -->
