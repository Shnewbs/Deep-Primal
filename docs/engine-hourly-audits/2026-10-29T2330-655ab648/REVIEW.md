# Independent Dev90 source audit — 2026-10-29 23:30 UTC

Pinned main: `655ab648212c3d378d638ea0a99ca1c42f2bf4ec`. Independent, read-only, isolated Linux x86_64 audit. The Builder was not paused, its lease/cache/output unchanged. No production files edited.

## Actual new checks
- Git-exact committed `V8OutletLedgerStore.java` blob `0a7e659432e8ef796e555ea6f73db82fd374fe92`, `V8ConservedOutletLedger.java` blob `e41226dbf0daf40ea455ee44247e2c4be81f29e9` and `WatershedOutletTopology.java` blob `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a` were reconstructed from approved retained evidence and verified with `git hash-object`.
- Strict local Java21 `javac --release 21 -Xlint:all -Werror` passes; class major 65. Two Java `-ea -Xverify:all` test runs each pass 384 independently authored assertions (768 total), covering 120 durable commits, exactly-once replay, conservation of [8000,4400,3700] water/salt/sediment, world binding, pending-crash fail-closed recovery, close/reopen and directory/symlink/hardlink aliases.
- Five independently compiled fault variants all caused the intended assertion (5/5): skipping pending-open or pending-commit guards, allowing checkpoint symlinks/hardlinks, or failing to poison after rejected I/O. These are separate from Builder-recorded Dev90 149 assertions twice and 16/16 mutation controls.
- Latest Dev89 code at [V8OutletAuthorityAdmission.java](https://github.com/Shnewbs/Deep-Primal/blob/655ab648212c3d378d638ea0a99ca1c42f2bf4ec/engine-core/src/main/java/com/shnewbs/deepprimal/core/V8OutletAuthorityAdmission.java#L58-L151) was reviewed at source level; its independent marine and directed-receiver witnesses remain owner-provided. Dev90 [store L149–211](https://github.com/Shnewbs/Deep-Primal/blob/655ab648212c3d378d638ea0a99ca1c42f2bf4ec/engine-core/src/main/java/com/shnewbs/deepprimal/core/V8OutletLedgerStore.java#L149-L211), [L230–304](https://github.com/Shnewbs/Deep-Primal/blob/655ab648212c3d378d638ea0a99ca1c42f2bf4ec/engine-core/src/main/java/com/shnewbs/deepprimal/core/V8OutletLedgerStore.java#L230-L304) implement the tested safeguards.

## Open findings / next Builder priorities
1. P1: Native Minecraft finite-water cells, unloaded chunks and player edits are **not atomically reconciled** to ledger receipts after crashes. Dev90 intentionally leaves orphan pending frames for an owner-driven recovery, not proof of physical transaction consistency.
2. P1: Certified global marine roots / directed outlet witness must be established from actual saved-world topology, not merely supplied to the Dev89 admission API.
3. P1: Owner's full Java25 Gradle9.2.1 / Minecraft / Windows AMD8060S Vulkan+OpenGL/packaging/CRC/native-save-reopen/shutdown acceptance remains outstanding; Java25 and owner archive not present in audit workspace and container cannot resolve GitHub for complete checkout. October27 override reserves full builds and JAR delivery to owner. **No new JAR was produced or accepted.**
4. P2: Root work-state remains Dev53 and `gradle.properties` declares `0.0.1-alpha.29-dev.88-vegetation-receipts.1`, while Dev89/Dev90 are source-only checkpoints. Reconcile through scoped Builder, without auditor changing Builder state.
5. P2: Native V8-r6 vegetation receipts and renderer jam43/real water cliff GPU performance are not verified here. Frozen V1-V7 replay was not rerun.

## Ten equally weighted categories — **carry-forward**, not new whole-engine regrade

| Category | Score |
|---|---:|
| Terrain realism and variety | 5/10 |
| Biome blending and coherence | 4/10 |
| Hydrology and caves | 6/10 |
| Features and structures | 4/10 |
| Performance and threading | 3/10 |
| Determinism and stability | 7/10 |
| Architecture and quality | 6/10 |
| Extensibility | 4/10 |
| Testing, benchmarks and documentation | 7/10 |
| Polish and tooling | 4/10 |
| **Total, Tier 2** | **50/100 (5.0/10)** |

The baseline is the accepted [2026-10-24 review](../../engine-review-2026-10-24/REVIEW.md). The goal requires a **fresh full grade strictly >8.5/10**; no score credit is assigned merely to staged/unused source helpers. Java25, full Gradle/Minecraft, native saves, packaged outer/nested CRCs, owner Windows GPU, sustained timing and accepted development JAR were **not run**. No release, overwrite, JAR rotation or Builder interruption. These test results are Linux local-filesystem component evidence, not Windows acceptance.

Independent full evidence ZIP is saved in Library at `/Deep Primal/Independent Audits/DeepPrimal_Dev90_IndependentAudit_20261029T2330_655ab648_Java21Evidence.zip`, SHA256 `b43c327dee5daada70d280456da4740ad8249722aed727447825ca306cf6c2f2`, 26,097 bytes; includes independent oracle, production source hashes, compiler logs, 5 negative cases, retained receipt and integrity manifest.
