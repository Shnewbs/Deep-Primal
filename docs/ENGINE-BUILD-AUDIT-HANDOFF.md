# Deep Primal build/audit continuation

The owner requested repeated implementation, audit and rebuild work on the independent terrain, water, graphics and LOD engine, with hourly evidence-based updates. This is a long-running engineering task; an arbitrary claim of perfection is not its completion criterion.

## Work coordination

Read `ENGINE-WORK-STATE.json`, this record, `ACTIVE-ENGINE-AUDIT.md` and the current source branch before continuing. If a foreground lease is current, report progress only. Do not edit the foreground checkout, start competing Gradle/native processes, or claim an idle foreground worker is doing background development. If the source advances during a run, preserve work on a separate branch and report the conflict; never overwrite another worker's changes.

When the foreground lease has ended or the work state explicitly permits continuation, take one concrete highest-priority unfinished item through reproduction, implementation, focused checks and a reviewable checkpoint. Work in an isolated checkout/directory. Use a normal authenticated Git checkout when available; the connected GitHub API supports private source reads and guarded tree/commit/ref writes when CLI authentication is unavailable. Do not reconstruct or expose credentials. Do not spawn sub-agents.

Checkpoint meaningful source, regression fixtures and compact evidence on the working branch. Use the latest tree and expected parent/ref, preserving unrelated files and the owner's documentation/catalog work. Do not publish an unvalidated installer or merge a failing experiment into the stable branch. Report exactly what ran, what failed, what changed and what remains.

## Current acceptance and evidence

Alpha.24 development installers have now been supplied. V3 geography is frozen; subsequent geographic changes need a new saved revision. Continue from the working ref in ENGINE-WORK-STATE.json. See `validation/0.0.1-alpha.24/native-evidence.txt` for the staged evidence; later stages supersede earlier binaries. The workspace reset around 04:04 UTC destroyed local pre-reset captures and installers. Recovered code is checkpointed on `audit/alpha24-recovery`; fresh core/native evidence is explicitly distinguished from historical measurements.

The latest source corrects river/lake/sea continuity, banks, final-slope materials, cave-noise reuse and water corner geometry across vertical cell/section boundaries. Final native GPU/lake checks of that corner fix now pass, including Vulkan destruction-time validation and a deliberately rejected shutdown completion. The default, no-LOD and Windows delivery profiles now pass nested archive, class/module, native-platform and common-payload audits. Repetitive terrain contours, distant water artifacts and broader worldgen quality are not accepted as complete.

Required checks include the full core/module build; preserved V1/V2 geography and native V2 block digest; actual Create World/join/save/reopen; native channel/inlet/estuary and bank checks; a fully loaded generated basin running the real solver; conservative filling/pressure; matched OpenGL/Vulkan animated/fallback captures; visual inspection and resource lifetime; and profile/installer auditing. Minecraft can crash after an assertion while Gradle exits zero: require explicit acceptance records and inspect failures, not just the process exit code.

The standard build uses JDK 25, Gradle 9.2.1, Minecraft 26.1.2 and NeoForge 26.1.2.122. The private current test toolchain is beside the checkout in `../toolchain`; it is not part of the mod. Its launcher uses an isolated-process compatibility shim only for NeoForm build tools, private software GPU/display dependencies and captured logs. Use the normal wrapper in an ordinary environment. If native execution is unavailable, report that limit and preserve reproducible work; never relabel core math checks as native or visual acceptance.

## Next substantive work

