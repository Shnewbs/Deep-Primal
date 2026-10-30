# Renderer jam 44 — read-only author self-audit

**Scored exact source:** [`b26868b83cf68e2dde6f9bc298d980d64ee4efcd`](https://github.com/Shnewbs/Deep-Primal/commit/b26868b83cf68e2dde6f9bc298d980d64ee4efcd); parent claim `d58359b9c70379a62280435d843671114dd7f403`, pre-claim observed head `fb88f7ca17b8a15a8e8ac40b647ccf1768c8be93`. Git source blob `035f7f7aba3914cf404a12942bcd1a34f4e927f5`, compared with exact jam43 source blob `32ae24afbfa3c9c5d5f668ac860443f05fc336b2` (renamed to `LodPublishedBoundsJam43` for parity testing). Declared version stays `0.0.1-alpha.29-dev.88-vegetation-receipts.1`; no new JAR was qualified.

**This is a read-only renderer author self-audit completed after code publication**, not the independent engine audit. The separately carried independent whole-engine grade remains 5.0/10.

## Change and correctness review

Consumed jam43 findings: invalid-uniform and unique-page synthetic CPU overhead, inconsistent mixed/unique p99, and absent owner Vulkan/OpenGL acceptance. The current patch accelerates only four-or-more mesh parts sharing one *identical immutable AABB object*, using one validity verdict and a complete adjacent visibility mask. A short identity guard avoids scanning ordinary all-unique pages. A divergent/shared-prefix page falls back to the original scan; separately allocated value-equal bounds are unaffected.

- [Pinned source L43–73](https://github.com/Shnewbs/Deep-Primal/blob/b26868b83cf68e2dde6f9bc298d980d64ee4efcd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L43-L73): a full object-identity scan proves uniformity, `finiteOrdered` still excludes NaN/infinite/inverted/null bounds, and the mask includes every adjacent part except index 0, including partial 64-bit words. Invalid boxes never gain coarse reject or fine alias credit.
- [Pinned source L74–100](https://github.com/Shnewbs/Deep-Primal/blob/b26868b83cf68e2dde6f9bc298d980d64ee4efcd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L74-L100): heterogeneous, small and disabled-broadphase cases retain the prior path; on uniform valid pages, two-back aliases correctly remain empty because adjacent reuse takes precedence. No geometry, transparency, draw ordering, shader, upscaler or simulation work changed.
- [Pinned source-exact differential test](https://github.com/Shnewbs/Deep-Primal/blob/b26868b83cf68e2dde6f9bc298d980d64ee4efcd/docs/renderer-jams/20261029-validity-fastpath/LodUniformIdentityFastpathChecks.java): actual previous renderer implementation compared with the new source, including raw signed-zero coordinates, negative positions, mask content and array markers. Tests cover 12,000 seeded multilevel page scenes per flag combination plus explicit 2/3/4/63/64/65/128/129/257/1024/1025-part boundaries, invalid/null and late divergence.

## Executed isolated Java validation

Host Linux x86_64, OpenJDK/Javac `21.0.12.1`. Temurin JDK25 and owner archive were not present. Exact Git-pinned Java21 sources compiled using `javac --release 21 -Xlint:all -Werror -d scratch/classes $(find scratch/src -name '*.java' ! -name 'LodTwoBackABChecks.java')`, **PASS**. The excluded historical `LodTwoBackABChecks` needs a separate `LodPartFrustumRuns` class not in this standalone bundle; the new test exercises both two-back states.

`java -ea -Xverify:all -cp scratch/classes -Ddeepprimal.lod.pageFrustumBroadphase={true,false} -Ddeepprimal.lod.twoBackFrustumReuse={true,false} com.shnewbs.deepprimal.lod.client.LodUniformIdentityFastpathChecks` across all four option combinations, **twice**, passed **8 processes / 938,888 assertions**: 4×198,648 with broadphase enabled and 4×36,074 with it disabled. Deliberately mutating the shared-invalid gate to accept all boxes caused the expected assertion **#95** failure; this control proves the invalid-page oracle detects that fault.

Two repeated paired synthetic Java CPU benchmarks (4 levels ×64 parts, 251 alternating-order samples per shape, 64 publications/sample) compared jam43 versus jam44 p50 nanoseconds per 64 publications:

| Shape | Run 1 old → new | Run 2 old → new |
|---|---:|---:|
| Uniform, shared AABB object | 36,515 → 9,194 | 36,184 → 8,893 |
| Invalid uniform shared | 11,558 → 6,269 | 11,567 → 6,330 |
| Uniform, separate equal-valued | 97,847 → 97,737 | 99,419 → 99,750 |
| All unique | 97,367 → 95,183 | 97,446 → 97,457 |
| Mixed-late, separate equal-valued | 183,536 → 184,768 | 182,434 → 183,466 |
| Alternating A/B | 99,720 → 100,231 | 99,700 → 100,271 |
| Alternating valid/invalid | 45,568 → 47,141 | 46,309 → 46,209 |

Shared-uniform **~75%** and shared-invalid **~45%** p50 reductions are *isolated component results only*. Mixed/alternating/invalid p95/p99 vary, and a page sharing a long prefix before diverging may pay an extra identity scan. No measured Minecraft FPS, GPU gain or native runtime prevalence is claimed. The archive's raw benchmark log records all p95/p99 values, not only p50.

## Equal-weight renderer grades

| Category | /10 | Pinned evidence and missing acceptance |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.80 carried** | source [L43–100](https://github.com/Shnewbs/Deep-Primal/blob/b26868b83cf68e2dde6f9bc298d980d64ee4efcd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L43-L100), exact parity; no new proven useful distance |
| Mesh generation/reuse/scheduling | **6.95 carried** | changes are only publication/culling metadata, no worker or mesh generation improvement |
| GPU culling/submission/memory | **4.55 carried** | no GPU timing, indirect draw, descriptor or VRAM work |
| Actual FSR/DLSS | **5.00 carried** | existing FSR1 background only; no new temporal FSR or active RTX NGX |
| Visual/backend/settings compatibility | **5.30 carried** | original fallback unchanged; no native Vulkan/OpenGL water pixel/reload gate |
| Validation and measured performance | **6.65 (+0.05)** | pinned Java21 exact parity + effective negative mutant + paired synthetic p50; unresolved tails and native gates |
| **Overall** | **5.88 (+0.01)** | 35.25/6 = 5.875; jam43 displayed 5.87; independent whole-engine grade unchanged |

**Not run:** Java25 full Gradle 9.2.1/NeoForge compile and JAR/package/native/outer+nested CRC, actual Vulkan/OpenGL/transparent-water GPU pixels, FSR1 rendered scene, CPU/GPU frame p50/p95/p99, draw/descriptor/RAM/VRAM, Minecraft startup/travel/edit/save/reopen/resize/resource reload/world switch/shutdown, temporal FSR and RTX DLSS. Owner handles full builds under October 27 policy. No new development JAR, prerelease or CurseForge upload.

**Next priority:** measure real resident AABB identity frequencies and matched owner GPU/Vulkan/OpenGL frame and water results before deciding whether to retain this path or conditionally disable it. Benchmarks, mutation source, exact-source fixtures, terminal logs and SHA256/CRC archive receipts are in the verified [jam44 source-evidence ZIP](https://github.com/Shnewbs/Deep-Primal/blob/b26868b83cf68e2dde6f9bc298d980d64ee4efcd/docs/renderer-jams/20261029-validity-fastpath/README.md) (Library path and SHA256 in LEDGER).
