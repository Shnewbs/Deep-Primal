# Dev119 — strict V8 chunk-generation NBT identity adapter (source-only)

Base `c31be0546cebfff71e338b2822b9c1bdb12c702d` (dev118). Existing hourly builder, bounded scoped claim; no renderer/water files changed. Latest available completed independent audit: `docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md`, pinned source `c57d2924562a7dd35eed007a164205953bb13061`. Its 50/100 score is carried, not freshly regraded.

## Implementation

`src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNbtAdapter.java` is the first native NBT-facing bridge for the dev117 chunk stamp and dev118 commit gate. It writes a canonical, bounded, CRC-checked string envelope to an explicitly provided *candidate* `CompoundTag`; the envelope contains the exact saved V8 generation stamp and nonnegative durable epoch. Reads reject missing or wrong-type tags, malformed/future/noncanonical envelopes, CRC corruption, invalid epochs, and wrong signed chunk coordinates, saved revision or profile fingerprint. Existing markers cannot be overwritten by the new-chunk writer.

`neighbor(...)` converts a native owner's certified GENERATED/UNGENERATED observation and independently supplied durable epoch into the existing `V8ChunkGenerationCommitGate.VersionedNeighbor` contract. Generated neighbors must have a matching NBT stamp and epoch. UNGENERATED must be certified separately by the authoritative lifecycle and have no marker. UNKNOWN always rejects. `ungeneratedTarget(...)` constructs the target snapshot only after that independent certification. No implicit neighbor loads, world edits, save migration, liquid mutation or V1–V7 path change.

**This is not a native chunk-save hook, NBT persistence proof, or atomic disk transaction.** A caller still must wire the adapter into the actual chunk serializer, provide authoritative persisted snapshots/epochs, hold the native compare-and-publish lock, and atomically commit completed chunk bytes and marker. An absent marker alone is never proof of an ungenerated chunk. Old/new border geometry remains unaccepted.

## Reproduction and scope

Run `python3 tools/test_v8_chunk_nbt_adapter.py` and `python3 -OO tools/test_v8_chunk_nbt_adapter.py`. The runner compiles the exact production adapter with `javac --release 21 -Xlint:all -Werror`, executes `java -ea -Xverify:all`, and rejects a compiled epoch-bypass mutant. When a complete checkout is available it compiles the real `V8GenerationBorderAdmission`, `V8ChunkGenerationStamp` and `V8ChunkGenerationCommitGate` classes; otherwise it uses explicitly labeled core API doubles. NBT types are API doubles in both modes.

Local OpenJDK 21.0.12.1, **isolated API-double mode**: two positive processes each under normal Python and `-OO`, `V8_NBT_ADAPTER_PASS assertions=84706` per process, **338,824 total**. Both epoch mutants rejected as `accepted invalid input` in the broken control. Exact production Git blob `1c2f6b5b2fcebc337e88344298cd239e4ca33782` matched locally tested source, SHA256 `38bc6a9dcc9f4bbc66fea65997735813469e8bde233bbfb19f1b435ca1ba6e47`; Java fixture blob `70c138860d978ffd62ef9bb71a45d20c7cfc69e3`, runner blob `ce00e1ed9396661046058ad569be26c66eb744fc`. Full checkout mode was **not** run.

The complete pinned root work-state validator could not be executed on its 130 KB pinned JSON because this runtime lacks a complete checkout and Git CLI DNS access; root ownership/state was not rewritten. Java25, Gradle 9.2.1, actual NeoForge/Minecraft NBT API compilation, native chunk serializer/reopen, atomic epoch+chunk publication, crash recovery, GPU, performance, complete water ledger, and qualified JAR were not run. Owner's October 27 full-build/JAR-publication policy remains in effect; last owner-confirmed playable build is dev53. No existing JAR was deleted.

Next: connect this NBT bridge to the authoritative native chunk serializer and durable epoch/CAS owner, verify actual save/reopen/edits and signed-coordinate old/new chunk seams, then continue V7 relief and finite-water ledger gates. No independent grade change.
