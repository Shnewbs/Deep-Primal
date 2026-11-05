# FSR 1 shader maintenance and checks

The runtime uses AMD's original FP32 EASU and RCAS kernels, pinned to the source
commit identified in `shaders/upscaling/vendor/readme.txt`. Generated shaders are
checked in, so ordinary Gradle builds and game launches need no Python or C tools.

## Regenerate shaders

Run from the repository root:

```sh
python graphics-settings/tools/generate_fsr1_shaders.py
```

The generator requires `cpp`. It preprocesses the unmodified vendor headers and
retains the functions reachable from the selected kernels. Callback code supplies
the actual texture reads. The generated files retain the MIT copyright notices.

## Compare actual shader pixels with upstream

On Linux with `libEGL`, `libGL`, and `cpp` available:

```sh
python graphics-settings/tools/validate_fsr1_kernels.py --output build/fsr-kernel-checks.json
```

The check creates a surfaceless OpenGL context and compares the production shader
outputs with a separate reference compiled from the full AMD headers. The reference
uses native `textureGather` and AMD's own constant-generation functions. Twenty-five
cases cover constant colors, gradients, hard edges, one-pixel inputs, odd extents,
and one-to-one scaling. Each case checks EASU and three RCAS strengths, resulting in
100 production draws and 100 reference draws. The tolerance is one byte per channel.

This checks the real shader math and borders. A software Mesa pass does not measure
game performance or validate the Minecraft render hooks.

## Check the production Minecraft GPU API

`FsrGpuApiChecks.java` is a standalone Java 25 harness in package
`com.shnewbs.deepprimal.validation`. Compile and run it with the Minecraft client
runtime dependencies and compiled graphics-settings classes on the classpath.
Its single argument is the absolute `assets/deepprimal_graphics/shaders` directory.
It needs a working display, which may be Xvfb with software Mesa.

The harness calls the actual `Fsr1Upscaler` through Minecraft's OpenGL `GlDevice`.
It checks pixel values, resizing, all three representative sharpness settings,
missing shader rejection, failure latching, recovery after shader cache reload,
alias rejection, caller ownership, and idempotent close. The deliberately missing
shader case logs shader errors before the final pass marker.

Minecraft's `GlDevice` is package-private, so the test constructs it reflectively.
The harness stays in the project's own package because Minecraft's client jar is
signed. It neither modifies the Minecraft jar nor ships in the production module.

## Check the bundled Vulkan shader converter

`FsrVulkanShaderChecks.java` uses the actual compiled `renderer-vulkan` converter.
Compile/run it with that module, graphics-settings, and Minecraft's dependencies on
the classpath. It accepts the `shaders/core` directory and an output directory.
It verifies one sampler and a 64-byte EASU / 16-byte RCAS uniform layout, then writes
both converted vertex/fragment shader pairs.

Compile each resulting `.vert` / `.frag` file with
`glslangValidator -V --target-env vulkan1.2`, and validate its SPIR-V with
`spirv-val --target-env vulkan1.2`.

These checks verify the real converter and shader compilation. A live Vulkan
client remains necessary to validate game-frame submission and presentation.
