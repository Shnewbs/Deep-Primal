# Renderer jam 69 follow-up backlog

1. P0 owner compile: run the three-file `javac` command from README on JDK25, then full Gradle/NeoForge. Confirm `LOD_COUNTER_COMPILE_PASS` and no additional source truncation.
2. P0 native client: verify camera travel/edit, stationary rediscovery, water transparency, reload/resize/world-switch/save/reopen/shutdown on Vulkan and OpenGL.
3. P1 matched performance: frame CPU/GPU p50/p95/p99, draw/descriptor count, culling/rebuild/upload, queue/RAM/VRAM with LOD/water on/off.
4. P1 renderer ownership: validate GPU color/depth/motion/output allocation provenance, barriers, fence-safe lifetime and DLSS temporal resources on RTX. Preserve actual FSR1/native fallback and do not claim unsupported FSR3/4 or frame generation.
