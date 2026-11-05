# Java25 focused scanner regression

Source code remains pinned to [89b8bb0](https://github.com/Shnewbs/Deep-Primal/commit/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb) without product changes. After the initial renderer self-audit, the owner's Temurin 25.0.5+8 Linux archive was materialized from the Library into this jam's isolated workspace, verified against the expected archive root and safely extracted. java and javac report version 25.0.5+8.

Both --release 21 and --release 25 compilation with -Xlint:all -Werror passed. Both executable scanner regression tests passed 9,006 real-directory assertions. A separate 21-run warm-directory Java25 diagnostic (8,206 filenames, at most 8,192 retained) reported p50 8.237ms, p95 13.074ms and p99 13.456ms. No native game, current Gradle project, JAR, GPU/fence, full-frame or other hardware acceptance ran, so no new performance grade.

**Library artifact:** DeepPrimal_Renderer_DiskNearest_Java25_20261028_89b8bb0_Evidence.zip (2,348 bytes; SHA256 40e0d5e67c003689593c5d5776c0a5523d5ec36de0cd47415a43ed4f4aa4615b; 7 CRC-valid entries), Library file libfile_307174058df08191b9e22fa49ae86f80. The exact current-source helper/test Git blobs are unchanged and appear in the primary evidence ZIP.

Owner still handles whole Java25 Gradle, Minecraft, native Vulkan/OpenGL, Windows AMD and distribution. The dedicated renderer schedule and separate engine/auditor scopes are unchanged.
