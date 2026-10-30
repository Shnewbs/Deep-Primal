# Renderer Jam 67 self-audit

This is the renderer jam's read-only self-audit, not an independent external audit.

| Category | Score |
|---|---:|
| Distance / LOD correctness and usable coverage | 6.85 |
| Mesh generation / reuse / scheduling | 6.95 |
| GPU culling / submission / memory | 4.90 |
| Actual AMD FSR / NVIDIA DLSS | 5.00 |
| Visual / backend / settings compatibility | 5.30 |
| Validation / measured performance | 6.85 |
| **Average** | **5.97** |

Change from jam 66: **0.00**. The increment improves baseline observability only; no matched runtime or named-hardware evidence was produced.

Pinned evidence:
- Production change: `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java`
- Previous diagnostics contract: jam 66 `pendingApplyRemaining`, `appliedRemovals`, `appliedAdds`, `appliedSheds`
- New counters: `retargets`, `completedScans`, `failedScans`

Unrun gates: Java25/full Gradle/NeoForge; native Vulkan/OpenGL; GPU p50/p95/p99, VRAM, draw/descriptor counts; water/lifecycle checks; actual FSR/DLSS dispatch; packaged JAR/CRC/native payload/release.
