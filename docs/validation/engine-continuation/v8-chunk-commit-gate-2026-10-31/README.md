# Dev118 — V8 chunk stamp commit-time epoch gate (source-only)

Base: `7eca33c263b181215fef3c9127a1ef5c2904a0bd` (dev117). This cycle used a bounded new-file scope, preserved concurrent renderer and foreground-water files, and did not update the stale root work-state. Last available completed independent audit: `docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md`, pinned `c57d2924562a7dd35eed007a164205953bb13061`. Its 50/100 grade is carried from October 24, **not a fresh regrade**.

## Source change

New `V8ChunkGenerationCommitGate` adds a distinct commit-time protocol after dev117's admission-only `V8ChunkGenerationStamp`. The native durable owner must capture a consistent target-plus-four-neighbor snapshot with durable monotonic epochs, validate it before materialization, then compare **all five epochs and persisted neighbor states/stamps under the same atomic publication transaction** that stores completed chunk bytes and the canonical stamp. This prevents a neighbor generated, edited, or ABA-restored during materialization from silently satisfying an earlier stale border witness. Neighbor list ordering does not change identity; duplicates, unknown, legacy, negative epochs, missing directions, and mismatched profiles fail closed. `requirePublished` rejects a false compare-and-publish result rather than treating uncommitted writes as successful.

**Not yet wired to a native NBT/chunk persistence owner.** The `AtomicPublisher` interface states a required atomic contract; it does not implement or prove disk transactions, crash recovery, owner locks, actual chunk writes, or live neighbor snapshots. The caller must discard/recreate an uncommitted native candidate after rejection. No saved V8 graph/profile format, V1–V7 geography, player edits, finite water, renderer or active native materializer was changed.

## Reproducible checks

- Production helper Git blob `08a3b667dc9b06a8f76299b5c01dc2d0a744cf4d`, SHA256 `00a4ac4168675be3df75e5a2349381ffbb34bff359701876fb2587a3841c6256`.
- Java fixture blob `97fc1a609f6f1967d7867637d79a0f248ecf74e8`; Python runner blob `08f16dec6f19041f65c0490b7d6d492a5cf8313a`.
- Linux OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. Local **isolated API-double** tests: Python normal and `-OO` each ran two positive processes, each `V8_COMMIT_GATE_PASS assertions=60009`, for 240,036 positive assertions total; both compiled epoch-check mutants rejected. Deterministic 5,000-case signed-coordinate/revision corpus includes target/each-neighbor epoch changes, order independence, publisher rejection, and malformed admissions. Production helper bytes and test fixture match the committed Git blobs. The local harness used simplified API doubles for the existing stamp/border classes; the committed runner compiles all three **real production classes** in a complete checkout, but that full-checkout mode was not executed here.
- Re-run from a complete checkout: `python3 tools/test_v8_chunk_commit_gate.py`, then `python3 -OO tools/test_v8_chunk_commit_gate.py`.

## Open gates

Full pinned root `tools/validate_engine_work_state.py` execution, Java25/Gradle 9.2.1, complete DensityGraph CoreChecks, native NBT persistence and atomic publication, crash/reopen/edits, actual old/new-generation seams, Vulkan/OpenGL GPU and performance, full finite-water ledgers and long soak, and qualified JAR were **not run**. Container Git CLI cannot resolve github.com; GitHub connector supplied the current main and direct source commits. Owner's October 27 full-build/JAR-publication override remains in effect; dev53 remains last owner-confirmed playable artifact.

Next: implement native authoritative chunk-stamp storage, exclusive durable epoch ownership and same-transaction stamp publication; feed actual four-side persisted witnesses into the existing optional materializer, then run owner-controlled Java25/native signed-coordinate and old/new border acceptance. Continue finite-water ledger integration independently of renderer work.
