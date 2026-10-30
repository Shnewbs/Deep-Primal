# Dev91 — V8 outlet pending-frame recovery, explicit native-witness gate

**Source-only checkpoint:** `0.0.1-alpha.29-dev.91-outlet-reconcile.1` (October 30, 2026 UTC). Scope: `V8OutletLedgerStore`, isolated filesystem tests, this evidence. Consumed independent Dev90 audit `docs/engine-hourly-audits/2026-10-29T2330-655ab648/REVIEW.md` pinned at `655ab648212c3d378d638ea0a99ca1c42f2bf4ec`; its P1 finding is unresolved native/restart reconciliation. Accepted whole-engine baseline remains **50/100 (5.0/10)**, not a new grade.

## Implementation

Added **explicit** `NativeCheckpoint` and `openBoundReconciled` on the existing V2 world/profile-bound durable outlet store. Ordinary `openBound` still fails closed on **any** pending checkpoint. Reconciliation acquires the same bounded single-writer lock, checks both sealed images and identity, requires a next-generation pending frame, verifies that each source's last receipt advances by exactly one (never infers missing intermediate transfers), and recomputes conservation for every existing account and all three measured quantities (water, salt, sediment). Account identities, capacities and receiver edges must remain unchanged.

The caller supplies a separately persisted native **world UUID, profile hash, generation and funded account balances** from the authoritative Minecraft finite-cell/coarse-store owner, never from the proposed outlet image. A matching next-generation witness promotes the exact sealed pending bytes atomically and forces the directory; a matching committed-generation witness quarantines the unused pending bytes under a non-overwriting forensic name. Mismatched, missing, noncanonical, rehashed-but-quantity-forged or ambiguous witnesses reject without mutating the committed image or pending evidence. Reopening the resulting accepted image preserves replay sequences and conservation. Existing V1 checkpoints, V1–V7 geography, V8 snapshots, hydrology amounts and renderer code are not migrated or changed.

**Boundary:** This is an explicit owner-driven *source API*, not a native Minecraft transaction. An arbitrary/misrepresentative `NativeCheckpoint` is not authenticated by this Java helper. Real chunk/loaded/unloaded ownership, crash-atomic native witness storage, journals with multiple receipts from one source, rollback of player edits, and cross-filesystem Windows/Linux durability/latency remain separate unaccepted gates. No claim of real-game exactly-once transfers or complete water engine.

## Reproduction

Run `python3 tools/test_v8_outlet_store.py --output <scratch>` and `python3 -OO tools/test_v8_outlet_store.py --output <separate-scratch>` from a full checkout. Exact normal and optimized Python runners each passed a strict OpenJDK **21.0.12.1** `javac --release 21 -Xlint:all -Werror` compile and `java -ea -Xverify:all` component process with **181** fixture assertions; **14/14** separately compiled broken controls rejected in *each* runner mode. Tests cover valid pending promotion, old-state forensic rejection, replay idempotence, signed coordinates, world/profile binding, mismatch, corrupted/forged sealed frame, changed quantities, CAS, path aliasing and pre-existing persistence. The before-fix dev90 source does not offer the new recovery API. Check `RESULTS.json` and the linked portable source/runner replay for input identities.

Java25, Gradle9.2.1, Minecraft/NeoForge, actual saved-world reopen/edits, Vulkan/OpenGL, native multi-seed visuals, water long-soak, named-hardware bounded performance, and playable JAR/release were **not** performed. The full pinned root work-state validator was unavailable with this partial source checkout; root state was not edited. Owner retains complete builds and publication.

## Next

Implement authoritative native ownership/revision stamping for each loaded/unloaded finite-water account, with a crash-safe world-owned transaction that persists the witness before invoking this opt-in recovery. Certify marine roots from real saved-world topology. In parallel validate V8-r6 vegetation native receipts, reopen and pre-hydrology relief/coastal joins; preserve other scoped workers and requested terrain-blending priority.
