# R53 — prevent unreadable LOD snapshot replacement

Parent `c9f79a74aaeb868635fc54d5d0cccc6d754bcc7b` (current main after R52).
One production fix in `LodStore.write` preflights the same per-name and aggregate
palette limits already enforced by `read`, before directory/temp-file creation or
replacement. One private real-filesystem regression and its CoreChecks invocation
are added. The v1 format, column data, revision checks, atomic-move/fallback behavior,
renderer, default presets, saved V1–V6 geography, quantities and player edits are unchanged.

## Reproduced defect

`ColumnarChunk` accepts a 257-entry palette containing 4,096-character names.
Before the fix, a new revision with 1,052,672 palette characters successfully replaces
a valid existing cache page. `write` returns true; `read` immediately rejects the
new page because it exceeds the 1,048,576 aggregate character limit. The previous
readable revision is lost. The regression uses real temporary directories, actual
production GZIP/header/CRC IO and different accepted revisions, with no filesystem
or Minecraft doubles. Its baseline explicitly proves all three outcomes.

The existing exception text calls this a byte budget, but the existing read limit
counts Java string characters, not encoded UTF bytes. The fix matches that contract;
it does not silently redefine valid files. These palettes are legal core objects;
the fixture uses duplicate entries to isolate the size contract, not as a claim
about their frequency in native captures.

## Fix and validation

The writer now rejects an over-budget palette with IOException before mutating a
cache path. Invalid initial writes create no directory. Invalid replacement writes
retain the prior compressed bytes, readable geometry and revision exactly. Valid
stale writes still return false. Exact aggregate-boundary ASCII and three-byte UTF
palettes roundtrip, including all 256 stored columns and separate-store reopen.
The existing persistence caller handles IOException; no new retry or device policy.
This does not repair already corrupt cache files or establish the cause of the
reported rectangular horizon cutoff.

Fresh checks:

- Baseline reproduction passes its bug-expectation checks: accepted write, rejected
  reopen and different previous-snapshot bytes.
- Candidate passes 19 real-filesystem checks; the forced full build invokes the same
  regression alongside existing V6 terrain, authored/legacy deposits, watershed,
  water/budget/cache and module gates. All 31 executable tasks pass.
- 660 isolated parent inputs are Git-blob verified. Only one production source and
  the existing private CoreChecks invocation differ, plus the new private fixture.
  Independent direct JDK25 compilation of all49core production sources matches all
  87Gradle classes byte-for-byte, with source hashes checked before/after.
- Seven root/nested archive CRCs and502compiled product class matches pass. All45
  compiled private paths are absent from the package. The packaged embedded core
  then passes the same19filesystem checks. Its actual loaded LodStore class hash
  matches independently compiled source, rather than trusting Gradle output alone.
  Package SHA256 remains unchanged across that run.

Private Linux test bundle: `b786aa3a1f47889d4f34e38bbffdad7ed3595c382347452834651ffd397b0ab9`.
Loaded fixed LodStore class: `05b61026b3df7c5599a204b927ead8f7427523ea0421bb6312fefd93b91e6475`.
No installer is supplied; the owner's delivered alpha.27 Windows JAR predates R53.

The initial reporting wrapper assumed the earlier alpha.27 report's32-task count.
The actual fresh build exits0 with31tasks and every required semantic PASS marker.
Only that reporting assertion was corrected, without altering logs or rerunning the
build. The original rejected wrapper result, reporter source, complete actual task
list and revised acceptance are retained. No Java compile, behavioral check or game
crash failed here. No Minecraft client/server, screenshot, graphics, timing or new
Windows/owner-GPU test ran. The embedded-core process is real packaged JVM IO,
not a Minecraft/native-render session or independent60Hz evidence.

## Reproduction and limits

Use JDK25. `JAVA_HOME=/absolute/jdk25 python evidence/run.py` compiles the preserved
baseline/candidate LodStore and unchanged ColumnarChunk, then runs both expectations
in separate JVMs using temporary files. Copy the accepted three source/check changes
onto a fresh immutable-parent checkout for full integration. `build.py` and
`fresh-compile.gradle` record the forced offline Linux build with the retained
JDK25/JDK21/Gradle9.2.1 and pinned private NeoForm fallback; restore equivalent tools
under `toolchain/` and the checkout under `project/`. Then run `verify_core.py`,
`audit_package.py` and `verify_package.py`. The latter checks the loaded class from
the embedded product core before invoking the regression. The fallback/toolchains
and tests are not shipped. `evidence/source-manifest.json` seals restored Git inputs;
`evidence/source-audit.json` and `runtime.patch` delimit the change.

This closes the scoped write/read palette-budget mismatch. Broad enabled-view LOD
coverage, paged discovery beyond8,192keys/coarser fallback, water/frame publication,
streams/oceans, pressure/budgets and owner-GPU/performance acceptance remain open.
Native rare-host/density/cave coverage from R52 is also unfinished. Next continue
those current-main priorities without changing frozen V1–V6; new geography needs V7.
Own work lease released after checkpoint; no background process remains.
