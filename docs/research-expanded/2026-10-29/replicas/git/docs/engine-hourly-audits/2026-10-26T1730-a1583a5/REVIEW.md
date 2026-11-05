# Expanded replica — docs/engine-hourly-audits/2026-10-26T1730-a1583a5/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T1730-a1583a5/REVIEW.md |
| Preserved source SHA-256 | `7b151f7c1fe9f8b7fc9e3c29d60c4e2de5b10120623cd5cfd102ff807d79c25c` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent development audit — a1583a5”. Its original opening states: “Download fresh Windows development testing JAR.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Turn the recorded finite-water scheduling behavior into a progress contract under contention, rather than equating an admission clock with a hard execution deadline.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Specify resumable cursor ownership, fair service among source/recharge/transport queues, and the durable state retained at budget exhaustion.
- Track admitted work, actual elapsed cost, longest indivisible callback, queue age, and deferred debt separately.

## Research expansion — acceptance checks

- Saturate source work while regular recharge is enabled and verify bounded starvation without creating or losing water.
- Interrupt every cursor boundary, unload neighboring chunks, and resume after reopen; compare visited cells and conservation with an uninterrupted oracle.

## Research expansion — source anchors

- Independent development audit — a1583a5
- Current implemented changes and inspected evidence
- Remaining defects and next priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `build-output.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/build-output.txt) |
| `default-deposit-0.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/default-deposit-0.json) |
| `default-native-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/default-native-result.json) |
| `extra-gates.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/extra-gates.json) |
| `independent-core.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/independent-core.txt) |
| `optin-native-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/optin-native-result.json) |
| `packaged-regressions-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/packaged-regressions-result.json) |
| `post-native-archive.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/post-native-archive.json) |
| `qualification-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/qualification-result.json) |
| `retention.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1730-a1583a5/retention.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent development audit — a1583a5

