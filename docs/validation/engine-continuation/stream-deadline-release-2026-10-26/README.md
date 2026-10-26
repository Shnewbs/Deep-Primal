# Stream-column deadline promotion — dev.30

The natural-source loop previously checked its deadline only at the page boundary, so a single admitted page could continue through all 256 source columns after its time allowance expired. Each source-column admission now checks the shared deadline. Survey and tick comparisons use signed elapsed subtraction, preserving behavior when `nanoTime` crosses its signed boundary. Headwater ordering, regional quota and exact water/constituent accounting remain unchanged.

This promotes the previously staged scheduler repair after restoring the complete current project and closing its full-build/native gates. Baseline source: `de95ee27dece40a76b15dd1e3a30a657eca50997`; the nonconflicting renderer claim at `9b26b8619b3a44c1542cc10f40a11658c4cd3cf0` is preserved.

## Fresh qualification

- Full Java 25 Gradle build: 35 executed tasks, exit zero, explicit core/module/resource pass records. Independent compilation: 134 sources; 144 core class hashes match the package.
- Exact tick extraction: baseline 216 characterization cases, candidate 1,266 cases. Slow-call reproduction: baseline 256 callbacks/255 late; candidate one callback/zero late. Wrapped clock: baseline admits none, candidate admits the expected 20.
- Actual survey extraction: baseline 38 cases/5,973 assertions; candidate 158 cases/27,752 assertions. Includes resume positions, unloaded pages and wrapped clocks. The generated candidate tick is byte-for-byte equal to the promoted method.
- Seven outer/nested archives pass CRC and duplicate-entry checks. 590 production class hashes match; 218 private test classes excluded. Themes, owner ground art, palettes, native payloads and notices verified.
- Ten packaged Linux dedicated-server sessions pass: default and channel-5 fresh/reopen, three aquifer persistence sessions, precipitation fresh/reopen and receiver capacity. Explicit native markers, saved flush acknowledgment, loaded-class identity where supported, failure precedence and ordered shutdown required. Channel-5 save reopens after its creation toggle is disabled.
- Packaged-core water-budget, 80,000 split-admission, waterfall/cliff continuity, 50,144 aquifer clock and LOD-bounds regressions pass.

`qualification.json` contains compact receipts. `build.log` retains the actual full-build result. Final committed-source producer identity, artifact checksum and delivery location follow in `delivery.json`.

## Limits and next work

This bounds admission, not the cost of one admitted callback, sorting, persistence or an entire server tick. Headwater-first ordering is retained; starvation/fairness is not proven. The native fixtures produce startup load warnings and blocked offline Mojang public-key requests; no latency result is inferred from them. No real OpenGL/Vulkan visual, Windows/AMD8060S, multiplayer soak or named-hardware frame/tick acceptance was performed here.

Consumed audit: `docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md`, pinned `2067e273e85fb4f80530c86a7c238106cbbb4cdf`. Its carried average and the last complete baseline remain **5.0/10**. This builder awards no grade. Next: reproduce/resume remaining hydrology scheduling work under tight budgets, then continue loaded/unloaded ownership and V8 saved-profile/topology gates. All broader engine/water acceptance gates remain open.

GitHub prerelease publication is unavailable through the exposed capabilities (no release-create/upload action); no release URL is claimed. Dev.30 remains a development test build, not stable promotion.

A newer static audit completed during qualification: `docs/engine-hourly-audits/2026-10-26T1433-9b26b86/REVIEW.md` at pin `9b26b8619b3a44c1542cc10f40a11658c4cd3cf0`. Read and consumed before promotion. It deferred its own heavy checks, carries 5.0/10 and repeats this scheduler qualification as first priority; its documentation commit `dc0f67dbdb01b9af224bbf157c4470ff4272ccd6` is preserved.
