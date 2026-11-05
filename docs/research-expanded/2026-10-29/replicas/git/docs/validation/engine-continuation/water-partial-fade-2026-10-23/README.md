# Expanded replica — docs/validation/engine-continuation/water-partial-fade-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-partial-fade-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-partial-fade-2026-10-23/README.md |
| Preserved source SHA-256 | `af8e185d085a0468b3773b4f41e18d8877405339421d6d46b7a2377af3b29622` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Preserving drying quads inside mixed wet/dry pages and carrying original opacity start offset so repeated uploads do not restart the fade. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that alpha endpoints retain 1/127 quantization and unchanged vertex size; the checkpoint does not implement regional reservoirs or chemistry. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a per-quad transition lifetime contract independent of page rebuild draw counts, with original age and remaining cleanup duration preserved. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Dry one patch while another stays wet, rebuild mid-fade and assert both the disappearing quad's lifetime and the surviving patch's geometry without extending the deadline. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Mixed wet/dry page fade continuity — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Mixed wet/dry page fade continuity — 2026-10-23

Continues `c2294b4b` on the working branch. The owner asked for seamless water and continued actual implementation/self-audit. This checkpoint fixes another demonstrated premature-disappearance path and records the owner's headwater/composition clarification. It does not finish the engine or implement chemistry/reservoirs. Main/installers, authoritative water quantity/solver, engine core, LOD, Vulkan backend and published V1/V2/V3 geography remain unchanged.

## Reproduction

An isolated production renderer uploads two wet patches in one page: 40 real GPU vertices (1,440-byte capacity). One patch dries at controlled t=100 ms; the other stays wet. At t=125 another coherent worker mesh sees only the remaining wet patch. The baseline rebuild replaces the GPU draw count with 20 vertices, deleting the first patch 25 ms into its 100 ms fade. Explicit native assertion/crash context are retained. A preceding first-action timeout never entered this fixture and is excluded; it proves nothing about this defect. Both outputs are sealed and preserved.

The independently authored fix retains only absent face groups whose opacity is still fading toward zero. Current/rewet geometry immediately replaces matching groups. Retained vertices keep their original capture revision, so they cannot masquerade as a new height observation and override newer shared-corner targets. Retention stays within admitted GPU vertex geometry; release/reset and completed fades clear history. Arrays are immutable and reused on subsequent recaptures to avoid copying unchanged dying faces. The focused fixture holds five face groups / 1,120 CPU float bytes, and verifies original revision, array identity, rewet cancellation and completed partial cleanup while the second patch remains visible. The general bound follows admitted vertex capacity (float payload is 56/36 times its GPU vertex payload, plus bounded per-face record/map overhead); this is not a total process/VRAM budget or a frame-time guarantee.

Opacity formerly restarted its 100 ms interval every upload. The GPU format's unused signed-normal third byte now carries the original start offset (-100..0 ms); the shader evaluates it against elapsed page-upload time. Alpha endpoints retain existing 1/127 quantization. Cleanup uses the actual remaining transition duration, preserving the original deadline through repeated captures. Height interpolation and the vertex format size remain unchanged; no water is added, removed or frozen.

## Fresh validation

The native candidate retains all 40 vertices at t=125. Six fenced GPU readbacks inspect 8,640 bytes: the drying patch has opacity .75 and .5; rewetting begins at .5 and advances through .625, .875 and 1 while the other patch stays opaque. Deadlines remain t=201/t=251 rather than extending on every rebuild. A second completed partial fade releases its CPU history/draw faces while retaining the other wet patch and same allocation. Readback uses a test-only legal COPY_SRC buffer on the real page's production GPU update path. Opacity is evaluated from the actual packed shader inputs, not sampled from displayed pixels; these controlled snapshots/time do not prove live-scene temporal coverage. Real scene screenshots still have repeated patterns/bands and cannot establish absence of flicker.

513 parent input paths are Git-blob verified; final comparison is 510 unchanged, two production renderer/shader files plus one validation hook changed and one opt-in fixture added. Full sealed uncached Linux Vulkan/core/module/geography build passes 25 executed tasks, including 10,000 animation retargets/40 shared-edge samples and 500,000 conservative transfers. Six outer/nested CRCs, 408 compiled-class matches, exact shader resource identity and five nested/standalone module identities pass. JAR SHA: bfdbcd957ab9f79610380f50ddab8977209562e94c01e775a896f65a23e89f88. No installer is released.

Two final strict development/packaged software-Vulkan sessions pass partial fades/readbacks/CPU cleanup and all prior water reuse/dry-fade, LOD retry/eviction/index/edit/capture, Vulkan device/frame/reload/normal-save/shutdown controls, with live/sealed hash identity, no new crashes/save/VUID/validation errors and zero final tracked GPU buffers/images. A separate scoped software OpenGL fallback session repeats partial/readback/reuse/fade/reload/save checks; Vulkan stays primary. Earlier candidate sessions preceded the final immutable-array/cleanup assertions and are not substituted for final-source validation.

## Owner clarification and next implementation

The adopted [water requirements](../../../WATER-BUDGET-REQUIREMENTS.md) now explicitly place metered stream supply at headwaters and separate chemistry from physical water volume. Compatible river/ocean water shares the volume solver; contact-only chemistry changes no cell levels, and actual flow advects conserved constituents. Stable average sea level still requires an accounted ocean exchange. Other liquids need miscibility/layering/reaction rules. Sparse versioned cell/chunk composition is planned; separate fluid IDs do not implement it automatically. These are recorded requirements, not shipped reservoir/composition behavior.

Next capture actual live-flow frame/coverage sequences across shared horizontal/vertical page and native/animated/LOD handoffs, including reload/travel/memory pressure, and trace flow UV/timebase. Keep regional reservoirs/constituents, pressure p95/waiter/residency, spatial/paged/coarser LOD, player-input/server physical unload/reopen and coherent geology open. Catalog/species stays deferred. Prior marker/console writer, atlas/reload/direct-stop failures and this first-action timeout remain unresolved. No independent 60 Hz, owner GPU/FPS, market-superiority or complete visual acceptance claim.

Reproduce with isolated project/toolchain using build_candidate.py and audit_vulkan_package.py, then run_vulkan.py partial-finished and partial-finished-packaged sequentially with private DEEPPRIMAL_CAPTURE_ROOT and disposable DEEPPRIMAL_NATIVE_RUN_DIR. Symlink captures beside the scripts for audit_water_partial.py; run reuse-gl-partial-final with audit_opengl_partial.py for fallback. Restore before-source renderer/shader to reproduce the baseline assertion. Respect the work lease.


<!-- END PRESERVED SOURCE -->
