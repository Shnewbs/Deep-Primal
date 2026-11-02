# Deep Primal — Dev179 independent audit (2026-11-02 03:30 UTC)

## Scope and commit review
- Compared prior independent audit head `2abf1a6081bb35bc16d5b1f2d2d85fc0941fa718` against dev178 head `0fb3afc456944e6b33ca3a50c0325a5ed126b2c5`: **six commits, five changed files**.
- Reviewed dev178's nonfinite horizon-sample admission, permanent `HorizonGeometryFiniteAdmissionChecks` registration, claim release and validation notes. No new water, terrain, renderer-vulkan, native persistence or build-file changes appeared in this compare.
- Independently inspected `HorizonMesh`, `HorizonGeometry`, `HorizonSamples`, `HorizonView`, `FarTerrain`, `HorizonRenderer` and build configuration. This is a source and isolated-Java audit, **not** a game/GPU acceptance run.

## Confirmed defect and repair
- **Defect:** `HorizonMesh.build` previously always advertised `minimum=floor`, although water-colored roof vertices use `height - 0.125f` and captured heights may be below `floor`. Consequently the reported vertical AABB did not always contain actual roof vertices, which can cause conservative visibility decisions to reject visible geometry. An in-game flicker regression was **not** demonstrated.
- **Correction:** Compute a conservative integer lower bound from each stitched roof height and its water-surface offset; keep existing maximum, all vertices, normals, colors, indices and GPU byte counts unchanged.
- **GitHub commits on main:** production `5e3aa3d00518ef9fdbc23752850fa0287adb1faf`; permanent regression `a928b6417057785ecb985a009e95ee01077b33d1`; registration `5130ad1edad75a0734eb32b852cb0d6b2dc11769`.
- Production blob before `3c252bc0d9febf42cb7881ddd11aa14bf67e5f8d`, after `5e7c373a3b545ae9b426e38abe10a2cbe7005571`. Regression blob `8aa428a15139d92d38345436b42c65a74d0b19c5`. Dependencies `HorizonGeometry` `c63dfc6058fc2147fa6aedd016406d7ac3575881` and `FarTerrain` `fd1a247028c9a4fda99422515438027b809ea2eb`.

## Reproduction and verification
- Original exact production `HorizonMesh` failed at **assertion #2**, water roof below declared minimum. This is a genuine negative control.
- Corrected exact production `HorizonMesh` with exact production geometry dependencies passed **370,359 assertions** across all three LOD detail tiers, including 150 randomized meshes and every vertex.
- **360 randomized meshes** gave identical SHA-256 geometry digests before/after: `ca92de2ecc605b23f5c562088514f8e4c5f0f21c75ee1680f014f276e2f797b2`. Digest includes positions, normals, colors, indices, heights and topology, excluding intentionally corrected bounds.
- OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed. This does not establish Java25 or full coreChecks acceptance.
- Local evidence ZIP: `DeepPrimal_Dev179_IndependentAudit_20261102T0330_Evidence.zip`; **SHA-256 `4e18d0482b26c510f965dca419223d6d5367b35682a4968d67c409cdaf1808ad`**; 19 files, ZIP integrity verified.

## Compatibility, grading, build and preservation
- `gradle.properties` still pins NeoForge `26.1.2.122`; root Gradle requests Java25, Gradle wrapper 9.2.1. Minecraft 26.3 is released; 26.4 remains in snapshot testing, with the Vulkan-default graphics change described by Mojang: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1 . Compatibility with NeoForge 26.3/26.4 is **not validated**.
- Scoped horizon mesh-bounds correctness **8.5/10 provisional** after isolated checks. Historical whole-engine baseline **5.0/10**, **not newly regraded**. Native water conservation, terrain seams, world persistence, Vulkan/OpenGL FPS, LOD GPU acceptance and full performance benchmarks remain untested.
- Full playable JAR: **not produced**. Local runtime has JDK21, no complete private repository checkout, no resolved Gradle/NeoForge dependencies, and GitHub DNS is blocked for local Git. Thus **no playable JAR SHA-256 exists**. Existing JARs were not deleted; no GitHub Actions were used.

## Next acceptance priority
Restore full checkout/JDK25/Gradle dependencies, run complete coreChecks and packaged NeoForge client/server smoke tests, then validate native water conservation, save/reload, cross-tile terrain continuity, Vulkan/OpenGL fallback and LOD FPS/VRAM. Do not delete any prior audit JAR until a replacement JAR is built and SHA-verified.
