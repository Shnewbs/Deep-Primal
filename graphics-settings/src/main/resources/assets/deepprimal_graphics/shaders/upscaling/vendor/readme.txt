AMD FidelityFX Super Resolution 1, pinned upstream source

Repository: https://github.com/GPUOpen-Effects/FidelityFX-FSR
Commit: a21ffb8f6c13233ba336352bdff293894c706575
Files: ffx-fsr/ffx_a.h, ffx-fsr/ffx_fsr1.h, license.txt

The two header files are unmodified upstream source. license.txt is the upstream
MIT license. The additional Michal Drobot notice in ffx_a.h is retained in the
generated shaders along with the AMD notice.

Run python graphics-settings/tools/generate_fsr1_shaders.py from the repository
to reproduce the checked-in GLSL shaders. This needs the C preprocessor cpp;
building and running Deep Primal do not require a C compiler or Python.

The generator selects the original FP32 EASU and RCAS kernels and their reachable
helpers. Algorithm bodies are preprocessed, not reimplemented. GLSL callback
adaptations provide clamped texture reads. The EASU callback reproduces
textureGather's four-component ordering with texelFetch for OpenGL 3.3 support.
The FSR_OUT macro preserves out parameters through the bundled Vulkan shader
converter; the shader compiler expands it to the ordinary out qualifier.

The Java side packs the same EASU constants as upstream FsrEasuCon. For the FP32
RCAS path, the sharpness slider directly supplies the weight that FsrRcasCon
would compute as exp2(-stops). Zero skips RCAS and one is maximum sharpening.

This is FSR 1 spatial upscaling. It is not temporal FSR 2/3/4 or frame generation.
