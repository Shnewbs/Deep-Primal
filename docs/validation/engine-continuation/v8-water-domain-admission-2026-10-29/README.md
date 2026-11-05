# Dev66: opt-in V8-r4 dual-water-domain preflight (source-only)

**Status:** explicit-call-only V8 pre-generation and materialization paths, not registered Minecraft world generation, not a published JAR and not native hydrology acceptance. Owner retains full Gradle/Minecraft/GPU/JAR work. New source version: `0.0.1-alpha.29-dev.66-water-domain-admission.1`.

**Independent auditor consumed:** [2026-10-28T2331-d99837b/REVIEW.md](../../../engine-hourly-audits/2026-10-28T2331-d99837b/REVIEW.md), pinned `d99837bb76ddfa6fd56cb5f133eb5021dde8c131` (baseline 50/100, 5.0/10; not regraded). Highest-priority unresolved water-domain/codec and saved-border findings motivated this fail-closed guard. It does not pretend to implement a routed V8 ocean mask.

## Changed production code

- `V8WaterDomainAdmission`: exact-bound seed, complete saved density definition and revision-4 `V8ClimateBinding` identity. Requires a caller-supplied water-domain callback with interface revision 1; rejects unsupported profile/authority and malformed wet/dry physical stages before block writes. Combines the saved V7 reference and caller V8 observation conservatively, never painting dry topsoil when either says ocean/river/lake. Dry terrain **below frozen sea plane Y=96 is withheld** pending actual V8 coastline evidence. Physical stage is read but never mixed into terrain heights or water quantities.
- `DensityChunkMaterializer.planSavedEcotoneAdmitted()` and `fillSavedEcotoneAdmitted()`: explicit V8 revision-4 query and write entry points use the *same* admission wrapper. As with existing dev53 prewrite behavior, every climate and hydrology observation is resolved before native block/heightmap mutations; callback failure keeps destination untouched. Existing r1–r4 saved envelopes, V1–V7 geography and legacy ecotone entry points are **unchanged**.
- This *is not* a producer of authoritative V8 hydrology. A callback's declared mask revision is not a persisted V8 routing policy, and the caller must eventually provide a real, versioned routed-water generator. The conservative undersea exclusion may intentionally omit dry lowlands, so it cannot be activated automatically or described as a complete ocean/coast solution.

## Executed evidence — isolated Java 21 API doubles only

OpenJDK and javac `21.0.12.1`; `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.

- **17/17** isolated focused production-class suites passed, **21,183,814** reported fixture assertions/checks in aggregate. Includes retained V8 base-height, column/heightmap parity, saved policy, relief V2–V4 coastal and material tests. The new `V8WaterDomainAdmissionChecks` executed **222,019** checks, including **208,896** prewrite/native-stub voxel comparisons, **2,057** conservatively masked columns and **12** explicit invalid-input/recovery rejections.
- The standalone exact-production-`V8WaterDomainAdmission` Java21/API-double runner passed **92,169** assertions in each of two identical runs, checksum `379ecf5601fc5d2c`. Three independently compiled broken controls (ignored V8 ocean, ignored undersea guard, ignored graph identity) each failed the positive oracle. The full-class isolated suite also rejected all three compiled mutants. These are not native game tests.
- Source blobs compared with the tested files: new authority `76be13f117c5fe59b1a9d928907cef77f9640df5`, materializer `aeab13a5da121e46da3da67455ede0e12937226e`, test `288e097d849cc24f78c979b6a093a289f9149e13`, runner `b2bb269e5ad621995dd030c1b2f37f8c6a824837`.
- Reproduce the **standalone** component regression in a normal source checkout: `python3 tools/test_v8_water_domain_admission.py --output /absolute/output/results.json`. The wider 17-suite API-double replay ran in an isolated source fixture backed by local test doubles; it is not a full-checkout Gradle test. Logs and manifest can be retained separately in the owner's source-evidence bundle.

## Nonacceptance gates

Complete pinned root work-state Python validation was not executed (full checkout/host Git transport unavailable). No root ownership or root state publication occurred. Java25, Gradle9.2.1, NeoForge/Mojang DFU native save/reopen, native V8 ocean/river/lake mask, old/new-generation borders, owner terrain/vegetation transects, finite-water loaded/unloaded/restart accounting, long multiplayer soak, OpenGL/Vulkan water pixel acceptance and hardware CPU/GPU/IO p50/p95/p99 are **not run**. New ecotone callbacks cannot be declared authoritative from fixture parity alone. Last independently accepted full grade **5.0/10**, unchanged.

**Next:** Save an explicit V8 hydrology/routing-policy identity (separate new revision), implement and validate a real routed V8 water-domain supplier before automatic registration; resolve coastal/old-new chunk borders and finite-water unload/restart ownership without editing renderer-owned files.
