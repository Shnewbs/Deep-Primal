# Pose-aware adaptive frustum gate — renderer jam 28

This is a renderer-only, source-tested experiment, not a new development JAR. Exact reviewed source/test SHA: [`5713d702`](https://github.com/Shnewbs/Deep-Primal/commit/5713d70252e658625fd0fff1b6c911efba073359); [read-only self-audit](REVIEW.md).

The optional `-Ddeepprimal.lod.pageFrustumAutoSkip=true` now forces a fresh coarse page test after more than four blocks of camera motion or more than eight degrees of yaw/pitch movement relative to the previous successful sample. The original fine-part fallback remains authoritative and the opt-in remains **disabled by default**. The separate `-Ddeepprimal.lod.pageFrustumBroadphase=false` override wins over adaptation.

Reproduce source-only tests with Java 21 and the two production-excluded test doubles from the previous page-frustum A/B jam. Compile the production gate, production page bounds and committed [pose regression](tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumPoseChecks.java) with `javac --release 21 -Xlint:all -Werror`; run with `java -ea -Xverify:all` in both adaptive modes. The archive in the user's Library includes exact test logs, source identities, mutant rejection and a SHA-256 manifest.

**Owner native gates:** a full Java25 Gradle/NeoForge build plus matched Vulkan/OpenGL fixed/adaptive/off camera-turn and all-visible trials, frame p50/p95/p99, VRAM, water pixels, reload/reopen/shutdown and valid DLSS/temporal FSR status. No FPS or active DLSS claims here.
