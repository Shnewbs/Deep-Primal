# Dev123+ independent audit — 2026-10-31

Pinned main before audit: `f3afacbe718a0dce3d7f14ed341169d4eeba6432`; version `0.0.1-alpha.29-dev.123-world-coordinate-admission.1`. Corrective code commit: `8bc672f4c0c5dbce81255aae7e9c8d7586a2c906`.

## Confirmed and corrected: invalid generated epoch zero

At the pinned source, [V8ChunkGenerationNbtAdapter.java lines 31–35 and 53–62](https://github.com/Shnewbs/Deep-Primal/blob/f3afacbe718a0dce3d7f14ed341169d4eeba6432/src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNbtAdapter.java#L31-L62) admitted a `Generated` identity at durable epoch zero and a correctly checksummed zero-epoch marker. That allowed a certified GENERATED neighbor to present an epoch that no publication through [V8ChunkGenerationNativeCommit.commit](https://github.com/Shnewbs/Deep-Primal/blob/f3afacbe718a0dce3d7f14ed341169d4eeba6432/src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeCommit.java) could have created: native publication increments a nonnegative target epoch by one.

The fix rejects zero in the `Generated` constructor and the canonical decoder, retains the seeded positive-epoch tests, and adds rejection tests for constructor zero, a correctly checksummed zero marker, and generated-neighbor zero. The test runner now checks the corrected count and a compiled zero-guard mutant. No legacy geography, saved profiles, finite-water quantities, player edits, renderer, LOD, or builder state was modified.

## Exact-source evidence

- Pinned original production blob: `b57e311d165a826f0cf9c8a8aa6c83266680c38e`; original test blob `89172509fca3cc498ef9802d0b24db591b8fa2e8`; original runner blob `30351eca2269714954765182565470b3a634cc39`.
- Corrected blobs: adapter `33782f5ef55b904c4cb3c3ac261d794ef5306c0e`, test `5a807345333644989290c3128055567c63c4ab83`, runner `3a5724503bce97d7ac798cce9e1b560b81f8dc99`.
- Exact engine-core dependencies compiled: `V8ChunkGenerationCommitGate` blob `08a3b667dc9b06a8f76299b5c01dc2d0a744cf4d`, `V8ChunkGenerationStamp` `0919a453d67956e3fd81185535df1fe2f2b613e2`, `V8GenerationBorderAdmission` `1b5ae577fc7f57fcff85c21a0ac67735fcafd349`.
- OpenJDK 21.0.12.1, strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`; Minecraft NBT API doubles, **not native Minecraft**.
- Original production + original test: 94,706 assertions pass. New test + original production: fails as expected (`accepted invalid input 94706`). Independent zero-epoch test also fails pre-fix.
- Corrected production + corrected test: **94,709 assertions/process, six positive processes (568,254 assertions)**, normal and Python `-OO` runner modes. Three compiled negative mutants rejected per runner execution, three executions, nine rejections. Independent zero-epoch test passes 13 checks.
- Local evidence: `DeepPrimal_Dev123_IndependentAudit_20261031T0733_f3afacbe_8bc672f4_Evidence.zip`, 17,791 bytes, SHA256 `7243b50f1a7cb34525e8a46730ee7917841aeec8673b00ca13b372646520b270`; ZIP CRC verified.

## Build/acceptance limits

No full Java25/Gradle 9.2.1 build or current JAR: isolated runtime lacks a complete checkout, local JDK25 and Gradle dependencies; Git CLI DNS failed to resolve GitHub. No packaged archive/class/payload checks, native save/reopen/shutdown, Windows AMD8060S Vulkan/OpenGL/LOD/FSR/DLSS acceptance, full water conservation, real performance benchmark or NeoForge 26.3/26.4 test. Declared NeoForge target remains `26.1.2.122`. No older JAR was deleted, no release/Actions was used. The October 27 owner-build override remains in effect.

## Independent grade

**50/100, average 5.0, Tier 2 — last accepted full baseline carried forward, not a fresh complete regrade.** Rubric: [REVIEW.md](../../engine-review-2026-10-24/REVIEW.md). Equal categories: terrain 5, biomes 4, hydrology/caves 6, features 4, performance 3, determinism 7, architecture 6, extensibility 4, testing/docs 7, polish/tooling 4. This focused safety fix is not gameplay acceptance or evidence to inflate the whole-engine grade. The required fresh average is **strictly >8.5**.

Next: registered atomic native chunk+stamp persistence and crash recovery; actual save/reopen and old/new V8 signed-coordinate borders; finite-water ledger and frozen V1–V7 replay; complete Java25 packaging; matched Windows Vulkan/OpenGL and real worldgen p50/p95/p99. Builder checkout, lease, caches, outputs, and timing reservations were not touched.
