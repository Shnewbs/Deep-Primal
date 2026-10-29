# Deep Primal independent Dev53/renderer-source audit — 2026-10-28T1227Z

**Pinned committed source:** [`49bef407845fb83ad563ceb40754144173449ebf`](https://github.com/Shnewbs/Deep-Primal/commit/49bef407845fb83ad563ceb40754144173449ebf), `main` at start. Version `0.0.1-alpha.29-dev.53-v8callback.1`. At later inspection `main` advanced by one commit to [`ded29c9f6dc490f73ca1ea0993815215ba1fe0ae`](https://github.com/Shnewbs/Deep-Primal/commit/ded29c9f6dc490f73ca1ea0993815215ba1fe0ae), which only claimed foreground terrain-blending scope; **the new claim is NOT part of the audited source**. The foreground claim was active until 13:00Z; auditor did not interrupt the worker or push into its active main-writing window.

**Status:** Source-level verification completed; native/JAR qualification **NOT RUN** under October 27 owner-build override. No production files changed by auditor. No stable release or new JAR. No new whole-engine grade; carry last accepted **50/100, 5.0/10 (Tier 2)**. Goal **strictly >8.5** not met.

## Fresh checks on exact pinned source

- V8 callback prewrite materializer, [L123–131](https://github.com/Shnewbs/Deep-Primal/blob/49bef407845fb83ad563ceb40754144173449ebf/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L123-L131) and [L232–315](https://github.com/Shnewbs/Deep-Primal/blob/49bef407845fb83ad563ceb40754144173449ebf/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L232-L315): independently reconstructed production Git blob `d5c2e88ac81b206c7e7ce421c32a3eb18977bf1e`; same-chunk **two future callback mutations were restored** after injected setter exception; snapshot read at 777 failed before writes; rollback-time poisoning was detected and signaled using suppressed `V8 rollback incomplete`; successful path kept prewrite snapshot semantics. New independent suite **16,397 passed assertions** using Minecraft API doubles. The exact old Dev52 blob `1399606ac7067a19dbf7e4529821135c161de7fa` intentionally **failed** the new callback regression at cell 6656. The historical Dev52-specific test also fails against Dev53 because its assertion expects the old bug; not a new production failure.
- V8 registered/source-carried suites rerun on matching pinned sources: exact base-height **874,642**; native section bridge doubles **11,047,019**; planned-ground doubles **1,235,414**; saved-profile **594,901**; strict root-profile **14,392**. All passed on Java21. In total **13,766,368** repeated V8 assertions, with **16,397** new independent callback assertions.
- Renderer [capture priority L21–68](https://github.com/Shnewbs/Deep-Primal/blob/49bef407845fb83ad563ceb40754144173449ebf/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L21-L68): exact Git blob `c0f94f73ffa989738a71b0ce019a0160a137c817`; fresh independent BigInteger oracle for **4,000 seeded queue/camera cases**, including signed 32-bit coordinate extremes, FIFO and deterministic ties, passed **12,007 assertions**. No GPU or measured FPS claim. Renderer author's focused benchmark reports slightly higher CPU microseconds/nanoseconds versus prior source, so this gains correctness but not a performance score.
- **Fresh + repeated positive checks: 13,794,772 assertions passed.** This is a scoped, source-level count, not 13.8 million independent native scenarios. Java21 `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all` passed. 12 tested production source Git blobs matched the pinned `main` snapshot. No independent entire-repo checkout exists on this host; reconstruction uses approved retained Dev52 audit sources plus exact Github Dev53 patch and compares Git blob IDs.

## Equally weighted rubric (prior full accepted baseline, carried)

| Category | /10 | Status |
| --- | ---: | --- |
| Terrain realism and variety | 5 | V8 source-only, opt-in; not registered native worldgen |
| Biome blending/coherence | 4 | Current registered thresholds; later terrain-blending claim excluded |
| Hydrology and cave quality | 6 | Finite-water native unload/rollback/constituent long-soak still open |
| Features/structure placement | 4 | No general structure spacing/exclusion pipeline accepted |
| Performance and threading | 3 | Latest cold/warm native <5ms acceptance and new whole-chunk presnapshot cost unmeasured |
| Determinism/stability | 7 | Saved V1–V7 evidence retained; cross-host/native V8 still open |
| Architecture/code quality | 6 | Modules separated; main generator responsibilities still coupled |
| Extensibility/data driven | 4 | V8 saved profiles opt-in, extension contract not live gameplay-ready |
| Testing/benchmarks/docs | 7 | Strong focused checks, but no owner-hardware/native acceptance |
| Polish/config/tooling | 4 | Native Windows/AMD8060S renderer and GUI/performance acceptance incomplete |
| **Overall Tier 2, carried** | **50/100** | **Not a fresh complete regrade** |

The score is conservative because a partial Java-only audit cannot certify native feature completeness. Original baseline review: [engine-review-2026-10-24/REVIEW.md](https://github.com/Shnewbs/Deep-Primal/blob/49bef407845fb83ad563ceb40754144173449ebf/docs/engine-review-2026-10-24/REVIEW.md).

## Outstanding risks / priorities

1. **Critical native gate:** V8 prewrite snapshot reads all 256×height states (up to **524,288** block references). Its rollback verifies all cells, but cannot reverse cross-chunk/asynchronous/lighting/block-entity/world-I/O effects; caller must abandon failed native chunks. Verify actual NeoForge callback handling, timing/allocation, save/reopen, heightmaps and lighting; preserve frozen V1–V7 and player edits.
2. **Hydrology:** native source/receiver ownership, exact water/salt/sediment quantity and loaded→unloaded→reopen lifecycle, cross-chunk callbacks, long continuous waterfall/chasm motion; stop any artificial infinite source/water-level growth.
3. **Render:** real Windows AMD8060S Vulkan with OpenGL fallback, persistent LOD capture/reopen, FSR/DLSS capability/fallback and realistic p50/p95/p99 frame times, seam/flicker/occlusion. Java capture-priority oracle is not GPU acceptance.
4. **Authoring/runtime completeness:** native V8 opt-in saved profile, deterministic long-order/worldgen seeds, blended ecotones, proper structures, measured <5ms chunk target, strict archive/native packaging with complete Themes and notices.

## Not performed

No complete Java25 installation/archive in this run; no Gradle 9.2.1 full build, no new development JAR, no outer/nested JAR CRC or class/source/installer payload validation, no actual native Minecraft client/server save/reopen/shutdown, no Windows execution, GPU Vulkan/OpenGL readbacks, sustained performance benchmarks, JAR retention mutations or GitHub Actions. The October 27 owner override assigns full builds to the owner. A Linux Java21 doubles run is not Windows acceptance. No staged R57/R60/R61 sources promoted or touched.
