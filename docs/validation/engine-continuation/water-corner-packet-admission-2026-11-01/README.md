# Deep Primal dev153 — GPU water-corner packet admission

**Date:** 2026-11-01. **Branch:** `main`. **Scope:** `WaterFlowField` float-exact direction and age packing; no native fluid quantity, renderer, worldgen or saved-world changes.

## Defect and correction

A malformed 5-bit direction (17..31) in any of the four packed corner lanes was passed directly into the GPU float packet. High bits from malformed input could also collide with the fourth-corner age. The legacy target/source packing silently truncated ages above 127 and could lose float-exactness for unconstrained words. The original production blob `943cee26227e1d3901a625a894bdf86a3bd7b4e6` failed the new regression at `raw=557056 age=15`.

The corrected `safeDirections` masks unrelated high bits and converts each invalid lane to the defined still-water direction 16, preserving the other lanes. `targets`, `sources` and `heightClock` clamp ages to 0..100 ms. Valid directions 0..16 and ages 0..100 retain bit-identical packets. This protects the GPU packet boundary, but is not a claim that native waterfall flicker has been resolved.

## Local isolated validation

- OpenJDK 21: `javac --release 21 -Xlint:all -Werror`; runtime `java -ea -Xverify:all`.
- New regression: **8,340,112 assertions passed**, including all **1,048,576** four-lane five-bit words and **100,000** randomized malformed/valid packet pairs.
- Existing `WaterFlowFieldChecks`: **8,435,621** valid codec cases, **98,307** height clock cases, **34,000** halo-edge checks; all passed with minimal compile-time dependency stubs matching the relevant core interfaces.
- The old source fails the new test (exit 1); the modified source passes.
- Production Git blob: `16e41cd7c9e80d442aa75b33d2c99503c2448063`; source SHA-256 `8f885f682f8c183c9f9d2e8a5d965d23d8ee8b685dec45903e3a3e2d4666476a`.
- New regression Git blob: `d5b7303029f8f09db8253092dd11a990404e1401`; SHA-256 `ff4a7bb346f1e9c25a2a0cbf203e7780b3e3689c1dc9248cd3c75f4ed5ab6d86`.
- Regression is registered in `CoreChecks`. A small standalone Java21 stub runner is included in the locally delivered evidence archive, not as production code.

## Acceptance limitations

This is an **isolated source-only** fix. No complete local private checkout, JDK25, Gradle/NeoForge cache, native GPU test, full CoreChecks run, native water save/reopen, or playable JAR is available in this execution environment. No JAR SHA-256 exists for dev153. The prior qualified JAR remains untouched; no GitHub Actions used. Whole-engine historical grade remains **5.0/10** pending independent comprehensive revalidation. Consume the latest independent dev152 audit and preserve its committed extreme-precision waterway correction.
