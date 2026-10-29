# Dev67 independent Java25 audit — 2026-10-28T18:31 PDT

Pinned main source: `a8e26ba492245d532eb4a116b13d9f842d7b013b`; source version: `0.0.1-alpha.29-dev.67-hydrology-binding.1`. This is a separate read-only audit of committed source, not a builder or renderer production change.

## Direct checks

Restored owner Temurin 25.0.5+8 in an isolated Linux directory; `java -version` and `javac -version` passed; JDK archive SHA256 `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`. Strict `javac --release 25 -Xlint:all -Werror` passed on four exact pinned Git-blob sources, local independent Mojang API shims and an independent binding test. `java -ea -Xverify:all` V8-hydrology fingerprint/codec test: **51,017 assertions**, PASS. Exact committed LOD submission regression: two runs of **29,276** assertions each, PASS. **109,569 passing source-level assertions total**. Separately compiled negative mutants rejected: failure-accounting omission at LOD assertion 33, all-zero digest acceptance at binding assertion 50,005. These are isolated Java component tests, not actual Mojang codec/native GPU acceptance.

Pinned source blobs: [V8HydrologyBinding.java](https://github.com/Shnewbs/Deep-Primal/blob/a8e26ba492245d532eb4a116b13d9f842d7b013b/src/main/java/com/shnewbs/deepprimal/engine/V8HydrologyBinding.java) `29502739de303a2b291991c439024b25cce8167c`; [LodSubmissionProbe.java](https://github.com/Shnewbs/Deep-Primal/blob/a8e26ba492245d532eb4a116b13d9f842d7b013b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodSubmissionProbe.java) `ce6edd9927d1beed278c197f9e27621d7ebb412f`; RollingTiming `0be7512957537596295ba204f8f1dfc176aa686f`; committed LOD regression `241f91c0207b278e59dcc8c074071d08078d365d`.

## Findings

Dev67 [V8-r5 hydrology binding](https://github.com/Shnewbs/Deep-Primal/blob/a8e26ba492245d532eb4a116b13d9f842d7b013b/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L149-L165) now persists a source fingerprint for a caller-supplied routed water observer. [Water admission](https://github.com/Shnewbs/Deep-Primal/blob/a8e26ba492245d532eb4a116b13d9f842d7b013b/src/main/java/com/shnewbs/deepprimal/engine/V8WaterDomainAdmission.java#L91-L120) checks the saved digest. **This does not implement or authenticate V8 ocean/river/lake routing** and earns no gameplay feature credit. LOD CPU timing measures API submission after transform writes only; not GPU completion, VRAM or whole-frame latency. Root `docs/ENGINE-WORK-STATE.json` remains Dev53 while scoped Dev67 evidence is current; reconcile without overwriting builder state.

Next: authoritative V8 routed watershed and saved ocean/river/lake masks; native signed chunk seams, r1-r5 save/reopen, finite water/constituent ledger over unloading; Windows AMD8060S Vulkan/OpenGL water/LOD pixels and real GPU/frame p50/p95/p99.

## Ten equally weighted categories — historical full grade **carried**, not freshly regraded

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/docs | 7 |
| Polish/tooling | 4 |
| **Overall** | **50/100 = 5.0/10; Tier 2** |

**No qualified JAR**: October 27 owner override assigns full Gradle/Minecraft/GPU builds to the owner. Complete Git clone failed in this isolated Linux container (github.com DNS unavailable); no Gradle9.2.1, NeoForge, native save/reopen, full source/package identity/CRC, Windows Vulkan/GL, water conservation or hardware FPS gate run. No source edits or renderer/builder lease interference. No releases, no JAR retention changes.

Evidence archive, CRC verified and Library saved: `/Deep Primal/Independent Audits/DeepPrimal_Dev67_IndependentAudit_20261028T1831_a8e26ba_Java25Evidence.zip`; 15,217 bytes, SHA256 `939e732e55705bed92595edb05dffe0e1be2f924a2d70b8064ea1906410bedb1`. Stop condition requires **fresh complete average >8.5**, not met.
