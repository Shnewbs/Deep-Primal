# Opportunistic worker — water-domain validation repair

Requirement: docs/ENGINE-HOURLY-BUILDS.md, Cycle contract item 5 (meaningful affected checks), and docs/LOD-AND-DRAINAGE-ARCHITECTURE.md, Engine charter and quality gates. Pinned review began at f96d36d2e1f580a491af2deba8bbc90802db5813. Integration parent: 182a35530f83d6bd8ce0708bc8c73672d48e88fd. This report and its source changes share one commit.

## Coordination and ownership
Privately inspected enabled engine builder 6ae0c445a6c4819194f58ed18e077f52 (water/terrain), renderer 6ae0c442344c81918397938c479981d8, independent auditor 6ae0c4335524819182d7cdd153d51931 and three-hour review/fix 6ae33020c72481919cee14382e66f20b: full prompts and latest available results/run records. Consumed Dev88 auditor result and review task's runner-failure findings. Scheduler status alone was not used as liveness proof. Current self run had no earlier result. No foreground task is active in this visible conversation. External foreground liveness remains unavailable.
Read root/work-state, water, terrain and graphics scoped state, main-writer lease, recent commits and renderer jam43 result. Existing stale terrain/water records were preserved. Root AGENTS.md was absent (404); tools directory listing has no AGENTS.md. A separate bounded claim was published at 04ae92ff2c58deca00a0f405ae84c29f1f2ddde0. The engine subsequently claimed a distinct dev90 outlet checkpoint crash-guard scope. This repair touches only its claimed runner, new regression test and dedicated handoff. No active implementation/output/cache was edited; no timed workload was run.

## Confirmed defects and corrections
1. P2: tools/test_v8_water_domain_admission.py accepted every nonzero mutant exit. A mocked launcher error reproduced MUTANTS_REJECTED compiled=3 and a success JSON receipt with no Java assertions. Now requires compile success, exit 1, a real Java AssertionError header and a WaterDomainSourceCheck stack frame. No success receipt is produced for rejected evidence.
2. The runner's snapshot double omitted VEGETATION_VERSION, so compiling actual current V8WaterDomainAdmission failed with cannot find symbol at production line 88. Added the production constant value 6 and an r6 observer parity case alongside r5. This test addition remains Java-execution unverified.

## Checks
- python3 -m unittest discover -s round/tools -p test_v8_water_domain_runner_failures.py -v: 3/3 passed.
- Same command with python3 -OO: 3/3 passed.
- Tests exercise compile failure preventing launch, assertion identity, launcher errors, signals, wrong exit codes, and main-path rejection without success receipt.
- Python syntax compilation passed; final diff self-reviewed.
- Initial Java25 attempt reproduced missing snapshot-double constant.
- Restored owner JDK archive from Library libfile_60dea1c869408191a8df3e77206f39fe into isolated resources. java/javac report Temurin 25.0.5+8; subsequent actual compilation failed inside jdk.internal.jimage.BasicImageReader with newLimit > capacity (123632738 > 122719744). This is a toolchain blocker, not a passing fixture. Normal and optimized runner attempts failed closed.
- Supported pinned wrapper bootstrap: bash gradlew --no-daemon build, isolated GRADLE_USER_HOME. Failed downloading gradle-9.2.1-bin.zip: java.net.SocketException: Network is unreachable. Complete checkout/dependencies unavailable; no full project or module build was performed.
- No native Minecraft, saved-world, Windows, GPU, performance or independent regrade claimed.

## Continuation / artifact retention
No successful JAR delivery exists for this worker on this first round; delivered artifact identity is null. No old JAR was deleted. Other roles' artifacts, sources and toolchains preserved. Full build is still required from this corrected source commit with a working JDK25, complete source/dependencies and Gradle9.2.1. Then run the water-domain positive/mutation suite (including r6), package checks and appropriate native gates before saving/delivering a new JAR.
Scoped claim is released by this commit; keep task enabled. Next concrete step: recover a validated complete Java25 runtime, rerun this fixture, and choose another unclaimed roadmap increment after refreshing claims.

Publication: first guarded update failed while main advanced; refreshed all affected production/runner identities and rebuilt on the newer parent, preserving dev90.
