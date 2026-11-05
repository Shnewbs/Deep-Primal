# Deep Primal Dev206 — immutable LOD column state query

Source-only validation. No NeoForge runtime or playable JAR was produced.

## Change

`ColumnarChunk.state(x,y,z)` previously scanned every vertical run until finding a queried height. The constructor validates every column as a nonempty, contiguous, ascending sequence of run triples. Binary-searching the exclusive upper Y endpoint returns the identical palette entry in O(log R) rather than O(R) for R runs per column. This does not modify serialization, saved-world formats, palette, mesh geometry, water or GPU rendering.

## Exact-source verification

- Baseline Git blob: `402a89d7b8da4ad90bd3759355f87f7191a06b6f`
- Candidate Git blob: `1699be49b34b223ad53e509f4a32e0730fc41865`
- Registered regression Git blob: `7dadf5e13065f8e7bae696af37affd2f6a018a77`
- `CoreChecks` registration Git blob: `1b45b1f9e6fe45244fede900358b8ad3cc822a53`

## Test evidence

`javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`:

- 88 scenarios spanning heights 1,2,7,64,257,4096; negative and positive world minimum Y; upper integer boundary; uniform, alternating, layered and seeded-random runs.
- **600,680 assertions passed** in both original and optimized source, including exact height boundaries and invalid coordinate rejection.
- Deliberately changed `<` to `<=` in the binary-search upper-bound predicate. The mutant compiled but failed on the exact run boundary `height=2 mode=1 column=0 y=1`.
- Median of 3 isolated Java21 JVM benchmark runs for 327,680 lookups over a 4096-high alternating-state chunk: **624,786,833 ns original** vs **79,943,349 ns optimized**, **7.82x** speedup. Equal sink `2631461`. This is not an in-game LOD, FPS, Vulkan or GPU benchmark.

## Build/acceptance boundaries

The local environment has Java21 and no complete private repository checkout, Gradle wrapper, resolved NeoForge dependencies or Java25 toolchain. Direct `git ls-remote` failed because github.com DNS resolution is unavailable. A full Java25/NeoForge build, client/server startup, saved-world replay, and native Vulkan/LOD performance acceptance were not run. No playable JAR or JAR SHA-256 exists for this run. Previous successful JARs were not removed; GitHub Actions were not used.

The Dev205 audit's registered close-lifecycle test correction was preserved. The historical whole-engine grade remains 5.0/10 pending independent full-engine revalidation.

Reproducible source/test evidence archive (provided in the automation response) SHA-256: `636ad4008683fd7e4f450815b35eedbbebf80e71c73a1b41bab1f63dfc35b317`.
