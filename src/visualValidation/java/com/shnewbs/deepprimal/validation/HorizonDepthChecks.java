package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Reads real native depth before/after the horizon pass; development only. */
final class HorizonDepthChecks {
  static boolean requested;
  private GpuBuffer before;

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void before(RenderLevelStageEvent.AfterSky event) {
    if (!requested || before != null) return;
    if (!RenderSystem.getDevice().getBackendName().equals("Vulkan"))
      throw new IllegalStateException(
          "Native OpenGL depth readback is unsupported; use Vulkan for this check");
    var target = Minecraft.getInstance().getMainRenderTarget();
    before =
        RenderSystem.getDevice()
            .createBuffer(
                () -> "Native depth before horizon",
                GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ,
                (long) target.width * target.height * 4);
    RenderSystem.getDevice()
        .createCommandEncoder()
        .copyTextureToBuffer(target.getDepthTexture(), before, 0, () -> {}, 0);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void after(RenderLevelStageEvent.AfterSky event) {
    if (before == null) return;
    requested = false;
    var target = Minecraft.getInstance().getMainRenderTarget();
    var first = before;
    before = null;
    var second =
        RenderSystem.getDevice()
            .createBuffer(
                () -> "Native depth after horizon",
                GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ,
                first.size());
    RenderSystem.getDevice()
        .createCommandEncoder()
        .copyTextureToBuffer(
            target.getDepthTexture(),
            second,
            0,
            () -> {
              try (var a =
                      RenderSystem.getDevice()
                          .createCommandEncoder()
                          .mapBuffer(first, true, false);
                  var b =
                      RenderSystem.getDevice()
                          .createCommandEncoder()
                          .mapBuffer(second, true, false)) {
                if (!a.data().equals(b.data()))
                  throw new AssertionError("Horizon changed native depth");
                long own =
                    com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE
                        .backgroundDepthBytes();
                if (own <= 0)
                  throw new AssertionError("Horizon did not render with isolated depth");
                System.out.println(
                    "HORIZON_NATIVE_DEPTH_PASS comparedBytes="
                        + first.size()
                        + " backgroundBytes="
                        + own);
              } finally {
                second.close();
                first.close();
              }
            },
            0);
  }
}
