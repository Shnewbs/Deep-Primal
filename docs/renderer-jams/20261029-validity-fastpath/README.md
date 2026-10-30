# Renderer jam 44 — identical-AABB resident page publication

Scope: **renderer-only**. The actual resident-page metadata builder takes a guarded path only when at least four mesh parts share one identical immutable AABB object. It validates that object once, constructs the full adjacent bitmask across 64-bit boundaries and skips all union/part-by-part work; invalid shared bounds never become coarse-cullable or repeat aliases. A mixed page takes the unchanged regular path. No new renderer inputs, GPU buffers, shaders, water simulation or draw ordering.

This increment consumes jam43's measured invalid-uniform and unique-bound publication regressions. The source-only Java21 parity fixture compares final metadata to the exact previous committed renderer implementation (not merely to a helper model), with broadphase and two-back enabled/disabled, seeded diverse shapes, null/invalid bounds, signed zero and explicit 2/3/4/63/64/65/128/129/257/1024/1025-part masks and late divergence. Benchmarks compare matched Java CPU publication loops; they cannot verify GPU pixels, frame time or resident real-world frequency.

Run in an isolated directory with:
```bash
javac --release 21 -Xlint:all -Werror -d classes $(find src -name '*.java')
for broad in true false; do for reuse in true false; do
  java -ea -Xverify:all -cp classes -Ddeepprimal.lod.pageFrustumBroadphase=$broad -Ddeepprimal.lod.twoBackFrustumReuse=$reuse com.shnewbs.deepprimal.lod.client.LodUniformIdentityFastpathChecks
done; done
for shape in uniform-shared uniform-values alternating mixed-late unique alternating-invalid invalid-uniform; do
  java -ea -cp classes com.shnewbs.deepprimal.lod.client.LodUniformIdentityFastpathBench "$shape"
done
```
Place `LodPublishedBounds.java`, `LodPublishedBoundsJam43.java`, `LodUniformIdentityFastpathChecks.java`, `LodUniformIdentityFastpathBench.java` and the two stub files into package-matching `src` directories. These isolated Java API doubles are **not** production sources; full Java25, Minecraft, GPU and JAR remain owner-run.

See `REVIEW.md` / `LEDGER.json` for exact commit, source blob, observed measurements and unrun acceptance gates.
