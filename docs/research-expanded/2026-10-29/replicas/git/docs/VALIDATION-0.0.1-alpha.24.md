# Expanded replica — docs/VALIDATION-0.0.1-alpha.24.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.24.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.24.md |
| Preserved source SHA-256 | `fb6b90ec3e9140a70f8e487a3a3ce78cb24db03293db2b5b0e9b6441909769cc` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Alpha 24 — connected drainage and engine audit”. Its original opening states: “This is a verified development increment. The full terrain, hydraulic and graphical engine remains in progress; the open requirement register is not closed by this build.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Alpha 24 — connected drainage and engine audit
- Resulting behavior

Related design: [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `ENGINE-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/ENGINE-WORK-STATE.json) |
| `validation/0.0.1-alpha.24/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.24/installer-audit.json) |
| `validation/0.0.1-alpha.24/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.24/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha 24 — connected drainage and engine audit

This is a verified development increment. The full terrain, hydraulic and graphical engine remains in progress; the open requirement register is not closed by this build.

## Resulting behavior

New normal worlds select the independent `deepprimal:continental_v3` generator. V1/V2 saved generators remain available and unchanged. V3 plans connected tributaries, rivers and lake spillways by region, distinguishes connected sea from inland depressions, gives channels actual fluid voxels through source/inlet/estuary transitions, and applies dry bank floors after the union of wet channels. Valleys widen around deeper cuts; materials use final shaped slope and riparian cave cuts remain sealed. Coastal relief and tree crowns were refined. A final analytical audit fixed a one-cell coastal depression incorrectly absorbing 50.6 km² of ocean into its lake component.

Shared water corners replace artificial center dimples. Corners crossing a vertical block or section boundary now agree in world space, including all partial-level pairs. Animated and fallback presentation use the same rule; side UVs stay within the sprite and culling bounds include the extended geometry. Stable planar patches use fewer vertices and corner heights are reused.

Chunk columns/height halos and exact cave-noise endpoints are reused. Native cave output remains identical for the same inputs. Pressure/drain searches reuse bounded primitive scratch storage. Optional Vulkan shutdown continues resource release after a rejected completion; final validation counters include device destruction. A forced rejection regression verifies that the following completion still executes and tracked resources reach zero.

## Fresh validation

All evidence below was rerun after the workspace recovery. Earlier stages and failures remain in [native evidence](validation/0.0.1-alpha.24/native-evidence.txt).

- Full build, portable-core boundary and module boundaries pass. Nine seeds cover 5,846,893 centerline samples, 964 headwaters, 650 lakes, 4,292 connected flowing reaches and 538,915 bank-containment checks. Frozen V2 comparisons cover 18,000 additional samples; existing V1/V2 checks also pass.
- Exact cave comparisons cover 965,965 Y positions in the first suite and 761,165 in expanded clipped/empty columns. Native V2 retains the published nine-chunk block digest. V3 save/reopen and codec round trips were checked; the final fresh world is created, saved and reopened through both APIs.
- Final native geography checks cover five sites, 12,213 water cells, 80 material columns, 187 sealed banks and 55 connected routes, including all 33 actual land-to-sea reaches in the seed's region.
- A fresh generated basin runs the real solver for 1,000 ticks with 49 chunks forced loaded. Planned surface 153.1875; retained center 153.375–153.4375. Median tick work 15.43 ms, p95 24.25 ms. Pending work falls from 9,057 to 3,635 and still transfers water; this is retention evidence, not complete settling or regional hydrology.
- Native finite filling conserves volume, fills the test basin by tick four and reaches zero pending work. Communicating vessels, blocked passages and missing-chunk boundaries pass.
- Surface math checks include 441 planar samples, 20,000 bounded interpolation cases, 64 shared vertical-cell vertices and 1,024 vertical-section seam vertices covering all 256 partial-level pairs.
- Animated/fallback checkerboard and sloped water are captured and inspected on OpenGL and Vulkan. Native section-bound checks verify cached expanded bounds. Six terrain views plus final fresh coastal comparisons were inspected. Vulkan validation reports zero errors and zero tracked buffers/images after cleanup, including the forced rejected-completion test.
- Native Video Settings takeover, Performance value reopening, staged Apply/Revert, resize, presets/texture reload and full restart pass on both APIs. Persisted native value is 3 and custom pacing target is 89. Inventory/entity-preview captures remain upright after resource reload, with items visible in every row.

These are Linux Mesa software-driver checks on the test host, not owner-hardware FPS guarantees or Windows driver certification. Individual generation/tick times are workload diagnostics, not comparative performance claims.

## Installer audit

The [full audit](validation/0.0.1-alpha.24/installer-audit.json) checks every outer/nested ZIP CRC, module declarations, duplicate/test/helper classes, core platform isolation, native platform files, optional-backend provenance and identical common root/module payloads across profiles. A truncated workspace output was caught, rebuilt and rechecked after staging; it was never delivered.

| Profile | Bytes | Nested modules |
| --- | ---: | ---: |
| Default OpenGL + LOD | 606,553 | 4 |
| OpenGL without LOD | 569,371 | 3 |
| Optional Windows Vulkan + LOD | 11,537,948 | 5 |

The default contains no native libraries. The Windows alternative contains only the intended Windows DLLs and retains the documented existing VulkanMod/LWJGL notices/provenance. No TFC/Terralith/Tectonic, PhysEx, Distant Horizons or terrain-tool runtime was added.

Install one Deep Primal JAR. Create a new world to see V3 geography. The supplied V3 geography is now frozen; later terrain changes require a new saved revision.

## Continuing work

Repetitive contours, broader terrain variety and suspicious distant-water/LOD patterns remain visual work. Waves/animated/fallback/near/LOD passes need an enabled-view A/B diagnosis before attributing those patterns to one subsystem. Persistent searches beyond the current 128-cell window, renewable watershed/glacier/aquifer/ocean budgets, geological strata/ore hosts, intentional underground rivers, displacement, erosion/deposition, breaking surf and full-distance mixed-transparency coverage remain incomplete.

Three solver passes per 20 Hz host tick are nominally 60 substeps/second; no independent 60 Hz simulation or minimum hardware FPS is claimed. [Work state](ENGINE-WORK-STATE.json) and the [continuation handoff](ENGINE-BUILD-AUDIT-HANDOFF.md) govern the next isolated build/audit work. Hourly continuation and evidence-based updates remain enabled.


<!-- END PRESERVED SOURCE -->
