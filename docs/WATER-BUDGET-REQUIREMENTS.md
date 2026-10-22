# Water mechanics — renewable watersheds and finite local physics

**Owner-approved direction, 2026-10-22:** use a hybrid water system with finite physical water locally, renewable regional headwater reservoirs, and accounted ocean/atmosphere recycling. The default design combines perennial major rivers, seasonal glacial melt streams, stable average ocean level and meaningful local flooding. Strict depletion and optional source-safety controls remain available.

This adopts the proposed water-mechanics design; it does not implement the reservoirs, recharge, emitters or ocean ledger. Numeric rates/capacities, the detailed climate model and release placement remain to specify. Biome identities are planned first; climate later supplies realistic placement and seasonal conditions.

## Current implementation boundary

At reviewed main **797c2851df7816f416e9a25be757444aa7a9836b** (alpha 21), an independent liquid cell holds at most sixteen finite units. Gravity, local routing and pressure transfer existing volume; a full cell is not a replenishing emitter. There is no runtime glacier-melt emitter or complete native ocean reservoir/water-cycle ledger. Generation initializes water in new terrain. Loaded simulation seals unloaded borders and does not simulate the whole planet.

Alpha 21 adds independent liquid presentation, height interpolation and local scheduling improvements. Neither interpolation nor faster transfer creates recharge. Existing conservation fixtures cover finite local flows; they do not validate the watershed mechanics below. This document changes planning only.

## Approved operating model

| Component | Required behavior |
| --- | --- |
| Designated major rivers | Allocate a renewable minimum baseflow in the balanced mode, backed by named regional/circulation accounts. River size and discharge fit catchment supply. This is an explicit gameplay allocation, not an endless source block. |
| Glacial/snow-fed streams | Draw measured meltwater from snow/ice and retained melt storage; supply varies seasonally. Snowfall/recharge replenishes the backing accounts. These streams can weaken, freeze or pause naturally instead of remaining permanently identical. |
| Springs and groundwater | Share regional aquifer storage/recharge. Wells can draw down or run dry through over-pumping and later recover; relocating across a chunk border does not reset supply. Perennial river allocation must not make every well infinite. |
| Physical water | Use the native finite-volume authority for gravity, pooling, pressure, containers, extraction, displacement and local inundation. Flow through a filled channel need not increase its stored volume when outflow matches inflow. |
| Lakes and reservoirs | Track storage, local water level, sill/spillway/outlet capacity and downstream conditions. Incoming water normally leaves through connected outlets; blocked outlets produce storage rise and possible local overflow. |
| Oceans and atmosphere | Exchange with an abstract accounted circulation reservoir under a prescribed average gameplay sea level. River input must not repeatedly stack blocks above ocean surfaces. Rain/snow/recharge use the corresponding circulation budget. |
| Unloaded catchments | Advance bounded coarse records under the chosen calendar rules and exchange volume transactionally with loaded cells. No forced generation/ticking of the entire river or ocean. |

The persistent-world concern is addressed through controlled supply, connected outflow and balanced accounting together. Rate limiting alone merely slows accumulation when a closed basin has sustained net input.

## Water accounting and source delivery

For every account: **change in stored water = accepted inputs - accepted outputs**. Track surface water, soil, aquifers, snow/ice, retained meltwater, containers and ocean/atmosphere exchanges without creating independent copies.

1. **Back every emitter.** Identify its watershed/reservoir, available storage, sustainable recharge allocation, maximum discharge and outlet head. Measure delivery over time instead of restoring a source block to full on every update.
2. **Debit only accepted delivery.** A transaction moves volume from the backing store to physical cells or another authoritative coarse store. If no receiving capacity exists, retain the undelivered water in its actual account; do not enqueue an unbounded imaginary emission backlog.
3. **Budget phase changes and precipitation once.** Snowfall, melting, condensation, infiltration and evaporation move water between accounts. Block removal, particles and precipitation hooks must not each add the same volume again.
4. **Allocate baseflow explicitly.** Balanced mode reserves a renewable environmental allocation for designated perennial rivers from the regional/circulation budget. Its source and priority relative to extraction must be inspectable. It is not a magical per-cell refill or a promise of unlimited player withdrawals.
5. **Conserve at joins and splits.** Downstream supply reflects accepted upstream contributions, storage changes and losses/transfers. Each outgoing branch receives part of the available discharge, never a complete duplicate.
6. **Handle small quantities and initial water.** Preserve fixed-point remainders below a native sixteenth and exact container conversions. Account initialized terrain water and coarse reservoir state once, including newly explored regions; source identity must survive save/reload.
7. **Keep work limits separate from water limits.** Saturated queues defer bounded work or reduce future delivery; they must not delete existing volume, fabricate missing supply or cause a huge unbudgeted catch-up burst.

