# Deep Primal dev97 — prepared V8 vegetation profile binding

## Implementation

The explicit V8-r6 native read-back path now derives a stable identity from the immutable saved V8GeneratorSnapshot and captures receipts through captureBoundToProfile(...).

- V8GeneratorSnapshot.vegetationProfileFingerprint() returns 0 for pre-r6 snapshots and a deterministic mixed identity for r6 snapshots.
- DensityChunkMaterializer.fillSavedEcotoneVegetatedWithReceipt(...) now records that identity in the postwrite receipt.
- The existing chunk-origin guard and legacy unbound capture(...) API remain unchanged.
- This prevents a receipt from one immutable saved vegetation profile being reused by a different prepared profile without altering saved wire formats.

## Validation

tools/test_v8_vegetation_profile_binding.py passed as a source-level guard against the committed files. This cycle did not run Java25/Gradle, Minecraft, native save/reopen, GPU, or water-runtime acceptance.

Source commits:
- 444080f408ffae5406b266725c832d8101cc965a — snapshot profile identity
- d067205ae2faecab9b8ce670e98393ecd1c7c0b0 — materializer receipt binding

The independent baseline remains 50/100 (5.0/10); no regrade is claimed. No playable or qualified JAR was built or published.
