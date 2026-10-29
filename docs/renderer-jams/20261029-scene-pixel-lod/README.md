# Renderer jam 31 — LOD error in actual scene pixels

**ID:** renderer-20261029T1100-scene-pixel-lod. **Exact source/tests:** [cf578b2e36fcdbde04320cc2564c5b1cd89004d6](https://github.com/Shnewbs/Deep-Primal/commit/cf578b2e36fcdbde04320cc2564c5b1cd89004d6). **Mod version in unchanged gradle.properties:** 0.0.1-alpha.29-dev.76-native-outlet-ports.1.

Previously LOD mesh selection always derived focal pixels from the native window height. When spatial or actual AMD FSR1 upscaling redirects the world into a smaller scene target, that overestimates screen error and can pick unnecessary fine geometry. The renderer now reads the current valid UpscalingRuntime.frameTarget().height during world extraction, otherwise uses native height with **bit-exact legacy focal arithmetic**. Per-page selection, hysteresis, fine visibility, GPU draw order and transparent-water shader/masks remain untouched. Opt-in LOD telemetry now reports effective pixel height and whether a world target was active, reset with the world.

Source-only strict Java21 test: 2 × **501,280 PASS** (1,002,560 total) checks of native bit parity, scaled selection monotonicity and 200,000 fixed-seed selector cases; scaled cases included 28,529 coarser selections. Negative old/native-only implementation rejected at assertion 3. **19/19** Git-pinned static/source integration checks passed. These are behavior checks, not GPU FPS, pixel or frame-time measurements.

The October 27 owner override reserves full Java25/Gradle/Minecraft/GPU/JAR builds to the owner. No JDK25 archive or install was available in this run; no such validation, qualified JAR or release is claimed. No other scheduler or module touched.

[Full read-only renderer self-audit](REVIEW.md) · [Next renderer work](NEXT.md) · [Source/test receipt](RECEIPT.json). The 12,880-byte CRC/SHA256-verified source evidence archive was saved to Library /Deep Primal/Renderer Jams/ (SHA256 9578e6c648b4f84611761c3fa27852a6a953c4138ccd421d94cd10df9af02258).
