# Dev74 — V8 mixed-corner shoreline routing coherence (opt-in)

**Source-only checkpoint.** Reviewed the independent `2026-10-29T0735-529bbe9` audit at pinned source `529bbe95f850ef561f195f99b7827ee53e94f50c`. Its new finding: production `WatershedRegion.sample` uses OR over four connected ocean nodes to assign sea-stage water, whereas V8 v6 routing metadata uses an all-four graph-mask marine check. A 2×2 partial coastal mask therefore produces stage 96 with water kind LAKE, not OCEAN.

## Actual change

`V8RoutedWaterSource` now offers **explicit, opt-in saved routing policy v7**, `authorShorelineAligned` and a domain-separated SHA-256 fingerprint. Its `plan` water-kind classification for a sea-stage cell uses `Region.oceanAt`, the same four-corner **OR** of connected, ocean-domain-admitted nodes that provides the routed sea-stage result. The existing v6 behavior and saved fingerprints are unchanged; saved V8 r5 graph, climate binding, prior policies v1-v6 and legacy V1-V7 geography do not silently migrate.

Version seven still depends upon a correctly authored `ocean_domain` graph. This is no globally connected ocean algorithm. It neither changes routed stage, ground geometry nor physical volume/constituent accounting. It does not activate V8 native gameplay.

## Focused verification

- Source ZIP from committed dev73 identity, Git objects verified before editing: previous route blob `2a19635c511403baf6eea40e60f084aa7abc146c` and previous test blob `dff4ecbe847124bb57cf81516b9c3fde75e5fe5c`.
- Modified route source blob `a4128096a81449b6455f397c0d2fc25b228dbf25`; modified test blob `42760f4d1d5545f109a1a54171a5e2f296fd1ec4`. Both GitHub blob identities matched byte-for-byte locally tested sources.
- OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`; **19/19 isolated source/API-double suites and 21,013,527 assertions/checks**. `V8RoutedWaterSourceChecks`: **5,100** checks.
- Four positive/negative signed-coast seam masks exercise mixed-corner v6 LAKE versus opt-in v7 OCEAN with equal stage/ground, saved-profile modeled reopen, mask-edited source identity, inland exclusion, reverse/parallel query order and bounded caches.
- Two separately compiled broken source variants were rejected by the new coastal assertion: retaining old all-four label, and replacing `Region.oceanAt` OR with AND.
- The positive suite includes API doubles for Mojang/Minecraft and **production WatershedRegion is not compiled with its real dependencies**. The original `tools/test_ecotone_blend.py` runner was absent from this partial portable workspace and not rerun; the existing isolated `EcotoneSurfaceFieldChecks` passed.

See `results.json` and the portable `DeepPrimal_Dev74_ShorelineCoherence_SourceEvidence.zip` from this cycle for complete 19-suite output and two mutation traces. No Gradle 9.2.1/Java25/full NeoForge, native saved-world reopening, Minecraft gameplay, GPU, finite-water lifecycle, cross-region discharge or performance acceptance has been claimed. No JAR was created; owner retains build responsibility.

Independent baseline carried unchanged at **50/100 (5.0/10)**. Next: true global marine/outlet topology, cross-region discharge and lake spill, native mixed-coast/reopen and conservation testing, continuous terrain blending/material transitions.
