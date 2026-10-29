# Expanded replica — docs/renderer-jams/20261028-batch-hard-limit/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-batch-hard-limit/README.md) |
| Source identity | git: docs/renderer-jams/20261028-batch-hard-limit/README.md |
| Preserved source SHA-256 | `0eadd15025aac5eaaca4ada298a1d584eb94c51059019f7d530e27f122893fa3` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Enforces the declared expected count and 4,096-record maximum through mutable indexed-batch list operations, including bulk, iterator, sublist, and capacity paths. Closed batches reject additions; current callers were already compliant, so this is a misuse guard rather than a reproduced game overflow.

## Research expansion — gaps and details to deepen

- The remaining boundary is the synchronous borrowed-list lifetime. Retaining a returned list across another begin can alias a later batch, and hard Java record bounds do not establish GPU indirect submission or lower driver overhead.

## Research expansion — proposed contracts

- Specify exclusive batch ownership from begin through end, prohibit retained aliases, and make reentrancy/closed-generation misuse observable. Preserve stable draw order and mandatory finally cleanup while measuring guard overhead separately from GPU work.

## Research expansion — acceptance checks

- Retain the 8,068-scenario abuse and order checks, then exercise actual native API failure, reset, and more-than-4,096 visible parts. Compare batched/direct CPU and GPU tails plus water pixels; accept the guard for correctness without inventing an FPS gain.

## Research expansion — source anchors

- Renderer jam 19 — enforce indexed API draw batch limits
- Verified isolated Java 21 test

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 19 — enforce indexed API draw batch limits

**Scope:** `BoundedIndexedDrawBatches` only. This fixes independent Dev64 audit finding #3: `begin(expectedCount)` used to return a mutable `ArrayList` without enforcing the declared count after creation. The current renderer caller was already compliant; this is a misuse safety guard, not a reproduced in-game overflow.

The returned compatible `ArrayList` is now backed by a checked list. Appends, indexed insertion, `addAll`, iterator insertion and sublist additions cannot increase draw references above either `expectedCount` or the 4096-per-API-call maximum. External `ensureCapacity` cannot enlarge the scratch allocation beyond the maximum. `end()` still clears references on submission/packing failure, and returned lists reject new records once the batch is closed. The synchronous rendering call site, draw order, vertex/lighting/water data, and OpenGL/Vulkan backends are unchanged. These are not hardware multi-draw-indirect commands.

## Verified isolated Java 21 test

```sh
find source tests baseline -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BoundedHardLimitChecks
```

**PASS:** 8,068 scenarios, 3,302,181 assertions, including randomized add/remove/insert/bulk-order parity, 131072 records, exceptional finalization, zero retained handles, listIterator/subList abuse, and clean reset. The historical unguarded helper compiles but fails the new regression at assertion 5. The tested source and test have exact Git blob identities `0ba6b843b105bde21d9d6df8efe069450b42f8a8` / `357067d98660a1b1941a69bb1a78b279f7dd2194`.

Two Java21 301-pair, 1500-warmup synthetic draw-add microbench passes show slight per-loop overhead at small/medium sizes (4096 records median 17831→19100 ns and 18549→19690 ns) and mixed 65536-record results (802569→799448 ns, 819929→795401 ns). This is **not** Minecraft FPS or GPU performance evidence. No GPU batching or distance improvement is claimed.

**Not run under the owner's build override:** Java25 (archive absent), full Gradle/NeoForge, packaged client, Vulkan/OpenGL GPU pixels, transparent-water visual parity, resize/reload/world reopen, VRAM/frame p50/p95/p99, temporal AMD FSR, NVIDIA RTX DLSS, or JAR packaging.

The general engine/water builders and independent auditor are unaffected.


<!-- END PRESERVED SOURCE -->
