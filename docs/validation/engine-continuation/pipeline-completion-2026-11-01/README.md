# Deep Primal dev146 — worldgen query completion on failed cache admission

Date: 2026-11-01 UTC. Source baseline: `39abfe101e02e901525ad53060b3864389745875`.
Consumed independent audit: `docs/engine-hourly-audits/2026-11-01T0530-dev145/REVIEW.md`.

## Defect and scope

`WorldGenPipeline.generateChunk` discards the CompletableFuture returned by
`whenComplete`. If an async materializer succeeds with a null result, or if
`chunk.payloadBytes()` / `cache.put` throws a RuntimeException during
the completion callback, that exception is captured only by the discarded
dependent stage. The externally returned query future never completes, even
though the pending-map entry is removed. Sequential worldgen batches can hang.

The fix treats null materialization as a failed query and catches runtime
cache-admission exceptions inside the callback. Both failures complete the
original query future exceptionally **outside the pipeline monitor**, preserving
successful caching, deduplication, normal exceptional completion, synchronous
dispatch failure, and shutdown cancellation. No terrain noise, water volume,
renderer, saved-world format or JAR packaging was changed.

## Reproduction and validation

Exact baseline `WorldGenPipeline.java` Git blob:
`3815c4395af2bccb893bcb24c9d3b18bc1e4613a`.
Patched production blob:
`c705484b042d91b53fc00b8ea2173e4c9538f798`.

- Java 21.0.12.1, `javac --release 21 -Xlint:all -Werror`,
  `java -ea -Xverify:all`, deterministic API doubles.
- Baseline fails with `AssertionError: Pending future never completed`
  (300 ms timeout), proving the stuck future.
- Patched exact production blob passes:
  `PIPELINE_COMPLETION_PASS checks=3750 trials=250 nullCompletion=true cacheFailure=true recovery=true coalescing=true exceptional=true synchronous=true shutdown=true`.
- Two deliberately defective variants, one without the null guard and one
  without the cache-exception guard, both fail with the expected timeout.
- The isolated fixture is in `fixture/`; run `bash run.sh` from this
  directory in a complete checkout. The doubles are not production code and
  must not be compiled with the real `ChunkMaterializer` or `ChunkCache`.
- Patched source SHA-256: `bae8c5213eceae11c258c18963e9d3592303f4cf56e533b8293450c86c065094`.

## Limits and acceptance

This is an isolated Java 21 source test, not a complete NeoForge/Minecraft
integration build. The fixture simulates an invalid null completion and cache
admission failure; real terrain generation, cache lifecycle, and live world
reopen remain to be tested. The full private checkout, Java 25, Gradle/NeoForge
dependencies and native Vulkan/LOD hardware are unavailable in this runner.
Git CLI DNS resolution of github.com fails; the authenticated GitHub connector
was used to commit and verify source. No GitHub Actions or JAR build was run.
Retain the last verified JAR until a qualified replacement is built and hashed.
The whole-engine 5.0/10 grade is historical, not a new audit score.
