# Dev122 — V8 native candidate NBT prewrite admission

**Implementation commit:** `7a94db54c279031a258609d76c6a91788426bef0`  
**Source version:** `0.0.1-alpha.29-dev.122-nbt-prewrite-admission.1`  
**Consumed independent audit:** `docs/engine-hourly-audits/2026-10-31T0526-990a6b33/REVIEW.md`, pinned `990a6b333d828e47a9612f3a56da8eea8f9cd06e`. The auditor's Dev121 self-suppression repair was already present on current main and was preserved.

## Defect and source repair

Before this change, `V8ChunkGenerationNativeFill.execute` admitted a candidate with an existing generation marker, acquired a border ticket and performed native block/vegetation writes; only the later NBT publication path rejected the marker. Even though a discard callback was mandatory, native setter callbacks may have external side effects, so this was an unnecessarily late rejection.

`V8ChunkGenerationNbtAdapter.requireFreshCandidate` now rejects **any** existing marker type, and `writeNewGenerated` reuses the same guard. Both V8 terrain and r6 vegetation/receipt paths invoke this guard **before** ticket acquisition and before materializer callbacks. An absent marker is **not** treated as evidence of an ungenerated chunk: the durable owner still must certify the target and four neighbors. Legacy V1–V7, saved profiles, terrain graph, finite-water quantities and renderer-owned code are unchanged.

Production Git blob identities:
- NativeFill `f1973e3e6f9555e733ddea25e18f989595935c94`, SHA256 `13b578d0ca21009cb62f2ba26b147c39fccc61e4cd1b5d42cdfc6d85e0b96377`
- NBT adapter `b57e311d165a826f0cf9c8a8aa6c83266680c38e`, SHA256 `876232828682a2d0fb82a972d407aaa8fa2c0bde0f8cd221dedf1464449e60c1`

## Executed validation

Linux OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. The two exact committed production blobs were independently reconstructed in a temporary isolated harness; `git hash-object` matched the GitHub blobs above. Minecraft/chunk/owner classes were **API doubles**, not a native runtime.

- NBT adapter: **60,002 assertions per positive process**, repeated twice identically (120,004 total). Checks include canonical stamp read/write, signed coordinates, durable epoch, generated/ungenerated neighbors, stamped and wrong-typed candidate rejection.
- NativeFill: **16,000 assertions per positive process**, repeated twice identically (32,000 total). Checks include marked terrain and vegetation candidates rejected before owner ticket and materializer invocation across 2,000 signed-coordinate scenarios, plus successful unmarked publication.
- **Two compiled negative controls rejected:** disabling NBT marker rejection and removing NativeFill's prewrite admission. Both compiled with Java21 and failed at the first invalid candidate.
- **Total local positive assertions: 152,004.** The first NativeFill compile attempt failed because a test-only ChunkPos stub used private record fields; the stub was corrected to public fields matching the production access pattern, then the production blob compiled and both positive/mutant checks ran. The rejected attempt is not counted as a pass.

Repository runners `tools/test_v8_native_fill.py` and `tools/test_v8_chunk_nbt_adapter.py` and their focused tests were extended with the new prewrite cases and mutants; **these repository runners were not executed in a complete checkout in this run**. The local isolated harness was not a full project test.

## Remaining acceptance and coordination

No Java25/Gradle9.2.1 full build, NeoForge chunk serializer, durable atomic disk publication, native save/reopen/edit, water conservation/long soak, OpenGL/Vulkan GPU, LOD/performance or verified JAR. Owner retains full build/JAR responsibility under the October 27 instruction. Git CLI DNS could not resolve github.com and the complete local checkout/JDK25 were absent; guarded GitHub connector commits were used. No existing JAR was deleted.

Water foreground claim was expired; renderer-owned files and output resources were not touched. Root `ENGINE-WORK-STATE.json` remains historical Dev53 and was not claimed; `tools/validate_engine_work_state.py` could not be run on a complete pinned checkout. Independent full grade remains **50/100 (5.0/10)**, not regraded. Next: authoritative durable owner/serializer, native signed-border save/reopen/edit fixtures, V7 relief parity and finite-water ledger integration.
