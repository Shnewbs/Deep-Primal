# Dev140 — fail-safe horizon LOD admission on invalid view telemetry (2026-10-31)

## Why this was chosen
The latest independent Dev139 audit corrected query-water dry material and left native build, LOD and hardware acceptance open. Its water files were excluded from this scoped change. The existing LOD `HorizonView.create` allowed a zero-sized/minimized viewport and FOV=0 to promote nearby horizon tiles to detail 0 rather than suppressing unnecessary work.

## Change
`HorizonView.create` now returns a view with zero fine/medium thresholds when viewport height is nonpositive, FOV is nonfinite or outside (0,180), or workload is nonfinite. Every tile then selects coarsest detail 2. All finite positive view sizes, ordinary FOV values and finite workloads preserve the original formula. No native renderer, saved-world, finite-water, shader, V8 geography or GUI file was changed.

## Reproduction and isolated validation
- Baseline exact source from `main` failed the new Java21 regression at `nonpositive viewport must be coarsest`.
- Patched `HorizonView`, exact `FarTerrain` source and new regression compiled with `javac --release 21 -Xlint:all -Werror` and ran with `java -ea -Xverify:all`.
- `HORIZON_VIEW_SAFETY_PASS checks=100038 tiers=[102, 433, 99465]`. Includes 100,000 deterministic valid-input parity cases plus malformed viewport, FOV, workload and camera boundary checks.
- Three targeted mutation controls (remove viewport, FOV, or workload guard) were independently rejected. Invalid camera positions already select coarsest detail under the existing distance arithmetic and need no new guard.
- New check is registered in `CoreChecks`; the complete project-wide `CoreChecks` suite was not executed in this environment.

## Acceptance limitations
This is a source-only LOD safety checkpoint, **not** a measured FPS, Vulkan, Minecraft, or packaged JAR result. The environment has Java21, no complete private checkout, Java25 or cached Gradle/NeoForge dependencies; Git CLI DNS cannot resolve GitHub. No full `gradlew build`, client/server tests, native Vulkan/AMD acceptance, qualified JAR, JAR SHA-256 or new whole-engine grade. GitHub Actions were not used. Previous qualified JARs must remain untouched until a verified replacement exists.

Next: restore native Java25 build and verify minimized/restore LOD transitions and Vulkan performance; continue V8 crash-safe persistence and finite-water save/reopen acceptance in their own nonoverlapping scopes.
