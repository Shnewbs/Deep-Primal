# Dev185 independent Deep Primal audit — 2026-11-02 06:30 UTC

## Scope
- Last audit baseline: `b76b2dea0ebb1ad735f2909fa03924afccf638ac` (Dev183).
- Reviewed Dev184 commits `3f5329a7c5fd00e8b3b88ac49bfab8c5f2b08bb5` and `8565fcf87ce8f0eabc465c94ab1fae26985bc444`: first-capture LOD dependency fix, registered lifecycle regression, portable runner.
- Verified exact GitHub source/test blob identities and independently replayed 225,011 Dev184 assertions.

## Additional confirmed defect
`TransitionDependencies.include()` throws when the fixed source capacity is exceeded. Previously it left `initialized=true` and could partially replace committed dependency identities. A retry at the same coordinates could therefore return `false` from `end()`, suppressing a necessary LOD transition rebuild. This is an exception-recovery edge case, **not** evidence of a current in-game visual artifact or normal-gameplay capacity exhaustion.

Original production blob `88f3c4eceda7146f0372a85365fbf5dcc2473e26` failed the new negative control at assertion 2: `Failed initial capture suppressed retry invalidation`. A second case covers an aborted partial overwrite after a successful capture.

Minimal fix: set `initialized=false` immediately before throwing the existing overflow exception. This forces the next completed capture to invalidate conservatively, without new allocations or changes to the normal identity-based path.

## Verification
- Strict isolated Java21 compilation: `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`.
- Existing lifecycle regression: **225,011 assertions PASS**.
- New overflow/retry regression: **400,007 assertions PASS**, including 100,000 deterministic overflow/retry/stability iterations.
- Total **625,018 isolated assertions PASS**; original negative control fails as expected.
- Corrected production Git blob `622cc2439905f77e663539af85536bfc3934f9e5`.
- New regression Git blob `24cd5eee808e458c0dce4fc184dcc1763bb04ca2`; registered in `CoreChecks.java`.
- Renderer call-site source review confirms `HorizonRenderer.transitionNeedsRebuild()` uses `begin/include/end` and `clearMeshes()` uses `clear`.

## Compatibility and acceptance gates
- Repository `gradle.properties` pins NeoForge `26.1.2.122`; `build.gradle` requests Java25 and ModDevGradle `2.0.148`.
- Official NeoForge project index listed 26.3.0.101: https://projects.neoforged.net/ . This does **not** establish 26.3 compatibility.
- Minecraft 26.4 Snapshot 1, published 2026-09-22, makes Vulkan default: https://www.minecraft.net/da-dk/article/minecraft-26-4-snapshot-1 . This does **not** establish 26.4 compatibility.
- No native water, terrain, saved-world, Vulkan/OpenGL, LOD GPU or FPS testing was possible.
- Local runtime has Java21 only; no full private checkout, Java25 or resolved Gradle dependencies. `git ls-remote` failed DNS. **No playable JAR or playable-JAR SHA256 was produced**; superseded JARs were not deleted. No GitHub Actions used.

## Grades
- Scoped LOD tracker correctness: **8.5/10 provisional**, based on exact-blob reproduction, negative control and 625,018 passing isolated assertions; native renderer acceptance outstanding.
- Whole-engine baseline: **5.0/10 historical, not freshly regraded**.

## Next steps
Restore Java25 and complete checkout, build/test a packaged JAR locally, verify its SHA256 and client/server startup, then test water conservation, saved-world recovery, terrain seams, Vulkan/OpenGL and LOD performance.
