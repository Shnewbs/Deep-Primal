# Expanded replica — docs/validation/engine-continuation/v8-conservative-density-bounds-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-conservative-density-bounds-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-conservative-density-bounds-2026-10-28/README.md |
| Preserved source SHA-256 | `c2572d8af5a0abdc75ef6363308b642aef5e1e8866fea59b62689dc48e01e273` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev42 closed-volume density certificates using outward interval arithmetic, conservative noise envelopes, expanded WARP axes and a 4,096-visit unknown fallback. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that a SOLID or EMPTY occupancy certificate is not a metric signed-distance bound for sphere tracing, and API-double checks do not qualify the complete graph. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a certificate proof contract with uncertainty propagation and exact fallback, explicitly separate from any future distance-field lighting representation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Exercise adversarial warp and floating-point intervals against exact samples, reject noise-zero mutants and require uncertain outcomes when work bounds expire without skipping exact occupancy. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deep Primal dev42 — conservative V8 density-volume certificates

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev42 — conservative V8 density-volume certificates

**Source-only version:** `0.0.1-alpha.29-dev.42-v8bounds.1`. No qualified JAR or GPU build. Direct-main source checkpoints: `1a780e0636593aaf034fad9b076e07d084a1986d`, `bbe719cf6b9fa64952a9c57657eaa2220cfebece`, `e26b1d6481ae8cc62892784c55de7b550e09aeb5`.

## Coordination

Consumed independent audit `docs/engine-hourly-audits/2026-10-28T0029-bcce835/REVIEW.md`, pinned to `bcce8355c7f8288e2749253578398e68c107bcda`. The seven single-cell water postwrite gaps identified there have been independently addressed in foreground dev41 commit `8756bd021713`. Its separate water writer owned HydrologyRuntime and gradle.properties until 2026-10-28 01:15 UTC. This engine cycle chose non-overlapping V8 code; the version increment was made only after that lease had expired, after checking current main and version. Renderer GPU-ledger files and its source were untouched. No parallel checkout or build-output reuse.

## Implemented

New `engine-core/.../density/DensityVolumeBounds.java` is an optional V8-only whole-box solidity preflight: `SOLID`, `EMPTY`, or `UNCERTAIN`. The interval proof covers a **closed volume**, not merely corners. It handles every current graph operation; noise always uses a conservative whole-field envelope rather than node interpolation; WARP expands all affected spatial axes; interval arithmetic rounds outward; invalid input is rejected; and a 4096-visit work ceiling returns unknown. The caller **must** use exact density when uncertain. It has no active generator integration and does not change frozen V1–V7 terrain or saved profiles.

`src/coreChecks/.../density/DensityVolumeBoundsChecks.java` is registered in `CoreChecks`; it performs exact-sample differential checks on linear, noise, warp, absolute, clamp, smoothstep, lerp, max, and a bounded V8 recipe corpus. This stage offers a potential future safe occupancy shortcut, **not** an accepted optimization or V8 promotion. Unaccepted R57/R60/R61/R62 overlays remain staged.

## Executed and limitations

- Local `javac --release 21 -Xlint:all` and `java -Xverify:all` against exact Git-blob-matched new production class and test bytes with lightweight **API-compatible doubles** for the unavailable full compiled graph: 1,037 standalone checks plus 65,881 integrated-class checks. This is **not** a complete product-graph run.
- Initial model mutation of noise-to-zero escaped tests. Added a positive-noise witness; the final fixture rejected all three deliberately invalid variants (noise-zero, WARP displacement ignored, and ABS missing interior zero). Rejected initial run retained, not claimed passing.
- Git blob identity: `DensityVolumeBounds.java` = `75c955721e93ae07c00604acdae99e431885165d`; final test = `446679e85b450316ce08c76f4301019fc94c86e8`.
- This environment has Java21.0.12, no owner JDK25 or archive mounted, and shell GitHub DNS fails. GitHub connector provided guarded direct-main writes. **Not run:** Java25/Gradle build, actual compiled graph tests, native V8 materialization/chunk/profile/save parity, hardware performance, OpenGL/Vulkan water/LOD, Windows AMD8060S, full engine grade. October 27 owner override assigns full builds to the owner.
- Last fresh complete external grade remains **50/100 (5.0/10)**. Requirement is strictly **greater** than 8.5/10 **and** all water gates. Neither is met.

**Next:** Run this new class against real Java25 DensityGraph and native V8 shape/materialization, add adversarial floating-point/warp bounds, integrate certified region filling only after exact chunk topology and saved-profile tests. Independently keep water source/postwrite, persistence, connected cliffs, long-soak and named-GPU performance gates open.


<!-- END PRESERVED SOURCE -->
