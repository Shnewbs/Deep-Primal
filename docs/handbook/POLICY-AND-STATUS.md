# Status and decision provenance

**Snapshot:** October 29, 2026, `8f28f833caa259a0e59adac494a3aee8d750bc63`. These are observations of records, not a fresh runtime audit. Later commits require a refresh.

## Separate status identities

| Field | Observation | Interpretation |
| --- | --- | --- |
| Declared source version | `gradle.properties`: `0.0.1-alpha.29-dev.88-vegetation-receipts.1` | A version declaration, not a qualified package |
| Main commit | `8f28f833...`, renderer jam43 code change | A source checkpoint; the backlog's latest summarized receipt is jam42 |
| Overview checkpoints | README/core summary describe Dev32; roadmap opens at alpha22; MODULES is alpha24 | Historical snapshots now marked and routed here |
| Aggregate coordination state | Root ENGINE-WORK-STATE retains a Dev53 checkpoint | Do not overwrite another owner's state to match a displayed version |
| Terrain scoped state | Dev88 candidate, status active, updated 22:06 UTC; active_until 22:33 UTC on October29 | Time-bounded record; an old active flag is not proof of a current live worker |
| Latest deeply reviewed independent record | [Dev87 review](../engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md) | API-double/source evidence with explicit unrun native gates |
| Whole-engine grade | Historical October24 50/100 (5.0/10, Tier2), carried by later reports | No fresh grade assigned by this merge |
| Target | Fresh independent complete ten-category mean strictly greater than 8.5/10, plus applicable water gates | Exactly 8.5 and unrelated renderer self-scores do not pass |
| Delivered game build | Not newly established in this documentation task | No new JAR, playable claim or publication receipt |

Repository timestamps above are preserved in UTC for exact provenance. October29 22:06/22:33 UTC corresponds to 3:06/3:33 PM Pacific. They are historical record fields, not new schedules.

## Decisions and discrepancies

| ID | Merged disposition | Source/provenance |
| --- | --- | --- |
| DOC-P01 — Existing saves | Saved generator/material/hydrology identities and edits override creation defaults; no silent retrogen | [V6](../TERRAIN-V6.md), [V7](../TERRAIN-V7.md), [terrain chapter](TERRAIN-WATER.md) |
| DOC-P02 — Water source semantics | Worldgen source markers and the drain-stop rule are retained; a marker is not an unaccounted emitter. Any permitted replenishment names a finite account or explicit external exchange | [Water master](sources/WATER-ENGINE.md), [budgets](../WATER-BUDGET-REQUIREMENTS.md), [TW-01](research/terrain-water-additions.md) |
| DOC-P03 — Source chemistry | Mixing transfers constituent mass without creating volume; optical interpolation owns no physical quantity | [Terrain/water chapter](TERRAIN-WATER.md) |
| DOC-P04 — Native and staged evidence | V8 core helpers, observed seam/forest routing, disk ledger and API-double guards do not close actual native world lifecycle or finite-cell coupling | [Evidence progression](audit/evidence-audit.md) |
| DOC-P05 — Renderer | Vulkan remains the owner priority, OpenGL supported fallback. Package defaults and actual selected API are separate facts | [Merged renderer chapter](RENDERING-LIGHTING.md), [primary receipt](../validation/engine-continuation/vulkan-primary-2026-10-22/README.md) |
| DOC-P06 — Upscalers | Keep FSR2/3/4 selection as requested work; FSR1 implementation, preference storage, optional NGX bridge and a functioning temporal provider are distinct states | [UPSCALING](../UPSCALING.md), [renderer chapter](RENDERING-LIGHTING.md) |
| DOC-P07 — Standalone | Independent-game direction is retained from owner conversation. Current hosted adapter and historical NeoForge prototype are not an independent game | [Architecture chapter](ARCHITECTURE-AUTHORING.md), [historical prototype](../../standalone-engine/README.md). The independent-game decision is conversation provenance, not inferred from that directory name |
| DOC-P08 — Assets | 32px block/item default, justified 64px and protected approved ground/sand work. Complete state families need their own review | [Texture plan](../art/BLOCK-TEXTURE-PLAN.md) |
| DOC-P09 — Config exceptions | Specific no-vanilla-XP/no-native-remote-storage and saved-world rules override broad historical toggle language | [Owner decisions](../design-decisions-2026-10-18.md), [architecture audit](audit/architecture-audit.md) |
| DOC-P10 — Build/publication authority | October25 policy authorizes direct-main testing releases; later October27 override is repeatedly reported by independent/scoped records as reserving full builds/JARs to owner. The original override text was not recovered here | [October25 cycle record](../ENGINE-HOURLY-BUILDS.md), [Dev87 reported override](../engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md), [evidence audit](audit/evidence-audit.md). Preserve the discrepancy; consult the actual current instruction before a build/publication action. This documentation-only task changes neither authority nor schedules |
| DOC-P11 — Grades | Keep author six-category renderer scores separate from the complete independent engine rubric | [Evidence audit](audit/evidence-audit.md) |
| DOC-P12 — Live vendor data | Pin deployed SDK/runtime and actual capability queries. The live FSR4.1.2 page contains internally inconsistent device guidance | [R-L03](research/lighting-rendering-additions.md); proposed capability evidence, not device acceptance |

## Historical findings with successor evidence

Dev78's tested perimeter-cycle admission was corrected in Dev79's source tests. Dev83's successor-candidate ledger concern progresses through Dev84's durable CAS store, then Dev85's world/profile-bound canonical envelope. These component closures remain linked to their original findings while native finite-cell atomicity and real saved-world recovery stay open. Dev86 placement plans and Dev87 prewrite guards have source/API-double evidence; a realized native placement receipt and lifecycle acceptance are different gates. See the [file/line-backed progression](audit/evidence-audit.md).

The Dev88 version/state declaration does not close any of those gates by itself. This handbook does not promote staged R57/R60/R61/R62, activate V8, change existing geography, replace approved art or infer performance from assertion counts.


## Publication refresh — October 30, 2026

This addendum refreshes the coordination view without rewriting the October 29 snapshot above. Current \`main\` is [\\`aa8043fd\\`](https://github.com/Shnewbs/Deep-Primal/commit/aa8043fdcca0692349f233ce905ae3518ca736d4), source version \`0.0.1-alpha.29-dev.101-prepared-receipt-handoff.1\`. The latest engine sequence (Dev101) wires the prepared V8 vegetation receipt handoff and adds a focused admission guard; the latest renderer sequence is jam 55's bounded LOD renewal-map capacity work. These are source checkpoints only.

| Current observation | Boundary |
| --- | --- |
| Dev101 receipt handoff is present in current source and recorded by the engine builder. | No Java 25/Gradle/NeoForge production build, native save/reopen/readback, or GPU acceptance is established by these commits. |
| Renderer jam 55 pre-sizes the bounded renewal map and records its evidence separately. | No native Vulkan/OpenGL frame-time, VRAM, water-pixel, or packaged-JAR qualification is established by the renderer commits. |
| The current engine lease file is checkpointed and explicitly released; the water work-state remains a historical owner record with an expired \`active_until\`. | Recorded timestamps and old in-progress flags do not prove a live worker. Re-read the live lease before any source write. |
| Roadmap items P0-RENDER, WATER-01..08, LOD-01/02 and broader native visual/performance gates remain open. | Do not convert source-only assertions or carried scores into native completion, a new grade, or a delivered artifact. |

This documentation refresh does not change build/JAR authority, schedules, ownership, saved-world policy, approved art, or any runtime behavior. It records the exact current boundary so later reviews do not mistake the October 29 handbook snapshot for current main.
