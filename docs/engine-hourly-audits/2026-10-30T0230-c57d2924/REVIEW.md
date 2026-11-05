# Dev93 independent audit — 2026-10-30T0230-c57d2924

**Pinned engine source:** [c57d2924562a7dd35eed007a164205953bb13061](https://github.com/Shnewbs/Deep-Primal/commit/c57d2924562a7dd35eed007a164205953bb13061). **Version:** `0.0.1-alpha.29-dev.93-relief-core-gate.1`. **Grade:** 50/100, Tier 2, **carried October 24 baseline, NOT freshly regraded**. Target strictly greater than 85/100 not met.

## Actual observed change since Dev92

Builder [Dev93 dd336db8](https://github.com/Shnewbs/Deep-Primal/commit/dd336db894f24c755150f162cacbfdf200abbad2) adds pre-save V5 DensityGraph compilation in [V8GeneratorSnapshot.java L163–176](https://github.com/Shnewbs/Deep-Primal/blob/c57d2924562a7dd35eed007a164205953bb13061/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L163-L176) and registers the new mandatory VM gate in [CoreChecks.java L27–30](https://github.com/Shnewbs/Deep-Primal/blob/c57d2924562a7dd35eed007a164205953bb13061/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L27-L30). The new [V8ReliefV5GraphChecks.java](https://github.com/Shnewbs/Deep-Primal/blob/c57d2924562a7dd35eed007a164205953bb13061/src/coreChecks/java/com/shnewbs/deepprimal/core/density/V8ReliefV5GraphChecks.java#L20-L109) checks actual graph compilation, signed coordinates, coast/canyon continuity and parallel/reversed chunk parity **when executed**. Real VM test **was not run**; static wiring passed 12/12 checks.

Renderer [jam47 18c8eb77](https://github.com/Shnewbs/Deep-Primal/commit/18c8eb774f58a96067eb17f04d0fb3b607ccbc39) reuses JNI argument arrays in the DLSS provider. This does **not** activate a complete temporal upscaler.

## Fresh auditor tests — isolated Linux

Temurin JDK25.0.5+8 restored from owner Library artifact; Java21 retained. Exact frozen V5 helper Git blob `b193fa30f25b6ace3dfa8be077f3137f17aa3d2e` checked; independent scalar/API-double test passed **2,419,907 assertions on Java25 and 2,419,907 on Java21**, with **6/6 compiled faulty variants rejected**. This is an unchanged Dev92 source regression, NOT the new production DensityGraph VM.

Exact committed renderer helper/test Git blobs `4cbccfbf8cb3c55f93ca549ddde379a581b2ac1a`, `d6d0d6a3d3e154d82afc170c0743b250749eb222`, `299a51362b0b0b4e002e750e3d6977fbd39c48a3`: strict Java25 passed **600,004** checks; Java21 passed **300,002**; one reallocating mutant rejected. Java25 compiled classfile version 69; Java21 target version 65. Combined **5,739,820 executed assertions** and **7/7 mutants rejected**. This does not prove GPU/NGX/whole-frame FPS.

## Open gates and grading

| Terrain | Biomes | Hydrology/caves | Features | Performance | Determinism | Architecture | Extensibility | Tests/docs | Polish |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 5 | 4 | 6 | 4 | 3 | 7 | 6 | 4 | 7 | 4 |

**No full Gradle/Minecraft/GPU/JAR build** under owner's October 27 build override. Full repo Git checkout failed CLI DNS; no Gradle 9.2.1 wrapper execution, native world save/reopen/shutdown, physical finite-water ledger authority, Windows AMD8060S Vulkan/OpenGL, complete Themes/notices or packaged CRC/class identity. No qualified JAR, no release, no retention change. Root ENGINE-WORK-STATE remains Dev53; scoped terrain state records Dev93. V1–V7, saved V8 revisions, player edits and finite quantities were not changed by the auditor.

**Next:** run mandatory real V5 VM CoreChecks with full Java25 checkout, fix any failures before V8 native activation; integrate durable ledger with finite-water blocks and certified global ocean witnesses; verify native world save/reopen and GPU performance. No builder pause/lease takeover. Fresh complete independent score strictly greater than 8.5 still required.

**Full local evidence:** `DeepPrimal_Dev93_IndependentAudit_20261030T0230_c57d2924_Java25Evidence.zip`, 15,708 bytes; SHA256 `6dfbf100a70509f160454a967b7b418dc199dc45d8c3a937079233ca95238610`. Saved in Library `/Deep Primal/Independent Audits/`, 24 members passed archive CRC and member-hash verification. Auditor source code changes: none.
