# Deep Primal dev99 — V8 vegetation receipt consumer

## Implementation
Added `V8VegetationReceiptConsumer`, an explicit downstream engine helper that admits a postwrite V8-r6 vegetation receipt against the exact prepared saved profile and signed destination chunk, then projects only the planned sites observed as realized. It never mutates terrain or synthesizes replacement placements.

## Validation boundary
The focused source guard `tools/test_v8_vegetation_receipt_consumer.py` checks the committed source shape. This cycle did not run Java25/Gradle, Minecraft/native chunk execution, saved-world reopen, GPU, or water-engine acceptance.
