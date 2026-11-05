# Expanded replica — docs/validation/engine-continuation/rcon-protocol-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/rcon-protocol-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/rcon-protocol-2026-10-26/README.md |
| Preserved source SHA-256 | `efd3a91d4bbd0c175aef8e85ee29b6b5fc825467adb8538f91f433fac18aece2` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repair of the native validation RCON parser for request IDs, response types, terminators, bounded frame lengths and one monotonic request deadline. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that parser tests qualify validation transport, not water runtime or a native save; toolchain restoration did not complete full project qualification. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a protocol receipt contract requiring the intended request's authenticated response and bounded connection lifetime before acknowledging save completion. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject wrong IDs, types, terminators and oversized frames, trickle bytes across the deadline, test empty authentication preambles and require failed-auth socket closure. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Native validation RCON protocol repair

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Native validation RCON protocol repair

Pinned baseline 0e05fa4356266524d11084df04d96034c8ff6624. Runtime remains alpha.29-dev.26-water.1; no product version or art/geography/water quantities changed.

The actual parser accepted a wrong request ID carrying `Saved the game`, wrong response types and invalid terminators. Lengths were unbounded and per-recv socket timeouts allowed indefinite trickling. Authentication failure leaked the socket, and an optional empty auth preamble was mistaken for authentication success.

Repair: validate 10..4106-byte frames (4096 payload plus framing), exact request ID, response type and two terminators; use one monotonic 20-second request deadline through every receive and initial send; consume at most one empty auth preamble; close failed-auth connections. Commands used by this harness have short responses. General multipart command aggregation is not implemented; subsequent mismatched packets fail closed rather than acknowledging another request.

New real-parser wire tests reproduce baseline failures (7 failures, 2 errors). Final 12 protocol plus 16 existing receipt/shutdown tests pass in normal, -O and -OO modes. Controlled fragmented bytes, not an actual Minecraft session, exercise the parser. These results do not qualify gameplay, native water or a new JAR.

Consumed latest completed audit: docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md, pinned4fb29e0; scoped5.0/10, no new full regrade. This repair strengthens native save/reopen evidence; it does not close V8, physical water ledgers, visual or performance findings.

Workspace maintenance removed the earlier checkout and caches. Pinned Java25/Java21/Gradle and helper archives restored with all size/SHA checks passing (toolchain-restore.log). Direct private-repository clone failed: could not read Username for https://github.com. Connector reads/writes remain available; full project/dependency restoration is unfinished. No full Java/Gradle/module/native/graphics checks or fresh artifact/release this run. Do not reuse old checks as current validation.

Expired root and water claims read before work; water-rendering/runtime files left untouched. Personal-context retrieval did not establish a live other-chat writer. Auditor run record reports no_update05:51Z, not a new audit. Builder remains enabled; lease released at checkpoint. Next restore pinned project/dependencies through authorized access and continue water loaded/unloaded ledger/persistence plus V8 saved-profile/topology acceptance, reconciling foreground water first.


<!-- END PRESERVED SOURCE -->
