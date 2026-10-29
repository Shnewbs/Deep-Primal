# Documentation audit and consolidation

**Input frozen:** `8f28f833caa259a0e59adac494a3aee8d750bc63`, October29, 2026. The request covers Deep Primal documents; unrelated projects' Markdown was left outside this corpus.

## Inventory and review depth

| Set | Coverage | Method |
| --- | ---: | --- |
| Original Git Markdown | 520 | Downloaded; every blob verified against its Git identity before editing; complete text structure/checkbox/duplicate/relative-target audit |
| Project Library Markdown identities | 24 | Downloaded exact bytes; version/identity retained; full text structure/hash/duplicate audit |
| Distinct source contents | 533 | Eleven byte-identical project/Git pairs share one canonical target; no source identity discarded |
| Prior research expansion | 507 Markdown paths | Complete tree/blob inventory; core integration/discrepancy chapters read; 487 frozen replicas not all reread line by line |
| Successors to Dev77 source set | 52 added, one changed, none removed | Complete path/blob comparison against earlier manifest |
| Repeated independent audit records | 107 documents | Structured extraction across all; latest ten chronological REVIEW files read fully |
| Repeated renderer records | 144 documents | Structured extraction across all; backlog and selected current/scoped reports read more deeply |
| Core design | Three merged chapters and source maps | Full readings of principal terrain/water, renderer/lighting, architecture/assets/UI specifications; targeted sections of large general/gameplay plans |

The automated pass examines every original's text, not merely filenames. It is a structural/document-consistency audit, not a claim that all repeated transcripts, catalog rows, executable implementations or external references were independently retested. Detailed domain review limits are in [architecture audit](audit/architecture-audit.md) and [evidence audit](audit/evidence-audit.md). Their line citations refer to pre-repair source bytes at the pinned commit.

A subsequent [publication supplement](PUBLICATION-SUPPLEMENT.md) checks five new Markdown documents from six concurrent commits; combined coverage is 525 Git originals plus 24 project identities.

## Changes made

- Created the documentation portal, three merged domain chapters, one cross-system requirement/gate register, policy/status ledger, complete catalog, research register and source-context index.
- Merged duplicate requirements by subject and retained detailed catalogs, original gap/decision IDs and historical evidence at their existing homes.
- Preserved 13 unique imported project documents byte for byte; eleven exact duplicates point to the existing Git document. The earlier expansion remains unchanged.
- Marked stale root/core/roadmap/module summaries as dated checkpoints. Added provenance notices to two historical build-policy files without rewriting their recorded authorization or altering schedules/worker state.
- Replaced the stale renderer backlog introduction with an explicit dated jam42 summary; converted its literal escaped newlines into actual Markdown line breaks.
- Repaired all **ten** malformed relative-target occurrences across six original Git documents where the intended tracked source/audit target was unambiguous.
- Recorded **16** inherited relative targets in preserved project snapshots through SOURCE-CONTEXT.md. One original telemetry CLAIM.json is unavailable in its expected layout; no substitute claim was fabricated.
- Added focused primary-source work on water numerical/boundary ownership, deterministic generation, edited LOD, presentation lifetime, checker limitations, temporal guide semantics, accessible focus/guide compilation and reproducible inputs.

The pre-repair scan found 1,213 relative-target occurrences and 26 untracked target occurrences across eight source identities. Ten were actual Git path errors repaired here; sixteen belong to preserved imported context and are explicitly mapped or marked unavailable. This scan checks file targets, not every historical heading anchor, external website, image appearance, local artifact or generated output.

## Evidence and unresolved work

The merge resolves documentation routing and makes contradictions explicit. It does not establish a new complete engine grade, functioning temporal upscaler, native V8 lifecycle, global routed discharge, durable native finite-cell coupling, hardware performance or a playable package. Component closure progress is retained alongside its unrun integration gates. The reported October27 full-build override remains an authority-provenance discrepancy because its original owner message was not recovered in this task.

The detailed research additions are proposed requirements, not proven runtime failures. Their primary-source dates/versions are retained, including the inconsistent current FSR4.1.2 device guidance. [POLICY-AND-STATUS.md](POLICY-AND-STATUS.md) gives the relevant distinctions.

## Verification

Run from a complete checkout:

```sh
python3 docs/handbook/tools/verify_handbook.py
```

The verifier checks all 544 source identities, expected published hashes, exact imported bytes, eleven duplicate groups, preserved expansion count, original Git relative targets and new handbook targets. It uses the frozen tracked-path inventory for code/assets not downloaded in this documentation-only workspace. The 12 intentionally edited original documents retain both source and published hashes in [catalog.json](audit/catalog.json) and [repairs.json](audit/repairs.json).

Later edits can intentionally invalidate this checkpoint. Refresh catalog identities only after inspecting that drift; do not silently change expected hashes to turn an unexplained failure green. The default run also hashes all locally available frozen-expansion files and reports those not materialized. Use `--require-complete-expansion` in a complete checkout to reject missing replicas as well. This workspace has nine expanded Markdown files available; the remaining 498 are tree-inventoried, not byte-reverified here.

The saved [verification receipt](audit/VERIFICATION.json) reports the checks actually run for this publication. No game build or GPU/native acceptance was performed for this documentation change.
