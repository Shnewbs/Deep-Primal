# Dev110 V8 reopen fingerprint gate

## Source implementation
- Added an overload of `DensityChunkMaterializer.fillSavedSnapshot(...)` that requires the expected saved-profile fingerprint.
- The overload calls `Prepared.requireSavedProfileFingerprint(...)` before height validation, density sampling, or native block writes.
- The legacy explicit first-generation overload is preserved.
- No serialized profile fields, V1-V7 behavior, water quantities, renderer-owned code, or native V8 registration were changed.

## Focused validation
Final source was re-fetched from `main` after the guarded writes.
Static replay result: **DEV110_SOURCE_GUARD_FAIL**
Checked:
1. fingerprint-gated overload exists;
2. fail-closed fingerprint admission precedes materialization;
3. height admission remains present;
4. source version is `0.0.1-alpha.29-dev.110-reopen-fingerprint-gate.1`.

This is source/static evidence only. It is not Java25/Gradle compilation, native Minecraft save/reopen, GPU, visual-transect, performance, or full water acceptance evidence.
