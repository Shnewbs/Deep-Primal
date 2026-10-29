# Expanded replica — docs/VALIDATION-0.0.1-alpha.13.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.13.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.13.md |
| Preserved source SHA-256 | `d6f8df8246f7c36577c6d64b0c022da3efdc24c6e52fcbb75e5468216d3b3edf` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Alpha 13 — contained stepped rivers and persistent currents”. Its original opening states: “Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Base: published alpha 12, 36d82377eb7517809861bc58912218fa17ae721e.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `Fluids.WATER`.

## Research expansion — gaps and details to deepen

- Extend the recorded generation change with saved-revision, chunk-order, boundary, and authoritative-water compatibility checks.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Freeze existing generator/channel/material recipes; new terrain behavior requires an explicit saved revision and complete dependency identity.
- Distinguish surface appearance, regional drainage plans, ocean connectivity, and live finite-water ownership in the acceptance record.

## Research expansion — acceptance checks

- Generate the same signed-coordinate corpus in reversed and randomized chunk orders, then compare overlap, seams, and saved-profile replay.
- Reopen with creation defaults changed and verify old geography, player edits, water quantity, and material identities remain governed by the saved recipe.

## Research expansion — source anchors

- Alpha 13 — contained stepped rivers and persistent currents
- Remaining scope

