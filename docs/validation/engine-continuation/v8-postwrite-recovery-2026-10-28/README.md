# Dev52 — bounded synchronous V8 block-write recovery

Independent audit consumed: [2026-10-28T1030-10cb0c6/REVIEW.md](../../../engine-hourly-audits/2026-10-28T1030-10cb0c6/REVIEW.md), pinned main `10cb0c6bfcc32492e573aa8c282da75d13864e91`. Last accepted full ten-category grade **50/100, average 5.0/10, carried without regrade**. New version `0.0.1-alpha.29-dev.52-v8recover.1`.

## Scoped implementation

Only the opt-in V8 `DensityChunkMaterializer.fillPlannedSurface` now journals the native destination's original block states once per unique cell before any setter. Any synchronous setter fault (including a setter which writes then throws), overlay fault, or final heightmap priming exception triggers reverse-order restoration of touched cells and re-priming of both derived heightmaps. A later topsoil repaint preserves the original prior-to-raw state, not the intermediate stone. Recovery failures are attached as suppressed exceptions instead of hiding the primary failure. A failed first read without any changes causes no heightmap mutation. Bounded journal: three arrays of at most `256 * 2048` entries; no neighbor/world/cache or disk access. Source Git blob: `1399606ac7067a19dbf7e4529821135c161de7fa`; previous source blob: `58c4b7d0f04435620858c1eef919d50ec171f15f`.

**Important limitation:** Block-state/derived-map recovery is best effort, **not a complete atomic Minecraft transaction**. Native callbacks, block entities, lighting notifications, scheduled tasks or disk I/O may have other effects. If native rollback reports suppressed failures or escaped side effects, the owner/native caller must discard and regenerate the affected chunk. The path is still unregistered; no V1–V7 geography/profile/edit, water-volume/chemistry, renderer, resource/theme or approved-art changes.

## Focused source validation

Actual changed source compiled with Java **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, using isolated Minecraft/Mojang API doubles, then `java -ea -Xverify:all`. Six existing V8 suites: **13,975,797 assertions passed**. New `V8PostWriteRecoveryChecks`: **152,803 assertions**, three setter faults (postwrite at native setter calls 1 and 1000, and mid-topsoil after 24,579 writes), one heightmap fault mid-prime, **65,536 original voxel-state comparisons**, both heightmaps restored, **25,577 rollback writes** in the injected setter cases. **Total 14,128,600 assertions passed.** An unchanged dev51 adapter was compiled against the same new test and **failed** the regression with unrecovered writes. Production source matched Git blob above.

Runnable test source and stand-ins: conversation file `DeepPrimal_Dev52_V8_PostwriteRecovery_SourceEvidence.zip` (**54,213 bytes; SHA256 `7552eeb18f4e6a2c890d566fd479305000aa9943696a1d85e10f010617199ec2`**, all ZIP CRC entries verified). These Java doubles are not native Minecraft execution; no runtime-performance benefit or general native transaction correctness is claimed.

## Open owner acceptance gates

Temurin JDK25 and its archive absent in this isolated workspace; Java25 and full Gradle/NeoForge build unrun. Under the **October 27 owner override**, owner performs all full builds, native save/reopen, Mojang JSON/NBT codec, heightmap/lighting/block-callback, Vulkan/OpenGL and Windows AMD8060S acceptance. No qualified new JAR, development prerelease, CRC/native payload validation or GPU frame-time claim. Water surface continuity and volume/salt/sediment across unload/restart/edits, multiplayer/soak, and bounded latency remain unaccepted. Auditor remains independent and the strictly-greater-than-8.5 threshold is unmet.

Next: native V8 transaction/caller-abandonment contract and exact save-profile/heightmap validation, benchmark the new bounded undo journal before selecting default V8; resume water ownership/chemistry work only after checking foreground claims.
