# Deep Primal dev105 — bounded ecotone property mixing

## Implementation
- Main source commit: 7938ce95c3f2382e9a1bf56320ecf9ce7d900c1a
- Added `EcotoneBlend.Weights.blendBoundedValues(...)` for continuous bounded cover/snow/soil coefficients.
- `blendUnitValues` now delegates to the bounded helper.
- The helper explicitly rejects invalid bounds and finite values outside the declared range.
- It does not mix physical heights, liquid volume, or hydrology ownership.

## Validation
- Focused source guard added at `tools/test_ecotone_bounded_property_guard.py`.
- This run re-fetched the committed source from `main` and checked the guard anchors.
- No Java25/Gradle, native Minecraft, saved-world reopen, GPU, or full water-engine acceptance was run.
- Existing independent baseline remains 50/100 (5.0/10); no regrade claimed.
