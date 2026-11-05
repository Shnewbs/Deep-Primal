# Independent development build audit — b198804

[Download fresh Windows development testing JAR](https://chatgpt.com/library?file_id=libfile_b85a3abe47308191916eaed9178e91b4&account_id=personal).

**50/100 · average 5.0/10 · Tier 2/5. All ten scores carried forward.** This is fresh scoped verification, not a fresh complete regrade or acceptance above 8.5. The renderer author's separate six-category self-grade is a different rubric.

Pinned main source: `b198804f19db960fba6efc52a76b18c603d50601`. Version: `0.0.1-alpha.29-dev.30-audit-20261026T1627-b198804`. Size: **125,294,856 bytes**. SHA256: `ad18e7e4bfbee3d1c9ba24bf7399a20f5c3fa11cecdd915c0eb6935665d0d36f`. Producer/export bytes were compared after producer completion, native execution and saving. Windows Vulkan AMD64 bundle with OpenGL fallback and complete Themes 0.1.0-alpha.8; private development testing only. No stable release, GitHub Release or CurseForge publication.

## Owner retention rule

Keep only the **three newest user-facing Deep Primal JARs by actual creation time**, across audit, Builder and renderer outputs. This supersedes prior keep-every-JAR instructions. This run moved **75 older JARs to trash**, including legacy records without MIME metadata. All pages were searched without a MIME restriction. After the fresh qualified save, retention was reconciled again. Remaining IDs and per-operation outcomes are in [the retention receipt](owner-retention-library.json). Old download links may therefore be retired.

Source/evidence archives, approved art, unrelated mods and tooling were excluded. Only completed auditor-owned local exports were cleaned; no other role's active output/cache/checkout was touched. The existing auditor task now carries this retention rule, with its other settings preserved. No older binary was saved as new.

## Ten equally weighted categories

| Category | /10 | Status | Pinned implementation and verified limits |
|---|---:|---|---|
| Terrain | 5 | Carried | [Source L34](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java#L34); [evidence](independent-core.txt). V1–V7 registration, current V6/V7/density and native replay pass. Registered saved V8 integration remains unaccepted. |
| Biomes | 4 | Carried | [Source L76](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76); [evidence](default-native-result.json). Threshold palette and replay pass; weighted ecotone surface/vegetation acceptance is missing. |
| Hydrology/caves | 6 | Carried | [Source L451](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L451); [evidence](allowance-overflow-characterization.txt). Source-column deadline and finite-account checks pass. Recharge starvation and extreme allowance overflow remain open; repairs staged. |
| Features/structures | 4 | Carried | [Source L115](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115); [evidence](default-deposit-0.json). Direct/native deposit checks pass; general structure spacing/exclusion remains incomplete. |
| Performance/threading | 3 | Carried | [Source L45](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45); [evidence](allocation-probe.json). Measured build-local scratch allocation improvement; no whole-chunk/frame/tick or named-hardware acceptance. |
| Determinism/stability | 7 | Carried | [Source L260](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260); [evidence](qualification-result.json). Ten accepted fresh packaged sessions, saved parcels/profile and replay pass; broad soak/cross-machine proof is missing. |
| Architecture | 6 | Carried | [Source L22](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22); [evidence](packaged-regressions-result.json). Core lifecycle/module boundaries pass; generator coupling and global water ownership remain open. |
| Extensibility | 4 | Carried | [Source L54](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54); [evidence](optin-native-result.json). Saved deposit/options validation passes; full V8 saved graph authoring and public lifecycle acceptance are missing. |
| Tests/docs | 7 | Carried | [Source L12](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12); [evidence](extra-gates.json). Full build/core/package/native matrix and scratch controls pass; platform/soak/performance corpus remains incomplete. |
| Polish/tooling | 4 | Carried | [Source L17](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/tools/server_smoke.py#L17); [evidence](post-native-archive.json). Strict save/shutdown controls, complete Themes/notices and approved art pass; client visual/authoring/platform acceptance remains open. |

## Actual source changes

Compared with prior audited 6bd85c6, production changes are confined to LodMesher: one build-local Builder reuses scratch arrays and the largest mask across three LOD levels. Flush copies returned arrays; finish copies the result list before clearing references. There is no global, ThreadLocal or cross-job scratch reuse. New private LodScratchChecks covers retained snapshots, 25 subsequent builds, 64 parallel jobs, height 4096, interruption, ten frozen geometry signatures and 18-part bounds. Both direct and extracted-product checks pass. The corpus preserves geometry, color, normals, indices, heights and bounds.

The promoted dev30 source-column deadline/wrap fix is unchanged. New regular-recharge reservation and streamAllowance overflow candidates remain staged and are **excluded**. No staged R57/R60/R61/R62 promotion. Frozen V1–V7 geography, saved profiles, finite water/constituents, owner edits, approved art, modular graphics/LOD/Vulkan and Deep Primal identity remain intact in the tested corpus.

Inspected pinned completed [renderer scratch evidence](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/docs/renderer-jams/20261026-lod-scratch/REVIEW.md) and [staged hydrology evidence](https://github.com/Shnewbs/Deep-Primal/blob/b198804f19db960fba6efc52a76b18c603d50601/docs/validation/engine-continuation/stream-allowance-overflow-2026-10-26/README.md).

## Fresh checks

- **Full Java 25.0.4.1 / Gradle 9.2.1 build passed:** all 35 actionable tasks executed with `--rerun-tasks`, 340.7 seconds. Core/cache/material/module gates and visualValidation compilation passed; no assertion/source-set removal.
- Independent `javac -g`: 74 production + 62 fixtures = 136 sources. Full `-Xverify:all` CoreChecks passed. Python 12 state, 16 shutdown and 12 RCON tests passed normally and under `-O` / `-OO`.
- Lifecycle, waterfall, split, LOD bounds, scratch, reduction and rolling timing fixtures passed against the actual extracted product core. Code-source checks prevent fixture classes from shadowing production.
- Seven outer/nested archives passed CRC and duplicate checks. **590** compiled product/package identities, **144** independent core identities and **220** private-fixture exclusions passed. Two Windows AMD64 PE DLLs; no Linux/macOS Vulkan payload. Complete 2,508 Themes resources, 43 palettes, 11 approved ground images, 148 generated soils and exact Vulkan LICENSE/COPYING/UPSTREAM notices passed.
- Ten accepted packaged Linux dedicated-server sessions passed: precipitation fresh/reopen; default channel 2 fresh/reopen; opt-in channel 5 fresh/reopen with creation toggle disabled before reopen; aquifer fresh/reopen/verify across two real restarts; receiver atomic-capacity checks. Nonzero precipitation demand, seven receiver rejections/four exact transfers, finite quantities/constituents, explicit markers, RCON save-flush acknowledgment, error precedence, ordered shutdown and exit 0 were required.
- Final post-native verification checked all six modules selected from the exported outer JAR in every accepted native log, revalidated nested CRCs/PE payloads and producer/export SHA equality. All 880 restored input identities were checked again after testing. Five pinned retained tool archives/private compatibility-agent hashes passed; tools and private fixtures remain outside the mod payload.

## Scoped allocation measurement

Fresh auditor compilation of exact 6bd85c6 LodMesher, with only class/constructor renaming, supplied a private baseline. Java 25, `-Xmx512m`, ThreadMXBean calling-thread allocated bytes, five fixed scenes, 30 warm-ups per variant/fixture, then 20 alternating measured builds per variant/fixture. Immutable input creation was excluded. Current class identity matches the packaged product.

Before: 446464 / 573160 / 509904 / 540992 / 790264 bytes per build. After: 226912 / 353608 / 290352 / 321440 / 552284. This reproduces **about 30–49% less temporary allocation** in these components. It does not measure resident/peak RAM, VRAM, GPU upload, FPS or complete frame/chunk/tick speed. Returned mesh/GPU bytes are unchanged. No controlled latency benchmark was rerun; the author's component medians/tails were mixed. No performance-score increase is claimed.

## Defects and next priorities

Exact current streamAllowance source, compiled privately against a BigInteger oracle, still returns **−51 instead of 1024** for quota 1024, start MIN+1, now MAX. This expected-negative characterization is retained as an open extreme-clock arithmetic defect, not evidence of ordinary-world water loss. Its repair remains staged.

The prior deterministic scheduling reproduction found zero regular recharge visits versus 500 with the staged 500 µs reservation. That complete scheduling matrix was not rerun here. A single expensive callback/survey/sort can still exceed admission reservation; broad fairness and loaded/unloaded water ownership remain unaccepted.

Next: qualify the combined recharge-reservation/overflow repair through full current finite-source/quota/weather/save/reopen/shutdown checks before promotion; prove loaded/unloaded single-owner accounts, edits and catch-up; register saved V8 integration; complete ecotones/features and natural cliff/LOD temporal scenes; obtain controlled named-hardware performance and soak evidence.

## Audit setup failures and isolation

Three initial build setup failures are retained separately from the accepted full build: memory-backed Themes processResources exhausted space; two abandoned default-path attempts encountered a dangling output link. They produced no delivered JAR. Final accepted compilation used a fresh ordinary Themes directory configured through a private Gradle init. Every build task was forced again.

The first native matrix passed functional checks but the post-native gate correctly **rejected** it: redirected Themes output escaped the source build.gradle default-path filter, so the producer Themes JAR was loaded instead of the nested copy. The private launch init now excludes the actual configured Themes build directory. The entire ten-session matrix was rerun, with the strict all-module selection gate unchanged. Earlier functional sessions are not counted as accepted packaged sessions. A stale extraction-directory resume error is also retained; no product source/assertion was changed to bypass it.

Unique nonce checkout, independent cache/daemon/output and lease/receipt. Only an older completed auditor cache was moved. Preflight found renderer timing completed and builder checkpointed/nonexclusive; the later renderer discovery claim explicitly reserved no performance window. No builder state, lease, process, checkout or cache edit/reset/pause/kill; audit progress creates no builder stop gate.

## Host and unrun acceptance

Linux AMD EPYC 9V74 virtual host, 8 GiB process budget; Gradle 768 MiB / one worker, server 1024 MiB, seed 20261020, view/simulation distance 2. No /dev/dri device or Windows executor.

**Not run:** Windows/AMD8060S execution; actual OpenGL/Vulkan client pixels/reload/resize/reset/culling/telemetry; named-device frame/chunk/tick/IO/VRAM latency and tails; long travel/edit/unload/multiplayer soak; cross-machine replay; complete world-water ledger; registered V8 saved topology/materialization. Linux smoke is not Windows acceptance. Unused FSR/DLSS libraries/providers/frame generation earn no feature credit.

Main re-read before publication: `d6750d3620a949031d4e8ab0d0cd11af4a27b742`. The JAR remains pinned to `b198804f19db960fba6efc52a76b18c603d50601`; later builder/renderer changes are not represented by it. Main advanced during the audit with DeliveredChunkScan plus LodClient/VoxelLodRenderer integration; those newer changes are excluded from this binary and receive no credit here. Publication is restricted to this audit's unique report/evidence path through a latest-parent/ref guard. The auditor remains enabled: a fresh complete mean **strictly greater than 8.5** has not been demonstrated.
