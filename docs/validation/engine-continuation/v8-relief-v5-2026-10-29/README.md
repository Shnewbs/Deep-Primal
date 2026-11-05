# Dev92: explicit V8 relief-V5 foothill/plateau joins

## Provenance and scope
Source-only implementation continuing the existing engine builder. Read root builder/auditor, handoff, active audit, terrain, water and original rubric documents at main, and independent `docs/engine-hourly-audits/2026-10-29T2330-655ab648/REVIEW.md` pinned to `655ab648212c3d378d638ea0a99ca1c42f2bf4ec`. Its top P1 gaps — native water ownership, certified marine roots and owner full-build/GPU tests — remain open after the Dev91 world-witness API. The bounded renderer claim concerns LOD only; no renderer or native hydrology files were edited. Expired foreground water/terrain claims were not interpreted as active ownership. Root work-state was not edited: full pinned Python validator was unavailable in the partial local checkout.

## Actual code
- `V8ReliefJoinV5.java` is a **real engine-core graph-builder component**, not a second ecotone classifier. Fourteen frozen density nodes combine absolute-coordinate 1,200-block warping and 640-block relief variation through smooth foothill/mountain/plateau overlap. New pre-hydrology height perturbation is bounded by 11 blocks. The graph is independent of liquid volume, water height, biome winners, block edits and renderers.
- `TerrainShapeV8.definitionReliefV5(boolean)` consumes this join **before** the existing coast-side-preserving `SEA_SIDE_BLEND`, canyon incision and density conversion. The saved graph explicitly includes `relief_recipe_revision=5`. V1–V4 recipe source paths and the canyon channel remain intact.
- `V8GeneratorSnapshot.newReliefV5(...)` creates an opt-in new V8-r3 snapshot containing this complete persisted graph. Old V8-r1–r6 snapshots, V1–V7 terrain and the Dev53 prewrite callback remain unchanged; no default/native registration occurs. `gradle.properties` is bumped to source checkpoint `0.0.1-alpha.29-dev.92-relief-joins.1`.

## Reproducible checks
Exact Git production join blob `b193fa30f25b6ace3dfa8be077f3137f17aa3d2e` matched the compiled local Java file. Using OpenJDK **21.0.12.1**, strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`, `python3 tools/test_v8_relief_v5.py` and `python3 -OO tools/test_v8_relief_v5.py` each passed two Java positives and rejected two independently compiled mutations with executed Java AssertionErrors. Four positives × **212,735 assertions** = **850,940** source/API-double assertions; **4/4** broken controls rejected. Positive receipts matched exactly. Covered deterministic signed-column sample order, bounded amplitude, unique graph references, smooth synthetic join fields and disabled-band mutations. This check uses an explicitly synthetic noise and graph API double; it does **not** execute the actual DensityGraph VM.

The post-patch `TerrainShapeV8.java` blob `2069a0c2455e94a6b4d08edd399640c1e8662f9b` was mechanically reversed through six unique V5 edits, matching previous `726edf77ed103c16a0f94c2a6dd45257763a4319` byte-for-byte. This establishes unchanged historical source text, not frozen native output replay.

`tools/tests/V8ReliefV5JoinChecks.java` provides a separate full-core test intended for a complete checkout: graph depth/cost, historic sea-side identity, exact canyon channel, signed-coordinate seams, reversed/parallel chunk occupancy and multi-seed height parity. **This full-core test was added but not run here** (no full pinned checkout).

## Remaining acceptance
No Java25/Gradle9.2.1 build, Minecraft native chunk generation or save-reopen, owner Windows GPU, physical water conservation, cross-generation chunk border resolution, broad visual transects, native old graph replay or CPU/memory benchmark occurred. V8 is not the active game generator; relief V5 is not a completed terrain-blending feature. Previous independent whole-engine grade remains **50/100 (5.0/10)** with target strictly >8.5 plus all water gates.

Next: real-core V5 checks and native V8-r3 saved-policy owner testing; integrate Dev91's world witness with actual authoritative native finite-water cells and certified ocean-root transfers without duplicate loaded/unloaded debits. Do not modify renderer-owned resources or silently migrate old saves.
