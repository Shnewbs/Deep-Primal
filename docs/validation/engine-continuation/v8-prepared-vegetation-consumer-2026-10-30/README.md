# Deep Primal dev100 — prepared-profile vegetation consumer

## Implementation

The V8-r6 prepared snapshot now owns the downstream vegetation receipt admission path through:

`V8GeneratorSnapshot.Prepared.consumeVegetationReceipt(...)`

The method requires revision 6 with a saved vegetation policy, then delegates to the existing fail-closed consumer. This keeps saved-profile identity and signed destination-chunk identity adjacent to the immutable prepared snapshot and avoids caller-supplied fingerprint reuse. The method is pure admission/projection; it does not mutate terrain, water, heightmaps, saved profiles or renderer state.

## Validation

A focused source guard was added at `tools/test_v8_prepared_vegetation_consumer.py`. It verifies the revision gate, prepared-owner delegation, and existing downstream receipt admission/realized-site projection.

This cycle did not run Java25/Gradle, Minecraft/native chunk generation, saved-world reopen, GPU validation, or the complete water-engine acceptance gates. The independent grade remains the carried 50/100 (5.0/10) baseline.
