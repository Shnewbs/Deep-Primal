# Dev62 — opt-in V8 sea-side relief guard (source-only)

Source version: `0.0.1-alpha.29-dev.62-coastal-proof.1`. Consumed independent audit `docs/engine-hourly-audits/2026-10-28T1930-2a341f9/REVIEW.md` pinned to `2a341f90b6aaff2a253a50f8f792b24a47cc80fc`. That audit reproduced two V3 shoulder height/sea-plane changes (seed 0 at x=-2006,z=-2282, and seed 922337203685 at x=9459,z=9273); no native ocean-mask conflict was established. The historical complete external grade is still **50/100 (5.0/10)**.

## Implemented source

- `TerrainShapeV8.definitionReliefV4` is explicit opt-in; `V8GeneratorSnapshot.newReliefV4` stores its complete density graph in the existing eight-field V8 profile revision 3. V1–V3 saved graph digests, frozen V1–V7 geography and owner/player edits are left unchanged.
- A new bounded `DensityGraph.Op.SEA_SIDE_BLEND` uses historical pre-canyon height, candidate V2 pre-canyon height and shared canyon incision. Its serialized parameters are sea plane and positive coast width. For signed historical/proposed final sea distances `d,p`, near-shore offset is `d * (abs(p)+w)/(abs(d)+w)`, with continuous safe release toward `p` only when the proposal is on the same sea side. Thus the historical height channel's side is preserved without destroying safe new inland relief. The canyon input remains unchanged and the operation runs before hydrology.
- The graph's scalar and thread-owned sampler evaluators, parameter checks, compile bounds, and conservative `DensityVolumeBounds` interval evaluator handle the new opcode. No physical water amount, source ledger, ocean stage, chemistry, renderer or already generated chunk is changed.

## Executed validation

Linux OpenJDK/javac **21.0.12.1**, strict `javac --release 21 -Xlint:all -Werror`, followed by **14 separate `java -ea -Xverify:all` suites: 20,929,784 assertions/checks passed**. This includes 3,907,136 new V4 checks. The V4 sampled corpus has **148,998 columns across four seeds**, **12 prior V3 height-side flips**, **zero V4 flips**, **25,731 changed inland/shoulder heights**, 123,267 bit-identical V1 protected coast columns, and both auditor witnesses. V1/V2/V3 frozen graph SHA-256 checks pass for floating enabled/disabled. V4 graph is 82/93 nodes, depth 22, evaluation cost 480/493. Four modeled snapshot reopen cases and full signed-column/voxel parity pass. An independently compiled mutant with the wrong signed-distance numerator exits 1 with an assertion failure, as intended.

Reproduction: compile the affected source classes and `tools/tests/V8ReliefV4CoastalChecks.java` against a complete project checkout (or the owner's retained Java21 source/API-double evidence bundle). This test is **not** added to the default `CoreChecks` launcher. The source bundle contains isolated stubs, all 14 suite logs, compiled-mutant failure and graph baseline fingerprints. The full source-evidence ZIP was prepared locally; no JAR is claimed.

## Still open / limits

The isolated workspace lacked the complete checkout, root work-state Python preflight, JDK25 archive and full Gradle dependencies. All 14 suites used Java21 and isolated Minecraft/Mojang API doubles—not a Java25/NeoForge build, real codec save/reopen, actual ocean-mask or native fluid check. There were **no** Minecraft/Vulkan/OpenGL/Windows AMD8060S tests or frame/tick/memory measurements. This is sampled/proved *height-channel* preservation, not certified seamless coast/river/waterfall generation. The authoring path is not native registered. No new JAR/release was produced under the owner's October 27 build-ownership override; dev53 is the last owner-confirmed playable build.

Next: bind authoritative persisted climate, ocean mask and routed hydrology to V8 before native activation, test old/new chunk borders, exact finite water/constituent unload/reopen transfers and real renderer pixel continuity. The independent grade threshold remains strictly greater than 8.5, **plus** all water-engine gates.
