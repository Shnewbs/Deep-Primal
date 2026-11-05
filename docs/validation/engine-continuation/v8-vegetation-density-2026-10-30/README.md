# Dev106 — Continuous V8 vegetation density

## Scope
The V8-r6 vegetation planner now consumes the continuous ecotone vegetation fraction from the saved absolute-coordinate surface sample. The saved vegetation policy remains the global density cap; local `Sample.vegetation()` scales each deterministic anchor's acceptance threshold.

## Source
- `src/main/java/com/shnewbs/deepprimal/engine/V8VegetationColumnPlan.java`
- Source commit: `cf233319ea54991dba4a7bfd748925d2bfefc8ad`
- Version bump: `fa11b098c175038c62839c8b7b34f2faad9df300`

## Focused source guard
Re-fetched final `main` source and verified:
- `cover.vegetation()` is read from the already planned ecotone sample.
- `localDensity = policy.densityScale() * cover.vegetation()` is used.
- non-positive local density rejects placement.
- the existing absolute-coordinate deterministic hash and spacing gates remain unchanged.
- no terrain height, physical water amount, hydrology domain, saved profile bytes, or renderer-owned file is modified.

## Limits
This is source/static validation only. No Java25/Gradle compile, native Minecraft generation, saved-world reopen, GPU/visual transect, performance, or water-engine acceptance was run in this cycle. The new policy is not native worldgen activation by itself.