[Download fresh Windows development testing JAR](https://chatgpt.com/library?file_id=libfile_4c1faa66c5d88191a5fd74d6019a690d&account_id=personal).

**50/100, average 5.0/10, Tier 2/5. All ten scores carried forward.** Fresh scoped repair/build evidence below is not a fresh complete regrade or a demonstrated mean above 8.5. Unused libraries and staged prototypes receive no gameplay credit.

Source pin: `a1583a508b0d67130d387bc018defd5758acd496`. Version `0.0.1-alpha.29-dev.31-audit-20261026T1730-a1583a5`. Size **125,296,766 bytes**, SHA256 `4661080982896a19bca54751c33b78fbbe07a4b59b7bbafa8d9b64e039630229`. Fresh unique Windows Vulkan AMD64 development bundle with OpenGL fallback, complete Themes 0.1.0-alpha.8 and notices. Final producer/export bytes verified after producer completion, all native sessions and saving. No stable label, GitHub Release or CurseForge publication.

## Ten equal categories

| Category | /10 | Status | Pinned source and current evidence/limits |
|---|---:|---|---|
| Terrain | 5 | Carried | [Source L34](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java#L34); [evidence](independent-core.txt). V1–V7 registration, current V6/V7/density and native replay pass. Registered saved V8 integration remains unaccepted. |
| Biomes | 4 | Carried | [Source L76](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76); [evidence](default-native-result.json). Threshold palette and replay pass; weighted ecotone surface/vegetation acceptance is missing. |
| Hydrology/caves | 6 | Carried | [Source L451](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L451); [evidence](qualification-result.json). Promoted 500µs recharge admission reservation and ordered allowance overflow saturation; four fresh loaded-runtime BigInteger oracles pass. Full world-ledger/cave/visual acceptance remains open. |
| Features/structures | 4 | Carried | [Source L115](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115); [evidence](default-deposit-0.json). Direct/native deposit checks pass; general structure spacing/exclusion remains incomplete. |
| Performance/threading | 3 | Carried | [Source L45](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45); [evidence](extra-gates.json). Bounded LOD discovery is implemented; no new whole-chunk/frame/tick or named-hardware latency acceptance. Prior scratch allocation result is carried, not rerun. |
| Determinism/stability | 7 | Carried | [Source L260](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260); [evidence](qualification-result.json). Ten accepted fresh packaged sessions, saved parcels/profile and replay pass; broad soak/cross-machine proof is missing. |
| Architecture | 6 | Carried | [Source L22](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22); [evidence](packaged-regressions-result.json). Core lifecycle/module boundaries pass; generator coupling and global water ownership remain open. |
| Extensibility | 4 | Carried | [Source L54](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54); [evidence](optin-native-result.json). Saved deposit/options validation passes; full V8 saved graph authoring and public lifecycle acceptance are missing. |
| Tests/docs | 7 | Carried | [Source L12](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12); [evidence](extra-gates.json). Full current build/core/package/native matrix plus direct/extracted-product discovery controls pass; platform/soak/performance coverage remains incomplete. |
| Polish/tooling | 4 | Carried | [Source L17](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/tools/server_smoke.py#L17); [evidence](post-native-archive.json). Strict save/shutdown controls, complete Themes/notices and approved art pass; client visual/authoring/platform acceptance remains open. |

## Current implemented changes and inspected evidence

Compared with prior auditor pin b198804, this committed snapshot includes:
- [HydrologyRuntime L442](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) reserves 500 microseconds of the existing 2ms admission interval for regular recharge/accounting when regular columns are enabled. This fixes the prior demonstrated source-work starvation scenario; it does not guarantee a hard deadline for an individual callback, survey or sort.
- [Allowance L419](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L419): ordered elapsed-tick subtraction overflow saturates to a completed interval. The prior quota1024/startMIN+1/nowMAX result −51 is corrected to1024. Four fresh actual loaded-runtime server sessions each pass 1,445 BigInteger-oracle cases; rewind and sentinel behavior are preserved.
- [DeliveredChunkScan L15](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L15) plus LodClient/VoxelLodRenderer integration replace the attach-time radius32 clipping with bounded square-ring traversal, up to128 coordinates per active client tick, pressure pause/retry and existing background-focus policy. At radius125, 63,001 coordinates require at least493 batches without backpressure. Completion means enumeration, not uploaded/visible readiness. Rediscovery uses newer revisions to reject stale persisted work.
- New private discovery and allowance fixtures plus dev31 version metadata. No terrain, profile, art or water-quantity formula changes. Existing dev30 source-column deadlines and previous scratch ownership/reduction are preserved.

Inspected pinned [Builder qualification](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/docs/validation/engine-continuation/hydrology-qualified-dev31-2026-10-26/README.md) and [renderer self-audit](https://github.com/Shnewbs/Deep-Primal/blob/a1583a508b0d67130d387bc018defd5758acd496/docs/renderer-jams/20261026-delivered-discovery/REVIEW.md). Their claims are distinguished from this run's fresh executable evidence. Builder's reported successful retry was 5 executed/30 up-to-date after a failed forced attempt; this auditor independently requests every build task with --rerun-tasks. The renderer's six-category3.67 self-grade is a separate rubric, not a whole-engine regrade.

No staged R57/R60/R61/R62 files were promoted by the auditor. The hydrology fixes are already accepted production in the pinned commit. Frozen V1–V7, saved profiles, finite quantities/constituents, player edits, Deep Primal identity and modular graphics/LOD/Vulkan are preserved in the checked corpus.

## Fresh checks

Full Java25.0.4.1 / Gradle9.2.1 build and visualValidation compilation passed in 369.0s with forced tasks. Exact output is [build-output.txt](build-output.txt). Independent compilation: 75 production + 63 fixture sources, complete -Xverify:all CoreChecks pass. Python12state,16shutdown and12RCON controls pass normally/-O/-OO. Actual extracted-product lifecycle/waterfall/split/LOD bounds and direct/packaged scratch, reduction, timing and discovery checks pass. Tests use product-first classpaths and actual code-source controls.

Seven outer/nested archives pass CRC and duplicate checks; 592 compiled/package product classes and 146 independently compiled core identities match; 222 private fixtures excluded. Two Windows AMD64PE DLLs, no Linux/macOS Vulkan payload, OpenGL fallback, exact Vulkan notices,2,508Themes resources,43palettes,11approved ground textures and148generated soils verified. Tooling/compatibility agent/private fixtures are outside payload. All 885 restored input hashes rechecked after native tests; pinned tool archives verified with the documented restoration helper in verify-only mode.

Ten fresh packaged Linux dedicated-server sessions pass: precipitation fresh/reopen, default fresh/reopen, opt-in fresh/reopen with saved channel5 retained after creation-toggle disable, aquifer fresh/reopen/verify across two real restarts, receiver capacity. Explicit markers, exact finite accounting/replay, nonzero precipitation demand, atomic rejection/transfer controls, actual selected root and all six nested modules, acknowledged save-all flush, error precedence, ordered shutdown and exit0 are required. Four profile sessions additionally exercise the new allowance oracle against the actual loaded runtime.

The prior scratch component allocation improvement (~30–49%) is historical carried evidence, **not rerun here**. No fresh timing, allocation, FPS, VRAM or peak-RAM improvement is claimed. The prior complete deterministic recharge scheduling matrix is also carried Builder evidence, not independently rerun in this audit.

## Remaining defects and next priorities

The previously reported extreme allowance overflow and demonstrated recharge admission starvation repair are included and freshly package-checked; do not repeat them as still-staged defects. Remaining: one callback/survey/sort can exceed admission budget; full loaded/unloaded single-owner water/chemistry ledger, catch-up, player edits/restart and long storm/drought behavior are unaccepted. Visible water flicker/seamless cliff flow is not resolved by a server fixture. LOD enumeration has no fresh live camera/travel/cache-race/handoff acceptance. V8 registered saved density/topology, weighted ecotones, general structures/features and broad cave families remain unfinished or unaccepted.

Next prioritize single-owner loaded/unloaded accounts and bounded catch-up/callback work, registered saved V8 compatibility, connected temporal water/LOD client scenes and named-hardware load-ready/frame/tick/IO/VRAM distributions plus soak. These breadth gaps prevent a higher complete grade.

## Isolation, restoration and omissions

Independent checkout/output/cache/daemon/lease. Builder's active reservation was functional-only, exclusive_benchmark=false; no timed run was overlapped. No builder checkout/state/lease/cache/process edit/reset/pause/kill or stop gate. Full-tree API reads failed with transport-closed errors; reconstruction uses the last Git-verified retained input receipt plus the inspected complete eight-commit delta and exact freshly fetched changed-file blobs, not uncommitted sources. Required current policies and completed reports were read. Storage was tight; only a completed auditor-owned local export duplicate with an existing verified saved copy was removed. The unique export is a hard link to this audit producer after its completion, avoiding a second 125MB disk copy; final SHA/CRC gates still cover its bytes after every native session and saving. The first native preparation failed before server execution because the private server-unused asset-properties file was absent; that setup file was restored and the complete native matrix restarted. Failed preparation diagnostics are retained and not counted as a native pass. No other role's active outputs were cleaned.

HostLinux AMD EPYC9V74,8GiB process budget; Gradle768MiB/oneworker,server1024MiB,seed20261020,view/simulation2. No Windows executor or /dev/dri device. **Not run:** Windows/AMD8060S execution; real OpenGL/Vulkan client pixels/reload/resize/reset/culling; live water/cliff temporal continuity and LOD discovery readiness; named-device performance distributions; long travel/edit/unload/multiplayer soak; cross-machine replay; complete water ledger and V8 materialization. Linux smoke is not Windows acceptance. Actual FSR/DLSS providers/frame generation remain unimplemented and earn no credit.

## Retention and publication

Keep only the newest three Deep Primal user-facing JARs by actual creation time, across Builder/renderer/auditor deliveries. Save only this newly qualified current build; trash older items by stable ID after successful save. Preserve source/evidence archives, approved art, dependencies/tools and unrelated mods. See [retention receipt](retention.json).

Main re-read immediately before report publication: `d235daac17c0e2f7dd7cc20e6ee181491db9ed4f`. JAR source remains `a1583a508b0d67130d387bc018defd5758acd496`; newer main changes are excluded from it. The later commits add dev31 delivery receipts and staged source-prelude admission guards only; production code has not changed. The staged second post-cleanup deadline guard is excluded and receives no feature credit. Its reported controlled-clock cases (baseline105 expired sorts, revised0) are Builder evidence inspected after the pin, not fresh auditor execution. Cleanup and any admitted sort remain unbounded. Guarded latest-parent/ref update touches only this audit's unique evidence/report path. Keep the auditor enabled: a fresh complete average strictly greater than8.5 has not been demonstrated. Finite runs do not imply continuous execution.


<!-- END PRESERVED SOURCE -->
