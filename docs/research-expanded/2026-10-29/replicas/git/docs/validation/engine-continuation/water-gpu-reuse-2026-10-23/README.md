# Expanded replica — docs/validation/engine-continuation/water-gpu-reuse-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-gpu-reuse-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-gpu-reuse-2026-10-23/README.md |
| Preserved source SHA-256 | `3d9483436ed223198e3364048e9d9fa8cececb50a619616bcee54a866de1de53` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reusing existing water GPU capacity for same-sized updates when allocating old plus new meshes would stall at the 32 MiB budget. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the focused update recovery does not establish every live-scene seam or whole-process peak memory. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a GPU allocation contract separating capacity, active draw size, in-place update eligibility and growth requiring peak reservation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Alternate same-sized uploads at capacity, shrink and regrow without replacing the object, preserve untouched bytes and keep oversized updates pending with old coverage until capacity is freed. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Water GPU updates under memory pressure — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water GPU updates under memory pressure — 2026-10-23

Continues `653c5bcd` on the unreleased working branch, following the owner's seamless-flow priority. This is a demonstrated stalled-update fix, not complete live-scene flicker acceptance. Two production files change: the water renderer and the shared graphics module's water GPU mesh. Authoritative water quantities/solver, core, LOD, Vulkan backend and published V1/V2/V3 geography remain byte-identical. Main and supplied installers remain unchanged.

## Reproduction and independent fix

The baseline's upload admission charges the new water mesh while retaining its old allocation. A same-sized update can fail at the 32 MiB budget when all other pages are closer or busy, leaving the existing water geometry stale and repeatedly retrying. The native fixture creates a real 46,080-byte water page and 33,508,224 bytes of actual GPU ballast in a closer page. Resident payload is 33,554,304 bytes, 128 below the cap. The next same-size update is rejected: `accepted=false`, followed by the expected assertion `Near-capacity same-size water update stalled or displaced closer coverage`. The baseline crash, explicit failure and sealed logs are retained; it is not counted as passing validation. Controlled pages are not drawn into the lake screenshot.

The renderer now reuses a page's GPU allocation when the new data fits. WaterGpuMesh creates an updatable vertex buffer, validates quad count/byte length/capacity, writes through the shared CommandEncoder API, then updates the index count. Full allocation capacity remains charged after smaller updates, allowing safe regrowth without understating resident memory. Oversized replacements retain the previous admission/closer-page protections and retry path; this does not allocate old+new beyond the existing payload budget or steal closer coverage. The cap is owned water vertex payload, not a total VRAM guarantee: staging, deferred frees and driver overhead are separate. Existing Vulkan transfer barriers handle prior reads and following vertex reads; the backend itself is unchanged. The OpenGL fallback uses the same public GPU API.

Native candidates execute 16 alternating real same-sized water uploads at the near-capacity occupancy, verify the same GPU object and unchanged resident capacity/closer ballast, then shrink and regrow. An oversized update stays pending with old coverage, and succeeds after actual ballast is released. A separate test-only mesh adds legal COPY_SRC solely for readback: updating its first 144 bytes preserves the remaining 144 bytes, with all 288 GPU bytes checked after a fence. Invalid count/byte shape is rejected before writing. This readback uses the production update method but a test-only readable-source buffer; it does not certify the displayed river pixels. All isolated GPU/worker ownership is released.

## Fresh checks and limits

512 parent input paths are Git-blob verified; final scope is 509 unchanged, two production files plus one validation hook changed and one new opt-in fixture. Full sealed uncached Linux Vulkan/core/module/geography build passes 25 executed tasks. Existing water/core checks pass 10,000 animation retargets, 40 shared edges, vertical/page shape controls and 500,000 conservative transfers. Six outer/nested archive CRCs, 407 compiled-class matches and five standalone/nested module identities pass; no validation/helper payload leaks. Development JAR SHA: eba28781328e21b45bb8515f44094a915f84b93c85a5e7227e565f8e08535cdc (unchanged through native validation; no installer release).

Two strict development/packaged software-Vulkan sessions pass memory-pressure reuse, GPU payload, earlier dry-fade/deadline/rewet/edit protection, index bound, LOD retry/eviction/edit/capture, frame/device/reload/normal-save/shutdown gates. Explicit pass records and producer-sealed/live hashes are required in addition to Gradle exit; both sessions have no new crash/save/VUID/validation errors and zero final tracked buffers/images. A third scoped OpenGL software-driver session passes water reuse/payload/fade, reload, capture and normal save; it is fallback coverage, not a full backend comparison. Vulkan remains the primary reference API. All screenshots are inspected as limited still-scene evidence; fine repeated water patterns/darker bands and repetitive terraces remain visible. No temporal/no-flicker conclusion follows from them.

The first OpenGL attempt was rejected despite Gradle exit zero: it inherited a Vulkan-only shutdown injection and logged `Shutdown fixture requires actual Vulkan`. This is a harness configuration failure, not evidence of a water/GPU implementation failure. Its sealed output and rejection reason are preserved. The corrected runner removes only that Vulkan-specific injection in OpenGL mode, and the separate final fallback session must pass the strict audit.

No independent 60 Hz, owner-GPU/FPS, market-superiority or complete enabled-view claim. Existing pressure p95/waiters/8192-node residency, regional water budgets, geology, spatial/paged/coarser LOD, actual player-input/server physical unload/reopen, scene overlaps/gaps and marker/reload/atlas/direct-stop failures remain open. Staged retained-drain source stays separate. Catalog/species stays deferred.

## Next work

Capture live-flow frame/coverage sequences across adjacent horizontal/vertical pages and native/animated/LOD handoffs, including dry/rewet, reload/travel and real memory pressure. Trace stable flow UV/timebase and remove only reproduced discontinuities while preserving water quantity and player edits. Isolated buffer reuse closes one cause of stale geometry; the engine acceptance register is not complete.

Reproduce in an isolated project/toolchain with build_candidate.py and audit_vulkan_package.py. Run run_vulkan.py reuse-final and reuse-final-packaged sequentially with private DEEPPRIMAL_CAPTURE_ROOT and disposable DEEPPRIMAL_NATIVE_RUN_DIR; symlink captures beside scripts for audit_water_reuse.py. Run reuse-gl-final and audit_opengl_reuse.py for the fallback. Restoring both before-source files reproduces the baseline assertion before the candidate-only payload checks. Respect the work-state lease.


<!-- END PRESERVED SOURCE -->
