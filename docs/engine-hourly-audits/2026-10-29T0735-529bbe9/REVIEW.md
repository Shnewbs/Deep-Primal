# Independent Deep Primal Dev73 audit — 2026-10-29T07:35Z

**Exact pinned source:** [529bbe95f850ef561f195f99b7827ee53e94f50c](https://github.com/Shnewbs/Deep-Primal/commit/529bbe95f850ef561f195f99b7827ee53e94f50c). Read-only auditor, no production/Builder checkout/renderer or lease changes. Baseline remains **50/100 (mean 5.0/10), Tier 2, CARRIED**, not fresh whole-engine regrading. Strict threshold >8.5 not met.

## Actual changes inspected

Opt-in saved V8 closed-ocean route v6, [per-lattice node ocean-domain gating](https://github.com/Shnewbs/Deep-Primal/blob/529bbe95f850ef561f195f99b7827ee53e94f50c/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java#L98-L176), [WatershedRegion interior flood predicate](https://github.com/Shnewbs/Deep-Primal/blob/529bbe95f850ef561f195f99b7827ee53e94f50c/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L269-L347), bounded per-thread DensityGraph samplers and default-off experimental LOD coarse-skip gate. Historical routing/saves are split by policy. Native replay still required.

Builder separately reported **19/19 Java21 source/API-double suites, 21,013,331 checks**, not rerun independently. Production WatershedRegion itself was represented by an API double in Builder's focused tests.

## Independently verified

Restored Temurin JDK **25.0.5+8**; both java and javac verified. Exact committed production LOD adaptive gate blob 6a518b2b0b69a88bec23b885223b8e67bd21e7f9 and unchanged LOD bounds blob 3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc compiled with --release 25 -Xlint:all -Werror against explicit test-only doubles. Runtime -ea -Xverify:all passes: new gate default **300,202**, enabled **166,333**, previous source-matched option tests **60,005 + 60,003**, independent 25k-scene bounds **412,258**, totaling **998,801 assertions**. Another **22/22 pinned production-source contract predicates** passed inspection. No native or GPU claim.

## NEW shoreline classification contract mismatch

[WatershedRegion.sample L1014–1020](https://github.com/Shnewbs/Deep-Primal/blob/529bbe95f850ef561f195f99b7827ee53e94f50c/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L1014-L1020) uses an OR of the four ocean lattice corners and returns a sea-level, speed-zero surface when base is underwater. [V8OceanConnectivity.isMarineAt L343–363](https://github.com/Shnewbs/Deep-Primal/blob/529bbe95f850ef561f195f99b7827ee53e94f50c/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java#L343-L363) requires ALL four graph ocean-domain corners to qualify. [V8RoutedWaterSource.plan L396–407](https://github.com/Shnewbs/Deep-Primal/blob/529bbe95f850ef561f195f99b7827ee53e94f50c/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java#L396-L407) consequently labels the same sea-level, speed-zero partial-corner coastal cell LAKE instead of OCEAN. Java25 independent Boolean source-condition witness: **14/14 mixed 2x2 masks label LAKE**, all-four mask labels OCEAN. **Synthetic source-path witness, not actual native Minecraft reproduction.** Require saved v6 coastline and signed-boundary/chemistry test before fix acceptance.

## Ten equally weighted category baseline (carried)

Terrain 5; biomes 4; hydrology/caves 6; structures 4; performance/threading 3; determinism 7; architecture 6; extensibility 4; testing/docs 7; polish/tooling 4. **50/100, mean 5.0/10, Tier 2.** No fresh full score awarded.

## Build/acceptance limitations and priorities

October 27 owner override assigns full Gradle/Minecraft/GPU/JAR builds to owner. **No current development JAR was built or published.** Git CLI checkout failed github.com DNS; connector provided pinned immutable source reads. Unrun: full Gradle 9.2.1/NeoForge, production WatershedRegion dependency build, archive/nested compiled identities and platform payloads, native Minecraft save/reopen/shutdown, finite-water quantity/chemistry and loaded/unloaded, owner Windows AMD8060S Vulkan/OpenGL/VRAM/fps/FSR, multiplayer soak and visuals. No JAR-retention changes.

Next: unify mixed-corner shoreline classification with saved ocean authority without altering older worlds; prove global connected oceans/outlets and regional spill/discharge; native restart/edit/water conservation; owner-built renderer/bundle acceptance; fresh complete score only with whole-engine evidence. Auditor does not block the Builder.

**Evidence ZIP:** Library /Deep Primal/Independent Audits/DeepPrimal_Dev73_IndependentAudit_2026-10-29T0735-529bbe9_Java25Evidence.zip; 15,775 bytes, SHA256 45b4d3aa94d56f47f128dfa4eda0cd7fe7089d7e7524366ffd6ebb7ea7d7b58c, 16 members CRC/manifest verified.
