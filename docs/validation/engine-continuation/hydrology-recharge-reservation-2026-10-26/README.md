# Staged regular hydrology admission reservation

Not promoted; not included in the dev.30 JAR. Baseline is production `293516cdb0c62d66ae12a5146e4ac2e6590c45a3`.

After closing the source-column deadline gate, the next exact-method reproduction found that a busy natural-source pass can consume every tick's remaining deadline before the regular visit loop. That loop performs regional recharge/accounting work. With 256 candidates and a deterministic 100 microsecond callback, 100 simulated ticks admit zero regular visits. This is a CPU scheduling reproduction with platform doubles, not an observed Minecraft water-loss or latency measurement.

The candidate gives the source pass a deadline 500 microseconds before the total deadline when regular columns are enabled. Its headwater ordering and finite quota code stay unchanged. `COLUMNS=0` retains the full source-pass admission window. The candidate yields 500 regular visits in the reproduction. Baseline and candidate each pass 3,360 boundary cases across five clock origins, varying survey cost, source count/cost and configured regular breadth.

The full candidate also compiles against current Minecraft/NeoForge binary APIs under Java25: five classes. The first standalone compile failed because source JARs accidentally caused dependency-source compilation. The failed log is retained; the corrected invocation excludes source JARs, uses an empty sourcepath and disables implicit compilation. This is not a full Gradle or native qualification.

Run `probe.py` with `--source` pointing at baseline HydrologyRuntime.java, `--source-pin` identifying that exact commit, `--java-home`, `--fixture` pointing at `../stream-deadline-release-2026-10-26/reproduce-admission.py`, and a fresh `--output` directory. It extracts the actual production tick method and emits the candidate independently of the native runtime.

Before promotion: verify natural quota/headwater ordering and precipitation/recharge cadence in actual packaged native runs, full Java25/core/module/build and package identities, save/reopen/shutdown, and any required visual gates. Keep source/column fairness, loaded/unloaded ownership, long-soak/multiplayer and hardware latency gates open. A single expensive callback can consume the reservation; this is admission budgeting, not a hard wall-clock guarantee. No grade increase or engine completion is claimed.
