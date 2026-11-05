# Deep Primal dev109 — saved V8 profile fingerprint guard

## Implementation
- Added `V8GeneratorSnapshot.savedProfileFingerprint()`, a stable identity over the complete immutable saved V8 profile envelope, including version, signed seed, density/ground profile, blend widths, surface/climate/hydrology bindings, and vegetation policy.
- Added `Prepared.requireSavedProfileFingerprint(long)` so saved-profile reopen/edit callers can fail closed before native materialization when a decoded profile is not the exact prepared profile.
- Existing serialized V1–V7/V8 envelopes are unchanged; this is an admission guard, not a migration or implicit terrain activation.
- No terrain heights, physical water, hydrology ownership, renderer files, or legacy profiles were changed.

## Direct-main commits
- `366f8093847953ac9d725eeba784a58b3f517178` — saved-profile fingerprint guard
- `d70e460fc830df0fcf4140f82883a844c3ddacf3` — source version `0.0.1-alpha.29-dev.109-saved-profile-fingerprint.1`
- `677078001785abba55e503d337d0aa80648507f5` — focused source guard

## Validation
The focused guard checks the final source for the fingerprint method, prepared-profile guard, fail-closed mismatch message, and inclusion of the latest vegetation-policy identity. This is source/static evidence only.

## Open gates
Java25/Gradle, native Minecraft save/reopen/edit, GPU, visual transects, performance, and full water-engine acceptance remain owner-side gates. No playable or qualified JAR was built or published.
