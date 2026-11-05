# Lighting and renderer: targeted consolidation additions

**Reviewed:** October 29, 2026 (America/Los_Angeles). **Status:** researched design additions and proposed acceptance work. No production code, executable, GPU qualification, or performance grade is supplied by this review.

## Existing scope and merge destination

The existing specification already covers gameplay-light authority, optical material semantics, contribution ownership, source removal, missing neighbors, temporal history, lighting-provider comparisons, native ownership, resource retirement, and numerical/image acceptance. Preserve that work. Merge these narrower additions into the corresponding canonical sections; do not create a second full lighting master.

| Reviewed document | Content identity returned by GitHub | Existing responsibility | Suggested addition |
| --- | --- | --- | --- |
| `docs/research-expanded/2026-10-29/ENGINE-DESIGN-EXPANSION.md` | blob `84cac6abdc4618ee8607008f71c3edbdfc0820da` | Cross-system contract summary | Link the presentation-lifetime and vendor-discrepancy requirements below |
| `docs/research-expanded/2026-10-29/chapters/render-performance.md` | blob `ed28b3745d83e538c6b7aef812335b041be73c97` | Renderer scheduling, resource ownership, provider gates | Separate presentation retirement; explicit validation coverage; provider envelope |
| `docs/research-expanded/2026-10-29/chapters/lighting-integration.md` | blob `94017b16e9e1c1b76b6b30b229ea2255addfa80a` | Bridge lighting to actual renderer inputs | Link guide-conformance fixtures |
| `docs/research-expanded/2026-10-29/replicas/project/LIGHTING-ENGINE.md` | blob `3ab7ebf4f191fea75a91b31ecff8128d7bc2b728` | Detailed preserved lighting master and review card | Add fixture IDs to the active acceptance index; retain preserved source unchanged |

The parent inventory pins current main at `8f28f833caa259a0e59adac494a3aee8d750bc63`. The frozen expansion describes Dev77, not every subsequent commit. These observations concern document coverage; they do not establish a current runtime defect. Source-present, compiled, exercised, and accepted remain separate evidence states.

## R-L01 — Treat presentation as a separate owner

**Primary-source evidence:** Khronos distinguishes submit completion from presentation completion. A submit fence alone does not establish safe reuse of a semaphore awaited by `vkQueuePresentKHR`. Its portable example associates these semaphores with acquired swapchain image indices. Presentation fences supplied by a supported maintenance extension offer another route. [LR-01]

**Proposed Deep Primal addition:** extend the retirement ledger with `present-pending`, swapchain generation, image index, and the actual retirement evidence. Keep per-frame rendering resources and per-image presentation resources separately keyed. Present-mode changes, resize, minimized windows, and repeated recreation must preserve old-generation ownership until the selected backend policy permits release. Document the host's actual ownership boundary before implementing this in an embedded Minecraft renderer.

**Acceptance LR-A01:** exercise differing frame and image counts, out-of-order acquired indices, resize/minimize/restore, and shutdown. Retain validation output and lifecycle traces. A test double may check indexing and ownership; native presentation evidence remains required.

## R-L02 — Record what validation cannot prove

**Primary-source evidence:** LunarG's current SyncVal documentation lists gaps involving memory aliasing, host accesses, indirect/indexed data, sparse resources, and precise shader descriptor access. Therefore a clean synchronization-validation run does not establish correctness of all resource hazards. [LR-02]

**Proposed Deep Primal addition:** give every validation result a coverage declaration: layer/version, enabled settings, exercised queues/resources, known exclusions, and remaining manual or instrumented checks. For transient-memory reuse, retain the framegraph lifetime/alias map and independently verify that physical ranges cannot overlap while either logical resource remains accessible. For mapped upload rings, keep host ownership and flush/visibility rules explicit. For descriptor reuse, record the last recording and submitted references before recycling.

**Acceptance LR-A02:** use small intentional hazard witnesses to verify the selected checker is active, then test descriptor retirement, mapped staging overwrite, and transient alias transitions through the real backend. Keep correctness runs separate from performance runs; instrumentation overhead is part of the evidence manifest.

