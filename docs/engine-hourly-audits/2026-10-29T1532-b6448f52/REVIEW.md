# Deep Primal independent Dev81 pinned audit — 2026-10-29T1532-b6448f52

**Pinned main:** `b6448f52fca405089f5dd73f611ededd2dbd9547`; changed engine source commit `5d9a82d7ec95e579afda5810b7d5c19b5acdae4c`. Current source version: `0.0.1-alpha.29-dev.81-route-dag.1`. Renderer subsequently changed at `7241700af3eaf4da30b8787c07b3c42e08abd25b` and main later received the renderer-only documentation commit. This auditor made **no production source edits**.

## Independent verification (actual execution)

- Retrieved and read main, ENGINE-WORK-STATE, ENGINE-BUILD-AUDIT-HANDOFF, ACTIVE-ENGINE-AUDIT, ENGINE-BUILDER-AUDITOR, ENGINE-HOURLY-BUILDS and the Oct 24 ten-category REVIEW.
- Exact production [WatershedInteriorPortRoutes.java](https://github.com/Shnewbs/Deep-Primal/blob/b6448f52fca405089f5dd73f611ededd2dbd9547/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedInteriorPortRoutes.java#L51-L83) matched Git blob `6b527106571054e88352cc5bdf07b289fdca73a0`; supporting WatershedOutletTopology.java matched blob `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`. Reconstructed from retained exact Git-blob source plus committed Dev81 patch, validated again with `git hash-object`.
- Restored Linux Temurin JDK25.0.5+8 from owner's Library archive. `javac --release 25 -Xlint:all -Werror` compiled **exact pinned production helpers** and independent tests. Two independent oracle runs passed **88,017 assertions each**, across 1,600 deterministic randomized signed-coordinate receiver graphs and 6,036 verified outlet routes per run. Independent cyclic-input/parallel/replay regression passed another **36,010 assertions** including 12,000 randomly placed reciprocal parent cycles and a 257-cell-side region. **Java25: 212,044 positive assertions.**
- Linux Java21 strict compilation and the same oracle/fuzz tests passed another **124,027 assertions**. **Total successful assertions 336,071.** The prior exact Dev80 helper *failed* the same Dev81 oracle on its disconnected-cycle witness: negative regression effective.
- [Dev81 source lines 64–83](https://github.com/Shnewbs/Deep-Primal/blob/b6448f52fca405089f5dd73f611ededd2dbd9547/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedInteriorPortRoutes.java#L64-L83) correctly reject disconnected receiver cycles before even a valid-looking boundary can be exported. This is source-logic safety, not a native water-conservation acceptance.
- Evidence ZIP `DeepPrimal_Dev81_IndependentAudit_20261029T1532_b6448f52_Java25Evidence.zip`, **19,438 bytes**, SHA256 `200a65ee48b65a5c11232d0f8517af4ff9a0a7e1bb465bd1fca41c92293a49f6`, saved to Library `/Deep Primal/Independent Audits/`. CRC and all 22 member SHA256 entries rechecked.

## Explicitly unrun / remaining defects

**No full development JAR was produced.** The October 27 owner override reserves full Gradle/Minecraft/GPU builds and publication to the owner. Complete pinned checkout/Gradle9.2.1/NeoForge, packaged nested/outer CRC, source/packaged class identity, Windows AMD8060S Vulkan/OpenGL, native save/reopen/unload and physical finite water/salt/sediment conservation **not run**. Linux Java25 tests are not Windows acceptance. No old JAR renamed, published, or deleted.

Dev81 is still an **opt-in observational** regional route export, not authoritative globally certified sea outlets, interior→cross-region discharge/constituent ownership or native V8 save/restart. Root ENGINE-WORK-STATE is stale at Dev53 while scoped TERRAIN-BLENDING-WORK-STATE confirms Dev81 completion. New renderer page adjacency evidence is Java21 CPU fixture only; not GPU frame-time acceptance.

## Ten-category grade — carried baseline, NOT fresh regrade

Terrain 5, Biomes 4, Hydrology/caves 6, Features 4, Performance 3, Determinism 7, Architecture 6, Extensibility 4, Testing/docs 7, Polish/tooling 4. **Total 50/100 = 5.0/10, Tier 2**, per `docs/engine-review-2026-10-24/REVIEW.md`. The fresh complete strictly-greater-than-8.5 threshold remains unproven. An opt-in source helper earns no gameplay grade inflation.

**Next:** authoritative once-only physical discharge and global outlet certification; V8 native seam/save/edit/conservation; actual owner Java25/NeoForge packaged build and two-API AMD GPU + water acceptance; comprehensive evidence-based regrade. No Builder timing reservation/lease modified; audit does not gate Builder progress.
