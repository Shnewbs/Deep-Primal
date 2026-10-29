# Watershed mutation runner failure classification

Reviewed main at `a7ade50ea8aac1458db8f2d6b1461e16967bc00d`, including relevant engine/renderer diffs since `1f2d99e34da848f34ce25d3a9fea263f3a58f7aa`. The scoped terrain writer had released its lease. Existing local uncommitted engine/water work was left intact; tests used separately fetched, commit-pinned source.

Both watershed runners previously returned the same false result for a Java compilation failure and an executed failing test. Their mutation loops accepted either as a rejected control. They also accepted launcher failures or a zero exit without the expected success token.

The runners now abort on compilation errors, missing success tokens, and unexpected JVM failures. A negative control must compile, exit with Java's assertion-failure status, report an uncaught AssertionError, and include its actual fixture in the stderr stack trace. Only that executed assertion counts as rejection.

## Verification

- Regression before correction: original runners failed the new failure-classification checks, including compiler failure and mutant launcher failure.
- `python tools/test_watershed_runner_failures.py`: 3 tests, 24 subcases passed. Subprocess outcomes are simulated in this regression suite.
- Actual JDK 25.0.4.1 compilation with `--release 21 -Xlint:all -Werror`, execution with `-ea -Xverify:all`: interior routes passed two positive runs and rejected 3/3 compiled mutants; region bridge passed two positive runs and rejected 14/14 compiled mutants. These use the existing isolated helper fixtures; region integration uses its committed API doubles.
- Latest committed `LodFrozenBoundsCommittedSmoke`: compiled with the same JDK/options; 403,094 assertions passed with broadphase enabled and 403,094 with it disabled. This is an isolated CPU helper fixture with committed Minecraft/GPU doubles.
- Full current-main Gradle/NeoForge compilation and in-game Vulkan/OpenGL/save-reopen tests were not run. A fresh git clone failed at network name resolution; the existing full local working tree is older and contains unrelated edits. Isolated Java compilation does not establish full game/build acceptance.

Only the two Python runners, their process-failure regression test and this receipt change. No runtime behavior, version bump, build publication or new engine grade.
