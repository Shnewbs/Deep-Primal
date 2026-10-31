# Dev127 — direct native V8 coordinate preflight

Pinned parent: `e784de952574c4812274d3785f70598eecc7426e`. Consumed the Dev126 independent audit: `docs/engine-hourly-audits/2026-10-31T1027-4ba0e816/REVIEW.md`.

## Defect and fix
Dev123 guarded the V8 native-fill facade and Dev125 guarded the materializer, but direct clients of `V8ChunkGenerationNativeCommit.begin` could still acquire a ticket for signed chunk coordinates whose 16-block footprint wraps Minecraft's 32-bit block grid. The core border witness checks chunk-coordinate arithmetic but not the conversion to block positions.

Reject out-of-range X or Z before `Owner.snapshot()` or ticket admission, checking the origin and inclusive last block with exact arithmetic. The valid signed chunk interval remains [-134217728, 134217727] per axis. No water, renderer, legacy geography or saved-profile changes.

## Validation and limitations
- Local Java21 extracted-guard probe passed 500,049 deterministic comparisons against a 64-bit oracle, including boundary pairs (`javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`).
- Repository `V8NativeEpochPreflightChecks` now tests direct-API invalid X/Z, no-owner-snapshot side effects and valid boundaries; random fixtures are restricted to representable chunks.
- The Python runner now expects 72,037 assertions per positive process and rejects a coordinate-gate bypass mutation as well as the epoch-overflow mutation.
- **Unrun:** updated repository runner, complete native class compilation, Java25/Gradle/NeoForge, save/reopen, finite water, Vulkan/LOD and JAR packaging. Git CLI DNS and complete checkout/Java25 are unavailable here. No new JAR produced; preserve last verified build.
- Independent baseline 50/100 is unchanged. This is an admission safety fix, not native durable publication or a registered save hook.
