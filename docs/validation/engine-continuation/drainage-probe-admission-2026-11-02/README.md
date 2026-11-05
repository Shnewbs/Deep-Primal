# Dev174 — Mountain drainage finite-probe admission

**UTC:** 2026-11-02 01:07. **Baseline:** `7d257452b4af338784152fed0c4bfc4bbe4fbd49` (Dev173 audit). **Scope:** `MountainDrainage`, its dedicated core regression and `DrainageChecks` registration. No Actions or renderer/LOD/organics changes.

## Finding

`MountainDrainage.build()` already rejected nonfinite values at the 65x65 integer routing grid, but its contained-channel profile and optional lake-bank calculations called the terrain callback again at interpolated/off-grid positions without validating the result. A `NaN` from a bank probe can propagate through `Math.min` into a cached profile, then to a sampled waterline or geometry. `-Infinity` can similarly enter a profile. Invalid sea levels were not rejected by the constructor.

## Correction

All four terrain callback sites now use `terrainHeight(x,z)`, which rejects nonfinite output before it can enter the routing grid, channel profiles, or lake-bank calculation. The constructor rejects null terrain and nonfinite sea level. The existing finite-valued terrain math and region-cache behavior are unchanged. No generator version, save schema, liquid amount, Vulkan shader, or LOD code is changed.

New `MountainDrainageFiniteProbeChecks` tests valid contained-region replay and rejects NaN/positive-infinity/negative-infinity on off-grid profile probes and invalid sea levels. It is registered by `DrainageChecks.run()`.

## Evidence and limitations

- Source commit: `afee02ba5807073b21050e4ba36dae6a5b493649`; Git blob `c288ce7b2122b80038ba8fe4151a84b4e95630c8`.
- Regression commit: `790e17ebe53b59028ebca6f0c5306030505183d7`; test blob `f04c826f8788710115132bef95ef85777fe99286`.
- Regression registration commit: `084448265ce2340156add34befb5f0a98b145a71`; blob `f38db53c58d0c9f03e32e0e9d18f92784c98044a`.
- Local Java21 strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed **100,006 assertions** on an independently reconstructed *admission-method fixture* (100,000 finite parity and six invalid-input/negative-control assertions). This is **not** a compilation or execution of the complete production class or the newly committed integrated test.
- Fixture SHA256: `3355af853cf91a70e2095d4f063b425444aa4cba8c5dda981d59f9f524e4b509`. Log SHA256: `c22fb45a607eefe99bd39d00c4a58534952704fe9edb728f9a5ea5e0329966c3`.
- GitHub source blobs and registered test are independently re-fetched after commit. Full build remains blocked by absent Java25, full private checkout, Gradle/NeoForge dependencies and local GitHub DNS.
- **No playable JAR, JAR SHA256, native save/reopen, GPU acceptance, or whole-engine grade change.** Preserve previous successful JAR until a replacement is actually built and verified.

## Follow-up

Restore a complete Java25 build workspace; run `./gradlew check coreChecks jar` plus JarJar packaging as configured by the root build, run `DrainageChecks` and fixed-seed river-profile/lake-bank acceptance, and inspect river/waterfall seams before changing any retained JAR.
