# Organic World Simulation — first portable increment

**Checkpoint:** 2026-11-01. **Status:** portable N/P/K ledger implemented; no native soil, plants, tree growth, seasons, fungi, persistence adapter or gameplay integration is claimed.

## Ownership and integration boundaries

This workstream owns new `core/organic/` portable biological rules, scoped tests and organic-specific documents. The existing engine/terrain worker owns V8 world generation and `V8Vegetation*` placement/revision code; hydrology owns physical water and drainage; renderer owns presentation; auditors own independent grading. Do not edit those workers' source, shared `CoreChecks`, world-state files, Gradle or art without a fresh nonoverlapping claim. No `AGENTS.md` was found in the inspected repository tree. The existing [agriculture specification](../systems/AGRICULTURE-SYSTEM.md), [shared contracts](../systems/SHARED-SYSTEM-CONTRACTS.md), [natural biology catalog](../NATURAL-TYPES-BIOLOGY.md) and [systems roadmap](../systems/SYSTEMS-ROADMAP.md) remain authoritative; this document records implementation state, not a replacement catalog.

The physical world owns **one** accepted environmental source for each transfer:

- Soil/organic core owns mineral N/P/K and detrital N/P/K integer game-unit pools for a patch. It never assumes free fertilizer, soil replenishment, biomass or rain.
- Plant physiology requests uptake and receives only the amount the soil can debit. A host transaction must credit the same amount to a durable plant/biomass account; this portable ledger does not perform that transaction.
- Decomposition moves **existing** detritus into mineral pools. Litter/manure credit requires an already-debited external owner. Explicit geological weathering and atmospheric fixation must be separately authorized inputs; fixation must never create P or K.
- Drainage takes mineral nutrients out and returns an explicit amount for the water/sediment authority to credit. The ledger never modifies hydrology, water quantity, salinity or river chemistry.
- The world calendar, climate, soil profile, spatial patch ownership, regional scheduling, species definitions and persistence are *inputs* supplied by their owning systems, not synthesized from wall time or coordinates here.

## Implemented: OS-01 portable soil nutrient transfer

`engine-core/src/main/java/com/shnewbs/deepprimal/core/organic/SoilNutrientLedger.java` is an immutable, schema-1, monotonically revisioned two-pool N/P/K ledger. `creditMineral` and `creditDetritus` explicitly label **external accepted** input; `mineralize` moves only detrital stock; `uptake` and `leach` return exact accepted withdrawals and the resulting ledger. Every component is a nonnegative signed 64-bit integer with checked additions and revision increments. A zero transfer is a no-op. Requests beyond availability receive the available portion, not negative stock or a free credit. No timestamps, random draws, tick loops, global state or chunk dependencies exist.

Run the isolated check using `python3 tools/organics/test_soil_ledger.py`. It compiles the **actual production source** and check source in a temporary directory, then executes 100,000 seeded randomized conservation trials (1,500,012 checks) plus invalid schema, negative, null, overflow and insufficient-balance rejection. Java 21 can execute this dependency-free component; the full project still targets Java 25. A compiled negative control that disables mineral withdrawal was rejected by the exact-uptake assertion. This check is not a full Java25/Gradle/Minecraft native or packaged test. `CoreChecks` integration is intentionally deferred to avoid overlapping the engine worker's shared test entry point.

**Important limitation:** A revision alone does not provide durable compare-and-swap, exactly-once transfer, save/reopen or cross-owner atomicity. A caller that credits the counterparty but fails to save the soil debit can still duplicate nutrients. No native adapter is registered, and no plant is presently grown by this class.

## Dependency-aware implementation roadmap

| Milestone | Scope | Dependencies and acceptance |
| --- | --- | --- |
| **OS-01** | Portable conserved mineral/detrital N/P/K pool | **Component implemented and locally tested**; native transaction/persistence still absent |
| **OS-02** | Versioned soil patch identity, horizon capacity, pH/texture/structure and explicit finite-water pore owner link | Real saved `SoilProfile`, geology and finite-water ownership; unknown/unsupported data must not create ideal soil |
| **OS-03** | Versioned species definitions (annual, perennial, tree, grass, fungus), climate and root requirements | Validated data codec/registry and stable IDs; reject missing/invalid species fields and keep existing saves on pinned revisions |
| **OS-04** | Deterministic bounded phenology and growth, including seedbank, dormancy and seasonal stress | Authoritative paused-when-empty calendar and admitted climate/light/soil inputs; no offline invented growth or free water |
| **OS-05** | Procedural tree architecture, structural age, canopy competition and root footprint | Independent from frozen V1–V7 geography; stable tree IDs and seed/address-order invariance; approval before new texture families |
| **OS-06** | Decomposition, fungal exchange, forest succession and regional cohorts | Paid nutrient/biomass transfers, bounded update budgets and unloaded-region policy; no per-leaf entity ticking |
| **OS-07** | Native integration and acceptance | Atomic soil/plant/water/biomass journal, duplicate/failure/reopen tests, signed seams, multiplayer, visual seasons and named-hardware p95/p99 |

### Next concrete increment

Implement a **versioned, validated species-physiology definition** in `core/organic/` with explicit thermal/light/moisture/phenology parameters and deterministic pure evaluation. Add malformed-definition and seeded replay tests. Keep it independent of `V8VegetationPolicy`, which freezes world-generation placement rather than living plant physiology. Do not wire native growth until calendar, finite-water and saved-world transaction ownership are qualified.

## Validation and release state

This checkpoint changes no resources, approved textures, worldgen snapshots, renderer, water or existing saves. It is a portable library component only. A full Java25 Gradle build, native save/reopen, actual seasonal visuals, game JAR and performance benchmark are **not** claimed. Every new caller must preserve the exact immutable ledger and counterparty transaction; dropping the returned `Withdrawal` or applying it twice violates the contract.
