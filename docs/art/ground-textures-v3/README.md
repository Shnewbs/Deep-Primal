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
