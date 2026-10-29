# Expanded replica — docs/validation/engine-continuation/v8-routed-source-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-routed-source-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-routed-source-2026-10-29/README.md |
| Preserved source SHA-256 | `38e28234e5ec4c3df05338117b821e5958d9d8f6fcaea62f22c2e2ea759d12ae` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev68 saved-profile-bound V8 routing using the graph's Y-independent height channel, validated producer fingerprints and a two-entry cache of 32-block-sampled region plans. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that regional boundary sea seeds do not provide global connectivity across 8,192-block regions; native generation and physically counted liquids remain unimplemented by this checkpoint. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an inter-region outlet and shoreline contract with a newly persisted routing revision, separating planned stage and flow from finite water ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare adjacent region outlets, interior depressions and signed seams, validate off-grid exact graph queries and profile reopening, then qualify native materialization and conserved water separately. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev68 — saved V8 geometry routed through region hydrology (opt-in source only)

This original shares identical bytes with: [library:Engine Builder Evidence/Dev68 V8 Routing/README.md](../../../../../project/Engine-Builder-Evidence/Dev68-V8-Routing/README.md). Both source identities are retained.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev68 — saved V8 geometry routed through region hydrology (opt-in source only)

This is a builder **source-only** checkpoint, not a playable JAR or native-routed worldgen. Baseline current main was `aed6bb2d7db0c7002355ef9ad57ecdd44aa94c9c`, then a bounded terrain lease was taken at `751aa46c3a3e23fc793cfd287e9aca6fcfc9ae9c`. Latest independent audit was `docs/engine-hourly-audits/2026-10-28T1831-a8e26ba/REVIEW.md` pinned to `a8e26ba492245d532eb4a116b13d9f842d7b013b`, carrying **50/100, 5.0/10** and explicitly requiring actual routed hydrology rather than a caller-declared fingerprint. The renderer's later main commits were retained.

## New engine implementation

`V8RoutedWaterSource` adds a strictly opt-in, saved-profile-bound planning source. `author(r4)` checks that the graph has a Y-independent `height` channel, fingerprints the actual saved V8 density graph, world seed, vertical span, frozen climate/reference channel revisions and routing implementation revision, then explicitly creates an r5 profile. `open(r5, seed)` validates the source fingerprint instead of merely accepting a caller-provided digest. `authority()` exposes an internally bound provider for the existing `V8WaterDomainAdmission` policy.

A bounded two-entry region cache constructs `WatershedRegion` using the **V8 graph's own pre-hydrology height field** at absolute-coordinate 32-block sample resolution, rather than V7 terrain shape. The existing vetted priority-flood, lake/outlet/river/delta/oxbow algorithms are reused; off-grid probes sample the exact saved graph, not rounded terrain. A deterministic four-connected boundary sea mask distinguishes connected ocean from interior depressions. Planned columns identify dry, ocean, river or lake and expose planned ground/stage/directional flow without touching native blocks, fluid amounts, surface presentation or the water/renderer owners' files. Saved V1–V7 and previous V8 profiles are not rewritten.

**Important limits:** This does **not yet** provide global coast/outlet connectivity across 8192-block regional boundaries (the current boundary sea seed is regional), native Minecraft V8 generation registration, actual saved-world climate/hydrology provenance validation, physically counted liquids, cross-chunk fill, multiplayer or old/new world border repair. No geometry/live-water completion claim is warranted. River planning uses saved V7 rainfall behavior and channel rules while using V8 relief. A sampled `height` channel is required; graphs without a suitable pre-hydrology 2D height channel fail closed rather than hydrologically routing arbitrary density.

## Evidence (Java21, isolated API doubles)

`javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. 18 focused suites passed; **21,012,488** test assertions/checks computed from the 17 previous exact suites (21,007,690) and 4,798 new adapter assertions. New fixture includes 32 explicit V8 r4→r5 author/codec/reopen cases, 1,440 water observations across signed coordinates, repeat and cache eviction replay, 101 rejection cases, changed graph/climate/seed detection, Y/3D height rejection. Two independently compiled broken variants (saved fingerprint bypass and Y-dependent height acceptance) were correctly rejected. First attempt with an unreachable density graph fixture was discarded before this final passing suite.

The **actual WatershedRegion is NOT compiled or executed here**: the fixture uses a behaviorally limited API double for it, alongside Mojang/MC doubles. The new adapter is compiled from production source; the positive results are not a watershed-routing correctness proof. No Java25 in the builder's isolated environment, no full project Gradle9.2.1/NeoForge build, actual Mojang JSON/NBT codec, native world creation/save/reopen, cross-region ocean/river/lake continuity, exact chunk/height parity, finite-water conservation, OpenGL/Vulkan pixel acceptance, GPU measurements or owner hardware tests. Root work-state validator unavailable due incomplete checkout; root state remains Dev53. No JAR/release was produced by this builder under the October 27 owner instruction.

Reproduce with an extracted evidence ZIP, strict source identity and local JDK21:

`python3 tools/test_v8_routed_water_source.py --repo . --bundle /path/to/Dev68_source_only_evidence.zip`

## Next gates

Port the 8192-region routing plan to an explicit boundary-connected watershed graph (outlets, inter-region shoreline and basins) with a new persisted routing policy revision; native Mojang codec/profile reopening and real chunk materialization without water/height mismatch; then native signed seams and finite-water loaded/unloaded/constituent ledger, restart, multiplayer soak and owner-operated GPU/profile gates. Terrain blending and water acceptance both remain open. The independent auditor alone may regrade.


<!-- END PRESERVED SOURCE -->
