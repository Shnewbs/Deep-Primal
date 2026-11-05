# Alpha.28 terrain and water audit — 2026-10-24

The owner reported exposed patches in flowing water, one-block streams and similar terrain/biomes across seeds. This release fixes reproduced bed clipping, the shared seed-origin field values, narrow channel planning and weak biome/landform distinction. It introduces saved terrain revision V7 and preserves V1–V6. Full engine/performance acceptance remains **incomplete**.

## Released package identity

[Download Windows JAR](https://chatgpt.com/library?file_id=libfile_60f52a56ae0c8191b99f14f2404675c9&account_id=personal) · [Raw validation archive](https://chatgpt.com/library?file_id=libfile_1d64e03688d0819190aee4b2c9188abb&account_id=personal).

| Package | Bytes | SHA-256 |
| --- | ---: | --- |
| Windows Vulkan owner preview | 124956629 | `b483541ce9a8e2d01f35d3ecd321bfab02806bb625327469e994a3a1407be48e` |
| Linux Vulkan native test package | 126166092 | `44080119987c5b45a6100de669f055c1964757071f61c607566779ccbde8dbc1` |
| Released alpha.27 Windows control | 124952383 | `c37d5c493ce90dfdf7e74fd049eff8b972e00ef7a2f893f845d00686d0ea7c10` |

Source starts at production `cbc20d86e626285dfa12bb2ed6ef747358fbec59`. Subsequent R57 changes are documentation/staged LOD work, preserved by the guarded update based on foreground lease `0f1adcd1a6c4aecdb96fe638bb4dc617c2973295`. No staged pressure or LOD candidate is promoted. Themes alpha.8 and independent maritime-1.21.1 remain intact.

The full final Gradle build passes all core/module checks: 32 tasks, 17 executed and 15 up-to-date, in 1m59s. Later edits affect private validation fixtures only. Windows packaging completes separately. Each package passes seven outer/nested archive CRC checks, 504 project class comparisons against compiled output, and exclusion of 158 private validation/core-check class paths. Independent JDK25 compilation of all 51 core sources produces 89 class files byte-identical to Gradle and the packaged payload. Linux and Windows share 7,137 identical entries after normalizing only the Vulkan archive filename and its metadata path; the remaining payload differences are platform shaderc/VMA natives and their git metadata. Windows contains DLLs and no Linux SO payloads. Native Linux execution is not Windows execution.

## Reproduction and fixes

V6's eight tested seed origins share height 165.42108, rainfall 0.63967 and temperature 0.80483. V7's shifted sampling lattice produces eight distinct heights and rain values. Its regression samples 2,312 columns and 127,840 reach points, observes 29 biome IDs and checks central drainage-domain ocean margins. The fixed seed report gives planned median channel widths of 5.125–5.843 blocks. These counts measure the sampled fields/plans, not aesthetic completeness or live river retention.

The baseline thin-water probe has two of four vertices below the solid bed, minimum relative height -0.4375. The candidate has none below, minimum 0.00390625. New regression coverage passes 6,144 stair cases, 24,576 supported top vertices and 512 shared-page vertices with unchanged input units. Existing vertical-layer/page seam, transport, volume, texture, animation, cache and module checks pass.

## Packaged native server checks

Every accepted native run requires explicit fixture records, selected-package loaded class hashes, normal save/stop, no crash report and successful exit. A Gradle exit alone is insufficient.

| Run | Chunks checked | Block/base-column comparisons | Result |
| --- | ---: | ---: | --- |
| Released alpha.27 V6 control | 20 | 1,419,669 | Saved and stopped normally |
| Candidate Standard initial | 44 | 3,062,331 | V6/V7, four seeds; 20 V6 chunk digests match control |
| Candidate Standard reopen | 44 | 3,062,331 | Saved profile and gold-block edit survive changed creation defaults |
| Actual alpha.27 world opened in alpha.28 | 20 | 1,419,669 | Legacy generation, profile and edit retained |
| Candidate Deep initial | 44 | 5,945,915 | Native -384..639 profile, V6/V7 |
| Candidate Deep reopen | 44 | 5,945,915 | Profile, edit and actual saved-world seed binding pass |

Standard candidate runs also inspect 704 biome palette samples, 139 log blocks and 51,181 water blocks; Deep inspects 100,596 water blocks. Repeated native generation matches each recorded digest. Separately, 5,202 core points across revisions 1–6 are bit-identical between original and candidate source. The last saved-world seed assertion was added after the earlier Standard/legacy runs; it is executed by the accepted Deep reopen and the actual UI creation check. Earlier runs still check four independently seeded native generator clones and are not relabeled as executing the late assertion.

## Native client and visual checks

The actual Create World form enters seed 20261020, joins a V7 world with that saved seed, checks the bound terrain engine and saves/disconnects. The alpha.27 control and alpha.28 candidate render matched frozen sand-stair beds at 1/16 and 12/16 water quantities. Native block-state hashes, biome and volume agree exactly. Candidate Vulkan views show thin supported water where the control clips into the bed; fuller water remains continuous. An explicit OpenGL run disables interpolation to exercise `SmoothWaterRenderer`, confirms its rendered-cell counter and captures both levels. No authority or default renderer settings are changed by these private fixtures.

A subsequent Vulkan reopen captures forest, dry terrain, mountain slope and river-mouth scenes with normal server ticking. Forest canopy and dry/badlands surface treatment are visibly different. The short render distance/fog limits the mountain view; these four views do not certify continent-wide variety or one-block headwater elimination. Thin-water flecks and other mixed/distant transparency still require further diagnosis; this release does not claim to remove all water artifacts.

Accepted Vulkan runs require actual Vulkan selection, no VUID messages, normal save/disconnect, `VULKAN_RESOURCES_RELEASED buffers=0 images=0`, and the deliberately rejected shutdown callback followed by a successful completion. OpenGL fallback is separately attributed and does not claim Vulkan resource/shutdown coverage. Both candidate Vulkan runs in a fresh `/tmp` configuration finish without a recovery marker, including ordinary saved-world reopen. All graphics are Mesa software-driver evidence, not owner GPU performance.

## Rejected attempts and audit corrections

- Offline missing runtime dependencies and incomplete asset/capture attempts are retained and excluded.
- Packaged validation originally selected development module JARs despite `MOD_CLASSES`; alpha.27 control failed module-version checks against alpha.28 development output. `build.gradle` now filters ModDev's classpath provider during task configuration and binds both `MOD_CLASSES` and `fml.modFolders` to the selected package plus private fixtures. Attempts to change JavaExec classpath too late or mutate a finalized provider are retained. Loaded class identities guard every accepted native server run.
- The first Standard result postprocessor incorrectly required an explicitly serialized default `min_y`; the codec validly omits it. Native checks and normal shutdown had passed. Reporter-only correction resolves the documented default and verifies the existing bytes; the accepted result explicitly records this distinction.
- A Deep reopen's persisted console capture was incomplete despite in-memory pass markers. That record is rejected. The helper now seals accumulated PTY bytes and their digest after exit; a separate full rerun passes.
- A client invocation used a missing old Linux JAR filename. It was stopped and excluded; selection now checks file existence.
- A workspace Vulkan recovery marker reappeared after a successful first frame. The next run fell back to OpenGL and failed the private required-Vulkan assertion; its crash/marker evidence is retained. Earlier project evidence already tracks this behavior. Fresh `/tmp` runs and reopen pass without retry flags. Root cause remains unproven; recovery guards were not bypassed and no speculative production workaround was added.
- The first platform comparator treated the expected nested archive filename/metadata difference as a runtime mismatch. The final comparator normalizes only those names and compares all common bytes; no class/asset difference is waived.

## Evidence and remaining work

Compact results, hashes, source manifest and runners are under [validation/alpha28](validation/alpha28/README.md). The delivered validation ZIP contains original logs, input records, screenshots, rejected runs and checksummed evidence. Current helper/tool pins are recorded separately; no private fixture or toolchain enters the mod.

This release does not change the finite-water solver, rainfall/source eligibility, pressure continuation, LOD or graphics backend. Broader storm/drought, live temporal water, enabled-view LOD/mixed transparency, rare terrain/geology cases, owner-GPU and performance gates remain open. V1–V7 geography is now frozen; future geography must use a new revision. Do not claim perfection, universal seed quality, independent 60Hz physics or guaranteed FPS.
