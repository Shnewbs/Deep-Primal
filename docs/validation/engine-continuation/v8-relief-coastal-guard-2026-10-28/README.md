# Deep Primal dev61 — opt-in V8 coastal relief guard

October 28, 2026. Production source version: `0.0.1-alpha.29-dev.61-coastal-relief-guard.1`.
Source implementation commit: `0c0e97d7c8194962dd8d273135b05de64dfd5956`.
Latest independent review consumed: `docs/engine-hourly-audits/2026-10-28T1830-7b854ed/REVIEW.md`, pinned `7b854edd9c32c8d20d621495d4a935a5e6418946`.
It reported **581/14,045** V8-relief-2 nearshore columns switching from >96 to <=96 versus historical V8. That is a density/sea-plane consistency risk, **not** a native flooding reproduction.

## Actual code

- Added **new** `TerrainShapeV8.definitionReliefV3()` and `V8GeneratorSnapshot.newReliefV3()` explicit-authoring factories. Historical V8 V1 and opt-in relief V2 graphs, saved V8 envelopes, V1–V7 geography and player edits are **not modified**.
- V3 uses the exact V1 pre-canyon height through continentalness **0.08**. A smoothstep from **0.08 to 0.16** interpolates into the V2 pre-canyon relief; at/above **0.16** V3 equals V2. The original canyon cut remains the final operation.
- Saved V8 revision-3 envelope serializes all new graph nodes. There is no silent migration, no worldgen registration/activation, and no water-quantity or physical sea-level change.
- **Limit:** continentalness is only a climate/relief input. The true native ocean mask, routed streams, river incision, lake outlets and existing edits still require separately evidenced native coherence; this does not prove the final coastline is flood-safe.

## Tests and independent baseline

Java **21.0.12.1**, isolated Minecraft/Mojang API doubles: strict `javac --release 21 -Xlint:all -Werror`, then `java -ea -Xverify:all`. **13/13 focused suites, 17,022,648 assertions/checks passed**; new V3-only suite **2,583,320**.

Four deterministic signed-coordinate seeds surveyed **148,996** columns. Among **91,956** nearshore columns, old relief V2 had **3,611** sea-plane side flips; new relief V3 has **zero** relative to V1 in that same band. **20,678** transition columns and **4,018** far-inland columns were checked for bounded interpolation and exact V2 parity. Largest adjacent-column height delta in this sampled sweep was **4.439**. The audit witness (seed 0, 4279,579) now has bit-exact V1 height.

Modeled V8 saved profile decode/reopen and signed-chunk exact voxel/height parity pass. Historical V1 and V2 graph node SHA256 digests exactly match the extracted Dev60 baseline in both floating-feature modes. A deliberately compiled negative mutant admitting new relief into the protected coast **fails** V3 regression; the unchanged Dev60 V2 remains available, so this change is a **new opt-in recipe**, not a repair in-place.

Source byte identities: `TerrainShapeV8.java` `7c518f9dbef345fa74051356aad4fbd52fda4834`; `V8GeneratorSnapshot.java` `bd5c211fc05c36db1741b28c75df04c0a7bd9583`; `V8ReliefV3CoastalChecks.java` `66eb1f315f9ce9bbf7f756deafaa402cfa294464`. All were checked against pushed main.

Reproducible isolated Java21 evidence ZIP: `DeepPrimal_Dev61_V8_CoastalRelief_SourceEvidence.zip`, 95,727 bytes, SHA256 `cb93a17fdfa6ac45bc23b4c3f38939e365b5256fd17f790f30845050b41915f9` (69 CRC-checked entries). Companion `results.json` contains suite counts.

## Open gates

The original 50/100 = **5.0/10** complete independent grade is **carried forward**, not a new grade. No project Java25, Gradle/NeoForge, actual Mojang codec/save/reopen, native coast/sea-mask/hydrology or GPU validation, owner Windows AMD8060S, long-soak water conservation, or JAR publication. The owner handles full builds. `tools/test_ecotone_blend.py` and `tools/validate_engine_work_state.py` could not be run against a complete pinned checkout in this DNS-isolated workspace; an in-memory root structure preflight passed and the root file remains untouched.

Next: integrate authoritative saved-world V8 climate and ocean mask with density graph and hydrology; prove V3 coastal protection on native cross-generation boundaries, signed chunks, existing edits and saved-profile reopen. Continue finite-water unload/restart constituent ownership only after water writer reconciliation; renderer owner retains GPU scope. **Builder acceptance remains incomplete.**
