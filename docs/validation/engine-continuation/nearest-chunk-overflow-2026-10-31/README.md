# Dev128 — overflow-safe nearest-chunk selection

Pinned source parent: `a2bae894deae4f5b35018c1be535ddccd66684ae`. Latest completed independent audit consumed: `docs/engine-hourly-audits/2026-10-31T1134-09f5eeb9/REVIEW.md` (Dev127 mutation runner correction; no full regrade).

## Defect
`NearestChunks.offer` previously calculated `dx*dx + dz*dz` in a signed Java `long`. A delta between two arbitrary signed 32-bit chunk coordinates can reach `2^32-1`; the squared-distance sum can require 65 unsigned bits. Overflow could make a very distant queued chunk appear nearer than a legitimate nearby chunk. This is a defensive priority/culling correction for malformed/extreme coordinates, not a claim that ordinary configured view distances exhibit the bug.

## Repair
Keep the primitive heap and stable packed-position tie break, but represent the exact squared sum as unsigned low 64 bits plus a one-bit carry. Compare the carry first, then `Long.compareUnsigned` for the low word. Swap the carry with each heap element. No `BigInteger` allocations occur in production; `BigInteger` is used only by the differential test oracle.

Add `NearestChunksOverflowChecks` to the existing `CoreChecks` entry point. Existing `RuntimeChecks` nearest-selection cases are preserved. No water, terrain-profile, renderer, saved-world or texture changes.

## Executed validation
- OpenJDK 21.0.12.1 isolated compile: `javac --release 21 -Xlint:all -Werror` — pass.
- Exact 65-bit `BigInteger` oracle: `java -ea -Xverify:all` — **56,707 assertions passed**, including both signed extremes and 2,000 deterministic randomized differential trials.
- Compiled negative control replacing the carry calculation with zero — failed at assertion 7, as expected.
- These results cover the standalone class and new regression test. The full repository `CoreChecks` task was **not** executed.

## Acceptance limits
Git CLI DNS to github.com unavailable; no complete local checkout or Java25 toolchain, Gradle/NeoForge, native Minecraft save/reopen, GPU or performance benchmark. No verified replacement JAR; retain the prior successful owner build. Independent baseline remains 50/100 (5.0/10), not freshly regraded.

## Coordination
Current main and the Dev127 audit were checked; the prior terrain lease is released, and the water/upscaling recorded scopes are distinct. This change is confined to primitive nearest-chunk selection, its CoreChecks regression, and this evidence. No GitHub Actions used.
