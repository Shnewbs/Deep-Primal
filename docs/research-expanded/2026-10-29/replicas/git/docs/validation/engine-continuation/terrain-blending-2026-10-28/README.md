# Expanded replica — docs/validation/engine-continuation/terrain-blending-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/terrain-blending-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/terrain-blending-2026-10-28/README.md |
| Preserved source SHA-256 | `3778c1876ac0b7369d56c1ad42945190b374857d59e2c9153fbf1fa046451064` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev54 EcotoneBlend with fixed normalized weights, quintic transitions, hard-policy compatibility and separate hydrological domains in the ordinary core source set. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the component is not called by active worldgen, and its Java 21 portability check is not the project's Java 25 or native qualification. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a blend-output contract distinguishing continuous properties, deterministic palette choice and categorical hydrological ownership without mutating terrain height. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Retain hard-classifier and smoothing negative controls, compare repeated fingerprints and domain isolation, then bind saved policies and native materialization through separately identified integration stages. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev54 — weighted ecotone foundation

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `results.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/terrain-blending-2026-10-28/results.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev54 — weighted ecotone foundation

Base: `49bef407845fb83ad563ceb40754144173449ebf`, the completed dev.53 callback
checkpoint. Owner supplied the current build and requested terrain blending.
Read independent audit `docs/engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md`:
biome coherence/blending remains a carried 4/10 category with no native weighted
ecotone evidence. This checkpoint does not regrade it or the 5.0/10 overall score.

## Actual source changes

- New `engine-core/.../biome/EcotoneBlend.java`: fixed-size normalized biome
  weights, continuous quintic threshold transitions, exact hard-policy control,
  separate hydrological domains, immutable output, deterministic palette choice
  and continuous unit-property mixing. No world IO, random state or height/water
  mutation. New public code is in the ordinary core source set but not called by
  active worldgen.
- New standalone `EcotoneBlendChecks` and `tools/test_ecotone_blend.py`.
  The runner compiles only the two component files, requires an unchanged legacy
  Landscape blob in a complete checkout, and offers explicitly recorded isolated
  mode. It runs twice, compares exact fingerprints, then requires two compiled
  broken implementations to fail. No Gradle task is claimed to run these checks
  automatically; invoke the runner below.
- `gradle.properties` source version becomes `0.0.1-alpha.29-dev.54-ecotone.1`.
  Existing generation/rendering/water/materializer code and presets are unchanged.

## Fresh executed checks

Linux OpenJDK/javac **21.0.12.1**, `--release 21 -Xlint:all -Werror`, then
`java -ea -Xverify:all`. Full receipts are in [results.json](results.json).
Each accepted positive process passes **7,333,288 assertions** covering:

| Corpus | Count per run |
| --- | ---: |
| Exact hard classifier versus frozen independent Landscape decision oracle | 46,950 |
| Normalized/finite/domain-isolated soft distributions, repeated exact outputs | 46,950 |
| Continuity neighborhoods at thresholds and blend-band endpoints | 21,888 |
| Synthetic absolute-coordinate X/Z chunk-halo comparisons | 2,754 |
| Reverse-order/parallel synthetic samples | 16,384 |

All 38 used legacy palette IDs occur in the oracle corpus; reserved IDs 38/39
remain zero. Boundaries include exact equality and adjacent floating-point
values, invalid input, maximal widths, subnormal positive widths and extreme
finite values. Output-array mutation cannot alter the distribution. A fixed
2,000-input/three-domain policy fingerprint is checked.

Two final positive processes have identical output. Both negative controls compile
successfully and fail on assertions: changing a strict legacy comparison to
inclusive fails the hard oracle; disabling smoothing fails continuity. Repeating
the runner under Python `-OO` also passes and rejects both broken controls.

The earlier two container runner captures timed out without final receipts and
are excluded. An interactive retry was rejected before execution because the
container does not support streaming sessions. Direct runs passed; a later
complete instrumented runner and optimized-Python repeat supply the accepted
receipts. A test-only change avoids building failure strings for every passing
assertion; no claim is made about the timeout's root cause or product speed.

## Limits and next integration

These are dependency-free source tests, not actual Minecraft chunks. The legacy
oracle is copied from the inspected immutable Landscape classifier, blob
`3c7b31e84c952878671511924188175f2b5181a0`; that full class/engine was not compiled
locally. Synthetic halo/order tests do not establish native worldgen seams.
No Java25/Gradle/NeoForge build, JAR, Minecraft startup, native save/reopen,
waterfall pixels, GPU execution or performance benchmark was performed.

This does not yet change terrain in the owner's world. Preserve legacy and V8
snapshots; integrate a saved versioned blend policy and its native surface/
vegetation consumers, then landform-density joins and owner-side validation as
listed in [TERRAIN-BLENDING.md](../../../TERRAIN-BLENDING.md).

## Reproduction from a full checkout

```sh
python tools/test_ecotone_blend.py --output /absolute/path/to/fresh-ecotone-evidence
```

The runner uses JAVA_HOME when provided and never changes global Java settings.
Its Java21 release target is a component portability check, not certification of
the project's Java25 build. For a two-file source snapshot only, pass `--isolated`;
the receipt then explicitly records that the full legacy source was not checked.


<!-- END PRESERVED SOURCE -->