## R-L03 — Track vendor documentation contradictions as findings

**Primary-source evidence:** AMD's live FSR Upscaling manual identifies version 4.1.2, dated October 28, 2026. It documents Vulkan/SPIR-V input integration and publishes selected integrated-GPU measurements, including Radeon 8060S. Its own Limitations section nevertheless states discrete-series requirements that do not describe those listed APUs. This is an internal documentation inconsistency, not proof that a particular Deep Primal installation supports or rejects FSR4. [LR-03]

**Proposed Deep Primal addition:** create a capability-evidence row per deployed effect version. Retain SDK/runtime version, package provenance, API, device/driver, query response, selected effect/model where observable, initialization result, first successful evaluation, and image acceptance. Record the vendor discrepancy and revisit it when official release notes/runtime evidence clarify it. Menu availability follows the deployed adapter's verified capability path; marketing generation numbers do not bypass that path.

**Acceptance LR-A03:** unavailable, query-failed, initialization-failed, evaluation-failed, and accepted cases display truthful effective states while preserving requested preference. Do not substitute vendor microsecond tables for owner-device benchmarks.

## R-L04 — Make guide semantics executable

**Primary-source evidence:** AMD's FSR3 integration describes motion coverage for animated geometry and textures, distinct reactive and transparency/composition masks, and separate exposure/pre-exposure roles. A generic texture labeled motion or reactive does not establish that a renderer supplies these meanings. [LR-04]

**Proposed Deep Primal addition:** a provider descriptor names the admitted render/output extent envelope, color/depth conventions, motion scale/direction, jitter treatment, exposure semantics, mask semantics, and history reset rules. Reject dispatch outside the admitted configuration; recreate or fall back through an explicit transaction. Provider-specific adapters translate this descriptor and retain the actual creation/dispatch settings.

**Acceptance LR-A04:** deterministic guide fixtures cover one-pixel camera translation, moving opaque objects, animated foliage/UVs, flowing water, exposure steps, camera cuts, extent changes, and origin rebasing. Compare guide outputs against known transforms before judging reconstructed images. Record opaque-world and hand/UI boundaries separately. Keep mask concepts distinct; an arbitrary material alpha channel is not automatically either required mask.

## Merge and implementation order

1. Add R-L01–R-L04 to the active requirements/findings register with owner, destination section, evidence state, and acceptance IDs.
2. Resolve presentation ownership and checker coverage first; resource correctness is a prerequisite for richer lighting or reconstruction.
3. Implement guide descriptors and deterministic conformance fixtures before enabling a temporal provider.
4. Qualify one provider/backend/device combination, preserving the documented native/spatial fallback.
5. Close a requirement only with the actual accepted artifact and runtime evidence. Document completeness alone never closes engine implementation work.

## Primary sources inspected

All four sources were opened on October 29, 2026. Live pages may change; pin the deployed SDK and retain relevant versioned references when implementation starts. These sources support mechanisms and vendor statements, not Deep Primal implementation status.

| ID | Primary source | Relevant scope |
| --- | --- | --- |
| LR-01 | [Khronos Vulkan Guide — Swapchain Semaphore Reuse](https://docs.vulkan.org/guide/latest/swapchain_semaphore_reuse.html) | Presentation semaphore retirement versus submit fences |
| LR-02 | [LunarG — Synchronization Validation](https://vulkan.lunarg.com/doc/view/latest/windows/synchronization_usage.html) | Current checker coverage and exclusions |
| LR-03 | [AMD GPUOpen — FSR Upscaling 4.1.2](https://gpuopen.com/manuals/fsr_sdk/techniques/super-resolution-ml/) | Current version/API statements and inconsistent device guidance |
| LR-04 | [AMD GPUOpen — FSR Super Resolution 3.1.6](https://gpuopen.com/manuals/fsr_sdk/techniques/super-resolution-upscaler/) | Guide signals, masks, exposure and input requirements |
