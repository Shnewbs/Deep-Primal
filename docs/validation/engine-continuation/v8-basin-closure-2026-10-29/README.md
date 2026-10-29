# Dev70 — opt-in bounded basin exclusion after dev69 independent audit

Source version: `0.0.1-alpha.29-dev.70-basin-closure.1`. Starting main: `45c777691a9d0934012eb114a57ab8501f0ec827`.
Independent auditor `docs/engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md` pinned `9a1d92a44d7d3864d35e0c3ac288158a37c33015`. Its Java25 isolated counterexample showed **dev69 routing v2** erroneously certifies a closed inland basin with height 20 and continentalness -0.4 as ocean on the positive 8192 watershed seam. This was not a native Minecraft world.

## New source behavior

- `V8OceanConnectivity` keeps the older default v2 algorithm unchanged. Explicit **v3** traverses the connected sampled wet component, probing exactly adjacent lattice points at inter-region gateways. A completely explored deep inland depression sealed by sampled dry land is rejected as ocean, even with a marine-like continentalness. Query order cannot borrow another corner's regional search budget; the bounded resident cache and work caps remain.
- An unresolved wet continuation beyond the **8-region/96-component cap** plus a deep marine core remains only a **heuristic** ocean admission. It is NOT evidence of a globally reachable ocean: exceptionally large enclosed lakes can still be false positives. This is a scoped negative-case repair, **not finished authoritative cross-region routed priority flood, outlet or discharge transport**. Do not activate native worldgen on this evidence.
- `V8RoutedWaterSource.authorBasinGuarded(revision4)` explicitly records the policy in an r5 profile using a new domain-separated SHA-256 fingerprint. `open` restores exactly v1, v2 or opt-in v3. Existing r5 v1/v2 serialized fields, graphs and replay remain frozen, as do V1–V7 geography, player edits, water quantities and renderer sources.
- The water-domain code still only classifies prospective wet columns for surface-material exclusion; it does not alter fluid volume, water height, chemistry or any block.

## Reproducible checks

Java `openjdk 21.0.12.1`, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` using **isolated Minecraft/Mojang API doubles**. All **19/19** focused suites passed with **21,012,678** fixture checks; the changed class suite passed **190** checks, including both signed 8192 seams, the independent deep-basin counterexample, v2 frozen output, v3 outward-continuing sampled corridors, a dry ridge, saved r5 profile encode/decode and seed/policy mismatch rejection. Two separately compiled broken v3 controls were correctly rejected (deep-core-only shortcut and dry gateway treated as wet).

Changed local Git blobs in [results.json](results.json) match blobs created for the commit. The portable source evidence ZIP contains real current source for the scoped V8 components, isolation doubles, all focused tests, a replay script, logs, and a compiled-mutant script. A replay is not native Minecraft testing.

## Gates that remain open

Real `WatershedRegion` regional outlet topology; authoritative global inland/ocean identity and cross-region water budgets; native Mojang saved profile; exact new/old chunk boundaries and world edits; finite-water quantities and constituents, loaded/unloaded/restart and multiplayer soak; real Vulkan/OpenGL waterfalls, 60Hz/frame and hardware memory/latency distributions; Java25/Gradle/NeoForge full build; source/package/JAR identity. These were not executed. The original ten-category grade remains **50/100 (5.0/10)**; no regrade or playable JAR.

The owner owns full builds/JAR publication. The complete pinned checkout/root validator was unavailable; `docs/ENGINE-WORK-STATE.json` was deliberately left unchanged. Scoped terrain work state records the temporary ownership, actual test scope and final lease release.
