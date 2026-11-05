# Expanded replica — docs/validation/engine-continuation/toolchain-cache-recovery-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/toolchain-cache-recovery-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/toolchain-cache-recovery-2026-10-26/README.md |
| Preserved source SHA-256 | `8f7f891df8d0e37014de81acda190edc7803969701cbc49e06b268c6b6ce82fd` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Private toolchain recovery from an explicit read-only archive cache with pinned size and SHA256 verification before copy and extraction. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that missing or corrupt cache inputs fail without network fallback, and tooling cleanup does not establish full build or runtime qualification. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a reproducible recovery receipt listing immutable tool inputs, bounded transfer behavior and the exact owned archives eligible for removal. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Supply truncated, oversized and wrong-hash cache entries, require rejection before extraction, and verify cleanup cannot delete extracted tools or another writer's files. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Private tool recovery and permanent cleanup

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Private tool recovery and permanent cleanup

- Base: 0807eccf4f5e986fb71cab7a7b40bc6247fcb7ab, preserving the concurrent renderer claim.
- Consumed independent report: docs/engine-hourly-audits/2026-10-26T2232-4a8b0fb/REVIEW.md, pinned 4a8b0fb52e7473a816655096c42f089dd340e735.
- Last full external grade remains 5.0/10. The audit reproduced the prior staged cursor repairs; it did not provide a fresh complete regrade.

## Change

tools/restore_validation_toolchain.py now accepts an explicit read-only archive cache. It validates pinned size/SHA256 before copying and again before extraction. Both cache and network transfers enforce the pinned byte limit and a 300-second transfer budget. Missing or corrupt cached assets fail without network fallback.

--remove-archives removes only the private copied/downloaded archive after successful extraction. Required standalone agent dependency JARs remain. Failed extraction retains its diagnostic archive. Existing output directories remain rejected by the CLI; individual copies use exclusive creation. Verify-only cannot copy or remove archives.

## Validation and cleanup

Eleven offline regressions passed in normal Python, -O and -OO (33 executions). They cover cache-only recovery, size/hash corruption, post-copy revalidation, existing-file protection, traversal, dependency retention, missing cache, transfer limits/deadline and tar extraction cleanup.

The final source restored all five pinned real assets into a fresh private output. Java25, Java21 and Gradle9.2.1 executable checks passed. The preliminary installation was permanently removed after the final installation passed. JDK25 and Gradle redundant archives totaling 276,865,317 bytes remain absent. Persistent deletion of the 207,473,347-byte JDK21 archive is unresolved.

A post-rename check detected the pinned JDK21 archive despite its earlier removal receipt. Size/SHA256 were reverified and Python unlink and shell rm both reported success with immediate absence. It repeatedly reappeared on later independent command calls. No matching active producer was observed; cause is unestablished. The original all-archives-removed conclusion is withdrawn. Do not count the JDK21 bytes as permanently reclaimed. See cleanup-receipt.json for the revised receipt.

The completed auditor archive source was read-only. No other writer checkout, cache or output was deleted. Active work, useful dependency caches, approved resources, saves/player edits, uniquely needed evidence and the newest three testing JARs remain protected. Permanently remove obsolete builder exports and reproducible intermediates only after source/evidence preservation; do not apply a broad age-only deletion rule.

## Open gates

This is validation tooling, not a new game runtime or a geographic/water promotion. Production remains 0.0.1-alpha.29-dev.31. No new builder JAR or release was produced. Full Java25 game build, module/core/package gates and staged hydrology packaged native/save/reopen/rendering gates were not run in this recovery cycle. Windows/AMD8060S, real Vulkan/OpenGL temporal water continuity, multiplayer/soak and named-hardware performance acceptance remain open.

Next: restore an isolated project/dependency setup, qualify the combined staged hydrology index/cursor repairs, then continue conservative loaded/unloaded water ownership. Writer lease released at checkpoint; the existing builder remains required while engine and water acceptance are incomplete.


<!-- END PRESERVED SOURCE -->
