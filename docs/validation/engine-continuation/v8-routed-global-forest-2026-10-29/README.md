# Dev82 — combined routed-receiver forest admission

Independent audit consumed: [2026-10-29T1532-b6448f52](../../../engine-hourly-audits/2026-10-29T1532-b6448f52/REVIEW.md), pinned `b6448f52fca405089f5dd73f611ededd2dbd9547`, identifying opt-in V8 routing without globally certified ocean or physical discharge. Current main source based on claimed Dev81 bridge blob `cd14393174af306bf3a0ca3c4cf7bb26f3b4a937`.

## Code change

The production `WatershedRegionPortBridge.assembleRouted` now validates the **combined directed receiver forest**, following direct observed boundary parents and collapsed interior route endpoints before they are admitted as undirected spillway links. A perimeter-only DAG cannot detect a cycle such as A → interior → B → interior → A. The new bounded O(ports) check rejects it. Missing interior data and unobserved neighbors remain **unresolved**, not inferred ocean outlets. It does not create or transfer physical water. The old `assemble()` path, saved profiles, native V8 registration, V1–V7 geography, owner assets and renderer-owned files remain untouched.

## Actual isolated validation

Linux OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`, API-double watershed. Bridge runner passed twice in normal mode and twice under Python `-OO`; **3,935 assertions per positive process**, with **14 compiled broken implementations rejected per mode**. Standalone interior route suite: two positives, three broken controls rejected. Outlet topology component suite: isolated positive process exited 0 and three broken controls nonzero. Exact prior Dev81 bridge source fails the newly introduced reciprocal collapsed-route regression (negative control). The local portable evidence archive includes source/test snapshots, normal and optimized receipts, and old-source negative failure log.

Not run: full production WatershedRegion graph compilation, Java25/Gradle9.2.1, NeoForge/Minecraft world creation or reopen, GPU Vulkan/OpenGL, physical water/salt/sediment conservation, multi-seed native terrain blending or bounded production performance. This is an opt-in source safety repair, **not** globally certified drainage, finished terrain blending, a new playable JAR or an independent regrade. Last full accepted score remains **50/100 (5.0/10)**. Owner performs full builds and JAR publication.

Next: authoritative V8 outlet identity and once-only physical transfer/constituent accounting; then native ecotone vegetation and relief blending, old/new border acceptance and water unload/restart.
