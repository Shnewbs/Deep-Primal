# Deep Primal dev186 — worldgen chunk-key admission

Date: 2026-11-02 UTC. Baseline main: `493a970bb7ce7ba7ee6cbfc9542e2b4d98c86710`. Read the independent Dev185 audit at `docs/engine-hourly-audits/2026-11-02T0630-dev185/REVIEW.md`.

## Defect and scoped correction
`WorldGenPipeline.generateChunk` previously accepted a non-null materializer result without checking its `MaterializedChunk.key()`. A misrouted or malformed asynchronous result could therefore be published and cached under a different requested chunk coordinate or dimension. The pipeline now rejects mismatched X/Z/dimension and null result keys **before cache admission**, completing the query exceptionally outside the pipeline lock. This does not modify native terrain, water volume, Vulkan, LOD, organics or saved-world formats.

## Reproducible validation
The committed exact production `WorldGenPipeline.java` was compiled on OpenJDK 21.0.12.1 using `javac --release 21 -Xlint:all -Werror` with isolated `ChunkCache` and `ChunkMaterializer` test doubles. Execution with `java -ea -Xverify:all` passed **110,000 assertions** across **10,000 deterministic trials** covering X/Z/dimension/null-key mismatches, coalesced listeners, cache-poisoning prevention, failed inline retry and correct-key cache-hit parity.

The original source, with only the new guard removed, compiled but failed immediately with `AssertionError: Wrong-key materialization succeeded`. Run `bash docs/validation/engine-continuation/pipeline-key-admission-2026-11-02/run.sh` in a checkout with JDK 21+ to reproduce the positive regression. The test is also registered in `CoreChecks`.

Verified Git blob IDs:
- Production `WorldGenPipeline.java`: `2e1a5cb55007d10745260dd355fbf180d5f2920b`.
- Regression `WorldGenPipelineKeyChecks.java`: `6df4e82840e17463c4c2918214baf4ba4bbe4d84`.

## Release gate
This was an isolated production-class test, **not** a complete NeoForge integration build. The runner has Java 21 but no complete private checkout, Java 25, resolved Gradle/NeoForge dependencies or native GPU/game acceptance. Local GitHub DNS fails, so source was committed and verified via the authenticated GitHub connector. No GitHub Actions were run. **No playable replacement JAR or JAR SHA-256 was produced**; previous successful JARs remain untouched. The historical whole-engine grade remains 5.0/10 pending revalidation.
