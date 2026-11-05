# Deep Primal dev114 — opt-in V8 relief V7 join-window correction

## Finding and implementation

Current main at the start of this scoped cycle: `57e484f105f07bcdfe2b114810ba10918bc41f75`.
Consumed the latest completed independent report
`docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md`,
pinned at `c57d2924562a7dd35eed007a164205953bb13061`.
Its outstanding V5 DensityGraph VM/Java25/native gates remain open; the
independent score remains the carried 50/100, not a new grade.

The existing V6 join window was effectively zero throughout the intended
foothill/plateau interior: the plateau entry begins at continentalness .12,
while the complementary coastal-release window is already zero at .14.
For example, V6 at continentalness .20, .35, .50, and .65 is exactly zero.
Do **not** silently modify V6: its complete graph may already exist in saved
V8 snapshots. Instead, the new explicit `V8ReliefJoinV7` uses a continuous
foothill entry (.10 to .34) and highland exit (.56 to .80). The resulting
window is 1.0 at .35–.56, bounded [0,1], and zero at <=.10 or >=.80.
Its signed absolute-coordinate noise proposal is bounded to +/-7 blocks.

`TerrainShapeV8.definitionReliefV7` feeds the V7 join into the existing
coast-protecting `SEA_SIDE_BLEND` **before** the unchanged canyon incision
and density field. `V8GeneratorSnapshot.newReliefV7` explicitly compiles
the graph before authoring, then persists the entire definition in the
existing versioned V8 profile. V1–V7 geography, older V8 graphs, V6 nodes,
physical water quantities, player edits, renderer files, and native generator
registration are not modified. No existing save is silently migrated.

Added mandatory `V8ReliefV7GraphChecks` to `CoreChecks` for real
DensityGraph VM budget/finite values, signed column/coastal/canyon parity,
changed interior geometry, reversed/parallel chunk parity, and the frozen
V6 control. **This full VM gate was added but not executed here.**

## Executed focused evidence

- Linux OpenJDK `javac 21.0.12.1`, `--release 21 -Xlint:all -Werror`;
  `java -ea -Xverify:all` with isolated API doubles: **57,317 assertions
  passed**. Production V7 helper Git blob `0c72f24a62d4c94544f64045ae492cdcd3428f1a`
  and frozen V6 helper `7d9144fc9d9c342a1c2e933a99143a7321b3a595`
  matched the local compiled source bytes exactly.
- Python 3.13.5 source-identity-pinned join-window regression:
  **37,316 assertions each** in normal and `python3 -OO` runs.
  Reproduce with `python3 tools/test_v8_relief_v7_window.py`.
- Negative control: an independently altered V7 highland band (.12 to .14)
  was rejected in both normal and Python `-OO` runs by the pinned source
  identity; after updating the isolated mutant's expected identity, the
  **behavioral** guard also rejected it with
  `V7 join not broader than frozen V6`. This is a Python source-window
  mutation control, not a compiled DensityGraph VM mutation.
- Post-commit GitHub re-fetch checked the actual committed V7/V6 helper,
  V7 graph wiring, saved factory, mandatory CoreChecks entry, signed-zero
  correction in the VM fixture, and `gradle.properties` version.
- V6 is deliberately frozen; V7 is explicit new-world authoring only.
  The new source version is `0.0.1-alpha.29-dev.114-relief-v7-window.1`.

## Limits and next gates

The Java21 component test uses a DensityGraph API double; it does **not**
compile or run the real DensityGraph VM, Gradle 9.2.1, Java25, NeoForge,
Minecraft chunk generation, native save/reopen/edit, signed native heightmaps,
OpenGL/Vulkan, performance transects or the finite-water ledger. The owner
retains full builds/JAR publication; no new qualified or playable JAR was
built. Dev53 remains the last owner-confirmed playable artifact.

Next: owner executes V5/V7 real VM CoreChecks with Java25; then native
signed-column/chunk/heightmap and old/new-generation border tests, and
durable finite-water authority/ledger integration. Do not mark terrain
blending or water complete. Root ENGINE-WORK-STATE was not rewritten:
the complete pinned Python validator could not be run without a full checkout.
The previous terrain scoped lease was released; the foreground water
claim is expired, not active coordination proof.
