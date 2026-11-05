# Deep Primal dev136 — signed ViewGrid ring boundaries (2026-10-31)

## Scope and defect
- Baseline main: `f9d70f5011bc98ea8e865272669c4e185af66f35` (Dev135 + independent audit).
- Original `ViewGrid.coordinate` uses signed 32-bit subtraction for `index-low` and `radius*2+1`. For `camera=Integer.MIN_VALUE+1, radius=1, index=0`, the complete window fits in `int` but the slot calculation overflows and maps to the wrong section.
- New code uses signed 64-bit intermediates and rejects negative radii or windows outside signed section coordinates, instead of wrapping to unrelated world positions.
- The ordinary bounded view-radius path and signed ring identity are preserved. No changes to the mixin, saved-world layout, Vulkan, LOD workers, water, terrain or approved assets.

## Local isolated validation
```sh
javac --release 21 -Xlint:all -Werror -d classes ViewGrid.java ViewGridBoundaryChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.core.ViewGridBoundaryChecks
```
- **PASS**: `VIEW_GRID_BOUNDARY_PASS checks=293252`, seeded 120,000 randomized cases, exact `BigInteger` oracle, signed-int edge cases, slot identity, bounded ring coverage.
- **Negative controls**: original implementation rejected at assertion 1 (valid signed-window overflow); removed-boundary-guard variant rejected at the first out-of-range window.
- Production source Git blob: `ee68f2d546f3624d7a10ec299209c8b27cdaef35`; SHA-256: `a02a461a4df030dfefefb5882c6fcb2a3076c3b4cf40a04500e805017528a0d7`.
- Test source Git blob: `7f29e7ae445735ab13f9b9837a25bc2f223e3e00`; SHA-256: `9f11d8228ded3e8dbda6e58784eae9ea676f57732aeb799faf63c3e37aeaf896`.
- `CoreChecks` registration added so full source builds execute this regression.

## Limitations and next gates
This is an isolated Java 21 source test, not a full NeoForge acceptance. The local environment lacks the complete private checkout, Java 25 and cached Gradle/NeoForge dependencies; Git CLI DNS cannot reach GitHub. No native `ViewAreaMixin` acceptance, JAR, JAR SHA-256, Vulkan/LOD benchmark, finite-water save/reopen or V8 durable publication was established. Preserve any previously verified JAR. GitHub Actions were not used. Independent whole-engine grade remains the historical **5.0/10**, not regraded here.
