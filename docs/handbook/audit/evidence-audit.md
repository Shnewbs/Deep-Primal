# Deep Primal documentation evidence audit

Audit date: 2026-10-29. Input: the parent's pinned `main` document download at `8f28f833caa259a0e59adac494a3aee8d750bc63`. This is a documentation audit: no production source was changed, no implementation was independently retested, and no engine grade was assigned.

## Review coverage and limits

Structured heading/status extraction covered all **107** Markdown files under `docs/engine-hourly-audits` (**13,083 lines**), including two supporting reports. The latest ten chronological `REVIEW.md` files, Dev77/78/79/81/82/83/84/85/86/87, were read in full. Root coordination files and the end of both large historical handoffs were inspected. Renderer structured extraction covered **144** Markdown files (**4,499 lines**) after the download completed; the current renderer backlog and selected current/scoped reports were read more deeply. Counts are scoped to the files present when this review ran, rather than an independent claim about every document in the repository.

Repeated historical build JSON, assertion transcripts, and all individual renderer reports did **not** receive equal line-by-line review. Detailed extraction is retained in the adjacent `engine-hourly-audits-structured-review.json` and `renderer-jams-structured-review.json`. Source statements below are observations of the documents, not fresh implementation acceptance.

## Definite documentation defects to correct

| ID | Finding and evidence | Consolidation action |
| --- | --- | --- |
| EVID-01 | `docs/renderer-jams/RENDERER-BACKLOG.md:3` labels jam28/5.71 as the last source checkpoint, while `:113` records jam42/5.86 and its implementation commit. | Replace the unqualified last/current header with a keyed latest checkpoint, or explicitly date it as historical. Never infer current state from the first paragraph of an append-only file. |
| EVID-02 | The same backlog `:38` embeds literal `\\n` sequences inside a heading/paragraph. | Convert those delimiters to actual Markdown line breaks. Preserve the entry's substantive evidence. |
| EVID-03 | The same backlog `:79` links to `docs/renderer-jams/20261029-squared-error-selection/REVIEW.md` relative to its own `docs/renderer-jams/` directory. | Use `20261029-squared-error-selection/REVIEW.md`; the prefixed target resolves to a nonexistent nested directory. |
| EVID-04 | `docs/ENGINE-BUILDER-AUDITOR.md:3,12` and `docs/ENGINE-HOURLY-BUILDS.md:3,19,24` present October25 build/release authority as current. Newer Dev87 audit `docs/engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md:19` records an October27 owner override reserving full builds/JAR publication to the owner; Dev85 `.../2026-10-29T1932-be9cde82/REVIEW.md:9` repeats it. | Record this as an authority/provenance discrepancy and add a current policy ledger. The repeated audit statement is evidence that the override was reported; it is not the original owner instruction. Do not invent release permission from either the old policy heading or an auditor's repeated paraphrase. The present user request authorizes document work. |
| EVID-05 | `docs/ENGINE-BUILD-AUDIT-HANDOFF.md:299` retains an inclusive “at least8.5” target. `docs/ENGINE-HOURLY-BUILDS.md:9` and Dev87 audit `:25` require a fresh complete result strictly greater than8.5. | Keep the handoff passage historical and use the strict threshold in the current requirements register. |
| EVID-06 | Dev87 audit `:21` reports root `ENGINE-WORK-STATE.json` as Dev53 and scoped terrain state as Dev87; the same drift is reported at Dev81 `.../2026-10-29T1532-b6448f52/REVIEW.md:18`. | Index state files by subsystem, schema, source commit, updated time and owner. Do not overwrite one role's ownership record merely to make version labels agree. Stale aggregate state must be clearly marked and reconciled by its owner. |

Line citations above refer to the downloaded source documents. Link repair and formatting are safe document edits; ownership or delivery policy changes require actual decision provenance.

## Merge historical findings as a progression

A chronological audit must keep a defect's detection, scoped correction and unaccepted integration gates together. Otherwise an older report can resurrect a corrected component defect, while a later helper test can be mistaken for a complete feature.

