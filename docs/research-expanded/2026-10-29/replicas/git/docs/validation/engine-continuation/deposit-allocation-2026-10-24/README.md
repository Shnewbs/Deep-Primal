# Expanded replica — docs/validation/engine-continuation/deposit-allocation-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/deposit-allocation-2026-10-24/README.md) |
| Source identity | git: docs/validation/engine-continuation/deposit-allocation-2026-10-24/README.md |
| Preserved source SHA-256 | `3a63db6915e1da9725086548041107d74548788d9efff0f0416f42c08a842577` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R51 lazy deposit metadata allocation that shares empty arrays for columns without a reachable body while retaining nonempty predicates, order and immutable columns. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the change targets allocation rather than ore distribution, and raw array payload is not total VM or process memory. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a deposit-column representation contract distinguishing shared immutable empty metadata, nonempty allocation and native source-identity comparisons. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat empty and hit columns, null-host rejection, retained-column isolation and integer extremes against the reference, and compare allocation only under matched independently compiled inputs. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R51: lazy deposit metadata and source-verified native coverage

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R51: lazy deposit metadata and source-verified native coverage

Parent `02afc23d5241be07704a653905503ed6b7bd8eda` (R50). This checkpoint stages
one production change since R50: `DepositField` reuses private empty arrays for
columns with no reachable body, allocating its fixed metadata arrays only when
there is a footprint hit. Nonempty metadata and the query predicate/order remain
unchanged. Null-host rejection and immutable retained columns are preserved.
Source capacity remains nine; nonempty arrays have324 raw bytes and empty arrays
zero raw payload. The two shared empty-array objects have a one-time VM overhead.
This is a memory-allocation fix, not a change to ore distribution or geography.

The final source retains R50's source-level five-hash expressions/order per cell
(45 across nine cells). An earlier candidate added early axis/hash rejection;
it is archived under `intermediate-early-rejection/` after worse pooled p95 in
all four native cases. The final version isolates lazy allocation at the source
level. Executed/JIT instruction counts were not instrumented. Neither variant is
promoted; public production paths, default/saved V1–V4 and installers are unchanged.
The eleven-file staged snapshot includes the unpromoted R50 V5 integration: five
production files and six private checks. `since-R50.patch` shows this run's delta.

## Functional and source-identity checks

- Final forced nonincremental31-task full Linux-Vulkan bundle build passes, including
  4,194,304 exact R50-reference comparisons with1542non-NONE hits, retained-column
  isolation, null-host rejection and integer extremes. The independent49-source
  oracle retains90960cases/1072clipped V4 witnesses. Existing water, terrain,
  watershed, cache and module gates pass.
- A separate direct JDK25 `javac -g` compiles all44core production source files
  for each variant; all71classes match Gradle outputs byte-for-byte. Source hashes
  are checked before/after compilation. The final native fixture also compares
  the loaded production DepositField class with its independently derived hash.
  This is stronger than archive/compiled-output equality alone.
- Seven archive CRCs and477compiled product-class identities pass. All compiled
  private test paths are checked absent from the product archives. Native core
  members equal the embedded core and independently compiled class hashes. Final
  candidate package SHA256 is400f743a7ab16f66ad901bd7663c7dd5b7887a0cac31cab467268f2a85217846.
- Four final matched packaged server processes (two R50 controls/two candidates)
  reopen copies of the same V5 snapshot. Explicit records, runtime class hashes,
  requested stops, all-dimensions saves, saved revision5, gold-block player edit
  retention,240260FULL-saved-chunk block comparisons and zero crashes pass.
- All120prior native chunk digests remain identical, including96V1–V4 digests.
  The old scoped native column/cave/water/soil/oracle checks also pass per process.
  Expanded native generation preserves128complete V5 chunk digests across16seeds,
  including long seed extremes. All eight **currently implemented** ore kinds have
  sixteen terrain-valid native witnesses each, checked against the wider oracle,
  native blocks and base columns. Observed ore/host pairs cover14of21compatible
  pairs; the seven missing pairs are explicitly listed in strict-native-audit.json.
  The native sample has32768columns:19255empty,13454one-body,59two-body footprints.

These selected witnesses establish functional coverage, not balanced gameplay
rarity or an unbiased ore-density ensemble. Larger catalogs, configurable deposits,
all host combinations, overlap balance, native client geological transects and
broader terrain/cave authoring remain unfinished; catalog/species stays deferred.
Dedicated-server runs do not exercise Vulkan graphics or establish owner GPU/FPS,
Windows compatibility, independent60Hz simulation or complete engine acceptance.

## Matched native allocation and timing

