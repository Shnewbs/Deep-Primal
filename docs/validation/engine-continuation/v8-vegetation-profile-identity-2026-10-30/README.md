# Deep Primal dev96 — V8 vegetation receipt profile identity

## Source change

`V8VegetationNativeReceipt` now carries the saved V8 vegetation-profile fingerprint in addition to the absolute chunk origin.

- `capture(...)` remains source-compatible and explicitly creates an unbound legacy receipt with fingerprint `0`.
- `captureBoundToProfile(...)` records the profile identity supplied by the owning profile codec.
- `matchesProfile(...)` and `requireSameProfile(...)` let native callers reject a receipt produced by another saved profile before applying the observation.
- The receipt remains immutable and the existing chunk-origin guard is unchanged.

This is an engine-mechanics guard only. It does not hash mutable chunk state, rewrite old saves, activate native V8, or claim native save/reopen acceptance.

## Validation

The focused source check `tools/test_v8_vegetation_receipt_profile_identity.py` passed against the committed files. This automation cycle did not run Java25/Gradle, Minecraft, GPU, or native save/reopen validation.

Current implementation commit: `79de9a8c5e18045558f2e8c607dc46b4d133d26c`
Focused guard commit: `de76136fbacac8d6a985dea7177d35ac12c6145a`
Current observed main after these guarded writes: `de76136fbacac8d6a985dea7177d35ac12c6145a`
