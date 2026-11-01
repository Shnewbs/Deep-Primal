# Deep Primal Dev147 — independent audit and raw snapshot direction repair

Date: 2026-11-01 UTC. Parent main: `b2687daa6ac405cde9769eee79a9caeaa47043a0`. Scope: independent water snapshot admission; no overlap with released Dev147 flow-transition writer.

## Reviewed new commits

- `19af0a25`: scoped claim only.
- `4fdcc42d`: validate transition initial/update direction IDs 0..16.
- `483f6fbb`: negative-control evidence and raw-snapshot gap.
- `b2687daa`: release writer claim.

## Confirmed defect

`WaterPatchMesher.Snapshot.direction` previously exposed the unsigned captured flow byte directly. Values 17..255 are outside the defined 0..16 material direction domain, so malformed capture bytes could escape the snapshot boundary even though the transition history now rejects invalid IDs. Original isolated regression fails at byte 17, assertion 35.

## Correction and verification

Normalize raw snapshot values greater than 16 to still-water ID 16 without modifying captured byte arrays. Preserve 0..16 and null-flow legacy semantics. Register `WaterSnapshotDirectionChecks` in `CoreChecks`.

- Independent Java21 strict compile (`--release 21 -Xlint:all,-cast -Werror`) and `-ea -Xverify:all` pass with a minimal API-equivalent Snapshot stub: 20,513 assertions, all 256 byte values and 10,000 randomized cases.
- Unmodified Snapshot method fails at raw byte 17. Production `WaterFlowTransition` was independently recompiled from an exact SHA256-matching source snapshot (`579f745955ed5fcf34de2a6c10a32264f1d302ac121da7d66405402462f8f661`) and passed 75,769 independent assertions across 15,000 randomized transitions.
- This is a **source-only isolated** validation; it does not qualify as a complete compiled engine or native GPU test.

## Build/compatibility and release gates

Repository metadata still pins `neo_version=26.1.2.122`, `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`; mod metadata requires Minecraft `[26.1.2]` and NeoForge `[26.1.2.122,26.1.3)`. Gradle wrapper is 9.2.1; Java toolchain is 25. The local environment provides Java21 only, no complete checkout, no Gradle distribution/cache, no GitHub DNS via Git CLI, and no GPU test target. Therefore **no full build, playable JAR, JAR SHA, or native performance grade**. No GitHub Actions used; no prior JAR deleted.

## Provisional grades

- Scoped transition admission: 8.5/10 (isolated evidence).
- Scoped raw snapshot admission: 8.0/10 (isolated source/API checks; full compile pending).
- Whole engine: 5.0/10 **historical baseline**, not a fresh whole-engine score.

## Follow-up

Full Java25 Gradle/NeoForge build and client launch; GPU corner material/LOD validation; finite-water volume conservation and V8 reopen; terrain seam benchmarks; recheck raw renderer inputs against the normalized Snapshot API.
