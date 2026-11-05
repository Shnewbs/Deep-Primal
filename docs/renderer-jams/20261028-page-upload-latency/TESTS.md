# Focused test receipt (renderer-only)

Pinned product code `0f1ef9cd0f0e739a6bd757421105df42b3f85d44`. This jam does **not** contain a qualified JAR.

- `javac --release 21 -Xlint:all -Werror -d classes @sources.txt`: **PASS** (three production Java source shapes plus the standalone helper and smoke).
- `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadProbeChecks`: **PASS** 36,117 assertions / 60,000 randomized observations across 12 capacity/seed combinations.
- `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadSmoke`: **PASS**; its committed blob `ba5a8d5686e6da74b83af3ad23c10df10e58f6b9` matches local executed source.
- Private mutant: change only `case MEMORY -> memoryDrops` to `case MEMORY -> staleDrops`; compilation passes and the extended fixture exits **1** at `successful-only counts` (expected negative control).
- Final Git read-back of all four source/test paths: **PASS** at the pinned commit. Six integration anchors verify two discard paths, saturating frame count, reset, labeled diagnostics and cache-commit-before-record ordering.
- Java25 owner archive and directory absent in this isolated Linux environment. `java -version`, `javac -version` = 21.0.12.1. **No** full Gradle/module/product build, native Minecraft, GPU, Windows DLL, delivered JAR or actual FPS acceptance.

Evidence ZIP: `DeepPrimal_Renderer_PageUploadLatency_20261028_0f1ef9c_SourceEvidence.zip`, **6,765 bytes**, SHA-256 `fb7e2192189eb6a54df397f1caec58da5e0c5131eb7a502424bd4a4a408c90ec`, 9 CRC-valid entries. Source-only; no release.