The profile measures server-thread ProtoChunk construction plus synchronous full
EngineChunkGenerator fill/decorate with prewarmed regional caches. It includes
48samples per process (12per site), two processes per variant. It excludes the
lighting/biome/network/render pipeline and process/GPU peak memory. Class hashes,
inputs and complete output digests match; there are no competing owned build or
native jobs. Host CPU isolation and equivalent external load are not established.
P95 uses nearest-rank over24samples per variant/site.

| Active footprint columns | Pooled median bytes saved | Median ms, R50 → final | P95 ms, R50 → final |
| --- | ---: | ---: | ---: |
| 33/256 | 80,280 | 6.143 → 6.162 | 7.000 → 7.506 |
| 81/256 | 62,424 | 8.670 → 8.631 | 12.034 → 9.093 |
| 125/256 | 47,160 | 6.110 → 6.185 | 6.712 → 6.446 |
| 219/256 | 13,320 | 6.400 → 6.300 | 7.734 → 6.972 |

All four total-allocation medians decrease. The array-only work model avoids
360VM-measured bytes per empty column:80280/63000/47160/13320bytes for these sites.
Observed total savings differ at the81-column site by576bytes; repeated processes
also show smaller allocation variations. Those differences remain unexplained,
not attributed to the source change. Individual samples and every earlier phase
are retained. One final p95 case worsens7.23%; the other three improve in this
sample. Medians are mixed. **No general latency or performance promotion is earned.**

The final sparse and dense escaping-column CPU probes use three processes/variant,
8warmups and9alternating batches each. Sparse allocated host+field bytes fall
480→179.2822per column (6843of8192empty), and dense480→427.96875 (1184of8192empty).
Query allocations are zero. Query timings vary less than1% in opposite directions;
construction timings are mixed and legacy-host controls vary between processes.
These are scoped observations, not a speed/FPS guarantee. Reference arrays are
outside the allocation counter; shared-empty initialization is outside warm samples.

## Rejected builds and reliability limits

Two attempts are excluded from final source acceptance. First, Gradle cannot read
an isolated copied `.rsync-tmp` output before Minecraft; cleaning only own outputs
allows retry. Second, a31-task nonincremental build reports success and its JAR
matches compiled outputs, but those outputs retain the earlier outer class. The
revised source SHA256 is0566beff7e02f7b70a69967f8594b42342fff69d83adaf8ba780e42b55c775b7;
expected class15411e2c22e74f3a6b019343db0918ad8a2e8d59a5aa61d4318e33831a702a38,
observed c60d43c9298601535a192f6b394559c1f40ae3aeb81283181f7e0151dee70870.
The earlier class's source and bytecode are retained. The revised-source build is
rejected despite Gradle0 and valid archive/compiled-output checks. Causes of both
artifact problems remain unproven; no speculative production workaround ships.

After clearing only the candidate's compiled outputs/bookkeeping, the final build
produces different, correct bytes. Independent compilation verifies all core
classes, and native runtime guards confirm the actual loaded production class.
Retain these guards for continuation: disabling incremental compilation alone was
insufficient in this environment. The earlier native profiles remain evidence for
their archived earlier source, not for the final revision. No game crash occurred
in this checkpoint; failures above were prelaunch/source-acceptance failures.

## Continuation and reproduction

Keep the candidate staged. Next investigate matched tail-latency context with
compilation/GC/host telemetry, cover the missing seven host pairs, and run Vulkan
client material/ore/cave transects plus broader density/overlap balance before V5
promotion. Continue terrain authoring and bounded Vulkan water publication after
those gates. Seamless streams/oceans, water budgets/pressure continuation,
enabled-view LOD and owner-GPU acceptance remain open. No installer was released.

Reconstruct the immutable parent, verify679original engine/Themes input hashes in
source-manifest.json, then apply only staged-manifest.json's eleven files. Never
restore the whole archival engine snapshot over consolidated main. Build the
candidate in project/ and an R50 control in baseline-project/ (same snapshot with
DepositField replaced by R50-DepositField.java). Use the forced build.py invocation
with fresh-compile.gradle, then verify_core.py before matched_server.py. The latter
uses root-game-task dispatch and expects the recorded production class hash.
coverage-sites.json/FindCoverage.java define the deterministic16-seed selection;
profile.py sparse/dense produce the microprobes. Exact commands and snapshot hashes
are recorded. Scripts identify the retained JDK25/JDK21/Gradle9.2.1, private build-only
NeoForm shim and local disposable V5 snapshot; use equivalent independently built
inputs in another environment. The synthetic saved-world input is preserved in world-input.zip with a file-hash
manifest; the guarded launcher restores and verifies it before making disposable
copies. Its extracted bytes match every file in the original R50 input used above.
For the control build, use the recorded helper invocation with --project pointing
to baseline-project; build.py defaults to the candidate project. The toolchain
and test-world snapshot are not distributed inside the mod. No background daemon is left running.


<!-- END PRESERVED SOURCE -->
