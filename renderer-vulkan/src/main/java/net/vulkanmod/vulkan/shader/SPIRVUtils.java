// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;

public class SPIRVUtils {
   private static final boolean DEBUG = true;
   private static final boolean OPTIMIZATIONS = false;
   private static long compiler;
   private static long options;
   private static final SPIRVUtils.ShaderIncluder SHADER_INCLUDER = new SPIRVUtils.ShaderIncluder();
   private static final SPIRVUtils.ShaderReleaser SHADER_RELEASER = new SPIRVUtils.ShaderReleaser();
   private static final long pUserData = 0L;
   private static ObjectArrayList<String> includePaths;

   private static void initCompiler() {
      compiler = Shaderc.shaderc_compiler_initialize();
      if (compiler == 0L) {
         throw new RuntimeException("Failed to create shader compiler");
      } else {
         options = Shaderc.shaderc_compile_options_initialize();
         if (options == 0L) {
            throw new RuntimeException("Failed to create compiler options");
         } else {
            Shaderc.shaderc_compile_options_set_optimization_level(options, Shaderc.shaderc_optimization_level_performance);
            Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2);
            Shaderc.shaderc_compile_options_set_include_callbacks(options, SHADER_INCLUDER, SHADER_RELEASER, 0L);
            includePaths = new ObjectArrayList<>();
            addIncludePath("/assets/vulkanmod/shaders/include/");
         }
      }
   }

   public static void addIncludePath(String path) {
      URL url = SPIRVUtils.class.getResource(path);
      if (url != null) {
         includePaths.add(url.toExternalForm());
      }
   }

   public static SPIRVUtils.SPIRV compileShader(String filename, String source, SPIRVUtils.ShaderKind shaderKind) {
      if (source == null) {
         throw new NullPointerException("Source for %s.%s is null".formatted(filename, shaderKind));
      } else {
         long result = Shaderc.shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", options);
         if (result == 0L) {
            throw new RuntimeException("Failed to compile shader %s into SPRI-V".formatted(filename));
         } else if (Shaderc.shaderc_result_get_compilation_status(result) != 0) {
            String errorMessage = Shaderc.shaderc_result_get_error_message(result);
            throw new RuntimeException("Failed to compile shader %s into SPIR-V:\n\t%s".formatted(filename, errorMessage));
         } else {
            return new SPIRVUtils.SPIRV(result, Shaderc.shaderc_result_get_bytes(result));
         }
      }
   }

   static {
      initCompiler();
   }

   public static final class SPIRV implements NativeResource {
      private final long handle;
      private ByteBuffer bytecode;

      public SPIRV(long handle, ByteBuffer bytecode) {
         this.handle = handle;
         this.bytecode = bytecode;
      }

      public ByteBuffer bytecode() {
         return this.bytecode;
      }

      @Override
      public void free() {
         this.bytecode = null;
      }
   }

   private static class ShaderIncluder implements ShadercIncludeResolveI {
      @Override public long invoke(long userData, long requestedSource, int type, long requestingSource, long depth) {
         String requested = MemoryUtil.memUTF8(requestedSource);
         String source = "#error Vulkan include not found\n";
         if (depth <= 32 && requested.matches("[a-zA-Z0-9_./-]+") && !requested.contains("..")) {
            try (var input = SPIRVUtils.class.getResourceAsStream("/assets/vulkanmod/shaders/include/" + requested)) {
               if (input != null) source = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.IOException error) { source = "#error Cannot read Vulkan include\n"; }
         }
         return ShadercIncludeResult.calloc().source_name(MemoryUtil.memUTF8(requested, false))
            .content(MemoryUtil.memUTF8(source, false)).user_data(userData).address();
      }
   }

   public static enum ShaderKind {
      VERTEX_SHADER(0),
      GEOMETRY_SHADER(3),
      FRAGMENT_SHADER(1),
      COMPUTE_SHADER(2);

      private final int kind;

      private ShaderKind(int kind) {
         this.kind = kind;
      }
   }

   private static class ShaderReleaser implements ShadercIncludeResultReleaseI {
      @Override
      public void invoke(long user_data, long include_result) {
         var result = ShadercIncludeResult.create(include_result);
         MemoryUtil.memFree(result.source_name()); MemoryUtil.memFree(result.content()); result.free();
      }
   }
}
