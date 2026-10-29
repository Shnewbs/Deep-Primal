# Expanded replica — docs/validation/engine-continuation/v8-snapshot-strict-envelope-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-snapshot-strict-envelope-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-snapshot-strict-envelope-2026-10-28/README.md |
| Preserved source SHA-256 | `b8a2ce659c6fe32f7e490e26c2adc27089db69ff9c3f47defbc051ad33d273a6` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev50 strict raw-root enumeration before the V8 value codec so misspellings, nonstring keys, unexpected fields and missing members cannot be silently ignored. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that isolated DynamicOps stand-ins do not replace actual JsonOps and NbtOps compatibility or saved-world reopening. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a strict runtime schema policy with precise unknown-field diagnostics, distinct from an editor's ability to preserve unsupported future source data. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Decode nonmaps and misspelled root fields in real JSON and NBT, round-trip signed seeds and supported versions, and reject malformed envelopes before preparing any generator. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev50 — V8 strict root-profile envelope

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev50 — V8 strict root-profile envelope

**2026-10-28, Builder source-only pass.** Independently audited starting source: `563b6ef23586f579ff2b972d7404b4f0a9054002`, report [08:30 UTC](../../engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md). Carried full external grade **50/100 (5.0/10)**; this focused repair does not regrade anything.

## Implemented

The V8 immutable saved-generator snapshot was already validated for signed long seed, graph, material channel, version, and height; however its six-field `RecordCodecBuilder` silently ignored unexpected outer profile keys (such as `heigth` alongside valid `height`). This cycle wraps its existing value codec with a `DynamicOps` decoder that **enumerates the raw JSON/NBT-style root map before delegation**, rejects nonmaps/nonstring/unknown keys and missing members, and limits the root to the six required fields: `version`, `seed`, `min_y`, `height`, `density`, `ground`. It retains the existing encoder, signed decimal seed storage and nested graph/material validation. This is an experimental V8-only path; V1–V7 codecs, saved profiles, player edits and world geography remain frozen. No water, graphics or LOD source edited.

Tested Git blob: `a97d93b143104247566442b63d285545c894eab8`; previous source blob: `29dd459dd4bbe73c481d498fcc5d6368f7ad7f26`. The implementation was compiled and tested with actual Deep Primal density/materialization sources and **test-only stand-ins** for Minecraft/Mojang APIs.

## Checks

- Eclipse Temurin **25.0.5+8**, `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`: complete.
- New root-envelope test: **14,392** assertions, **4,122** deliberately invalid inputs rejected, **2,048** signed 64-bit roundtrips.
- Replayed V8 saved snapshot, base-height, ground material and native-section stand-in suites: **594,901 + 874,642 + 1,235,414 + 11,047,019** assertions.
- Aggregate affected focused checks **13,766,368 assertions**, all passed; original dev49 codec and **four mutant decoders** fail the new negatives. Test-run logs and minimal doubles retained in `DeepPrimal_Dev50_V8_StrictCodec_SourceEvidence.zip` in the cycle's conversation artifacts.
- No actual `JsonOps`/`NbtOps`/Mojang codec decode, native NeoForge V8 save/reopen/lighting, full Gradle, platform binary/JAR, AMD8060S, real Vulkan/OpenGL or water long-soak validation. Owner performs those builds/gates per October 27 authorization; **no testing JAR or prerelease** claimed.

## Coordination and handoff

Work-state root lease acquired by `builder-20261028T0900-v8-codec-envelope`; latest independent audit consumed. The separate renderer's active `20261028-nonempty-refresh` claim and concurrent LOD disk work were **not modified**. The old foreground water claim has an expired `active_until`, but no direct foreground live-session confirmation was available, so no water files were edited. Other-chat context checked for reported earlier activity, not a verified real-time lock.

**Next:** Owner native Mojang JSON/NBT exact root roundtrip/misspelling/unknown-key tests, V8 saved-profile reopens, native chunk/heightmap/lighting and frozen V1–V7 replay. Then recover water single-owner loaded/unloaded transfers, restart/durable constituent accounting and live waterfall pixel/latency acceptance. The builder remains enabled until fresh independent ten-category average is **strictly >8.5** *and* the separate complete water gates pass.


<!-- END PRESERVED SOURCE -->
