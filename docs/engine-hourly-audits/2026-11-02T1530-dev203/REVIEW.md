# Dev203 — Independent Deep Primal audit (2026-11-02 15:30 UTC)

## Scope and provenance

- Baseline audited GitHub `main`: `683cd036e04d75782282523f76e1be5470ea4833`.
- Previous independent audit: `121f443bb4b02bc13cd9d1672e00e140751c151a` (Dev201).
- GitHub compare: **9 commits ahead**, six changed files: `SampleStats.java`, `TickWindow.java`, `TickWindowSnapshotChecks.java`, `CoreChecks.java`, and two scoped-claim documents.
- Changes are scoped to tick telemetry allocation/percentile correctness. No water physics, terrain, Vulkan or LOD implementation changed in this interval. The writer's claim is marked `completed-released`.
- Production files fetched directly from the connected private GitHub repository at the audited SHA. Local `git hash-object` matched exact GitHub blob IDs:
  - `SampleStats.java`: `37b6ac9099dd039faa583f7fd60f33921a95335c`
  - `TickWindow.java`: `db37a1d5ef6cb450a150caa617581c98033c2700`

## Independent reproduction and verification

- Reproduced original redundant allocation path from commit diffs: `TickWindow.snapshot()` copied the initialized prefix, then `SampleStats.of` copied again. Current implementation uses `SampleStats.of(nanos, count)` and exactly one defensive copy.
- Independent Java21 source audit and differential test: `javac --release 21 -Xlint:all -Werror`, then `java -Xverify:all` **PASS**.
- **270,141 assertions passed** across **108,000 seeded ring operations** with capacities 1, 2, 3, 5, 17, 31, 65, 256 and 513. The oracle verified count, nearest-rank p50/p95/p99/max, clear/wrap behavior, rejected negative values, extreme longs, public array immutability and ignored invalid uninitialized suffixes.
- Independent mutation control: intentionally changed the snapshot's initialized count to full array capacity. Strict compilation passed, but the oracle rejected the mutant at `cap=2 step=0` (exit code 1). This demonstrates the test detects incorrect prefix accounting.
- ThreadMXBean allocated bytes per snapshot after warmup, 40,000 samples per variant: full 256-element ring **4175 old vs 2112 new** (49.4% lower); partial 17-element ring **304 old vs 200 new** (34.2% lower). The historical path was reconstructed from the GitHub commit diff; these are local Java21 allocation figures, **not Minecraft FPS or native Vulkan performance**.
- No additional confirmed nonconflicting production defect found. Therefore **no speculative production patch** was made; this audit report is the only intended repository change.

## Compatibility and coverage boundaries

- `gradle.properties` currently pins `neo_version=26.1.2.122`; root `build.gradle` specifies Java toolchain 25 and ModDevGradle `2.0.148`.
- Official NeoForged project index lists `26.3.0.101` as of this audit; the official Maven index also contains `26.1.2.123` and `.124`. **No binary or source compatibility claim** is made for a version change.
- Minecraft's official 26.4 Snapshot 1 notes (2026-09-22) make `Default` prefer Vulkan and disable automatic graphics API fallback after a startup crash. This increases the importance of client startup and fallback-path validation; it does not demonstrate current Deep Primal compatibility.
- Full `CoreChecks`, NeoForge Gradle compilation, mod client/server startup, water conservation/save-reopen, terrain seam validation, Vulkan/OpenGL/LOD native FPS and GPU visual checks were **not run**.

## Build gate and retention

- Runtime has OpenJDK **21**, no Java25 installation, no Gradle executable, no complete private checkout, and no cached NeoForge dependency tree.
- Direct `git ls-remote` failed: `Could not resolve host: github.com` (exit 128). GitHub connector allowed scoped read and report commit, but does not provide a mounted full checkout for Gradle.
- **No playable replacement JAR or JAR SHA256 produced.** No superseded JAR was deleted. No GitHub Actions were invoked.

## Grades and next actions

- **Scoped tick snapshot correctness: 8.5/10 provisional** — exact production blobs, independent property oracle, negative control, and allocation evidence. Native game integration remains untested.
- **Whole-engine: 5.0/10 historical baseline, not freshly regraded** — no comprehensive runtime evidence to justify changing it.
- Next: restore complete checkout + Java25 + Gradle/NeoForge cache; run full `coreChecks` and `jar`, verify JAR checksum and contents, launch client and dedicated server, test native water/save/terrain and Vulkan/LOD performance. Delete prior audit JAR only after a verified playable replacement exists.

## External references

- NeoForged official project index: https://projects.neoforged.net/
- NeoForged Maven releases: https://maven.neoforged.net/releases/net/neoforged/neoforge
- Minecraft official 26.4 Snapshot 1: https://www.minecraft.net/fi-fi/article/minecraft-26-4-snapshot-1
