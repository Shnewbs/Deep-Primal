# Expanded replica — docs/engine-hourly-audits/2026-10-28T0933-4b61329/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0933-4b61329/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T0933-4b61329/REVIEW.md |
| Preserved source SHA-256 | `836f600a452b8eb56bd86160f286fedd7bde219f85f1d967f677c3a7206ea8dc` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal — independent audit 2026-10-28T0933-4b61329”. Its original opening states: “Pinned committed main: 4b6132969ab517bd581b044308a30a62e95fb411 (0.0.1-alpha.29-dev.50-v8strict.1). Read-only auditor: no production edits, builder/renderer leases or checkout interference, no JAR and no release. Owner's October 27 latest override assigns full Gradle/Minecraft/GPU builds to the owner; the previous JAR instruction is superseded.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DeepPrimal_Audit_20261028T0933_4b61329_SourceEvidence.zip`.

## Research expansion — gaps and details to deepen

- Convert the persisted-LOD finding into a recovery state machine that remains correct for stationary cameras, empty results, retries, and stale completion.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Track request epoch, recovery epoch, cursor ownership, retry reason, and next eligible retry independently of camera movement.
- Document mutable-versus-immutable result ownership and bounded incremental publication; old scans cannot consume a newer recovery request.

## Research expansion — acceptance checks

- Inject an I/O error followed by recovery without camera movement, and create new persisted data after a successful empty scan.
- Complete an old scan after a reset or newer request; verify no duplicate admission, lost wakeup, wrong-world publication, or unbounded retry spin.

## Research expansion — source anchors

- Deep Primal — independent audit 2026-10-28T0933-4b61329
- Unrun acceptance and priorities

Related design: [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — independent audit 2026-10-28T0933-4b61329

**Pinned committed main:** `4b6132969ab517bd581b044308a30a62e95fb411` (`0.0.1-alpha.29-dev.50-v8strict.1`). **Read-only auditor:** no production edits, builder/renderer leases or checkout interference, no JAR and no release. Owner's October 27 latest override assigns full Gradle/Minecraft/GPU builds to the owner; the previous JAR instruction is superseded.

## What changed since the 08:30 UTC audit

- Builder changed [V8GeneratorSnapshot.java L47–91](https://github.com/Shnewbs/Deep-Primal/blob/4b6132969ab517bd581b044308a30a62e95fb411/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L47-L91) (Git blob `a97d93b143104247566442b63d285545c894eab8`) to strictly reject unknown/missing/non-string root snapshot fields. V8 remains experimental and explicit-only; not registered as a Minecraft chunk generator. Version was bumped to `0.0.1-alpha.29-dev.50-v8strict.1`.
- Renderer changed [LodDiskIndexService.java L19–24 and L114–144](https://github.com/Shnewbs/Deep-Primal/blob/4b6132969ab517bd581b044308a30a62e95fb411/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L114-L144) (blob `d3c80764a533f86e4d1623ec3d86d1df1c3752a0`) to periodically retry successful nonempty folder scans after 400 client ticks; the 200-tick empty/20–320-tick failed/missing-directory contract remains.
- The auditor did **not** implement these changes. This is an independent test of already committed snapshots. Original frozen V1–V7 code and water source were not edited.

## Independent checks actually run

- Temurin Java **25.0.5+8**, `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`; exact committed V8 snapshot, materializer, density graph/chunk and LOD nearest-scanner/service source files verified against pinned Git blobs.
- V8 strict-root negative and signed-seed tests **14,392** assertions (4,122 invalid records rejected; 2,048 signed-long roundtrips). V8 saved profile **594,901**, base height **874,642**, planned ground **1,235,414**, section bridge **11,047,019**. **V8 total 13,766,368 passed on Java25**. Tests use API-compatible Minecraft/Mojang stand-ins; native JsonOps/NbtOps/NeoForge not run.
- Six LOD index/scanner/retry/epoch/stationary-rediscovery tests on actual temporary Linux folders: **26,327** assertions passed Java25 and **26,356** passed Java21. Bounded 256-entry/turn scan and 8,192-key queue, recovery-epoch, missing directory retry and 400-tick nonempty rediscovery checks passed. Privileged filesystem bypassed the denial fixture; POSIX permission-denial subcase not applicable.
- Two old-source negative controls reproduced the prior defects: old V8 accepted an extra `heigth` root member; old LOD coordinator failed to rediscover a saved page at stationary camera. Current pinned implementations passed these same tests. Independent source and logs are retained separately in `DeepPrimal_Audit_20261028T0933_4b61329_SourceEvidence.zip` (SHA256 `31631c9714d901ffc9967559194e15aa07b424e622ae991b35b6dd9972d7108f`).

## Ten-category whole-engine grade

**Historical full baseline carried, not a fresh complete regrade** (source of rubric: [REVIEW.md](https://github.com/Shnewbs/Deep-Primal/blob/4b6132969ab517bd581b044308a30a62e95fb411/docs/engine-review-2026-10-24/REVIEW.md)). Equal weight:

| Terrain | Biomes | Hydrology/caves | Features | Performance | Determinism | Architecture | Extensibility | Testing | Polish |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 | 4 | 6 | 4 | 3 | 7 | 6 | 4 | 7 | 4 |

**Total 50/100 = 5.0/10, Tier 2.** The threshold is a **fresh complete** audit **strictly >8.5/10**; not met. Source-only opt-in V8 and renderer tests do not earn native gameplay performance credit.

## Unrun acceptance and priorities

No full Gradle/Java25 game build, native NeoForge chunk generation or world save/reopen, actual Mojang JSON/NBT codec, package/inner JAR CRC or source-compiled-packaged class identity, Windows AMD8060S Vulkan/OpenGL validation, frame/VRAM benchmarks, frozen V1–V7 production replay, loaded/unloaded water constituent conservation, or release. This is Linux x86_64 with no `/dev/dri`; **not Windows or native/GPU acceptance**.

Priorities: (1) owner-native Mojang V8 saved snapshot codec and worldgen/lighting/heightmap, V1–V7 exact replay and saved profile reopens; (2) finite-water transaction ownership/constituent conservation through edits/unloads/restart and matched waterfall pixels/latencies; (3) native LOD stationary-page/edit/teleport tests and representative Vulkan/OpenGL benchmark and I/O tails. Audit progress must not pause the builder. No old JAR retention changes were needed because no qualified new JAR was created.


<!-- END PRESERVED SOURCE -->
