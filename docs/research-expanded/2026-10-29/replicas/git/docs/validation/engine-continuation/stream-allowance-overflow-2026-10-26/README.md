# Expanded replica — docs/validation/engine-continuation/stream-allowance-overflow-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/stream-allowance-overflow-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/stream-allowance-overflow-2026-10-26/README.md |
| Preserved source SHA-256 | `a7c1d9cf094dfe305b1aa86eec0838eced039c7e19d2b0befeb890ebc3ddffbf` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged streamAllowance repair for subtraction overflow between ordered signed ticks, kept separate from the existing recharge-starvation candidate. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the source-only arithmetic evidence was not full native qualification and must be combined with reservation behavior without changing finite quantities. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a persisted elapsed-tick allowance contract with explicit ordered-time, saturation and maximum-admission semantics. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Exercise extreme signed tick pairs and ordinary intervals against a wide-arithmetic oracle, then qualify the combined reservation and allowance code through packaged quota, weather and restart cases. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Staged stream allowance overflow repair — 2026-10-26

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Staged stream allowance overflow repair — 2026-10-26

Production is unchanged. No new JAR, full Gradle build, Minecraft server session,
save/reopen, GPU result or grade is claimed. This candidate remains staged until
the existing full/module/package/native qualification gates pass.

## Source and repair

Pinned main: `431c9ecdcea9cacfad3dd5c4d11931436c485ebf` (production HydrologyRuntime
unchanged from qualified source `293516cdb0c62d66ae12a5146e4ac2e6590c45a3`).
Consumed audit: `docs/engine-hourly-audits/2026-10-26T1533-6bd85c6/REVIEW.md`,
audit pin `6bd85c6647a9b9f256df5ae2103589bbc8347161`; all scores carried 50/100.
Its recharge-starvation finding remains open in production.

The exact runtime `streamAllowance` method subtracts two ordered signed ticks.
If their mathematical distance exceeds Long.MAX_VALUE, subtraction becomes
negative and can produce negative or otherwise incorrect source allowances.
For quota1024/start=Long.MIN_VALUE+1/now=Long.MAX_VALUE, production returns -51
instead of the completed interval's1024. This is an extreme saved-clock arithmetic
edge case, not a claim that ordinary-duration worlds reach it or that water is lost.

`HydrologyRuntime.java` saturates negative elapsed subtraction to phase19 only
after the existing sentinel/rewind guards. The 20-tick ramp, rewind semantics,
headwater order, quota, finite debit and geography are otherwise untouched.
`combined-HydrologyRuntime.java` additionally contains the prior staged500us
regular-admission reservation. Its SHA256 is in source-identity.json. The original
reservation remains separately preserved; neither staged variant is enabled.

## Fresh evidence

- Exact method extraction against independent BigInteger arithmetic:105148cases
  per normal/-O/-OO Python run. Production disagrees12687times; candidate passes.
- Entire baseline, fixed and combined HydrologyRuntime classes compile against
  retained Minecraft/NeoForge APIs using Java25 and binary-only classpaths.
- The private reflection fixture executes the **actual API-compiled runtime**:
  baseline fails with an allowance mismatch; fixed and combined each pass1445cases.
  This is isolated JVM class execution, not a running Minecraft server.
- The staged server fixture calls the same allowance check before its existing
  quota/dam/drought/recharge tests. Both private fixture classes API-compile;
  that server integration has **not been executed**. Seven classes per variant.
- The combined candidate retains3360boundary and1000multi-page scheduler cases
  per variant. The deterministic100tick reproduction remains0regular visits for
  the unreserved path versus500 for the combined candidate. It does not establish
  hard timing or general fairness: a single callback can exhaust reserved time.

Initial test setup failures are retained: the first chosen negative-value witness
(-20 to MAX, quota1024) aliased to the expected result after overflow multiplication;
the corrected witness uses MIN+1 to MAX. The first API wrapper looked for `-cp`
in a retained command that uses `-classpath`; corrected before compilation. These
were fixture/setup errors, not hidden engine passes. The final wrappers use
explicit return-code/marker checks, not removable Python assertions.

Reproduce: run `prepare.py --reservation-source <prior staged-HydrologyRuntime.java>`,
then `check_allowance.py --java-home <jdk25> --output <private output>`.
Run `compile_runtime.py --java-home <jdk25> --classpath <binary MC/NeoForge/mod/core classpath>`
for actual-class negative/fixed/combined controls. Compile with empty sourcepath
and implicit compilation disabled. For scheduling, use the committed strict probe
under hydrology-recharge-reservation-2026-10-26 against this allowance-fixed source
and the existing stream-deadline-release reproduction fixture. Receipt source pins
explicitly include the local staged allowance overlay; they are not production SHAs.

## Coordination, blockers and continuation

Renderer scratch ownership was active at start; no heavy build/native/benchmark
job was launched. Its timing reservation changed to completed/nonexclusive in
`535a504e5d8fafdef1e210db9c6bbdb247cb2b08`; its scoped writer claim remains active.
Only tiny deterministic/targeted compilation checks ran here. No renderer files,
caches, outputs or processes were touched. Personal-context lookup did not provide
fresh ownership proof for the specified foreground engine conversation; actual Git
claims and auditor recorded completion were checked instead. Auditor latest recorded
run completed15:48UTC with no new JAR and carried5.0; this is not a worker-liveness claim.

Disk began near1.6GB free, fell to154MB, and fluctuated during the run. An exact
duplicate builder dev30 producer was hash-matched to the retained qualified export
and removed, recovering125MB at that instant. It subsequently reappeared under a
different inode without an observed local builder producer; no cause/owner is
inferred. Stop using that mutable checkout for delivery until ownership is resolved.
No further cleanup was attempted there. Keep the qualified export/Library file and
all source/evidence. The cleanup receipt reports point-in-time observations only.

Next: obtain sufficient private build space and restore current main into a genuinely
isolated checkout/cache/output, preserving new renderer commits. Bump beyond current
production, qualify combined reservation+overflow source with full Java25 Gradle/core/
module checks and explicit packaged quota/recharge/weather/save/reopen/shutdown,
archive/native/resource/class identities and final producer byte equality before
promotion. Add the new private fixture to package-exclusion checks. Preserve all
water visual/world-ledger/loaded-unloaded/multiplayer/soak/named-hardware gates and
V8 integration priorities. No Windows/AMD8060S or OpenGL/Vulkan acceptance follows.
Last complete external grade5.0/10 unchanged; builder remains enabled.


<!-- END PRESERVED SOURCE -->
