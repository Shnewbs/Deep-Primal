# Dev134 — overflow-safe vertical render section windows

## Scope
Source-only correction in the engine-core vertical section-window helper, used by the client ViewArea ring. Existing 0..96 user settings, normal saved worlds, player edits, water, V8 geography and Vulkan/LOD worker sources are unchanged. Reviewed the independent Dev133 audit (commit cacd165) and the engine hourly roadmap. No active claim for these three source paths was found in repository search.

## Reproduction and correction
The original `count(128, Integer.MAX_VALUE)` returned a negative count from `radius * 2 + 1`. `low(Integer.MIN_VALUE,-64,128,96)` selected the wrong end of the world when `cameraY - radius` wrapped. `coordinate(Integer.MIN_VALUE,63,Integer.MAX_VALUE)` used overflowing `slot - low` and mapped to the wrong section. All three affect section-ring indexing and can corrupt culling/repositioning for extreme camera/config values. Long intermediate arithmetic, representable-world validation and explicit invalid-window rejection now prevent these failures.

## Local validation
- OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror` then `java -ea -Xverify:all`.
- `VERTICAL_RANGE_OVERFLOW_PASS checks=1431760`: signed boundary cases, 200,000 seeded randomized inputs, independent long-arithmetic oracle, ring slot identity, world containment and invalid input rejection.
- Compiled mutation controls rejected: reverting camera subtraction fails at check 2356; reverting slot subtraction fails at check 1; reverting radius multiplication fails at check 2.
- Source SHA-256 `86ebd996a67ff17efede99957237f69bcde503786d04a1de270b29d4ac7cfb53`; test SHA-256 `95e61748e6e96344984a27338dd730cca8a99b05b480d27b4d8303687c53b8cb`. Validate committed blobs against these before treating the evidence as applicable.

## Boundaries and follow-up
No complete Java25/Gradle/NeoForge checkout or dependency cache; Git CLI cannot resolve github.com. No full game compilation, client native vertical-window scene, Vulkan/OpenGL or GPU performance acceptance; no verified replacement JAR and no JAR SHA-256. Do not delete an earlier verified build. GitHub Actions were not used. Independent whole-engine grade remains 5.0/10, not freshly regraded. Next priority: recover the full pinned checkout and JDK25 for a real JAR; V8 durable chunk publication and finite-water save/reopen remain major acceptance gaps.
