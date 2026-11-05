# Renderer jam 25 — conservative LOD page frustum broadphase

Only the actual-chunk LOD renderer, its diagnostics and focused test/evidence files are in scope. This adds a cached union AABB per multi-part resident LOD detail, computed once after every accepted GPU mesh has its world-space bounds. Empty/singleton detail levels or malformed bounds use the previous per-part path; page early rejection cannot reorder opaque/water material draws. No GPU occlusion/indirect submissions are claimed.

Opt-in diagnostic keys: `lodCoarseRejectedPages`, `lodCoarseSkippedPartTests`. `lodTestedParts` and `lodRejectedParts` remain **fine** frustum tests only, and the bounds test count is not included in them. The preliminary Linux Java21 test double used plane-conservative visibility, 4,096 pages and six parts per page. Real GPU frame-time, pixel and backend tests remain owner-operated.

This is a source-only development checkpoint. It does not increase configured render distance, extend unseen-terrain delivery, change simulation/water chemistry, package native FSR/DLSS, or provide a validated JAR.
