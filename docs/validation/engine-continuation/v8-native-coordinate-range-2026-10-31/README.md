# Dev123 — signed V8 native block-coordinate prewrite admission

Base: 6a68aef5869b91fa9f5c3004d0e98b2a5003711c (after the bounded scoped lease). Consumed independent audit: docs/engine-hourly-audits/2026-10-31T0633-84208b59/REVIEW.md pinned 84208b59eaa7f72370b3f82b8dc4a33c3932860c. Its early durable-epoch exhaustion repair is preserved.

## Implementation

Minecraft ChunkPos block origins use 32-bit coordinates. At chunk X/Z outside [-134217728, 134217727], shifting a chunk index into a block origin can wrap into a different coordinate, aliasing native setters and absolute-coordinate climate/material sampling. Explicit new-V8 native terrain and vegetation publication now reject any chunk whose 16x16 footprint cannot be represented in signed int world block coordinates. Math.multiplyExact and Math.addExact check both axes and the far corner before the owner ticket, materialization or native setters. The two signed extremes remain valid. Existing V1-V7, saved V8 profiles, water quantities, renderer files and player edits are unchanged.

## Validation and limitations

Linux OpenJDK 21.0.12.1: a standalone strict Java21 check of the same coordinate-range algorithm passed **200,121** assertions, including 200,000 seeded signed-coordinate pairs and edge-case Cartesian products. This was a standalone algorithm harness, not a compilation of the full production NativeFill class. The repository's API-double regression test was extended to exercise both terrain and vegetation rejection, owner/setter non-entry, signed edge acceptance, and a compiled missing-guard negative control; **that repository runner was not executed in this environment** because the complete checkout is unavailable. No full Java25/Gradle, Minecraft native save/reopen, GPU, water conservation or performance acceptance was run. No new qualified JAR was produced or old JAR removed. Owner retains full builds under October 27 instruction.

Next: real durable chunk+stamp save hook and crash-safe atomic publication; signed old/new generation seam/reopen acceptance, V7 relief parity and finite-water ledger integration. Independent full grade remains 50/100 (5.0/10), not regraded.
