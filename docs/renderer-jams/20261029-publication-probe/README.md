# Renderer jam 41: publication CPU split and alias evidence

Reproducible ON/OFF comparison needs the cost of constructing two-back frustum aliases as well as its avoided fine-frustum predicates. This renderer-only change adds opt-in `lodPublicationFreezeCpuTiming`, `lodPublicationMetadataCpuTiming`, `lodPublicationTotalCpuTiming` (rolling 240 prepared-page CPU p50/p95/p99), plus counts of prepared pages, frozen parts, adjacent alias bits and two-back alias bits.

Enable `-Ddeepprimal.lod.telemetry=true`; compare `-Ddeepprimal.lod.twoBackFrustumReuse=true` and `false` in matching hardware/scene/budget runs. Broadphase=false deliberately produces zero reuse masks. Metrics are bounded per world and reset on world switch. They do not measure GPU execution, wall-frame latency, VRAM or quality.

Java21 isolated unit fixture (owner still operates full Java25/Gradle/Minecraft/GPU/JAR):
```sh
javac --release 21 -Xlint:all -Werror -d classes \
 engine-core/src/main/java/com/shnewbs/deepprimal/core/RollingTiming.java \
 lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublicationProbe.java \
 docs/renderer-jams/20261029-publication-probe/LodPublicationProbeChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPublicationProbeChecks
```

Keep Vulkan first/OpenGL fallback, water order, actual FSR1 and unaccepted temporal FSR/DLSS unchanged. Audit/results in this jam folder.
