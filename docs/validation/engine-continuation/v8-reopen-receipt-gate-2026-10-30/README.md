# Deep Primal dev111 — V8 reopen receipt gate

## Implementation
- Added a fingerprint-gated overload to `DensityChunkMaterializer.fillSavedEcotoneVegetatedWithReceipt(...)`.
- The saved-profile fingerprint is checked before the delegated receipt path can run climate/density preflight or native block setters.
- The original explicit first-save overload remains unchanged.
- No serialized profile fields, terrain height, finite-water state, hydrology ownership, renderer files, or V1–V7 paths were changed.

## Guard
- Final main source blob: `ed26ba2060a76dcd6e792705011e7ff0713455c1`
- Final gradle.properties blob: `cab45d6dc51b809942e876233e8b8c2809f87fc7`
- Static guard: `DEV111_SOURCE_GUARD_PASS`
- Checks: {"overload":true,"delegation":true,"ordering":true,"version":true,"old_overload_preserved":true}

## Limits
This is source/static evidence only. Java25/Gradle, native Minecraft save/reopen/edit replay, GPU validation, visual transects, performance, and full water-engine acceptance were not run in this cycle. No playable or qualified JAR was built or published.
