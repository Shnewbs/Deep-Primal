# Expanded replica — docs/engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md |
| Preserved source SHA-256 | `497ca58b72be30b2c246760549fd30696e2d10d899b44e98afea5f8b132928b6` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal — independent source audit, 2026-10-28 06:33 UTC”. Its original opening states: “Pinned committed source: 1aa65a8b1068f6261fe0eb99e6a4394132636fdb; version 0.0.1-alpha.29-dev.47-v8columns.1. No Builder/renderer/production source changes were made by the auditor. Full builds/JARs remain owner-operated under the October 27 override.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityChunkMaterializer.java`.

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

- Deep Primal — independent source audit, 2026-10-28 06:33 UTC
- Verified new LOD retry defect
- Focused verification on pinned source

Related design: [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — independent source audit, 2026-10-28 06:33 UTC

**Pinned committed source:** [`1aa65a8b1068f6261fe0eb99e6a4394132636fdb`](https://github.com/Shnewbs/Deep-Primal/commit/1aa65a8b1068f6261fe0eb99e6a4394132636fdb); version `0.0.1-alpha.29-dev.47-v8columns.1`. No Builder/renderer/production source changes were made by the auditor. Full builds/JARs remain owner-operated under the October 27 override.

## Verified new LOD retry defect

The prior immutable-list sort defect is **repaired** in [VoxelLodRenderer.java L282–288](https://github.com/Shnewbs/Deep-Primal/blob/1aa65a8b1068f6261fe0eb99e6a4394132636fdb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L282-L288). New [LodDiskIndexService L26–45](https://github.com/Shnewbs/Deep-Primal/blob/1aa65a8b1068f6261fe0eb99e6a4394132636fdb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L26-L45) starts a scan only when not yet started or after camera movement of at least **64 chunks**. On a completed I/O error, [L55–62](https://github.com/Shnewbs/Deep-Primal/blob/1aa65a8b1068f6261fe0eb99e6a4394132636fdb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L55-L62) discards the cursor and returns failure but **leaves `started=true`**. A stationary player therefore gets no automatic retry after a transient directory failure. [LodDiskNearestScan L148–151](https://github.com/Shnewbs/Deep-Primal/blob/1aa65a8b1068f6261fe0eb99e6a4394132636fdb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L148-L151) similarly treats an absent directory as successful empty completion; files created later are not rediscovered at the same camera.

**Independent reproducible JDK25 tests** compiled the exact committed scanner/service blobs (`dcb1510`/`0bd9366`) with `--release 25 -Xlint:all -Werror` and ran with `-ea -Xverify:all`. On an isolated filesystem, (1) a directory created after initial missing-folder scan and (2) a previously unreadable directory (mode 000, Java run as unprivileged user) returned to normal accessibility. Both remain undiscovered for **200 further same-camera ticks**; calling `service.reset()` and rescanning discovers the existing .plod file. **808 assertions passed**. This confirms a product-code/real-filesystem retry gap, not native Minecraft restoration.

**Recommended renderer-only repair:** bounded backoff/retry after transient scan error or initially absent folder; retain dedicated worker, queue/edit guards, 256-entry turns, same-session cancellation and safe suppression of log spam. Add native world reopen/travel/edit tests.

## Focused verification on pinned source

| Test | Executed Java25 result |
|---|---:|
| Existing LOD nearest scanner | 9,006 assertions passed |
| Existing incremental cursor/cancel | 8,295 assertions passed |
| Current integration service | 521 assertions passed |
| New I/O failure and late-folder retry reproductions | 808 assertions passed |
| V8 base heights / planned columns | 874,642 assertions passed; 34,304 columns |
| V8 native-section bridge with Minecraft API doubles | 11,047,019 assertions passed; 1,376,512 positions |
| **Total** | **11,940,291 focused assertions passed** |

All ten V8 product Java source Git blobs match current pinned Git sources, including `DensityChunkMaterializer.java` blob `6a6fafe`. Temurin **25.0.5+8** and `javac 25.0.5` were verified. Source-only materializer checks **do not** verify real NeoForge, V8 registration, lighting, heightmaps, save/reopen, Windows/AMD8060S, GPU or performance. Other role checkouts/caches/leases and active tasks remained untouched.

## Ten equal-weight category scores — carried, not a fresh full regrade

| Category | /10 | Evidence boundary |
|---|---:|---|
| Terrain realism | 5 | Unregistered V8 section/base-height prototype; native geography unrun |
| Biome coherence | 4 | Weighted ecotone and material blending not accepted |
| Hydrology/caves | 6 | Finite water present; unload/conservation/soak and cave variety open |
| Structure/features | 4 | No accepted full deterministic structure pipeline |
| Performance/threading | 3 | Historic warm fill 14.45–14.99ms vs 5ms goal; no fresh full benchmark |
| Determinism/stability | 7 | Historic frozen V1–V7 replay, native V8 and LOD recovery open |
| Architecture | 6 | Modular services; persisted LOD retry state-machine omission |
| Extensibility | 4 | No production V8/feature authoring lifecycle |
| Testing/docs | 7 | Fresh focused JDK25 tests and reproducible defect; no native testing |
| Polish/tooling | 4 | LOD recovery/visible water/LOD horizon defects outstanding |
| **Overall, Tier 2** | **50/100 = 5.0/10** | **Carried 2026-10-24 complete rubric, not a new independent whole-engine regrade** |

Target is a **fresh complete grade strictly greater than 8.5/10**; currently unproven. Experimental source-only libraries earn no gameplay credit.

## Build, artifact and boundaries

Under the owner's **October 27 latest override**, the owner performs full Gradle/Minecraft/GPU builds. **No fresh JAR was built, uploaded or released** in this auditor cycle. Not run: full Gradle 9.2.1/NeoForge, native generation and save/reopen/shutdown, package/archive CRC/source-to-bytecode, Windows/AMD GPU, Vulkan/OpenGL/LOD frame p95/p99, or water long-soak. No older binary claimed as current; no JAR deletion needed. No GitHub Actions, Releases or CurseForge. Audit-only evidence ZIP (44 valid entries) is 53,254 bytes, SHA-256 `107f6d16b031f16aa0b6ff9b5d9f0c823842de39dc7925fe883b4fc0b0fd97eb`.

**Next:** renderer owns bounded scan failure retry and native resume; owner verifies V8 native section/base-height/legacy save, Builder continues finite-water conserved ledger, then full-scene Vulkan/GL profiles. Auditor remains read-only and does not gate Builder progress.


<!-- END PRESERVED SOURCE -->
