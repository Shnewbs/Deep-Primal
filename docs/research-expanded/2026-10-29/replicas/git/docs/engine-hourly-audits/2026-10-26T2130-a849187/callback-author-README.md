# Expanded replica — docs/engine-hourly-audits/2026-10-26T2130-a849187/callback-author-README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2130-a849187/callback-author-README.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T2130-a849187/callback-author-README.md |
| Preserved source SHA-256 | `994c63818c970b37cb83f92b85deac2036b22e8a0ea010ba1be04689a518ef4b` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Staged hydrology callback-safe source passes”. Its original opening states: “Base main: 23eafba3ea7bf0f6544870ea34dd5fbe3729b7a0. Consumed external audit: docs/engine-hourly-audits/2026-10-26T2032-0a936f9/REVIEW.md, pinned 0a936f9dbacf9bc363ae01c317f64060cc7587b3. All ten scores remain carried at 50/100 (5.0/10); this builder assigns no grade. Production remains dev.31, code identity a1583a508b0d67130d387bc018defd5758acd496. No candidate promotion or newly qualified user-facing JAR. The next production promotion must bump beyond dev.31 and satisfy the remaining gates below.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `StreamSourceIndex.Pass`.

## Research expansion — gaps and details to deepen

- Carry the water finding through the real native callback boundary while preserving quantity, constituent identity, and exactly-once ownership.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Bind write scopes to world, level, immutable cell coordinates, expected state, and revision; unrelated callbacks must retain their own invalidation and accounting.
- Recheck final state after synchronous callbacks before committing water/salt/sediment ledgers; define bounded compensation and explicit failure when restoration is impossible.

## Research expansion — acceptance checks

- Exercise refused-but-applied and accepted-but-replaced writes, same-volume chemistry changes, unrelated-cell mutation, cross-level callbacks, and unload during commit.
- Compare joint loaded/unloaded/reservoir quantity and composition before and after edit, save, restart, and reopen using the actual host runtime.

## Research expansion — source anchors

- Staged hydrology callback-safe source passes
- Failures and limitations

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Staged hydrology callback-safe source passes

Base main: `23eafba3ea7bf0f6544870ea34dd5fbe3729b7a0`. Consumed external audit:
`docs/engine-hourly-audits/2026-10-26T2032-0a936f9/REVIEW.md`, pinned
`0a936f9dbacf9bc363ae01c317f64060cc7587b3`. All ten scores remain carried at
50/100 (5.0/10); this builder assigns no grade. Production remains dev.31,
code identity `a1583a508b0d67130d387bc018defd5758acd496`. No candidate promotion
or newly qualified user-facing JAR. The next production promotion must bump
beyond dev.31 and satisfy the remaining gates below.

## Implementation

The previous staged TreeSet iterator throws ConcurrentModificationException
when a callback changes the source index. A separate one-source witness also
continues two obsolete-cursor calls after removal during the chunk lookup.
Both baseline failures are retained. These are exact source-loop extractions
with deterministic native/clock doubles, not real callback injection in Minecraft.

`StreamSourceIndex.Pass` captures an identity revision. An actual index mutation
invalidates the pass; hasNext then ends it without touching the stale iterator.
The actual source loop checks validity after native chunk lookup and between
source columns. A currently executing transaction completes; remaining sources
are reconsidered next tick in current headwater/registration order. No full
snapshot copy, mutation backlog, retry loop or overflow-prone counter is added.
Idempotent put, absent remove and retired-generation remove do not invalidate.
This remains server-thread-only and does not claim concurrent-reader safety or
fair progress under perpetual mutations.

A source whose chunk is no longer FULL now uses the existing shared invalidation
path immediately in the source pass. Callback replacement is checked before that
invalidation, so an obsolete lookup cannot delete a replacement. Existing dry
registration priority, old-cursor identity protection and exact physical methods
are preserved. `replenishStream`, `streamAllowance`, `visit`, `precipitation` and
`write` match current production method bodies byte for byte as text.

