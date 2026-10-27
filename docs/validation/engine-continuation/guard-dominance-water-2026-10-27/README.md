# dev38 — Guard entry-dominance and finite transfer callback admission

- **Production source:** [816aab2b2150ff5463253f2d5636d41970914740](https://github.com/Shnewbs/Deep-Primal/commit/816aab2b2150ff5463253f2d5636d41970914740)
- **Version:** `0.0.1-alpha.29-dev.38-guarddominance.1`
- **Consumed latest independent finding:** [21:31 UTC audit](../../../engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md), source `d62030f4b6a2a13da0d09a83a278ece2aa49b8ea`. The validator accepted unreachable `if(false)`, `else`, and contradictory guards. Production guards were not found unsafe.
- **Previously accepted complete engine grade:** 50/100 = 5.0/10, **carried only**, not a new grade. Strict >8.5 and complete water acceptance remain open.

## Source changes

1. `tools/validate_hydrology_height_admission.py` now fails closed unless the known placement guard is the first method statement and the native read is inside the same short-circuited condition, or the water-column guard is the first statement of its directly entered bounded scan. This is a source-specific control-path contract, **not a full Java AST/CFG analyzer**. It rejects a new outer dead `if` / dead `else` / contradictory prerequisite, while preserving the nine earlier operator/comment/string controls. Review the gate when production method structure changes.
2. `src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java` now checks loaded/source identity **after** the first native source write; a synchronous callback cannot silently replace that source before receiver commit. It revalidates the original loaded receiver before overwriting it, and only rolls back a source still equal to the state this transfer authored. If source ownership is lost it throws rather than clobbering a newer edit or falsely recording conserved rollback. No new quantity, constituent or geographical allocation is introduced.
3. `gradle.properties` increments development version to dev38.

## Scoped verification — 2026-10-27 UTC

- Isolated Python3 validator on source-signature stand-in from earlier dev37 evidence ZIP: `WATER_HEIGHT_ADMISSION_STATIC_PASS checks=29 mutationsRejected=17`; `python3 -m py_compile` pass. That stand-in has matching method admission structure but is **not the full current NeoForge class**. Eight added dead-branch/contradiction controls pass. A prior permissive checker reproduced three unsafe false positives in the independent audit.
- Reproducible isolated fixture: [WaterReceiverCommitModel.java](WaterReceiverCommitModel.java). A deterministic Java21 model for the receiver/source callback branch passed **274,989 assertions**, command `javac --release 21 -Xlint:all WaterReceiverCommitModel.java && java -Xverify:all WaterReceiverCommitModel`. A deliberately incomplete candidate failed the source-edit case; the subsequently corrected model passed. Checked refused receiver, reentrant receiver replacement, unloaded receiver, source replacement detection, normal transfer, stale quantity, and randomized accounting/commit-once controls. **This is not a compiled production method or Minecraft test**, and chemistry persistence is not proven.
- Pinned post-push GitHub read verified the final source blobs: validator `8fb090bd7c8e8b8988ed4783feba13d9914aeb1b`, water runtime `f1a073024f16b3219df0d030d2cc5b235126edfb`, version as above.
- Tool host: Linux shell, Python3, Java/Javac **21.0.12.1**. The owner-provided JDK25 archive and restored directory were absent in this isolated session; shell GitHub DNS also failed. Connector-backed guarded main commits succeeded. Owner handles full builds as of October 27.
- **Not run:** Java25 Gradle/core/module compile, NeoForge, outer/nested JAR checks, native loaded/unloaded callbacks, exact salt/sediment save/reopen, multi-user soak, real Vulkan/OpenGL waterfall pixels, AMD8060S or tick/frame p50/p95/p99. **No JAR, release or acceptance regrade.**

## Next engine priority

Owner native build and event-injection for source/receiver callback changes; enforce loaded/unloaded transfer ownership and exact volume/salt/sediment conservation across rejected writes, save/reopen and edits. Then controlled rock-contact waterfall pixel continuity on both actual backends and performance tails. Keep unrelated renderer state, delivered V1–V7 geography, approved art, profiles and owner edits untouched; do not promote staged R57/R60/R61/R62 overlays.
