# Renderer-specific follow-on — after jam 32

1. Owner hardware: fixed world/seed/camera, native Vulkan and OpenGL, FSR1 off/Quality, near and 125 full-detail + 192 LOD baselines; capture whole-frame CPU/GPU p50/p95/p99, draw/descriptor calls, visible/culled page counts, rebuild/upload CPU, RAM/VRAM, water ordering and visual pixels. Include camera travel, edits, FOV/resize, world-switch and save/reopen. No evidence yet for useful 512 distant unseen terrain.
2. Turn actual trace into mesh/GPU scheduling improvements: upload parts vs frame budget, page cache churn and stable order, synchronous indexed draw cost; do not claim API batches are GPU multi-draw indirect.
3. Preserve exact FSR1 reconstruction; temporal FSR3/4 and NVIDIA DLSS require temporal depth/motion/history and real SDK integration, native hardware testing, rights/packaging. Frame generation is independent.
4. Keep separate full-detail, vertical, LOD and simulation distance requested/effective budgets; don't raise a slider without host tracking/network/memory acceptance. Keep V1–V7, player edits and water appearance unchanged.
5. Next self-audit begins from fresh main and consumes jam32; external engine 5.0/10 remains separate.
