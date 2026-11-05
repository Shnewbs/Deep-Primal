# Reproduction and source identity

Pinned renderer code `f569a31cd3e59593c0343a8617fa0934289bb3e9`. Source helper Git blob `770dfde8609da73d3e96bdbfad8d9606cc09efda`. Reproducible test source is `LodMeshPartSplitterChecks.java` here; standalone tests use API-compatible type stubs and are **not** a full Minecraft or native GL/Vulkan validation.

```sh
# In the isolated evidence workspace (with source/, stubs/, tests/):
find source stubs tests -name '*.java' > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodMeshPartSplitterChecks
# LOD_MESH_PART_SPLITTER_PASS assertions=282240 randomizedSingle=1800 randomizedPages=300 optIn32KiB=true
# Mutation: replace 'indices[i] -= firstVertex;' with 'indices[i] -= 0;' in private copy
# Test fails with ArrayIndexOutOfBoundsException, exit 1; not committed.
```

`LodMeshPartSplitterBench.java` is in the external source-evidence archive. It reports a CPU-only Java21 component profile with 400 measured samples and 800 warmups: 94208 source bytes, 3 output parts at 32KiB, p50 34883ns, p95 76335ns, p99 183867ns, maximum 1461268ns. No GPU/whole-frame metric follows.

Toolchain preflight: owner Temurin 25.0.5+8 archive and installation path absent in this run; observed Java/javac 21.0.12.1. No Java25/Gradle/full build, package, Minecraft or GPU execution. 
