# Dev87 independent engine audit — pinned source 5f7c3f680e47bd29d03a90cd09c5c34e202f09e7

**Version:** `0.0.1-alpha.29-dev.87-plant-support.1`. Previous independent Dev86 audit source: `3df845f0b26511ce676d006ced01969e3aa00699`. Builder implementation: `8118f138d99d611070e70ef0ab7d04f2dc50ca76`. This is read-only product audit evidence, not a release or a new engine implementation.

## Independently executed

- Verified exact new production source [`V8VegetationNativeAdmission.java`](https://github.com/Shnewbs/Deep-Primal/blob/5f7c3f680e47bd29d03a90cd09c5c34e202f09e7/src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeAdmission.java#L23-L38), compiled from byte-for-byte Git blob `a2ec63c472a015aa3d16b140e4245c9f2d99fdec` against auditor-authored Minecraft API doubles.
- Linux OpenJDK21.0.12.1: strict `javac --release 21 -Xlint:all -Werror`, verified Java class major 65, and `java -ea -Xverify:all`. Two independently written test executions each passed **427,596 assertions / 106,648 cases**, covering signed ±30M coordinates, live block changes, wet/occupied/unsturdy support, nulls, no writes and native read ordering: **855,192 admission assertions passed**.
- Five independently compiled defective implementations (disabling expected support, dry support, sturdy face, air destination, or dry destination) each failed the independent assertion oracle. **5/5 negative controls rejected**; these are test mutants, not observed product failures.
- Replayed the previous Dev86 independent vegetation planner fixture under strict Java21: **152,491 assertions passed**; planner Git blob `df0c7a273eee94657ab28c4847872854d1f55e25` unchanged. Archived planner fixture's additional supporting model is not a production source identity check.
- **Fresh/replayed independent assertions total: 1,007,683**. Builder and renderer self-test assertion counts excluded.

## Scope / important gaps

Dev87 [`DensityChunkMaterializer.java` lines 322–346](https://github.com/Shnewbs/Deep-Primal/blob/5f7c3f680e47bd29d03a90cd09c5c34e202f09e7/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L322-L346) calls the new guard after raw and topsoil writes, before the r6 plant write, within the existing prewrite undo and final heightmap scope. The source changes touch explicit V8-r6 vegetation only. No new reproducible defect was found in this narrowly isolated guard.

**Integration observation:** `V8VegetatedResult.vegetation()` returns the original preflight placement plan even if live support/destination validation skips a plant. Treat this as a plan, **not a receipt of realized native placements**. Validate callback edits, plant survival, chunk/base-column/heightmap parity, rollback and saved-world reopening in the actual NeoForge runtime before claiming acceptance. No evidence of active native registration for r6 was established.

JDK25 and its owner archive were not present in this run. A separate direct Git CLI checkout failed github.com DNS; pinned source and documentation were inspected through connected GitHub read operations. Therefore **no full Gradle9.2.1/NeoForge build, JAR/class/package CRC, native save/reopen/shutdown, water conservation, or Windows AMD8060S Vulkan/OpenGL/GPU gate was run**. The October27 owner override reserves full builds and JAR publication for the owner; **no new development JAR** was produced, no artifact relabeled, no JAR retention change.

Root `ENGINE-WORK-STATE.json` still records Dev53 while scoped `TERRAIN-BLENDING-WORK-STATE.json` checkpointed Dev87; no root ownership fields, builder/renderer source, leases or caches changed. Neither R57/R60/R61 files nor any staged prototype promoted.

## Complete ten-category grade

**Carry-forward only, NOT a fresh whole-engine regrade:** terrain 5; biome 4; hydrology/caves 6; features/structures 4; performance/threading 3; determinism 7; architecture 6; extensibility 4; tests/docs 7; polish/tooling 4. **50/100 = 5.0/10, Tier 2** per [`2026-10-24 REVIEW`](https://github.com/Shnewbs/Deep-Primal/blob/5f7c3f680e47bd29d03a90cd09c5c34e202f09e7/docs/engine-review-2026-10-24/REVIEW.md). Unaccepted opt-in source features earn no gameplay score. Owner threshold strictly **>8.5/10** not demonstrated; leave independent audit enabled.

## Evidence / next action

Immutable local/Libraries ZIP `DeepPrimal_Dev87_IndependentAudit_20261029T2131_5f7c3f68_SourceEvidence.zip`, 18,128 bytes, SHA256 `aa576518bca5d7f9875b1521e377ca015a2d616a63352b38aa66064344d217a5`, 24 entries, all ZIP CRC and internal member SHA256 verified. Library: `/Deep Primal/Independent Audits/` (stable ID `libfile_f554b5c921b48191ba18277d96ef3113`). Includes exact product source, custom API doubles, independent test, mutation controls, Dev86 regression source/logs and receipt.

Next: owner Java25 full compile, real native r6 foliage/rollback/heightmap/profile/reopen; correct accounting of planned vs realized placements if callers require it; durable physical V8 finite-water integration and native Vulkan/OpenGL/waterfall performance tests. Audit does not stop the Builder.
