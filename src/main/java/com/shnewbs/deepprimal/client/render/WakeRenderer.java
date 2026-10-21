package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.core.WakeField;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Persistent trails in one reused 12 KiB GPU buffer; no wake entities or unbounded queues. */
public final class WakeRenderer {
  public static final WakeRenderer INSTANCE = new WakeRenderer();
  private static final int BYTES = WakeField.CAPACITY * 6 * 16;
  private static final RenderPipeline PIPELINE =
      RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "wakes"))
          .withVertexShader(Identifier.fromNamespaceAndPath("deepprimal", "core/wakes"))
          .withFragmentShader(Identifier.fromNamespaceAndPath("deepprimal", "core/wakes"))
          .withSampler("NativeLight")
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
          .withDepthStencilState(
              new DepthStencilState(
                  com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL, false))
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withCull(false)
          .build();
  private final WakeField field = new WakeField();
  private net.minecraft.client.multiplayer.ClientLevel level;
  private GpuBuffer vertices;
  private ByteBuffer staging;
  private int count, originX, originZ;
  private boolean dirty;
  private final Matrix4f identity = new Matrix4f();
  private final Vector4f white = new Vector4f(1, 1, 1, 1);

  public long residentBytes() {
    return vertices == null ? 0 : vertices.size();
  }

  public static boolean available() {
    return RendererOwnership.owned()
        && WaterVisualConfig.WAKES.get()
        && !RendererOwnership.installed("wakes")
        && !RendererOwnership.installed("wakesreforged")
        && !RendererOwnership.installed("wakes_reforged")
        && !RendererOwnership.installed("particular");
  }

  public void tick() {
    var mc = Minecraft.getInstance();
    if (level != mc.level || !available()) {
      close();
      level = mc.level;
    }
    if (level == null || mc.player == null || mc.isPaused() || !available()) return;
    field.tick();
    dirty = true;
    int examined = 0, admitted = 0;
    for (var entity : level.entitiesForRendering()) {
      if (++examined > 128 || admitted >= 16) break;
      if (!(entity instanceof AbstractBoat) && !(entity instanceof LivingEntity)) continue;
      var velocity = entity.getDeltaMovement();
      if (!entity.isInWater()
          || entity.distanceToSqr(mc.player) > 48 * 48
          || velocity.horizontalDistanceSqr() < .003) continue;
      var p = BlockPos.containing(entity.getX(), entity.getY() + .2, entity.getZ());
      if (!level.hasChunkAt(p)) continue;
      if (!level.getFluidState(p).is(FluidTags.WATER)) p = p.below();
      var fluid = level.getFluidState(p);
      if (!fluid.is(FluidTags.WATER) || level.getFluidState(p.above()).is(FluidTags.WATER))
        continue;
      field.add(
          entity.getX(),
          p.getY() + fluid.getHeight(level, p) + .015,
          entity.getZ(),
          velocity.x,
          velocity.z,
          Math.min(1, velocity.horizontalDistance() * 5));
      admitted++;
    }
  }

  private void pack() {
    var player = Minecraft.getInstance().player;
    originX = player.blockPosition().getX();
    originZ = player.blockPosition().getZ();
    if (staging == null)
      staging = MemoryUtil.memAlloc(BYTES).order(java.nio.ByteOrder.nativeOrder());
    staging.clear();
    count = 0;
    for (int i = 0; i < WakeField.CAPACITY; i++) {
      if (!field.alive(i)) continue;
      var p = BlockPos.containing(field.x(i), field.y(i) - .03, field.z(i));
      if (!level.hasChunkAt(p) || !level.getFluidState(p).is(FluidTags.WATER)) continue;
      float x = (float) (field.x(i) - originX),
          z = (float) (field.z(i) - originZ),
          y = (float) field.y(i);
      float dx = field.dx(i), dz = field.dz(i), spread = field.spread(i);
      float ax = x - dx * .3f - dz * spread, az = z - dz * .3f + dx * spread;
      float bx = x - dx * .3f + dz * spread, bz = z - dz * .3f - dx * spread;
      int alpha = (int) (field.alpha(i) * 255);
      vertex(ax, y, az, alpha);
      vertex(bx, y, bz, alpha);
      vertex(bx - dx * .1f, y, bz - dz * .1f, alpha);
      vertex(ax, y, az, alpha);
      vertex(bx - dx * .1f, y, bz - dz * .1f, alpha);
      vertex(ax - dx * .1f, y, az - dz * .1f, alpha);
    }
    staging.flip();
    if (count > 0) {
      if (vertices == null)
        vertices =
            RenderSystem.getDevice()
                .createBuffer(
                    () -> "Deep Primal wake field",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    BYTES);
      RenderSystem.getDevice()
          .createCommandEncoder()
          .writeToBuffer(vertices.slice(0, staging.remaining()), staging);
    }
    dirty = false;
  }

  private void vertex(float x, float y, float z, int alpha) {
    staging
        .putFloat(x)
        .putFloat(y)
        .putFloat(z)
        .put((byte) 210)
        .put((byte) 233)
        .put((byte) 239)
        .put((byte) alpha);
    count++;
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    var mc = Minecraft.getInstance();
    if (level == null || level != mc.level || mc.player == null || !available()) return;
    if (dirty) pack();
    if (count == 0 || vertices == null) return;
    var camera = event.getLevelRenderState().cameraRenderState.pos;
    var transform =
        RenderSystem.getDynamicUniforms()
            .writeTransforms(
                new net.minecraft.client.renderer.DynamicUniforms.Transform(
                    event.getModelViewMatrix(),
                    white,
                    new Vector3f(
                        (float) (originX - camera.x),
                        (float) -camera.y,
                        (float) (originZ - camera.z)),
                    identity));
    var target = mc.getMainRenderTarget();
    try (var pass =
        RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "Deep Primal wakes",
                target.getColorTextureView(),
                OptionalInt.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty())) {
      pass.setPipeline(PIPELINE);
      RenderSystem.bindDefaultUniforms(pass);
      GpuLighting.bind(pass);
      pass.setUniform("DynamicTransforms", transform[0]);
      pass.setVertexBuffer(0, vertices);
      pass.draw(0, count);
    }
  }

  private void close() {
    if (vertices != null) vertices.close();
    vertices = null;
    if (staging != null) MemoryUtil.memFree(staging);
    staging = null;
    count = 0;
    dirty = false;
    for (int i = 0; i < WakeField.LIFETIME; i++) field.tick();
  }

  @SubscribeEvent
  public void logout(
      net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
    close();
    level = null;
  }

  @SubscribeEvent
  public void shutdown(net.neoforged.neoforge.event.GameShuttingDownEvent event) {
    close();
    level = null;
  }

  private WakeRenderer() {}
}
