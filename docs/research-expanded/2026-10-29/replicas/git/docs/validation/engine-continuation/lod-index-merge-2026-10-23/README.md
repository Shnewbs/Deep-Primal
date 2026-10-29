# Expanded replica — docs/validation/engine-continuation/lod-index-merge-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/lod-index-merge-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/lod-index-merge-2026-10-23/README.md |
| Preserved source SHA-256 | `3dd9a204e641e3315e63b963934469928fa00541689d941f7fbcdec1872123ee` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Bounded merging of an 8,192-key startup scan with existing LOD recovery keys after addAll produced 8,193 entries. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the queue-cap repair preserves existing recovery order but does not solve beyond-cap discovery or prove physical scene coverage. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a merge contract that prioritizes existing ordered recovery metadata, deduplicates new keys and admits only nearest keys fitting remaining capacity. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Seed empty, partial and full queues, include duplicates and failed futures, then assert the cap, edit-guard identity and original order in both exact-source and native tick cases. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Bounded LOD disk-index merge — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Bounded LOD disk-index merge — 2026-10-23

Continues `918675d0` on the unreleased working branch. One production LOD file changes; water runtime/core, Vulkan and frozen geography match the parent. Main and supplied alpha.24 installers remain unchanged.

## Reproduction and fix

A completed startup scan returns at most 8,192 keys, but `disk.addAll(keys)` ignored recovery keys already queued by eviction or rejected upload. An exact-source fixture and a native Minecraft tick both reproduce one existing key plus a full disjoint scan becoming **8,193** keys. The native baseline asserts and exits Gradle one; its sealed output and crash excerpt remain preserved.

The independently authored repair preserves the existing ordered recovery queue and its edit/deferral metadata, then admits nearest-first scan entries only into the remaining slots. It changes `addAll` to a loop checking the shared cap before each addition. The existing bounded scan/sort and 64-key disk-job selection are otherwise unchanged. This does not create a paged regional index, rediscover files beyond the scan's initial prefix, or solve coverage beyond 8,192 keys. Those architecture/coverage requirements remain open.

## Fresh checks

All 510 current build/validation inputs were Git-blob verified in a separate directory. Final comparison finds 508 unchanged: one production file plus ClientCapture differ, and one new opt-in validation class is added. New validation is excluded from the mod archive.

- Exact production scan-merge block: baseline fails at 8,193; candidate passes seeded queues of 0, 1, 4,096 and 8,192, duplicates, nearest-first remaining slots, existing order/edit guard identity, pending futures and failed-future cleanup. These use explicit position/future/queue doubles.
- Native development and packaged sessions execute the actual production tick with a controlled completed scan and an in-flight placeholder stopping unrelated capture. Four cases finish at 8,192; existing order/edit guards and scan cleanup pass. This is injected scan completion, not an actual 8,192-file discovery or a large-world coverage test.
- The same two sessions repeat actual 552-byte partial GPU upload cancellation beside a real 4 MiB ballast allocation, bounded same-view retry and five cancellation guards; prior persisted eviction/capacity/edit controls, real native snapshot/store revision, frame completion/open-pass checks, shader/transfer/depth/atlas/stencil/lightmap, resource reload, normal saved disconnect and injected shutdown continuation pass. Final tracked buffers/images, VUIDs and validation errors are zero. Packaged origins point to actual module JARs. Live native logs equal producer-sealed bytes and their recorded hashes; no new crashes/save failures appear in accepted cases.
- Full sealed uncached Linux Vulkan/core/module/geography build passes all required records with 25 executed tasks. Six ZIP CRCs, 407 compiled-class matches and five nested/standalone identities pass. Development JAR SHA 3184f5665ce6e362f1ba1d63de62e1318402e26520c249f7044ca36f8e134821 remains identical before/after native checks; no new installer is delivered.

Screenshots were inspected as limited actual scene evidence. Repeated terrain contours, distant page transitions and water/transparency remain open visual concerns; simulation continues between images. These checks do not establish pixel-complete coverage, total resident memory or owner GPU/FPS performance. Software Vulkan is the primary sample test API; OpenGL remains a separate fallback. Three solver substeps are not independent 60 Hz simulation. Prior marker/writer, reload/atlas stalls and direct-stop save failures remain unresolved.

## Delivery and next work

The owner requested an existing current release JAR and continued implementation toward a market-leading engine. The already supplied Windows Vulkan alpha.24 artifact was made available again; its recorded SHA is f03c7e5b864e627daa667bb45ec7c4cfc0d51a73ccd1fc9cc32e7909398e42ac. It does not include subsequent alpha.25 working-branch changes. Market leadership is an objective, not a measured claim.

Next build a bounded spatial/paged recovery index and coarser fallback with actual scene coverage assertions under travel, edits and memory pressure. Preserve player-input/server physical unload/reopen requirements, marker/reload investigations, staged drain p95/1,024 waiters/larger residency, finite/renewable regional water budgets and coherent geology. Catalog/species remains separately deferred. Keep acceptance incomplete until the agreed gates pass.

Reproduce in isolated project/ beside pinned toolchain/: run check_index_merge.py and build_candidate.py, then audit_vulkan_package.py. Use a disposable audit world and private output root with DEEPPRIMAL_NATIVE_RUN_DIR/DEEPPRIMAL_CAPTURE_ROOT. Run run_vulkan.py index-final and index-final-packaged sequentially, then audit_index.py with their capture directory names (symlink external capture roots beside the script). Respect the work-state lease. before-VoxelLodRenderer.java reproduces the native baseline.


<!-- END PRESERVED SOURCE -->
