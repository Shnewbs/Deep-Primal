# Deep Primal Dev78 independent audit — 2026-10-29 12:31 UTC

Pinned immutable main: `bee0509a258c9b7b9a3c735a8f998f903b6423b4` (version `0.0.1-alpha.29-dev.78-observed-receivers.1`). Builder's Dev78 source change: `d2606e26e11c0ed936ddf1b93c8424f7f03cb503`; later renderer and coordination docs are separate. No source edits by auditor.

## Independent execution

Owner Library Temurin 25.0.5+8 restored and verified. Strict `javac --release 25 -Xlint:all -Werror` and `java -ea -Xverify:all` compiled/exercised exact committed [bridge](https://github.com/Shnewbs/Deep-Primal/blob/bee0509a258c9b7b9a3c735a8f998f903b6423b4/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java#L114-L216) (blob `de10020346d41a1b9a2091f8f0ec8a45885a55d3`) and [fixture](https://github.com/Shnewbs/Deep-Primal/blob/bee0509a258c9b7b9a3c735a8f998f903b6423b4/tools/tests/WatershedRegionPortBridgeChecks.java#L86-L145) (blob `ac9782cdba99ec3590f44a277d66c9d68342b6aa`). Git blob identities independently matched. Two committed fixture executions **3,919 assertions each** and two independent runs **6 assertions each**, total **7,850 assertions passed**. Three separately compiled source mutations rejected and older Dev77 source failed the new Dev78 regression. Output: 1,791 ports, 1,792 links, 257 shared coordinates, 1,536 unknown frontiers. Tests use *actual production bridge/topology but WatershedRegion/TerrainEngine API doubles*, not full core/native gameplay.

## Audit finding

A synthetic boundary observation with two equal-stage, adjacent perimeter parents pointing at each other is admitted by `WatershedRegionPortBridge.assemble`. Parent geometry and stage checks do **not** prove acyclic authoritative parent order; the directed 2-cycle collapses into one undirected perimeter link. The real `WatershedRegion.verify()` has a route-order assertion but `boundarySnapshot()` does not call it. This is malformed-input admission in an isolated source test, **not** a reproduced normal generated-world failure. Require verified parent-order provenance/cycle checks before native activation.

Dev78 contributes correctly guarded adjacent/perimeter diagonal links and rejects forged parent offsets, ocean-root violations and reverse stage. Still *explicit-call-only*, zero physical runoff, no global interior-to-port routing, no live quantity or saved-world acceptance.

## Rubric / unfinished gates

**Carried full baseline, NOT fresh regrade:** terrain 5; biomes 4; hydrology/caves 6; features 4; performance/threading 3; determinism 7; architecture 6; extensibility 4; tests/docs 7; polish/tooling 4. **50/100 = 5.0/10, Tier 2**. Target must be strictly >8.5/10.

Unrun: complete Git checkout (CLI DNS failure), full Java25 Gradle9.2.1/NeoForge, full production WatershedRegion compile, native V1–V7/profile/edit/save/reopen, JAR/nested CRC and class identity, water/salt/sediment lifecycle, Windows AMD8060S Vulkan/OpenGL and p50/p95/p99 GPU. **No JAR** under Oct 27 owner full-build override; no Releases, CurseForge or JAR retention mutation. Builder and renderer untouched, independent output only.

Evidence ZIP `DeepPrimal_Dev78_IndependentAudit_20261029T1231_bee0509a_Java25Evidence.zip`: **21,506 bytes**, SHA256 `4234e5721e7482bfe008f2a0adc8fcad07ab31cb2d6372269b8314bb5da9a115`, CRC and manifest-member SHA256 pass, saved in Library `/Deep Primal/Independent Audits/`. Priorities: authoritative interior-to-port routes; once-only discharge; native saved geography and finite-water conservation; measured backend performance.
