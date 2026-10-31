# Dev125 — V8 direct materializer coordinate admission

Base main: `310804530eb00cffac9920a11808dab037f47355` (scoped lease acquired at 08:59 UTC). Latest independent audit:
`docs/engine-hourly-audits/2026-10-31T0833-5003a970/REVIEW.md`,
pinned `5003a9709d9d02acd124df1f3299a515600f765b`.
The auditor found no additional core defect and verified Dev124 under Java25;
the durable native transaction and border reopen remain open.

## Production repair

Dev123's signed-coordinate guard protected `V8ChunkGenerationNativeFill`,
but direct opt-in V8 calls into `DensityChunkMaterializer.fillPlannedSurface`,
`fillPlannedRaw` and `fillSavedEcotoneInternal` still accepted ChunkPos
coordinates whose 16-block origin wraps the Java int range. They could
sample/write a different signed world coordinate before a later owner gate.
A single private preflight now uses exact signed multiply/add checks on
**both axes**, before origin calculation, density/climate callbacks or
native setters. Valid chunk X/Z range is [-134217728, 134217727] inclusive.
No legacy V1-V7 path, V8 saved profile, water quantity, art or renderer
source was changed. No V8 generator registration is implied.

## Focused evidence

OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`,
`java -ea -Xverify:all`. The focused runner extracts the exact
production guard method and checks its three direct entrypoint callsites.
Two positive processes passed **130,121 assertions each** (260,242 total):
signed boundaries, both-axis invalidity, 100,000 seeded random pairs and
30,000 targeted pairs. Two independently compiled unchecked-multiply
mutants were rejected. The first harness attempt failed because javac
warned about auxiliary test classes under `-Werror`; moving doubles
to separate source files resolved it, and the accepted runner passed.
These are Java21 isolated exact-method/API-double checks, **not** a full
production materializer compile, Minecraft native chunk execution,
saved-world reopen, Java25/Gradle or GPU evidence.

Reproduce: `python3 tools/test_v8_materializer_coordinates.py`
(and `python3 -OO tools/test_v8_materializer_coordinates.py`).
Full checkout, dependency cache and Java25 absent in the builder runtime;
Git CLI cannot resolve github.com. Owner retains full builds/JAR publication
under the October 27 policy. No JAR created or deleted. Independent baseline
remains 50/100, 5.0/10; no new grade.

Next: authoritative native chunk+stamp durable CAS and crash recovery;
old/new V8 border save/reopen, relief V7 native parity and finite-water ledger.
