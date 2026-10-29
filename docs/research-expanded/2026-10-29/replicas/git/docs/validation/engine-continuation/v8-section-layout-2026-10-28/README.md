# Expanded replica — docs/validation/engine-continuation/v8-section-layout-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-section-layout-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-section-layout-2026-10-28/README.md |
| Preserved source SHA-256 | `fc0214fe5ca987cd782b5da1fe805461df9e146f36bd4e357ebc6283d33de128` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev45 canonical projection from density column bits to 16-cubed native section order, with floor-divided negative section identity and immutable packed exports. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that raw bitmap payload excludes Java metadata, and a correct layout projection does not activate or qualify the native section writer. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a binary layout contract specifying axis order, signed division, partial coverage, empty sections and defensive ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Transpose X/Z, truncate negative division, alter the upper bound and mutate returned words as negative controls, then require the real writer to preserve every occupancy bit. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev45: canonical V8 chunk-to-native-section occupancy layout

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev45: canonical V8 chunk-to-native-section occupancy layout

Version: `0.0.1-alpha.29-dev.45-v8sections.1`. **Source-only, opt-in V8 infrastructure.** This is not a Minecraft/JAR/GPU release.

## Independent audit consumed and coordination

Read `docs/engine-hourly-audits/2026-10-28T0330-b14f8ba/REVIEW.md`, pinned at `b14f8ba6eaf279212a0b96098ae493d6a34c1440`, with the carried externally verified grade **50/100 (5.0/10)**. The audit found exact V8 density-bit parity but no native block/section layout, save/reopen or measured performance. Builder claimed a bounded main writer lease, checked the separate renderer/water scopes and existing timing reservation, and edited no water/renderer/LOD/world assets. Personal context offered historical ownership but no reliable live cross-chat process state. Native builds remain owner-operated per October 27 override.

## Actual code

`DensitySectionLayout.project(DensityChunk)` transforms the opt-in V8 density chunk's z/x/y column-major BitSet into native **16 × 16 × 16** section-palette bits at `(localY << 8) | (localZ << 4) | localX`. It retains exact negative-Y floor-division section identity, partial top/bottom coverage, deterministic Y/Z/X iteration, independent immutable packed-word export and explicit empty sections. The projected words can be consumed later by a native section writer; there is no Minecraft class or registry dependency and the code does not activate V8 world generation.

The source occupancy is traversed exactly once, without querying/resampling terrain or forcing chunks. At most **129 section records** intersect a valid 2048-high nonaligned chunk and nonempty bitmaps each use **512 raw bytes**, excluding Java objects/metadata. Empty sections use a private shared zero buffer until the caller asks for a defensive copy. All certified/planned solid and air cells retain the original exact `DensityChunk` occupancy and heightmap semantics. No per-frame/tick speedup is claimed.

## Fresh executed checks

Linux x86_64 Java/javac **21.0.12.1**, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`, compiled against actual Git-matched production `DensityGraph`, `DensityChunk`, `DensityVolumePlan/Bounds`, `SeededNoise3D`, `NoiseSpec`, `DensityField` and `TerrainShapeV8`:
```
DENSITY_SECTION_LAYOUT_PASS assertions=3792966 nativePositions=2043904 sections=499 emptySections=186 certifiedCells=1452800 productGraph=true
```
Tests cover negative/nonaligned Y (-33,-17,-1,0 and more), section seams/partial endcaps, actual authored V8 floating and ordinary density, irregular caves, positive/noise/all-solid/all-air, world coordinate extremes, up to 2048 vertical cells, native word order and immutable export, exact versus planned section parity, and independent top-exclusive heightmap reconstitution.

Four deliberately defective compiled variants were **all rejected**: X/Z packing transposition, truncating negative section division, upper-bound off-by-one, and mutable packed-word leak. An initial test-only mutation string assumed a unique X/Z expression; the fixture was corrected to mutate both production occurrences. This was not a production defect and the final four compiled variants each failed the intended regression.

Git object identities for new code/test: `669a9781fb6c7cb78d7de7744b85d26d9b0bf712` and `75ee8114a375cde51c3560d52244fd667ab804f5`. Existing V1-V7 geography, finite water/constituents, saved profiles, player edits, approved art, Themes and distinct rendering modules are unchanged.

## Explicit unrun gates / next implementation

Owner Java25.0.5+8 toolchain/archive is **not mounted** in this run, so no Java25 full Gradle/modules, NeoForge native section writes/lighting/base-height/base-column/saved V8 graph profile, Vulkan/OpenGL, Windows AMD8060S, performance/soak or multiplayer evidence. No full build, JAR, GitHub release, Actions or CurseForge action was attempted. The section layout is a source-only bridge, not an accepted active V8 generator. Next: wire it through an explicitly versioned native V8 generator and saved graph profile **only after** owner Java25/native parity, chunk/topology/lighting/save-reopen controls; profile p50/p95/p99 and allocation. Continue separate water ledger/single-owner unload/temporal cliffs/seasonal exchange and named GPU acceptance. Strict >8.5 overall plus all water gates remain unpassed.


<!-- END PRESERVED SOURCE -->
