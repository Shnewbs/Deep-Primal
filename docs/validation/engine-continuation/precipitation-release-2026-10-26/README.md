# Dev.29 precipitation receiver admission

This continuation addresses the native precipitation crash independently reproduced by
`docs/engine-hourly-audits/2026-10-26T1033-4a583db/REVIEW.md` at source
`4a583dbcdec4df4be1ac7572c384813986067f2a`.

`WaterAccount.distribute` now treats a full constituent receiver as an ordinary
admission rejection: it returns zero before mutating any of the three accounts.
The donor retains the undelivered water, salt and sediment. It does not reroute
the rejected share, create a delivery backlog, weaken invalid-account checks,
change geography or change cosmetic surface height. The sole production caller
is the existing native precipitation path.

The 80,000-case split regression is now part of the normal CoreChecks entrypoint.
The private native fixture is available through visualValidation, excluded from
the shipped JAR, and requires an explicit fresh/reopen mode. It exercises the
actual precipitation caller with a nonzero-demand positive control, repeated
capacity rejection, exact parcel persistence and no forced remote aquifer load.
It is only for disposable test worlds. Constituent-bearing atmosphere plus
saturated receiving stores is an extreme supported saved/account state; this
report makes no frequency claim about ordinary rainfall.

## Reconciliation and qualification

The candidate retains the committed cliff-wall and shared-history repairs from
`6cd99717785d26d3f107d99b827f0379ad736795`. While this run was active, the separate
renderer task committed LOD part bounds at
`7772c36daf3fd6cd6a90bea95484272eaf1c603f`. Those files are preserved verbatim.
An initial full build and six native sessions preceded that integration; their
receipts remain intermediate. A second full build and fresh packaged/native
gates qualify the combined source. Final producer/source identity is recorded in
the delivery receipt; do not use an intermediate archive hash as the final JAR.

The first initial precipitation attempt reached its pass marker and normal
shutdown but was rejected because its hard-linked export changed when Gradle
rebuilt producer inputs during startup. Both the failure and its successful
fresh-world retry are retained. Final archive/source/class checks must pass
after all producers finish; a Gradle zero exit alone is never acceptance.

## Scope and open gates

Host: Linux x86_64 AMD EPYC9V74 virtual host, nine exposed CPUs and an 8 GiB
memory limit. Windows Vulkan payload and OpenGL fallback packaging are checked;
Windows/AMD8060S execution, real client GPU/pixel tests, natural moving waterfalls,
multiplayer/long-soak, controlled tick/frame/I/O measurements and whole-world
water acceptance are not established by these checks. Prior foreground GPU
receipts are not rerun or relabelled as this build's visual acceptance.

No score is awarded by this builder. Last full accepted external grade remains
50/100 (5.0/10). The consumed auditor's scoped provisional 48/100 (4.8/10) is
retained until the independent auditor reassesses it. Neither the >8.5 threshold
nor the separate water acceptance gates are complete.

Next engine priorities remain bounded loaded/unloaded ownership and conserved
restart/edit/catch-up cases, followed by V8 saved-profile/topology integration.
Natural water temporal/pixel and named-hardware tail acceptance remain open;
the separate rendering task retains its own scoped work. R57/R60/R61/R62 stay
staged, frozen V1-V7 and approved art are unchanged.

## Completed combined gates

Full forced Java25/Gradle build: 35 tasks, complete CoreChecks and both module boundaries pass. Independent javac: 132 sources. Package: seven CRC/duplicate checks, 588 product classes, 142 independent core classes, 217 private exclusions. Packaged water/cliff/LOD regressions pass. Ten Linux native sessions pass: precipitation fresh/reopen, default and channel5 fresh/reopen, aquifer fresh/reopen/verify and receiver capacity. All require explicit records, acknowledged flush and clean shutdown; final bytes remain unchanged through those sessions.

GitHub prerelease upload is unavailable in this environment (no release/upload action or gh executable). Only a qualified final file may be saved and linked; no GitHub release is claimed.
