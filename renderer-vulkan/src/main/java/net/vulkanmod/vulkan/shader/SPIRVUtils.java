// VulkanMod 0.6.8+26.1.2; Deep Primal cache and resource-lifetime changes. LGPL-3.0-only.
package net.vulkanmod.vulkan.shader;

import java.nio.*;
import java.nio.charset.StandardCharsets;
import net.vulkanmod.Initializer;
import net.vulkanmod.cache.*;
import org.lwjgl.system.*;
import org.lwjgl.util.shaderc.*;

/** Single compiler owner, preprocessed source identity and explicit native result lifetimes. */
public final class SPIRVUtils {
  private static long compiler, options, compiled, cacheHits;
  private static int outstandingResults;
  private static ShadercIncludeResolve includer;
  private static ShadercIncludeResultRelease releaser;
  private static boolean cacheAvailable = true;

  private static void initCompiler() {
    if (compiler != 0) return;
    compiler = Shaderc.shaderc_compiler_initialize();
    if (compiler == 0) throw new IllegalStateException("Cannot create shader compiler");
    options = Shaderc.shaderc_compile_options_initialize();
    if (options == 0) {
      Shaderc.shaderc_compiler_release(compiler);
      compiler = 0;
      throw new IllegalStateException("Cannot create compiler options");
    }
    includer =
        ShadercIncludeResolve.create(
            (user, requested, type, requesting, depth) -> include(requested, depth));
    releaser =
        ShadercIncludeResultRelease.create(
            (user, pointer) -> {
              var result = ShadercIncludeResult.create(pointer);
              MemoryUtil.memFree(result.source_name());
              MemoryUtil.memFree(result.content());
              result.free();
            });
    Shaderc.shaderc_compile_options_set_optimization_level(
        options, Shaderc.shaderc_optimization_level_performance);
    Shaderc.shaderc_compile_options_set_target_env(
        options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2);
    Shaderc.shaderc_compile_options_set_include_callbacks(options, includer, releaser, 0);
  }

  private static long include(long pointer, long depth) {
    String name = MemoryUtil.memUTF8(pointer), source = "#error Vulkan include not found\n";
    if (depth <= 32 && name.matches("[a-zA-Z0-9_./-]+") && !name.contains(".."))
      try (var input =
          SPIRVUtils.class.getResourceAsStream("/assets/vulkanmod/shaders/include/" + name)) {
        if (input != null) source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      } catch (java.io.IOException error) {
        source = "#error Cannot read Vulkan include\n";
      }
    return ShadercIncludeResult.calloc()
        .source_name(MemoryUtil.memUTF8(name, false))
        .content(MemoryUtil.memUTF8(source, false))
        .address();
  }

  private static void check(long result, String file) {
    if (result == 0) throw new IllegalStateException("Cannot compile shader " + file);
    if (Shaderc.shaderc_result_get_compilation_status(result)
        != Shaderc.shaderc_compilation_status_success)
      throw new IllegalArgumentException(
          "Cannot compile shader "
              + file
              + ":\n"
              + Shaderc.shaderc_result_get_error_message(result));
  }

  public static synchronized SPIRV compileShader(String file, String source, ShaderKind kind) {
    java.util.Objects.requireNonNull(source);
    initCompiler();
    long preprocessing =
        Shaderc.shaderc_compile_into_preprocessed_text(
            compiler, source, kind.kind, file, "main", options);
    String expanded;
    try {
      check(preprocessing, file);
      var bytes = Shaderc.shaderc_result_get_bytes(preprocessing);
      int length = bytes.remaining();
      if (length > 0 && bytes.get(bytes.limit() - 1) == 0) length--;
      byte[] raw = new byte[length];
      bytes.duplicate().get(raw);
      expanded = new String(raw, StandardCharsets.UTF_8);
    } finally {
      if (preprocessing != 0) Shaderc.shaderc_result_release(preprocessing);
    }
    String key =
        BinaryCache.key(
            ("dp-spv-v1;lwjgl-shaderc-3.4.1;vk1.2;performance;" + kind + "\n" + expanded)
                .getBytes(StandardCharsets.UTF_8));
    if (cacheAvailable)
      try {
        byte[] data = BackendCaches.SHADERS.read(key);
        if (data != null
            && data.length >= 20
            && data.length % 4 == 0
            && ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt() == 0x07230203) {
          cacheHits++;
          outstandingResults++;
          return new SPIRV(0, MemoryUtil.memAlloc(data.length).put(data).flip());
        }
      } catch (java.io.IOException error) {
        disableCache(error);
      }
    long result =
        Shaderc.shaderc_compile_into_spv(compiler, expanded, kind.kind, file, "main", options);
    try {
      check(result, file);
      var bytes = Shaderc.shaderc_result_get_bytes(result);
      if (cacheAvailable)
        try {
          byte[] raw = new byte[bytes.remaining()];
          bytes.duplicate().get(raw);
          BackendCaches.SHADERS.write(key, raw);
        } catch (java.io.IOException error) {
          disableCache(error);
        }
      compiled++;
      outstandingResults++;
      return new SPIRV(result, bytes);
    } catch (RuntimeException | Error failure) {
      if (result != 0) Shaderc.shaderc_result_release(result);
      throw failure;
    }
  }

  private static void disableCache(java.io.IOException error) {
    cacheAvailable = false;
    Initializer.LOGGER.warn("Vulkan shader disk cache unavailable; compiling normally", error);
  }

  public static synchronized int outstandingResults() {
    return outstandingResults;
  }

  public static synchronized long compiledShaders() {
    return compiled;
  }

  public static synchronized long shaderCacheHits() {
    return cacheHits;
  }

  public static synchronized void shutdown() {
    Initializer.LOGGER.info(
        "VULKAN_SHADER_CACHE compiled={} hits={} outstandingResults={}",
        compiled,
        cacheHits,
        outstandingResults);
    if (options != 0) {
      Shaderc.shaderc_compile_options_release(options);
      options = 0;
    }
    if (compiler != 0) {
      Shaderc.shaderc_compiler_release(compiler);
      compiler = 0;
    }
    if (includer != null) {
      includer.free();
      includer = null;
    }
    if (releaser != null) {
      releaser.free();
      releaser = null;
    }
  }

  public static final class SPIRV implements NativeResource {
    private long handle;
    private ByteBuffer bytecode;

    private SPIRV(long handle, ByteBuffer bytecode) {
      this.handle = handle;
      this.bytecode = bytecode;
    }

    public ByteBuffer bytecode() {
      return java.util.Objects.requireNonNull(bytecode, "Shader result is closed");
    }

    @Override
    public void free() {
      synchronized (SPIRVUtils.class) {
        if (bytecode == null) return;
        if (handle != 0) {
          Shaderc.shaderc_result_release(handle);
          handle = 0;
        } else MemoryUtil.memFree(bytecode);
        bytecode = null;
        outstandingResults--;
      }
    }
  }

  public enum ShaderKind {
    VERTEX_SHADER(0),
    GEOMETRY_SHADER(3),
    FRAGMENT_SHADER(1),
    COMPUTE_SHADER(2);
    private final int kind;

    ShaderKind(int kind) {
      this.kind = kind;
    }
  }

  private SPIRVUtils() {}
}
