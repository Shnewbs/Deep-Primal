# Deep Primal Dev159 — independent audit and rebase overflow correction

**Date:** 2026-11-01 17:30 UTC. **Previous audit:** `2988083597b17a3290de8453fed3a294a32f3f81`. **Pre-audit head:** `c18339d68585e925bb1c8562dac22e6acb3b8f91`.

## Commit review
Four commits since Dev157: one organic soil ledger implementation, one water-flow clock correction and two writer-lease claim/release commits. Changed paths and line counts were inspected through the GitHub compare API. No overlap with the completed Dev158 writer lease; the organics system remains independently owned.

## Confirmed defect and correction
`WaterCornerPacket.rebaseCorners` used `Math.max(0, now - queuedAt)`. Signed subtraction wraps negative for a queued timestamp near `Long.MIN_VALUE` and observation near `Long.MAX_VALUE`. This incorrectly treats a completed transition as age zero, potentially reviving its old source corner. Corrected by treating backward timestamps as zero and saturating positive signed subtraction overflow to `Long.MAX_VALUE`. This is a boundary robustness defect, not proof of a common gameplay issue. Ordinary elapsed calculations remain unchanged.

## Independent reproduction and tests
- Java 21 strict compilation of exact packet encoding/decoding/patching method excerpts plus the verbatim original `rebaseCorners`: PASS compilation, FAIL expected at `source retired corner 0 expected age 100`.
- Corrected method: PASS 30,072 assertions, four corner positions, signed-long boundaries, 10,000 deterministic ordinary cases.
- Independent elapsed arithmetic oracle: PASS 900,007 checks, including 200,000 overflow-boundary cases and 500,000 ordinary parity checks.
- Dev158 `WaterFlowTransition` implementation re-entered from GitHub source, tested against matching regression: PASS 500,011 checks. This is not a complete game compile or a byte-for-byte verified production file.
- Permanent `WaterCornerRebaseOverflowChecks` added and registered in `CoreChecks`; no GitHub Actions run.

## Build and compatibility gates
Repository `gradle.properties` pins NeoForge `26.1.2.122`, wrapper is Gradle `9.2.1`, and `build.gradle` specifies Java 25. The isolated environment has only Java 21; Git CLI cannot resolve github.com, and the complete private checkout plus Gradle dependencies are absent. Thus no full NeoForge build, verified playable JAR, or JAR SHA exists for this audit. No previous JAR was deleted. NeoForge 26.3/26.4 compatibility, Vulkan/OpenGL, LOD FPS, live finite-water conservation, save/reopen, and terrain seams remain unverified.

## Grade
Scoped water rebase arithmetic: **8.5/10 provisional**, after isolated regression. Whole-engine baseline: **5.0/10 historical**, not independently regraded. Native integration remains the release gate.

## Evidence artifact
`DeepPrimal_Dev159_IndependentAudit_20261101T1730_Evidence.zip`, SHA-256 `05b2f252f03921e3f3bdea08d71e33ad138571b0f737d281c3a7a151db526d5a`. Contains original failing excerpt, corrected passing excerpt, independent oracle, source-level Dev158 test, and logs. Archive integrity verified.
