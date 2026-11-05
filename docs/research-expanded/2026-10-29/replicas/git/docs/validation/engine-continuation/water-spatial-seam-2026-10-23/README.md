# Expanded replica — docs/validation/engine-continuation/water-spatial-seam-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-spatial-seam-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-spatial-seam-2026-10-23/README.md |
| Preserved source SHA-256 | `018bac4f5cae7d536a731f3c025bc570cca3f2137a8977e236c07d672d6ddd6b` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reproduction of different-direction water seams at the same analytic shared edge and rejection of naive wrapped-UV averaging that destroyed uniform-flow fidelity. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the sampled-color reference was staged and did not use the real upload path for its spatial field, so no production seam fix was promoted here. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a reference-candidate evaluation matrix requiring both edge continuity and preservation of uniform material behavior. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Retain the 1,920 seam failures and same-direction controls, compare sampled-color endpoints with original images and require real upload, history-cap and mip tests before integration. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Different-direction water edges: reproduced; staged sampled-color reference

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Different-direction water edges: reproduced; staged sampled-color reference

Fresh checks on 2026-10-23 continue from `fab340ae07e9cf6a85e34b08bbaf0e53744357d8` on `work/engine-after-alpha24`. Main and supplied alpha.24 installers remain unchanged. V1/V2/V3 geography, quantity, solver, LOD, Vulkan/OpenGL implementations, production renderer and shaders are unchanged. **No production seam fix was promoted and no installer was released.**

## Actual changes

Added validation-only `WaterSpatialSeamChecks.java` and its `ClientCapture` action, plus the independently authored, staged `SharedFlowReference.java`. The fixture compares both cells at the **same analytic world-space shared edge**, using the production phase helper and shader with diagnostic vertices/atlas. It does not pretend that adjacent pixels at different locations should be equal. It does not use the real upload path for the spatial field.

The production mapping has a real discontinuity at a direction change: each cell rotates its UV field independently. A temporal fade within each cell cannot guarantee equal sampled colors across their shared boundary.

## Results and rejected candidate

Each of two final, sequential software Vulkan sessions (development and packaged renderer) records:

| Check | Result |
| --- | --- |
| Current shader, different-direction edges | Seams reproduced in 1,920/1,920 routes |
| Current shader, same-direction controls | 128/128 pass, byte-exact |
| Naive shared average of wrapped UVs, edge equality | 2,048/2,048 pass |
| That candidate, uniform direction fidelity | **60/64 fail**, maximum 240/255 channel error |
| Fixed equal-weight shared sampled-color blend | 2,048/2,048 edges pass, byte-exact |
| Sampled-color uniform-flow fidelity | 256/256 individual endpoint images match original, byte-exact |

The rejected wrapped-UV candidate is confined to validation code. It can hide an edge while folding/reversing the mapping inside a uniform stream. It was never put in production. The passing sampled-color diagnostic reuses the existing shader's two endpoints with fixed equal weights; it is a proof for an exact two-cell edge, **not** a finished four-corner field or its temporal transport.

The complete final diagnostic totals **24,832 draws, 12,416 pair comparisons and 25,427,968 bytes of GPU readback** across the two sessions. The native detector permits one diagnostic texel (8/255), but the independent strict audit requires and verifies **zero** error for every same-direction control and both staged edge candidates; it also rechecks every comparison from retained raw bytes. Max baseline edge error is 248/255. Axes X/Z and near, negative and distant origins are covered. These are not actual section-boundary uploads, natural streams, ocean-wave captures, alpha/resource-pack acceptance or GPU performance measurements.

## Staged implementation reference

`SharedFlowReference.java` builds a normalized direction distribution at canonical global corners using four neighboring wet cells, then bilinearly blends the **sampled colors**. No wrapped UV averaging is involved. A cell touches at most nine neighboring cells/directions. The executable reference passes 100,000 deterministic shared-edge checks and 17,000 uniform-direction checks, with equal weights exactly and maximum analytic color difference 1.3322676295501878e-15. It includes dry/still direction metadata, but its analytic periodic diagnostic is not validation of actual still-sprite, chemistry, tint or alpha semantics.

This reference is staged outside production. Actual GPU data packing/bindings, shared temporal histories, coherent neighbor capture, page-halo transitions, fallback behavior, bounded resident storage and practical sampling cost remain to implement. A naive consumer could require nine texture evaluations per fragment; do not promote it without a bounded design and matched profiling.

## Build, native and audit gates

The completed uncached serial build/core ran all 25 tasks; six archive CRCs, 409 compiled class identities and five module boundaries pass. All 569 existing source/build/test inputs were checked against the parent Git blobs: 568 unchanged and only `ClientCapture.java` edited, plus the new validation class. Retained actual-upload repeated/queued/stale-revision, 32,768-entry history cap/reset and world-phase/mip controls pass in both final sessions. They do not establish new cap-churn acceptance.

Explicit spatial audit records, frame-completion, stencil, normal world save/shutdown, crash deltas and final Vulkan `validationErrors=0`/`buffers=0 images=0` are required and pass. The shutdown fixture deliberately rejects one completion and requires the next completion to run. Audio-device startup failure and unused shader-output warnings are recorded environmental/existing warnings, not silently treated as a clean warning log. Gradle exit zero alone is insufficient. No OpenGL rerun was needed for this validation-only/staged checkpoint; earlier fallback results remain separately recorded.

The first native launcher overlapped the end of the initial build; that run saved normally and is excluded from all final counts. The final full serial build finished before either accepted native launch. Auditor corrections preserve mandatory shutdown-file and independently verified module-JAR origin gates. See `rejected-and-excluded.json` and `excluded/`.

## Reproduction and compact evidence

Run `python verify_compact_evidence.py` from this directory to restore compressed raw data in temporary storage and independently replay both strict reports. `compressed-data-manifest.json` records original and gzip lengths/SHA256. Sealed console hashes, command/action history, PNGs, raw pixel/detail records and source/input proofs are retained. `spatial-contact.png` was visually inspected; it displays diagnostic strips, not a real-scene acceptance image.

Run `python run_reference.py --java-home /absolute/path/to/jdk` to compile the staged reference with `--release 21` and execute its deterministic checks. For native reruns use the repository validation action `water-spatial-seam-check` with a working JDK25/Gradle9.2.1/Minecraft26.1.2 environment and software Vulkan or an owner GPU. Saved `native/*/command.json` and `tools/` document this isolated setup. The private compatibility agent and toolchain are not mod payloads. Build/package evidence identifies tested production bytes; this checkpoint is not an exported installer.

## Next implementation and open acceptance

Carry shared corner sampled-color weights, phase and history through actual mesh uploads with explicit vertex/texture/history/resident budgets. Reproduce across actual page halos and resource changes; retain uniform direction, temporal blends, height/opacity/edit behavior, filtering/mips and fallback. Then run real sustained streams/ocean, history-cap churn and matched CPU/GPU profiling. Keep the existing passing top/side/underside material transitions.

Production spatial seams, broader water budgets/compatible-liquid composition, bounded pressure/drain continuation/performance, distant rectangular LOD/horizon cutoffs, complete enabled-view/transparency/unload/edit coverage and coherent geology remain open. No independent 60 Hz, owner GPU/FPS, market-superiority or engine-completion claim follows. Continue the owner-authorized engine work and hourly updates.


<!-- END PRESERVED SOURCE -->
