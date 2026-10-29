# Renderer jam 33 — next scoped priorities

1. Owner hardware A/B on identical seed, scene, camera, API, resolution, LOD/full-detail/vertical/simulation and warm/cold state. Test **fixed coarse**, **experimental adaptive** and **fully disabled** broadphase in Vulkan and OpenGL; collect native CPU/GPU frame-time p50/p95/p99, resident/pending/cache memory, VRAM, draw/descriptor counts and upload/rebuild timings.
2. Check transparent-water pixels and depth/ordering on cliffs and all-visible/offscreen camera turns under FSR1 spatial/native, resize, resource reload, world switch and saved edits.
3. Choose next **measured** GPU/mesh/LOD streaming bottleneck; do not promote adaptive by a CPU microfixture alone. FSR temporal and production RTX DLSS remain integration gates; no frame-generation substitution.
4. Preserve isolated renderer ownership, no engine/water file edits. Current jam33 source only removes pointless coarse AABB construction in the explicit off A/B path and does not claim FPS or usable-distance improvement.
