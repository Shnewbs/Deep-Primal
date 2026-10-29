# Renderer jam 40 — scoped two-back frustum A/B option

**Production source:** [40553d0b](https://github.com/Shnewbs/Deep-Primal/commit/40553d0b69a2928fb2961cb433c91c96b1215fa7). At startup, `-Ddeepprimal.lod.twoBackFrustumReuse=false` disables two-back aliases and their publication comparisons while retaining neighboring/whole-page culling. Default is **true**. `lodTwoBackFrustumReuseEnabled` reports effective status, including coarse broadphase-off.

[Read-only author self-audit](REVIEW.md), [machine ledger](LEDGER.json). Strict Java21 source tests passed **21,823,712 assertions**, including four committed test modes; forced-enabled negative mutant rejected. Paired isolated CPU page-publication measurements favored two-back OFF, but it also performs extra fine-frustum tests: actual GPU/frame break-even is unknown. Full Java25/Gradle/GPU/JAR remains owner-operated. Evidence ZIP uploaded and verified in Library /Deep Primal/Renderer Jams. Claim released; other workstreams unchanged.
