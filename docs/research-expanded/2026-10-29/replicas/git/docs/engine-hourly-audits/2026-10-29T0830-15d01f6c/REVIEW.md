# Expanded replica — docs/engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md |
| Preserved source SHA-256 | `fd9d1cdf677a2c61e90a79758a6713988b6d9675c115803ac0d1fa2e472efd3c` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This Dev74 report records the v7 shoreline source change and a separate OR-model repair of the fourteen mixed-mask cases; it does not claim compiled production V8 execution for that model. Exact renderer classes were executed independently. The new finite-yaw witness shows overflow when opposite extreme finite float angles are subtracted before modulo, suppressing optional resampling. Fine culling remains active, so the report labels this a bounded helper edge case rather than a reproduced visibility regression.

## Research expansion — gaps and details to deepen

- A repair that agrees with the Boolean model still needs end-to-end saved-policy and native-water checks. The shoreline and renderer findings must retain separate source identities, models and acceptance boundaries.
- The yaw problem is an arithmetic/progress issue in an optional gate; its severity and affected invariant should be recorded without implying missing visible geometry when fine culling is intact.

## Research expansion — proposed contracts

- Create distinct claim chains for shoreline semantic coherence and motion-trigger arithmetic. Link later finite-yaw and normal-angle repairs to their original bounded witness without closing unrelated frame-time or native gates.
- Specify yaw normalization and subtraction domains, finite/nonfinite admission, periodic equivalence and wrap behavior. Keep the common-angle fast path observationally equivalent to a robust reference.
- Retain exact execution category per run: production class, independent model, static predicate or native adapter. Aggregate assertion totals only within a clearly labeled evidence summary.

## Research expansion — acceptance checks

- Test opposite Float.MAX_VALUE endpoints, ordinary wraparound, multi-turn angles, signed zero and invalid values against a robust angle oracle; verify the resampling state changes without weakening fine culling.
- Replay mixed-corner v6/v7 source cases through real generation and save/reopen before treating the shoreline fix as native acceptance.

## Research expansion — source anchors

- Independent engine audit — Dev74 / Java25
- Current source differences and acceptance
- Ten-category equal-weight baseline (carried, not fresh full regrade)

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — Dev74 / Java25

**Pinned source**: [15d01f6c80c1b54555b40c996d93bbe653ecf867](https://github.com/Shnewbs/Deep-Primal/commit/15d01f6c80c1b54555b40c996d93bbe653ecf867), `0.0.1-alpha.29-dev.74-shoreline-coherence.1`. **Read-only auditor**: no production source, builder lease, render cache or producer output changed. **No JAR**, per the owner's October 27 full-build override; no release.

## Current source differences and acceptance

- Dev74 explicit v7 saved shoreline fingerprint and four-corner connected sea (OR) classification are in `src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java` lines 191–199, 312–327, 421–456 (blob `a4128096a81449b6455f397c0d2fc25b228dbf25`). This addresses the Dev73 **synthetic** 14/14 mixed-mask LAKE mislabels without changing v6 saved policy or world quantities. See `WatershedRegion.java` lines 1011–1020 (blob `3066eac9ecae5fc5979bf1c3b3b663b06ee56fc0`). The Builder's Java21 API-double suite reports 19/19 and 21,013,527 checks; those broader tests were **not independently rerun**. Global ocean connectivity and native priority-flood acceptance remain unproven.
- Renderer jam's exact committed `LodPageFrustumAdaptiveGate.java` blob `39531d82e55d905b503d52d439913914c7c1cfc1`, bounds blob `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc` and test blob `61852c3fdd13613a4f85830612560113f2976d2b` matched retrieved Library evidence via Git object hash.
- Isolated Linux Temurin 25.0.5+8: strict `javac --release 25 -Xlint:all -Werror`, nine `java -ea -Xverify:all` runs, **1,507,197 assertions passed** (eight actual production renderer/class+test executions; one independent v7 OR-model, not compiled V8 production). Renderer no-camera-movement mutant failed the positive check at assertion 8; synthetic old all-four shoreline mutant failed at check 2. Fourteen partial 2x2 masks went from 14 old model false lakes to zero in the v7 model.
- **New bounded edge witness:** exact production renderer helper subtracts finite float yaw values before modulo 360 at lines 50–60; moving `-Float.MAX_VALUE → Float.MAX_VALUE` causes a NaN angle delta and skips optional resampling. Deliberately failing independent Java25 witness retained. Not a reproduced Minecraft bug or visibility regression, because fine per-part culling remains active; low priority next to native hydrology/engine acceptance.
- Root `docs/ENGINE-WORK-STATE.json` remains stale at Dev53; scoped `docs/TERRAIN-BLENDING-WORK-STATE.json` records Dev74 and a released lease. Auditor did not edit either.

## Ten-category equal-weight baseline (carried, not fresh full regrade)

| Area | /10 |
|---|---:|
| Terrain | 5 |
| Biomes | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/benchmarks/docs | 7 |
| Polish/tooling | 4 |
| **Total, average, tier** | **50/100, 5.0/10, Tier 2** |

**Not run**: full repository checkout (local git DNS unavailable), Gradle 9.2.1 wrapper/toolchain, complete Java25 NeoForge build, product V8 class with real watershed, current JAR and nested CRC/class/source/native payload validation, actual saved-world reopen, water conservation/chemistry long soak, Windows AMD8060S Vulkan/OpenGL, GPU/FSR/DLSS benchmarks, owner performance acceptance. No older JAR was relabeled, shipped, deleted or released. Existing JAR retention unchanged.

**Priority**: global marine/outlet topology and lake spill; native signed-border save/reopen and finite water/salt/sediment conservation through unload/edits; current platform build and measured frame/tick/memory performance; accepted 3D density, biomes, caves, structure/extension feature behavior. The auditor stays enabled until a **fresh complete** overall average strictly above 8.5/10 (greater than 85/100), not at 8.5.

**Independent retained evidence**: `DeepPrimal_Dev74_IndependentAudit_20261029T0830_15d01f6c_Java25Evidence.zip` in Library `/Deep Primal/Independent Audits/`; 28,509 bytes, SHA256 `7720f718683c0a8c4065b3243de6f50b27f4e8cb53ba69892f05478ffc838b21`, 38 outer-CRC-verified entries and SHA256 member manifest. Archive includes detailed review, receipt, exact source snapshots, test API doubles, two rejected mutations, an intentionally failing extreme-yaw witness, and logs.


<!-- END PRESERVED SOURCE -->
