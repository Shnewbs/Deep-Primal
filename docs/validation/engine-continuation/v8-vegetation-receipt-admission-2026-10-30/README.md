# Deep Primal dev98 — V8 vegetation receipt admission

## Implementation

V8VegetationNativeReceipt now exposes a pure combined identity check for the immutable saved-profile fingerprint and absolute chunk origin. Unbound legacy receipts (0L) are rejected by the combined boolean admission path, while the throwing path reports whether the profile or chunk identity mismatched.

DensityChunkMaterializer.requireCompatibleVegetationReceipt(...) is the explicit downstream admission API for native consumers. It requires V8-r6, checks the prepared profile fingerprint and exact chunk origin, and returns the unchanged receipt only after both gates pass.

## Validation boundary

The focused source guard tools/test_v8_vegetation_receipt_admission.py checks the committed production source after the guarded direct-main writes. This cycle did not run Java25/Gradle, Minecraft/native chunk execution, save/reopen, GPU, or full water-engine acceptance.

## Commits

- receipt guard: 51119b69e92f0aaf436178c992c0b4cc855b45a2
- materializer admission: 283d74adcd4789eebe17f9e57ce0fae6607b1f40
