# Dev184 — LOD transition first-capture invalidation (2026-11-02 UTC)

## Confirmed defect
`TransitionDependencies` used `x == Long.MIN_VALUE, z == 0, radius == 0` as an implicit never-captured sentinel. Those are also legal input values. The first empty capture with exactly those values returned `false` from `end()`, suppressing the first transition rebuild. `clear()` could reproduce the same failure. This is an edge-case invalidation bug, not a claim that a normal in-game camera reaches this coordinate.

## Scoped correction
Track explicit `initialized` state. Every first capture after construction or `clear()` is a change, regardless of its coordinates. Subsequent captures still use the exact coordinate, dependency identity and length comparisons. No per-frame allocation, native renderer file, water-volume calculation, saved-world format or Vulkan API is modified.

## Reproducible validation
The original production class was retrieved from `main` at Git blob `fa1177425109fafca5d17a6abe48540510e7ad58`; local `git hash-object` matched. With that original source, the new regression fails at assertion 1 (expected negative control). With the correction, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` pass **225,011 assertions**. The regression covers first empty capture, repeated capture, identity swaps, removal, zero-capacity tracker, clear/reopen, signed-long coordinate boundaries, and 200,000 deterministic randomized iterations with a separate reference model. This is an isolated production-class test, not a complete mod compilation.

Run `bash run.sh` with Java 21+ in the local evidence ZIP.

## Coordination and release gates
Baseline main: `b76b2dea0ebb1ad735f2909fa03924afccf638ac` (Dev183 independent audit). The engine lease was `completed-released`; the stale water claim expired 2026-10-28. Only `TransitionDependencies.java`, new test registration, this report and its scoped claim are in scope. No renderer/Vulkan/organics/auditor-owned files or GitHub Actions touched.

The local environment has Java 21, no Java 25, no complete private checkout, no Gradle/NeoForge cache, and `git ls-remote` fails DNS resolution. A playable JAR and JAR SHA-256 cannot be claimed. Existing successful JARs must remain until a validated replacement exists. The historical whole-engine grade 5.0/10 is unchanged. Native LOD first-frame, seam and GPU acceptance remain open.
