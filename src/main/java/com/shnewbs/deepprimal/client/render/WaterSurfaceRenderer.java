package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.client.WaterColors;
import com.shnewbs.deepprimal.core.HorizonMesh;
import com.shnewbs.deepprimal.graphics.GpuLighting;
import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** One shared, indexed patch over loaded deep ocean; no wave entities or chunk requests. */
public final class WaterSurfaceRenderer {
  public static final WaterSurfaceRenderer INSTANCE = new WaterSurfaceRenderer();
  private static final RenderPipeline PIPELINE =
      RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "surface_water"))
          .withVertexShader(Identifier.fromNamespaceAndPath("deepprimal", "core/surface_water"))
          .withFragmentShader(Identifier.fromNamespaceAndPath("deepprimal", "core/surface_water"))
          .withSampler("NativeLight")
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
          .withDepthStencilState(
              new DepthStencilState(
                  com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL, false))
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withCull(false)
          .build();
  private ClientLevel level;
  private TerrainGpuMesh patch;
  private int originX, originZ, radius;
  private long sampled;
  private PatchBuild building;
  private HorizonMesh pending;
  private boolean pendingReady;
  private int pendingX, pendingZ, pendingRadius;

  private boolean loaded(BlockPos p) {
    var chunk = level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, false);
    return chunk != null && !(chunk instanceof net.minecraft.world.level.chunk.EmptyLevelChunk);
  }

  public long residentBytes() {
    return patch == null ? 0 : patch.byteSize;
  }

  public int pendingSamples() {
    return building == null ? 0 : building.count - building.cursor;
  }

  private final Matrix4f texture = new Matrix4f();
  private final Vector4f white = new Vector4f(1, 1, 1, 1);

  public void tick() {
    var mc = Minecraft.getInstance();
    if (mc.level != level || !WaterVisualConfig.WAVES.get()) {
      close();
      level = mc.level;
    }
    if (level == null || mc.player == null || !WaterVisualConfig.WAVES.get() || mc.isPaused())
      return;
    int r = WaterVisualConfig.WAVE_RADIUS.get();
    int cx = Math.floorDiv(mc.player.blockPosition().getX(), 4) * 4;
    int cz = Math.floorDiv(mc.player.blockPosition().getZ(), 4) * 4;
    if (building == null
        && !pendingReady
        && (level.getGameTime() - sampled >= 10
            || radius != r
            || cx != originX + radius
            || cz != originZ + radius)) building = new PatchBuild(cx, cz, r);
    if (building != null) {
      building.sample();
      if (building.cursor == building.count) {
        pending = building.finish();
        pendingX = building.cx - building.r;
        pendingZ = building.cz - building.r;
        pendingRadius = building.r;
        pendingReady = true;
        building = null;
        sampled = level.getGameTime();
      }
    }
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

  private void close() {
    building = null;
    pending = null;
    pendingReady = false;
    if (patch != null) {
      patch.close();
      patch = null;
    }
  }

  private final class PatchBuild {
    final int cx, cz, r, cells, side, count;
    final float[] positions;
    final int[] colors;
    final boolean[] valid;
    int cursor;

    PatchBuild(int cx, int cz, int r) {
      this.cx = cx;
      this.cz = cz;
      this.r = r;
      cells = r;
      side = cells + 1;
      count = side * side;
      positions = new float[count * 3];
      colors = new int[count];
      valid = new boolean[count];
    }

    void sample() {
      int end = Math.min(count, cursor + 256);
      while (cursor < end) {
        int i = cursor++, x = i % side, z = i / side, wx = cx - r + x * 2, wz = cz - r + z * 2;
        positions[i * 3] = x * 2;
        positions[i * 3 + 2] = z * 2;
        var p = new BlockPos(wx, 0, wz);
        if (!loaded(p)) continue;
        var top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p);
        var water = top.below();
        if (!level.isEmptyBlock(top)
            || !level.getBiome(water).is(BiomeTags.IS_OCEAN)
            || !level.getFluidState(water).is(FluidTags.WATER)
            || !level.getFluidState(water.below(2)).is(FluidTags.WATER)) continue;
        boolean shore = false;
        for (var d : net.minecraft.core.Direction.Plane.HORIZONTAL)
          if (!loaded(water.relative(d, 2))
              || !level.getFluidState(water.relative(d, 2)).is(FluidTags.WATER)) {
            shore = true;
            break;
          }
        if (shore) continue;
        positions[i * 3 + 1] = water.getY() + 8f / 9f;
        double fade = Math.clamp((r - Math.hypot(wx - cx, wz - cz)) / 8.0, 0, 1);
        colors[i] =
            (int) Math.round(100 * fade) << 24 | (WaterColors.blended(level, water) & 0x00ffffff);
        valid[i] = true;
      }
    }

    HorizonMesh finish() {
      int[] indices = new int[cells * cells * 6];
      int next = 0;
      for (int z = 0; z < cells; z++)
        for (int x = 0; x < cells; x++) {
          int a = z * side + x, b = a + side, c = a + 1, d = b + 1;
          if (!valid[a] || !valid[b] || !valid[c] || !valid[d]) continue;
          float h = positions[a * 3 + 1];
          if (Math.abs(h - positions[b * 3 + 1]) > .1
              || Math.abs(h - positions[c * 3 + 1]) > .1
              || Math.abs(h - positions[d * 3 + 1]) > .1) continue;
          indices[next++] = a;
          indices[next++] = b;
          indices[next++] = c;
          indices[next++] = c;
          indices[next++] = b;
          indices[next++] = d;
        }
      if (next == 0) return null;
      return new HorizonMesh(
          positions,
          colors,
          Arrays.copyOf(indices, next),
          new int[count],
          new short[0],
          level.getMinY(),
          level.getMaxY());
    }
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null || !WaterVisualConfig.WAVES.get()) return;
    if (level != mc.level) {
      close();
      level = mc.level;
    }
    if (pendingReady) {
      var next = pending == null ? null : TerrainGpuMesh.upload(0, 0, 0, pending, false);
      if (patch != null) patch.close();
      patch = next;
      originX = pendingX;
      originZ = pendingZ;
      radius = pendingRadius;
      pending = null;
      pendingReady = false;
    }
    var camera = event.getLevelRenderState().cameraRenderState.pos;
    long ticks = level.getGameTime();
    if (patch == null) return;
    texture
        .identity()
        .m00(
            (float)
                (((ticks + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)) * .045)
                    % (Math.PI * 200)))
        .m01(level.getRainLevel(1))
        .m10(originX)
        .m11(originZ);
    var transforms =
        RenderSystem.getDynamicUniforms()
            .writeTransforms(
                new net.minecraft.client.renderer.DynamicUniforms.Transform(
                    event.getModelViewMatrix(),
                    white,
                    new Vector3f(
                        (float) (originX - camera.x),
                        (float) -camera.y,
                        (float) (originZ - camera.z)),
                    texture));
    var target = mc.getMainRenderTarget();
    try (var pass =
        RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "Deep Primal ocean surface",
                target.getColorTextureView(),
                OptionalInt.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty())) {
      pass.setPipeline(PIPELINE);
      RenderSystem.bindDefaultUniforms(pass);
      GpuLighting.bind(pass);
      pass.setUniform("DynamicTransforms", transforms[0]);
      patch.draw(pass);
    }
  }

  private WaterSurfaceRenderer() {}
}