Exact equilibrium is a regional water-budget problem. Do not assume evaporation will automatically match every source, or increase source output merely because another reach is unloaded.

## Ocean recycling and stable average sea level

The approved default uses an **accounted ocean reservoir with prescribed average sea level**, rather than a globally simulated ocean whose blocks rise after every river contribution.

- Transfer accepted downstream discharge only at a validated connected ocean/reservoir boundary. Never delete inland water simply because it reaches the same Y level as the sea.
- Debit loaded/coarse river storage and credit the ocean account once. Rainfall, snowfall and relevant recharge draw through explicit atmospheric/circulation transfers; do not debit ocean water again for the same precipitation when it infiltrates or melts.
- Keep ocean storage/circulation budgets bounded and balanced over long runs. Merely moving unlimited net creation into an ever-growing hidden counter would hide the underlying error.
- Treat the prescribed level as a deliberate hybrid boundary approximation. Receiving flow still obeys local head/capacity and coupling rules; isolated inland pools do not acquire an ocean drain.
- Keep visual waves and planned tides/surge distinct from actual inundation. Physical coastal flooding needs an accounted stage/storage/exchange policy, not visual displacement alone.

Global mean sea-level evolution remains a separate future opt-in with storage/area, terrain, save and performance implications. Stable mean sea level does not mean suppressing every wave, tide or local storm surge.

## Seasonal surface water and dry-biome recession

**Owner clarification, 2026-10-22:** a desert basin may flood during its wet period and dry again through the following hot/dry period. Include temporary desert lakes, washes/wadis and exposed beds in the same mechanics. Biome/habitat context helps define the expected regime; actual supply, weather, terrain and substrate decide how much water remains. Do not force every desert to have the same winter-rain/summer-dry calendar.

| Water regime | Expected behavior |
| --- | --- |
| Perennial | Designated river reaches retain planned renewable baseflow under ordinary balanced-mode conditions; catchment budgets include downstream losses. Passing through a desert does not by itself erase the current or water. |
| Seasonal/intermittent | A channel/lake connects and fills during its locally appropriate wet or melt season, then contracts, separates into pools or dries as supply declines. |
| Storm-driven/ephemeral | A normally dry wash or basin receives temporary storm runoff, may flood locally, then drains/infiltrates/evaporates back toward a dry bed. |
| Groundwater-fed refuge | A spring/oasis or residual pool can persist through dry weather if its actual groundwater supply supports the losses. A dry surface elsewhere need not imply an empty aquifer. |

Required behavior:

- Rainfall/runoff may exceed local drainage and produce temporary physical floods. Later water level and wet area fall when evaporation, infiltration, downstream discharge and extraction exceed new input.
- Evaluate evaporation against water availability, exposed surface, temperature, humidity, wind and sunlight; infiltration uses material permeability, soil saturation and groundwater capacity. A sandy bed and a sealed clay basin need not recede at the same rate.
- Transfer evaporated water into the atmospheric account, infiltrated water into soil/aquifer storage, and drainage into the connected downstream account. The visible water may disappear while its quantity remains accounted elsewhere.
- Let physical shallow water retreat into pools and sub-unit films, then damp ground and an exposed dry bed where the budget supports it. Cosmetic wetness should follow available moisture and dry gradually; never hide a still-full physical fluid cell to simulate evaporation.
- Preserve suitable sediment/soil state through inundation and drying. Any proposed mud/cracked-bed/salt appearance depends on substrate and retained composition; salt or valuable materials are not generated freely each cycle. Catalog implementation remains deferred.
- Keep frozen water distinct from dry ground: seasonal ice/snow stores retain their water and feed thaw/melt accounts later.
- Keep the biome roster and identity stable across ordinary floods and dry seasons. Seasonal inundation is an environmental state; it does not require converting the biome into desert, lake or river IDs.
- Use coarse summaries and event-driven active patches for recession. Seasonal drying complements the wider budget; it is not a guaranteed sink capable of canceling unlimited upstream emissions.

The wet-period desert example is a required acceptance scenario, alongside a supplied perennial desert river, a rapidly infiltrating reach, a slower-draining basin and groundwater beneath a dry surface. Repeat several fill/dry cycles with mass, appearance and saved-state checks.

