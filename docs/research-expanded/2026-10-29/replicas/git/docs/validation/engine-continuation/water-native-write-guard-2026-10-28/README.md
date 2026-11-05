# Expanded replica — docs/validation/engine-continuation/water-native-write-guard-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-native-write-guard-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-native-write-guard-2026-10-28/README.md |
| Preserved source SHA-256 | `bf789d545efed641520ddfde080e4966b26d78cf23d9cb47772bbe8e1d0f5ea2` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev41 post-native admission for seven single-cell water entry points before reservoir, inventory or chemistry accounting, including one guarded compensation attempt. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that refusing chemistry success or throwing on failed compensation is not proof of durable conservation across arbitrary foreign callbacks and restart. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an accounted-write state machine for original, requested and foreign final states, with explicit recovery ownership when native behavior contradicts its return flag. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject false-but-written, true-but-overwritten, unloaded and failed-compensation cases for place, pickup, emit, infiltrate, ocean restore/sink and ice melt, then verify native persisted ledgers. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- dev41 — post-native admission for single-cell water accounting

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# dev41 — post-native admission for single-cell water accounting

**Version:** `0.0.1-alpha.29-dev.41-waterwriteverify.1` (source checkpoint; no JAR).
**Consumed independent audit:** `docs/engine-hourly-audits/2026-10-28T0029-bcce835/REVIEW.md`, pinned to `bcce8355c7f8288e2749253578398e68c107bcda`. Source base including that audit: `5c146b95f4e90a10fb3985fdb6fc0f6fc143930d`. Scoped foreground claim: `defa915cfc2bb2b2342b8ef6897b9c20d955a8f4`.

## Implemented production change

`HydrologyRuntime.writeAccounted` now guards the seven audited single-cell account/inventory entry points: `place`, `pickup`, `emit`, `infiltrate`, `restoreOcean`, `oceanSink`, and `meltIce`. A successful native return is no longer sufficient to reach their subsequent reservoir debit/credit, returned pickup parcel, or chemistry store.

The helper freezes the native cell coordinate, refuses an initially missing/out-of-bounds cell before reading it, and checks loaded status and exact requested block state after synchronous native callbacks. A moved caller-owned mutable position is rejected before its new coordinate can receive the old cell's chemistry. Different replacement values and unloads throw before the caller's account update; they are not silently called successful placements or pickups.

An ordinary false return with the original state intact remains a normal refusal. A false return that nevertheless left the requested state in place gets **one** compensating raw write to the original state, followed by loaded/final-state proof. A failed compensation throws. The compensation never deliberately replaces a different observed foreign value and does not recurse into the accounted helper. Its native return flag alone is not proof; a false flag with the original value actually restored still yields an ordinary refusal, never success.

The existing public raw `write` method is byte-for-byte unchanged. The two-cell `WaterRuntime.transfer` implementation still owns its existing source/receiver rollback ordering; this checkpoint does not alter it. The dev40 `WaterWriteScope` source is unchanged, and each forward/compensating native write gets its own exact single-event scope. All other `HydrologyRuntime` code is unchanged after reversing only the seven call-site substitutions and removing the helper.

## Fresh executed evidence

The complete pinned original runtime and scope were reconstructed through authenticated GitHub reads and checked against Git blob identities before any modification. Original runtime blob: `898649a479972c77c3c46470447197261107235a` (30,721 bytes). Unchanged scope blob: `79c537fbb6fdd5ae169448c324b09b7137ee6bca`.

The runner extracts the exact production `write` and `writeAccounted` declarations, without rewriting their bodies, then compiles them alongside the exact production `WaterWriteScope` and explicitly named world/block/position **boundary doubles**. This is neither full `HydrologyRuntime` compilation nor a Minecraft execution.

- OpenJDK / javac **21.0.12.1**; `javac --release 21 -Xlint:all -Werror`, then `java -Xverify:all`.
- Candidate: **2,761 scenarios / 45,986 assertions passed**, no compiler warnings. Seven representative operation input profiles, 18 callback/return/compensation modes, signed/extreme identifiers, 2,000 deterministic randomized cases, initial unavailable-cell refusal, value-equal no-op and nested other-cell/other-level writes are covered. The operation profiles exercise the shared helper, not seven native Minecraft methods.
- Original dev40 raw write compiles but fails the unchanged acceptance fixture: it accepts a reported successful placement whose requested state is absent. This is an exact-source/doubles reproduction, not proof of native callback reachability.
- **Six deliberately broken implementations compile and fail**: accepting replacement after success; removing loaded checks; skipping compensation; overwriting a different foreign value; aliasing a mutable position; swallowing a callback exception.
- **21 structural assertions** verify the seven actual caller bodies each use accounted admission, do not bypass it, and perform their chemistry store afterwards. A caller-bypass mutation is rejected separately.
- Callback exceptions retain their original exception identity, close native write scopes and do not turn into successful account admission. The fixture does **not** claim that physical changes made before an exception have been durably recovered.

Run from the repository root:

```sh
python3 tools/water/test_native_write_guard.py --root . --result build/validation/water-native-write-guard.json
```

To rerun the before-failure, also pass `--baseline` pointing to the original pinned `HydrologyRuntime.java`. The test runner works with Java 21+; use `--java` and `--javac` to select a toolchain explicitly. The normal game's Java25 toolchain configuration is unchanged. Source identities, command/runtime records and each rejected mutation's output are retained in the evidence.

## Read-only self-review and limits

This closes the inspected **final-state admission omission** for the seven callers at source level. It is not a complete finite-water transaction implementation. In particular, exceptions after a native mutation, same-value ABA replacement, unload/reload identity, callback-induced reservoir changes, failed compensation, durable journaling, and recovery across save/restart remain essential open gates. Throwing an ownership error is not proof of water/salt/sediment conservation. The final state checks are value-based and cannot identify every foreign same-state write. No new replay format or saved geography is introduced.

The normal successful path adds state/loaded checks; no latency improvement or hard time bound was measured. One explicit compensating native call is bounded, but arbitrary third-party callback work is not. No production renderer, approved texture, terrain revision, player save, other role's checkout/output/cache, or schedule was modified. The foreground water claim is released at the final checkpoint.

Under the owner's October 27 build handoff, **not run:** full Java25/Gradle/module/core compilation, actual NeoForge event injection, native server/client, save/reopen/unload conservation, Vulkan/OpenGL pixels, Windows/AMD8060S, hardware frame/tick benchmarks or long soak. No JAR or release was produced. The independent **5.0/10** whole-engine score is carried unchanged, not regraded.

Next: native injection on these exact entry points and explicit single-owner transaction/recovery handling, including callback timing, reservoir reentrancy and persisted constituent ownership. The natural-cliff fluidity and broader engine/renderer acceptance gates remain open in their existing workstreams.


<!-- END PRESERVED SOURCE -->
