# Dev.28: reject saturated constituent receivers before block writes

The packaged dev.27 native regression reproduced four failures with valid account
states at the supported salt/sediment limit. Infiltration (separate salt and
sediment controls) and ocean intake removed 1024 water quanta from a cell before
the destination rejected its constituents. Cell-to-cell flow changed both block
volumes before throwing; the old composition then no longer matched either
volume. Retained observations show the resulting lost water/constituents.
These are extreme supported accounting limits, not a measured frequency claim
about ordinary worlds.

The fix exposes WaterAccount's existing admission predicate as `canAccept` and
checks aquifer/ocean receivers before writing a block. Cell flow prepares both
exact parcels through `HydrologyRuntime.prepareTransfer` before either block
write; committed writes publish those prepared parcels. A rejected preparation
returns without changing blocks or accounts. Account ownership stays on the
existing server thread. This is not a new cross-thread transaction protocol or
durable coarse/loaded transfer journal.

Version advances to alpha.29-dev.28 and includes the separately promoted dev.27
aquifer clock fix. Frozen V1–V7, saved profiles, approved art/Themes, water units,
Vulkan/OpenGL modularity and staged R57/R60/R61/R62 status are preserved.

## Qualification

- The packaged baseline records four concrete failed transfers; its clean
  diagnostic shutdown does not make those failures passing behavior.
- The final packaged candidate records seven atomic rejections, four successful
  exact-capacity transfers, no exception and exact water/salt/sediment retention.
  Repeated intake after reaching capacity also rejects without mutation.
- Full Java25/Gradle9.2.1 build/core/module/resource checks pass. Independent
  compilation covers 130 pure-core/fixture inputs; 142 independent core classes
  match the package. Seven archives have valid CRCs; 588 product classes match
  compiler output; 211 private classes are excluded. Themes, all 43 palette
  resources, 11 approved ground textures and 148 generated soil resources match.
- Final candidate native receipts cover default fresh/reopen, channel-5 opt-in
  fresh/reopen with the creation toggle disabled, and three aquifer-persistence
  sessions across two actual restarts at depths 1/64. Explicit pass records,
  replay hashes, source/class identity, save-flush acknowledgments, failure
  precedence and completed ordered shutdowns are required. Alongside the receiver
  regression this is eight final-candidate Linux native sessions.
- Exact-source rebuild/package/delivery identity is recorded separately after
  guarded promotion. A candidate receipt alone is not an uploaded release.

## Retained rejected attempt

The first dev.28 incremental build reported success, and package-vs-compiler
comparisons passed, but the compiler output for WaterRuntime still invoked the
old `transferred` signature. The native positive control exposed NoSuchMethodError;
Gradle exited zero despite the Minecraft crash. This artifact is rejected and was
not uploaded. The crash, harness failure, original class disassembly/hash and
package receipt are retained. A forced nonincremental compilation replaces the
old call with preparation-before-write; the corrected disassembly and subsequent
native passes are checked. The underlying incremental-cache cause is not proven.
No compatibility overload was added to hide the stale class. Final qualification
uses the private strict-compile init script, with no product build default change.

## Coordination, remaining gates

Consumed audit: docs/engine-hourly-audits/2026-10-26T0653-0818658/REVIEW.md,
pinned 081865869425a1ce7d7148d6782ee485218549d1. This addresses its conservation
and persistence weakness within the explicit cases above. Last full external
grade remains 50/100 (5.0/10); there is no new complete regrade.

Foreground claim b9a6206 reserves cliff/waterfall mesh and rendering work in a
separate Windows checkout and explicitly preserves this conservation builder.
Its files, scoped claim and root-version ownership are preserved. The e62c669b
GPU receipt/fixture commit is also retained; those GPU measurements belong to
the earlier 3acffb22 artifact and do not certify this JAR. The owner's cliff
flicker remains open pending foreground qualification.

Host: Linux x86_64 AMD EPYC9V74, nine exposed CPUs, 8GiB cgroup limit. Native tests
use private 1024MiB heaps and isolated localhost-only worlds/cache. These are
functional checks, not frame/tick performance measurements. Windows/AMD8060S
execution of this artifact, current rendered Vulkan/OpenGL scenes, long soak,
multiplayer, complete world water ledger/coarse transfers, seasonal ownership,
V8 saved-profile/topology and full LOD acceptance remain open. Public GitHub
release upload is unavailable in the connected capabilities; Library delivery
must not be described as a GitHub prerelease.

Next: incorporate qualified foreground cliff commits without overwriting them;
continue loaded/unloaded water accounting and bounded persistent ownership,
then V8 saved-profile/topology and measured latency gates. Builder stays enabled.

## Final source build and saved delivery

Committed source `d97cb341159cfbe656fe4cac079dd8f0f870a4c7` rebuilt successfully with all 35 tasks executed. The final 125,290,369-byte JAR is byte-identical to the corrected candidate used by all eight native sessions: SHA256 `9df2a957da1dc1b9c4e8b633798dde0256669bd152a2d40a12f1570296362f40`. Final archive/class/private-fixture checks and transfer disassembly pass. All 844 present non-document engine inputs match their Git blob identities; all five changed source files were additionally fetched back at the pinned commit and compared exactly. Recursive tree refresh returned a transient transport error, so the manifest was reconstructed from the previous fetched tree and exact successful guarded commit payloads, retaining the documented excluded non-engine roots.

[Download dev.28 testing JAR](https://chatgpt.com/library?file_id=libfile_052d5a1e0c2081919f3e1c60829ed5dd&account_id=personal). Save/upload completed and the local SHA was rechecked. No GitHub prerelease was published: the connected capabilities do not offer release-write/asset-upload and no gh executable is available. This is development testing delivery, not stable promotion or Windows/GPU acceptance. See `final-delivery.json`, `final-package.json`, `final-source-identity.json`, `published-source-readback.json` and `final-build.log`.
