# Renderer jam 43 — immutable AABB validity reuse

**Jam:** `renderer-20261029T2255-immutable-aabb-validity`  
**Scored source:** [`8f28f833caa259a0e59adac494a3aee8d750bc63`](https://github.com/Shnewbs/Deep-Primal/commit/8f28f833caa259a0e59adac494a3aee8d750bc63)  
**Source version (unchanged):** `0.0.1-alpha.29-dev.88-vegetation-receipts.1`  
**Parent:** `27620460fb51c06c9d13a034d8802c67d3ae7878` (renderer-only claim, original main `d93050d9adae10603c934d23fdf233ad8d7d63e1`).

Resident LOD page metadata now reuses the finite/ordered verdict from an identical immutable AABB one or two parts earlier (including **invalid verdicts**), and avoids a second representative-box comparison when adjacency already proves a uniform page. No mesh, water simulation, shader, backend, camera settings or draw-order changes.

The test [`LodImmutableAabbIdentityChecks.java`](LodImmutableAabbIdentityChecks.java) compares the production helper against [its exact previous implementation](LodPublishedBoundsPreIdentity.java) with all 4 broadphase/two-back flag combinations, negative coordinates, signed zero, malformed bounds, identity reuse and deterministic random pages. Two Java21 runs of this test and the earlier two-back regression passed **23,885,080 assertions** across 16 JVM processes. Mutating an invalid identity into a valid one fails at assertion 15.

Exact compact stubs are under `stubs/` (test doubles, **not** Minecraft acceptance). Reproduce the self-contained parity test from the project root:

```sh
mkdir -p /tmp/renderer-jam43-classes
javac --release 21 -Xlint:all -Werror -d /tmp/renderer-jam43-classes \
  docs/renderer-jams/20261029-immutable-bounds-validity/stubs/{AABB,LodPageFrustumBounds}.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java \
  docs/renderer-jams/20261029-immutable-bounds-validity/{LodPublishedBoundsPreIdentity,LodImmutableAabbIdentityChecks}.java
for broad in true false; do for two in true false; do
  java -ea -Xverify:all -cp /tmp/renderer-jam43-classes \
    -Ddeepprimal.lod.pageFrustumBroadphase=$broad \
    -Ddeepprimal.lod.twoBackFrustumReuse=$two \
    com.shnewbs.deepprimal.lod.client.LodImmutableAabbIdentityChecks
done; done
```

**Synthetic CPU-only, 251 paired samples × 64 publications × 4 levels × 64 parts:** shared uniform p50 -42.8% to -43.0%; value-equal uniform -16.7% to -16.9%; alternating A/B -15.1% to -16.9%; mixed-late -4.5% to -6.1%. **Regressions:** unique +4.7% to +5.7%, repeated invalid uniform +15.2% to +15.5%. Some p99 tails varied or regressed. No GPU FPS, VRAM or native pixel claim.

See [REVIEW.md](REVIEW.md), [LEDGER.json](LEDGER.json), and [BACKLOG.md](BACKLOG.md). Exact benchmark source, previous two-back fixture, CPU logs, test mutation and SHA-256 evidence are in the verified 30,758-byte Library source-evidence archive `DeepPrimal_Renderer_Jam43_ImmutableAabbIdentity_20261029_8f28f83_SourceEvidence.zip`, SHA256 `ead5d79173846b3dbfa94cdf7168ab0048d54f92740e85b6b97582b31799b7b6`, Library ID `libfile_cf9aad9cc6688191afd480f9fdd4cfde`.

Owner policy: Java25 Gradle/NeoForge, native Vulkan/OpenGL, water-pixel and frame-time/VRAM checks, and JAR packaging remain **owner-operated and unrun here**. No Actions, release or JAR asserted.
