# Expanded replica — docs/art/ground-textures-v3/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/ground-textures-v3/README.md) |
| Source identity | git: docs/art/ground-textures-v3/README.md |
| Preserved source SHA-256 | `18cbba56bbf43cb949771cbb52275c37a5e2064f33c3faa5e6e9043231def889` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The ground-v3 document records a superseded attempt to remove a dark top cap by regenerating and processing side textures, including grayscale conversion, blur subtraction/recentering, shallow complementary masks and retained sand. It explicitly separates CPU/source proof from runtime material and native validation. Subsequent owner direction rejected the pale, sandy/noisy appearance and restored approved-source v4 artwork. Therefore v3 is useful negative design evidence and a reproducible experiment, not the current approved ground asset or a starting point for automatic 32-pixel conversion.

## Research expansion — gaps and details to deepen

- The source-specific rejection should be visible before the recipe so a reader cannot mistake a technically reproducible export for an approved visual target. Border continuity and matching soil rows were measured, but those metrics did not capture the loss of intended grain and root structure. That difference belongs in the acceptance rationale.
- The document's original statements about masks not yet used and no fresh native validation belong to that experiment's time. Later shader work should not be back-projected into the v3 evidence. Keeping a clear link to v4 avoids treating a newer renderer as retrospective approval of rejected artwork.

## Research expansion — proposed contracts

- Add a superseded/rejected asset-state record naming the owner-observed visual problem, exact source/export identity and approved successor. Preserve the recipe and source evidence for traceability while excluding these bytes from current runtime manifests and from any generic latest-directory selection rule.
- Use this attempt as a negative art-review fixture: automated seams, complementary masks and grayscale consistency may all pass while the result fails the intended material character. Require structural grain, side-root transition and broad-field repetition review alongside export validation, with separate decisions for source quality and renderer correctness.

## Research expansion — acceptance checks

- Verify current manifests resolve to v4 and final sand rather than v3, and that a rebuild cannot accidentally choose assets by directory date or lexical order. Confirm protected current assets remain byte-identical when this historical experiment is archived or documented.
- Reproduce v3 only in an isolated comparison destination when needed, label its images rejected, and compare owner-visible structure at native scale. Do not promote it merely because repeat-edge or mask arithmetic tests pass, and do not claim a new native test unless run.

## Research expansion — source anchors

- Ground-side correction and final sand
- Evidence
- Reproduction

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Ground-side correction and final sand

The owner screenshot exposed an asset defect: the v2 side export embedded a dark cap and reused broad soil mottling. Matching boundary pixels had not established visually quiet repetition. This revision replaces only dirt_side, grass_side and podzol_side plus their four surface/soil masks. The three approved top textures remain byte-identical. Final sand is added as ground/sand.png for all faces.

The side materials were reauthored with the built-in image-generation tool, using the approved dirt art and defective sides as references. Exact prompts and masters are retained here. Export converts to 64x64 grayscale, removes broad baked illumination with a five-texel blur reference, centers luminance at 192, and preserves fine detail with a 1.35 gain. A common lower soil substrate replaces the lower portion of grass/podzol faces, with a five-row transition. Three-texel wrap bands match boundaries. Grass and podzol repeat horizontally only; dirt repeats on both axes. This does not guarantee invisible repetition or connected-material transitions.

Surface masks now define a shallow irregular edge and are zero below row 15; soil masks are exact complements. They are authored region weights, not semantic leaf/root segmentation. Multiply grayscale luminance by the surface/soil weighted color, using the existing tint contract. These files do not implement that renderer integration.

## Evidence

Seven corrected PNGs independently decode as 64x64 grayscale; intended wrap edges match; all 8192 mask pairs sum to 255; lower soil rows 16-60 match across the three side types; a second export reproduces every corrected PNG byte-for-byte. checks.json contains hashes and measured cap/body luminance. The old/new cap-minus-body differences are dirt -15.00/+1.02, grass -15.07/-2.76 and podzol -26.84/-2.82. Thus the cap is no longer encoded as a broad dark stripe.

side-proof.png shows grass, dirt, podzol from left to right, each repeated twice horizontally. Top row is grayscale; bottom row uses illustrative CPU tint swatches. It is not a game screenshot, actual shader/lighting test or approved final tint palette. The 64x64 sand and its 8x8 proof are retained in ../sand-final. The runtime sand bytes match the final owner-requested revision exactly.

Main advanced during this export from 3b86383f57d7d8b098018097091714f442271664 to d94f9cbe6f9cd4713cd926a7c1ab5379edaa2df2. The newer snapshot adds ground block models and GroundMaterialColors; all those changes are preserved. Its ground_material model uses tint index 1 (#b6a080) for the entire side, so the surface masks are still not sampled. current-soil-tint-proof.png demonstrates this actual constant color applied on the CPU, without game lighting. The green/litter fringe in side-proof.png illustrates the intended future two-color material path and is not the current model behavior.

This commit fixes the source assets and adds sand. No renderer code, geography, engine state, release or JAR is changed. Existing model references automatically resolve the corrected side files in subsequent builds. Sand is uploaded as an asset only. The inherited integration's earlier native checks do not validate these replacement images: native resource reload, mipmap/lighting behavior and screenshot matching for this revision remain untested. Existing v2 export hashes/proofs describe the historical export, not these current side files.

## Reproduction

Requires Node.js, ImageMagick and Pillow for the independent checker. Export into an isolated directory:

    node tools/ground-textures/export-sides-v3.mjs docs/art/ground-textures-v3/masters build/sides-v3

Run it again into build/sides-v3-repeat, then check against the v2 files from commit 5c54904a1316b27d6512aa219f938867d31a49f5:

    python3 tools/ground-textures/check-sides-v3.py build/sides-v3 PATH_TO_V2_GROUND_DIRECTORY build/sides-v3-repeat

The exporter never writes into src unless explicitly given that output path. The v2 export tool is historical and should not be used to replace the corrected side files.


<!-- END PRESERVED SOURCE -->
