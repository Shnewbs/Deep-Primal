# Expanded replica — docs/VALIDATION-0.0.1-alpha.27.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.27.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.27.md |
| Preserved source SHA-256 | `aece4ae157ac98413ce967543fb72f3245d0aa93a5a93701d045483ef5074819` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Alpha.27 terrain validation — 2026-10-24”. Its original opening states: “New worlds use V6. Saved V1–V5 worlds retain their generation and edits. This is a terrain alpha, not full-engine or Windows/owner-GPU acceptance. See scope and configuration.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Alpha.27 terrain validation — 2026-10-24
- Failures caught before release
- Reproduction and evidence

Related design: [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/alpha27/` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/alpha27) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.27 terrain validation — 2026-10-24

New worlds use V6. Saved V1–V5 worlds retain their generation and edits. This is a terrain alpha, not full-engine or Windows/owner-GPU acceptance. See [scope and configuration](TERRAIN-V6.md).

## Delivered archive

`DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-windows.jar`, 124,952,383 bytes.

SHA256: `c37d5c493ce90dfdf7e74fd049eff8b972e00ef7a2f893f845d00686d0ea7c10`.

Outer ZIP and all six nested module CRCs pass. The Vulkan module contains Windows native libraries and no Linux/macOS native libraries. Its 4,257 root/Vulkan Java classes match the Linux bundle; the other five module JARs are byte-identical. Private validation classes are excluded. This verifies packaging and common code, not Windows execution.

## Accepted gates

| Gate | Result |
| --- | --- |
| Full build | 32-task behavioral build, core/module boundary checks and tests pass. Release packaging rebuilt after correcting the creation-config description from V3 to V6. |
| Source/payload identity | Independent JDK25 compilation matches 49 core sources / 87 classes. Packaged checks match 502 compiled project classes; native fixtures record loaded identities. |
| V6 geography | 8,712 columns across two seeds and four climate regions; 110,398 protected cave cells; 1,281 icy columns; 37 witnessed surface-biome indices; supported tunnel transect; parallel/order/cache-eviction stability. |
| Authored deposits | 1,559,040 independent wider-oracle comparisons, 13,631 ore hits, four shapes, three vertical systems and signed chunk seams. Existing V5 checks also pass 4,194,304 prior-implementation comparisons and 90,960 wider-oracle cases. |
| Standard native V6 | 25 selected chunks, 6,400 columns and 1,948,226 actual-block/base-column comparisons per run. Includes 61,167 cave-air cells, 4,598 packed/blue ice blocks, 35,841 water blocks, 1,915 ore blocks and 63 tunnel columns. All eight ores have witnesses; 16 transverse columns verify side support. |
| Deep native V6 | Saved Y=-384..639 / 1,024-block height; 3,533,277 block comparisons per run. |
| Save/reopen | Standard and Deep preserve profiles, edits and regenerated protochunk digests after all creation-time feature defaults are changed. |
| Environmental water | Shared-aquifer debit, blocked outlets, infiltration, bucket chemistry, transport, ocean stage, atmospheric weather debit, one-time initialization and no forced remote chunk loads pass. Groundwater, chemistry and edits survive restart. |
| Ice and support | One physical ice block pays one finite water block; cold/covered ice and repeat refills are rejected. Cave-ins preserve rock, respect construction supports and refuse to replace water. |
| Older generations | Final packaged run matches all 120 V1–V5 native digests plus 128 expanded ore-coverage digests from R51; 8,393,529 base-column blocks, 1,305,522 ore oracle blocks and 240,260 actual saved-chunk blocks checked. |
| Vulkan client | Actual Create World, join, save, reopen and four native terrain captures pass on Mesa software Vulkan. Zero VUID errors/crashes, saved disconnect, rejected-callback shutdown continuation, and zero final tracked buffers/images. |

Native fixtures deliberately select rare feature witnesses. Ore/ice counts are not unbiased density or balance estimates. Software-driver captures at a five-chunk view and per-chunk generation timings do not establish owner-GPU/FPS, full-scene memory or comparative latency. Startup/teleport overload warnings remain in the logs. No new OpenGL or Windows runtime acceptance is claimed.

## Failures caught before release

Compilation caught a switch-expression syntax error and a pinned NeoForge event/import mismatch. Two early native coverage gates lacked selected ore/glacier witnesses and were rejected. A covered-ice test exposed asynchronous lighting as an unsafe exposure check; melt now reads the actual surface height. Initial Vulkan inspection revealed narrow unsupported roof strips despite a passing vertical-roof test. V6 now requires a covered cross-section, restores the surrounding bank footprint and protects it from cave carving. Lateral-support checks and the corrected capture pass. Failed native runs were rejected even when Gradle returned zero. Their intermediate archives are not delivered.

## Reproduction and evidence

Compact results and launch scripts are under [validation/alpha27](validation/alpha27/). Use Java25, Gradle9.2.1, Minecraft26.1.2 and NeoForge26.1.2.122. Scripts identify this run's private pinned toolchain; adjust local paths when restoring. Use root-only game task dispatch and unique fresh world directories. Require explicit PASS records, normal saves, no crash reports, independent compilation and packaged identities. Incremental outputs in this environment have previously been stale.

Full logs and native PNGs are retained in `DeepPrimal-alpha.27-validation.zip`, including the rejected and corrected tunnel captures. Broader pressure continuation, enabled-view water/LOD quality, extreme authored profiles, surface landslides, dynamic ice-sheet movement, optional KubeJS authoring, external editor and content catalogs remain outside this release's acceptance.

[Windows JAR](https://chatgpt.com/library?file_id=libfile_0c1facd1de4481918eeddfe9387a4498&account_id=personal) · [Full logs and captures](https://chatgpt.com/library?file_id=libfile_c7c0c6a815508191b26f1f91dcb05bc0&account_id=personal)


<!-- END PRESERVED SOURCE -->
