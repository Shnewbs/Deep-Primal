# Expanded replica — docs/validation/engine-continuation/v8-callback-prewrite-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-callback-prewrite-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-callback-prewrite-2026-10-28/README.md |
| Preserved source SHA-256 | `076d818812db50012011aea2cb6532ec8766454b0bb33b0c8ca3653d9e2c3cbe` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev53 capturing every destination block before any V8 setter so synchronous callbacks cannot corrupt future cells before their undo state is recorded. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the same-chunk snapshot can hold 524,288 block references at maximum height, and API-double rollback checks do not establish native memory or recovery acceptance. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a prewrite transaction boundary with complete snapshot acquisition, heightmap reconstruction, verification and explicit incomplete-restoration diagnostics. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Mutate a future cell from a setter callback, fail mid-snapshot before any write and fail postwrite after mutation, verifying original states and an explicit abandonment outcome if restoration cannot complete. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev53 — prewrite V8 callback-recovery snapshot (source-only)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev53 — prewrite V8 callback-recovery snapshot (source-only)

Independent audit consumed: [2026-10-28T1128-581f343/REVIEW.md](../../../engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md), pinned `581f343e460ea21ef645ece1a7f6205a4ed2fb31`. It reproduced a synchronous native-setter *test double* callback mutating a future cell before Dev52's first-touch undo recorded its original value. The original state was then lost on postwrite failure. The last accepted complete score is **50/100 (5.0/10)**, carried, not freshly regraded.

Version `0.0.1-alpha.29-dev.53-v8callback.1`. Production `DensityChunkMaterializer.java` blob `d5c2e88ac81b206c7e7ce421c32a3eb18977bf1e` (Dev52 blob `1399606ac7067a19dbf7e4529821135c161de7fa`). The experimental opt-in V8 materializer now captures every destination cell **before any setter**, so a synchronous callback changing a not-yet-visited V8 cell cannot poison its original snapshot. Rollback restores changed cells against the complete snapshot, rebuilds heightmaps, then verifies all original destination block states and attaches a diagnostic if restoration remained incomplete. No V1–V7 path, owner art, resources, saved profile, water quantities or renderer module was modified. At maximum height the snapshot holds 524,288 block references; no memory/time performance acceptance is claimed.

Focused Java **21.0.12.1** isolated Minecraft/Mojang API stand-in checks: `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`, **14,144,988 passed assertions across eight suites**, including **16,388** new callback/read-failure assertions. Deliberate unchanged Dev52 negative control **failed** at future-cell index 7169; Dev53 passed and returned all 8,192 simulated original states. Mid-snapshot read failure resulted in zero writes. Source/tests/stand-ins/script/log: conversation artifact `DeepPrimal_Dev53_V8_CallbackPrewrite_SourceEvidence.zip`, 62,751 bytes, SHA256 `f9f75631994a3aee1363c2540057e06c6353e4a839f139c04cb8c48f15171ab8`, ZIP CRC clean. The new isolated regression source is stored next to this report; it is **not** compiled by the real Minecraft build.

**Limits:** This addresses one bounded **synchronous, same-destination-chunk** callback pattern. It does not reverse asynchronous callbacks, cross-chunk edits, block entities, light engine changes, scheduled tasks or persistent writes; native callers must discard any failed chunk. The additional prewrite reads/full failure-path scan require native latency/alloc profiling. Temurin25 archive and JDK25 were absent on this execution host; full Gradle/NeoForge, native save/reopen, actual Mojang codecs, Vulkan/OpenGL and Windows AMD8060S acceptance were not run. No qualified new JAR or release. Under the October 27 owner override the owner performs those builds. Water temporal edges, exact volume/salt/sediment unload/restart, long-soak and GPU gates remain unpassed.

Next: owner native V8 callback and lifecycle validation; confirm failure-abandonment contract; performance/alloc measurement; then finite-water ownership and constituent conservation after checking foreground claims. Engine completion still requires an independent **strictly >8.5/10** complete regrade and all water gates.


<!-- END PRESERVED SOURCE -->
