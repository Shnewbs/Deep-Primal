# Deep Primal dev133 — immutable ByteLru admission weights (2026-10-31)

## Pinned source and scope
- Base main: `9ea2591a0f23b328ff129d6ff941b2d7714f6135` (Dev132 independent audit).
- Changed production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/ByteLru.java`.
- Changed already-registered test: `src/coreChecks/java/com/shnewbs/deepprimal/core/ByteLruOverflowChecks.java`.
- This is engine cache accounting only; no water state, renderer native code, terrain profiles, saved formats or other writer-owned source changed.

## Defect and repair
Previously `ByteLru.put` reweighed the *current* payload on replacement and eviction, although callers retain mutable payload ownership. If the payload's weight changes after admission, subtraction differs from the reserved weight. This can corrupt the byte counter, admit too much data or exhaust the eviction iterator with `NoSuchElementException`. The prior code reproduced the exception when a 6-byte payload was mutated to weight 1 before adding a 7-byte payload to a 10-byte cache.

The cache now retains `Entry(value, admittedWeight)` and subtracts the original weight on eviction/replacement. Admission still weighs each new value exactly once, enforces the byte/entry limits before addition, retains LRU access ordering and existing oversized-replacement behavior. Tradeoff: one small wrapper object per admitted cache entry; no extra allocation on `get`.

## Locally executed validation
- OpenJDK `21.0.12.1`; `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- Exact new production and already-registered regression class: **3,566,640 checks per run**, three identical successful runs (**10,699,920 checks total**).
- Existing BigInteger overflow/eviction oracle retained; new mutable-payload deterministic oracle covers limits 0, 1, 10, 100, Long.MAX_VALUE and capacities 0, 1, 4, 20 across 80,000 steps, plus targeted eviction/replacement witnesses.
- Original committed production source compiled with the updated test and **failed** with `NoSuchElementException`.
- Eviction-only defective control **failed** with `NoSuchElementException`; replacement-only defective control **failed** its targeted assertion.
- New production SHA-256: `ccfba8e3bed25501b0d8306a89e75b3ea95f9ff3da3779f38ee0e9571d896400`.
- New test SHA-256: `bec93927fb37fc8c8bd17dff5cd4b9bce871687fa5b83aca9d08ef6222cb6774`.

Reproduce from the repository root:
```sh
mkdir -p /tmp/deep-primal-byte-lru-checks
javac --release 21 -Xlint:all -Werror -d /tmp/deep-primal-byte-lru-checks \
  engine-core/src/main/java/com/shnewbs/deepprimal/core/ByteLru.java \
  src/coreChecks/java/com/shnewbs/deepprimal/core/ByteLruOverflowChecks.java
java -ea -Xverify:all -cp /tmp/deep-primal-byte-lru-checks com.shnewbs.deepprimal.core.ByteLruOverflowChecks
```

## Acceptance limits
This is isolated Java 21 correctness validation, not the complete pinned Java25 Gradle/NeoForge build, native Minecraft save/reopen, GPU test or throughput benchmark. Git CLI could not resolve `github.com`; this execution environment had no complete checkout, Java25 toolchain or Gradle dependency cache. **No replacement game JAR or JAR SHA-256 exists from this cycle; do not delete the previous successful JAR.** GitHub Actions were not used. Whole-engine independent grade remains **5.0/10 carried**, not newly regraded.
