# R57 — recover tools and fix staged scan Future cancellation

Parent `cbc20d86e626285dfa12bb2ed6ef747358fbec59` (R56).
One staged runtime candidate changes: `LodIndexWorker`. Cursor, request and actual
renderer adapter are copied unchanged from R56. No public production source,
installer, geography, water quantity, edits or graphics/default settings changes.

## Reproduction and focused fix

On R56's exact worker, cancelling a queued scan Future returns true but never signals
the request. The cancelled FutureTask skips its Callable, so the Callable's finally
never releases indexPending. Two real-executor baseline processes fail explicitly:
`baseline-future.log` at request cancellation, and `baseline-slot.log` at the stuck
submission slot after explicit request cleanup. Fixtures close workers/cursors on
exit. Baseline worker Git blob:9389a9757df77bb65dc93fe0a227e4d04635c1be.

The candidate owns its FutureTask. Successful Future cancellation signals the request
and wakes same-worker cleanup, using non-interrupting cancellation for both caller
boolean choices. Unsuccessful cancellation leaves completed results publishable.
Release runs for both normal Callable completion and cancelled queued tasks. Each turn
has a distinct atomic token: late release from an old task cannot clear a new task's
admission. A still-occupied slot reports bounded rejection for the renderer's existing
retry path. No self-resubmission or render-thread filesystem close/wait is added.
Existing one-worker/two-slot queue and 256-entry turn limits remain.

A first candidate released only after FutureTask.run returned. Full JDK25 checks caught
an actual adapter timing failure: the Future could be done before admission released.
That variant/source/results are retained under intermediate/failed-full-core-results
and LodIndexWorker-before-slot-fix. The final token release runs before normal result
completion and safely again after a cancelled run. No failed variant is promoted.
The first new fixture also submitted a barrier into the intentionally full queue;
its rejection is retained, then corrected to bounded retry. This is distinct from the
candidate's actual timing failure. Baseline failure logs use corrected barriers.

## Fresh accepted validation

Pinned restoration helper recreates all five archives with exact recorded sizes and
SHA256: JDK25, JDK21, Gradle9.2.1, NeoForm2.0.31 and ASM9.9.1. JDK21/version and
Gradle9.2.1 under JDK25 execute successfully. No private NeoForm compatibility agent,
full game checkout, dependency cache or native graphics/display environment is claimed
restored merely from these archive/version checks.

All49 public engine-core sources are read from immutable parent and Git-blob verified.
Together with the three staged core helpers, the complete **52-source candidate core**
compiles with pinned JDK25.0.4.1+1. Core output contains94 product classes, with hashes
in full-core-results.json. This is direct javac, not the full Gradle/module build or
independent comparison with shipped bytes. No product bundle/JAR was built or delivered.

Accepted JDK25 -Xverify:all executions:

- **803 new Future checks**:100 queued cancellation runs covering cancel(false/true),
  request signal, same-owner cursor close, released admission/replacement, clean shutdown
  and completed-result cancellation behavior.
- **898 R56 worker checks**, including100 saturated shutdown cases and abrupt execute
  failure/worker replacement.
- **20 exact extracted renderer service/reset checks** with non-game state doubles.
- **347 unchanged R55 request checks**.
- Twelve additional JVM repeats each of FutureChecks and AdapterChecks pass.

The final full-core source/fixture/class identities, raw logs, extraction hashes, failed
attempt and repeat results are retained. Initial Java17 smoke results/logs precede the
final token fix and are intermediate; final JDK25 full-core-results is authoritative.
`python evidence/extract_adapter.py` regenerates the exact-method fixture.
`python evidence/run_full_core.py` verifies restored49inputs, overlays three staged core
files, compiles and executes the four gates. Restore immutable files listed by
core-inputs.json into project/ and use the pinned sibling toolchain after pruning.
Tools/binaries/class outputs/source copies are local only; Git owns original sources.

## Remaining gates

This closes the scoped staged queued-Future cancellation/admission defect. Actual
running filesystem cancellation timing, arbitrary IO/close faults, forced worker death,
rapid world-reset retiring-worker accumulation and old geometry writes/shared failure
state still need native/lifecycle evidence. The renderer itself remains unchanged from
R56 and has only extracted-method checks, not full compilation or Minecraft execution.

Next reconstruct the remaining current-main engine/Themes build inputs/dependencies,
build the compatibility helper if needed, run full Gradle/core/module/source-bytecode/
package guards, and restore native display/driver. Then Vulkan-first travel/backtrack,
edit/toggle/world reset/save/reopen/shutdown and visible coverage with explicit passing
records, screenshots and crash/VUID/resource inspection before promotion. Preserve
modular GL fallback. Public renderer still has the prefix cutoff; full disk backlog,
coarser hierarchy/rediscovery/frame/memory/2ms, water publication/pressure/budgets and
native terrain/ownerGPU acceptance remain open. V1–V6 stay frozen. Metadata/JVM turns
are not native rendered frames, independent60Hz, Windows or ownerGPU/FPS guarantees.
Engine acceptance is incomplete; no new installer.
