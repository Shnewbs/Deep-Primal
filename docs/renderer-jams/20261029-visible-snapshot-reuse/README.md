# Renderer jam 23 — reusable visibility extraction scratch

Jam ID: `renderer-20261029T0300-visible-snapshot-reuse`. Owner builds full Minecraft/Gradle/GPU.

The frame extraction path formerly created an ArrayList for every LOD-visible frame
and then an immutable List.copyOf for rendering. A single renderer-owned transient
array collector is now reused; List.copyOf remains the only published data and
finally always clears the collector. World reset drops the retained backing array.
No changes to culling, selection, mesh data, draw ordering, water transparency,
GPU API or simulation.

Focused source-excluded check (system Java21, tested before commit):

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  LodVisibleSnapshot.java LodVisibleSnapshotChecks.java
java -ea -Xverify:all -cp classes \
  com.shnewbs.deepprimal.lod.client.LodVisibleSnapshotChecks
```

10,000 randomized frames (0–512 visible items, duplicates, stable detached
immutable snapshots) plus null rejection, nested extraction, failed publication,
reference clearing and reset. The paired warmed 10,000-frame/256-item Java21
ThreadMXBean allocation probe saw old 66,480,000 B vs reusable 21,044,568 B,
a 45,435,432 B reduction in this isolated fixture (not a client frame-time,
GPU VRAM, FPS or native allocation measurement).

Open: owner Java25 Gradle/NeoForge, native Vulkan/OpenGL and transparent-water
pixels, same-scene p50/p95/p99 CPU/GPU frame, VRAM and allocation profiles,
resize/save/reopen/reload/shutdown, genuine temporal FSR/DLSS integration.
