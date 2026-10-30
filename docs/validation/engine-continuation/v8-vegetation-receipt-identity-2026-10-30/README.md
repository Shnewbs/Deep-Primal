# Dev95: world-bound V8 vegetation receipts

Source-only checkpoint `0.0.1-alpha.29-dev.95-receipt-identity.1`.

## Implementation

`V8VegetationNativeReceipt` now records the absolute chunk minimum X/Z at capture time, rejects impossible planned/realized counts, and exposes `matchesChunk` / `requireSameChunk` so a postwrite vegetation mask cannot be silently reused on another signed-coordinate chunk. The read-back bitmap remains defensive-copy based and the existing capture path is unchanged except for attaching its chunk identity.

This is a bookkeeping/authority guard for the explicit V8-r6 vegetation path. It does not activate V8, change terrain, alter water quantity, or claim player-edit/save durability.

## Validation

A source-level Python guard was added at `tools/test_v8_vegetation_receipt_identity.py`. It checks the committed implementation anchors and fail-closed count/origin checks. It is not a native Minecraft or Java25/Gradle test.

Owner-controlled full Java25/Gradle, native chunk write/readback, save/reopen, callback, GPU, signed old/new-generation border, and water-engine acceptance gates remain open. Historical independent baseline remains 50/100 (5.0/10); no regrade is claimed.
