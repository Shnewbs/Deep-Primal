# Jam 32 — projected-error selector reuse

Source [85a4859](https://github.com/Shnewbs/Deep-Primal/commit/85a48595e0ab172c8779f4f2a95a92d822c08ce8) keeps the current native/FSR1 scene-height LOD error calculation but avoids reconstructing its trig thresholds every unchanged extraction. An effective world-target height, camera FOV or LOD-error setting change immediately invalidates the cache; world reset drops it. No water, terrain, GPU draw or upscaler provider changed. Default behavior is unchanged; no toggle needed.

Focused tests: Java21 strict compilation, 750,010 seeded parity assertions twice, height-omission mutant rejected, 11/11 pinned source checks. The 100k-iteration isolated allocation fixture is detailed in [REVIEW](REVIEW.md); it does not measure game FPS. Owner runs Java25/Gradle, Minecraft clients and GPUs. The source/test evidence ZIP (8,859 bytes, SHA256 `8ba0675a684625cb8c6c8f9f7680bf187368cf35f26b214a52abb8bb46122e8f`) is saved under Library `/Deep Primal/Renderer Jams`. No JAR was built or released.

The dedicated renderer schedule/claim covers only this change. See [NEXT](BACKLOG.md).
