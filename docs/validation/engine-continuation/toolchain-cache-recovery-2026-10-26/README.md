# Private tool recovery and permanent cleanup

- Base: 0807eccf4f5e986fb71cab7a7b40bc6247fcb7ab, preserving the concurrent renderer claim.
- Consumed independent report: docs/engine-hourly-audits/2026-10-26T2232-4a8b0fb/REVIEW.md, pinned 4a8b0fb52e7473a816655096c42f089dd340e735.
- Last full external grade remains 5.0/10. The audit reproduced the prior staged cursor repairs; it did not provide a fresh complete regrade.

## Change

tools/restore_validation_toolchain.py now accepts an explicit read-only archive cache. It validates pinned size/SHA256 before copying and again before extraction. Both cache and network transfers enforce the pinned byte limit and a 300-second transfer budget. Missing or corrupt cached assets fail without network fallback.

--remove-archives removes only the private copied/downloaded archive after successful extraction. Required standalone agent dependency JARs remain. Failed extraction retains its diagnostic archive. Existing output directories remain rejected by the CLI; individual copies use exclusive creation. Verify-only cannot copy or remove archives.

## Validation and cleanup

Eleven offline regressions passed in normal Python, -O and -OO (33 executions). They cover cache-only recovery, size/hash corruption, post-copy revalidation, existing-file protection, traversal, dependency retention, missing cache, transfer limits/deadline and tar extraction cleanup.

The final source restored all five pinned real assets into a fresh private output. Java25, Java21 and Gradle9.2.1 executable checks passed. The preliminary installation was permanently removed after the final installation passed. Exactly 484,338,664 bytes of redundant archives are absent from the retained installation.

A post-rename absence check detected the pinned JDK21 archive despite its earlier removal receipt. The check failed rather than accepting that receipt alone. Its size/SHA256 were reverified, no matching active producer was observed, and the archive was explicitly removed. Its origin is unestablished. Final archive absence checks passed. See cleanup-receipt.json for the complete checks and asset identities.

The completed auditor archive source was read-only. No other writer checkout, cache or output was deleted. Active work, useful dependency caches, approved resources, saves/player edits, uniquely needed evidence and the newest three testing JARs remain protected. Permanently remove obsolete builder exports and reproducible intermediates only after source/evidence preservation; do not apply a broad age-only deletion rule.

## Open gates

This is validation tooling, not a new game runtime or a geographic/water promotion. Production remains 0.0.1-alpha.29-dev.31. No new builder JAR or release was produced. Full Java25 game build, module/core/package gates and staged hydrology packaged native/save/reopen/rendering gates were not run in this recovery cycle. Windows/AMD8060S, real Vulkan/OpenGL temporal water continuity, multiplayer/soak and named-hardware performance acceptance remain open.

Next: restore an isolated project/dependency setup, qualify the combined staged hydrology index/cursor repairs, then continue conservative loaded/unloaded water ownership. Writer lease released at checkpoint; the existing builder remains required while engine and water acceptance are incomplete.
