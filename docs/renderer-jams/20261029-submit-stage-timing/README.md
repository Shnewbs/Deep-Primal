# Renderer jam 21 — synchronous LOD submit-stage CPU telemetry

This is opt-in source-only renderer instrumentation. It does not measure GPU fence
completion, Vulkan hardware draw calls, actual frame time or FPS.

With `-Ddeepprimal.lod.telemetry=true` the LOD diagnostics now distinguish
transform array preparation/write CPU time, render-pass creation/record/close CPU
time and total CPU time, each with bounded 240-observation p50/p95/p99.
Unlike the older success-only submit timing, these stage observations include
exceptions occurring **after** transform writing. Earlier transform failures and
frames without any visible LOD remain outside the sample. Returned record and
API-call counts include successful calls before a failing pass; no GPU success
is inferred from API return. Native/Vulkan/GL completion remains owner-run.

Standalone validation (not full Minecraft/module compilation):

```sh
find src tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSubmissionProbeChecks
```

The class needs the unchanged `engine-core/.../RollingTiming.java`.
The Java21 isolated probe regression passed 20,000 scenarios and 29,276
assertions. This probes pure-Java percentile/accounting logic, not live
Vulkan/OpenGL or GpuLighting passes.

Implementation scope: `LodSubmissionProbe.java`, `VoxelLodRenderer.java`,
`LodClient.java`, this jam's source check and notes. No water/terrain/shader
or other automation file changes.
