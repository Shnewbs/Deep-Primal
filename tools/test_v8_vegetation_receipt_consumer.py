#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=(root/"src/main/java/com/shnewbs/deepprimal/engine/V8VegetationReceiptConsumer.java").read_text()
for needle in (
    "requireCompatibleVegetationReceipt",
    "admitted.realized(x, z)",
    "unplanned site",
    "placements.clone()",
    "realizedCount()",
):
    assert needle in src, needle
print("V8_RECEIPT_CONSUMER_SOURCE_PASS")