1. Finish the candidate's current native/visual gates. Keep the default installer lean and graphics/LOD/Vulkan independently modular. Native tests on Mesa software drivers do not certify Windows/owner GPU performance.
2. Reproduce enabled-view distant water/page coverage and mixed transparency. Capture separate near terrain, animated water, fallback water and LOD A/B cases, including teleport, edits, unload/reload and resource reload. Disabling LOD in a diagnostic screenshot is not acceptance of an enabled-view defect. Preserve coverage under memory pressure.
3. Reproduce a sealed communicating U-bend with a water path longer than 128 nodes. Current pressure/drain searches have a bounded window without general persistent continuation. Implement fair bounded continuation and safe edit/unload handling; raising a constant is not a complete global hydraulic engine. Preserve volume and never force-load missing chunks. Keep settled worlds idle.
4. Implement native finite/renewable watershed, glacier/snowmelt, aquifer and ocean/atmosphere budgets. Inputs and removals need explicit accounting and world persistence. Do not freeze natural water as an apparent fix or add unlimited source refill. Respect the owner's balanced/default and optional strict/source-safety directions in the requirement records.
5. Continue coherent terrain/geology/material and LOD architecture against `TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md`, `LOD-AND-DRAINAGE-ARCHITECTURE.md` and the configuration/deposit contract. Catalog/species naming and environmental-content work remains separately deferred; do not invent a finished roster. Prioritize connected terrain and real editable-world behavior over superficial noise or asset scatter.

## Constraints that survive updates

- Keep the Deep Primal product identity. It owns its Overworld generator and native liquid; inspiration from TFC/Terralith/Tectonic, PhysEx, finitewater, BetterWaterMesh, Water Erosion, Distant Horizons and terrain/rendering tools does not mean bundling or wrapping their engines.
- Retain graphics settings ownership and complete native Video Settings takeover, with optional LOD and Vulkan implementations behind module boundaries. Preserve settings persistence and saved-world compatibility.
- Published V1/V2 generation stays reproducible. Once a V3 installer is delivered, changing its geography requires a new saved generation revision.
- Do not equate three substeps in a 20 Hz host tick with an independent 60 Hz simulator or a guaranteed 60 FPS minimum. No unbounded flood-to-completion loop or whole-world ocean scan.
- Use primary technical sources and verify licenses/provenance before incorporating code. Proprietary terrain/renderer code is not available to copy merely because it is a reference.
- Measure matched workloads, output equivalence, memory and latency. Keep individual software-test timings distinct from broad performance or reference-mod superiority claims.
- Preserve user edits and water quantity across chunk boundaries, save/reload, multiplayer, teleports and resource changes. Mark absent evidence as open.
- Do not pause the hourly task until the owner asks or the agreed acceptance work is demonstrably complete. Each update must report actual changes and tests; say plainly when there is no new verified progress.

## Archive and shutdown gates

A truncated large workspace JAR was caught during this audit. Build and stage final archives after producer completion, validate every outer/nested ZIP CRC, then check staged bytes/hashes again before delivery. A compiler success or native development launch does not validate the exported installer. Avoid concurrent reads/edits of live build outputs.

A rejected screenshot callback also exposed a real shutdown cleanup bug. The native VulkanShutdownChecks fixture injects a failed completion and requires the following completion to run. Require zero destruction-time Vulkan VUID messages, zero tracked resources and final counters after cleanup. UI capture fixtures must await the image write. Preserve this regression when changing the graphics backend.

## Latest continuation focus

The final native coast uses about 1.76 MB of animated water residency at this camera. Repetitive contours and suspicious distant-water patterns remain visible quality concerns; their cause is unproven. Isolate the waves overlay as well as animated/fallback water, near terrain and LOD before diagnosing missing pages. A useful lake-outlet camera for seed 20261020 is x=107.68827612381756, y=176.5875, z=-226.797007486035, yaw=180, pitch=33.02386755579665. Keep geometry/volumes and the camera fixed across comparisons.

The final ocean-grouping fix removes seven false lake classifications from the nine-seed ensemble: 650 lakes, 964 headwaters, 4,292 connected reaches. The latest 1,000-tick basin is retained; pending work remains nonzero. Do not turn these into claims that regional hydrology or the full terrain design is finished. New installer releases need unique versioned names; do not replace the supplied alpha.24 bytes.
