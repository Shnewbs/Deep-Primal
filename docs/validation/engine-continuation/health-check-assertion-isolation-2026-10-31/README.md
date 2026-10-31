# Dev138 — invariant assertion containment (2026-10-31)

## Scope and defect
- Source head inspected: `34fdcdacab2834e8d4297b799106e54faa09a7ec` (Dev137 independent audit); claim created at `6289ad3262f0f7aeffc14e2d2d0d7fd7733fab92`.
- `EngineHealthCheck.runAll()` caught `Exception` but not `AssertionError`. A failing invariant using a Java assertion could abort periodic validation on the server tick and prevent later registered checks from executing.
- Fix catches `AssertionError` only, retains it as the cause of a diagnostic `Exception` (preserving the public `CheckResult.exception()` type), and continues remaining checks. Fatal `LinkageError` and other VM errors still propagate.
- The dependency-free regression is invoked by `CoreChecks`. No saved formats, water volumes, terrain geometry, Vulkan, LOD or settings changed.

## Reproduction and tests
- Original production blob `ed0c8e89afad6a5cb3a2a7084801bba6e1892a4a`, SHA-256 `6be0a1f2ba462b371f680c495115b5c0b0c3a7d73163087514b4a9289ad4c0b1`, reconstructed locally byte-for-byte (Git blob identity verified).
- Before repair, the new regression terminated with `java.lang.AssertionError: water invariant failed` at its first `runAll()` call.
- After repair: `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed **179,820 checks**, including **50,000 seeded randomized trials** of healthy, assertion, runtime exception, explicit failure and null-return suppliers.
- Additional tests: later suppliers continue; `runOrThrow` still rejects failures; disabled checks are not invoked; a deliberately thrown `LinkageError` is not swallowed.
- Isolated compilation uses exact committed production source plus minimal `WaterTransport` and `ChunkCache` API stubs. It is **not** a full NeoForge, Java25 or live-server build.

## Blockers
- No complete local private repository checkout, JDK25 or Gradle/NeoForge cache in this execution environment. `git ls-remote` fails DNS resolution for `github.com`. The connected GitHub integration supports source commits.
- No full mod JAR, runtime FPS/terrain/water acceptance, or new JAR SHA-256. Retain any prior successful JAR until a verified replacement exists. No GitHub Actions invoked.
- Latest independent whole-engine grade: historical **5.0/10**, not regraded by this scoped change.
