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
