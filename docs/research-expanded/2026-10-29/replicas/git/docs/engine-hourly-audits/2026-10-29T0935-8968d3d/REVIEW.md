# Expanded replica — docs/engine-hourly-audits/2026-10-29T0935-8968d3d/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-29T0935-8968d3d/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-29T0935-8968d3d/REVIEW.md |
| Preserved source SHA-256 | `25273a8879aa882c7e58b370974cbeef3f3ee0e28d5890a9981d90486e3643a9` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev75 audit reports independent execution of the exact standalone WatershedOutletTopology production class against an all-pairs minimax oracle, with two 93,015-assertion runs and three rejected source mutations. It validates bounded graph planning, receiver chains, unknown frontiers and planned runoff within that helper. The source is not wired to native V8 in this checkpoint; authentic marine anchors, regional input completeness and live water/constituent ownership remain separate obligations.

## Research expansion — gaps and details to deepen

- A correct solver cannot certify untrusted or incomplete input ports. Solver correctness, regional-port derivation, ocean-anchor authority and saved-policy activation need separate acceptance nodes.
- Planned runoff aggregation is a graph quantity with a declared input meaning. It must not be presented as conservation of physical runtime water, salts, sediment or edited-world reservoirs.

## Research expansion — proposed contracts

- Record graph schema/version, stable signed port identities, unique runoff ownership, floor barriers, tie-breaking, unknown frontiers and authentic anchor provenance before admitting a topology result.
- Keep the independent minimax oracle and defective variants as durable solver evidence, then add a production-boundary extraction gate and an explicit saved V8 activation gate.
- Define complete versus partial topology snapshots and the action when MAX_PORTS or MAX_LINKS is exceeded. Exceeding a budget must not silently drop barriers, duplicate discharge or convert UNKNOWN to CLOSED/MARINE.

## Research expansion — acceptance checks

- Vary input order, signed coordinates, equal-cost paths, intermediate barriers, unknown frontiers and exact capacity boundaries against the independent graph oracle.
- Reject unauthenticated marine anchors and duplicate upstream contribution IDs; separately measure native water conservation across actual save/unload/restart boundaries before gameplay promotion.

## Research expansion — source anchors

- Deep Primal Dev75 — independent Java25 audit (2026-10-29)
- Exact-source independent evidence
- Findings and boundaries

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal Dev75 — independent Java25 audit (2026-10-29)

**Pinned source:** `8968d3d007bd1c9e976710c43a2ed1553480793b` (`0.0.1-alpha.29-dev.75-outlet-topology.1`); new implementation committed at `1395e845a4cd7327e409a7de8e162401c1e44583`.
**Report parent immediately before publication:** `8968d3d007bd1c9e976710c43a2ed1553480793b`. The audit is observational and did not modify the Builder or production sources.

## Exact-source independent evidence

- Matched complete `engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java` Git blob `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a` and SHA256 `a912949a188ec7d1ca1d6e102b4f764c771a671da211989c2bad7dc13512fb64` byte-for-byte.
- Restored verified Temurin JDK **25.0.5+8** from owner-provided Library archive into independent audit output; Java21 installation untouched.
- Exact committed production class and independently written oracle compiled with `javac --release 25 -Xlint:all -Werror`, ran `java -ea -Xverify:all` **twice**, each run passing **93,015 assertions** across **1,600 signed-coordinate randomized graphs**.
- Independent all-pairs minimax closure compared spill-stage, reachability and status; verified ordered stable receiver chains, cycle freedom, unknown/closed frontiers, planned runoff totals, reversed-input parity, guards and negative coordinates.
- Three separately compiled deliberately broken variants were **rejected**: missing intermediate-floor barrier; duplicated downstream runoff; discarded unknown-frontier flag.
- Builder Dev75 report separately claims Java21 18,111 positive assertions and 3 rejected mutants (inspected, **not rerun**). New Java25 assertions are independent and do not substitute for native V8 acceptance.

## Findings and boundaries

- New helper is bounded (`MAX_PORTS=16384`, `MAX_LINKS=65536`) and implements deterministic minimax/receiver planning. Production evidence: [`WatershedOutletTopology.java` L15–31](https://github.com/Shnewbs/Deep-Primal/blob/8968d3d007bd1c9e976710c43a2ed1553480793b/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java#L15-L31), [L112–215](https://github.com/Shnewbs/Deep-Primal/blob/8968d3d007bd1c9e976710c43a2ed1553480793b/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java#L112-L215).
- **Not wired to native V8 or `WatershedRegion` yet**. Authoritative verified ocean anchors, complete regional port sets, saved routing provenance and real finite-water quantity/salt/sediment state remain open. Planned runoff tree equality is not conservation of live water, world edits or native saves. Dev75 checkpoint README confirms no gameplay activation.
- Root `docs/ENGINE-WORK-STATE.json` remains at Dev53; Dev75's scoped `docs/TERRAIN-BLENDING-WORK-STATE.json` marks its worker checkpointed and lease released. Staged R57/R60/R61 are not promoted. No evidence of new native compatibility or GPU acceptance.

## Ten-category grade (last accepted complete baseline, **not** a fresh regrade)

| Area | /10 |
|---|---:|
| Terrain realism and variety | 5 |
| Biome blending and coherence | 4 |
| Hydrology and cave quality | 6 |
| Features and structures | 4 |
| Performance and threading | 3 |
| Determinism and stability | 7 |
| Architecture and code quality | 6 |
| Extensibility and data-driven design | 4 |
| Testing, benchmarks and documentation | 7 |
| Polish, config and tooling | 4 |
| **Overall, Tier 2 (carried)** | **50/100 = 5.0/10** |

Original comprehensive rubric: `docs/engine-review-2026-10-24/REVIEW.md` at reviewed source `f141c0b4d058abb311cb088e4f17a575685a2939`. Unintegrated/new helper earns no gameplay points. **Stop threshold strictly greater than 8.5/10 remains unmet.**

## Not run / next actions

October 27 owner override places full Gradle/Minecraft/GPU/JAR builds with owner. Local Git CLI DNS cannot resolve GitHub, so no full independent checkout or Gradle wrapper; **no JAR built or released**. Did not run outer/nested JAR CRC, source-to-package identity, Windows AMD8060S Vulkan/OpenGL, real native save/reopen/shutdown, mixed-gen border, finite water/constituent lifecycle, GPU bench or multiplayer soak. No shared heavy benchmark ran.

Next: integrate real `WatershedRegion` signed ports and authenticated ocean anchors into saved V8 policy; accept cross-region outlet/lake discharge and incomplete frontiers; verify finite water/chemistry through native unload/restart; owner to perform full Java25 Gradle, packaged archive and Windows Vulkan/OpenGL acceptance before any regrade/release.

**Retained independent ZIP:** `DeepPrimal_Dev75_IndependentAudit_20261029T0935_8968d3d_Java25Evidence.zip`, 13,674 bytes, SHA256 `241e8b77970a38b21e1096121a8b4baa9baed9d9cc1112e0ff14d7b0094af212`; 17 entries with outer CRC and SHA256 member manifest verified; saved to Library `/Deep Primal/Independent Audits/`.


<!-- END PRESERVED SOURCE -->
