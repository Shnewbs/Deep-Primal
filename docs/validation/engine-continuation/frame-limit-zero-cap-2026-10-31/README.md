# Dev137 — native-unlimited background FPS ceiling (2026-10-31)

## Scope and cause
`VisualBudget.frameLimit` uses a zero native frame-limit value as an uncapped sentinel. Its old `Math.min(nativeLimit, cap)` returned zero whenever the native limit was zero, even if the unfocused/minimized configuration requested a positive ceiling. This prevented the background FPS policy from applying in the zero-sentinel case. The fix treats zero as unlimited **only when a positive configured ceiling applies**, while preserving zero in the focused/unrestricted case, lower positive native caps, disabled overrides, and minimized precedence.

The existing `graphics-settings/.../FrameLimitMixin.java` invokes this helper; no mixin, native Vulkan, water, worldgen, LOD, save/profile, asset or configuration file was modified.

## Evidence
- Baseline exact-source Java 21 `--release 21 -Xlint:all -Werror` compilation succeeded; new checks **failed** at native=0, minimized=true, hidden=1 (expected 1, actual 0).
- Fixed isolated Java 21 compile and `VisualBudgetFrameLimitChecks`: **PASS 100565 checks**, 560 enumerated combinations + 5 direct invariants + 100000 seeded trials.
- A deliberately defective `return cap` control fails at native=1, minimized=true, hidden=5, proving that a lower native cap is not allowed to rise.
- New checks are registered in the existing `CoreChecks` entry point, but the **full** suite was not run.
- Production source SHA-256: `7c96fe8aec9160ba9a5ee1d0202eb5475bf44d2bdff525ba44de32ceb4362989`; test SHA-256: `ee88ce5df1e3757099ee11c9d11662999d4dfd0de944c8f0cf22f34028e671e3`.
- Consumed independent audit: `docs/engine-hourly-audits/2026-10-31T2030-914fca38/REVIEW.md`. Its 5.0/10 whole-engine grade is historical; this patch is **not** a regrade.

## Unrun gates and blocker
The isolated environment has JDK 21 only, no complete private checkout or Gradle dependency cache, and `git ls-remote` fails DNS resolution for github.com. Therefore no Java25/NeoForge build, Minecraft client/frame pacing measurement, native GPU test, qualified game JAR, JAR SHA-256 or GitHub Actions run is claimed. No existing JAR was deleted. The next full owner build should test uncapped foreground -> unfocused -> minimized -> refocused with both Vulkan and OpenGL, and validate frame pacing under actual window transitions.

The existing claim for this scope is marked completed/released; changes are bounded to the helper, new regression, CoreChecks registration and evidence files.
