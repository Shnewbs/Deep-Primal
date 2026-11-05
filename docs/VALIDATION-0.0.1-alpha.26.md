# Alpha 26 validation — environmental water and terrain V4

Date: 2026-10-24 UTC. This is an experimental owner preview on the consolidated main branch. It adds finite environmental recovery and a versioned terrain extension; it does not close the complete engine, terrain-mechanics or renderer-performance roadmap. See [implementation scope](ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md).

## Delivered artifact

- `DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-windows.jar`
- Minecraft 26.1.2 / NeoForge 26.1.2.122; Windows Vulkan bundle, retaining Themes alpha.8 and optional engine modules.
- 124,893,144 bytes; SHA-256 `f282128baa482759571f5478c25d95f494a0615491181b60132f8affb91c4d5d`.
- Root plus six nested archives pass CRC checks. All nested artifacts match their built module JARs; 114 root classes and the recorded module classes match compiled outputs. Private validation classes and temporary trace code are absent. See [archive audit](validation/alpha26/archive-audit.json).

## Completed checks

| Check | Result and scope |
| --- | --- |
| Full Windows bundle build | All 31 tasks and required core/cache/module-boundary/package checks pass. Java compilation was forced without incremental compilation. |
| Exact environmental accounting | 200,000 transfer cases; volume, salt and sediment conservation, blocked destinations, large account arithmetic, evaporation, activation hysteresis and no offline catch-up. Seeded groundwater alone cannot activate a new dry-land outlet. |
| V4 terrain | Deterministic replay, 237 bank checks, 1,343 ore samples and eight rock suites. Existing watershed suite also passes 5,846,893 centerline samples and 538,915 containment cases. |
| Packaged dedicated server | Shared aquifer outlets, blocked/head-limited discharge, infiltration, measured buckets and advection, accounted fixed-stage ocean, finite inland water, actual weather debit, codec round-trip, once-only initialization and missing-chunk checks pass. Nine loaded chunks took 316.05 ms in this fixture. |
| Full process restart | Saved V4 generator, groundwater, dynamic-outlet age, water composition and player edits survive restart; the integration fixture retains seven units and flow direction three. |
| Long communicating passage | 120 ticks, 4,190 units before/after, changing heads and retained frontier pass. Maximum search slice 128 visits. The fixture deliberately pins 18 chunks; this is not a no-loaded-chunks test or a latency benchmark. |
| Legacy geography | 27,000 V1–V3 sample/column/biome values match the delivered alpha.25 core exactly: digest `54accc2ee4b90cc01ad52107a96a8234933621f837ddfba56db242d8d1e95c89`. A real saved alpha.25 V3 world reopens in alpha.26 with its saved generator and edit intact. |
| Native watershed fixture | Five sites, 12,213 water cells, 2,976 dry columns, 80 material columns, 187 sealed banks, four trunks, 55 connected reaches and 33 estuaries pass. This earlier run has the same V4 geographic implementation; final hydrology/pressure changes are covered separately above. |
| Client scenes | Coast, headwater, river, lake, outlet and ridge complete on Mesa lavapipe Vulkan in 103.85 s. No crash reports or Vulkan validation errors; explicit shutdown-continuation marker passes. Hydrology is enabled with default rates. |

The server executed the packaged Windows root JAR and the separately built core module; archive identity checks establish that the tested core equals the embedded core. The client used compiled production classes and Linux Vulkan natives. This is not Windows runtime or owner-GPU validation. The container has no audio output device; the client disables audio after the expected OpenAL startup error. The shutdown fixture intentionally rejects one completion, then verifies the following completion succeeds; that injected error is not counted as an unhandled game failure.

## Visible behavior and limits

The final fresh headwater scene has dry hillsides and water following its generated channels. The final saved audit over chunk X=100..109, Z=-39..-31 finds 7,289 water cells: 7,126 in generated wet columns and 163 outside them, containing 325 sixteenth-block units. Local finite spill remains possible. This short clear-weather scene is not a proof of zero overtopping, long-duration storm stability, permanent streams or entire-world hydrology. Rain/drought behavior is exercised in the account and native weather fixtures.

Early client runs in a reused destination unexpectedly reopened older player/world state and showed widespread hillside patches. A direct dedicated-server load of the source chunk was clean; unique client destinations restored a fresh player/world and clean initial terrain. Those reused-world runs are excluded from acceptance. The external writer/restoration cause is not established. Earlier incremental Java compilation also produced stale output; final builds force non-incremental Java compilation and verify archive/class identities. No speculative filesystem workaround is shipped.

LOD is disabled for these scene captures. Existing LOD coverage, water publication/retirement latency, full scene performance, Windows native execution and owner GPU/FPS acceptance remain open. The environmental loop has a bounded column count and a nominal 2 ms admission window, not a proven hard wall-clock bound. No renderer-performance completion is inferred here.

Expanded biome/material catalogs, authored deposit controls, geology-linked cave networks and through-mountain rivers, glaciers, dunes, support-dependent cave-ins, natural obstruction generation, surface evaporation and unloaded-border coarse hydrology remain on the terrain roadmap. V1/V2/V3 geography is frozen; existing terrain and player edits are not regenerated.

Raw build/server/client records, tested configuration, source and screenshot hashes, package audit and hardware description are in [validation/alpha26](validation/alpha26). The pre-existing R44 evidence and its acceptance caveats remain preserved.
