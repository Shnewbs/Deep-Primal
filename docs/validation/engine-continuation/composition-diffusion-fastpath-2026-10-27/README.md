# Exact diffusion arithmetic continuation

Base main 501886ce09f64809fdb19dca3d9c6d275e4e4c04; consumed docs/engine-hourly-audits/2026-10-27T0004-685a739/REVIEW.md (pinned685a7390a8371c9b423594888a5a8e2aad744271). External grade carried at5.0/10, not a new complete regrade. Candidate dev.32-composition-candidate.2 is source-only and unpromoted.

## Implementation

Replaces unconditional BigInteger cross products in the previous staged WaterAccount.diffuseWith primitive with a primitive-long path when BOTH nonnegative products fit Long.MAX_VALUE. The difference of two nonnegative in-range signed longs cannot overflow. Positive combined volume fits because valid water is bounded by2^40 per account. Signed Java division truncates toward zero just like the previous arbitrary-precision calculation. Larger valid products retain the exact original fallback.

No water quantity, constituent limit, dry-deposit behavior, alias handling, snapshot schema, geometry, scheduler or runtime integration changes. The fast path avoids constructing arbitrary-precision intermediates for ordinary native cell quantities; no JVM allocation, latency, FPS or target-hardware gain is claimed. Diffusion result objects and other allocations may remain. Both accounts still require server-thread ownership and actual connected contact validation by the future native caller.

## Checks

JDK25 direct compilation and four functional suites pass; complete outputs in evidence/checks.json.
-201134 exact arbitrary-precision oracle cases, including both sides of multiplication overflow boundaries, opposite concentration gradients, native-cell and large-account ranges.201740 constituent calculations meet fast-path predicates and200528 require fallback in this corpus.
-101024 original pair properties,10000 equilibrium repeats.
-200000 mixed-network steps,1575 snapshot reconstructions; exact water/salt/sediment.
-Unchanged aquifer/budget suites:200000 transfers,10000 precipitation splits,180000 weather intervals,186 aquifer reconstructions.

Final JVMs explicitly use a private temporary path and disable shared perf counters after the first run warned that the full system temp location could not create hsperfdata. The first oracle process also exited0, but final recorded checks are clean reruns. These are functional checks, not a full-core suite or native/game persistence test.

Reproduce: compile stage/WaterAccount.java and evidence/ExactDiffusionChecks.java with JDK25 and run ExactDiffusionChecks. Reuse DiffusionChecks.java, DiffusionNetworkChecks.java and BudgetMain.java from the prior composition-diffusion-2026-10-27 evidence directory. Aquifer.java and WaterBudgetChecks.java are unchanged base-main inputs. Compile all into a fresh private output, then run the four main classes recorded in checks.json.

## Remaining gates and coordination

Native HydrologyRuntime/WaterRuntime/HydrologyData were inspected: live cell contents/default ocean chemistry and sparse saved composition still require a coordinated connected-contact adapter. No partially validated native implementation was promoted. The existing staged source index/cursor repair is also still excluded. Full Gradle/module/package/native qualification remains the priority before any delivery.

Overlay zero available; memory-backed free space about1.1GiB, insufficient for isolated full project/dependencies/outputs. Used only /dev/shm/dp-builder2500 and read own prior fixture/JDK files. No other checkout/cache/output touched, no exclusive benchmark or reclaimed-space claim. Git water and renderer frame-window claims checkpointed; personal context and current automation records checked. Auditor recorded in_progress since00:32, latest completed report is the consumed0004 report; this does not prove live execution. Original other-chat current ownership unavailable. Guarded main publication preserves concurrent work.

No new game version/JAR/GitHub release or Library artifact. No full Gradle, native Minecraft/save/reopen/shutdown, actual GL/Vulkan, Windows/AMD8060S, multiplayer/soak or named-hardware latency acceptance. Source-only candidate must remain staged. Next bounded native contact integration, full combined qualification, then conservative loaded/unloaded ownership. Lease released; builder acceptance remains incomplete.

Concurrent renderer transition-interval claim e3cb72a was preserved during final rebase; no production files overlap.