## Validation

- 2,004 exact source-loop cases: removal/replacement/reordering/drying/insertion,
  lookup and delivery callback points, no-op controls, missing FULL, next-pass
  recovery and no forced chunk load. Both old failures remain expected controls.
- Complete staged runtime compiles against Minecraft/NeoForge APIs. Existing
  5,512 lifecycle checks, 20,008 index checks, 100,003 priority comparisons over
  100,000 transitions, 1,000 actual-runtime priority cases, 144 regular-loop
  cases and 1,445 actual-runtime allowance cases pass.
- Fresh independent complete core suite: 75 core + 63 fixture sources; all
  138 input hashes rechecked before/after; PASS in 271.121 seconds. Functional
  elapsed time is not a benchmark.
- New Linux dedicated-server diagnostic: six scenarios run the **actual
  HydrologyRuntime.tick** against loaded native chunks and a controlled private
  work record. Shared one-unit regional quota is delivered to the correct
  source after ordinary registration, same-window dry/return, expired-window
  return, unload/re-registration, height reorder and a missing-FULL source.
  Each scenario checks exact groundwater + physical-cell water/salt/sediment.
  A real restart checks the final saved three parcels exactly. Both sessions
  require loaded candidate class hashes, save-all flush acknowledgement,
  failure-precedence log inspection and clean shutdown.
- Seven diagnostic archives pass CRC/duplicate-path checks after native consumers.
  Private fixture classes are separate from the diagnostic root. All six original
  modules, including complete Themes/resources and Windows Vulkan payloads, are
  unchanged hash-verified immutable inputs. The diagnostic root is 684,114 bytes;
  **it omits nested bundles and loads these six modules separately**. It is not a
  distributable Windows bundle or a full-build qualification.
- Producer guards reject changed fixture source, compiled classes and archive
  bytes before any world write: 12 positive/negative checks in normal/-O/-OO.

## Failures and limitations

The initial full diagnostic archive producer hit ENOSPC after 106,123,264 bytes.
Its corrupt archive was verified and removed; source and failure details remain.
A small split-module diagnostic recovered native testing without changing other
checkouts/caches/outputs. Initial native fixture compilation exposed package-private
composition access, corrected through fixture reflection. Early direct-launch
attempts had build-tool Gson contamination and missing NeoForge runtime selection;
launch dependencies now come from the pinned Minecraft version manifest plus
NeoForge userdev runtime libraries and universal JAR. An RCON password mismatch
prevented qualification of another attempt. These failed logs remain excluded.

When extending the fixture to actual tick calls, an incorrect event constructor
failed compilation. A shell sequence still launched older fixture classes; that
session is explicitly rejected, not counted. Corrected constructor and successful
producer identity are now required; final accepted fresh/reopen logs have
`actualTick=true`, respectively `cases=6` and `cases=1`.

No full Java25/Gradle/module/bundled release producer ran for this candidate.
Overlay capacity was zero; memory-backed capacity fluctuated and the first
archive producer failed. No release uploaded or Library artifact saved. No
Windows/AMD8060S execution, real OpenGL/Vulkan scene, latency measurement,
multiplayer/soak, complete weather matrix, or real native callback stress.
The fixture controls index membership and disables unrelated environmental visits;
it is not an ordinary gameplay travel/unload test or persistent world-wide ledger.

## Resume

Restore sufficient isolated full-build capacity; integrate the two staged runtime
files with a unique version and run full build/module/package gates. Reuse the
native quota/restart fixture, then add partial-survey, actual native callback,
weather and travel/load/unload cases against the final bundled bytes. Retain all
broader water/engine acceptance gates. No geography, saved profile, owner art or
physical transfer-method changes are included here. Root lease is released at
checkpoint; the existing builder remains enabled.



<!-- END PRESERVED SOURCE -->
