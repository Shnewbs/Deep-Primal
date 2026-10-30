# V8 blend-policy admission — 2026-10-30

## Source

- Implementation commit: 534b285bd9dd1219a2f937be2a59e366a524b243
- Focused guard commit: f2a8c2a65a41a3b45ba44b520a952da6c1b1f6f2
- Scope: V8 saved-profile policy admission only

## Implemented

- Legacy V8 revision 1 with a missing policy resolves to HARD, preserving existing hard behavior.
- V8 revisions newer than 1 require an explicit versioned policy and fail closed when the policy is missing or incompatible.
- No saved graph/profile bytes are rewritten and no native V8 registration is activated.

## Validation

The guard checks the committed source for the legacy fallback, explicit-policy requirement, and incompatible-version rejection. This is source/static evidence only; no Java25/Gradle, Minecraft/native save-reopen, GPU, visual transect, water, or performance acceptance was run in this cycle.

## Open gates

Native V8 profile reopen and edit parity, old/new-generation borders, continuous material/soil/snow/vegetation integration, finite-water authority integration, and the complete independent ten-category regrade remain open.
