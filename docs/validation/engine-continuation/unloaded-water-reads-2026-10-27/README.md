# Loaded-only water cell reads — source checkpoint (2026-10-27)

**Scope:** WaterRuntime.units native read guard and one-read wake-neighbor loop. Production source version `0.0.1-alpha.29-dev.33-loadedreads.1`; no qualified build or publication.

Before this increment, the public `units(level,pos)` helper unconditionally used `getBlockState`, even when a caller supplied an unloaded neighboring chunk. Such a read can trigger unwanted host chunk loading while the engine promises sealed/unloaded borders. It now checks bounds and `hasChunkAt` before the native read; unavailable chunks return **non-admission (0)** rather than initiating a load. Separate `open`/transport admission continues to seal absent neighbors. Native wake scans reuse one sampled neighbor amount rather than repeating a block-state read.

**Validation:** The included `verify_loaded_only.py` extracts the **exact production method**, compiles it under the available Java 21 with throwing API fakes, then runs seven `-Xverify:all` cases. Out-of-world and unloaded paths make zero native block reads; loaded dry/1/15/16-unit cases preserve units. This is not Minecraft or NeoForge native verification. Need to rerun after any source edit.

**Missing gates:** Complete Java25/Gradle checkout, module/core build, packaged JAR identity/CRC, native water transport and unload/reload/save/edit, real Vulkan/OpenGL water surfaces, measured tick/frame tails and Windows AMD8060S. Staged water chemistry contact-index/retry adapters stay unpromoted. No new JAR, development release or independent grade. Last external full score **50/100 (5.0/10)**, carried from `docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md` at `bd28005`.

**Next:** qualify complete pinned source and these border reads in native 26.1.2; demonstrate no force-load on unloaded chunk boundaries and conserved amount/constituents on reload, then run all staged water-contact integration and GPU continuity.

The additional `verify_wake_equivalence.py` checks that the source contains the one-sample branch and enumerates all **544** combinations of water units 1..16, neighbor units 0..16 and admitted/not-admitted neighbor. Its stable-state predicate matches the prior two-read branch exactly. This is source-bound model equivalence, not a native tick/frame benchmark.
