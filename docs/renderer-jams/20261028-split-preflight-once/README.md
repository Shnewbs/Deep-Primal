# Renderer jam 16 — single-pass optional quad split preflight

Production commit [`0daeacbb7f5c3f26893a0c258341290abb96fa5c`](https://github.com/Shnewbs/Deep-Primal/commit/0daeacbb7f5c3f26893a0c258341290abb96fa5c) avoids verifying canonical quad indices twice for each oversized opt-in 32KiB mesh. This is **off by default**, alters no water/terrain/shader paths and grants no verified Minecraft FPS or GPU-memory gain.

Isolated Java21 tests: source-pinned candidate and baseline each pass 14,154,029 exact/parity assertions (750 randomized scenarios, same digest), committed smoke passes 256 seeded cases/504 pieces, inverted-decision mutant is rejected. Three paired same-process CPU fixtures show 14.0–18.9% lower medians, mixed p99 and only 24 B less per-call allocation. See [self-audit](REVIEW.md), [receipt](RECEIPT.json), [released claim](CLAIM.json), [committed smoke](LodSplitPreflightSmoke.java). Native Java25/Gradle/GPU/JAR gates remain owner-operated and unrun.
