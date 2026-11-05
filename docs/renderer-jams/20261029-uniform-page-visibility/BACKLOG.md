# Jam 34 — next feature-scoped renderer work

1. Validate the uniform-bound positive-frustum reuse with **same seed, coordinates, camera angle, resolution, full-detail/vertical/LOD/simulation distances, warm and cold cache, transparency and FSR1/native** under both Vulkan and OpenGL. Compare fixed coarse, experimental adaptive and broadphase off. Confirm the part AABB does not mutate while cached, after resize/reload/world switch or GPU reset; any unexpected difference requires disabling this fast path until repaired.
2. Collect actual CPU/GPU frame-time p50/p95/p99, uploads/allocations/rebuilds, API submissions and hardware draw calls, VRAM/RAM resident/pending/cache counts; synthetic Java p50 is insufficient to declare native FPS gains. Note synthetic CPU p99 outlier on the second all-visible run (**7,215,383 ns**).
3. Prioritize measured mesh batching, upload scheduling and fence-safe storage; no claim of hardware GPU multi-draw indirect from Java API batching. Keep water transparency and original part ordering.
4. Enforce separate full-detail/native, vertical, LOD streaming, network/tracker admission and simulation semantics; do not raise usable distances by slider changes alone. Maintain explored-terrain delivery contract for unseen areas.
5. Defer AMD temporal FSR and NVIDIA DLSS runtime provider until valid temporal inputs, vendor SDK packaging/licensing and supported GPU capabilities. Keep FSR1 spatial and native fallback honest; no frame-gen substitution.

Owner does full builds/GPU under Oct 27 override. No source outside the renderer's scoped ownership, other feature schedules, Engine/Water work-state, gradle.properties or build caches is to be modified.
