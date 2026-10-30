# Renderer jam 58

Focused renderer-only increment: bounded renewal overflow preflight in `LodDiskIndexService`. No other workstream changed.

Implementation commit: 88fe13f3d7ad493d36e63b234a9edffc1f400c3c

The preflight rejects a new renewal key before insertion when the 8,192-key map is full, avoiding transient growth on the fail-closed path. Existing generation-token and invalidation semantics are preserved.

Owner-operated gates not run: Java25/full Gradle/NeoForge, native Vulkan/OpenGL, GPU timings/VRAM, water/lifecycle, FSR/DLSS dispatch, packaged JAR/CRC/release.