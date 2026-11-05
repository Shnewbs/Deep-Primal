# R54 — staged bounded nearest-key LOD scan

Parent `c8f37b813da28fe769fc479f50eaaaa120bfa310`.
The current renderer still reads only the first8,192directory matches, then sorts
that prefix by distance. R54 reproduces a readable saved page at the camera being
excluded. One independently authored `LodDiskIndex` component is staged; **it is
not wired into VoxelLodRenderer or promoted into public production source**.
No installer, preset, saved geography, quantity, player edit or graphics API changes.

## Real-file reproduction and candidate

The fixture creates8,205valid v1 LOD snapshots using current production LodStore /
ColumnarChunk (including the R53 fix). BaselineIndex extracts the actual scan body
from the Git-verified renderer. After its8,192-key prefix returns, the fixture
chooses a camera at an omitted key and successfully reads that actual snapshot.
This avoids assuming any filesystem enumeration/creation order. Baseline omission
is verified, not inferred from a screenshot or empty fabricated cache file.

The staged cursor enumerates **all** directory entries over separate calls, keeping
only the nearest fixed-capacity heap. Each call accepts a quota1–256entries, and
invalid/nonmatching names consume quota too: using a filtered directory iterator
would hide arbitrarily many skipped entries within a nominal turn. Key capacity is
1–8,192. Only LodStore's canonical signed integer filenames are accepted. Full-int
squared distances use an unsigned64sum plus carry bit, with deterministic coordinate
ties, avoiding signed subtraction/square overflow. The cursor never reads geometry,
requests chunks or writes files. It releases its directory stream on completion or
explicit cancellation. Result access requires successful complete enumeration;
incomplete cancellation clears retained keys.

The finished result matches an independent BigInteger-distance full-sort oracle.
The8,205-entry case completes in33calls at quota256, retains exactly8,192nearest
keys and includes the camera page first. Four independently restarted camera scans
with quota17match the oracle while ignoring eight malformed/non-cache names.
Three signed-extreme coordinate cases exercise65-bit ordering. Other assertions
cover per-call visits, heap/result caps, finite progress, stream completion,
normal cancellation, invalid limits and missing-root/no-mutation behavior.
**14,303checks pass**. The small signed-extreme files are filename-only ordering
inputs; the main8,205-page omission/recovery case uses valid readable geometry.

## Source identity and execution

`evidence/source-manifest.json` records immutable Git blob IDs for the renderer,
LodStore and ColumnarChunk. `evidence/run.py` verifies these, verifies the exact
baseline extraction, compiles the staged source and fixture, executes actual temp-file
IO and seals source/result/class hashes in identity.json. The test cleans its own
unique temp directory. Final runner execution repeats the same semantic result;
this is no timing/performance selection.

Workspace maintenance removed the pinned JDK25/JDK21/Gradle and native/dependency
caches. System Java17retains the jdk.compiler module even though the javac CLI is
absent, so the runner invokes `java -m jdk.compiler/com.sun.tools.javac.Main`.
An initial compile attempt referenced pruned prior LodStore/ColumnarChunk files and
failed before execution; both were restored from immutable Git and the accepted
runner then passed. The missing executable probe was an environment discovery,
not a product test. No full Gradle build, current49-source JDK25 compilation,
packaged/native Minecraft, graphics, Vulkan/OpenGL, screenshot, FPS or owner-GPU
check ran here. Earlier R53/alpha27 evidence remains historical, not refreshed.

## Integration still required

This component fixes selection across the full enumeration **in its own fixture**;
the shipped/production renderer retains its prefix cutoff. A renderer adapter must
resubmit one bounded worker turn at a time, interleave geometry jobs, retain the
existing shared8,192-key merge/edit guards, and cancel/close worker-owned cursors
on world changes. A camera move must discard stale results and start an appropriately
attributed scan. Session identity, reset/shutdown, worker rejection, error propagation,
camera churn, native travel, visible coverage and loaded/edit supersession need
native tests before promotion. Do not restore an archival renderer snapshot over main.

Entry count is bounded per call; filesystem open/iterator/close latency is not a
2msdeadline guarantee. Final snapshot sorting/copying is bounded by8,192keys but
still requires worker scheduling. Heap objects, boxed result keys, sorted copies
and parser transients need allocation/peak-memory profiling. The test calls turns
sequentially in one JVM; they are not rendered frames or independent60Hz ticks.
Whole-world completion still depends on directory size. There is no spatial on-disk
hierarchy/coarser fallback, completed-scan rediscovery policy or partial-result
publication yet. This does not establish the cause/removal of rectangular horizon
cutoffs, broad enabled-view water/transparency/LOD acceptance or superiority.

## Reproduction and next work

From this directory: `python evidence/run.py` with Java17+ and the jdk.compiler
module. Optional JAVA environment variable selects its executable. Compilation
creates classes/ locally; no game, toolchain or class binaries are committed.

Restore the pinned build/native environment, implement the renderer lifecycle
adapter in isolated current-main and run full/module/package plus Vulkan-first
travel/edit/save/shutdown/coverage checks before source promotion. Preserve R53,
current water bounds and frozen V1–V6. Continue rare-host/native density and other
water/pressure/LOD requirements afterward. The cursor remains under staged/.
Own lease released; engine acceptance incomplete; no background process remains.
