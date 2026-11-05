# Renderer jam 22 — failed LOD pre-draw CPU telemetry

LOD submission histograms previously ignored target/camera acquisition,
transform buffer reservation and uniform-writing exceptions. These failures
now enter the opt-in CPU timing history as preparation-only failed passes.
No draw calls or records are credited, and the original exception is rethrown.
Draw-pass errors continue on their existing finally path without duplication.

The new lodSubmissionPreDrawFailures diagnostic distinguishes pre-draw failures
from errors during command recording. Only when the system property
deepprimal.lod.telemetry=true is set does the catch handler read CPU clocks.

Isolated Java21 source regression (not native Minecraft or GPU validation):

    find src -name '*.java' | sort > sources.txt
    javac --release 21 -Xlint:all -Werror -d classes @sources.txt
    java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPretransformFailureChecks

Tests cover 30,000 mixed successful, pre-draw failure, and partial API
submission cases, precise rolling percentiles, counters and world reset.
Source-only integration checks verify the renderer call boundary and telemetry
opt-in. No geometry, water surface, shader, LOD distance, FSR/DLSS, Vulkan
backend, terrain, or unrelated workstream changed.

Java25 Gradle, native Vulkan/OpenGL, water pixels, VRAM/FPS and packaged
Minecraft client/JAR are owner-operated and explicitly unrun in this jam.
