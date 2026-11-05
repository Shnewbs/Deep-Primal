# Renderer jam 18 — bounded indexed-draw API submission

Pinned implementation uses at most 4,096 Java draw-record references per synchronous \`drawMultipleIndexed\` call. It preserves the visible-part order and every draw, including extremely large visible sets, without holding on to the previous frame's indexed records after the API returns or raises a failure. Ordinary API batches are **not GPU multi-draw indirect**.

Existing nonbatched submission stays unchanged. Synchronous batching keeps the same pipeline, transforms, depth, lighting, materials, alpha, water handling, and vertex/index format; the unchanged native GPU and transparency behavior still require owner validation. A new diagnostic reports completed \`drawMultipleIndexed\` API calls, not hardware draw counts.

## Isolated source test

\`\`\`sh
find source tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BoundedIndexedDrawBatchesChecks
\`\`\`

Production-excluded checks exercise ordered complete delivery, 4,096-entry budget, 131,072 input parts, callback failure, nested submission, reset, and retention scrubbing. See \`BoundedIndexedDrawBatchesChecks.java\` and the separately retained test evidence. Hardware frame-time/FPS, Native Vulkan/OpenGL, Java25/full Gradle/NeoForge and JAR qualification remain owner-run gates.
