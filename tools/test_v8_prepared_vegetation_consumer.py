#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
snapshot = (root / "src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java").read_text()
consumer = (root / "src/main/java/com/shnewbs/deepprimal/engine/V8VegetationReceiptConsumer.java").read_text()

needles = (
    "consumeVegetationReceipt(",
    "snapshot.version() != VEGETATION_VERSION",
    "V8 vegetation receipt requires revision 6 snapshot",
    "V8VegetationReceiptConsumer.consume(this, chunk, planned, receipt)",
    "requireCompatibleVegetationReceipt",
    "admitted.realized(x, z)",
)
for needle in needles:
    if needle not in snapshot + consumer:
        raise SystemExit(f"missing prepared-consumer guard: {needle}")
if "return V8VegetationReceiptConsumer.consume(this, chunk, planned, receipt);" not in snapshot:
    raise SystemExit("prepared profile does not own downstream admission")
print("DEV100_PREPARED_VEGETATION_CONSUMER_GUARD_PASS")
