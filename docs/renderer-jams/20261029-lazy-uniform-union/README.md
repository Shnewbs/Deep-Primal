# Renderer-only jam 42 — exact union preparation

Source commit: [`0a500a49ba1c4481b1162eea3783cb10de8c3862`](https://github.com/Shnewbs/Deep-Primal/commit/0a500a49ba1c4481b1162eea3783cb10de8c3862). This code-only optimization defers six per-part min/max bounds reductions while a resident LOD page remains uniformly bounded. On first divergence, it replays the equal-bound prefix in original order, preserving signed-zero bits, negative coordinates, conservative union and adjacent/two-back masks. No mesh geometry, shaders, transparency pass or other workstream changed.

[Read the renderer self-audit](REVIEW.md) and [machine ledger](LEDGER.json). Reference baseline and deterministic parity source are stored next to this file; exact stubs, benchmark harness, logs and negative mutant are in the verified source evidence ZIP uploaded to Library `/Deep Primal/Renderer Jams/` (SHA256 `9805a1618a71d65b5bbf92f3eecbcbdbc1cbd60c2b427e5120a6e4dfa7121e03`).

Run from unpacked evidence archive with Java21+:

```sh
mkdir -p classes
javac --release 21 -Xlint:all -Werror -d classes stubs/*.java source/*.java baseline/*.java tests/*.java
for b in true false; do for t in true false; do
  java -ea -Xverify:all -cp classes -Ddeepprimal.lod.pageFrustumBroadphase=$b -Ddeepprimal.lod.twoBackFrustumReuse=$t com.shnewbs.deepprimal.lod.client.LodLazyUnionParityChecks
  java -ea -Xverify:all -cp classes -Ddeepprimal.lod.pageFrustumBroadphase=$b -Ddeepprimal.lod.twoBackFrustumReuse=$t com.shnewbs.deepprimal.lod.client.LodTwoBackABChecks
done; done
java -cp classes com.shnewbs.deepprimal.lod.client.LodLazyUnionBench uniform-values
```

No qualified owner JAR was built. Full Java25/NeoForge/Minecraft/GPU/native FSR-DLSS checks are explicitly outstanding.
