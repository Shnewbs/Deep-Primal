# Expanded replica — docs/validation/engine-continuation/regional-budget-cache-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/regional-budget-cache-2026-10-24/README.md) |
| Source identity | git: docs/validation/engine-continuation/regional-budget-cache-2026-10-24/README.md |
| Preserved source SHA-256 | `7ce78b18a491601b8ca6fce6bc83a8ac72f227afa0da1447924c0d508e6f5f01` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R46 preventing reconstructed RegionWork from spending another 32-unit regional allowance inside an unchanged aquifer interval after cache clearing. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the defect bypassed a shared rate cap while still debiting finite groundwater, so it was not total-water duplication. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a persistent-rate ownership rule separating cached work state from the aquifer interval that authorizes discharge. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Clear caches mid-interval and unload unrelated regions, assert no second allowance until a successful aquifer step, and separately verify ordinary throughput and constituent conservation. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R46: regional emission allowance after cache reconstruction

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R46: regional emission allowance after cache reconstruction

Source: consolidated main c7a0373efe4be832f492c5e05ed2aeb15257e283 (alpha26).
HydrologyRuntime.unloaded can clear every RegionWork when cache count exceeds
loaded chunk count, including regions whose persistent aquifer interval has not
advanced. A reconstructed RegionWork starts discharged=0, while aquifer.step
returns false inside its existing 20-tick interval. Its outlets can spend another
32-unit allowance. Water remains debited from finite groundwater; this is a shared
rate-cap bypass, not duplication of total stored water.

The focused candidate names the existing32-unit limit and initializes a rebuilt
RegionWork with its allowance exhausted. Only the already existing successful
aquifer.step path resets it. This fails closed until the next fixed step (at most
the remainder of the normal interval when visited), without persisting extra data,
creating a backlog or changing generated geography/chemistry/volume accounting.
Repeated churn can suppress output; it cannot mint more emission allowance.

check.py extracts the actual inner RegionWork class, reset statements, admission
guard and remaining-limit expression from baseline/candidate runtime source. It
compiles them with unchanged production Aquifer and WaterAccount using retained
JDK25. The harness replaces Minecraft plumbing with four-unit accepted transfers
and declared cache-reconstruction schedules. It enumerates861 schedules across
200 visits each (172200), checking the per-environmental-step ceiling and exact
groundwater+delivered volume after every visit. It also checks ordinary32-unit
delivery and one allowance after a long offline gap. Baseline fails with54972
over-budget observations and maximum80; candidate passes with zero and maximum32.
These are modeled source-extracted visits, not native ticks or independent60Hz.

The first generated fixture failed compilation because an unrestricted placeholder
replacement also replaced part of REGIONAL_BUDGET_RESULT. The extractor was corrected
to replace only the isolated placeholder line; final baseline/candidate compilation
passes. This was a private fixture defect; no product code was changed to satisfy it.

Only candidate/HydrologyRuntime.java is the staged runtime fix. Public production
source and the alpha26 installer are unchanged. No full build/native/visual/performance
check was run for this candidate; it must remain staged until real unload/reload
events and multi-region/multi-outlet save/reopen integration are validated. Actual
column service latency is not guaranteed by the nominal2ms admission window.

Run `python check.py` beside baseline/ and candidate/ with the recorded JDK25 path
available. The result deliberately expects a baseline failure and candidate pass.
results.json preserves exit codes, raw output hashes and source hashes. Next promote
only after the packaged native hydrology suite exercises cache clearing mid-interval,
unrelated-region unloads, ordinary throughput and constituent conservation.
Continue the terrain/cave/material and Vulkan bounded-publication work afterward.


<!-- END PRESERVED SOURCE -->
