# R56 — staged renderer index adapter and non-strandable cancellation cleanup

Source parent: `6deeeced1294e7d2e76ff95beef42c69bfe0749c` (R55).
This checkpoint stages one new independently authored `LodIndexWorker` and a modified
`VoxelLodRenderer`, alongside unchanged R54/R55 cursor/request sources. No public
runtime source, geography, water, player edit, configuration or installer changes.
Delivered V1–V6 remain frozen. The public renderer still has its prefix cutoff.

## Implementation and concrete reproduction

R55 requires same-worker cleanup before shutdown. A real one-thread/two-slot executor
reproduction opens the cursor, blocks geometry, fills both slots, cancels the request
and attempts to queue close. Submission is rejected; after all work drains the cursor
is still open. The fixture then closes it on its original thread: no intentional leak
is left behind. This reproduces the unfinished adapter's cleanup gap, not a new claim
that the current production prefix scanner leaks this particular cursor.

The new bounded worker registers one index request and allows one outstanding index
turn. Cancellation signals and attempts a nonblocking wake; if the two-slot queue is
full, existing work's `afterExecute` drains cancellation on the owning worker. Graceful
shutdown signals/wakes before closing admission. It does not discard queued turns or
require a render-thread filesystem close/wait. `shutdownNow` is explicitly refused.
An abrupt `execute` failure closes the current cursor before worker replacement;
ordinary submitted-job failures remain observable through Futures. Cleanup IOException
is recorded, but filesystem failure/close-failure matrices are not accepted.

The staged actual renderer submits at most one 256-entry metadata turn per service
tick, without worker self-resubmission, beside its existing geometry scheduling.
Camera changes or persistence disable cancel the prior owner; replacement waits for
cleanup. Publication uses request identity, epoch and live camera. Recovery order,
shared8192-key cap and existing disk/edit/revision guards remain. Reset uses graceful
shutdown and does not cancel/discard the scan Future. Existing render-owned GPU closes
and geometry Future cancellation remain. Directory and geometry latency can still
block shutdown completion; this is not a hard time bound or rendered-frame guarantee.

Self-audit caught an initial adapter bug: camera out-and-back could retain a cancelled
terminal owner indefinitely. Replacement now also detects that cancelled owner.
The corrected source-extracted harness reproduces the earlier bug with exit1, then
accepts the final candidate. Earlier source and rejected fixture records are retained.
The first harness completion predicate also incorrectly accepted cancelled terminal
requests before replacement. It is corrected to require a consumed result, uncancelled
owner and matching view; the earlier partial passes are superseded, not acceptance.

## Fresh scoped checks

`python evidence/extract_adapter.py` extracts exact serviceIndex/reset bodies from the
staged renderer, hashes them and verifies four production guard methods unchanged.
`python evidence/run_checks.py` uses retained system Java17's jdk.compiler module.

- New real-worker suite: **898 checks**, including100 saturated shutdown runs,
  33 scan turns/32 interleaved geometry jobs over8205 entries, late nearest-page
  discovery, idle wake, queued cancellation, rejected admission/retry, refusal of
  forced discard and original-thread cleanup before abrupt worker replacement.
- Exact extracted renderer methods: **20 checks** for camera/replacement/backtrack,
  disable/re-enable, recovery order/cap/edit-state preservation and graceful reset
  with saturated queue. Twelve JVM repeats pass. GPU/level/logger/configuration
  are minimal non-game doubles; this is not full renderer compilation.
- Unchanged R55 real-executor regression freshly passes **347 checks** on Java17.
- Immutable Git blobs verify both reused core files, original renderer and R55
  fixture; final source/fixture SHA256 and compiled identities are retained.

The intentionally failing earlier adapter, failed initial harness expectation and
corrected failure oracle are under intermediate/ and evidence/rejected-harness-record.
No full build/native game was attempted or inferred successful. Tests use metadata
filenames, not native terrain geometry; they do not establish physical scene coverage.

## Environment, limits and next step

The previous R55 source/tools/cache directory was pruned. Current local environment
has Java17, no javac/Gradle executables and no restored JDK25/full dependency checkout.
R55's31-task/package/14303checks are historical, not fresh R56 validation. The recovery
helper and immutable source manifest remain in Git; recovery is possible. This is a
recoverable environment gap, not reason to end engine work.

Apply these four staged production candidates only to isolated current main, with
R55's private invocation if needed. Restore pinned JDK25/21, Gradle/source/dependencies,
compile the full renderer and run all core/module/package/independent-source guards.
Then perform Vulkan-first actual camera travel/backtrack, edits, persistence toggle,
world/session reset, save/reopen and shutdown, with explicit pass records, screenshots,
crash/VUID/resource inspection. Preserve OpenGL fallback. Do not promote before these.

Open risks: arbitrary filesystem faults, externally cancelling returned index Futures,
forced thread death, rapid reset's accumulation of retiring workers and outstanding old
geometry writes/shared persistenceFailed state need native/lifecycle review. The patch
retains existing geometry cancellation; it does not prove old jobs stop before a new
world worker starts. Completed scans preserve existing disk backlog; a full backlog can
still prevent fresh nearest keys. Coarser hierarchy, beyond-cap rediscovery, live directory
mutation, total frame/peak memory and2ms acceptance remain open. Worker turns are not
independent60Hz or rendered frames. No Vulkan/GL/Windows/owner GPU/FPS, visual,
performance or installer acceptance follows from this checkpoint. Water publication,
pressure/budgets, rare-host/cave and wider engine acceptance remain incomplete.
