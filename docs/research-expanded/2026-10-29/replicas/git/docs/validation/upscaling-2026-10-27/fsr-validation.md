# Expanded replica — docs/validation/upscaling-2026-10-27/fsr-validation.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/fsr-validation.md) |
| Source identity | git: docs/validation/upscaling-2026-10-27/fsr-validation.md |
| Preserved source SHA-256 | `f5de46203290a73a42cf1a2a344f2ca866eb1d218e661ad5e9a1c7e9d613251d` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

FSR1 kernel comparison against AMD references, production OpenGL API lifecycle tests and Vulkan conversion plus SPIR-V validation. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that llvmpipe correctness evidence supports no hardware-performance claim, and converted shader validation does not submit Vulkan game frames. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a layered shader verification record linking pinned vendor headers, generated shaders, production API behavior and later live presentation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat unusual image sizes and sharpness values, reject aliased targets, preserve caller-owned resources and exercise failure-latched shader readiness followed by cache invalidation recovery. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- FSR 1 validation — 2026-10-27

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `fsr-api-validation.log` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/fsr-api-validation.log) |
| `fsr-gpu-validation-portable.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/fsr-gpu-validation-portable.json) |
| `fsr-vulkan-converter.log` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/fsr-vulkan-converter.log) |
| `fsr-vulkan-spirv.log` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/fsr-vulkan-spirv.log) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# FSR 1 validation — 2026-10-27

**All three FSR checks passed:** actual GPU kernel output against AMD's reference,
the production `Fsr1Upscaler` through Minecraft 26.1.2's OpenGL GPU API, and the
bundled Vulkan shader converter followed by SPIR-V compilation and validation.

These checks ran on **Mesa 25.2.8 / llvmpipe (LLVM 20.1.2, 256 bits)** with OpenGL
4.5 and Java 25. This software renderer provides correctness evidence; it supports
no performance or hardware compatibility claims. The converter test does not
submit Vulkan game frames. Full-client render-hook and Vulkan presentation tests
are separate from this report. No NVIDIA DLSS evaluation is covered here.

## Checks and recorded evidence

| Check | Coverage and result | Evidence |
| --- | --- | --- |
| Production EASU and RCAS pixels | 25 cases; 100 production draws and 100 independent AMD reference draws; all passed within one byte per channel | [Kernel results](fsr-gpu-validation-portable.json) |
| Production Minecraft GPU API | 12 renders across four resize cases; all lifecycle, shader recovery, ownership and alias checks passed | [GPU API log](fsr-api-validation.log) |
| Actual bundled Vulkan converter | EASU: one 64-byte uniform block and one sampler; RCAS: one 16-byte uniform block and one sampler; `out` parameters preserved | [Converter log](fsr-vulkan-converter.log) |
| Converted Vulkan shaders | Both vertex/fragment pairs compiled for Vulkan 1.2; all four SPIR-V modules validated with exit code 0 | [SPIR-V log](fsr-vulkan-spirv.log) |

The kernel test uses the checked-in production GLSL and separately compiles the
complete, unmodified AMD headers with native `textureGather` and AMD's own
`FsrEasuCon` / `FsrRcasCon` constant generation. Production callbacks use clamped
`texelFetch` reads with equivalent gather ordering. The test compares the actual
rendered pixels, including edge clamping, opaque alpha and constant-field
preservation. The recorded edge cases also show RCAS changing pixel values.

The 25 cases combine black, white, constant RGB `(53, 133, 209)`, gradients and
hard edges with these input → output dimensions:

- `1×1 → 3×5`
- `7×5 → 13×9`
- `17×13 → 34×26`
- `31×19 → 41×29`
- `16×8 → 16×8`

Each case checks EASU, then RCAS strengths `0.1`, `0.5` and `1.0` against the AMD
reference. The production API harness uses the four size pairs other than
`17×13 → 34×26`, and sharpness `0`, `0.5` and `1`; zero exercises the RCAS bypass.
It checks pixel values, buffer layout, intermediate-target resizing, rejection of
aliased input/output, preservation of caller-owned resources and repeated close.
It deliberately removes shader sources, verifies that readiness fails and stays
latched, then clears the pipeline cache and invalidates shaders to verify recovery.
The missing-shader errors in its log are expected test inputs, followed by the
`FSR_GPU_API_PASS` marker. SLF4J duplicate-provider warnings come from the standalone
test classpath.

`FsrVulkanShaderChecks` calls the real compiled `GLSLParser` from `renderer-vulkan`,
using `DefaultVertexFormat.EMPTY`. It verifies descriptor layout and preservation
of `FSR_OUT` before writing the converted shaders. `glslangValidator` and
`spirv-val` then check those outputs, rather than manually approximated Vulkan
shader source.

## Reproduction commands

Run from the repository root. The kernel test needs Python, `cpp`, `libEGL` and
`libGL`; it uses a surfaceless EGL context:

```sh
python graphics-settings/tools/validate_fsr1_kernels.py \
  --output /tmp/fsr-gpu-validation-portable.json
