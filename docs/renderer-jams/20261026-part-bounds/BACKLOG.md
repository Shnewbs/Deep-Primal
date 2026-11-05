# Renderer backlog after 20261026-part-bounds

- Done: exact per-part bounds with baseline failure, geometry-equivalent result, complete build and packaged regression.
- Next: reproducible fixed-camera/travel/edit counters and telemetry (CPU/GPU frame p50/p95/p99, cull/rebuild/upload/allocation, draws/descriptors, resident/pending/cache RAM/VRAM), with named device/API and warm/cold scene identity. Current host has no GPU device; CPU-only counts are not acceptance.
- Verify live full-frustum bounds coverage, unload/edit/native handoff, reload/resize/world-switch/save/reopen/shutdown on both backends.
- Improve mesh reuse, worker/upload bounds and fence-safe storage; only then evaluate hierarchy and compatible real indirect batches.
- Define renderer-owned authoritative unseen-terrain delivery contract and coordinate generator/network prerequisites; currently explored-only.
- Expand independently persisted full-detail/vertical/LOD budgets with tracker/network admission; do not just increase sliders or alter simulation.
- Integrate actual officially documented/licensed FSR/DLSS providers, inputs, dispatch, lifetimes and device detection; linear spatial scaling is not FSR, frame generation is separate.
- Delivery gate owned by water builder: qualify precipitation admission fix and packaged native lifecycle; current combined renderer JAR is withheld.

First renderer self-audit3.50/10 (4,5,4,0,4,4); previous renderer delta unavailable. Whole-engine external rubric unchanged. Dedicated renderer schedule remains enabled; other schedules untouched.
