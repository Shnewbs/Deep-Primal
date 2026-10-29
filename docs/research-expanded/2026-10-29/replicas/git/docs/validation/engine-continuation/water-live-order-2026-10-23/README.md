# Expanded replica — docs/validation/engine-continuation/water-live-order-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-live-order-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-live-order-2026-10-23/README.md |
| Preserved source SHA-256 | `db15a130025b4189e9afafb7a6e7fe81bf87b0e1ffb0a06ea2285344b91a6ee1` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Validation of actual water pixels and page ordering after the suspected insertion-order bug was not reproduced because visible pages already sort far-to-near. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that two translucent quads demonstrate an ordering consequence, not a production sorting defect or complete primitive transparency correctness. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an evidence classification explicitly marking non-reproduced suspicions and distinguishing page order from within-page transparent geometry. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Permute actual page insertion order, compare the extracted visible order and controlled readback, restore world state and avoid describing this validation-only checkpoint as a renderer fix. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Actual water pixels and page-order audit — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Actual water pixels and page-order audit — 2026-10-23

Validation only: no new production fix in this checkpoint. Continues e3f46dde. The suspected insertion-order defect was not reproduced: production already sorts visible page bounds far-to-near. The new opt-in fixture permutes the actual page map near/far/near, checks the actual extracted VISIBLE list and restores the original order without changing water/world state. Do not present this as a sorting fix.

The real production pipeline renders two overlapping translucent water quads into 32x32 RGBA8/depth targets. Near-first yields center RGBA128,0,0,128; far-first yields128,0,64,192. This demonstrates a controlled order consequence; it is not proof of an existing production bug or complete primitive transparency correctness.

Two final development/packaged Vulkan sessions and one scoped OpenGL fallback each test three actual views: animated water with waves off/LOD on, waves on/LOD on, waves off/LOD off. Each waits for a stable visible-key set for12 frames and captures3 consecutive actual water-stage frames at a fixed camera while permuting page insertion order. Total27 live frames across9 views, plus18 controlled pixel renders. Source fixture reports actual wave/LOD activity: this lake has zero resident ocean-wave bytes, even with the waves option enabled. Thus ocean-wave geometry remains untested. LOD geometry is resident/visible in enabled profiles and absent in disabled profiles. Frames are taken after translucent blocks, before final postprocessing/HUD. Scene physics/texture animation continues; this is not a matched equal-state A/B or full water-coverage acceptance.

Nine lossless RGB VP9 clips preserve all27 actual frame pixels. Decoding is byte-compared to source PNG RGB; alpha is255 throughout. Per-frame PNG and decoded RGB hashes and real nanosecond/game-time records are retained.5FPS playback is illustrative, not measured FPS. Inspected frames show fine water patterns/darker bands in all profiles and terraced terrain; no seamless-flow claim. Original PNG serialization is not retained for those frames; exact decoded pixel content is. Controlled32px PNGs and ordinary recovery stills are retained.

514 parent source/build inputs are Git-blob verified;513 remain unchanged, one validation hook changes and one opt-in fixture is added. All production source remains unchanged. The full uncached sealed build passes25 executed tasks,10,000 animation retargets/40 shared-edge samples,500,000 conservative transfers and frozen geography checks. Six outer/nested CRCs,408 compiled-class matches, exact shader resource identity and five nested/standalone module identities pass. Development JAR SHA256: bfdbcd957ab9f79610380f50ddab8977209562e94c01e775a896f65a23e89f88. No installer or main change.

Strict native audit requires explicit passing records, completed actions, sealed/live console equality, no new crashes/save/VUID/validation errors and normal save/disconnect. Final Vulkan tracked buffers/images are zero. Prior mixed-page six readbacks/8,640bytes/CPU cleanup, near-capacity GPU reuse, dry fade continuity, bounded8192-key LOD merge, actual partial-upload pressure/retry/edit/capture, frame/device/reload/save/shutdown checks repeat. The deliberately rejected completion callback is followed by shutdown continuation. The first new harness compile attempt failed due to ambiguous wildcard JOML Math imports. An initial OpenGL fixture run crashed because sampled textures lacked USAGE_TEXTURE_BINDING; named legal texture flags correct the fixture. Both failures are excluded/preserved, and final source is rerun on both APIs. Earlier prototype passes predate final actual-wave/LOD instrumentation and are not substituted for final-source checks.

Source audit finds a concrete remaining rendering gap: EngineFluid exposes FLOW/SPEED, but animated-water capture/meshing drops it and all animated cells use water_still. Next implement focused flow-aware texture/UV behavior with tests, preserving quantity, bounded memory, shared timing and edits; then obtain actual ocean-wave/shared-edge and longer live-flow/native/LOD coverage. Regional reservoirs/chemistry, pressure p95/waiters/residency, spatial/paged/coarser LOD, geology and prior marker/reload/atlas/direct-stop failures remain open. Catalog/species remains separately deferred. Software drivers establish no owner GPU/FPS, independent60Hz, market superiority or complete engine acceptance.

Reproduce sequentially with build_candidate.py/audit_vulkan_package.py, run_vulkan.py order-validated and order-validated-packaged, then reuse-gl-order-validated. Supply private DEEPPRIMAL_CAPTURE_ROOT and a disposable DEEPPRIMAL_NATIVE_RUN_DIR with the saved lake. Symlink captures beside scripts; audit_water_order.py and audit_opengl_order.py verify records. make_frame_video.py verifies exact decoded pixels. Respect the work lease and do not run competing native/Gradle jobs.


<!-- END PRESERVED SOURCE -->
