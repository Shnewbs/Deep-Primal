# Dev67 — V8 saved routed-observer identity, not native hydrology

## Scope and parent

From current main at the bounded engine-builder writer lease (2026-10-29T01:00Z).
The last completed independent audit was `docs/engine-hourly-audits/2026-10-28T2331-d99837b/REVIEW.md`, pinned to `d99837bb76ddfa6fd56cb5f133eb5021dde8c131`, with a carried **50/100 (5.0/10)**; it specifically called out absent authoritative V8 routing and native saved profiles. The source version is `0.0.1-alpha.29-dev.67-hydrology-binding.1`.

This change is deliberately one small gate *before* authoritative routing: V8 saved profile revision **5** requires a strict versioned, nonzero lowercase SHA-256 fingerprint of a separately supplied routing-mask producer. `V8HydrologyBinding` provides a canonical `1:1:<64 lowercase hex>` key. `V8GeneratorSnapshot` writes/reads a ten-field revision-5 envelope containing `hydrology_binding`; previous r1–r4 envelope sizes and selected graph nodes remain as before. Existing r4 profiles must explicitly call `withHydrologyBinding` to produce a new profile. V8 snapshots may not be silently downgraded.

`V8WaterDomainAdmission` requires the same digest and interface revision from the caller-declared observer before any V8 prewrite surface planning, plus the existing exact saved seed/graph/climate checks. Old r4 admission remains supported without persisted identity, but may not claim to have a saved r5 digest. V8 climate source, ecotone and materializer explicit-call ranges now accept r5. Neither callback honesty nor the actual routing algorithm is independently authenticated by this field. A digest is source identity *only*, not authoritative water data. This does not generate any fluid, move physical water stages, alter chemistry, register native V8, or retrofit player saves.

## Source-only checks run

OpenJDK **21.0.12.1** `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. No Java25 toolchain present in this execution environment. Compiled 42 Java source files, including exact current-production V8 code and isolated Minecraft/Mojang API doubles, and all 17 focused checks passed: **21,007,690 fixture assertions** in that multi-suite run. New `V8HydrologyBindingChecks`: **45,895 assertions**, **800 modeled r5 envelope roundtrips**, **256 observer admissions**, **36,864 modeled water-mask comparisons**, **4,227 expected rejection cases**. Older r3/r4 exact field-set and decode checks pass.

New `tools/test_v8_hydrology_binding.py` exact source-only test: two identical Java21 positive processes, **8,207 assertions** per process, two independently compiled deliberately broken controls rejected. Normal Python and `python -OO` run passed. The initial negative-control attempt escaped because one malformed direct-constructor input was missing; that attempt is **rejected evidence**, not counted as a passing mutation result; the enhanced final test caught both controls.

Updated `tools/test_v8_water_domain_admission.py`: two identical source-only positive processes, **184,332 assertions**, **62,130 protected observations**, **30,030 dry observations**, three independently compiled broken controls rejected. Normal Python and `python -OO` passed, matched deterministic checksum `379ecf5601fc5d2c`.

`tools/test_ecotone_blend.py` is not automatically in CoreChecks and was not replayed here. Previous V8 ecotone fixtures in the 17-suites collection passed, but this is not complete legacy replay.

## Unrun and open gates

No complete Gradle9.2.1/Java25/NeoForge build; no native Minecraft or actual Mojang DFU/JSON/NBT codec, world creation, save/reopen, mixed-generation old/new borders, native chunk/height parity, Vulkan/OpenGL, GPU or performance acceptance. No new JAR or artifact release; owner retains builds. The full root `tools/validate_engine_work_state.py` could not run on a complete pinned checkout, and root `ENGINE-WORK-STATE.json` remains the old Dev53 checkpoint; only scoped work-state is updated.

The V8 routed-mask algorithm itself still must be implemented and connected to *V8 relief*, with basin/ocean connectivity and river/lake outlets, not claimed by this admission. Additional open gates: signed-coordinate native seam evidence; saved-policy edits and old/new chunk joins; finite-water quantity/constituent ledger across unloading/restart, source delivery and lake/ocean exchange, multiplayer soak, and named hardware tail latency; seasonal climate/snow and GPU-connected waterfalls. Renderer-owned files and foreground water production files were untouched. V1–V7 delivered geography and existing V8 profiles remain frozen.

Next: authoritative versioned V8 watershed producer and actual native codec integration, then native water-domain and compatibility acceptance.
