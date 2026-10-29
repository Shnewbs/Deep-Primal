# Renderer jam 37 — one-pass resident page culling metadata

The exact source commit is [d20f9c9](https://github.com/Shnewbs/Deep-Primal/commit/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1) (parent 88491c2). One-pass LodPublishedBounds.populate replaces two resident-page AABB metadata scans without changing GPU mesh parts, fine frustum ordering, frozen bounds, water transparency, distance limits, shaders or FSR/DLSS. This is a CPU-only publication optimization; existing independent renderer/engine responsibilities remain separate.

Strict Linux Java21 standalone reproduction used the previous jam36 source-pinned LodPageFrustumBounds and LodPartFrustumRuns, test-only AABB/TerrainGpuMesh stubs, the new helper, and the committed smoke from this folder. Build command:

    find src baseline stubs tests -name '*.java' | sort > sources.txt
    javac --release 21 -Xlint:all -Werror -d out @sources.txt

Run java -ea -Xverify:all for LodFrozenBoundsSmoke, LodFrozenBoundsCommittedSmoke, LodAdjacentRunSmoke, LodPartFrustumRunsChecks, LodUniformPageVisibilityChecks, LodUniformPageVisibilitySmoke, LodPublishedBoundsParity and LodPublishedBoundsCommittedSmoke with JVM property deepprimal.lod.pageFrustumBroadphase true and false. 16 passes / 6,786,388 terminal assertions, two rejected negative variants. The production-excluded full randomized parity/benchmark sources, compiler/test logs and SHA256 manifest are in the verified source evidence ZIP in the user's Deep Primal / Renderer Jams Library folder.

See [REVIEW.md](REVIEW.md) for source-linked six-category grades, actual matched CPU publication numbers and explicit Java25/Minecraft/GPU gates; [LEDGER.json](LEDGER.json) records hashes and artifact identity. Owner performs full Gradle/Minecraft/GPU builds under the October 27 owner override; there is no new JAR or release from this source-only jam.
