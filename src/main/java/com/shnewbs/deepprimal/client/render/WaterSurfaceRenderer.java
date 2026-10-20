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
          .withDepthStencilState(DepthStencilState.DEFAULT)
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withCull(false)
          .build();
  private ClientLevel level;
  private HorizonGpuMesh patch;
  private int originX, originZ, radius;
  private long sampled;
  private final Matrix4f texture = new Matrix4f();
  private final Vector4f white = new Vector4f(1, 1, 1, 1);

  public void tick() {
    var mc = Minecraft.getInstance();
    if (mc.level != level || !WaterVisualConfig.WAVES.get()) {
      close();
      level = mc.level;
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
    if (patch != null) {
      patch.close();
      patch = null;
    }
  }

  private void rebuild(int cx, int cz, int r, long ticks) {
    close();
    originX = cx - r;
    originZ = cz - r;
    radius = r;
    sampled = ticks;
    int cells = r, side = cells + 1, count = side * side;
    float[] positions = new float[count * 3];
    int[] colors = new int[count];
    boolean[] valid = new boolean[count];
    for (int z = 0; z < side; z++)
      for (int x = 0; x < side; x++) {
        int i = z * side + x, wx = originX + x * 2, wz = originZ + z * 2;
        positions[i * 3] = x * 2;
        positions[i * 3 + 2] = z * 2;
        var p = new BlockPos(wx, 0, wz);
        if (!level.hasChunkAt(p)) continue;
        var top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p);
        var water = top.below();
        if (!level.isEmptyBlock(top)
            || !level.getBiome(water).is(BiomeTags.IS_OCEAN)
            || !level.getFluidState(water).is(FluidTags.WATER)
            || !level.getFluidState(water.below(2)).is(FluidTags.WATER)) continue;
        boolean shore = false;
        for (var d : net.minecraft.core.Direction.Plane.HORIZONTAL)
          if (!level.getFluidState(water.relative(d, 2)).is(FluidTags.WATER)) {
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
    if (next == 0) return;
    patch =
        HorizonGpuMesh.upload(
            0,
            0,
            0,
            new HorizonMesh(
                positions,
                colors,
                Arrays.copyOf(indices, next),
                new int[count],
                new short[0],
                level.getMinY(),
                level.getMaxY()),
            false);
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null || !WaterVisualConfig.WAVES.get()) return;
    if (level != mc.level) {
      close();
      level = mc.level;
    }
    int r = WaterVisualConfig.WAVE_RADIUS.get();
    var camera = event.getLevelRenderState().cameraRenderState.pos;
    int x = Math.floorDiv((int) Math.floor(camera.x), 4) * 4,
        z = Math.floorDiv((int) Math.floor(camera.z), 4) * 4;
    long ticks = level.getGameTime();
    if (ticks - sampled >= 10 || radius != r || x != originX + r || z != originZ + r)
      rebuild(x, z, r, ticks);
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