```

The Java harnesses need JDK 25, compiled `engine-core`, `graphics-settings` and
`renderer-vulkan` classes, plus the Minecraft 26.1.2 client runtime dependencies
including the LWJGL native jars. Set `MC_RUNTIME_CP` to that resolved runtime
classpath. Exclude source/userdev jars and NeoForm decompilation outputs. The API
harness also needs a working X display; this run used Xvfb with Mesa llvmpipe.
These are standalone validation programs, not shipped game entry points:

```sh
FSR_CP="$MC_RUNTIME_CP:engine-core/build/classes/java/main:graphics-settings/build/classes/java/main:renderer-vulkan/build/classes/java/main"
FSR_SHADER_ROOT="$PWD/graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders"
mkdir -p /tmp/fsr-validation-classes
javac --release 25 -proc:none -sourcepath '' -cp "$FSR_CP" \
  -d /tmp/fsr-validation-classes \
  graphics-settings/tools/FsrGpuApiChecks.java \
  graphics-settings/tools/FsrVulkanShaderChecks.java
java --enable-native-access=ALL-UNNAMED \
  -cp "/tmp/fsr-validation-classes:$FSR_CP" \
  com.shnewbs.deepprimal.validation.FsrGpuApiChecks "$FSR_SHADER_ROOT" \
  > /tmp/fsr-api-validation.log 2>&1
java --enable-native-access=ALL-UNNAMED \
  -cp "/tmp/fsr-validation-classes:$FSR_CP" \
  com.shnewbs.deepprimal.validation.FsrVulkanShaderChecks \
  "$FSR_SHADER_ROOT/core" /tmp/fsr-vulkan-converted \
  > /tmp/fsr-vulkan-converter.log 2>&1
for shader in /tmp/fsr-vulkan-converted/*.vert /tmp/fsr-vulkan-converted/*.frag; do
  glslangValidator -V --target-env vulkan1.2 -o "$shader.spv" "$shader" || exit 1
  spirv-val --target-env vulkan1.2 "$shader.spv" || exit 1
done
```

The saved SPIR-V log records the exit status of each of those eight compiler and
validator invocations. Kernel regeneration is a separate maintenance step:
`python graphics-settings/tools/generate_fsr1_shaders.py`. Ordinary builds use the
checked-in shaders and do not require Python or a C toolchain.

## AMD provenance and license

The kernels are **FSR 1 spatial EASU / RCAS**, sourced from
[AMD's FidelityFX-FSR repository at commit a21ffb8f6c13233ba336352bdff293894c706575](https://github.com/GPUOpen-Effects/FidelityFX-FSR/tree/a21ffb8f6c13233ba336352bdff293894c706575).
They do not implement temporal FSR or frame generation. The unmodified headers and
upstream MIT license are in
`graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/upscaling/vendor/`.
Generated shaders retain the AMD notice and the additional Michal Drobot MIT
notice from `ffx_a.h`. SHA-256 of the pinned files:

```text
f60e2722fcd13989523b9164d776ab382b3692791767f3bf8bb19967f763f3fb  ffx_a.h
93c3922362ea7fc99cbcc698ca30c98de4f8c246d1fbb0b09e015ddef38ce3a5  ffx_fsr1.h
db089274ce766da70f5b7d791029c3486f9f9e27c8c79c652689603d3192e802  license.txt
```

Resource filenames are lowercase (`license.txt`, `readme.txt`) to satisfy
Minecraft resource identifiers. The generator and maintenance documentation use
those same names.


<!-- END PRESERVED SOURCE -->
