# Expanded replica — docs/VALIDATION-0.0.1.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1.md) |
| Source identity | git: docs/VALIDATION-0.0.1.md |
| Preserved source SHA-256 | `2ab413c842127a036c553e6aa91d66fd17b3a9af6c72a83e5d1cfac095fcdc63` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Validation — 0.0.1-alpha.1 worldgen prototype”. Its original opening states: “Date: 2026-10-18. Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25 / Gradle 9.2.1.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Treat restoration, test harnesses, and output paths as independently validated infrastructure so they cannot manufacture product success or failure.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define terminal process status, complete output set, bounded protocol/deadline behavior, and unconditional failure checks.
- Record environment repairs separately from product changes; retain original failure logs and exact fixture/tool revisions.

## Research expansion — acceptance checks

- Run the harness against deliberately failing, truncated, stale, timed-out, and success-marker-only producers.
- Verify wrong request IDs, malformed frames, dangling output paths, missing required inputs, and cleanup after cancellation.

## Research expansion — source anchors

- Validation — 0.0.1-alpha.1 worldgen prototype
- Reproduction and raw evidence

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.1/` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.1) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Validation — 0.0.1-alpha.1 worldgen prototype

Date: 2026-10-18. Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25 / Gradle 9.2.1.

## Outcome

Compiled JAR; both selectable world presets passed headless dedicated-server world creation, FULL-chunk generation, sampler commands, bedrock-floor verification, save/flush, clean shutdown, and restart. Four fixed-point sampler results were identical before/after restart in each preset. A 289-point grid in each preset sampled elevations up to Y=392 for seed 20261018. Sampling is not a rendered terrain screenshot.

This is an **experimental alpha for terrain playtesting**. It does not complete the survival mod or pass the full worldgen-performance release gate. No graphical client session, player joining, client rendering, multiplayer contention, gameplay XP interaction, or Earth-mode test was performed. Vanilla XP suppression compiled and loaded; no connected-player behavioral test is claimed.

## Initial timing observations

Environment: Linux 6.18.55 container; 8 available processors on an AMD EPYC 9V74 host; about 9.7 GiB host-visible RAM. Dedicated server heap capped at 2 GiB; view/simulation distances 3; no players. Shared virtualized hardware, not a recommended-user-hardware specification.

Seed 20261018, nine FULL requests around chunk (200,200), radius 1, one in flight. First pass uses previously ungenerated target coordinates in a fresh disposable world; dependent-neighbor generation is included. Repeat pass may load cached or saved data, not necessarily resident chunks. No formal confidence interval is meaningful from nine samples; nearest-rank p95/p99 equal the maximum.

| Preset | Pass | Requests | Median | p95 | Elapsed | Requests/sec |
| --- | --- | --- | --- | --- | --- | --- |
| prototype | cold | 9 | 298.64ms | 1861.63ms | 4.15s | 2.17/s |
| prototype | repeat | 9 | 150.00ms | 201.42ms | 1.39s | 6.48/s |
| prototype_deep | cold | 9 | 291.51ms | 2105.26ms | 4.44s | 2.03/s |
| prototype_deep | repeat | 9 | 149.92ms | 201.49ms | 1.38s | 6.53/s |

These are request-to-ready wall times with tick polling, scheduling, dependent neighbors, I/O, and generation included. They do not isolate CPU, peak memory, or player frame stalls. Treat the latency as work to investigate, not proof of performance targets being met. A repeat pass is not evidence of fast fresh generation. Numeric release budgets and representative-player benchmarks remain open.

## Reproduction and raw evidence

See [prototype instructions](WORLDGEN-PROTOTYPE.md). After accepting the Minecraft EULA:

```sh
python3 tools/server_smoke.py ./gradlew --preset prototype --accept-eula
python3 tools/server_smoke.py ./gradlew --preset prototype_deep --port 25569 --accept-eula
```

Run from the repository root. Use clean `run-smoke-*` directories when measuring cold generation. The test harness binds only to loopback and uses disposable offline-mode test servers; it is not a public server configuration. Its temporary force-load is removed after verifying the bedrock floor. Native benchmark loading tickets are released after each request.

Raw sampler records, smoke summaries, benchmark CSV files, and timing metadata are in [validation/0.0.1-alpha.1](validation/0.0.1-alpha.1/). No Minecraft binaries, game assets, world saves, or third-party mod assets are committed.

## Defects found during verification

- Startup exposed two invalid climate density-function references; replaced with registered shifted-noise definitions.
- The engine chunk-request method managed-waits on its main-thread path. The probe now uses its supported off-thread dispatch path, holds one non-persistent loading ticket, and releases it on completion/cancel/failure. Without a stable ticket, the initial asynchronous attempt returned an unavailable chunk; the final tests passed.
- A floor check initially targeted a chunk whose temporary request ticket had expired. The harness now temporarily loads exactly that chunk for inspection and removes its force-load.

Build environment notes: Java 25 was installed for the target and Java 21 for NeoForm's asset-download helper. The local NeoForm CLI needed a java.home executable-path fallback because ProcessHandle command metadata was unavailable in the container. That local build-tool workaround is not shipped in the mod and does not modify Minecraft gameplay code. Java trust used the environment's supplied CA store; TLS verification was retained.

## Artifact

`DeepPrimal-26.1.2-0.0.1-alpha.1.jar`

SHA-256: `1cf5b0744e1ec74ffc76638499b146272cf4e4eb9bdf6b7e049c4860002a8f71`

The JAR contains mod code and authored worldgen resources, not the Minecraft runtime. Build output naming and metadata identify an alpha. Use new disposable worlds; height profiles are not an existing-world migration facility.


<!-- END PRESERVED SOURCE -->
