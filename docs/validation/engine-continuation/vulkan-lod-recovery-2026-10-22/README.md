# Vulkan frame completion and bounded LOD disk recovery — 2026-10-22

Two independently authored production fixes extend `work/engine-after-alpha24` after accepted checkpoint `5d36e3ce`. Main remains `d021557d`; no installer is released. Vulkan remains the primary sample/reference API and OpenGL remains a modular fallback.

## Reproduction and source changes

1. Actual V3 Create World joined successfully and then crashed during save/disconnect: the screenshot readback callback mapped a public encoder while lazy frame startup incorrectly marked its render pass open. An injected native completion reproduces the same mapBuffer → MemoryManager.doFrameOps → beginFrame → fbo.bind → createRenderPass stack. Move `inRenderPass = true` after binding/clear/viewport setup, immediately before returning the public render pass. The guard against mapping inside a genuinely open pass remains.
2. `VoxelLodRenderer.startDiskJob` discarded indexed persisted pages outside the current radius. The real-store native baseline retained zero keys and could not start after the camera position changed. Rotate out-of-range keys instead; examine at most `min(64, initialQueueSize)` per call. Cached/dirty skips, the existing single worker, read/upload budgets and authoritative terrain remain unchanged. The caller can try twice on a scheduled client tick: the existing worst-case selection bound is therefore 128 inspections, not 64 per tick.

The initial water-discovery-bounds suspicion was rejected: `WaterSectionBoundsMixin` already expands the discovery bounds. No water-bounds patch was made.

[Source audit](source-audit.json) verifies all 505 original restored build inputs: 502 unchanged, two production files changed and ClientCapture validation dispatch/shutdown changed. Two native fixtures are new. Water runtime, core and frozen V1/V2/V3 geography match the parent. The unaccepted retained-drain runtime/core experiment is not integrated.

## Fresh passing evidence

- Uncached Linux Vulkan full build: 25 tasks executed, core/water/geography/module checks pass. [Build records](combined-build-summary.json).
- Six archive CRC checks and 406 compiled-class comparisons pass. All five standalone module JARs equal their corresponding nested payloads. Validation/private-agent classes are absent. Final outer JAR SHA-256: `db8e3ea50b6c76ff641032116c3408302cc11a91af776e599fe6b0aec14ac1c3`. [Package audit](vulkan-build-audit.json), [module origins](packaged-module-origin-audit.json).
- Exact production queue/range methods compiled against explicit doubles: old code fails; fixed code passes 1,000 seeded camera selections, an 8,192-key queue, 2,000 out-of-range calls, the 64-inspection bound, no duplicate growth, small-queue-once, persistence-disable, absent-player and cached/dirty controls. This is a source-extracted scheduling check, not Minecraft teleport acceptance. [Results](disk-recovery-extracted.json).
- **Two strict combined software-Vulkan native sessions pass**, one from development classes and one from packaged module JARs. Each loads a real LodStore page through the real worker and uploads actual GPU buffers: retained=1, diskReads=1, uploadedChunks=1, isolated buffers released. Each also executes eight frame-boundary completions mapping 128 bytes and verifies eight in-pass map rejections. Device shader/buffer/storage/depth/texture/stencil/lightmap checks, reload, normal save/disconnect and injected shutdown-completion continuation pass. No new crashes, chunk-save failures, VUIDs or Vulkan validation errors; final tracked buffers/images zero. [Strict audit](strict-combined-native-audit.json).
- Actual fresh V3 creation and save/disconnect also pass with the Vulkan ordering fix before the LOD patch was installed; this separate scope is retained in [creation evidence](vulkan-create-attempt1/console-evidence.txt). It is not counted as a third combined-source native session.

The isolated LOD fixture briefly changes client player position within a callback and restores it immediately; it does not physically teleport/load/edit authoritative world chunks. The synthetic page uses real store/mesher/worker/upload/reset operations. Scene observations have LOD enabled/active, but this camera reports zero visible LOD triangles. The isolated upload is not evidence of complete visible distant-world coverage.

