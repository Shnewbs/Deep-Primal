# Expanded replica — docs/renderer-jams/20261029-page-frustum-broadphase/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-page-frustum-broadphase/README.md) |
| Source identity | git: docs/renderer-jams/20261029-page-frustum-broadphase/README.md |
| Preserved source SHA-256 | `ffd0047e4db0e246ed55223d9bef0e71dbc430f733758a897bacb01d05107b25` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds a cached union AABB for each complete multi-part resident LOD detail and rejects a wholly off-frustum page before ordered fine-part tests. Empty, singleton, malformed, or nonfinite bounds use the previous fine path. Coarse counters remain separate from fine-test metrics.

## Research expansion — gaps and details to deepen

- Cached bounds are correct only for the geometry and coordinate convention they contain. The note needs an explicit validity rule for any subsequent water-height publication, deformation, source replacement, or origin change; static union tests do not establish those lifecycle cases.

## Research expansion — proposed contracts

- Bind the union to the complete page, geometry revision, and declared world-space convention. Rebuild it or prove conservative expansion after permitted position changes; invalidate coarse rejection when that proof is unavailable. Preserve material order and independent fine counters.

## Research expansion — acceptance checks

- Verify every rendered/deformed part remains contained after publication and in-place updates, with fallback on invalid metadata. Run native all-visible and offscreen scenes, near handoff, signed coordinates, and water transitions; record total cull/frame cost rather than describing CPU broadphase as GPU occlusion.

## Research expansion — source anchors

- Renderer jam 25 — conservative LOD page frustum broadphase

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 25 — conservative LOD page frustum broadphase

Only the actual-chunk LOD renderer, its diagnostics and focused test/evidence files are in scope. This adds a cached union AABB per multi-part resident LOD detail, computed once after every accepted GPU mesh has its world-space bounds. Empty/singleton detail levels or malformed bounds use the previous per-part path; page early rejection cannot reorder opaque/water material draws. No GPU occlusion/indirect submissions are claimed.

Opt-in diagnostic keys: `lodCoarseRejectedPages`, `lodCoarseSkippedPartTests`. `lodTestedParts` and `lodRejectedParts` remain **fine** frustum tests only, and the bounds test count is not included in them. The preliminary Linux Java21 test double used plane-conservative visibility, 4,096 pages and six parts per page. Real GPU frame-time, pixel and backend tests remain owner-operated.

This is a source-only development checkpoint. It does not increase configured render distance, extend unseen-terrain delivery, change simulation/water chemistry, package native FSR/DLSS, or provide a validated JAR.


<!-- END PRESERVED SOURCE -->
