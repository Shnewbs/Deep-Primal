# Dev68 — independent Java25 source audit (2026-10-28 19:28 PDT)

Audited **committed main snapshot 2c6728576daadeee45fba8ccee84336167d241c2**. Main before guarded publication: `2c6728576daadeee45fba8ccee84336167d241c2`. This independent auditor did not change production source, Builder/renderer checkouts, leases, or processes. The source-only Dev68 producer is not a native/packaged release.

## Independent evidence

Owner-supplied Eclipse Temurin 25.0.5+8 restored in an **isolated** Linux x86_64 audit folder. `javac 25.0.5 --release 21 -Xlint:all -Werror` compiled 62 exact production/API-double/test sources into 140 class files. Seven production/test Git blob identities matched pinned GitHub content. Focused checks: V8 routing 4,798; hydrology binding 45,895; saved snapshot 594,901; saved climate 16,138; surface policy 5,235; V8 prewrite 26,128; renderer pre-draw 37,092; renderer submission 29,276; **759,463 total passing assertions**. Three separately compiled deliberate mutations were rejected. Full 18-suite replay was attempted but timed out, so not counted as independently passed. API doubles do **not** execute real `WatershedRegion` or Minecraft/GPU.

**New independent region-seam witness:** Exact committed `V8RoutedWaterSource` plus API-double `WatershedRegion` classified three signed-side positions x=8190,8192,8194 (z=2048) as OCEAN in an otherwise enclosed low basin ringed by ground height 120. See [route seed lines 78–99](https://github.com/Shnewbs/Deep-Primal/blob/2c6728576daadeee45fba8ccee84336167d241c2/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java#L78-L99) and [ocean query 124–132](https://github.com/Shnewbs/Deep-Primal/blob/2c6728576daadeee45fba8ccee84336167d241c2/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java#L124-L132); production `WatershedRegion` also seeds sea from regional edges [lines 276–303](https://github.com/Shnewbs/Deep-Primal/blob/2c6728576daadeee45fba8ccee84336167d241c2/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L276-L303). This **reproduces a planning risk with an API double**, not a native in-game failure. Fix authoritative cross-region ocean connectivity/outlets with a saved revision and run actual watershed/native seam acceptance before activating V8.

## Equal-weight ten-category grading

**Carried last complete accepted scores** (not a fresh whole-engine regrade): Terrain 5, Biomes 4, Hydrology/caves 6, Features 4, Performance/threading 3, Determinism 7, Architecture 6, Extensibility 4, Testing/docs 7, Polish/tooling 4. **50/100 = 5.0/10, Tier 2**, below target **strictly >8.5/10**.

## Unrun / delivery

No fresh development JAR: owner October 27 override assigns full Gradle/Minecraft/GPU builds to owner; the complete repository could not be cloned because `github.com` DNS failed in this isolated runtime. Full Java25 Gradle9.2.1/NeoForge, actual Mojang codecs, real watershed, native save/reopen/edits/constituents, Windows AMD8060S Vulkan/OpenGL, installer nested CRC/class identity, multiplayer soak and GPU/frame-time gates **not run**. Therefore no JAR was uploaded, released or used for the three-JAR retention policy. The root work-state is still Dev53; scoped Dev68 terrain work-state documents the current source-only checkpoint. Builder remains independent.

Full CRC-verified local and Library evidence: `/Deep Primal/Independent Audits/DeepPrimal_Dev68_IndependentAudit_20261028T1928_2c672857_Java25Evidence.zip`, 138,567 bytes, SHA256 `fdcec67ca0f44c735449338efcf47959918f111ac7a426ab51d21308de2abdcd`. It includes original source archives, independent boundary witness, strict Java25 logs, blob hashes and receipt.
