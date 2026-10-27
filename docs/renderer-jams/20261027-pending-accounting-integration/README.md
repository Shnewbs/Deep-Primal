# Renderer jam 20261027-pending-accounting-integration

The renderer schedule promoted the already-staged, exact private-array accounting loop into **production** [VoxelLodRenderer lines 96–104](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96-L104), without pulling an older full VoxelLodRenderer candidate over current retargeting, changing water/transparency, or editing engine/water/graphics-upscaling ownership.

**Parent main:** a5b5f0c7bf3a6b2774d47721c9a8b6ccb71253f0. **Scoped lease commit:** bab2ec0ce5ef38f40281076765c31a7d37a918ca. **Product code:** [b53d66c0c326de8f5259d42a49598cdd79004d92](https://github.com/Shnewbs/Deep-Primal/commit/b53d66c0c326de8f5259d42a49598cdd79004d92). **Source blob:** e035637f91b1800d06b0c47c7676f35db8ddc118. Exactly one method is changed: the nested PendingGpu.bytes() once used a newly built three-stage Stream pipeline on repeated upload/memory budget checks. It now performs simple indexed accumulation over the same three ArrayLists and returns the same long total. No GPU draw method, shader, mesh geometry, resource, resident-accounting mutation, water surface, profiling switch or version label was changed. Source before/after prefix and suffix were verified byte-identical. The result matches the previously staged candidate algorithm, excluding its comment.

## Reproducible scoped execution

Temurin **Java 25.0.5+8** was located in /mnt/data/deepprimal-toolchains/jdk-25.0.5+8 and actually invoked. A deterministic standalone Java25 structure-equivalence control compared the original stream expression and exact integrated indexed-loop algorithm on 10,063 three-level cases: **10,063/10,063 pass**. Under a separate warmed, interleaved four-old/four-new 200,000-call JVM microprobe, every pair had identical checksum 3026541304310; median wall times were **27.0195 ms old** and **10.164 ms indexed** and allocated byte counts **108,800,040 vs 40 per 200,000 calls**. This is a *synthetic accounting microprobe*, not real client FPS, GPU memory, load time, p99 frame latency or a full source build. See [probe summary](probe.log), [isolated build attempt](build-attempt.log) and [self-audit](REVIEW.md). Portable test source, full logs and structured evidence were saved in evidence ZIP SHA256 e119228921390b1020f59bcd49fac74f396c98787f2947ef1ec650909b02ea66.

The official module/private admission fixture and exact-root Java25/Gradle build could **not** execute: an isolated main clone failed `Could not resolve host: github.com` (exit 128). Full checked-out source/Gradle wrapper absent, despite JDK25 being available. There is **no fresh qualified JAR** and no GitHub or CurseForge release, and no old dev32 binary has been relabeled. The checked source's existing `mod_version` is 0.0.1-alpha.29-dev.33-loadedreads.1; it is not a new distributable identity.

## Renderer backlog handoff

1. Restore exact Git checkout+Gradle dependencies and run full Java25 root, module, native, package and source identity gates, including the already-staged admissionChecks for actual private PendingGpu.
2. Capture qualified OpenGL/Vulkan and AMD8060S camera/travel/edit matched p50/p95/p99 frame/upload/heap/VRAM, culling, section rebuild, draw submissions; test pending upload cancellation, resize/reload/world switch and transparency.
3. Integrate staged capacity/admission checks only after current-main API/test reconciliation; preserve the released source retarget fix, frozen V1–V7 geography and independent water+FSR1/DLSS ownership.
4. Investigate real native horizon delivery/LOD fairness and explicit unexplored-terrain authority; do not equate a larger slider or old software FSR1 results with new FPS/RTX/DLSS acceptance.

The scoped lease is released at this checkpoint. Previous independent full-engine grade 50/100 is unchanged.
