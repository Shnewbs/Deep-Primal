# Dev78 — observed watershed receiver link validation

**Audit consumed:** `docs/engine-hourly-audits/2026-10-29T1132-1f926907/REVIEW.md`, pinned to `1f9269078a9ffa58275dbc04825de8b3802cbf5b`. The independent auditor confirmed Dev77's contradictory-seam rejection; real interior-to-port receivers, native cross-region discharge and finite-water accounting remain open. The last complete independent grade remains 50/100 (5.0/10), carried, not rescored.

## Source implementation

`WatershedRegionPortBridge` now validates each exported downstream parent according to the actual immutable `WatershedRegion` eight-neighbor lattice contract: the receiver must be a signed, 32-block aligned, in-region non-self neighbor. Ocean cells cannot also be inland lakes or have a land receiver. When a receiver is itself observed on that region's perimeter, the bridge adds the **actual direct physical passage**, including diagonal links, with a bounded spill sill and non-increasing planned downstream stage. Already-admitted perimeter paths and Dev77 strict shared-node observation parity remain intact. An interior receiver is not converted into a fictitious shortcut. No cumulative catchment is converted into volume.

## Checks

OpenJDK `21.0.12.1`, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` with isolated API doubles. `python tools/test_watershed_region_port_bridge.py` and `python -OO tools/test_watershed_region_port_bridge.py` both passed: two positive processes each, **3,919 assertions per positive process**, **12/12 compiled mutant controls rejected per runner**. New regressions cover real diagonal perimeter receivers, unobserved interior parents, upstream/downstream stage inversion, far/outside/unaligned/self parents, illegal ocean parents, and lake/ocean contradiction. Existing signed seams, reversal, concurrency and zero-fabricated-runoff cases still pass. Unchanged `tools/test_watershed_outlet_topology.py` passed its isolated Java21 normal run and three negative controls. Recorded source/test blob identities are in `results.json`.

## Unpassed acceptance

This was a **source-only** change. The modified real `WatershedRegion` and complete dependency graph were not compiled together. API doubles were used. No full Java25/Gradle/NeoForge build, Minecraft generation, native save/reopen, live liquid quantity/salt/sediment ledger, GPU Vulkan/OpenGL captures, old/new chunk borders, performance budgets or playable JAR. The full root work-state validator could not run without a complete pinned checkout; the old root work-state was not modified. Owner retains full builds/JAR publication.

Next: authoritative interior parent-path export with one-owner discharge receipts, saved V8 hydrology binding and native integration; continue **existing** V8 ecotone saved-policy surface/vegetation wiring and relief with compatibility gates. This change does not declare terrain blending or water complete.
