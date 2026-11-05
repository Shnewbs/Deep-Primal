# V8 saved blend-policy consumption — 2026-10-30

## Implementation
- Source commit: 7fc70431c4ee878887b7eded30c4e3d0237890ff
- Scope: V8GeneratorSnapshot constructor admission only.
- Change: every snapshot now resolves its saved blend policy through V8BlendPolicy.effectiveForSnapshot(...) before invariant checks. Legacy revision 1 retains the HARD fallback; newer revisions still require the explicit versioned policy and fail closed.
- No graph bytes, saved profiles, native registration, terrain blocks, water quantities, or renderer-owned files were changed.

## Validation
- Focused source guard: tools/test_v8_saved_blend_policy_consumption.py
- Result: DEV104_SOURCE_GUARD_PASS
- This is source/static evidence only. No Java 25/Gradle build, Minecraft native execution, save/reopen, GPU, visual transect, or water acceptance was performed.
- The independent baseline remains 50/100 (5.0/10); no regrade claimed.
