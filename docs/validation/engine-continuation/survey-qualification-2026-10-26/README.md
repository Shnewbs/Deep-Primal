# Actual survey-method qualification of staged water scheduling

Base main dd00cee27160a358ddeed2d46ed9528f655d3afb. Consumed audit docs/engine-hourly-audits/2026-10-26T1247-68b2278/REVIEW.md, pin68b227868701967908bd5e3b7b46443a207fce7c, carried5.0/10 (not full regrade). Its explicit missing actual-survey-method coverage is addressed here.

New check.py compiles the unchanged production survey method and the prior wrap-safe candidate method verbatim, with deterministic platform boundary doubles. Baseline38cases/5973assertions and candidate158cases/27752assertions pass. Candidate covers five signed clock origins, six column costs, five partial cursor positions, resume-to-completion with no duplicate or skipped columns, unloaded-page defer/no forced loading, stale-page removal, wet/dry/ocean filtering, expired deadlines and wrap control. Production fails to admit columns in the wrapped-deadline characterization; candidate admits10 as expected. Existing staged code is not changed or promoted.

The full staged HydrologyRuntime.java additionally compiles on Java25 against retained Minecraft/NeoForge and product classes, not just stub types. api-compile-command.json records every dependency input; source/class hashes are retained. This is a targeted API compilation with annotation processing disabled, not a full Gradle/module/core check or independent current dependency restoration. Deprecated API note retained; compiler exit0. Five emitted class hashes are recorded in receipt (use receipt as authoritative count).

Exact current Git production bytes match SHA256 d09c46e587ec298bcd269b7eaf35e632961e3abcd9a8c21a219d9f373a758b12. Candidate remains SHA256 ec9c07dcc1b3b7621fe6b099e3e51d812bda9984dfee270ffb63fa67a820c311 from stream-clock-wrap-2026-10-26. No geographic revisions, quantities, profiles, art or renderer changes.

Reproduce with Java25:
python3 check.py --source /checkout/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java --candidate /checkout/docs/validation/engine-continuation/stream-clock-wrap-2026-10-26/staged-HydrologyRuntime.java --java-home /jdk25 --output /isolated/output

Limitations: world/block/terrain interfaces and monotonic clock are doubles. No real Minecraft survey, source quota, account debit, persistence, native latency, GPU pixels or fairness claims. Full current Gradle/core/module/package and actual native budget/save/reopen/shutdown gates remain before production promotion and a new version. Free filesystem headroom observed499MB; no full Gradle/native attempt made in this continuation. No newly qualified JAR or release. Last independent current JAR is the auditor's68b2278 artifact, not this candidate.

Root and foreground water claims were checkpointed before acquisition. Personal context was checked but supplied no newer live writer claim; auditor recorded completed13:02UTC. No exclusive timing reservation observed. Own survey1310 outputs only, no other checkout/cache/process modified. Bounded writer lease released with checkpoint. Keep acceptance incomplete and builder enabled; next restore safe build headroom and complete integrated qualification, then continue world ownership/V8 gates.
