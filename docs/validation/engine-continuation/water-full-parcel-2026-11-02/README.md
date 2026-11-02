# Dev214 — full-volume water constituent fast path (2026-11-02)

## Audit and scope
Consumed [Dev213 independent audit](../../../../engine-hourly-audits/2026-11-02T2030-dev213/REVIEW.md). No new confirmed defect; the native build, conservation and GPU gates remain open. Writer claim is in [CLAIM.json](CLAIM.json).

## Implementation
`WaterAccount.portion(mass, amount, volume)` now returns the original constituent mass when `amount == volume` after the existing zero checks. Full-volume water transfers therefore avoid unnecessary multiplication and `BigInteger` allocations while preserving exact salt/sediment conservation. Partial-transfer rounding and all admission logic are unchanged. The new `WaterAccountFullParcelChecks` is registered in `CoreChecks`.

## Reproducible isolated evidence
- Pinned original `WaterAccount.java` Git blob: `3e18f72b42f32569fd7fa2c7c3751af12508dee2`.
- Candidate `WaterAccount.java` Git blob: `0b38c08342182152af0397f3102ecd8f84e9b951`.
- Registered regression Git blob: `b2e5c1a20fa05eacb5d1e625c5459f81e387a40d`.
- Strict OpenJDK 21 `javac -Xlint:all -Werror` and `java -Xverify:all`: baseline and candidate each passed **700,000** checks, identical digest `79e36dba79042795`.
- Existing `WaterAccountDebitChecks` passed **150,000** checks against candidate.
- Isolated thread-allocation fixture, 100k warmup + five 100k-preview rounds: median **744 bytes** per full preview before, **40 bytes** after (**94.6% less**). Not a native tick/FPS benchmark.
- Reproducible evidence ZIP SHA-256: `aec0943a8c0f3f3e0d4b0ec4cafe7f916aa7b386df1af801e372a9c35a5df9b1`.

## Build and release limits
No complete private checkout, Java 25, Gradle/NeoForge dependency cache or local GitHub DNS. `git ls-remote` failed DNS. Therefore no full production compilation, playable JAR, or JAR SHA-256. No old qualified JAR deleted; no GitHub Actions invoked. Whole-engine grade not remeasured.

Next: restore the complete Java25 production build; validate CoreChecks, native save/reopen water conservation, long soak, waterfall GPU continuity, Vulkan/LOD frame/tick metrics.