Research grounding for this simplified game design: [USGS intermittent desert streams and recharge](https://www.usgs.gov/publications/ground-water-recharge-small-intermittent-streams-western-mojave-desert-california), [USGS ephemeral-stream terminology](https://water.usgs.gov/water-basics_glossary.html), and [NPS temporary surface waters](https://www.nps.gov/articles/000/water-on-the-colorado-plateau.htm). These support the process distinction; they do not prescribe our numerical climate model.

## Operating modes and blocked rivers

These are required behaviors; names, numeric settings and config keys are not yet shipped.

| Mode/control | Behavior and limit |
| --- | --- |
| **Balanced — default direction** | Renewable environmental baseflow for designated major rivers; seasonal meltwater; finite local physics; fixed average ocean boundary; real local storage, damming and floods. |
| **Strict — optional** | Remove the protected minimum-flow allocation. Supply follows finite stores and declared recharge; drought or excess extraction may dry rivers/springs. Ocean boundary policy remains explicit and separate. |
| **Source safety — optional independent control** | Reduce or pause new source delivery when its head/storage/capacity limits are reached. Preserve existing water and reservoir accounting. It is a gameplay abstraction, not a physically unlimited source that somehow cannot flood. |

**Default blocked-river behavior:** water accumulates behind a dam, raises upstream level, finds a legitimate spillway or overtops available banks. Backwater affects exchanges and springs according to head. Topology/storage changes must update affected reaches without scanning the world or deleting player structures.

A positive minimum delivered flow cannot be guaranteed into a permanently sealed finite basin while also guaranteeing no flooding. Under source safety, the delivery guarantee yields to the storage/head limit. The unrealized requested flow is not newly created water. Physical snow/ice already melted remains in an accounted store or follows an overflow route; preventing further melt/delivery as a safety action must be labeled as that abstraction.

Retain the earlier owner decision that shared aquifers and wells can be depleted by extraction. Define environmental-flow reservation versus extraction priority before implementation so renewable major rivers do not become an unbounded pump exploit.

## Loaded/unloaded ownership and performance

Use a versioned seeded watershed graph with shared outlet identities, finite regional stores and coarse discharge summaries. Loaded detail and coarse records must have one owner for each represented volume.

- Persist boundary-transfer transactions/revisions and water composition where relevant. Reconcile once when chunks activate; reject stale work and repeated transfers.
- A receiving unloaded region must accept into an authoritative coarse store with capacity, or apply backpressure. An absent chunk must not become a free sink or freeze every downstream ledger indefinitely.
- Bound active records, hydrology steps, graph invalidation, pending transfers, I/O and network updates. No per-block global ocean scan, permanent forced chunk tickets or whole-world fluid ticking.
- Follow the selected host-game/TFC pause, calendar and empty-server direction. Specify unloaded/offline advancement and bounded catch-up before enabling recharge.
- Client interpolation, LOD, cosmetic canopy droplets, puddles, foam and graphics quality settings cannot create, consume or change authoritative water quantity.

## Open implementation and acceptance requirements

- [ ] Specify regional storage/flux schema, units, source identity, baseflow allocation, extraction priority, capacities and migration/version rules.
- [ ] Implement metered reservoir-backed delivery and seasonal melt/recharge; test accepted versus blocked output and phase-change accounting.
- [ ] Implement connected lake/sill/outlet and coarse-to-fine river/ocean exchange, including local backwater and overflow.
- [ ] Expose balanced/strict policies and independent source safety with visible effective settings and inspection of storage/inflow/outflow.
- [ ] Pass long-run closed-box conservation, renewable circulation cycles, depletion/recovery, small-quantity and container/machine cases.
- [ ] Verify many simultaneous glacier/catchment inputs do not progressively raise default ocean blocks or grow hidden storage without bound.
- [ ] Verify a perennial major river in normal operation, a seasonal melt stream, an over-pumped shared aquifer, and balanced versus strict drought behavior.
- [ ] Verify dam filling/overtopping, a closed inland basin, head-limited delivery and optional safety with no lost water or unbounded emission backlog.
- [ ] Verify repeated desert wet-season flood/dry-season recession, ephemeral versus supplied perennial reaches, substrate differences, residual groundwater, freezing versus drying and conserved sub-unit moisture.
- [ ] Verify branch splits, generated-region initialization, unloading/reloading, restart, worker ordering and multiplayer exchange without duplication or forced remote generation.
- [ ] Record named-hardware worldgen, server-tick, memory, I/O and network budgets; verify visual settings and disabled presentation leave physics unchanged.

See the [owner decisions](design-decisions-2026-10-18.md), [roadmap](../roadmap.md), [glacial landscape plan](GLACIAL-LANDSCAPES.md), [reference completeness audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) and [stream/canopy/puddle review](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md). Environmental catalog and climate implementation remain deferred; this is adopted mechanics direction with implementation work still open.
