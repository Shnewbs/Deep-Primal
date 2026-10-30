# Renderer jam 63 — read-only renderer self-audit

This is the jam's author self-audit, not an independent external audit.

## Exact candidate
- Implementation commit: 3e006784887363c0184be1f45b4ae09928eeadd9
- Candidate branch: renderer-jam-63-publish-discard-fastpath
- Final integration: 6182bb354459e10f98aee8f5b96b82a87b5063e0

## Evidence
- Production file changed: lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java
- Existing selection, admission, queue, retry, worker-retirement, water-ordering, Vulkan/OpenGL, and upscaling paths are unchanged.
- The change only removes a needless temporary allocation on the cursor worker when no recovery key is shed.

## Validation
Passed at source-control level:
- current main re-read before branch creation
- isolated renderer branch
- exact production-file blob readback
- renderer-scope review

Not run:
- Java 25 / full Gradle / NeoForge
- native Vulkan/OpenGL
- GPU frame-time p50/p95/p99, draw/descriptor counts, resident/pending/cache RAM, VRAM
- water pixels, reload, resize, world switch, save/reopen, shutdown
- actual FSR/DLSS dispatch
- packaged JAR, CRC, native payload, source/compiled identity, release upload
- matched before/after runtime measurements

## Rubric
| Category | Score /10 | Evidence and missing gate |
|---|---:|---|
| Distance / LOD correctness and usable coverage | 6.85 | Existing bounded 8,192-key nearest-first selection and recovery queue semantics are preserved; no native travel, unseen-terrain delivery, or effective-distance proof. |
| Mesh generation / reuse / scheduling | 6.95 | Worker-side publish path does one less allocation on the no-shed case; no mesh throughput or game-frame measurement. |
| GPU culling / submission / memory | 4.90 | No GPU submission, fence, descriptor, or VRAM code changed; hardware evidence absent. |
| Actual AMD FSR / NVIDIA DLSS | 5.00 | No upscaler provider or dispatch changed; existing state remains the only evidence. |
| Visual / backend / settings compatibility | 5.30 | Completion payload remains immutable and water/backend code is untouched; native pixel/lifecycle evidence absent. |
| Validation / measured performance | 6.85 | Source-level guarded integration and readback passed; all Java25/native/GPU/package gates are unrun. |

Average: 5.97/10
Change from jam 62: 0.00

## Remaining defects / next repair
- No reproducible owner-side renderer baseline exists for camera/travel/edit churn.
- No actual mesh/GPU submission or VRAM evidence.
- FSR/DLSS temporal/provider integration remains unimplemented or unvalidated.
- Next priority is a bounded measurement harness plus authoritative Vulkan allocation provenance and real temporal upscaler resource validation.
