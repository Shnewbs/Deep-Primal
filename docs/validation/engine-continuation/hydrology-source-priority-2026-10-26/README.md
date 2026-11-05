# Staged source registration priority

Production remains alpha.29-dev.31 at a1583a508b0d67130d387bc018defd5758acd496. No new JAR or production promotion. This supersedes only the staged source-index candidate from e528d066d8202ac1a2822de700099a34ce67d470.

Consumed independent audit: `docs/engine-hourly-audits/2026-10-26T1934-e528d06/REVIEW.md`, pinned e528d066d8202ac1a2822de700099a34ce67d470. Grade remains carried 50/100 (5.0/10), not a fresh complete regrade. The audit requires tie-order and actual native lifecycle qualification before promotion.

## Repair

The previous staged index immediately discarded a dry source's registration order. If that source returned before the next source pass, it moved behind equal-height sources. Production's LinkedHashMap registration survived until cleanup after survey, so its priority did not change in that case. Since sources share finite regional quotas, order is observable behavior.

The revised index deactivates dry entries in the ordered set but retains a dormant registration marker. A constant-time `beginPass` changes the registration-window identity after survey, at the original cleanup boundary. Re-registration within the same window keeps priority; re-registration after that boundary gets a new sequence. No whole-index cleanup, copy or sort is introduced. Explicit unload and missing-FULL removal release both active and dormant entries; stale-cursor identity protection is retained.

Dormant metadata stays bounded by previously registered, still-loaded chunk keys, not source iterations or lifetime exploration. The window token avoids numeric epoch wrap and costs one object per source pass; retired tokens remain reachable only through dormant loaded entries. This is an algorithmic bound, not measured heap/frame/tick acceptance. Iterators remain server-owned and fail-fast; actual callback reentrancy still requires native qualification.

## Checks

- Previous exact index plus two compatibility methods (`deactivate` delegates to its old `remove`; `beginPass` is a no-op) fails the same-window witness. Candidate passes 100,000 randomized lifecycle transitions / 100,003 comparisons against an independent LinkedHashMap stable-sort/phase-cleanup model, including registration visibility and full dormant release on unload.
- Complete previous staged runtime also reproduces the failure through its actual refreshSource method, without those compatibility methods. Revised actual runtime passes 1,000 same-window/next-pass scenarios. These are JVM reflection fixtures, not Minecraft worlds.
- Complete candidate runtime API compilation passes against retained Minecraft/NeoForge dependencies. Existing 5,512 runtime lifecycle checks, 20,000 index mutations / 20,008 checks, 144 extracted regular-visit loop cases and 1,445 actual-runtime allowance cases pass. Baseline failures are negative controls and retained separately.
- Source/class identities are in `runtime-results.json`; additional fixture source identities are recorded separately. The complete Git comparison from a1583a5 to read-main cbc9ed5 contains 13 commits/236 changed files and no non-documentation changes. Current core inputs are verified against the retained qualified manifest before and after fresh compilation/execution.

Fresh complete core qualification passed: 138 current core/fixture sources compiled, full CoreChecks exited zero in about292seconds with a512MiB heap, and all138 source hashes were rechecked afterward. This is a fresh core pass, not a full mod build. See fresh-core-result.json and core-run.txt. Five complete physical method bodies also match production exactly; this alone does not prove world conservation.

Full Java25/Gradle/module/package/native qualification remains unavailable: overlay free space is zero; shared-memory free capacity fluctuated from626MiB to267MiB during functional tests. No new mod archive, CRC/native-payload/private-fixture/package-identity gate, real OpenGL/Vulkan scene, Windows/AMD8060S test, latency benchmark or long soak is claimed. No fresh release or Library JAR was created or old binary relabeled. Full core results are recorded separately and do not replace full mod/native qualification.

## Coordination and next gates

Read current main/policies/work states/original rubric and latest completed audit. Root/water claims were checkpointed. Auditor completed at19:43UTC. Renderer has an active functional-only claim under docs/renderer-jams/20261026-candidate-runner; this work does not touch it, its outputs or caches. Personal Context did not establish fresh direct ownership evidence from the originally named other-engine conversation. This builder uses its bounded root lease, unique memory-backed outputs and read-only retained builder dependencies; no exclusive timing reservation.

Restore independent full-build capacity; qualify the combined staged index in full Gradle/module/package and actual native quota, partial-survey, callback, load/unload, weather/save/reopen/shutdown cases before promotion and version bump. Then continue persistent single-owner water transfers, bounded catch-up, real water/LOD scenes and registered V8 acceptance. Frozen geography, profiles/player edits, approved art, production quantities and R57/R60/R61/R62 staging remain unchanged. Both independent >8.5 grade and water acceptance remain incomplete.

The concurrent renderer checkpoint3947797 changed only its own candidate-runner documentation/tooling path and is preserved in the publication parent. No test failure occurred for the corrected candidate; baseline failures are intentional controls.
