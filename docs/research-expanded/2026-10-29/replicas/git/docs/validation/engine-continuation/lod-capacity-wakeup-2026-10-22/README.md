# Expanded replica — docs/validation/engine-continuation/lod-capacity-wakeup-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/lod-capacity-wakeup-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/lod-capacity-wakeup-2026-10-22/README.md |
| Preserved source SHA-256 | `6ea120ef8b2ca295e0c3b00b62f93fc41fd84b39b3d799b055362118f86c6afd` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Waking an evicted persisted LOD page when its known payload fits newly freed GPU capacity at an unchanged camera position and budget. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the focused fit-without-eviction behavior does not establish broad enabled-view coverage or general renderer performance. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an explicit capacity-change wakeup event and a distinction between blocked retries, fitting admission and eviction-triggering requests. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Keep a real page blocked behind ledger-tracked ballast, assert zero repeated disk work while full, free capacity and require one successful retry without player movement. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- LOD wakeup after capacity is freed — 2026-10-22

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `capacity-baseline-failure.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/lod-capacity-wakeup-2026-10-22/capacity-baseline-failure.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# LOD wakeup after capacity is freed — 2026-10-22

Extends `1b6ef4e2` on `work/engine-after-alpha24`. One focused production change lets an evicted persisted page resume at the same player chunk and memory setting **when its known GPU payload fits without another byte or entry eviction**. No main merge or installer release.

## Reproduction and implementation

The exact-source baseline fails with free bytes but `started=false`. The native baseline independently uploads a real persisted 1,656-byte page, evicts it to make room for an actual 4,193,476-byte Vulkan vertex buffer under the isolated renderer's 4 MiB payload setting, and verifies 16 attempts perform no disk work while full. The ballast is inserted into the real cache ledger and closed through production eviction. Current production then still refuses to restart at the unchanged camera/budget; Minecraft asserts. See [baseline evidence](capacity-baseline-failure.json) and the preserved crash excerpt. This is a synthetic ballast allocation in an isolated renderer, not an authoritative-world teleport or full-scene pressure reproduction.

The fix adds the evicted page's byte size to `DeferredView`. An unchanged view stays deferred while those bytes do not fit or the 4,096-entry limit is full. Free capacity now makes the key eligible. Camera/rebudget behavior, 64-key selection/cleanup, ordered uniqueness, the 8,192-key index limit, revision guards, player edits and no-force-loading behavior remain intact. The pending-job/upload ownership path is unchanged. Source audit checks all 508 original build inputs: 506 unchanged, one production file and one opt-in fixture changed. Water runtime/core, Vulkan code and frozen V1/V2/V3 geography match the parent.

## Fresh checks

- Full uncached Linux Vulkan build: 25 tasks executed, explicit cache/core/water/geography/module records pass. The producer seals stable console bytes and raw/compressed hashes before exit. Six ZIP CRCs, **407 compiled-class matches**, five nested/standalone identities, platform natives/licenses and exclusion of validation/private-agent payload pass. The final development JAR is unchanged across the native runs: `d89d4ed97c0480e13eb69ebfcb222a24aa485225c41c1776cab74435a73b53c3`. No installer is supplied.
- Exact production record, eviction, forgetting, stale-result and queue/range methods run with explicit world/store/entry/byte-accounting doubles. Baseline fails at the required capacity assertion; candidate passes 1,000 blocked calls, the one-byte/exact-fit boundary, 4,096-entry admission, camera/rebudget behavior, stale-edit rejection, missing player, 9,000 admissions bounded to 8,192 indexed/guarded keys, 128 cleanup calls and 1,000 rotation calls bounded to 64 keys. These are not native measurements or FPS comparisons.
- **Two strict software-Vulkan sessions**, development and packaged origins, pass the real ballast/free-capacity recovery at the unchanged view, 16 no-read pressure calls, four earlier eviction cycles, four admission controls, injected queued-edit/stale-read rejection, actual native-chunk capture/store revision matching, eight frame-completion/eight open-pass rejection checks, device shader/transfer/depth/atlas/stencil/lightmap, reload, saved disconnect and rejected-completion shutdown. Final buffers/images, VUIDs and validation errors are zero. Both unforced sample launches use fresh copied `/tmp` game data and have absent markers at join/reload/disconnect/exit. The sample still explicitly requests Vulkan; saved UI preference is not certified.

## Restoration and self-audit

Workspace maintenance removed the old checkout/toolchain. GitHub recovery verified 511 blobs (508 build inputs plus three coordination docs); pinned JDK25/JDK21, Gradle, NeoForm/ASM and 11 graphics packages were restored and checked. The private compatibility agent passes its pinned-target/refusal controls and is excluded from the mod.

The first native setup fails before Minecraft in Gradle's temporary dependency Binary store. It is excluded; an unchanged baseline retry reaches the intended native assertion. The underlying cache failure is not claimed fixed. The cold baseline's client console is incomplete even though its independent daemon contains passing build records; that client log is not used for full build acceptance. The candidate's complete sealed build and strict sealed native records supply fresh acceptance. Prior marker/reload/atlas failures remain unresolved and are not converted to passes.

Two upright lake captures were inspected; both report two visible LOD pages. Repeated terraces and fine water patterns remain visual concerns. World simulation and renderer warming continue, so captures do not prove identical-state output or complete enabled-view water/mixed transparency. Software Mesa llvmpipe correctness does not guarantee owner AMD 8060S/FPS performance. No authoritative player edits or physical teleport roundtrip is accepted here; synthetic pressure does not establish full-scene sustained memory behavior. Solver substeps are not independent 60 Hz.

## Remaining and reproduction

Next test actual authoritative teleport, player edits, physical reload and sustained scene memory pressure with coverage assertions, then improve the demonstrated gaps. Resolve recovery beyond the 8,192-key limit and coarser fallback/hierarchy. Keep marker writer, resource reload/atlas stalls and direct-stop save safety open. Staged pressure p95/1,024 waiters/larger-region continuation, finite/renewable regional water budgets and coherent geology remain unfinished; catalog/species stays separately deferred. Earlier solver measurements are historical to this checkpoint.

Restore the current source into an isolated `project/` beside these scripts and pinned `toolchain/`. Run `check_capacity_recovery.py` and `build_candidate.py`, then `audit_vulkan_package.py`. Copy an audit saved world into a new disposable run directory and set `DEEPPRIMAL_NATIVE_RUN_DIR` explicitly without removing any owner's recovery marker. Run `run_vulkan.py disk-capacity-fixed` and `run_vulkan.py disk-capacity-packaged`; pass their actual output-directory names to `audit_capacity_native.py`. Respect the work-state lease and requirement register before launching. `before-VoxelLodRenderer.java` reproduces the baseline.


<!-- END PRESERVED SOURCE -->
