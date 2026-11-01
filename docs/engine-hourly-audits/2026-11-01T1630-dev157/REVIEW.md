# Dev157 — independent Deep Primal audit and correction

**Date:** 2026-11-01 16:25–16:30 UTC (approx.). **Previous audited main:** `d0c4c1131774bdbc6de9d27c7d599de5f7d4b10e`. **Builder Dev156 head:** `bbfc78891c69e6e0a54c811bfcccf6abe04ca58f`. **Seven new Builder commits reviewed.**

## Builder review and scope

Builder Dev156 corrected `WaterCornerPacket.matches` and `changedCorners` to normalize malformed directions consistently with `corner`/`patchCorners`, added `WaterCornerPredicateAdmissionChecks`, registered the regression in `CoreChecks`, documented source-excerpt tests, and released its writer lease. The audit inspected the actual changed files and reported 4,307,250 isolated Java21 checks in the Builder evidence; **the Builder's complete test suite was not independently rerun**. No conflicting active writer claim was found: `docs/ENGINE-MAIN-WRITER-LEASE.json` showed `completed-released`.

## Independently reproduced defect

`WaterCornerPacket.retirementState(b, begin, end, now)` rejects reversed ranges with `begin > end`, but the mutating `WaterCornerPacket.retire(...)` did not. For aligned `begin=576, end=144`, `retire` returned `false` without inspecting or retiring anything, silently accepting an invalid sweep. This is a confirmed API-contract inconsistency for malformed inputs; **a real-game reversed-range occurrence has not been established**.

The production fix adds `begin>end` to the existing mutating method's range guard. The permanent `WaterRetirementStateChecks` regression now covers invalid negative, out-of-bounds, unaligned and reversed ranges; invalid inputs leave data unchanged; empty valid ranges remain no-ops. `CoreChecks` already invokes `WaterRetirementStateChecks.run()`, so no registration change was needed.

- [Production correction](https://github.com/Shnewbs/Deep-Primal/commit/58d57626962659920d2fe81d43d7dc81cfd89537), blob `381e0c1dc5e730751f9495f798a9b036cd212121`.
- [Permanent regression](https://github.com/Shnewbs/Deep-Primal/commit/610fcba5306c17d8ff220fe32de2e1e792f4d207), blob `623c9e708a3b356e0a46a1126985dd993770b183`.

## Independent evidence and limitations

- `javac --release 21 -Xlint:all -Werror`: **PASS** on verbatim original/corrected guard excerpts.
- `java -ea -Xverify:all`: corrected excerpt **10,008 assertions PASS** (10,000 randomized aligned ranges plus eight boundary cases).
- Negative control: original guard **FAILED as expected**, `begin=576,end=144` accepted.
- Exact corrected production and test blobs were re-fetched from GitHub and verified against the above SHAs.
- **Limit:** isolated source-excerpt tests, not compilation of the entire `WaterCornerPacket` class, Gradle `coreChecks`, or full native game.

## Acceptance matrix

| Area | Status |
|---|---|
| Seven new Builder commits / predicate admission review | Complete source review; full Builder suite not independently rerun |
| Retirement-range contract | Reproduced, fixed, isolated regression passed |
| GitHub main commits | Production and permanent regression verified |
| Java25 Gradle 9.2.1 NeoForge build | BLOCKED |
| NeoForge 26.1.2.122 | Configured, not runtime tested |
| NeoForge 26.3 / Minecraft 26.4 snapshot | Compatibility unverified |
| Water conservation, V8 save/reopen, terrain seams | Not natively tested |
| Vulkan/OpenGL, LOD/upscaling and FPS | Not hardware benchmarked |
| Fresh playable JAR and JAR SHA256 | Not produced |

**Build blocker:** local runtime is OpenJDK21 without a complete private repository checkout, JDK25, Gradle installation/resolved NeoForge dependencies or Git CLI DNS access. `build.gradle` declares Java25 and Gradle 9.2.1, `gradle.properties` pins NeoForge `26.1.2.122`. Official NeoForge 26.3 source uses Java25: https://github.com/neoforged/NeoForge/blob/26.3.x/gradle.properties . **No JAR deleted. No GitHub Actions used.**

**Grades:** scoped retirement-range validation **8.0/10 provisional**; historical whole-engine baseline **5.0/10**, not freshly regraded. Full engine acceptance requires real build and hardware/runtime measurements.

**Local evidence:** `DeepPrimal_Dev157_IndependentAudit_20261101T1630_Evidence.zip`, 4,707 bytes, SHA256 `530c66a3553da19fd643f8efb5f720f3f822f31b4063e083a58c9d243ca5fc17`; ZIP CRC verified.

**Next:** restore full Java25/Gradle/NeoForge packaging, build and hash a playable JAR, only then remove any superseded audit JAR. Prioritize finite-water conservation, saved-world persistence, terrain borders and Vulkan/LOD performance.
