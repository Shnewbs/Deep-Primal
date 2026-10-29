# Expanded replica — docs/validation/engine-continuation/water-contact-integration-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-contact-integration-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-contact-integration-2026-10-27/README.md |
| Preserved source SHA-256 | `905aec555b50fa6d1bc1223590f6a9eb0b5481aea5cb4ce9e476bb7584dab29d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged native contact adapter allowing at most 2,048 neighbor attempts and exact pair deduplication per level tick, with independent constituent mixing caps. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that cosmetic surface contact across an air gap is excluded, and split diagnostic module loading after storage failures is not the final bundled game. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a physical adjacency and per-tick mixing contract distinguishing unchanged water quantity, server-owned sparse chemistry and bounded deferred activation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Test reverse pairs, partial lower cells, empty attempts and tick rotation, preserve exact salt and sediment totals and repeat through the fully qualified production classpath. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Bounded native water-contact integration candidate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Bounded native water-contact integration candidate

Source-only candidate `dev.32-contact-candidate.1`, based on main `6f90fe5c44877b7fe7f760f2ba86cb673dd7a64f`. Production remains alpha.29-dev.31. The new adapter and solver hooks are staged here, not enabled in a delivered JAR.

Consumed audit: `docs/engine-hourly-audits/2026-10-27T0127-5ba6c80/REVIEW.md`, pinned `5ba6c80f028fce77e237ee6dedc32f2fe876e33a`. Its ten equally weighted scores remain 50/100, average 5.0, carried rather than freshly regraded. The source-index production prelude/cursor findings remain open. This continuation addresses the roadmap's next contact-integration gap while complete producer capacity remains unavailable.

## Implemented

`WaterContactRuntime` connects the preceding exact `WaterAccount.diffuseWith` operation to native loaded cells. The finite solver invokes it only after admitting an existing wet cell. At most 2,048 neighbor attempts per level/game tick, including empty/blocked attempts, are admitted. A canonical pair set prevents reverse or repeated activation from mixing the same contact twice that tick. Retained edges are also bounded by 2,048. Retry and changed-cell activation use the existing capped finite-water FIFO; no new unbounded queue or elapsed-time catch-up is introduced. Starting direction rotates each tick.

Only adjacent loaded engine-water cells participate. A vertical pair requires its lower cell to be physically full; a cosmetic surface bridge across an air gap cannot create contact. Mixing caps salt and sediment independently at 256 mass quanta per pair/tick and changes neither cell's water quantity. Sparse metadata is committed on the owning server thread after rechecking native state and sampled parcels. No block write, ocean credit, source delivery, or neighbor notification occurs between the two metadata writes. Explicit zero-salt ocean overrides remain stored. Native level-unload/server-stop hooks clear transient contact ownership.

Sleeping components are not globally scanned or instantaneously homogenized. Existing activation initiates contact work. Broad wake coverage, fairness under sustained saturation, physical diffusion calibration and named-hardware allocation/tick tails remain unaccepted. Geometry V1–V7, source locations, saves, approved art, renderer and module identity are unchanged.

## Validation and limits

- 12,302 deterministic Java25 boundary-double cases pass, including 12,288 volume/direction/random-mass pairs, reverse/repeated visits, physical vertical gaps, loaded-border reopening, wrong-thread rejection, clock wrap, stale block/metadata reads, boundary defaults, snapshot reconstruction and lifecycle cleanup. 100,000 repeated dry-neighbor calls remain capped at 2,048 attempts. A disabled-mixing negative control fails at the contact oracle as intended. These doubles are not multiplayer or native unload timing evidence.
- All three complete candidate Java sources compile against privately copied, hash-verified Minecraft26.1.2/NeoForge26.1.2.122 APIs: 13 class files. No dependency or output of the completed auditor was modified. Existing exact diffusion (201,134 oracle cases), pair/network (101,024 properties, 10,000 repeats, 200,000 steps), and aquifer/weather/accounting regressions pass.
- Full 139-source Java25 core compilation/suite is recorded separately in `full-core.log` and `checks.json`. Baseline sources match all corresponding Git-blob/SHA256 entries in the pinned auditor's source manifest; only WaterAccount is overlaid.
- Actual Linux native diagnostic fresh/reopen processes pass. Two full sealed cells straddling x=15/16 mix salt and sediment to exact equal quantities while each retains 4,096 water quanta. Reverse replay and the actual finite solver tick preserve those values. The real HydrologyData codec, acknowledged `save-all flush`, process restart, persisted parcels, clean stop and exit0 pass. Loaded WaterAccount/WaterRuntime/WaterContactRuntime hashes match freshly compiled candidates. Seven diagnostic archives pass CRC/no-duplicate checks; 13 compiled class matches and private-fixture exclusion pass; six module hashes remain unchanged across both runs.
- The diagnostic substitutes candidate classes into a qualified baseline and loads its modules separately to avoid nested extraction duplication. **It is not a full Gradle-built distribution, newly versioned release, or user-facing JAR.** Source identity and split-assembly hashes are retained. No new JAR is uploaded or saved.

## Failures retained

Initial broad dependency selection included build-tool ASM and failed Java25 class scanning before the fixture. Runtime-only filtering corrected that setup. The next bundled diagnostic failed with ENOSPC while extracting Themes; it was rejected and replaced by separately loaded unchanged module bytes. An initial split preparation assumed every module had mods.toml; Core is an FML LIBRARY and is now explicitly placed on the diagnostic classpath. Another native run reached the assertions but was rejected for malformed default flat settings and a debug-appender/kqueue initialization error. Final runs use explicit flat settings and the existing INFO-level server logging configuration, on a new world.

The final runs retain a narrowly classified offline-auth warning: the host cannot resolve api.minecraftservices.com/publickeys. No online-auth or multiplayer claim is made. All other exception/error lines fail the diagnostic receipt; earlier runtime errors are not counted as success.

Overlay storage remains full. A fresh isolated Gradle project/cache/output footprint cannot fit; the previously measured retained producer minimum alone exceeded 1.3GB before source/temp expansion. Full Gradle/build/module/distribution qualification, packaged weather/travel integration with the staged source index, real OpenGL/Vulkan continuity, Windows/AMD8060S, long soak and owner-hardware latency remain open. Exposed GitHub tools still have no release/asset-upload mutation. No release was attempted without a qualified binary.

Own obsolete duplicate restoration files were removed after byte comparison with retained tools; two incomplete archive intermediates were discarded. A temporary archive reappeared on independent inspection, so durable space reclamation is not claimed. Other roles' checkouts/caches/outputs and the newest-three user-facing JAR policy are preserved.

## Reproduction and continuation

Stage overlays: WaterAccount into engine-core, WaterRuntime and new WaterContactRuntime into native engine. `make_fixture.py` materializes controlled boundary doubles; compile them with the staged contact/account sources and ContactChecks, then run its main. `api_compile.py` snapshots pinned dependencies and compiles the entire native candidate; `prepare_native.py`, `split_native.py`, ContactNativeChecks, serverLog4j2.xml and `run_native.py fresh/reopen` reproduce the private native diagnostic. Scripts retain this run's explicit private paths; change them together when restoring into another isolated workspace. Never run them against another writer's mutable outputs. Core source hashes, API input hashes and diagnostic archive receipts are included.

Next: restore full producer capacity; qualify the combined source-index/contact candidate with complete build/module/package and broader native weather/travel tests; then single-owner loaded/unloaded exchange, persistence/player edits and bounded catch-up. No engine/water acceptance gate or score is closed by this scoped result. The builder remains enabled; its bounded writer lease is released at checkpoint.

Final cleanup: after checkpoint `4aa62dd72fe72090efc07630d8ee3a1a5c46accd`, 382,313,574 bytes of this run's disposable copied dependencies, compiled classes, private diagnostic assemblies and fixture worlds were removed. Every selected path was absent on a separate inspection. Source, compact failure diagnostics and successful checks remain committed. Post-cleanup memory-backed free space was 964MiB; overlay remained full. This success does not resolve the separate reappearing overlay archive.


<!-- END PRESERVED SOURCE -->
