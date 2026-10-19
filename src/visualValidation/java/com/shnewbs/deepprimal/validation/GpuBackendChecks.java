package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryUtil;

/** Device-level transfers, independent of terrain algorithms. Development source set only. */
final class GpuBackendChecks {
  static void run(Minecraft mc) {
    if (!RenderSystem.getDevice().getBackendName().equals("Vulkan"))
      throw new AssertionError("Vulkan was not selected");
    shaderChecks();
    storageChecks();
    depthChecks();
    var device = RenderSystem.getDevice();
    var encoder = device.createCommandEncoder();
    var bytes = MemoryUtil.memAlloc(16);
    for (int i = 0; i < 16; i++) bytes.put((byte) (i * 13 + 7));
    bytes.flip();
    var source =
        device.createBuffer(
            () -> "Transfer check source",
            GpuBuffer.USAGE_COPY_SRC | GpuBuffer.USAGE_COPY_DST,
            bytes);
    MemoryUtil.memFree(bytes);
    var target =
        device.createBuffer(
            () -> "Transfer check destination",
            GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
            16);
    var query = encoder.timerQueryBegin();
    encoder.copyToBuffer(source.slice(), target.slice());
    encoder.timerQueryEnd(query);
    RenderSystem.queueFencedTask(
        () -> {
          try (var view = device.createCommandEncoder().mapBuffer(target, true, false)) {
            for (int i = 0; i < 16; i++)
              if (view.data().get(i) != (byte) (i * 13 + 7))
                throw new AssertionError("Buffer copy differs at " + i);
          }
          var value = query.getValue();
          if (value.isEmpty() || value.getAsLong() <= 0)
            throw new AssertionError("No device timestamp result");
          query.close();
          target.close();
          source.close();
          System.out.println("VULKAN_BUFFER_QUERY_PASS bytes=16");
        });
    var texture =
        device.createTexture("Transfer check texture", 3, TextureFormat.RGBA8, 2, 2, 1, 1);
    var copy = device.createTexture("Transfer check copy", 3, TextureFormat.RGBA8, 2, 2, 1, 1);
    try (var image = new com.mojang.blaze3d.platform.NativeImage(2, 2, false)) {
      for (int y = 0; y < 2; y++)
        for (int x = 0; x < 2; x++) image.setPixelABGR(x, y, 0xff332211 + (x + y * 2));
      encoder.writeToTexture(texture, image);
    }
    encoder.copyTextureToTexture(texture, copy, 0, 0, 0, 0, 0, 2, 2);
    var readback = device.createBuffer(() -> "Texture check readback", 9, 16);
    encoder.copyTextureToBuffer(
        copy,
        readback,
        0,
        () -> {
          try (var view = device.createCommandEncoder().mapBuffer(readback, true, false)) {
            for (int i = 0; i < 4; i++)
              if (view.data().getInt(i * 4) != (0xff332211 + i))
                throw new AssertionError(
                    "Texture copy differs at "
                        + i
                        + ": "
                        + Integer.toHexString(view.data().getInt(i * 4)));
          }
          readback.close();
          copy.close();
          texture.close();
          System.out.println("VULKAN_TEXTURE_COPY_PASS pixels=4");
        },
        0);
    var atlas =
        mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getTexture();
    for (int level : new int[] {0, atlas.getMipLevels() - 1}) {
      int width = atlas.getWidth(level), height = atlas.getHeight(level);
      var atlasData = device.createBuffer(() -> "Atlas check", 9, (long) width * height * 4);
      encoder.copyTextureToBuffer(
          atlas,
          atlasData,
          0,
          () -> {
            int colored = 0;
            try (var read = device.createCommandEncoder().mapBuffer(atlasData, true, false);
                var image = new com.mojang.blaze3d.platform.NativeImage(width, height, false)) {
              for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++) {
                  int pixel = read.data().getInt((x + y * width) * 4);
                  image.setPixelABGR(x, y, pixel);
                  if ((pixel & 0xffffff) != 0) colored++;
                }
              image.writeToFile(
                  java.nio.file.Path.of("/tmp/deepprimal-vulkan-atlas-" + level + ".png"));
            } catch (java.io.IOException error) {
              throw new RuntimeException(error);
            }
            atlasData.close();
            if (colored == 0) throw new AssertionError("Rendered atlas is empty at mip " + level);
            System.out.println(
                "VULKAN_ATLAS_RANGE level="
                    + level
                    + " colored="
                    + colored
                    + " total="
                    + (width * height));
          },
          level);
    }
    for (var format :
        new TextureFormat[] {TextureFormat.DEPTH24_STENCIL8, TextureFormat.DEPTH32_STENCIL8}) {
      try {
        if (!(boolean)
            device
                .getClass()
                .getMethod("supportsTextureFormat", TextureFormat.class)
                .invoke(null, format)) {
          try {
            device.createTexture("Unsupported format check", 9, format, 8, 8, 1, 1);
            throw new AssertionError("Unsupported format accepted");
          } catch (UnsupportedOperationException expected) {
            System.out.println("VULKAN_FORMAT_REJECT_PASS format=" + format);
            continue;
          }
        }
      } catch (ReflectiveOperationException error) {
        throw new RuntimeException(error);
      }
      var color = device.createTexture("Stencil test color", 15, TextureFormat.RGBA8, 8, 8, 1, 1);
      var depth = device.createTexture("Stencil test depth", 9, format, 8, 8, 1, 1);
      var depthSource = device.createTexture("Stencil copy source", 11, format, 8, 8, 1, 1);
      var colorView = device.createTextureView(color);
      var depthView = device.createTextureView(depth);
      var data = MemoryUtil.memCalloc(96);
      var uniform = device.createBuffer(() -> "Stencil test light uniform", 128, data);
      MemoryUtil.memFree(data);
      encoder.clearStencilTexture(depthSource, 1);
      encoder.clearDepthTexture(depthSource, 0.375);
      encoder.copyTextureToTexture(depthSource, depth, 0, 0, 0, 0, 0, 8, 8);
      encoder.clearColorTexture(color, 0xffffffff);
      for (int reference = 0; reference <= 1; reference++) {
        final int expectedReference = reference;
        var face =
            new net.neoforged.neoforge.client.stencil.StencilPerFaceTest(
                net.neoforged.neoforge.client.stencil.StencilOperation.KEEP,
                net.neoforged.neoforge.client.stencil.StencilOperation.KEEP,
                net.neoforged.neoforge.client.stencil.StencilOperation.KEEP,
                com.mojang.blaze3d.platform.CompareOp.EQUAL);
        var stencil =
            new net.neoforged.neoforge.client.stencil.StencilTest(face, 255, 255, reference);
        var pipeline =
            net.minecraft.client.renderer.RenderPipelines.LIGHTMAP.toBuilder()
                .withLocation(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                        "deepprimal",
                        "validation/stencil_" + format.name().toLowerCase() + "_" + reference))
                .withStencilTest(stencil)
                .build();
        try (var pass =
            encoder.createRenderPass(
                () -> "Stencil test",
                colorView,
                java.util.OptionalInt.empty(),
                depthView,
                java.util.OptionalDouble.empty())) {
          pass.setPipeline(pipeline);
          pass.setUniform("LightmapInfo", uniform);
          for (int repeat = 0; repeat < 3; repeat++) pass.draw(0, 3);
        }
        var pixels = device.createBuffer(() -> "Stencil test readback", 9, 256);
        encoder.copyTextureToBuffer(
            color,
            pixels,
            0,
            () -> {
              boolean changed = false;
              try (var view = device.createCommandEncoder().mapBuffer(pixels, true, false)) {
                for (int i = 0; i < 64; i++) changed |= view.data().getInt(i * 4) != 0xffffffff;
              }
              pixels.close();
              if (changed != (expectedReference == 1))
                throw new AssertionError(
                    "Stencil test differs for " + format + " reference=" + expectedReference);
              if (expectedReference == 1) {
                colorView.close();
                depthView.close();
                color.close();
                depth.close();
                depthSource.close();
                uniform.close();
              }
              System.out.println(
                  "VULKAN_STENCIL_PASS format=" + format + " reference=" + expectedReference);
            },
            0);
      }
    }
    var light = mc.gameRenderer.lightmap().texture();
    var lightData =
        device.createBuffer(
            () -> "Lightmap check", 9, (long) light.getWidth(0) * light.getHeight(0) * 4);
    encoder.copyTextureToBuffer(
        light,
        lightData,
        0,
        () -> {
          int min = 255, max = 0;
          try (var view = device.createCommandEncoder().mapBuffer(lightData, true, false)) {
            for (int i = 0; i < light.getWidth(0) * light.getHeight(0) * 4; i++)
              if (i % 4 != 3) {
                int value = view.data().get(i) & 255;
                min = Math.min(min, value);
                max = Math.max(max, value);
              }
          }
          lightData.close();
          System.out.println("VULKAN_LIGHTMAP_RANGE min=" + min + " max=" + max);
          if (max == 0) throw new AssertionError("Entire lightmap is black");
        },
        0);
  }

  private static void depthChecks() {
    var device = RenderSystem.getDevice();
    var encoder = device.createCommandEncoder();
    var depth = device.createTexture("D32 readback", 9, TextureFormat.DEPTH32, 4, 4, 1, 1);
    encoder.clearDepthTexture(depth, 0.375);
    var read = device.createBuffer(() -> "Depth readback", 9, 64);
    encoder.copyTextureToBuffer(
        depth,
        read,
        0,
        () -> {
          try (var view = device.createCommandEncoder().mapBuffer(read, true, false)) {
            for (int i = 0; i < 16; i++)
              if (view.data().getFloat(i * 4) != 0.375f)
                throw new AssertionError("Depth readback differs");
          }
          read.close();
          depth.close();
          System.out.println("VULKAN_DEPTH_READBACK_PASS pixels=16");
        },
        0);
  }

  private static void shaderChecks() {
    try {
      var compiler = Class.forName("net.vulkanmod.vulkan.shader.SPIRVUtils");
      var kind = Class.forName("net.vulkanmod.vulkan.shader.SPIRVUtils$ShaderKind");
      Object fragment =
          java.lang.Enum.valueOf(kind.asSubclass(java.lang.Enum.class), "FRAGMENT_SHADER");
      var compile = compiler.getMethod("compileShader", String.class, String.class, kind);
      String source =
          "#version 450\nlayout(location=0) out vec4 color;void main(){color=vec4(VALUE);}";
      var a = compile.invoke(null, "fixture.frag", source.replace("VALUE", "0.0"), fragment);
      var b = compile.invoke(null, "fixture.frag", source.replace("VALUE", "1.0"), fragment);
      var c = compile.invoke(null, "fixture.frag", source.replace("VALUE", "0.0"), fragment);
      var result = a.getClass();
      var bytecode = result.getMethod("bytecode");
      var free = result.getMethod("free");
      try {
        var ab = (java.nio.ByteBuffer) bytecode.invoke(a);
        var bb = (java.nio.ByteBuffer) bytecode.invoke(b);
        var cb = (java.nio.ByteBuffer) bytecode.invoke(c);
        if (ab.equals(bb) || !ab.equals(cb))
          throw new AssertionError("Shader cache source identity failed");
      } finally {
        free.invoke(a);
        free.invoke(a);
        free.invoke(b);
        free.invoke(c);
      }
      try {
        compile.invoke(
            null, "fixture-invalid.frag", "#version 450\nnot valid shader syntax", fragment);
        throw new AssertionError("Invalid shader accepted");
      } catch (java.lang.reflect.InvocationTargetException expected) {
        if (!(expected.getCause() instanceof IllegalArgumentException)) throw expected;
      }
      if ((int) compiler.getMethod("outstandingResults").invoke(null) != 0
          || (long) compiler.getMethod("shaderCacheHits").invoke(null) < 1)
        throw new AssertionError("Shader result lifetime/cache hit failed");
      System.out.println(
          "VULKAN_SHADER_CACHE_PASS sourceIdentity=true nativeResults=0"
              + " invalidShaderRejected=true");
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void storageChecks() {
    var device = RenderSystem.getDevice();
    var encoder = device.createCommandEncoder();
    int offset = 65792, count = 17;
    var data = MemoryUtil.memCalloc(offset + count);
    for (int i = 0; i < count; i++) data.put(offset + i, (byte) (i * 17 - 128));
    var source =
        device.createBuffer(() -> "SSBO signed bytes", GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER, data);
    MemoryUtil.memFree(data);
    var id =
        net.minecraft.resources.Identifier.fromNamespaceAndPath(
            "deepprimal", "validation/storage_bytes");
    var pipeline =
        com.mojang.blaze3d.pipeline.RenderPipeline.builder()
            .withLocation(id)
            .withVertexShader(id)
            .withFragmentShader(id)
            .withVertexFormat(
                com.mojang.blaze3d.vertex.DefaultVertexFormat.EMPTY,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES)
            .withUniform(
                "Bytes", com.mojang.blaze3d.shaders.UniformType.TEXEL_BUFFER, TextureFormat.RED8I)
            .withCull(false)
            .build();
    device.precompilePipeline(
        pipeline,
        (shader, type) ->
            type == com.mojang.blaze3d.shaders.ShaderType.VERTEX
                ? "#version 330\n"
                      + "void main(){vec2"
                      + " p=vec2((gl_VertexID<<1)&2,gl_VertexID&2);gl_Position=vec4(p*2.0-1.0,0.0,1.0);}"
                : "#version 330\n"
                      + "uniform isamplerBuffer Bytes;out vec4 fragColor;void"
                      + " main(){fragColor=vec4(float(texelFetch(Bytes,int(gl_FragCoord.x)).r+128)/255.0,0.0,0.0,1.0);}");
    var color = device.createTexture("SSBO output", 15, TextureFormat.RGBA8, count, 1, 1, 1);
    var view = device.createTextureView(color);
    try (var pass =
        encoder.createRenderPass(
            () -> "SSBO signed-byte check", view, java.util.OptionalInt.of(0))) {
      pass.setPipeline(pipeline);
      pass.setUniform("Bytes", source.slice(offset, count));
      pass.draw(0, 3);
    }
    var read = device.createBuffer(() -> "SSBO readback", 9, count * 4);
    encoder.copyTextureToBuffer(
        color,
        read,
        0,
        () -> {
          try (var mapping = device.createCommandEncoder().mapBuffer(read, true, false)) {
            for (int i = 0; i < count; i++)
              if ((mapping.data().get(i * 4) & 255) != (i * 17 & 255))
                throw new AssertionError("Signed-byte fetch differs at " + i);
          }
          read.close();
          view.close();
          color.close();
          source.close();
          System.out.println("VULKAN_STORAGE_BYTES_PASS offset=" + offset + " count=" + count);
        },
        0);
  }

  private GpuBackendChecks() {}
}
