# Expanded replica — docs/validation/engine-continuation/v8-basin-closure-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-basin-closure-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-basin-closure-2026-10-29/README.md |
| Preserved source SHA-256 | `1d877f32cae69215188c8bdd6f8b42e4a7f9dae797bc963e274c5f4ea906e5f2` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This Dev70 receipt records an opt-in routing policy v3 after an independent API-double counterexample found Dev69 v2 falsely classifying a deep inland seam basin as marine. The new sampled-component traversal excludes fully explored closed depressions, separates corner search budgets, and preserves v1/v2 fingerprints inside saved-profile revision 5. It reports 19 isolated Java21 suites and rejected broken controls. The document explicitly retains a heuristic admission for budget-limited outward continuation, acknowledges large enclosed-lake false positives, and leaves native worldgen, real watershed routing, conservation, and full-build acceptance open.

## Research expansion — gaps and details to deepen

- Actual limitation already acknowledged: a marine core plus unresolved continuation beyond eight regions or 96 components remains heuristic evidence, not proof of ocean reachability. The scoped negative-case repair cannot support an unconditional authoritative marine claim.
- Elaboration: independent corner budgets and cache bounds need a consumer-visible reason and coverage record so exhausted, disconnected, and successfully connected results are not flattened into the same boolean.
- Actual integration remains open for the real priority-flood planner, global outlet/discharge identity, and genuine saved-profile/native replay. The new counterexample is historical source-model evidence, not a fresh live-world defect.

## Research expansion — proposed contracts

- Define connected, disconnected, and unresolved marine evidence with policy fingerprint, explored bounds, observed frontier, and rejection/admission reason. Any future stronger authority policy requires its own saved identity rather than changing frozen v3 output.
- Specify per-corner search accounting independently of cache warmth and query order. Cache only results with the complete seed/profile/policy dependency key; eviction cannot borrow or reset a logical search allowance.
- Require a globally justified ocean anchor before promoting sampled marine evidence to authoritative boundary exchange. Keep optional heuristic surface classification visibly separate from finite-water ownership.
- Add a promotion record linking the Dev69 counterexample, Dev70 scoped repair, preserved legacy results, and the still-required production watershed and native codec tests.

## Research expansion — acceptance checks

- Replay the height-20, continentalness-minus-0.4 closed-basin witness under frozen v2 and opt-in v3, on both signed seams, recording the expected policy-specific result.
- Exercise a closed lake larger than the search limit and an actual outward marine corridor; require truthful unresolved/heuristic status rather than an unsupported global guarantee.
- Compare cold/warm caches, reversed corner queries, parallel calls, and eviction while checking separate eight-region/96-component accounting.
- Before activation, compile the real watershed dependencies and run genuine profile reopen plus native water/constituent conservation and border fixtures.

## Research expansion — source anchors

- New source behavior
- Reproducible checks
- Gates that remain open

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `results.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-basin-closure-2026-10-29/results.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev70 — opt-in bounded basin exclusion after dev69 independent audit

Source version: `0.0.1-alpha.29-dev.70-basin-closure.1`. Starting main: `45c777691a9d0934012eb114a57ab8501f0ec827`.
Independent auditor `docs/engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md` pinned `9a1d92a44d7d3864d35e0c3ac288158a37c33015`. Its Java25 isolated counterexample showed **dev69 routing v2** erroneously certifies a closed inland basin with height 20 and continentalness -0.4 as ocean on the positive 8192 watershed seam. This was not a native Minecraft world.

## New source behavior

- `V8OceanConnectivity` keeps the older default v2 algorithm unchanged. Explicit **v3** traverses the connected sampled wet component, probing exactly adjacent lattice points at inter-region gateways. A completely explored deep inland depression sealed by sampled dry land is rejected as ocean, even with a marine-like continentalness. Query order cannot borrow another corner's regional search budget; the bounded resident cache and work caps remain.
- An unresolved wet continuation beyond the **8-region/96-component cap** plus a deep marine core remains only a **heuristic** ocean admission. It is NOT evidence of a globally reachable ocean: exceptionally large enclosed lakes can still be false positives. This is a scoped negative-case repair, **not finished authoritative cross-region routed priority flood, outlet or discharge transport**. Do not activate native worldgen on this evidence.
- `V8RoutedWaterSource.authorBasinGuarded(revision4)` explicitly records the policy in an r5 profile using a new domain-separated SHA-256 fingerprint. `open` restores exactly v1, v2 or opt-in v3. Existing r5 v1/v2 serialized fields, graphs and replay remain frozen, as do V1–V7 geography, player edits, water quantities and renderer sources.
- The water-domain code still only classifies prospective wet columns for surface-material exclusion; it does not alter fluid volume, water height, chemistry or any block.

## Reproducible checks

Java `openjdk 21.0.12.1`, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` using **isolated Minecraft/Mojang API doubles**. All **19/19** focused suites passed with **21,012,678** fixture checks; the changed class suite passed **190** checks, including both signed 8192 seams, the independent deep-basin counterexample, v2 frozen output, v3 outward-continuing sampled corridors, a dry ridge, saved r5 profile encode/decode and seed/policy mismatch rejection. Two separately compiled broken v3 controls were correctly rejected (deep-core-only shortcut and dry gateway treated as wet).

Changed local Git blobs in [results.json](results.json) match blobs created for the commit. The portable source evidence ZIP contains real current source for the scoped V8 components, isolation doubles, all focused tests, a replay script, logs, and a compiled-mutant script. A replay is not native Minecraft testing.

## Gates that remain open

Real `WatershedRegion` regional outlet topology; authoritative global inland/ocean identity and cross-region water budgets; native Mojang saved profile; exact new/old chunk boundaries and world edits; finite-water quantities and constituents, loaded/unloaded/restart and multiplayer soak; real Vulkan/OpenGL waterfalls, 60Hz/frame and hardware memory/latency distributions; Java25/Gradle/NeoForge full build; source/package/JAR identity. These were not executed. The original ten-category grade remains **50/100 (5.0/10)**; no regrade or playable JAR.

The owner owns full builds/JAR publication. The complete pinned checkout/root validator was unavailable; `docs/ENGINE-WORK-STATE.json` was deliberately left unchanged. Scoped terrain work state records the temporary ownership, actual test scope and final lease release.


<!-- END PRESERVED SOURCE -->