## Self-audit and rejected runs

[Original crash stacks](inspected-before-failures.json) preserve the natural/injected Vulkan failures and native LOD baseline assertion. Two later direct-`mc.stop()` sessions passed the regression assertions but logged chunk-save/rejected-executor errors despite **Gradle exit zero**. They are rejected as whole-session passes. ClientCapture now calls the normal `disconnectWithSavingScreen()` path before stopping, and the strict auditor rejects save/scheduler/server-stop errors. This validates the normal disconnect path; it does not repair or establish direct-stop/quit safety. [Rejected sessions](rejected-direct-stop-sessions.json).

A generated standalone Vulkan archive was non-ZIP/truncated; another audit saw BadZipFile. No such bytes were accepted. A later snapshot was valid and matched the finished nested module; the underlying cause is unproven. The private runner now verifies CRC/current compiled bytes, pins that verified standalone module from the full archive and excludes regenerating its jar task during native launch. [Rejected archive](rejected-generated-archives.json), [later snapshot](malformed-direct-module-audit.json). Early canceled startups and stale Xvfb-lock retries are not native passes; private displays now use unique IDs.

A packaged launch without forced retry actually selected OpenGL recovery and correctly failed the Vulkan assertion. Both final combined sessions explicitly used Vulkan retry. The recovery marker was present afterward despite FIRST_FRAME and clean shutdown; its persistence cause is unproven. **Clean unforced reopen is not accepted.** [Fallback evidence](rejected-packaged-fallback.json). Nonfatal shader-output/unused-attribute warnings and unavailable headless audio remain distinct from zero Vulkan correctness errors.

## Visual inspection and remaining work

The two final lake screenshots were inspected: upright terrain/shore and animated water are visible. They show some exposed brown regions on the right and differ while simulation/delivery continues. No identical-state comparison, complete water/LOD coverage, transparency correctness, owner GPU result or FPS improvement is claimed. See [screenshot manifest](screenshot-manifest.json).

Next instrument the recovery-marker lifecycle in-process and reproduce an unforced reopen; establish whether persistence is an engine or workspace effect without erasing evidence. Audit direct shutdown save safety separately. Then extend bounded coverage checks to real physical teleports, memory pressure, edits and reload, including consumed/evicted disk pages. Rotation only preserves still-indexed out-of-range pages; it does not solve all eviction/reindex recovery.

Continue the requirement register in `docs/TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md`, `docs/WATER-BUDGET-REQUIREMENTS.md` and `docs/LOD-AND-DRAINAGE-ARCHITECTURE.md`. Regional renewable/finite water budgets, larger pressure-search residency, staged basin p95/1,024 waiters, coherent geology and LOD hierarchy remain open; catalog/species remains separately deferred. Solver substeps are not independent 60 Hz. Software llvmpipe correctness is not owner GPU/FPS acceptance.

## Reproduction notes

The committed source and fixtures are authoritative; patches and the original LOD file support the exact-source reproduction. Restore the project at this checkpoint in an isolated `project/` beside the evidence scripts, restore checksum-pinned JDK/Gradle/private NeoForm tools with the repository helpers, and restore pinned graphics packages with `restore_graphics.py`. Private tools are not mod payload. Run an uncached full Linux Vulkan build before `audit_vulkan_package.py`, then `run_vulkan.py disk-after-graceful-retry` and `run_vulkan.py disk-after-packaged-retry`, followed by `audit_native.py` using the actual output directories. Existing case directories get attempt suffixes. `check_disk_recovery.py` uses the restored JDK and the included before-source.

Compact selected console records, full-console hashes, crash excerpts, explicit result files, tool checksums and package/source audits are preserved. Large logs, worlds, private tool binaries and installer bytes are omitted. Pre-reset and prior-checkpoint measurements are historical, not fresh validation here.
