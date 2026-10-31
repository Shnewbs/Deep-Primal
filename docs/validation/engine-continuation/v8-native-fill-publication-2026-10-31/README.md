# Dev121 — explicit native V8 materialize-and-publish lifecycle (source-only)

**Base:** main `a5553fd99b2c3d912158dd589dab56187db07e9a`; bounded scoped claim at `49e2d60945a09bda2ef08d672a7faa225f7a825c`. **Version:** `0.0.1-alpha.29-dev.121-native-fill-publication.1`. Latest available completed independent audit: `docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md`, pinned `c57d2924562a7dd35eed007a164205953bb13061`. The auditor carried 50/100, not a new regrade.

## Actual source implementation

`V8ChunkGenerationNativeFill` connects the existing explicit V8 terrain and V8-r6 vegetation/receipt materializers to the dev120 durable-border ticket and NBT publication coordinator. The entry points check saved-profile fingerprint, revision, signed chunk coordinates and vertical range **before** obtaining the owner's border ticket or performing any native setter. They call `V8ChunkGenerationNativeCommit.begin` before materialization, then `commit` only after materialization returns successfully. The r6 path returns a realized vegetation receipt only after publication succeeds.

A mandatory caller-provided discard callback runs on materializer exceptions, callback `Error`, failed commit and owner publication rejection; a discard exception is suppressed on the original failure. A rejected candidate must be discarded in full, not merely unmarked, and must not be retried. Preflight and border admission failures occur before writes and do not invoke discard. Existing explicit V8 and frozen V1–V7 paths remain unchanged. No saved-profile wire changes, water quantities, rendering files or player edits are modified.

**Still not a registered Minecraft generator/chunk-save hook.** This opt-in integration requires an authoritative native owner with durable consistent target/four-neighbor snapshots, atomic chunk+NBT+epoch compare-and-publish, crash/reopen recovery, and a real discard implementation. It does not activate V8 or prove old/new border continuity.

## Reproducible checks

Run `python3 tools/test_v8_native_fill.py` and `python3 -OO tools/test_v8_native_fill.py`. OpenJDK **21.0.12.1**: `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`. Each Python mode executes two deterministic Java processes with `V8_NATIVE_FILL_PASS assertions=25000` per process. **100,000 positive assertions total**, with four compiled defective variants rejected in each mode (8/8 total): omitted fingerprint, omitted discard, skipped durable commit and skipped signed-coordinate check. Exact new production source SHA-256 `c9d01326f34571a72c93ab2542d53d8cbfd4c5db1b684e279cd472db3943e664`; Git blob `1130a888fe9d6077bfef7984920da7bda845577f`. Runner, test and all 11 stub Git blobs were checked against locally tested bytes.

**Limits:** API-double source tests, not real Minecraft/NeoForge, not the complete production NativeCommit or materializer compile, not actual native NBT save/reopen, not durable transaction, not real GPU or performance, and not finite-water acceptance. The Java25 archive/toolchain and complete checkout are absent in this runtime; Git CLI DNS cannot resolve GitHub. No full Gradle build, JAR, release or old-JAR deletion occurred. The owner's confirmed playable dev53 remains the last confirmed JAR; October 27 owner full-build responsibility is preserved.

**Next:** wire this explicit lifecycle into a real chunk owner and registered serializer using the actual NeoForge API; verify atomic publication/reopen, edits and signed old/new borders. Execute the full V5/V7 graph checks and continue finite-water ledger authority integration in nonconflicting scopes. Water and renderer files were not touched.
