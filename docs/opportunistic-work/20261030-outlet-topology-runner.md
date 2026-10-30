# Opportunistic worker: outlet-topology validation repair

Reviewed source: fe797d88935cc5d4516c65878bc61b9189a46804. Validated/pushed correction: 9838b2ed0e869112d986231cdf62e712b95d5d4e.
Version remains 0.0.1-alpha.29-dev.88-vegetation-receipts.1 (test tooling only; no production version bump or new binary).

Requirement: docs/WATER-BUDGET-REQUIREMENTS.md, “Water accounting and source delivery” item 5, “Ocean recycling and stable average sea level” and “Open implementation and acceptance requirements” branch-split/restart checks. These require rejecting unverified ocean drainage and duplicated routed runoff; the existing topology fixture exercises both but its runner accepted infrastructure failure as rejection.

## Coordination
Inspected enabled engine, renderer, independent auditor, review/fix and this worker: full prompts and latest available run/results. Engine dev91 finished at fe797d8; renderer jam44 finished at a739001; latest independent audit was Dev90 at 655ab648; review task corrected vegetation receipts at fb88f7c. Recorded status is not liveness proof. No other visible foreground task uses this scope. External foreground/live process coordination unavailable.
Read root engine state, terrain/water/graphics scoped states, main-writer lease, recent main commits, prior worker state/handoff and actual roadmap/code. Root/tools/tools-tests/docs AGENTS.md reads returned 404; no applicable instructions found there. Terrain/water stale claims preserved; renderer and engine scopes untouched. Previous worker state was released, last delivered JAR null.
Published bounded claim via aab930a6ae761ff66e6b161a52acfc5774f50855, then extended it only to the previously unclaimed topology Java fixture. No measurements/benchmarks or shared caches were mutated. All work used run001 resources.

## Reproduction and fix
P2: tools/test_watershed_outlet_topology.py previously accepted any nonzero mutant exit and zero-exit positive without a pass marker. New real-main-flow regression failed in eight cases against original source: missing JVM class, signal, wrong exit, wrong fixture/exception, stdout/misleading AssertionError, and empty positive.
Require compile success, positive marker, and negative exit 1 with Java AssertionError header and intended fixture stack frame. Genuine production invariant rejection from a valid fixture input is converted by the Java fixture into AssertionError with cause; launcher/linker failures are not converted. No production behavior changed.
Self-review: compile errors still abort before launch; invalid evidence raises before writing a success receipt; no Python assert dependencies; scoped diffs only. All three committed files fetched and matched tested contents.

## Validation
- python3 tools/test_watershed_outlet_runner_failures.py: four tests pass, including eight negative subcases.
- python3 -OO tools/test_watershed_outlet_runner_failures.py: four tests pass.
- JAVA_HOME=<verified isolated JDK25> python3 tools/test_watershed_outlet_topology.py --evidence results/topology.json: 18,111 positive assertions, three compiled mutants rejected.
- Same with Python -OO: 18,111 positive assertions, three mutants rejected.
- Actual Temurin java/javac 25.0.5+8; javac --release21 -Xlint:all -Werror, java -ea -Xverify:all. This is dependency-free Java21-targeted component validation executed on JDK25, not Minecraft.
- Corrected source SHA 9838b2ed0e869112d986231cdf62e712b95d5d4e; machine-readable receipts adjacent under 20261030-outlet-topology-evidence.

## Build/delivery and continuation
Attempted supported wrapper build using the root build/settings/properties and wrapper from the reviewed snapshot; these were unchanged by the test-only correction. Bootstrap failed: java.net.SocketException Network is unreachable downloading Gradle9.2.1. Restored existing distribution into this worker's private cache. Wrapper --version then verified Gradle9.2.1/JVM25. Offline --no-daemon build failed at settings.gradle:8: Foojay resolver convention 1.0.0 missing. Complete authenticated source checkout and NeoForge dependencies also remain unavailable; the partial source assembly is not a completed checkout/build.
No full Java25/NeoForge compilation, installable JAR, archive/native payload checks, saved-world reopening, Windows/Vulkan/OpenGL/GPU or full-engine grade claimed. No Actions started.
No successful delivered JAR exists for this worker: identity null. No replacement saved; no previous JAR or other role artifact deleted.
Next: restore complete authenticated sources plus plugin/dependency cache, run full supported build and package/native gates from the corrected commit; then deliver a real JAR and apply exact-identity retention. Native ledger/cell integration remains engine-builder work, not resolved by this validation fix.
Scope released at this handoff commit. Task remains enabled.
