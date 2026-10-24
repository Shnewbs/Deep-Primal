# Exact isolated audit runners

These scripts retain the executed commands. Copy them into a task root with `engine-review/` (the development checkout), `project/` (the alpha.28 checkout containing its accepted Linux JAR), `toolchain/` (the pinned JDK25/JDK21/Gradle/cache/compatibility helper), and `fresh-compile.gradle` from the previous release's validation runners. They intentionally create new evidence/native-run directories and refuse to overwrite prior runs.

Run `build_review.py`, then `run_worldgen_benchmark.py baseline-1`, `baseline-2`, `candidate-1`, `baseline-profile` sequentially. Run `run_noise_benchmark.py` after all native producers finish. The profile run uses JFR and is separately attributed. No competing build or timed workload should run during a benchmark. The reproduction uses the repo's current-proxy helper for this managed environment; normal setups can use the Gradle commands in the parent README. No credentials or tool binaries are embedded.
