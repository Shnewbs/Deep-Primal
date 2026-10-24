# R45: attributable engine client validation

Fresh consolidated-main reproduction: Gradle's unqualified `runClient --dry-run`
selects both `:runClient` and `:gui-themes:runClient`. This explains why the earlier
combined GUI run cannot be treated as independently attributed root-client evidence.

The development proxy helper adds opt-in `--root-game-task runClient` or `runServer`
before `--`. It appends the absolute root task and rejects conflicting game selectors
before starting Gradle. README documents it. Explicit standalone task paths and legacy
arguments remain unchanged. This changes validation tooling, not engine behavior.

Three real offline Gradle graphs pass: control selects both clients; the opt-in selects
only the root client; an explicit standalone Themes selector selects only Themes. Eight isolated argument controls pass, including client/server,
explicit standalone/legacy preservation and four conflicts rejected before Gradle.
Raw compressed dry-run logs retain their original SHA256 in results.json. Helper
syntax is compiled without executing a build. The isolated source manifest verifies
669 existing engine/Themes inputs before copying; independent Maritime and five
standalone Themes wrapper/ignore files are outside this build snapshot.

Another foreground native job was observed, so owned-process cleanup was requested.
Final raw/hash/results inspection showed the third dry-run had already completed,
correcting the initial assumption that it was interrupted. All three graphs pass.
No fresh full build, Minecraft/native session, screenshot or timing claim follows.
The eight argument controls then used a mocked subprocess, starting no Gradle jobs.
One process-cleanup attempt could not import optional psutil; cleanup used standard
library /proc parent/cwd ownership checks instead. No other worker's processes or
checkout were modified. Task-graph commands used the shared pinned cache, so no
performance comparison is valid.

Reproduce argument controls with `python check_arguments.py` beside `project/`.
`validate.py` is the bounded original three-graph runner; its preserved results record
all three completed graphs. Do not rerun it during an active foreground lease/job.
Use current main for continuation, rather than restoring this partial build snapshot.

Next run the merged packaged Vulkan water suite with explicit root task and separate
save/crash/resource/shutdown records. Bounded cross-frame normal publication, actual
stream/ocean temporal continuity, scene churn, hard2ms, LOD, hydrology/chemistry,
geology and owner-GPU/FPS acceptance remain unfinished. No installer was generated.
