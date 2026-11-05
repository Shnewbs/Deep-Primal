# Dev131 — reject invalid frame/heap telemetry before optional-work recovery

2026-10-31 UTC. Parent pinned to `5d0a68174e12b56a58502117248083a860ee017a`. Independent Dev130 audit: `docs/engine-hourly-audits/2026-10-31T1431-e6d38f32/REVIEW.md`.

## Scope and motivation
Scoped to `WorkBudget.java`, a new dependency-free `WorkBudgetTelemetryChecks.java`, `CoreChecks.java` registration, and this evidence. No renderer, water, terrain, saved profile, GPU or native publication files modified. No overlapping active WorkBudget claim was found in repository code search. The scoped claim is completed/released in the same commit.

The optional-work pacer previously treated an infinite frame target or negative heap-pressure fraction as a valid stable frame. NaN/infinite elapsed samples were ignored without clearing the 59-frame recovery streak. A single subsequent good frame could therefore restore background work without 60 uninterrupted valid good samples. Reject nonfinite or physically out-of-range telemetry, reset the recovery streak and preserve the existing budget fraction.

## Executed isolated validation
OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`.
- 300,031 checks passed: 14 invalid-sample classes, 60-good-frame hysteresis, floor/reset and 150,000 deterministic reference-model trials with exact double-bit parity and bounded fractions.
- Original production WorkBudget compiled as a defective control with the same test and was rejected at assertion 3 (invalid sample failed to reset recovery).
- Local candidate SHA-256: WorkBudget `e725d00192bff2b2f454323c3f0f5749999c0318bf267ad09fb7f1e42d86c3f5`; test `7085fa691e960b6288a0e12f175332c777061aedebb97acf859817db6955dcbb`.
- Tests were run against locally staged exact source text before the GitHub tree commit; remote Git blob verification must be checked after push.

## Acceptance boundary
No complete checkout, Java25, Gradle dependencies, working Git CLI DNS, native game, GPU or verified NeoForge replacement JAR available. Isolated checks are not full integration, no Actions used, and the previous owner JAR must remain intact. Existing independent whole-engine grade remains 5.0/10 without a new regrade.
