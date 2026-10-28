# Renderer-only continuation backlog — after optional bounded-quad upload

1. **Owner-side hardware A/B gate:** same world/camera, default part layout versus `-Ddeepprimal.lod.experimentalPartUploadKiB=32`. Include CPU/GPU frame p50/p95/p99, part upload sizes/costs, draw/descriptor counts, peak RAM/VRAM, tested/culled/visible parts and startup/travel/edit/resize. Preserve water transparency and native handoff. Turn off experimental split if more draws/tails dominate.
2. **Actual nonblocking upload improvement:** If hardware confirms large-part driver stalls, evaluate fence-safe reusable GPU storage or separately staged vertex/index chunks. Preserve atomic pending-page publication and old visible page until complete.
3. **Visibility/distance:** Fix persistent beyond-index horizon page cutoffs, stable near/native and vertical coverage, independent simulation semantics and authenticated unseen delivery. No distance-slider-only credit.
4. **Upscaling:** Maintain official AMD FSR1 spatial; implement valid temporal motion/depth/jitter handoff before real DLSS provider dispatch. Keep NGX native packaging/license and RTX hardware validation as gates. Frame generation is separate.
5. **Audits:** Source-only 4.67/10 self-audit; independent whole-engine 5.0/10 carried. Do not increase without live/native evidence. Owner handles Java25 full builds/GPU/JAR publication.