Related design: [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.13/checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.13/checks.txt) |
| `validation/0.0.1-alpha.13/runtime-summary.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.13/runtime-summary.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha 13 — contained stepped rivers and persistent currents

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Base: published alpha 12, `36d82377eb7517809861bc58912218fa17ae721e`.

The supplied screenshots show broad water curtains and air beneath river water. This release keeps the requested stepped descent, replaces the new profile's limited surface-water band with bed-backed filling, and supplies persistent downstream currents through routed flat stretches. It does not claim that the screenshot locations themselves were reproduced: their world seed and coordinates were not supplied.

## Behavior

- The new `waterways_v3` and `waterways_v3_deep` profiles resolve seventeen stations along each selected routing reach, checking its center and both banks. Running downstream minima and shared junction heights retain descending terraces while reducing waterlines that span lower intervening terrain.
- Region drainage can continue along selected sea-level reaches, seeded at deep water or wet regional exits. Enclosed regions fall back to a real wet minimum; wholly dry regions create no invented outlet.
- Native density interpolation determines the local bed. Surface water fills continuously from that bed to the local waterline, rather than stopping sixteen blocks below a potentially much higher waterline. A three-block floor band prevents native biome carvers from opening holes immediately under the generated water. Deeper aquifers retain their native behavior.
- Generated routed water carries a sixteen-direction block-state value. Its fluid is exactly vanilla `Fluids.WATER`. One common fluid-vector hook supplies the current to native water rendering and entity physics. Native boats and items handle the push; there is no additional boat-force hook.
- Block-state persistence and normal chunk networking retain the direction without block entities, custom current packets or tick-driven terrain searches. Player-placed vanilla water remains ordinary water.

Geometry remains in the platform-neutral core and native worldgen adapter. The registered water block and current settings live in root `water`, independently of Vulkan, graphics settings and the common performance engine. The [requested-mod comparison](RIVER-FLOW-COMPARISON-2026-10-19.md) records source inspection versus author-description evidence and explicit differences. No third-party river code, assets or dependencies are bundled.

## Settings and saves

For a **new world**, `riversAndCoasts`, `mountainStreams` and `containedRivers` default to true in startup `config/deepprimal-worldgen.toml`. Restart after changing creation settings. `extraUndergroundDepth` still chooses -128..639 or -384..639.

| Creation selection | Stored noise profile |
| --- | --- |
| `riversAndCoasts = false` | Original `prototype` / `prototype_deep` |
| Rivers enabled, `mountainStreams = false` | Frozen alpha 11 `waterways_v1` / `_deep` |
| Mountain streams enabled, `containedRivers = false` | Frozen alpha 12 `waterways_v2` / `_deep` |
| All three enabled | Alpha 13 `waterways_v3` / `_deep` |

Existing worlds keep their serialized noise profile and height. Changing these defaults does not upgrade an old world's future chunks, repair its explored water or convert existing sources. **Use a new world to test the repaired generation.** All thirty-five existing terrain/preset/pack resources are byte-identical to alpha 12, including their packaged bytes.

World-synced server config `deepprimal-rivers.toml` controls `currents` (default true) and `currentStrength` (default 1.0, range 0..2) separately from terrain creation. In dedicated worlds it is a server config under the world's `serverconfig` directory. Disabling currents leaves native water physics; stored directions remain available for re-enabling. Current blocks are registered game content, so retain the root mod in worlds containing them.

## Validation

`build compileVisualValidationJava --offline` passed. Core checks include 100,000 legacy waterway cases, 20,000 original drainage cases, 20,000 contained-profile random cases and 44,088 contained longitudinal course samples. The contained test retains both flat terraces and descending steps, finite deterministic terrain, and normalized current directions. Existing queue, horizon, cache, module-boundary and 200,000 surface differential checks also passed.

Packaged dedicated-server validation uses the installer and a separate development fixture. The launch harness excludes loose project classes and older module JARs from its runtime classpath. The loader verifies alpha 13 nested modules. Checks run automatically after startup, record complete atomic per-run journals, and halt/save normally; console readiness alone is not evidence. Development fixtures are absent from the installer.

| Native world | Seed | Selected graph segments in nine surveyed regions | Elevated segments above Y160 | Gorge segments | Checked mountain waterline |
| --- | --- | --- | --- | --- | --- |
| Standard, minY -128 | 452343 | 3,595 | 388 | 695 | Y306 at (-73, 4597) |
| Deep, minY -384 | 947291 | 3,105 | 332 | 747 | Y311 at (-769, 6469) |

Each profile checks a twelve-node course with intermediate samples, water-filled centers, submerged bed material and actual downhill waterlines. Whole 16×16 chunk columns are checked from world minimum Y to the ceiling, including sea-level river water; a generated current block must have water or solid support beneath it and a nonzero native flow vector. Selected mountain/gorge chunks are checked again at FULL status after carvers and features. Native coastal sand/sandstone regressions pass.

Sixteen stored directions round-trip through the native block-state codec. Native water buckets work. In a controlled flat-water pool, forty manually advanced native entity ticks move a boat approximately 3.923 blocks downstream; with currents disabled its horizontal displacement is zero. Items likewise drift only with the current enabled. This is a behavior fixture, not a travel-speed benchmark: the item reaches the pool boundary. Saved current direction 11 and its flow vector survive actual shutdown and reopening in both height profiles.

Ten creation/reopen cases pass: standard, standard-reopen, deep, deep-reopen, original-profile creation/reopen, alpha-11-profile creation/reopen and alpha-12-profile creation/reopen. Old profiles retain identical noise-setting IDs, heights and hexadecimal terrain samples when reopened with the new creation defaults enabled. These compatibility worlds are fixture-created legacy-profile saves, not user-uploaded old worlds. The strengthened whole-height support checks and complete journals were repeated for standard/deep creation and reopening on the same installer bytes.

Machine-readable evidence: [checks](validation/0.0.1-alpha.13/checks.txt) and [runtime summary](validation/0.0.1-alpha.13/runtime-summary.json).

## Package and cost

Installer: `DeepPrimal-26.1.2-0.0.1-alpha.13.jar`, 22,558,763 bytes. SHA-256: `5815367efe9ba14af3e19b989a88ddc314b2cfa39d1966ef3ab6a510562cc560`.

The standard installer grows 17,948 bytes versus alpha 12. It contains exactly four current nested module JARs, matching their build outputs. All 280 project classes match compiled bytes; no duplicate project classes, test fixtures or repackaged Minecraft/Mojang classes are included. Performance, graphics and Vulkan module classes, including their bundled dependencies, are byte-identical to alpha 12.

The four-way cache remains capped at thirty-two routing regions. Seventeen-station profiles add bounded memory and cold terrain work. No additional generation executor or per-tick hydrology simulation is introduced. Per-chunk water, bed and state arrays keep routing and fresh noise out of the per-voxel filler. Default-strength current lookup returns a cached vector. Nine-region survey observations were approximately 1.045 s standard and 0.829 s deep in this environment, after spawn had already warmed parts of the sampler. These are single observations, not cold-generation or hardware FPS benchmarks.

The existing local-only NeoForm tool workaround and dedicated-server asset setup described in alpha 12 were reused. They are not bundled or production startup fixes. The without-Vulkan build was not separately repeated for this release; source boundaries and unchanged graphics/backend class bytes are verified.

## Remaining scope

Routing is regional, not a globally stitched watershed or guaranteed ocean route. Selected routed sea-level reaches have currents; oceans, pools and unselected legacy lowland channels keep native behavior. There is no elevated-lake generator, general source-water momentum propagation, rainfall/erosion simulation, underground-river network or Sable/Create floating-structure integration. Finite routing samples can still produce angular bends. The horizon shares carved terrain height but retains its sea-level water approximation. No interactive visual session, shader-pack compatibility run or new GPU/hardware-FPS certification was performed. Broader visual balance across seeds remains playtesting work.


<!-- END PRESERVED SOURCE -->
