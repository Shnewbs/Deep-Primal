# Expanded replica — docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/README.md |
| Preserved source SHA-256 | `0e2f6900bb39d01e1a90fc060f026a7d911ca7ba79ee3074d5d492d4e73ec335` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Resetting Startup.complete after every successful arm so a prior completed Vulkan attempt cannot suppress a later marker cleanup. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that 32 rearm cycles inside an existing session do not recreate the GPU device or prove normal gameplay reaches a second initialization. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a startup-attempt state machine with per-attempt completion identity and separately tracked forced-retry versus ordinary reopen evidence. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat arm and firstFrame transitions, require marker creation then removal each cycle, and retain rejected packaged unforced launches until their independent cause is resolved. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Vulkan startup lifecycle audit — 2026-10-22

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `inspected-failures.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/inspected-failures.json) |
| `packaged-module-identity.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/packaged-module-identity.json) |
| `packaged-unforced-marker-failure.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/packaged-unforced-marker-failure.json) |
| `screenshot-manifest.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/screenshot-manifest.json) |
| `source-scope-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/source-scope-audit.json) |
| `startup-source-extracted.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/startup-source-extracted.json) |
| `strict-startup-native-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/strict-startup-native-audit.json) |
| `vulkan-build-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/vulkan-build-audit.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Vulkan startup lifecycle audit — 2026-10-22

Extends source checkpoint `fb3b885b` on `work/engine-after-alpha24`. One production change resets `Startup.complete` after a new successful `arm()`. A previous successful startup must not suppress completion of a subsequent attempt. Temporary method tracing is removed. Main and supplied installers remain unchanged.

## Reproduction and scope

The native baseline deliberately called the real startup owner's arm/firstFrame methods in an already-loaded Vulkan session. Completion stayed latched true and the marker survived; Minecraft asserted. The fixed native replay performs 32 cycles, requiring a marker plus complete=false after each arm, followed by marker absence after firstFrame. It does **not** recreate the GPU device or establish ordinary gameplay reachability of a second device initialization.

The separate ordinary-marker symptom is **not explained by this fix**. Fresh in-process observations confirm marker presence with complete=true. Temporary tracing shows the initial deletion returned true with immediate exists=false, then the marker is present before another traced owner arm. Cached-owner and preference paths agree. Do not attribute this to an extra engine arm, host-only observation, external writer, driver, or workspace synchronization without evidence. Child syscall tracing is denied by the container's ptrace policy.

## Fresh checks

- Uncached Linux Vulkan full build: 25 tasks executed; core, water, geography and module checks pass. Six CRC checks and 406 current compiled-class comparisons pass. All five standalone module JARs equal their nested outer payloads. Private-agent/validation classes and temporary trace strings are absent. Final development JAR SHA-256: `1045c236ff611bd62169b08e8c00b614aa412b54799c7db1283ab9d2865874dc`. No installer is released.
- The full exact Startup source is compiled with explicit readiness/preference/ownership doubles. The old lifecycle fails at the required re-arm assertion. All five candidate JVM scenarios pass: 32 cycles, readiness, deletion retry, fallback consumption, crash-marker guard, explicit retry, OpenGL selection and competing-renderer ownership controls. Fresh marker directories prevent an old marker from causing the wrong baseline failure.
- Two strict focused native re-arm sessions pass, one from development classes and one from the packaged Vulkan JAR: **32 cycles each**, actual GPU shader/buffer/storage/depth/texture/atlas/stencil/lightmap checks, reload, saved disconnect and rejected-completion shutdown. No new crashes, save errors, VUIDs or Vulkan validation errors; final buffers/images zero.
- One **unforced development reopen** passes with marker absent at join, reload, disconnect and after exit. No retry flag or explicit arm replay is used.
- The following **unforced packaged launch fails the marker-cleanup gate**: it starts with no marker and selects actual Vulkan, but the marker is present throughout the in-process observations and afterward. GPU checks/save/shutdown still pass. This is not a second clean-reopen pass. Ordinary marker persistence remains intermittent and unaccepted. Here "unforced" means no recovery-retry flag; Vulkan is still requested through the sample graphicsBackend property. Saved UI preference persistence is not certified.

See [strict native records](strict-startup-native-audit.json), [package audit](vulkan-build-audit.json), [module identity](packaged-module-identity.json), [model controls](startup-source-extracted.json), and [packaged marker failure](packaged-unforced-marker-failure.json).

[Source audit](source-scope-audit.json) compares all 507 original inputs with the source commit: 504 unchanged, one production change, two modified validation files and one new validation helper. Water runtime/core, prior frame/LOD fixes, player-edit and frozen V1/V2/V3 geography code match the parent. The retained-drain experiment remains separate. The added store is per startup attempt; no new frame-loop I/O or measured FPS claim is made.

## Self-audit and rejected attempts

- The original native re-arm assertion and ordinary-marker observations are preserved in [inspected failures](inspected-failures.json), with a crash excerpt/digest.
- An early candidate session timed out in the atlas check before re-arm. The controller stopped it at its action bound; no cleanup or native pass is credited. A later workload runs reload/re-arm before GPU checks and completes. That does not establish the timeout's cause or repair. The validation-only helper adds a bounded delayed stack probe; it is not mod payload.
- A packaged attempt's producer reported completion, but the later live console lacked completion lines and differed from the recorded digest. It is excluded. The retry samples console stability after launcher exit and seals one immutable compressed copy. The strict auditor verifies both compressed and raw hashes, full completion records, crashes and shutdown. Live-console divergence remains recorded; the original report hash is never changed to manufacture a pass.
- Trace-setup path errors/affected rebuilds and a reused model-marker run are excluded. Corrected sequential uncached builds and fresh model inputs are required. See the rejection JSON files; none is relabeled as successful native validation.

The evidence `console.log` in accepted case directories contains the exact canonical bytes checked by the auditor; for sealed cases it is extracted from `console-final.gz`, not copied from a later mutable live log. Full sealed/raw hashes and selected readable records are retained. No claim about the underlying cause of console mutation is made.

## Visuals, remaining work and reproduction

Two final upright local terrain/tree captures were inspected; they do not certify water coverage, distant LOD, mixed transparency or identical-state output. [Screenshot manifest](screenshot-manifest.json) preserves the two re-arm images and hashes of the other accepted capture. Software Mesa llvmpipe correctness is not owner AMD 8060S/FPS acceptance.

Next reproduce intermittent marker recreation with bounded in-process filesystem events and owner-call tracing, then repeat clean unforced packaged reopen. Reproduce the early atlas timeout with a phase-aware thread capture and matched warm state. Direct-stop save safety and physical teleport/edit/memory-pressure water/LOD coverage remain open. Regional finite/renewable water budgets, larger pressure-search residency, staged p95/1,024 waiters, coherent geology and LOD hierarchy remain unfinished; catalog/species stays deferred. Solver substeps do not establish independent 60 Hz.

Use the source/requirement register and current work state. To reproduce, restore this commit in an isolated `project/` beside the scripts, using the repository's pinned private toolchain helpers. Run an uncached Linux Vulkan build and `audit_vulkan_package.py`; run `check_startup_lifecycle.py` with the included original source. Native commands are `run_vulkan.py marker-rearm-stack-retry`, `marker-rearm-packaged-sealed-retry`, `marker-unforced-after`, and `marker-packaged-unforced-after`. Output directories get attempt suffixes when already present; pass the actual accepted directory names to `audit_startup_native.py`. The packaged-unforced failure is audited separately and must remain excluded. Prior-checkpoint/pre-reset measurements are historical, not fresh checks here.


<!-- END PRESERVED SOURCE -->
