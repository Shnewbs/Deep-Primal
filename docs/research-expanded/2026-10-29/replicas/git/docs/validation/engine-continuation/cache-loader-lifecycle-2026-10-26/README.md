# Expanded replica — docs/validation/engine-continuation/cache-loader-lifecycle-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/cache-loader-lifecycle-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/cache-loader-lifecycle-2026-10-26/README.md |
| Preserved source SHA-256 | `fbfdd6296bb7d3edd4a9c3eb9954d6b176d981851c91586c51913082286cc705` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Moving ChunkCache loaders outside the cache monitor and using a generation counter to reject post-clear admission of work started earlier. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that clear does not cancel arbitrary caller code, and a caller may still receive its computed result; native server checks do not establish client performance. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add explicit cache-clear, close and ownership semantics covering winner selection, one-time byte reservation and caller-owned executor survival. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Block a loader with a latch, clear or close concurrently, complete same-key contenders, and verify prompt shutdown, one admitted winner and no stale-generation insertion. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Cache loader lifecycle repair — alpha.29-dev.24

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Cache loader lifecycle repair — alpha.29-dev.24

The dev.23 `ChunkCache.getOrCompute` invoked terrain loaders while holding the
cache monitor. A blocked caller-owned loader prevented `clear()` and therefore
`EngineCore.close()` from finishing. Both failures were reproduced against
`5aa353a6f78628d9c04eb3f6b1eeec66fef0cc26` with latch-controlled fixtures.

The loader now runs outside the monitor. A generation counter prevents a result
started before `clear()` from being admitted afterward. Concurrent same-key
completions return the already admitted winner and reserve its weight once.
Negative weights fail before invoking a callback. A caller can still receive its
computed result after a clear; clearing does not cancel arbitrary caller code.
The existing pipeline owns normal query coalescing and its 64-query bound.
CacheLifecycleChecks and CoreCloseLifecycleChecks run in the normal core suite
and remain excluded from the delivered product. No terrain/water formulas,
saved profiles, owner art or graphics modules changed.

## Audit consumed and scope

Consumed `docs/engine-hourly-audits/2026-10-26T0047-905e550/REVIEW.md`, pinned to
`905e550174e32f9d25d4e01e4e398bb6712288b2`. Its compilation/boundary failures
were already repaired in dev.23; this cycle addresses the remaining bounded
lifecycle requirement with a separately reproduced defect. The audit's 4.2/10
was a provisional partial assessment of that older source. The last complete
external baseline remains 50/100 (5.0/10), unchanged. No builder regrade.

## Fresh checks

- Both baseline cache-clear and EngineCore-close fixtures fail as expected.
  Candidate cache checks pass 13 assertions, plus EngineCore close/caller-worker
  ownership checks. Twenty repeated cache interleavings pass. Both fixtures also
  pass against the core extracted from the final mod JAR.
- Full Java 25.0.4.1 / Gradle 9.2.1 Windows-payload build passes: 35 tasks in the
  completed run, including core, cache, module/core-boundary and ground-resource
  gates. The first run rebuilt all sources but failed because this isolated
  checkout lacked the repository's sand validation manifest. Restoring that
  manifest fixed setup; the complete build was rerun without excluding checks.
- Seven archive CRCs pass; all 587 compiled product classes match packaged bytes,
  including 142 independently compiled core classes. All 205 private fixture
  classes are excluded. Windows native payloads, OpenGL fallback, 2,508 Themes
  resources, 43 palette resources, 11 approved ground textures and 148 generated
  soil resources pass their checks. All 836 unchanged restored inputs match Git.
- Four packaged Linux dedicated-server sessions pass: default channel-2 fresh
  and reopen, opt-in channel-5 fresh and reopen. Explicit native pass records,
  save-flush acknowledgments, ordered shutdown and unchanged replay hashes are
  required; Gradle exit zero alone is not used as acceptance. The channel-5
  creation toggle was disabled before reopening and the saved revision stayed 5.
  Default terrain/deposit hashes match the prior verified source. Loaded class
  records match current compiler outputs. JAR bytes are unchanged after runs.

This build uses the existing builder-owned dependency cache with a new isolated
checkout/output tree. Unchanged source inputs were Git-hash-verified before reuse.
Only reproducible outputs of the builder's prior completed run were removed to
recover storage. Native sessions share a cache private to this run. Server-only
asset metadata is used; no client asset/render acceptance follows from it.

## Artifact and limits

`DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-vulkan-windows.jar`

Size: 125288625 bytes.
SHA256: `8c7079b4d174ffbe4d2a42c6fa259c0732a3abbb870b71f40ab2c3d344d0275e`.

Development testing only. Windows/AMD8060S execution, Vulkan/OpenGL visuals,
FPS, long-soak and complete engine acceptance are not established by these
server checks. There is no GitHub release/asset-write tool available and no `gh`
executable on this host; a GitHub prerelease is therefore not claimed. The
delivery receipt records the saved testing artifact separately.

Next: retain the native channel-5 save/profile result, complete its visual and
hydrology acceptance, and resume the staged V8 native saved-graph/topology gates.
R57/R60/R61/R62 remain unpromoted. Frozen V1–V7, owner edits and finite quantities
remain protected. No continuous execution or acceptance completion is claimed.


<!-- END PRESERVED SOURCE -->
