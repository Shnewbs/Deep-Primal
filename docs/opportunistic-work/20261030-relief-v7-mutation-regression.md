# Opportunistic V7 relief mutation regression — 2026-10-30

## Requirement and scope

This bounded validation increment supports `docs/TERRAIN-BLENDING.md`,
integration sequence 3-4: versioned landform joins must remain bounded and the
native-result acceptance path needs reproducible checks. It follows dev114's
V7 source-window work without editing its production source or existing runner.

The engine dev114 and renderer jam68 scopes were completed and released. The
recorded foreground water lease expired on 2026-10-28 and is not evidence of
live ownership. Enabled engine, renderer, independent-audit, three-hour review,
and opportunistic scheduler prompts/latest results were inspected. Scheduler
timestamps alone were not treated as activity. No AGENTS.md was present in the
inspected repository paths.

## Change and evidence

Added `tools/test_v8_relief_v7_runner_failures.py`. Its isolated fixture
exercises the committed V7 runner logic and requires:

- the known-good broad window to emit the exact 37,316-assertion receipt;
- an unpinned V7 source mutation to fail source identity;
- the same narrow-window mutation, after intentionally re-pinning its blob, to
  fail the behavioral breadth requirement;
- a missing V6 dependency to fail rather than count as a pass.

Executed with Python 3.12.14:

- `python3 tools/test_v8_relief_v7_runner_failures.py`: 4/4 passed.
- `python3 -OO tools/test_v8_relief_v7_runner_failures.py`: 4/4 passed.

These are source-runner regressions only. They do not execute the real
DensityGraph VM, Java 25, Gradle, NeoForge, Minecraft, saved-world reopen,
water conservation, native rendering, GPU, or performance acceptance.

## Handoff

Next concrete gate remains the owner-side Java25 real VM CoreChecks for V5/V7,
then native signed chunk/heightmap/save-reopen and old/new border acceptance.
No user-visible art or texture changed.
