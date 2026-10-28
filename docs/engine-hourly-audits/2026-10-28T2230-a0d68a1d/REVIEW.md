# Deep Primal Dev64 — independent pinned-source audit

**Pinned committed source:** `a0d68a1d916e3cc6f71de827de2cf3e7b34d0da0`, 2026-10-28T22:13:31Z. Independent audit used an isolated Linux workspace and never modified production files or any Builder/renderer lease, cache, checkout or job.

## Committed changes and observed evidence

- [Dev64 V8-r4 reference climate binding](https://github.com/Shnewbs/Deep-Primal/commit/a7ab7dde6e0aedf87cd524de02e4388415d6b6f4): 9-field strict versioned snapshot, full saved V7 terrain options and channel revision, retained r1-r3 formats. V8 remains opt-in and unregistered. Builder's 20,945,922 Java21 fixture assertions are **producer-reported**, not independently counted here.
- [LOD bounded indexed batches](https://github.com/Shnewbs/Deep-Primal/commit/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9): synchronous ordered batches of at most 4096 items, post-call reference cleanup, unchanged single-draw path. Renderer producer reports 2,412,262 Java21 assertions, but its CPU-only timings are **slower** than the former unbounded loop; no GPU frame gain is claimed.

## Fresh independent verification

Exact compiled production Git blob identities: `TerrainOptions.java` `62c92fec60876d3e220b47acfe5d2b1e5c7ae61a`, `V8ClimateBinding.java` `1d6b4924672c03f06cd1a546189c9f9b07a7ae74`, `V8SavedClimateSource.java` `1450777a54144604ee2d07c0283c31ecff6a13b0`, `BoundedIndexedDrawBatches.java` `2bfc247e178024350807c2f144a411a845fa8330`.

Using Java 21 strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`, with isolated Mojang codec and TerrainEngine API doubles:

| Independent test | Assertions | Result |
| --- | ---: | --- |
| Canonical V8 saved climate identity, 30k randomized options, 3,081 rejection cases | 183,081 | PASS |
| Saved r4 vs r3/V7 climate bridge, 50k coordinates, 16,384 parallel replay samples | 150,012 | PASS |
| 401 indexed-draw batching scenarios including 131,072 records, exact order, exceptions, cleanup | 3,661,555 | PASS |
| **Total** | **3,994,648** | **PASS** |

Both deliberately faulty variants compiled, but their required assertions failed: permitting channel revision 6, and leaking indexed-draw references in `end()`. This confirms relevant regression detection in the independently tested scope, not native acceptance.

## Audit findings

1. **Open high-priority integration gate:** `V8SavedClimateSource.java` lines 12–16 and 91–102 still use reference V7 ocean/river classifications, not an authoritative V8 mask. Do not enable V8 native generation before real routing/coastline/save testing.
2. **Static V8 authoring hazard:** `V8GeneratorSnapshot.java` lines 66–70 permit `withBlendPolicy()` on newer saved V8 snapshots and always return revision 2, dropping revision-3/4 surface/climate fields. No actual player-save corruption was reproduced; guard this API before exposing it in world authoring.
3. **Future misuse risk:** `BoundedIndexedDrawBatches.begin()` validates the expected count, but returns a mutable raw ArrayList whose actual additions are not capped. Its current sole render caller obeys the 4096 maximum. This is not an observed runtime overflow.
4. **Stale root checkpoint:** `docs/ENGINE-WORK-STATE.json` is still checkpointed at Dev53 (2026-10-28T12:04Z) despite Dev64 commit. Use current per-feature evidence, not root state alone, for acceptance.

## Whole-engine rubric (carried, not fresh complete regrade)

Terrain 5; biomes 4; hydrology/caves 6; features 4; performance/threading 3; stability 7; architecture 6; extensibility 4; testing/documentation 7; polish/tooling 4. **50/100, 5.0/10, Tier 2** (historical accepted grade). The strictly greater-than-8.5/10 target is unproven.

## Unrun/owner-build override

The owner's October 27 override assigns complete Gradle/Minecraft/GPU builds to the owner. JDK25 and archive absent locally; GitHub DNS blocks complete checkout. **No JAR built or released.** Full Java25/Gradle9.2.1, real Mojang DFU JSON/NBT/save-reopen, native chunk/loaded-unloaded fluid tests, packaged class/CRC checks, Windows AMD8060S Vulkan/OpenGL GPU p95/p99/VRAM and multiplayer soak **not run**. No older binary relabeled or removed; existing Builder and renderer kept running independently.

Next priorities: authoritative V8 saved climate/ocean/routing, native cross-chunk/edit/save, finite-water quantity+chemistry conservation, owner hardware GPU, and V8 authoring downgrade guard. Exact source-only evidence ZIP is in Library `/Deep Primal/Independent Audits/DeepPrimal_Dev64_IndependentAudit_20261028T2230_a0d68a1d_SourceEvidence.zip` (18,570 bytes, CRC verified, SHA256 `b73a95bc1a8288bef9dcfdc18b72a776ad989c8c06521932327791c30df97b87`).
