# Independent Deep Primal Dev60 source audit — 2026-10-28 18:30 UTC

**Pinned main:** `7b854edd9c32c8d20d621495d4a935a5e6418946`; source version `0.0.1-alpha.29-dev.60-relief-v2.1`. Read-only auditor, isolated Linux Java21 test environment. No Builder, renderer or root work-state mutation.

## New finding — coastline consistency risk

The new opt-in V8 relief-2 graph passed **92,811 independent Java21 assertions**, including exact historical V8 graph equality, finite/bounded height and density, unchanged canyon and erosion inputs, signed coordinates and continuity. But of **14,045 sampled nearshore columns** (four deterministic seeds, |continentalness| < 0.08), **581 crossed from old height > 96 to revised height <= 96**. Example seed 0, x=4279, z=579: continentalness 0.008058, old 99.006, revised 95.755. Max measured relief change 145.683 blocks; maximum tested adjacent-column height difference 2.689 blocks. This contradicts an *unqualified* assertion that the revision cannot affect shore height. **This is NOT a native Minecraft flooding reproduction**; actual ocean masks, routing and water materials were not exercised, and V8 relief is opt-in/unregistered. Before native V8 activation, prove consistency between derived height, sea mask, routed hydrology and saved profiles.

Source: [TerrainShapeV8.java L55-L90](https://github.com/Shnewbs/Deep-Primal/blob/7b854edd9c32c8d20d621495d4a935a5e6418946/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/TerrainShapeV8.java#L55-L90), [V8GeneratorSnapshot.java L71-L85](https://github.com/Shnewbs/Deep-Primal/blob/7b854edd9c32c8d20d621495d4a935a5e6418946/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L71-L85).

## GPU reset cleanup

Pinned production `LodGpuResetCleanup.java` passed **29,041 independent assertions**: 83,817 close callbacks with 21,039 injected faults, every callback attempted, original first fault retained and subsequent failures suppressed in stable order. A deliberate suppression-removal mutant failed at assertion 3; a relief plateau-scale mutant failed the relief test at assertion 42. The renderer's actual Vulkan/OpenGL cleanup and earlier `diskIndex.reset()` or `worker.shutdownNow()` exceptional exits remain untested. Source: [cleanup helper](https://github.com/Shnewbs/Deep-Primal/blob/7b854edd9c32c8d20d621495d4a935a5e6418946/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java#L11-L27), [integration](https://github.com/Shnewbs/Deep-Primal/blob/7b854edd9c32c8d20d621495d4a935a5e6418946/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L832-L875).

**Fresh independent checks:** 121,852 assertions passed (92,811 relief + 29,041 GPU cleanup). All six reconstructed production Java sources matched pinned GitHub blob identities, strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed. The source/evidence ZIP is saved under `/Deep Primal/Independent Audits/DeepPrimal_Dev60_IndependentAudit_20261028T1830_7b854ed_SourceEvidence.zip`, SHA256 `9a2ee6cb402a39d9809e06814a3dc79c3359a30be15ec62213a31c07dd0234d3`, 22,155 bytes, 19 CRC-verified entries.

## Equal-weight engine grade (prior complete baseline carried, NOT fresh regrade)

| Category | Score /10 |
| --- | ---: |
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology and caves | 6 |
| Features and structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Total** | **50/100, average 5.0, Tier 2** |

The owner target is **strictly greater than 8.5/10**; new opt-in features and a passing component test do not warrant credit for unverified gameplay. No source files were changed during this audit. The source changed since Dev59 (opt-in V8 relief and LOD GPU-reset cleanup), but the auditor only observed it.

**Not run:** Java25 (archive absent in this isolated workspace/Library lookup), full private repo checkout (container GitHub DNS unavailable), Gradle/NeoForge build, package CRC/class identity, native Minecraft saved-world/reopen/shutdown, finite-water long-soak/ledger, full Windows AMD8060S Vulkan/GL hardware benchmarking and true GPU fault recovery. The Oct 27 owner override delegates full builds, GPU acceptance and JAR delivery to the owner. **No JAR was built or qualified**; no GitHub release, CurseForge upload or old-JAR retention action. Linux source testing is not Windows/native acceptance.

Next: resolve V8 sea-plane/coastline and hydrology interface, activate saved-authoritative V8 climate with native lifecycle tests, verify finite water conservation through unload/reopen, then run matched GPU/frame-time/VRAM tests. The Builder and renderer must proceed independently. `ENGINE-WORK-STATE.json` still records dev53 while scoped terrain state records dev60; root state was left untouched.
