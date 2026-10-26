# Terminal query shutdown — alpha.29-dev.25

Dev.24 fixed loaders holding the cache monitor. Continued testing then reproduced
a second defect: `WorldGenPipeline.generateChunk` dispatched an executor while
holding the pipeline monitor. A legal inline executor blocked world shutdown
behind terrain work. Merely moving dispatch outside the monitor was insufficient:
an already-started task could enter the density cache after shutdown cleared it.
The rejected candidate produced `pending=0 density=1 mesh=0` after close.

The final repair moves dispatch outside the pipeline monitor and gives world-owned
query caches terminal closure. Closing seals admission, clears retained entries,
and invalidates in-flight admissions. Later puts/loads fail before invoking their
loader; ordinary `clear()` remains reusable on an open cache. EngineCore closes
both caches and retains ownership of caller-provided executors. Arbitrary caller
code is not forcibly cancelled. Pending pipeline results remain cancelled and
late work cannot repopulate either query cache. The initial dispatch-only candidate
was rejected and is retained only as diagnostic text, never production source.

## Validation

- Baseline inline-executor close times out. The dispatch-only candidate's late
  density admission is reproduced and retained. Corrected candidate passes 24
  focused runs: integration, cache, core-close, inline-close and twenty repeated
  inline-close interleavings. Cache checks now contain 14 explicit assertions.
- Complete Java 25 / Gradle 9.2.1 build passes all 35 selected tasks, including
  core/cache/resource/module boundaries. The first packaging attempt failed while
  adding Themes to the ZIP; that error's exact cause is not established. A later
  attempt explicitly failed with `/tmp` ENOSPC. Moving Java temporary files to
  the private run directory allowed the complete unchanged checks to finish.
- Seven archive CRCs pass; 587 product classes and 142 independently compiled
  core classes match packaged bytes. All 206 private fixture classes are excluded.
  Windows native payloads, OpenGL fallback, 2,508 Themes resources, 43 palettes,
  11 approved ground textures and 148 generated soil resources are verified.
  All 834 unchanged source/resource inputs match the original Git snapshot.
  All three lifecycle fixtures pass against the core extracted from the final JAR.
- Four final packaged Linux server sessions pass: fresh/reopen for default
  channel 2 and opt-in channel 5. Save-flush acknowledgments, explicit native pass
  records, ordered shutdown and equal replay hashes are required. The revision-5
  creation toggle is disabled before reopen; the saved profile stays revision 5.
  Default hashes match the older verified terrain/deposit controls. Runtime class
  records match current compiler outputs; JAR bytes remain unchanged afterward.

The first dev.25 native attempt exited 137 before passing and is rejected.
The host was near its 8 GiB limit; an OOM kill is consistent with the observed
exit/cgroup records, but is not attributed solely from the exit code. Reclaiming
only this builder's completed prior outputs and using a private test-only 768 MiB
game heap / 512 MiB Gradle heap allowed all four final sessions to pass. No product
heap default, assertion or fixture coverage was removed. Failed diagnostics are
retained. Source snapshots, other actors' outputs and dependency caches are intact.

## Source, coordination and audit

The engine baseline is dev.23 `5aa353a6f78628d9c04eb3f6b1eeec66fef0cc26`;
dev.24 checkpoint is `4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc`. Dev.25 supersedes
that testing artifact after the follow-up race was found. Production changes are
limited to cache/pipeline/core lifecycle, private regressions and the version.
Frozen V1–V7, water/chemistry quantities, owner edits, art and graphics modules
are unchanged. R57/R60/R61/R62 stay staged.

Initially consumed external audit:
`docs/engine-hourly-audits/2026-10-26T0047-905e550/REVIEW.md`, pinned to
`905e550174e32f9d25d4e01e4e398bb6712288b2`. Its compile failures were already
repaired in dev.23; this cycle addresses bounded lifecycle findings. Its partial
4.2/10 assessment is historical. Last complete accepted external baseline remains
50/100 (5.0/10), unchanged; no builder regrade.

Personal-context and recorded-run checks were refreshed during work. Foreground
waterfall-sheet work is separate. Main added only the water-only lease at
`9434965b90bcbaf2c307044376ff01391832dc33`; it is preserved. That claim explicitly
excludes cache/pipeline/root-state edits and uses another checkout/cache. Recorded
auditor `in_progress` is not proof of live execution or a completed new grade.

## Delivery and remaining gates

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.25-vulkan-windows.jar`.
Size: 125288863 bytes.
SHA256: `a6555b12db16916c445659302f21b32d0e455716bb9d72b852d2b552b3c89a4d`.
The delivery receipt binds these bytes to the guarded pushed source commit.

Development testing only. No Windows/AMD8060S execution, Vulkan/OpenGL visual,
FPS, long-soak or complete engine acceptance is claimed. There is no connected
GitHub release/asset-write capability or `gh` executable; no GitHub prerelease
upload is claimed. The saved testing JAR is recorded separately.

Next: V8 native saved-graph/topology parity and remaining channel-5 visual/hydrology
acceptance, while preserving the foreground water-only claim. Completion still
requires an independent full average strictly greater than 8.5/10.

A newly completed audit was consumed before final publication: `docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md`, pinned to dev.24 `4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc`. Its scoped reassessment restores 50/100 (5.0/10), not a fresh full regrade. Dev.25 addresses the additional inline-executor shutdown and late-cache-admission defects; the next architecture work remains registered V8 saved-profile/topology/routed-water validation. Its audit-only commit eeb907b0 is preserved along with the foreground water reservation.
