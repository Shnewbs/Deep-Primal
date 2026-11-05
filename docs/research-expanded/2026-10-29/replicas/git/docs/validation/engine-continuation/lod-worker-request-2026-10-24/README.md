# Expanded replica — docs/validation/engine-continuation/lod-worker-request-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/lod-worker-request-2026-10-24/README.md) |
| Source identity | git: docs/validation/engine-continuation/lod-worker-request-2026-10-24/README.md |
| Preserved source SHA-256 | `054b7578a42d4ea1cd0078b3877da726cd7ff6783f9967eec5f93f492b353bc9` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R55 wrapping the staged LOD cursor in a worker-owned request with cancellation and completed-result publication guards while restoring pinned build inputs. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that restored builds do not imply restored graphics dependencies, and same-worker close requirements were still an integration risk. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a request lifecycle distinguishing scan completion, publish eligibility, caller cancellation and resource closure across world reset. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Race cancellation and completion on real executors, preserve the final canPublish guards, and require native travel, edit, save and shutdown checks before promoting the adapter. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R55 — restored build environment and staged LOD worker request

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R55 — restored build environment and staged LOD worker request

Parent `669380658114459001bebd0dcf588501059c7dfa`.
R54's cursor remains staged. Since R54, one new independently authored component,
`LodIndexRequest`, wraps that cursor with worker ownership, cancellation and completed
result publication guards. One private executor fixture/invocation is added. Four
files are staged: two core components (one reused), the new private check and its
CoreChecks invocation. **No public production file, renderer hook, default, saved
V1–V6 geography, water quantity, player edit or owner installer changes.**

## Recovery

The Git-owned recovery helper re-downloads and verifies five pinned size/SHA256
archives: JDK25, JDK21, Gradle9.2.1, NeoForm2.0.31 and ASM9.9.1. Its private build
compatibility agent is rebuilt from verified repository source and passes its checks.
The delivered alpha27 Windows JAR is recovered and verified against its recorded
124,952,383bytes/SHA256; all seven archive CRCs pass. It supplies158resource inputs
whose bytes match Git blob IDs. The remaining source inputs are recovered from the
immutable Git parent. All661project inputs are verified before staged changes.
Library transfer helpers, credentials, downloaded tools, artifact binaries and
compiled classes are not committed or shipped.

The restored workspace is `lod-lifecycle-1232/project`, with tools/cache alongside.
The JDK/Gradle/source restoration blocker from R54 is resolved. Full Minecraft,
private software graphics/OS dependencies and renderer-native acceptance are not
claimed restored or executed merely because the build passes.

## New request contract

`turn()` runs one1–256entry cursor call and never schedules another job itself.
The owner can interleave geometry work on the existing single-thread executor.
Partial steps expose no keys. Results carry request identity, world epoch and camera
chunk; `currentRequest.canPublish(step, currentEpoch, cx, cz)` rejects old owners,
late cancellation and wrong session/camera, including a same-view replacement
request. Existing renderer edit/latest-revision guards are still required.

Render-side `cancel()` only signals an atomic flag; it does no filesystem work or
waiting. Cursor open/advance/cleanup stay on the original worker. A cancelled queued
turn opens no directory. Mid-scan cancellation closes on the next worker turn.
`closeOnWorker()` is idempotent and must be queued ahead of graceful shutdown.
Wrong-thread cleanup is rejected. Terminal requests cannot be advanced again.
Cleanup/turn exceptions propagate; their full failure matrix is not yet validated.

This is a worker-request building block, **not an automatic renderer scheduler**.
Its caller must retain requests across executor rejection, retry/drain cleanup and
not Future.cancel/discard a needed close job. A saturated cleanup queue, forced
worker death/shutdownNow, I/O faults and actual render lifecycle still need tests.
No claim of complete renderer integration or leak safety under arbitrary failures.

## Fresh accepted validation

