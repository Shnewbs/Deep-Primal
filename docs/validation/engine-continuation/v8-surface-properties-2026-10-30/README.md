# V8 surface-property consumer — dev107

## Implementation
- Source commit: 21c826c1792cd175445266037cc4b6f23def8f97
- Version bump: 4f2b2e09cfd28b27adf3a3796b4015e0ba5a126c
- Focused guard: 3a4bb0d1159d34f0dbe320803b30722513eb74c9
- Scope: `V8EcotoneColumnPlan.Column` now exposes continuous snow, vegetation, and soil factors from the saved ecotone sample, plus a bounded material-band projection for downstream preflight.
- Guardrail: the discrete band is explicitly nonphysical and must not drive height or water; continuous fields remain authoritative.

## Validation
The final files were re-fetched from `main` after the guarded writes. The focused source guard was replayed against the final committed source and passed as `DEV107_SOURCE_GUARD_PASS`.

This is source/static evidence only. No Java25/Gradle build, real Minecraft/native save-reopen, GPU, visual transect, performance, or full water-engine acceptance was run in this cycle. No playable or qualified JAR was built or published.
