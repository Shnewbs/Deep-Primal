# Alpha 11 — Rivers, streams and coastal beaches

Target: Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Base: alpha 10 commit `ebd3fa6235ae95f204f2fe3409f862c5ffefe74c`. This remains a terrain prototype.

## Behavior

New worlds default to `riversAndCoasts = true` in `config/deepprimal-worldgen.toml`. Wider lowland river corridors follow the seeded native ridge field; a separate smooth branch field adds narrower tributary channels. Both share a bed around Y=89 and surface water through Y=95. Channel carving never raises an existing lower seabed across a river mouth. Coastal grading creates shallow shelves and dry beach strips; beach biomes receive sand over sandstone. Inland channel beds use gravel, while steep faces and stony shores retain rock precedence.

The standard height remains Y=-128..639. `extraUndergroundDepth = true` selects -384..639 before new-world creation. The new noise-setting IDs are `deepprimal:waterways_v1` and `deepprimal:waterways_v1_deep`. Original terrain/preset/pack resources remain byte-for-byte intact. Disabling `riversAndCoasts` before creation selects the legacy terrain profile. Saved dimensions retain their serialized settings and height; existing terrain is not regenerated.

Only the selected creation pack loads automatically. All four pack IDs remain registered for saved-world loading. This also fixes the former pack-source behavior that could enable unselected height profiles.

## Code and runtime cost

`WaterwayShape` is a stateless platform-neutral core function. `WaterwayHeight` evaluates the seeded inputs once per call, wrapped by the existing native flat cache and bounded 5×5 chunk grid. Native terrain, preliminary surface height, biome depth and horizon sampling use the same field. There is no per-tick river simulation, extra terrain worker pool, global drainage map, or unbounded river cache.

Only the new profiles set the optional `ColumnSurface.surface_water` flag. A constructor-selected `SurfaceWaterAquifer` decorator supplies water above the cached preliminary surface inside the sixteen-block band below sea level. Solid density, Y>=96 and Y<80 delegate to Minecraft's aquifer. Legacy profiles keep the original aquifer instance. This fixes carved inland streams being left dry by native randomized aquifer fluid levels without replacing deeper cave-fluid rules.

Worldgen stays in the root platform adapter plus the shared core. Vulkan, common performance and graphics settings retain their independent modules. JarJar's generated output is cleared when its task runs, preventing old module versions from inflating subsequent installers.

## Validation

- `clean build compileVisualValidationJava --offline`: passed, including module boundaries, 100,000 randomized waterway cases, 200,000 differential surface cases, 3,000 queue cases, 34,300 grid coordinates, 1,540 horizon interpolation/seam cases and the existing cache checks.
- Waterway checks cover deterministic finite output, continuous shoulders, ridge symmetry, narrow tributary shaping, no mouth lip, dry coastal shelf, rough cliff preservation and bounds.
- Resource generation is idempotent. All sixteen original terrain/preset/pack files match alpha 10 exactly, in source and installer.
- Dedicated native generation scans 66,049 points per seed across -4096..4096 in both axes at 32-block spacing. It checks the shared terrain/biome-depth field and then inspects actual SURFACE-stage blocks in 25×25 regions, including negative coordinates and chunk boundaries.
- Native channel checks require actual water and wet columns, plus gravel beds for rivers/streams. Beach checks require dry sand and sandstone. Adapter assertions check native activation, deep/solid/above-sea delegation and surface fill without scheduled fluid updates.
- Legacy compatibility uses a world created with the frozen legacy profile, saves it, enables the new-world toggle, and reopens it. It verifies the saved legacy density graph and native aquifer in newly generated chunks. This is a legacy-profile save test, not a claim that an uploaded alpha 10 save was tested.

| Profile / seed | Sampled river points | Sampled stream points | Sampled beach points | Stream water blocks / wet columns | Beach sand / sandstone / dry sand |
| --- | ---: | ---: | ---: | ---: | ---: |
| Standard / 452343 | 301 | 246 | 25 | 1,856 / 391 | 1,841 / 1,534 / 1,001 |
| Extra depth / 947291 | 285 | 251 | 19 | 898 / 197 | 2,470 / 1,609 / 1,588 |

Point counts describe classification of the fixed sample grid; they are not river counts or a biome distribution guarantee. Block counts describe the tested regions only. One fresh extra-depth startup stalled before spawn selection and was interrupted after about two minutes. Two subsequent fresh extra-depth launches completed, including the full native block checks; the stall was not reproduced and its cause remains unresolved. Map scanning took a few seconds in this environment; it is not an isolated chunk-generation benchmark or an FPS comparison.

Reproduce with Java 25: `python3 tools/validate_waterways.py ./gradlew --accept-eula` after accepting Minecraft's EULA. Prepared dependencies support `--offline`. The fixture generates and saves standard/deep worlds, then creates and reopens a legacy-profile world. Existing test worlds are preserved rather than deleted automatically. The commands and capture classes are excluded from the installer.

The sandbox required a local NeoFormRuntime 2.0.31 build-tool workaround: two process-command lookups use `java.home/bin/java` because this environment withholds `ProcessHandle.Info.command()`. That patched dependency lives only in the local build cache and is not shipped or required on ordinary installations.

## Package audit

Installer: `DeepPrimal-26.1.2-0.0.1-alpha.11.jar`, **22,520,885 bytes**; 16,862 bytes larger than alpha 10.

SHA-256: `51c19aab659ffc8c4c268ba06fe5277e184fc3dd1eb50f197c76d89557e3342f`.

Own class counts: root 41, core 22, performance 14, graphics settings 16, Vulkan 178. Every compiled project class matches its shipped class, and all four nested module JARs match their build outputs. There are no duplicate project classes, validation fixtures or repackaged Minecraft/Mojang classes. Performance, graphics and Vulkan classes, including their bundled dependency classes, match alpha 10 byte-for-byte. Their version metadata changes with the release.

`build -PwithoutVulkan --offline` also passed. Switching profiles without cleaning produced a 216,697-byte installer with exactly three current module JARs and no Vulkan binary, confirming stale module removal. Its SHA-256 is `332a5f769baba324e1cd2835cabcbae25f282ce53bed60058c538fda817be06b`. Restoring the standard build produced the exact installer bytes listed above. This is a package/build check, not a separate client boot.

## Limits

These are static sea-level lowland channels. Branch contours can connect with river corridors, but global watershed connectivity is not guaranteed; closed contours can produce ponds or isolated sections. Variable-altitude streams, waterfalls, catchment routing, dynamic discharge, erosion, recharge and water budgets remain unimplemented. Width varies with the local noise gradient. Climate/vegetation still uses native placeholder biomes/features. Material balance and appearance need interactive visual playtesting across more seeds. This update does not establish hardware rendering gains or repeat alpha 10's GPU validation suite.

Evidence: [runtime summary](validation/0.0.1-alpha.11/runtime-summary.json) and [check markers](validation/0.0.1-alpha.11/checks.txt).
