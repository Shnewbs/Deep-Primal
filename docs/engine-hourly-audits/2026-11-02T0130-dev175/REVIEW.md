# Dev175 — Independent Deep Primal audit (2026-11-02 01:30 UTC)

**Previous audited head:** `7d257452b4af338784152fed0c4bfc4bbe4fbd49` (Dev173). **Reviewed engineering head:** `c502dc7ac33dc37c0eea865c632c17ecefebe768` (Dev174). **Delta:** six commits, five paths. **Correction commit:** `0065d516864060f894d80c929a8918e9797f6a65`. No GitHub Actions used.

## Reviewed changes and scope

The six Dev174 commits claim/release bounded drainage work, reject nonfinite terrain callbacks at routing and off-grid profile/lake probes, add `MountainDrainageFiniteProbeChecks`, register it in `DrainageChecks`, and document isolated validation. The Dev174 writer claim was `completed-released` before this audit. No Vulkan/OpenGL, LOD, performance, save format, or other engine module was modified by this delta. Those systems were not independently benchmarked or runtime accepted in this run.

[Compare Dev173 to Dev174](https://github.com/Shnewbs/Deep-Primal/compare/7d257452b4af338784152fed0c4bfc4bbe4fbd49...c502dc7ac33dc37c0eea865c632c17ecefebe768)

## Confirmed defect and correction

The Dev174 `terrainHeight()` gate rejected `NaN` and infinities, but allowed finite double heights that cannot be represented by the `float[] water` and `float[][] profiles` storage. Example: `height = 2 * Float.MAX_VALUE` passes `Double.isFinite`, but `(float) Math.max(96, height - 2)` is `Infinity`. A similarly extreme but finite sea level also passed constructor admission. These values can poison a cached drainage waterline even though the callback input is finite.

The Dev175 correction rejects finite-but-float-unrepresentable sea levels at construction and terrain heights at the callback boundary, before float water storage. Ordinary finite terrain within the representable range follows the same numerical path. Added `MountainDrainageFloatStorageChecks` and registered it in `DrainageChecks.run()`.

- Production blob: `cc5c7f82ebc39a04dcf9fc5d4f9894ab5d4b56c1`.
- New regression blob: `e9d7216dd7f7f38c5c656416946fe6b8769d26d6`.
- Registration blob: `d04758be8921d3905ee742756afab6d44e40db37`.
- [View correction commit](https://github.com/Shnewbs/Deep-Primal/commit/0065d516864060f894d80c929a8918e9797f6a65).

## Local verification (limited scope)

- Java21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed **200,016 isolated fixture assertions**, including 100,000 seeded ordinary finite input pairs, boundary admission and invalid input cases.
- A negative-control program reproducing the original float-narrowing expression failed as expected: `Finite terrain produced nonfinite cached water: Infinity` (exit 1).
- New integrated regression source passed strict Java21 syntax compilation against a **compile-only API double**. It was **not run against the complete production `MountainDrainage` class**.
- All three corrected GitHub files were re-fetched at the exact correction commit and verified to contain the intended production guards and regression registration.
- This is not a full Java25 Gradle `coreChecks`, `check`, NeoForge launch, or playable JAR acceptance.

## Compatibility and packaging gate

`gradle.properties` remains `neo_version=26.1.2.122`; root Gradle targets Java25, ModDevGradle 2.0.148, Gradle wrapper 9.2.1 and JarJar packaging. NeoForge 26.3 builds exist and 26.4 is in snapshot development, but neither target was compiled or launched here. The local environment contains Java21 only, no complete private checkout, no Gradle dependency cache and cannot resolve `github.com` for local Git. The GitHub connector was used to inspect and commit source; no GitHub Actions were invoked.

**No fresh playable JAR or JAR SHA256 was produced; no previous audit JAR was deleted.** Renderer backend, saved-world reopen, real river seams, chunk generation timing, Vulkan/OpenGL fallback, LOD and FPS remain unverified. Historical whole-engine baseline remains **5.0/10** (not regraded); scoped float-storage admission is **8.0/10 provisional** for source-level isolated evidence, not runtime proof.

## Next acceptance

Restore Java25 and complete checkout/dependencies, run `./gradlew check coreChecks jar` and packaged JarJar checks, then run the new regression in production and fixed-seed drainage/river-bank continuity tests. Only replace the retained audit JAR after the new packaged JAR passes verification and has a recorded SHA256.
