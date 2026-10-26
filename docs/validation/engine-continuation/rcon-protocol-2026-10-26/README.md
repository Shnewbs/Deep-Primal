# Native validation RCON protocol repair

Pinned baseline 0e05fa4356266524d11084df04d96034c8ff6624. Runtime remains alpha.29-dev.26-water.1; no product version or art/geography/water quantities changed.

The actual parser accepted a wrong request ID carrying `Saved the game`, wrong response types and invalid terminators. Lengths were unbounded and per-recv socket timeouts allowed indefinite trickling. Authentication failure leaked the socket, and an optional empty auth preamble was mistaken for authentication success.

Repair: validate 10..4106-byte frames (4096 payload plus framing), exact request ID, response type and two terminators; use one monotonic 20-second request deadline through every receive and initial send; consume at most one empty auth preamble; close failed-auth connections. Commands used by this harness have short responses. General multipart command aggregation is not implemented; subsequent mismatched packets fail closed rather than acknowledging another request.

New real-parser wire tests reproduce baseline failures (7 failures, 2 errors). Final 12 protocol plus 16 existing receipt/shutdown tests pass in normal, -O and -OO modes. Controlled fragmented bytes, not an actual Minecraft session, exercise the parser. These results do not qualify gameplay, native water or a new JAR.

Consumed latest completed audit: docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md, pinned4fb29e0; scoped5.0/10, no new full regrade. This repair strengthens native save/reopen evidence; it does not close V8, physical water ledgers, visual or performance findings.

Workspace maintenance removed the earlier checkout and caches. Pinned Java25/Java21/Gradle and helper archives restored with all size/SHA checks passing (toolchain-restore.log). Direct private-repository clone failed: could not read Username for https://github.com. Connector reads/writes remain available; full project/dependency restoration is unfinished. No full Java/Gradle/module/native/graphics checks or fresh artifact/release this run. Do not reuse old checks as current validation.

Expired root and water claims read before work; water-rendering/runtime files left untouched. Personal-context retrieval did not establish a live other-chat writer. Auditor run record reports no_update05:51Z, not a new audit. Builder remains enabled; lease released at checkpoint. Next restore pinned project/dependencies through authorized access and continue water loaded/unloaded ledger/persistence plus V8 saved-profile/topology acceptance, reconciling foreground water first.
