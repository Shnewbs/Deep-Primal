# Foundations Soil physical compatibility — OS-02 portable slice

**2026-11-02.** Reference: [Shnewbs/Foundation-soil](https://github.com/Shnewbs/Foundation-soil), **main**, 0.1.0-beta for Minecraft 1.21.1. This repository is private; Deep Primal does not embed its mod or require it at runtime. Reviewed sources: SoilTexture, SoilProfile, SoilPhysics, SoilPhysicalSnapshot, SoilSnapshot, and docs/beta/INTEGRATION_CONTRACT.md (contract 3). The separate 26.1.2 port is not a qualified Deep Primal runtime integration.

## Ownership and no-double-accounting boundary

Foundations Soil already implements persistent 4x4 cells, seven soil health statistics, sand/silt/clay texture, derived water holding, aeration, root resistance, drainage and moisture classification, crop response, amendments and field observations. Its existing TFC integration owns calendar, hydration, growth and N/P/K. Do **not** mirror or overwrite that persistent state in Deep Primal. SoilNutrientLedger is a *portable prospective* ledger, not a second authority when Foundations Soil/TFC already owns N/P/K. A future host chooses exactly one nutrient owner and a durable transaction adapter before enabling uptake or leaching.

FoundationsSoilPhysicsBridge is a **read-only pure function** that accepts the seven canonical profile statistics, optional geological texture and an admitted hydration reading from the finite-water owner. It reproduces the Foundations Soil 0.1.0-beta SoilPhysics.evaluate, SoilPhysics.responseModifier, SoilProfile.nutrientAvailabilityModifier and SoilTexture normalization equations, without depending on NeoForge/TFC. It returns derived physical conditions and nutrient-access *modifier*, **not** nutrient stock or water quantity. Missing legacy texture or missing authoritative hydration yields Optional.empty(); never substitute loam or a made-up wetness value. Reject nonfinite/out-of-range profile and hydration inputs instead of silently converting corrupt saved records into fertile soil. The source mod clamps profile values at its own trusted construction boundary.

This portable component does not save data, bind to a ServerLevel, tick a plant, model soil horizons, or transact with SoilNutrientLedger. No per-leaf or per-root simulation is introduced. The native adapter must obey the source mod's server-thread and no-chunk-force-load requirements, source revision and accepted water ownership. Native integration needs save/reopen, cross-owner atomicity, 26.x compatibility and gameplay validation before any release claim.

## Reproducible isolated validation

Run python3 tools/organics/test_foundations_soil_physics_bridge.py. Java 21 javac compiles the actual production class and independent reference-equation check class in a temporary directory. The deterministic suite exercises 50,000 seeded physical profiles and **550,035 assertions**, including moisture categories, texture normalization, exact derived response, absent-input fail-closed behavior and replay equality. Local elapsed time including compilation: **2.03 s**, peak process RSS **97,064 KiB**; this is not a per-patch engine benchmark or server TPS measurement. A deliberate .55 -> .45 drainage-coefficient mutation was rejected by the suite. Java 25 full Gradle/NeoForge, live Foundations Soil API comparison, game saves and playable JAR are **not run/verified**.

## Next bounded increment

OS-03: validated versioned species-physiology definitions (thermal/light/moisture/root/season requirements), deterministic pure evaluation and malformed-definition/replay tests. Do not claim native plant growth until shared calendar, soil/water transaction, saved plant identity and regional scheduler owners are qualified. No textures were changed or proposed for approval in this slice.
