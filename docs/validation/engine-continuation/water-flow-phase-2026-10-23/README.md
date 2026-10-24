# World-anchored flowing-water phase — 2026-10-23

Parent dad0e5581f183b36dd5ef3490cc3f11e20bf5b1b; guarded lease parent 56a156fc1e7b767e75882c1557693778480b92a9. Unreleased continuation on work/engine-after-alpha24. Main/installers and frozen geography remain intact; engine acceptance is incomplete.

## Defects and change

Flowing top UVs restarted in each block. Six controlled neighboring-tile versus continuous-reference comparisons on unchanged production fail with 6,144 differing pixels and matching geometry. Baseline production identities, exact fixture source, raw pixels, complete sealed console and normal saved shutdown are preserved. The reference specifies periodic world phase; the gradient is a diagnostic, not representative water art.

The first spatial-phase prototype passes six base pairs, but self-audit caught a second defect: implicit mip derivatives after fract select coarse mips at diagonal wrap boundaries. Diagnostic hardware-repeat comparisons show 132 pixels beyond a one-unit channel allowance, maximum error255 (1,713 total differences including ordinary one-unit rounding). That candidate is superseded and preserved, not promoted as complete.

Four production files now add bounded world-cell phase helpers, apply them once per top quad, carry a top tag in unused light bits, wrap only at sampling, and derive mip selection from unwrapped UV gradients. Native block light is masked before lookup, skylight and side-height UV morph remain unchanged. CPU stride14 floats, GPU stride36 bytes and uniform allocation remain unchanged. A flat vec4 interstage value and additional arithmetic are real costs, not a performance improvement claim. Quantity, solver, opacity clocks, page bounds, player edits, LOD/backend code and geography remain unchanged.

## Fresh checks

Final clean serial uncached full build passes25 executed tasks and explicit core/geography/hydrology records. All6 archive CRCs,408 compiled class identities and5 nested/standalone module identities match; validation/private-agent/unaccepted-drain payloads are excluded. Core checks cover16 directions,100,000 direction/origin samples and400,000 edge-component comparisons, including negative coordinates, section edges and near30-million coordinates; maximum circular error is5.960464477539063e-8.

Two strict software Vulkan sessions (development and packaged origins) and a scoped OpenGL llvmpipe fallback pass the final source. New controls total18 exactly equal base pixel pairs and18 mip pairs with explicit one-channel-unit tolerance at the mip boundary;72 controlled renders, zero pixels beyond tolerance. Mip pairs are not described as pixel-exact. Prior side-height tests retain18 exact pairs and36 actual upload readbacks/25,920 bytes for repeated uploads/stale revisions/light; real-client FLOW capture and18 GPU direction/material samples before/after reload pass with world phase and top tags. Prior memory-capacity/partial-fade/LOD index/retry/edit/reload/frame/save/shutdown gates remain passing; final Vulkan tracked resources and validation errors are zero. No new final crashes or save errors.

27 actual lake frames in9 wave/LOD profiles were inspected and preserved as lossless RGB VP9 with byte-exact decode verification. First development frames have changing distant-left terrain while loading; they are not matched-state/full-scene coverage or flicker acceptance. Lake waveBytes=0 even with waves enabled, so actual ocean geometry remains untested. Video timing is illustrative, not measured FPS.

Rejected evidence includes a missing-reference-world launch, a compile/artifact method mismatch and a truncated Vulkan dependency archive. Their causes are not established by successful retries. Only isolated build state was discarded; final serial outputs pass independent CRC/byte checks. The native helper now rejects a missing reference world before Gradle/display startup, with a passing preflight check. Reused pinned toolchain and software-driver provenance are linked in the parent checkpoint; no pre-reset measurements are counted as fresh acceptance.

## Continuing work

This fixes spatial phase for equal directions, not complete seamless water. Next smooth temporal direction/material retargeting and different-direction shared edges; world anchoring changes phase on direction changes and needs dedicated continuity checks. Speed-driven advection, sustained actual streams, ocean waves and native/LOD boundary coverage stay open. Added shader/helper work needs matched frame-time/GPU profiling; software-driver checks do not establish owner GPU/FPS or market superiority. Solver substeps are not independent60Hz.

Retain the requirement register: renewable headwater/reservoir and constituent budgets, compatible-liquid composition/volume separation, pressure fanout/p95 and1,024 waiters, beyond8,192-node/key continuation/coarser LOD, physical unload/reopen/input, unexplained marker/reload/atlas/save failures and coherent geology remain unfinished. The staged drain experiment stays separate and unaccepted. Catalog/species stays deferred. No installer is released; this is a continuation checkpoint.
