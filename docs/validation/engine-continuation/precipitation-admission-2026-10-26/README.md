# Staged precipitation split admission — 2026-10-26

Base main: `d6d7fc5c0fc317debc31a9b9556eb4e7d2e67eb1` (production dev.28).
Consumed independent report: `docs/engine-hourly-audits/2026-10-26T0653-0818658/REVIEW.md`,
pinned `081865869425a1ce7d7148d6782ee485218549d1`. Its broad conservation/persistence
weakness remains open; this is one narrow valid-state admission repair, not a regrade.

## Reproduction and change

`WaterAccount.distribute` preserves account amounts on constituent-capacity
rejection but throws IllegalStateException. The precipitation caller in
`HydrologyRuntime` does not catch that exception. A valid receiver at the salt
or sediment limit plus a constituent-bearing donor therefore cannot reject
weather delivery normally. This is directly reproduced at the core call, not
a reproduced Minecraft crash or an ordinary-world frequency claim. Default
evaporation supplies pure water to the atmosphere, so these tests deliberately
exercise supported extreme saved/account states rather than typical rain.

The staged implementation returns zero before mutation when either destination
rejects. It retains all three original parcels and preserves the existing
allocation/rounding algorithm. It does not redirect rejected supply, queue a
backlog, remove constituents, create water, or change alias-programming errors.
The existing saturation test expected a throw; its revised contract now requires
zero and unchanged state. Alias rejection remains an IllegalArgumentException.

`SplitAdmissionChecks` uses fixed-seed varied demands for both recipients and
both constituent limits: 40,000 atomic rejections and 40,000 exact-capacity
successes with exact aggregate water/salt/sediment accounting. The baseline
exits 1 with the original exception; the staged candidate passes all 80,000.
The full independent core suite additionally retains existing randomized split,
rounding, bounded capacity, frozen geography and alias checks.

## Promotion remains gated

Only this evidence directory and work state are committed. No production source,
version, owner artwork, frozen geography or renderer file is changed. The
foreground cliff claim reserves gradle.properties and rendering through 10:02Z;
its Windows jobs/cache/output are untouched. Cross-chat context at 09:27Z reports
OpenGL readbacks passed and Vulkan/full build underway; this is reported context,
not a fresh builder GPU run. The auditor's latest completed record is still07:20Z.

No new full Gradle build, packaged native weather/save/reopen check, current GPU
check, benchmark or JAR is claimed for this staged patch. Dev.28 remains the
previous qualified binary and does not contain this change. Next: exercise the
actual precipitation caller with a persisted constituent-bearing atmosphere and
saturated ground/snow stores, require clean save/reopen/shutdown and exact totals,
then reconcile the foreground version and promote with a uniquely versioned full
build. Do not promote this core-only evidence as complete water acceptance.

Last full external grade remains 50/100 (5.0/10). Loaded/unloaded single-owner
transfers, broad seasonal coupling, long soak, multiplayer, current rendered
cliff continuity, V8 native/profile topology and owner hardware latency remain open.

Run `python3 <this-directory>/run_checks.py <jdk25-bin> <new-output-directory>`
from repository root. The runner overlays only the staged files in compilation;
it does not replace production files or use shared Gradle outputs.

## Retained interrupted attempt

The first independent CoreChecks process had no explicit heap ceiling and did
not complete after about five minutes. Its wrapper buffered stdout until exit,
so no completed broad-suite receipt was obtained. A scoped jcmd attachment failed
with `No such process` in this environment; no thread dump or root cause is
claimed. Only that owned test session was interrupted (exit130). A single bounded
retry uses -Xmx768m, a180-second deadline and streaming file diagnostics. See the
retry log/exit receipt; never treat the interrupted run as passed.

Final scope: the focused regression passes80,000 cases. WaterBudgetChecks passes
200,000 transfers,10,000 randomized splits and180,000 weather intervals with186
reconstructions. The broad retry exits124 at its180-second deadline after passing
density/water-bed checks; the complete suite is NOT passed. This is a qualification
blocker, not evidence of a diagnosed product deadlock. Retain both attempts.
