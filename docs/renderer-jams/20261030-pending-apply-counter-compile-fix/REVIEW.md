# Renderer Jam 69 — read-only self-audit

This is a **renderer jam self-audit**, not the independent engine audit. Candidate source/test snapshot: `2e9e825f85039cb1ebe524d9256350718b098efb`; production blob `160f6eda48deaa0c6ece59511f1f4bb1c97ad5d5`. Scoring was performed after source/test commits without changing code during review.

## Regression and repair evidence

- `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java:177`: `PendingApply` was declared `static` while `advance()` modified owner instance counters at lines 207, 236 and 249. Java disallows this; the historical source could not compile.
- Historical blob `dbe990a022fd02aaccbe29e7b0f3407a8331f7aa` ended immediately after `clearRetry();` in `reset()`, missing its closing braces and focused accessors. A pre-jam60 snapshot `0a500a49ba1c4481b1162eea3783cb10de8c3862` retains the original closure/accessors.
- Candidate `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java:177-249` makes `PendingApply` an instance inner class, retaining the bounded 256-entry apply and 8192-key queue policy. `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java:339-364` restores class closure, focused accessors and reset of all seven new diagnostic counters, without resetting scan generation or force-closing the retiring worker.
- `docs/renderer-jams/20261030-pending-apply-counter-compile-fix/LodDiskCounterCompileChecks.java`: source-pinned regression for queue removal/addition, full-cap shed, callback, counter ownership and reset. Added to repo, but **not executed against production classes** in this environment.
- Read-only compare `6c5cd3dcad696a523b040619dd7943cca30fbf29...2e9e825f85039cb1ebe524d9256350718b098efb`: exactly three changed paths — claim, source, focused regression test; no water/engine/upscaling/automation changes.

## Checks

| Gate | Result |
|---|---|
| Java21 minimal negative reproduction: `javac -Xlint:all StaticNestedCounterRepro.java` | PASS (expected compilation error: non-static field in static nested class) |
| Java21 positive isolated model: `javac -Xlint:all -d ... InnerCounterSmoke.java && java -cp ... InnerCounterSmoke` | PASS (`INNER_COUNTER_SMOKE_PASS`) |
| Exact candidate GitHub readback, balanced braces, nonstatic inner, seven reset counters, restored accessors | PASS (static/source checks; not a compiler) |
| Guarded GitHub integration and scoped file compare | PASS |
| Java25 toolchain | NOT RUN — only Java21 present, owner JDK25 archive absent in this run |
| Actual production `javac`/Gradle/NeoForge and added reflection regression | NOT RUN — complete checkout unavailable locally; owner performs full builds |
| Native Vulkan/OpenGL, GPU p50/p95/p99, VRAM, water pixels, resize/reload/reopen/shutdown, FSR/DLSS dispatch | NOT RUN |
| Packaged JAR, CRC/native payload, SHA256, release | NOT RUN (owner builds) |

No before/after runtime or FPS measurements were taken. Source compilation regression is addressed structurally but remains **unqualified until the owner compiles the actual production classes**.

## Six-category renderer-only score

| Category | /10 | Pinned evidence and limitation |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 6.75 | `LodDiskIndexService.java:177-257` retains bounded queue apply; no native travel/reopen or unexplored coverage proof |
| Mesh generation/reuse/scheduling | 6.85 | Worker and apply path unchanged except owner counter access; no matched mesh throughput |
| GPU culling/submission/memory | 4.90 | No GPU code changed or GPU memory/draw evidence |
| Actual AMD FSR/NVIDIA DLSS | 5.00 | Prior FSR1 and incomplete DLSS evidence carried, no new provider dispatch |
| Visual/backend/settings compatibility | 5.30 | No water ordering/backend/settings change; no pixel test |
| Validation and measured performance | 5.50 | Found severe historical compile regressions, source repair and model tests; production Java25/native and matched performance unrun |
| **Average** | **5.72** | Arithmetic mean of six scores = 5.7167, rounded to 5.72 |

**Delta vs jam 68 self-score 5.97: −0.25.** The prior score was optimistic given the discovered compile-breaking source; this is a conservative correction, not a renderer performance regression measurement. Independent whole-engine grade remains unchanged.

Next repair: owner Java25 `javac` on the two production files plus `LodDiskCounterCompileChecks`, full Gradle, and native Vulkan/OpenGL travel/edit/water-pixel baselines; then resume GPU resource lifetime and DLSS temporal input work.
