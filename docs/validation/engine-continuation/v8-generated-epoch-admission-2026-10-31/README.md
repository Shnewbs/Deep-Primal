# Dev124 — reject impossible zero-epoch GENERATED core witnesses

Base main: `55118b45a50ca2ea24116b0db8835e8f8ff67bb2`; scoped lease: `dd351e80f9d53ddd5bd19e76b688c66306f14866`.
Consumed latest completed independent audit: `docs/engine-hourly-audits/2026-10-31T0733-f3afacbe/REVIEW.md`, pinned `f3afacbe718a0dce3d7f14ed341169d4eeba6432`, corrective commit `8bc672f4c0c5dbce81255aae7e9c8d7586a2c906`. The auditor fixed zero-epoch GENERATED identities in the native NBT adapter; that fix is preserved.

## Production change

The dependency-free `V8ChunkGenerationCommitGate` still admitted a directly supplied `VersionedNeighbor(GENERATED, ..., epoch=0)` or `Snapshot(GENERATED, ..., targetEpoch=0)`. Neither state can arise from a successful new-V8 native commit, which increments a nonnegative target epoch before atomic publication. This bypassed the NBT adapter's new positive-epoch check when callers supplied core witnesses directly.

Both core record constructors now reject GENERATED at zero epoch, before admission/materialization. UNGENERATED zero epochs and positive GENERATED epochs remain valid; legacy V1-V7 and saved V8 graph/profile wire bytes are unchanged. No native serializer registration, geography, liquid amounts, renderer, LOD or art files were modified.

Implementation commit: `d92e10c7a85db9b17f0da1d418bb022a517f1dba`; focused runner filename correction: `652799e32a74c1bfa6edbfcfa8bf86b2679c2e9e`. Source version: `0.0.1-alpha.29-dev.124-generated-epoch-admission.1`.

## Exact-source checks executed

OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. Reconstructed local production and test source Git blob identities were verified against main:

- Commit gate: `08650c6c289b6595be7ec3c38ad4a297b31b5180`; SHA-256 `2b3e8b8896a97be3f983bc4df4df956dd614bd29accb294dc427460d865c8d7d`.
- Unchanged real core dependencies: Stamp `0919a453d67956e3fd81185535df1fe2f2b613e2`; BorderAdmission `1b5ae577fc7f57fcff85c21a0ac67735fcafd349`.
- New exact focused Java test `abac555b72b9796a0fca08ce4ff09411df5a5dbd`; existing commit-gate test `97fc1a609f6f1967d7867637d79a0f248ecf74e8`.
- Exact Python focused runner `7241095dadc532ada012bb785a922c68f4387639`.

The new runner passed under normal Python and `python -OO`: **2 x 120,000 assertions per mode** (480,000 total). Each mode compiled and rejected both deliberately defective guard-removal controls (four rejections total). The existing unchanged core-gate suite also passed **2 x 60,009 assertions**. These compile the real dependency-free engine-core classes, not Minecraft API doubles; they are still *not* full Minecraft/NeoForge, save/reopen or native durable-transaction evidence.

An initial local runner attempt failed at Java compilation because the new public class's filename differed; the test was renamed to `GeneratedEpochChecks.java`, runner path corrected, and both normal and optimized reruns passed. Failed attempt is excluded from positive totals.

## Coordination and outstanding acceptance

Scoped terrain lease released at the handoff. Git CLI cannot resolve github.com, so GitHub connector guarded expected-parent direct-main commits were used; no Actions. The complete pinned checkout/root `tools/validate_engine_work_state.py` was not executed and no root state was modified. Local JDK25, full Gradle 9.2.1 dependencies, native Minecraft save/reopen, durable compare-and-publish, signed old/new borders, GPU visual transects, finite-water ledger, multiplayer soak and named-hardware performance are still open. No JAR was built, delivered, replaced or deleted. Owner retains full builds per October 27 policy.

Last accepted independent full grade: **50/100 (5.0/10)**, not regraded. Next: authoritative native chunk+stamp serializer and crash recovery; V8 signed-border reopen, V7 relief native parity and finite-water conservation; then owner Java25/Windows Vulkan/OpenGL qualification.
