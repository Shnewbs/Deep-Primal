# V8 explicit saved ecotone policy — 2026-10-28

This is a source-only builder change after dev54 EcotoneBlend. At integration parent c0d1100315820ebe9d06851c401866217b622b68 the independent renderer had committed additional graphics settings; untouched and preserved. Prior complete external overall score is 50/100 (5.0/10); this is not an audit regrade.

## Changes
- `V8GeneratorSnapshot` retains the exact original six-field revision 1 encoder and old six-argument constructor. Old/missing blend policy always means EcotoneBlend.Widths.HARD. No implicit migration.
- Revision 2 is opt-in via `withBlendPolicy` or seven-argument constructor. The seven-field snapshot includes required `blend`: nested version 1 policy with five exact widths: temperature, rainfall, height, slope and patch. No caller is automatically switched to revision 2.
- Strict codecs reject missing/unknown/duplicate root keys, invalid policy fields, nonfinite/negative/out-of-range widths, unsupported versions and injected policy on revision 1. Existing signed 64-bit decimal seed codec and graph/ground remain unchanged.
- The policy is PERSISTENCE ONLY. No native material blending, vegetation, native generator registration or relief deformation is activated. Frozen V1–V7 geography, existing V8 saves, owner edits, water quantity/chemistry, renderer and approved art remain unchanged.

## Focused isolated checks, Java 21, not native
Source under test: pre-existing `V8GeneratorSnapshot.java` Git blob `a97d93b143104247566442b63d285545c894eab8`, modified snapshot tested source Git blob `ea6fed7b455ed831c06654508bbcd551361ea527`; new `V8BlendPolicy.java` tested source Git blob `9caaa2e566b1b1446bc9182205cd014db61dcb28`. The testing fixture compiled these production sources with the previously archived dev50 Mojang/Minecraft API doubles, plus an API double representing the unchanged EcotoneBlend.Widths interface and validation ranges. Java 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.

- New `V8BlendPolicyChecks`: **53,316 assertions, 4,096 exact revision-2 profile roundtrips, 30 intentionally invalid inputs rejected**. Legacy six-key roundtrips and hard behavior were checked; malformed widths, absent/unknown fields, version swaps and signed 64-bit seed limits rejected.
- Existing `V8SnapshotRootFieldChecks`: **14,392 assertions, 4,122 malformed roots rejected**, 2,048 legacy seed roundtrips.
- Existing `V8GeneratorSnapshotChecks`: **594,901 assertions**; the first candidate failed the original six-argument version rejection, so the compatibility constructor was tightened and the full test reran PASS.
- Earlier V8 planned ground / section bridge / base-column suites reran: 1,235,414 / 11,047,019 / 874,642 assertions; all PASS. Test-only stubs are excluded from runtime source.
- The Java 25 toolchain/archive was not present in this run's workspace. Full Gradle/NeoForge, real Mojang JSON/NBT, persisted native world save/reopen, visual GPU transects and Windows AMD8060S benchmarks NOT RUN. The owner's October 27 instruction retains build and JAR ownership; no JAR/release claimed.

## Next steps
Consume the saved continuous weights in deterministic spatially correlated ground/snow/soil and vegetation transitions for explicit revision-2 profiles only, then implement versioned relief blending BEFORE hydrology. Preserve cliffs, layers, river incision/outlets. Add real saved-profile reopen/edits, chunk-border legacy/new boundaries, exact height/column parity, multi-seed visual transects and bounded CPU/memory checks. Alongside that, repair cross-chunk finite-water persistent transaction ownership and run genuine GPU water-surface/rock-contact continuity and long-soak conservation tests under water ownership.

## Coordination
Scope: `V8GeneratorSnapshot.java`, `V8BlendPolicy.java`, `gradle.properties`, this evidence path, scoped lease. Root work-state validator was not run on a complete checkout, so root state was not claimed or changed. Other renderer, water and auditor files/outputs untouched. Lease released at the checkpoint.
