# Independent committed-snapshot audit — 2026-10-24

Pinned main: `dea1246899f52c3392a094ab389e91416bc57b71`. Development version: `0.0.1-alpha.29-dev.2-audit-20261024T1946-dea1246`. Independent checkout/cache/output; no builder lease/state/source mutation or performance benchmark. No runtime implementation was authored by this auditor.

## Grade

**Carried forward, not a fresh complete regrade: 50/100, average 5.0/10, Tier 2/5.** The original equally weighted rubric and pinned file/line/native performance evidence remain in [the accepted review](https://github.com/Shnewbs/Deep-Primal/blob/dea1246899f52c3392a094ab389e91416bc57b71/docs/engine-review-2026-10-24/REVIEW.md). Fresh build/core/package checks do not demonstrate new gameplay features, owner hardware performance or artistic acceptance.

| Category | /10 | Evidence / unresolved gate |
|---|---:|---|
| Terrain realism/variety | 5 | Fresh V6/V7 tests; current TerrainEngine heightfield remains active; V8 unregistered. |
| Biome blending/coherence | 4 | Landscape thresholds/RegionBiomeSource categorical palette remain; weighted ecotones absent. |
| Hydrology/caves | 6 | Fresh budget/conservation, cave/pressure/watershed core checks; large-region/long-duration native acceptance open. |
| Features/structures | 4 | Existing trees/deposits; general spacing/structure pipeline absent. |
| Performance/threading | 3 | No fresh timing credit. Historical native warm fill14.45–14.99ms exceeds5ms; staged R62 order sensitivity unresolved. |
| Determinism/stability | 7 | Fresh differential/seam/replay core tests; cross-platform/long soak acceptance open. |
| Architecture/code quality | 6 | Separate modules and source boundary gates; monolithic materialization/revision stages remain. |
| Extensibility/data-driven | 4 | Saved profiles/deposits; density library remains inactive in gameplay. |
| Tests/benchmarks/docs | 7 | Fresh independent JDK25 complete core run; no fresh broad graphics/Windows/soak matrix. |
| Polish/config/tooling | 4 | Water/LOD/transparency/recovery defects and current-version/editor gaps remain. |

## Changes reviewed

R62 is a **staged constructor-only** DensityGraph optimization, not production promotion. Its completed evidence reports1,911,605 checks for both control/candidate,180 generated graph layouts, three orders and full128-node cases. Terrain graph construction allocation falls from~37–44KB to~18–21KB. Sampling stays mixed: Y-inner~32–47ms and X-inner~255–307ms, excluding native work. These are reported builder measurements, not rerun timings in this audit. [Pinned R62 source and evidence](https://github.com/Shnewbs/Deep-Primal/blob/dea1246899f52c3392a094ab389e91416bc57b71/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md).

R57/R60/R61/R62 staged variants are excluded from this snapshot build. No new V8 gameplay credit. FrozenV1–V7, saved profiles, water quantity, edits, Deep Primal naming and independent graphics/LOD/Vulkan/Themes source are retained.

## Recovery and validation

682 build inputs independently match Git blob identities from the pinned recursive tree. Original full Gradle attempt failed before mod compilation because a copied NeoForm decompile cache file was not a ZIP. Rejected bytes/inspection and full failure log are retained. Only the audit cache's bad decompile entry was quarantined; regeneration passes patching and source transformation. This is an environment/cache failure, not a reproduced mod runtime defect.

Fresh independent core compilation/execution passes original checks including183806 noise,40987 density graph,6144 water-bed,500000 transfer conservation,5846893 watershed centerline and existing frozen terrain/cache/seam/pressure suites. Detailed raw output retained. Fixture timings in those logs are not performance acceptance during build activity.

## Next priorities

Finish staged density full module/package/native materialization/base-column/heightmap/saved-profile gates; fix conservative topology (906 coarse occupancy mismatches) and order-dependent latency before V8 registration. Continue weighted ecotones, drainage/cave/aquifer coupling, deterministic feature ownership, bounded host scheduling and actual target-version integration. Water/LOD temporal coverage, publication budgets and owner8060S/Windows acceptance remain open. Average8.0 has not been demonstrated; auditor remains enabled.

## Final completed gates

- Full Java25/Gradle9.2.1 Windows Vulkan build passes32actionable tasks:31executed/1up-to-date; boundary/cache/core checks and complete visual fixture compilation pass.
- Export CRCs:7archives. Compiled/package identity:518product classes. Independent57-source core compilation matches101packaged classes. All165compiled private fixture paths and build tools excluded. Windows DLL payload and complete Themes assets retained.
- Linux packaged dedicated-server fresh/reopen sessions pass V7 profile/seed/base-column/biome checks:3,062,331blocks,44chunks,704biome checks,139log and51,181water observations per session. Replay hashes match and the gold-block player edit survives reopen. Both sessions save-all flush and normal stop with exit0. Loaded product class hashes match compiled/package identities. No GPU rendering is exercised by dedicated server.
- Final development JAR size124,987,216bytes; SHA256`f824865ae4ed82e240b555c059b36c975300a2ac0efbd9ccf8c4df7f1d54035a`. Export unchanged after producer completion and both native sessions.
- Initial independent class mismatch was debug metadata: javac default omits local variables, Gradle includes them. Recompiling with matching`-g`gives101exact matches; no runtime fix needed.
- First packaged launch ended during client asset download, cause unproven; no game pass. Dedicated server retry excludes irrelevant downloadAssets. Second rejected launch used auditor-selected legacy`deepprimal:prototype`, which deliberately has vanilla NoiseBasedChunkGenerator; engine fixture cast fails. That failed world/crash and logs are retained. Corrected independent fresh world uses`minecraft:normal`, selecting the required built-in V7 pack, and both final sessions pass. These are retained harness/environment failures, not hidden successful runs or demonstrated production engine regressions.

**Not run:** Windows execution/owner8060S, Vulkan or OpenGL client/visual/shutdown-resource checks, fresh timing benchmarks, cross-platform or multiplayer/travel/storm/drought soak, unpromoted staged variants. Historical software-GPU evidence stays historical. No stable labeling, CurseForge or GitHub Release publication. Builder source/work state remains untouched. Only unique per-audit report files may be published.


Saved development JAR: [download](https://chatgpt.com/library?file_id=libfile_29775211fc1c8191ab22cff77f75db50&account_id=personal). Main was still the pinned SHA on the final pre-publication re-read; this report-only commit adds no runtime code and changes no builder state.
