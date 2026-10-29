# Expanded replica — docs/renderer-jams/20261028-eviction-failure-recovery/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-failure-recovery/README.md) |
| Source identity | git: docs/renderer-jams/20261028-eviction-failure-recovery/README.md |
| Preserved source SHA-256 | `ccfc2a1b3e34e7a15d27f234f13e53f774337b68ad814e8f2a18eec0a8683460` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Keeps memory-pressure eviction bookkeeping and later victim closes running after an earlier GPU close exception, then rethrows the first failure with later failures suppressed. Persisted recovery admission and stale-key cleanup remain in the required order.

## Research expansion — gaps and details to deepen

- Attempted cleanup is not verified GPU reclamation. The ledger should distinguish logically evicted ownership from resources still referenced or uncertain after failure, so a hard memory budget cannot be falsely satisfied by subtracting unusable-but-live allocations.

## Research expansion — proposed contracts

- Add a retirement/quarantine state with last recorded/submitted consumer, uncertain release reason, and eventual completion or renderer-failure policy. Preserve recovery-index and revision cleanup even when a close fails; do not describe this as device-loss recovery.

## Research expansion — acceptance checks

- Retain 6,000 model fault sessions and baseline rejection, then inject actual backend close/submission faults under cache pressure. Verify remaining victims, recovery continuity, first-error identity, bounded memory, and correct native water/terrain coverage after the defined failure path.

## Research expansion — source anchors

- Renderer jam 17 — LOD GPU eviction-failure continuity

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `LodEvictionFailureSmoke.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-failure-recovery/LodEvictionFailureSmoke.java) |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-failure-recovery/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 17 — LOD GPU eviction-failure continuity

**Jam:** `renderer-20261028T2055-eviction-failure-recovery`  
**Pinned source commit:** [`5eb5c8961ad778a28036416f498387c988fb865e`](https://github.com/Shnewbs/Deep-Primal/commit/5eb5c8961ad778a28036416f498387c988fb865e); claim parent `2b588b11fc0e16a642e81cde905f199357981cbc`. Version `0.0.1-alpha.29-dev.62-coastal-proof.1` is the **shared unchanged engine version**, not a new JAR.

A GPU buffer close failure during memory-pressure eviction previously interrupted remaining victim closes, persisted recovery indexing, and stale-key cleanup. `evictToFit` now uses the existing failure aggregator, continues the hard-budget eviction loop and each victim's bookkeeping, then rethrows the first failure (later failures suppressed). This is **not device-loss recovery**; all ordering, material, transparency, geometry, and other workstream behavior remain unchanged.

**Evidence:** [read-only renderer self-audit](REVIEW.md), [machine-readable receipt](RECEIPT.json), [committed source-only Java21 smoke](LodEvictionFailureSmoke.java). A source-and-test evidence ZIP was created in the chat workspace; no qualified JAR/GPU acceptance is claimed. Full Java25/Gradle/Minecraft/RTX/FSR3/4 and hardware benchmarks remain owner-operated.


<!-- END PRESERVED SOURCE -->
