# Expanded replica — docs/validation/engine-continuation/water-quad-packet-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-quad-packet-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-quad-packet-2026-10-23/README.md |
| Preserved source SHA-256 | `d6bc151ce0ecabc2068f9ac37f674156fb46fbe53be65fdd2ca39c80fa209a94` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R41 coalescing changed corners into one semantic match, decode and quad-packet encode after repeated per-corner work caused controlled admission overruns. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that cold route maxima are not latency distributions and do not attribute every tail to packet arithmetic or establish visible-scene memory coverage. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a quad-level update contract with one decode/encode per selected mask and a benchmark protocol separating arithmetic, backend submission and frame cost. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare scalar and coalesced packet bytes for all corner masks, retain native rejection controls and measure matched visible cap-churn scenes before any performance promotion. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R41 — coalesce water boundary packet updates per quad

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R41 — coalesce water boundary packet updates per quad

2026-10-23. Staged on `work/engine-after-alpha24`; parent checkpoint `d82b50eaba5c027fdc3d398a39336fe1e7569acd`. Engine and performance acceptance remain incomplete. Main, public production sources and installers are unchanged.

## Reproduction and focused source change

The R40 boundary path revisited the shared quad packet for every changed corner: separate semantic matching, repeated start-field decoding and four vertex encodes per selected corner. The fresh R41 scalar native control passes its data gates but reproduces the synchronous admission overrun: large2,048-quad routes reach8.677ms; the complete control's maximum is14.925ms. These cold route observations are not a latency distribution or proof that packet arithmetic causes all the overrun.

The independently authored change gathers the requested corner mask, targets, sources and ages, uses a read-only shared-packet predicate, then patches the private quad once. Each selected quad's four vertices are encoded once, retaining unselected starts/pairs and each vertex's opacity, light and other bytes. Accepted CPU bytes and cleanup deadlines still commit only after a successful GPU range. Existing scalar APIs remain compatible. GPU range counts, byte payloads, grouping,262,080-byte bound,36-byte vertices, shader/layout and retry ordering are unchanged. Helpers add no allocations or persistent state; the existing144-byte heap quad and bounded transfer buffer are retained.

`since-r40.patch` changes two production files (core packet helper and renderer) and two private validation files (core-check registration and new bulk checks). The complete source snapshot has584inputs and22staged files:5production/17validation. The other580prior inputs match R40. Water quantity, physical solver, edits and frozenV1/V2/V3 geography are unchanged.

## Fresh validation

- Two full builds pass, each with25executed tasks. The candidate retains210,000existing packet cases and adds210,000known-input/exact-scalar bulk cases across all16masks, clock wraps/long clocks, nonzero quad offsets, arbitrary vertex opacity/light and nonmaterial bytes. A standalone quick core check also passes the same new cases; it is not additional unique coverage.
- Four native sessions pass: fresh scalar boundary control, development Vulkan, immutable packaged Vulkan and OpenGL fallback. All three candidate sessions pass132boundary cases/264paired CPU-GPU reads; their full after-call and after-retry packets match the fresh control and independent Python packet/range/rejection oracle. Fixture class identities agree.
- Prior presentation regressions pass in each final session:48admission cases/432paired reads with zero budget-denied resident losses,15,336seam pixel comparisons/30,672draws,48synchronous rejection cases/192reads,nine extraction-deadline cases and129distinct-frame retirement turns per session. The R40 admission and older publication baselines are replayed historical controls, not fresh runs.
- Save/disconnect, sealed/raw console identity, crash delta and Vulkan validation/resource/shutdown records pass explicitly. Package audit passes6archive CRC checks,411compiled class identities and5nested/standalone identities; private bulk checks/validation/compatibility-agent code are absent. The packaged launch verifies the audited full-build outer package is unchanged before/after execution.
- Three lake captures were manually inspected: water, banks and terrain render without an error screen. Differing dimensions/live load states and static stills are not temporal-flicker, complete enabled-view, visible cap-pressure or API-parity acceptance. The earlier distant LOD cutoff remains open.

## Packet CPU benchmark and self-audit

