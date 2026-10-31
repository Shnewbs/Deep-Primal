# Dev121 independent audit — 2026-10-31 05:26 UTC

**Pinned source:** `990a6b333d828e47a9612f3a56da8eea8f9cd06e` (`0.0.1-alpha.29-dev.121-native-fill-publication.1`). **Correction commits:** `5b1c7ac9929065eb842515cacd2b89ff86ec1ced`, `f1e8348a6f7bd03fa4c9851c78f07fcc29ad4a1c`. Corrections are newer than the pinned snapshot.

## Confirmed and fixed

[NativeFill.java L108–114](https://github.com/Shnewbs/Deep-Primal/blob/990a6b333d828e47a9612f3a56da8eea8f9cd06e/src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeFill.java#L108-L114) unconditionally used `failure.addSuppressed(discardFailure)`. A native callback and its cleanup may rethrow the *same* Throwable. Java then throws `IllegalArgumentException: Self-suppression not permitted`, masking the original error. The exact pinned production blob `1130a888fe9d6077bfef7984920da7bda845577f` reproduced the failure. The guarded fix checks `discardFailure != failure` before suppression; committed test covers the shared-instance error. Fixed production blob `81c327777eff56b7c73215d13a803d5bdaf5b6cb`, SHA256 `4688eebeb631db3c5265893a8e826caea3c3786533e029480ee6359531f36224`.

## Independent validation

Linux x86_64 OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **336,006 focused checks passed** over 12,000 signed-coordinate scenarios plus shared-error tests. Real pinned production NativeFill, NativeCommit, NBT adapter and three engine-core classes were compiled with isolated Minecraft/chunk/materializer/profile doubles. The unpatched source failed the new shared-error reproduction; a no-discard compiled mutant failed. Original production blobs and fixed Git blob identities matched. This is not full NeoForge/Java25 or real native save acceptance.

Three commits since Dev120 introduce the explicit Dev121 native fill/publish lifecycle and released scoped state. No renderer/water/LOD source changed in that diff. This opt-in helper still lacks registered NeoForge owner/serializer and durable atomic chunk+NBT+epoch publication. Root work state still records historical Dev53; it was not edited.

**Unrun:** Java25/Gradle9.2.1 full build, package CRC/class identity, native save/reopen/shutdown, finite-water conservation, Windows AMD8060S Vulkan/OpenGL and GPU performance, NeoForge 26.3/26.4. Current declared NeoForge version is `26.1.2.122`. Complete isolated checkout, JDK25 and network dependencies were unavailable. **No JAR produced or qualified, no old JAR deleted, no GitHub Actions or releases.**

**Grade:** historical ten-category baseline **50/100 = 5.0/10 (Tier 2)** carried, *not* a fresh complete regrade. Categories: terrain 5, biomes 4, hydrology/caves 6, features 4, performance 3, determinism 7, architecture 6, extensibility 4, testing/docs 7, polish/tooling 4. Strict >8.5 remains unproven. [Rubric](../../engine-review-2026-10-24/REVIEW.md).

**Next:** owner Java25 full compile/package; native durable V8 owner and signed border save/reopen; finite-water ledger; Windows Vulkan/OpenGL/LOD GPU/VRAM/timing acceptance. No builder lease/checkout/cache/output or timing reservation was touched.
