# Deep Primal dev101 — prepared V8 vegetation receipt handoff

## Implementation

Added DensityChunkMaterializer.consumeSavedEcotoneVegetationReceipt(...) as the concrete downstream handoff for the V8-r6 native vegetation receipt path. The method requires the prepared revision-6 profile, destination chunk, exact planned vegetation plan and postwrite receipt, then delegates to Prepared.consumeVegetationReceipt(...).

This keeps the existing fail-closed profile-and-signed-chunk admission and returns only realized planned sites. It does not synthesize replacements, mutate the chunk, modify terrain geometry, change physical water, rewrite saved profiles, or touch renderer-owned code.

## Validation boundary

tools/test_v8_prepared_receipt_handoff.py is a focused source guard only. No Java25/Gradle compilation, native Minecraft execution, saved-world reopen, GPU validation, or complete water-engine acceptance was performed in this cycle. The carried independent baseline remains 50/100 (5.0/10); no regrade is claimed.
