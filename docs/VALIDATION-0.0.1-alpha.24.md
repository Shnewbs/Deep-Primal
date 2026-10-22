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