Primary controls compile baseline and candidate core sources with the same pinned JDK25 compiler options, seal class identities, and run scalar/bulk/bulk/scalar in separate JVMs. Each mask has16warmup rounds and31measured samples; each sample processes16passes of2,048quads. Initial/final binary packets are archived and independently reconstructed in Python, including unselected fields and per-vertex opacity/light. Both variants end with identical bytes.

| Packet control | Scalar median ms per sample, two repeats | Bulk median ms per sample, two repeats |
| --- | --- | --- |
| Two selected corners, mask3 | 3.683298 / 3.668736 | 1.864223 / 1.879777 |
| Four selected corners, mask15 | 8.507351 / 7.972056 | 2.508362 / 2.493790 |

These are packet arithmetic measurements, not full neighbor scans, GPU publication, frame/FPS or owner-GPU results. The four-corner control checks API generality; it does not assert that a side-neighbor production update touches all four corners. P95/sample details and exact commands/classes are in `benchmark-results.json` and `benchmark-identities.json`.

Self-audit found that preliminary scalar timing counted work per corner while bulk counted per quad. Those counters could bias timing. Both timed counters were removed and the primary pinned ABBA controls rerun. Earlier differing-classpath/instrumented controls remain separate diagnostic data; only the counter-free controls above support this scoped comparison. Product/native code did not change during this benchmark correction.

## Scope, remaining defects and next work

Native publication still exceeds2ms: development Vulkan reaches5.591ms and packaged Vulkan5.722ms; individual routes vary and some regress. Private retirement maxima are6.711ms development/10.144ms packaged. This reduces repeated packet work; it introduces no incremental whole-neighbor continuation or hard elapsed-time bound. Range counts and payloads do not shrink. No broad performance promotion follows.

Actual visible-scene discovery/admission/eviction under matched travel, edits and cap churn remains unaccepted. The private32MiB allocation controls and nondrawn frame pages do not establish total GPU/process peak memory or full-scene coverage. R40nominal reservation remains intact, but allocation/device failure after a sufficient plan, automatic retry policy, whole-cohort GPU atomicity and asynchronous loss remain open. Long streams/oceans, complete enabled-view/mixed transparency, spatial/coarser LOD, owner GPU/FPS and market comparison remain unfinished.

Retain the requirement register: renewable/finite regional budgets, headwater delivery, shared compatible river/ocean composition without duplicate volume, bounded pressure continuation and geology remain open; catalog/species stays deferred. Separate solver substeps from independent60Hz simulation. The separate retained-drain experiment remains unaccepted; presentation/core checks here do not replace its full native basin/output/latency/residency acceptance. Prior stale-class/empty-descriptor, marker/reload/atlas and save failures remain unresolved historical blockers; no speculative workaround or root-cause claim is added.

Next implement bounded/resumable scattered neighbor publication and match actual visible-scene cap churn while preserving local corner clocks, quantity, retry, edit and save/resource gates; then continue sustained streams/oceans and LOD/coarser coverage. This is a continuation checkpoint, not engine completion.

## Source restoration and evidence

```
python scripts/restore_snapshot.py EVIDENCE_DIR NEW_ISOLATED_PROJECT
python scripts/replay_checkpoint.py EVIDENCE_DIR NEW_REPLAY_DIR
```

Destinations must not exist. Restoration verifies all584inputs and archive hashes before writing. The staged manifest is an overlay; use the complete source snapshot. Replay validates raw GPU/pixel and benchmark data plus pinned benchmark class bytes; it performs no new build, native run or timed benchmark. `offline-audit-replay.json` records1,027physical capture/build/benchmark/historical-admission members and15,707current logical capture paths, plus the complete source restore.

Lossless capture compaction preserves all logical hashes while reducing36,458,204compressed bytes to24,216,463. Benchmark content compaction preserves316logical paths/14,893,985bytes using85objects/1,159,166unique bytes. Source, original scalar controls, all diagnostics, commands, raw arrays/readbacks, logs, runtime fixture identities and self-audits remain reviewable. All recorded Gradle/native and timed benchmark processes are sequential. Earlier repository evidence remains intact;73older missing originals remain missing. Reuse the recorded R38–R40 toolchain/Mesa25.2.8/Xvfb1.9 pins; software-driver checks provide no owner-GPU guarantee.


<!-- END PRESERVED SOURCE -->