- Final direct, full-build and embedded-product-core runs pass **347checks** against
  an actual one-thread/two-slot ThreadPoolExecutor and8,205directory entries. Each
  completed scan needs33turns with32interleaved geometry jobs. Independent BigInteger
  ordering, per-turn bounds, mid/queued cancellation, request/session/camera
  attribution, same-view supersession, rejected submission/retry and cleanup before
  graceful shutdown pass. These request-test entries are filename-only metadata,
  not fabricated native geometry or rendered scenes.
- The R54 regression separately reruns under restored JDK25: **14,303checks pass**,
  including8,205actual readable snapshots, nearest-key oracle and cancellation.
- A fresh terminal-backed forced **31task full build passes**, including the new
  checks and existing V6/legacy deposits, terrain, watershed, water budgets, cache
  and module gates. All661parent inputs remain unchanged except the private
  invocation; new candidate/private files are explicitly enumerated by source-audit.
- Independent direct JDK25 compilation matches all**51sources/92core classes** to
  Gradle output byte-for-byte. Seven archive CRCs,**507compiled product classes**
  and exclusion of46private class paths pass. The final embedded core then repeats
  all347checks with independently derived actual-loaded LodStore/LodDiskIndex/
  LodIndexRequest class hashes. The archive stays unchanged across that execution.

Final private Linux candidate package:
`d3a65fd36e3784cb20b2492d5760beb00c92289d2925cd7335726917615922f6`.
No installer is delivered. This packaged JVM/executor test is not Minecraft,
Vulkan-driver, OpenGL, Windows, owner-GPU/FPS or independent60Hz validation.
No new graphics, screenshots or performance measurements were made.

## Rejected capture and intermediate scope

The first cold build returns0. Its daemon log reports all31tasks and semantic gates
passing, but the normal stdout file ends after startup. The reporter rejects that
capture; no missing PASS is inferred from exit0. Both raw streams/result are retained.
The daemon log also contains a registry teardown integrity warning. Their cause is
unproven; no speculative product workaround is shipped. A separate fresh PTY-backed
build provides complete explicit PASS and task records. Final source/package guards
are repeated afterward; both audited candidate package hashes are identical.

An early request version passed340checks before the explicit canPublish API and
seven extra publication checks were added. Its source/result are labelled
intermediate/pre-publication-guard. Final347records are authoritative. No behavioral
or game check failed. Internal recovery serialization retries are not build passes.

## Reproduction and continuation

With JDK25 at JAVA_HOME, `python evidence/run_request.py` independently compiles the
staged core components/private fixture and executes the347checks. With a restored
candidate checkout present it also matches all five generated production classes.
For integration reproduce immutable parent sources, verify full-source-manifest,
then apply only the four staged files. `build.py` records ordinary forced build;
`build_tty.py` gives the accepted terminal capture (recorded path prefix is relocated
to this directory). The latter uses the retained initial command as a template.
Run verify_core.py, audit_package.py and verify_package.py afterward. Toolchain
restoration/helper checks and exact source/payload hashes are retained as evidence.

Next wire this request/cursor into **isolated current-main VoxelLodRenderer**. Service
one bounded scan turn fairly with geometry, validate current request/epoch/camera
and existing edits on merge, retain the shared8,192-key cap/recovery guards, and
prove close/rejection/supersession/reset/shutdown behavior in the native renderer.
The old production reset uses shutdownNow; it has not been changed here and cannot
be assumed to drain the required close job. The production prefix cutoff remains.
Actual Vulkan-first travel/edit/save/reopen/shutdown and visible coverage tests are
mandatory before promotion. Directory latency, final8,192key sort/copy, result/heap
memory and full scene/frame timing remain unaccepted; turns are not rendered frames.
Spatial hierarchy/coarser fallback, water/frame publication, pressure/budgets,
rare-host/cave coverage and owner-GPU acceptance remain open. Own jobs closed and
lease released; full engine acceptance incomplete.


<!-- END PRESERVED SOURCE -->