| Evidence progression | Correct merged status |
| --- | --- |
| Dev78 admits a malformed perimeter cycle (`2026-10-29T1231-bee0509a/REVIEW.md:11`); Dev79 rejects it and independently rejects older/mutated variants (`2026-10-29T1333-83801d65/REVIEW.md:7–11`). | Observed perimeter-cycle admission corrected in the tested source; global routing/native water acceptance remains open. |
| Dev83's immutable ledger admits two separate successor candidates from one initial snapshot (`2026-10-29T1730-2e3c945d/REVIEW.md:10`); Dev84 documents a generation-CAS durable store (`2026-10-29T1834-f53abbf6/REVIEW.md:5–9`). | Component storage/CAS tests exist; atomic Minecraft finite-cell plus ledger ownership and real-world recovery remain open. |
| Dev84 accepts a valid foreign-world checkpoint and noncanonical ordering (`.../2026-10-29T1834-f53abbf6/REVIEW.md:10–11`); Dev85 tests V2 world/profile binding and canonical tables (`.../2026-10-29T1932-be9cde82/REVIEW.md:7`). | Component cross-world/order findings corrected in V2; native publication and coupling unaccepted. Do not re-list V2 as unbound merely by copying Dev84. |
| Dev86 planner tests and Dev87 live admission tests run with API doubles; actual materializer/rollback/save lifecycle remains unexecuted (`.../2026-10-29T2030-3df845f0/REVIEW.md:13–15`; `.../2026-10-29T2131-5f7c3f68/REVIEW.md:15–19`). | Source-tested opt-in vegetation and guard, rather than native gameplay acceptance. Placement plan differs from realized placement receipt (`Dev87:17`). |

Directory timestamps and displayed development versions are insufficient to establish ancestry. Preserve full pinned commit IDs, implementation commits, evidence hashes and test scope when merging; concurrent renderer and engine changes can share a main version without sharing qualification.

## One evidence vocabulary

Each merged requirement should carry separate fields for implementation state and qualification state. Recommended implementation states are `proposed`, `staged`, `source-integrated`, `runtime-active`, and `retired`. Evidence records should identify `document-inspected`, `source-exact component execution`, `model or API-double execution`, `full build`, `package identity verified`, and `native platform acceptance`. A test can pass while the feature remains staged or inactive. Keep failed attempts and negative controls distinct from positive product execution.

Every qualification record needs a requirement ID, immutable source commit and input blobs, environment/JDK/platform, command, actual result, evidence location/hash, author versus independent role, unrun gates, and applicability limit. Counts cannot be summed across repeated reports to imply growing coverage; a million assertions in one isolated arithmetic helper do not supply a missing native lifecycle test.

Grades also require a rubric ID and coverage date. The latest fully read audit carries **50/100,5.0/10,Tier2** from October24 rather than conducting a fresh regrade (`Dev87:25`). Renderer jam37 explicitly identifies its six-category score as an author self-audit (`docs/renderer-jams/20261029-fused-resident-publication/REVIEW.md:5,30–44`). These are separate instruments; neither can substitute for a fresh complete ten-category independent grade.

## Prioritized acceptance register to retain

1. **Native water transaction and recovery:** demonstrate one durable owner for finite cells, water/salt/sediment ledger, receipt/generation and world/profile binding through loaded/unloaded transfers, edits, restarts and failure injection. A forced Linux component file is not Windows filesystem or Minecraft-save proof (`Dev84:12–15`; `Dev85:9,13`).
2. **Certified marine routing:** distinguish observed region links and acyclic forests from authoritative global outlet chains and realized discharge. Current helper qualification does not establish global ocean endpoints (`Dev81:18,24`; `Dev83:10,33`).
3. **V8 native lifecycle:** real codecs/profile snapshots, generation activation, chunk/base-column/heightmap parity, vegetation support, callbacks, rollback, old profiles and saved reopen. Preserve older geography and staged candidates until relevant gates pass (`Dev86:13–17`; `Dev87:17–21`).
4. **Renderer acceptance:** named-hardware Vulkan/OpenGL same-scene tests, cold/warm travel/edit/reset/reload/resize/shutdown, transparent-water pixels, p50/p95/p99 CPU and GPU frames, RAM/VRAM and draw/descriptor counts. Component medians and CPU submission timers remain component evidence (`RENDERER-BACKLOG.md:5–12`; jam37 `:28,44,48`).
5. **Current artifact identity:** only a completed full build plus nested/outer archive checks, native payload/notices, private-fixture exclusion and exact pushed-source/compiled/package identities can qualify a new JAR. A source-evidence ZIP and an old JAR renamed to a new version do not satisfy this gate (`ENGINE-HOURLY-BUILDS.md:12,17–19`; `Dev87:19,29`). Publication and retention rules must be resolved from actual current owner instructions before execution.

Historical acceptance gaps remain useful as test requirements. They should not be repeatedly copied as current defects without checking later closure evidence, and no unrun gate should become complete merely because the document set has been merged.
