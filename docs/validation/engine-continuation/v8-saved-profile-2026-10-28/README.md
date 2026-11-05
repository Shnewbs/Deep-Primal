# Dev49 — V8 immutable saved-profile envelope (source-only)

**Version:** `0.0.1-alpha.29-dev.49-v8snapshot.1`. Consumed external audit `docs/engine-hourly-audits/2026-10-28T0731-5a23f28/REVIEW.md`, pinned `5a23f285d4680f9537b8baf1639b75036e7392ef`. The newly identified stale persisted-LOD discard is renderer-owned, so this engine cycle changes no renderer/LOD code. Last full independently accepted grade remains **50/100 (5.0/10)**.

## Source changes

- `V8GeneratorSnapshot` is an immutable *explicit-only* full V8 snapshot with a strict `Codec` envelope: `version`, decimal-string `seed` (entire signed 64-bit range), `min_y`, `height`, `density` graph definition, and `ground` surface definition. Invalid version, graph, channel, seed and vertical bounds are rejected. `Prepared` has a private constructor to prevent combining unrelated world profile/graph data, and can be compiled once per saved generator instead of every chunk.
- `DensityChunkMaterializer.fillSavedSnapshot` accepts that prepared profile and enforces saved minY and height **before any block or heightmap write**, then delegates to the existing exact planned soil/stone path. No V8 chunk-generator registration, automatic world migration or V1–V7 geography changes.
- `gradle.properties` is bumped to the uniquely named dev49 source version; no binary was built.

## Focused tests (not native Minecraft)

Eclipse Temurin **25.0.5+8**, `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`, with isolated test-only Minecraft and Mojang-codec API doubles: `V8_SAVED_PROFILE_PASS` **594,901 assertions / 116,736 voxel parity / 384 seed replay comparisons**, plus retained planned ground **1,235,414**, base height **874,642**, and section bridge **11,047,019** assertions. Aggregate **13,751,976**. Five deliberately broken source variants were separately compiled and rejected (height mismatch bypass, seed truncation, missing material channel, forged prepared owner, incorrect compiled seed). Existing production density classes were copied unchanged from the validated dev48 inputs; changed source SHA-1 blobs are recorded in the cycle evidence bundle.

**Not run/accepted:** actual Mojang JSON/NBT codec, native 26.1.2 generator/save/reopen, real heightmap/lighting/block placement, frozen V1–V7 world replay, full Java25 Gradle, Vulkan/OpenGL, owner Windows AMD8060S, finite-water ledger/chemistry and loaded/unloaded/multiplayer soak, JAR or release. The owner retains all full builds under the October 27 override. Codec stand-in roundtrip is **not** a saved Minecraft world roundtrip.

Next owner gate: actual V8 snapshot serializer and native heightmap/lighting/reopen parity. Builder next: reconcile V8 registered profile lifecycle after native qualification and finite-water unloaded-owner ledger. No completion/regrade claim.
