# Preserved source link context

Raw project documents retain their exact downloaded bytes. Their relative links refer to their original authoring layout, rather than the snapshots directory. This table supplies canonical targets without rewriting those source bytes. Source IDs, versions and full hashes remain in [catalog.json](audit/catalog.json).

| Source | Original relative link | Canonical target / availability |
| --- | --- | --- |
| Water Engine | `../roadmap.md` | [Roadmap](../../roadmap.md) |
| Water Engine | `ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md` | [Alpha26 checkpoint](../ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md) |
| Water Engine | `WATER-BUDGET-REQUIREMENTS.md` | [Budget requirements](../WATER-BUDGET-REQUIREMENTS.md) |
| Water Engine | `VALIDATION-0.0.1-alpha.26.md` | [Alpha26 validation](../VALIDATION-0.0.1-alpha.26.md) |
| Water Engine | `VALIDATION-0.0.1-alpha.23.md` | [Alpha23 validation](../VALIDATION-0.0.1-alpha.23.md) |
| Water Engine | `TERRAIN-MECHANICS.md` | [Terrain mechanics](../TERRAIN-MECHANICS.md) |
| Water Engine | `design-decisions-2026-10-18.md` | [Owner decisions](../design-decisions-2026-10-18.md) |
| Water Engine | `TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md` | [Terrain/water audit](../TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) |
| Water Engine | `STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md` | [Streams/canopy/puddles audit](../STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md) |
| Water Engine | `LOD-AND-DRAINAGE-ARCHITECTURE.md` | [LOD/drainage architecture](../LOD-AND-DRAINAGE-ARCHITECTURE.md) |
| Water Engine | `GLACIAL-LANDSCAPES.md` | [Glacial landscapes](../GLACIAL-LANDSCAPES.md) |
| Water Engine | `BIOME-PLAN.md` | [Biome plan](../BIOME-PLAN.md) |
| Water Engine | `CONFIGURATION-AND-ORE-AUTHORING.md` | [Configuration/ore authoring](../CONFIGURATION-AND-ORE-AUTHORING.md) |
| Telemetry review | `../20261027-pending-gpu-ledger/REVIEW.md` | [Pending GPU ledger review](../renderer-jams/20261027-pending-gpu-ledger/REVIEW.md) |
| Telemetry review | `TESTS.md` | [Preserved tests document](sources/DeepPrimal_Renderer_20261027_part_upload_telemetry_TESTS.md) |
| Telemetry review | `CLAIM.json` | Not present at this document's original layout in the pinned tree; no claim file invented or reconstructed |

Ten malformed relative-link occurrences across six original Git documents were repaired with unambiguous tracked targets. Those edits and the navigation/status repairs are recorded in [repairs.json](audit/repairs.json); the original bytes remain recoverable at the pinned Git commit. Historical findings, test results and grades were preserved.
