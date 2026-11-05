# Deep Primal — expanded Markdown corpus

**Edition:** 2026-10-29  
**Source repository:** [Shnewbs/Deep-Primal at 33c43c943f04](https://github.com/Shnewbs/Deep-Primal/commit/33c43c943f0469195e7ab1a6852ab4360fad1788)  
**Coverage:** 487 original Markdown documents, 487 expanded replicas.  
**Scope:** documentation research and proposed engineering detail.

This edition preserves every inventoried original and adds source-specific scope notes, gaps or details to deepen, proposed contracts, acceptance checks, and links to shared technical chapters. The originals in the repository and project collection remain unchanged.

## Start here

1. Read [the integrated engine design expansion](ENGINE-DESIGN-EXPANSION.md) for the recommended architecture, cross-system ownership, and implementation order.
2. Read [the discrepancy register](DISCREPANCIES.md) for version drift, historical policy, finite-water interpretation, renderer generations, asset exceptions, and evidence boundaries.
3. Open a technical chapter below, then its linked original replicas.
4. Use the coverage indexes for all 487 documents and the [manifest](manifest.json) for hashes, provenance, duplicates, review method, and original dependency references.

## Technical chapters

| Chapter | What was expanded |
| --- | --- |
| [Architecture and authoring](chapters/architecture-authoring.md) | Module and data ownership, schemas, import/reimport, stable IDs, migration, extension contracts |
| [Assets, materials, and UI](chapters/assets-materials-ui.md) | Texture policy and exceptions, material families, channel semantics, preview/import workflow, settings transactions |
| [Terrain and finite water](chapters/terrain-water.md) | Typed terrain data, world context, deterministic placement, regional topology, conservative transfers, durable recovery |
| [Rendering and performance](chapters/render-performance.md) | LOD coverage, bounded queues, epochs, resource ledgers, GPU lifetime, temporal inputs, capability gates, measurement |
| [Lighting integration](chapters/lighting-integration.md) | Binding the lighting master to actual world, material, renderer, LOD, and temporal reconstruction boundaries |
| [Validation and evidence](chapters/validation-evidence.md) | Independent oracles, native boundaries, lifecycle fixtures, package identity, benchmarks, carried grades, promotion |

The detailed [Lighting Engine](replicas/project/LIGHTING-ENGINE.md), [Lighting Research Notes](replicas/project/LIGHTING-RESEARCH-NOTES.md), and [Water Engine](replicas/project/WATER-ENGINE.md) are included as expanded replicas.

## Complete coverage

| Source group | Documents | Index |
| --- | ---: | --- |
| Terrain, water, geology, ecology | 33 | [All terrain/water replicas](coverage/terrain.md) |
| Rendering, lighting, LOD, performance | 136 | [All rendering replicas](coverage/render.md) |
| Architecture, assets, UI, continuation records | 175 | [All architecture replicas](coverage/architecture.md) |
| Audits, release validation, evidence | 143 | [All audit replicas](coverage/audit.md) |
| **Total** | **487** | [Machine-readable manifest](manifest.json) |

The inventory contains 468 Git Markdown files and 19 project-collection Markdown files. 9 pairs share identical source bytes. All identities are retained, and duplicate relationships are recorded rather than silently removing documents.

Research began at the Dev68 source snapshot. An initial 17-commit refresh to Dev69 added three Markdown originals and refreshed the renderer backlog. The final fixed input is **Dev77**, `33c43c943f0469195e7ab1a6852ab4360fad1788`: a complete 102-commit comparison from Dev69 added another 35 Markdown originals and refreshed the backlog again. All changes are included; the nineteen project-collection Markdown originals were rechecked and unchanged. Later concurrent Git work is preserved by publication but is outside this edition's explicitly pinned input snapshot.

## Research coverage

The new comparative study uses **40 primary sources**, covering Unreal Engine, Unity, Godot/Voxel Tools, Houdini, Gaea, World Machine, Blender, Adobe Substance Designer, meshoptimizer, AMD/NVIDIA rendering SDKs, Khronos Vulkan, scientific terrain/hydrology work, schema evolution, asset reproducibility, and test/benchmark infrastructure.

Together with the retained lighting study, this edition references **108 distinct primary-source URLs**. This is a selected technical survey of relevant systems and interfaces; proprietary implementation details and uninspected products are not inferred.

- [Terrain and water research notes](research/terrain-water-research.md)
- [Rendering and performance research notes](research/render-performance-research.md)
- [Architecture and authoring research notes](research/architecture-authoring-research.md)
- [Validation and evidence research notes](research/validation-evidence-research.md)
- [Complete new-source bibliography](research/SOURCES.md)

## How to read a replica

Each replica begins with provenance and the new research expansion. Its final section contains the original bytes unchanged. Original source links keep their original context. An added source-context index resolves tracked code/log/image dependencies against the pinned Git commit; missing historical/generated artifacts are listed in the manifest without claiming current availability.

**Full-text review (95 documents)** means the complete document was read for the expansion. **Structured-evidence review (392 documents)** means the document's headings, scope, findings, limitations, reproduction/evidence sections, and named subjects were examined through structured extraction and targeted reading. Large repeated historical records and catalog rows were not all given the same line-by-line treatment as core design specifications.

The machine-readable review cards in [reviews](reviews) record the same per-document decisions. Shared chapters hold common contracts so hundreds of historical reports do not need to repeat the entire research survey.

## Preservation and verification

The verifier checks inventory coverage, uniqueness, complete replica hashes, embedded-source hashes, exact Git blob identities, per-document reviews, and authored relative links. It treats original embedded links as historical source content and reports their unresolved dependencies separately.

From the repository root:

~~~sh
python3 docs/research-expanded/2026-10-29/tools/verify_corpus.py
~~~

See [the verification receipt](VERIFICATION.md) for the checks actually completed for this edition. This documentation task does not run game/native/GPU tests, certify an installer, alter saved worlds, or assign a fresh engine grade.

## Updating this edition

Create a new dated edition from an explicitly pinned input inventory, or make a clearly versioned correction to this edition. Keep original-source bytes and provenance immutable. Exclude the generated research-expanded subtree when inventorying original project documents in a future edition, so replicas do not recursively become new source documents.
