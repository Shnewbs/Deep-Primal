# Staged source-index lifecycle repairs

Production remains alpha.29-dev.31 at `a1583a508b0d67130d387bc018defd5758acd496`. These files supersede the prior staged incremental-index candidate only; no new JAR, version promotion or water-engine acceptance is claimed.

Consumed audit: [2026-10-26T1834-8665e07](../../../engine-hourly-audits/2026-10-26T1834-8665e07/REVIEW.md), pinned `8665e07be705c9cf6470adf3d6a6d325cea094fc`, published in `094033d41147693e0ba3881026d009ca37fc895b`. All ten scores remain carried at 50/100, average 5.0/10. The audit identifies source-index lifecycle/native qualification and bounded water work as open; staged work receives no shipped-feature credit.

## Changes and reproduced failures

The previous staged index removed production's whole-index cleanup but missed the regular visit path that drops a chunk when `getChunk(FULL, false)` returns no full chunk. Its source entry could consequently remain in the incremental index indefinitely. The revised runtime shares `forgetChunk` between that path and the existing unload event. It removes chunk/source/survey membership and applies the existing regional-cache retention policy. It never requests a chunk load and does not alter persistent groundwater or composition accounts.

A stale `refreshSource` previously removed the key even if a newer cursor had already registered a source there. `StreamSourceIndex.remove(key, expectedCursor)` now invalidates only the matching cursor identity. A stale refresh removes its own stale entry but preserves any replacement generation, including when the old cursor is dry.

Both failures reproduce against the exact previous staged source. The missing-FULL failure uses the extracted actual regular-visit loop with explicit native/clock doubles; the generation failure invokes the complete actual staged runtime through reflection. Neither is a live Minecraft reproduction.

## Fresh qualification

- Complete old/new HydrologyRuntime and index API compilation against retained Minecraft/NeoForge dependencies, in separate clean outputs with Java 25. Final candidate source/class hashes are in `runtime-results.json`.
- Actual-runtime lifecycle fixture: **5,512 checks**, including 1,000 cursor replacements at signed-extreme chunk keys, active and dry stale refreshes, 512 removals, repeated removal and final empty indexes.
- Actual extracted regular-visit loop: **144 cases**, combining 1/2/17/64/128/8192 entries, quotas 0/1/2/16/64/128, mixed available/unavailable chunks and expired/open admission. Missing-FULL entries are removed; loaded entries survive; no forced load; expired admission performs no work.
- Existing incremental-order oracle freshly passes **20,000 randomized mutations / 20,008 checks**, stable ties, replacement identity, invalid-height rejection, unload and caller-limited prefixes.
- Existing BigInteger allowance fixture freshly passes **1,445 cases against the candidate HydrologyRuntime**. This is a standalone JVM execution of the fixture, not a server/native session.
- Five physical method bodies (`replenishStream`, `streamAllowance`, `visit`, `precipitation`, `write`) match production exactly. This check does not prove full scheduling equivalence or world-level conservation.

The standalone loop runner accepts explicit `--baseline`, `--stage`, `--jdk` and `--output` paths. The runtime runner accepts `--baseline-stage`, `--stage`, `--jdk`, `--classpath-file` and `--output`; the classpath must contain resolved current Minecraft/NeoForge/product dependencies. Use independent outputs. Baseline failures are required by both runners and are not passing product checks. Candidate process failures always raise exceptions, independent of Python assertion optimization.

Fresh complete `CoreChecks` also passes after compiling all **138 current core/fixture sources** into a clean private output. The 138 input hashes match the retained qualified manifest before and after execution; the complete Git comparison from production `a1583a5` through read-main `094033d` contains no non-documentation changes. The accepted core run exits zero in about 309 seconds with a 512 MiB heap. This is a fresh full **core** suite, not a full mod/Gradle/native build. See `fresh-core-result.json` and `core-run.txt`.

## Diagnostics, coordination and limits

The first loop-fixture generation used a broad placeholder replacement that also expanded its PASS marker, causing compiler syntax errors before execution. `fixture-initial-compile-failure.txt` retains it. Restricting the placeholder to its own line fixes the harness; no runtime assertion or case was removed.

The first fresh core execution failed when LodStoreBudgetChecks attempted to create a temporary directory on the full disk. Its log/result remain separate. The retry gives the JVM a unique memory-backed temporary directory and working directory; no production/test source was changed for that retry.

Overlay free space was zero at initial and repeated preflights. The shared-memory filesystem had only approximately 276–464 MiB available at sampled preflights; outputs for this run are under 3 MiB. Full Gradle/module/package/export and packaged native/save/reopen/shutdown qualification were not started because sufficient independent build capacity was unavailable. Do not replace those gates with the standalone results. No archive CRC, fresh packaged class/payload/private-fixture validation, Windows/AMD8060S, actual OpenGL/Vulkan visuals, latency benchmark, multiplayer or soak acceptance is claimed. No new release or Library JAR was created; retained artifacts were not relabeled or resaved.

The prior builder and water leases were checkpointed. The independent auditor run completed at 18:55 UTC; the renderer byte-accounting claim was checkpointed and its measurements completed. Personal Context lookup did not establish fresh direct ownership from the originally named other-engine chat; that evidence remains unavailable. This run holds only its bounded root builder lease and uses unique outputs, with read-only retained builder dependencies. No other role's checkout, cache, output or process was changed. Tests are functional checks with no exclusive timing reservation or comparative performance claim.

Next: restore build capacity and qualify the combined index using full Gradle/core/module/package and actual source quota, partial survey, callback reentrancy, loaded/unloaded, weather/save/reopen/shutdown cases before promotion. Verify tie-order behavior over real dry/re-add and generation transitions. Then continue persistent single-owner water transfers, bounded catch-up, live water/LOD and registered V8 acceptance. Frozen geography and approved art remain untouched; R57/R60/R61/R62 remain staged. The independent >8.5 grade and complete water gates remain open.
