# Deep Primal renderer jam 41 — read-only publication telemetry self-audit

**Exact scored implementation:** [`13d9f07af3e991d2186e366d778d15bae35b7ef0`](https://github.com/Shnewbs/Deep-Primal/commit/13d9f07af3e991d2186e366d778d15bae35b7ef0). **Jam ID:** `renderer-20261029T2055-publication-cost-telemetry`. Original main `13e7cf743bd0471b2579d4b05cc661cff78e62aa`; claim commit `4b8355593d0057de7e7709b4ee9ca9268752c5af`; implementation parent equals claim commit. Existing version **0.0.1-alpha.29-dev.86-v8-vegetation.1** unchanged. This is an **author self-audit**, performed after code completion without editing code during scoring, **not the independent engine audit**.

## Consumed findings and actual change

Consumed [jam40's source review](../20261029-two-back-ab-toggle/REVIEW.md), particularly its high two-back page-publication CPU overhead and 95,416 fine predicates avoided only when ON; the actual frame-time/GPU break-even was not measured. Consumed [Dev86 independent audit](../../engine-hourly-audits/2026-10-29T2030-3df845f0/REVIEW.md), which left native graphics and the whole-engine grade unaccepted. Checked current main, engine/water state and leases, LOD architecture, graphics/upscaling docs, renderer backlog, previous renderer receipt, and root inventory (no `AGENTS.md`).

Five changed implementation/source-test paths:
- [`LodPublicationProbe.java` L14–82](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublicationProbe.java#L14-L82), blob `4cb6a9614133ca0d1e835f62335cd77dd178899b`: bounded 240-observation rolling CPU p50/p95/p99 for freeze/capture, metadata, total, and saturating prepared-page/frozen-part/alias-bit counters. Validation rejects malformed input before changing counters. The method never reads a clock.
- [`VoxelLodRenderer.java` L874–888 and 1054](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L874-L888), blob `3939ca1351c8e771d5fd6045fc01aa97840329d8`: `System.nanoTime()` only under `TELEMETRY`; brackets actual frozen bound copy and subsequent mask/union assembly. Records after successful metadata preparation but before Entry installation and resets on world cleanup. Draw order, Vulkan/GL, water materials and shader unchanged.
- [`LodClient.java` L94–103](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L94-L103), blob `b43213acb3c79bd6b9344348dead5bfe777efc24`: exposes CPU percentiles and reuse-bit counts with explicit non-GPU, non-FPS and not-necessarily-committed limitations.
- [`LodPublicationProbeChecks.java` L1–100](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/docs/renderer-jams/20261029-publication-probe/LodPublicationProbeChecks.java#L1-L100), blob `45a18d63cff1db768478aa14ba76c2f01871941c`: 8,000 deterministic randomized pages, cross-word masks, broadphase-off arrays, strict input rejection, rolling stats and reset.
- Scoped [README](README.md) provides exact commands and owner hardware comparison semantics.

**Metrics count prepared page candidates, not necessarily installed/resident pages or saved fine frustum tests.** For actual break-even compare the previous extract counters in a matched owner-GPU run. This adds instrumentation; it is **not a new mesh/GPU optimization or hardware performance gain**.

## Checks and evidence

Linux x86_64, system `java`/`javac` **21.0.12.1**. JDK25 installation and owner's archive absent from this run; Git CLI clone failed `Could not resolve host: github.com`. Pinning and guarded commits used the connected GitHub repo API. Exact committed new Java helper/test blob identities matched the locally compiled files, and unchanged `RollingTiming.java` matched Git blob `0be7512957537596295ba204f8f1dfc176aa686f`.

```sh
find src tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPublicationProbeChecks
```

**PASS:** three separate strict Java21 fixture executions, **48,023 assertions each, 144,069 total**. One compile-valid mutant changing mask bit count to nonzero-word count **failed at assertion 5** (exit 1), proving a negative control. **PASS 13/13** read-only pinned integration checks, including telemetry gating, freeze-to-metadata order, reset, diagnostic scope, unchanged translucent pipeline and `drawMultipleIndexed` API. The isolated fixture compiles the real helper, test and `RollingTiming`, **not the complete module or host renderer**.

**Matched before/after:** none executable in this isolated session; no new native frames, hardware FPS, CPU/GPU p50/p95/p99 or VRAM measured. Diagnostics are newly available to the owner's subsequent matching run.

## Six equal-weight renderer-only grades

| Category | Score /10 | Pinned evidence and missing acceptance |
| --- | ---: | --- |
| Distance / LOD correctness and usable coverage | **6.80 carried** | Existing ordered culling [source](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L585-L685); no useful unseen-terrain native acceptance |
| Mesh generation / reuse / scheduling | **6.95 carried** | No mesher, worker or native upload change |
| GPU culling / submission / memory | **4.55 carried** | Existing synchronous indexed API, not true indirect; new publication counters are CPU-only [L874–888](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L874-L888) |
| Actual AMD FSR / NVIDIA DLSS | **5.00 carried** | [FSR1 spatial implemented](../../UPSCALING.md), temporal FSR and active RTX DLSS still unaccepted |
| Visual / backend / settings compatibility | **5.30 carried** | Telemetry-only instrumentation, untouched draw/transparent-water path; no native pixels |
| Validation and measured performance | **6.50 (+0.05)** | [strict randomized test](https://github.com/Shnewbs/Deep-Primal/blob/13d9f07af3e991d2186e366d778d15bae35b7ef0/docs/renderer-jams/20261029-publication-probe/LodPublicationProbeChecks.java#L30-L99), negative mutant, 13/13 checks; new measurement capability but no actual measured GPU gain |
| **Average** | **5.85 (+0.01)** | `35.10/6 = 5.85`; jam40 `5.84`. Independent whole-engine **5.0 carried**, unchanged |

**Unrun owner gates:** full JDK25 Gradle9.2.1/NeoForge, packaged client/backend and JAR/outer-nested CRCs, platform/native payload, Windows Vulkan/OpenGL pixels/VRAM/frame timers, FSR 1/native quality comparison, water alpha order, save/reopen/resize/reload/world switch/shutdown, temporal FSR, active NVIDIA RTX DLSS. No test JAR, GitHub prerelease, Actions or CurseForge publication claimed under the Oct27 owner-build override.

**Next renderer-only priority:** owner-native same-seed/same-camera/same-water/FSR1/native Vulkan and OpenGL comparisons, with two-back ON/OFF and fixed/adaptive/disabled page culling; capture freeze/metadata p50/p95/p99, avoided fine cull tests, whole-frame CPU/GPU percentiles and RAM/VRAM. Use actual break-even to retain or remove expensive two-back metadata. Renderer schedule stays independent.

**Artifact:** `DeepPrimal_Renderer_Jam41_PublicationProbe_20261029_13d9f07_SourceEvidence.zip`, **7,781 bytes**, SHA256 `d7da09b8a6ef68c8527d6b5c29193db5625dfc74f4c47ed2d26a96d05e14e80e`, 12 ZIP entries, outer CRC and 11 internal SHA256 verifications passed; uploaded to Library `/Deep Primal/Renderer Jams` (ID `libfile_9ec26206d1d48191aae0ec741b6b84c9`). Source/tests/logs only, not a JAR.
