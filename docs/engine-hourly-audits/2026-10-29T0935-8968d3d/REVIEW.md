# Deep Primal Dev75 — independent Java25 audit (2026-10-29)

**Pinned source:** `8968d3d007bd1c9e976710c43a2ed1553480793b` (`0.0.1-alpha.29-dev.75-outlet-topology.1`); new implementation committed at `1395e845a4cd7327e409a7de8e162401c1e44583`.
**Report parent immediately before publication:** `8968d3d007bd1c9e976710c43a2ed1553480793b`. The audit is observational and did not modify the Builder or production sources.

## Exact-source independent evidence

- Matched complete `engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java` Git blob `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a` and SHA256 `a912949a188ec7d1ca1d6e102b4f764c771a671da211989c2bad7dc13512fb64` byte-for-byte.
- Restored verified Temurin JDK **25.0.5+8** from owner-provided Library archive into independent audit output; Java21 installation untouched.
- Exact committed production class and independently written oracle compiled with `javac --release 25 -Xlint:all -Werror`, ran `java -ea -Xverify:all` **twice**, each run passing **93,015 assertions** across **1,600 signed-coordinate randomized graphs**.
- Independent all-pairs minimax closure compared spill-stage, reachability and status; verified ordered stable receiver chains, cycle freedom, unknown/closed frontiers, planned runoff totals, reversed-input parity, guards and negative coordinates.
- Three separately compiled deliberately broken variants were **rejected**: missing intermediate-floor barrier; duplicated downstream runoff; discarded unknown-frontier flag.
- Builder Dev75 report separately claims Java21 18,111 positive assertions and 3 rejected mutants (inspected, **not rerun**). New Java25 assertions are independent and do not substitute for native V8 acceptance.

## Findings and boundaries

- New helper is bounded (`MAX_PORTS=16384`, `MAX_LINKS=65536`) and implements deterministic minimax/receiver planning. Production evidence: [`WatershedOutletTopology.java` L15–31](https://github.com/Shnewbs/Deep-Primal/blob/8968d3d007bd1c9e976710c43a2ed1553480793b/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java#L15-L31), [L112–215](https://github.com/Shnewbs/Deep-Primal/blob/8968d3d007bd1c9e976710c43a2ed1553480793b/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java#L112-L215).
- **Not wired to native V8 or `WatershedRegion` yet**. Authoritative verified ocean anchors, complete regional port sets, saved routing provenance and real finite-water quantity/salt/sediment state remain open. Planned runoff tree equality is not conservation of live water, world edits or native saves. Dev75 checkpoint README confirms no gameplay activation.
- Root `docs/ENGINE-WORK-STATE.json` remains at Dev53; Dev75's scoped `docs/TERRAIN-BLENDING-WORK-STATE.json` marks its worker checkpointed and lease released. Staged R57/R60/R61 are not promoted. No evidence of new native compatibility or GPU acceptance.

## Ten-category grade (last accepted complete baseline, **not** a fresh regrade)

| Area | /10 |
|---|---:|
| Terrain realism and variety | 5 |
| Biome blending and coherence | 4 |
| Hydrology and cave quality | 6 |
| Features and structures | 4 |
| Performance and threading | 3 |
| Determinism and stability | 7 |
| Architecture and code quality | 6 |
| Extensibility and data-driven design | 4 |
| Testing, benchmarks and documentation | 7 |
| Polish, config and tooling | 4 |
| **Overall, Tier 2 (carried)** | **50/100 = 5.0/10** |

Original comprehensive rubric: `docs/engine-review-2026-10-24/REVIEW.md` at reviewed source `f141c0b4d058abb311cb088e4f17a575685a2939`. Unintegrated/new helper earns no gameplay points. **Stop threshold strictly greater than 8.5/10 remains unmet.**

## Not run / next actions

October 27 owner override places full Gradle/Minecraft/GPU/JAR builds with owner. Local Git CLI DNS cannot resolve GitHub, so no full independent checkout or Gradle wrapper; **no JAR built or released**. Did not run outer/nested JAR CRC, source-to-package identity, Windows AMD8060S Vulkan/OpenGL, real native save/reopen/shutdown, mixed-gen border, finite water/constituent lifecycle, GPU bench or multiplayer soak. No shared heavy benchmark ran.

Next: integrate real `WatershedRegion` signed ports and authenticated ocean anchors into saved V8 policy; accept cross-region outlet/lake discharge and incomplete frontiers; verify finite water/chemistry through native unload/restart; owner to perform full Java25 Gradle, packaged archive and Windows Vulkan/OpenGL acceptance before any regrade/release.

**Retained independent ZIP:** `DeepPrimal_Dev75_IndependentAudit_20261029T0935_8968d3d_Java25Evidence.zip`, 13,674 bytes, SHA256 `241e8b77970a38b21e1096121a8b4baa9baed9d9cc1112e0ff14d7b0094af212`; 17 entries with outer CRC and SHA256 member manifest verified; saved to Library `/Deep Primal/Independent Audits/`.
