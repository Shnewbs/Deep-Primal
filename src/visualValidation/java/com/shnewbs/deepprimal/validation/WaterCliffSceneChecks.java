package com.shnewbs.deepprimal.validation;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.settings.*;
import java.lang.reflect.Field;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;

/** Disposable client-only cliff geometry in a real integrated-world renderer; never shipped. */
final class WaterCliffSceneChecks {
  private static int stage, ticks, frames;
  private static volatile boolean prepared;
  private static final List<BlockPos> water = new ArrayList<>();
  private static final List<Long> timings = new ArrayList<>();
  private static long comparisons, mismatches;
  private static float maximumGap, maximumHorizontalGap;
  private record ShownVertex(float x, float y, float z) {}
  private static final Set<Long> pagesSeen = new HashSet<>();

  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var f = type.getDeclaredField(name); f.setAccessible(true); return f;
  }

  private static void fill(Minecraft mc, int units) {
    var state = EngineRegistry.WATER_BLOCK.get().defaultBlockState()
        .setValue(EngineFluid.UNITS, units).setValue(EngineFluid.FLOW, 4);
    for (var p : water) mc.level.setBlock(p, state, 2 | 16);
  }

  static void tick(Minecraft mc) {
    ticks++;
    if (stage == 0) {
      WaterVisualConfig.INTERPOLATE.set(true);
      WaterVisualConfig.SMOOTH.set(true);
      LodConfig.ENABLED.set(false);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      mc.options.hideGui = true;
      mc.getSingleplayerServer().execute(() -> {
        var s = mc.getSingleplayerServer(); var src = s.createCommandSourceStack();
        for (String command : new String[] {"gamemode spectator @a", "time set noon",
            "weather clear", "tp @a 27 535 39 150 26"})
          s.getCommands().performPrefixedCommand(src, command);
        prepared = true;
      });
      stage = 1; ticks = 0;
      return;
    }
    if (stage == 1 && prepared && ticks > 80) {
      checkCeiling(mc);
      for (int z = 0; z < 30; z++) for (int x = 0; x < 22; x++) {
        int bed = x < 11 ? (z < 9 ? 527 : 510) : 527 - Math.max(0, (z - 3) / 2);
        for (int y = 508; y <= 532; y++)
          mc.level.setBlock(new BlockPos(x, y, z),
              y <= bed ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2 | 16);
      }
      for (int x = 4; x <= 7; x++) {
        for (int z = 3; z <= 8; z++) water.add(new BlockPos(x, 528, z));
        for (int y = 511; y <= 528; y++) water.add(new BlockPos(x, y, 9));
        for (int z = 10; z <= 24; z++) water.add(new BlockPos(x, 511, z));
      }
      for (int z = 3; z <= 26; z++) for (int x = 14; x <= 17; x++)
        water.add(new BlockPos(x, 528 - (z - 3) / 2, z));
      fill(mc, 8);
      stage = 2; ticks = 0;
      return;
    }
    if (stage == 2 && ticks > 100 && AnimatedWaterRenderer.INSTANCE.residentBytes > 0) {
      capture(mc, "cliff-before.png", false);
      stage = 3; ticks = 0;
      return;
    }
    if (stage == 3) {
      // Slower backends still need 100 moving frames. Extend the fixture, with a tick limit.
      if ((ticks <= 100 || frames < 100) && ticks <= 360 && ticks % 4 == 0)
        fill(mc, (ticks / 4 & 1) == 0 ? 8 : 16);
      if (ticks == 40) capture(mc, "cliff-moving.png", false);
      if (ticks > 120 && (frames >= 100 || ticks > 400)) {
        try {
          if (frames < 100 || comparisons < 100 || pagesSeen.size() < 2 || mismatches != 0)
            throw new AssertionError("Live cliff coverage/continuity failed: frames=" + frames
                + " comparisons=" + comparisons + " pages=" + pagesSeen.size()
                + " mismatches=" + mismatches + " maximumGap=" + maximumGap
                + " maximumHorizontalGap=" + maximumHorizontalGap);
          var result = new LinkedHashMap<String, Object>();
          result.put("backend", RenderSystem.getDevice().getBackendName());
          result.put("frames", frames); result.put("sharedCornerComparisons", comparisons);
          result.put("mismatches", mismatches); result.put("maximumGapBlocks", maximumGap);
          result.put("maximumHorizontalGapBlocks", maximumHorizontalGap);
          result.put("pages", pagesSeen.size()); result.put("waterCells", water.size());
          result.put("residentWaterBytes", AnimatedWaterRenderer.INSTANCE.residentBytes);
          timings.sort(Long::compare);
          result.put("heightPublicationP95Millis", timings.get(timings.size() * 95 / 100) / 1_000_000d);
          result.put("scope", "Controlled client-only cliff and cascade in a live integrated world; "
              + "per-frame opaque top CPU upload mirrors and rendered screenshots, not a world solver or pixel-difference test.");
          if (RenderSystem.getDevice().getBackendName().equals("Vulkan")) {
            var stats = (Map<?, ?>) Class.forName("net.vulkanmod.vulkan.VulkanStatistics")
                .getMethod("snapshot").invoke(null);
            int errors = ((Number) stats.get("validationErrors")).intValue();
            result.put("validationErrors", errors);
            if (errors != 0) throw new AssertionError("Vulkan validation errors: " + errors);
          }
          Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
          Files.createDirectories(out);
          Files.writeString(out.resolve("cliff-live-scene.json"),
              new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
          System.out.println("WATER_CLIFF_LIVE_SCENE_PASS frames=" + frames + " comparisons=" + comparisons
              + " mismatches=" + mismatches + " maximumGap=" + maximumGap
              + " maximumHorizontalGap=" + maximumHorizontalGap);
          stage = 4;
          capture(mc, "cliff-settled.png", true);
        } catch (Exception e) { throw new RuntimeException(e); }
      }
    }
  }

  private static void checkCeiling(Minecraft mc) {
    var pos = new BlockPos(24, mc.level.getMaxY(), 24);
    var previous = mc.level.getBlockState(pos);
    var state = EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS, 16);
    mc.level.setBlock(pos, state, 2 | 16);
    try {
      var renderer = AnimatedWaterRenderer.INSTANCE;
      var pageType = Class.forName(renderer.getClass().getName() + "$Page");
      var constructor = pageType.getDeclaredConstructor(long.class);
      constructor.setAccessible(true);
      Object page = constructor.newInstance(SectionPos.asLong(pos));
      var capture = renderer.getClass().getDeclaredMethod("capture", pageType);
      capture.setAccessible(true);
      var snapshot = (WaterPatchMesher.Snapshot) capture.invoke(renderer, page);
      int x = pos.getX() & 15, y = pos.getY() & 15, z = pos.getZ() & 15;
      if (snapshot.water(x, y, z) != 16 || snapshot.solid(x, y + 1, z))
        throw new AssertionError("A full ceiling water cell requires an open sky halo");
      int cell = x | z << 4 | y << 8;
      float[] mesh = WaterPatchMesher.build(snapshot, null, true).vertices();
      for (int n = 0; n < mesh.length; n += WaterPatchMesher.STRIDE) {
        if ((int) mesh[n + 9] == cell && WaterPatchMesher.face(mesh[n + 10]) == 0) {
          System.out.println("WATER_CEILING_NATIVE_PASS y=" + pos.getY());
          return;
        }
      }
      throw new AssertionError("Ceiling water top is missing from the mesh");
    } catch (ReflectiveOperationException failure) {
      throw new RuntimeException(failure);
    } finally {
      mc.level.setBlock(pos, previous, 2 | 16);
    }
  }

  static void frame() {
    if (stage != 3) return;
    try {
      var r = AnimatedWaterRenderer.INSTANCE;
      var type = r.getClass();
      long clock = field(type, "clock").getLong(r);
      @SuppressWarnings("unchecked")
      var pages = (Map<Long, Object>) field(type, "pages").get(r);
      var corners = new HashMap<String, ShownVertex>();
      for (var entry : pages.entrySet()) {
        var page = entry.getValue(); var pt = page.getClass();
        int px = field(pt, "x").getInt(page), py = field(pt, "y").getInt(page), pz = field(pt, "z").getInt(page);
        if (py < 496 || py > 528 || field(pt, "used").getLong(page) != clock) continue;
        byte[] bytes = (byte[]) field(pt, "presentation").get(page);
        if (bytes == null) continue;
        var b = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
        long uploaded = field(pt, "uploaded").getLong(page);
        for (int p = 0; p < bytes.length; p += 36) {
          if (!WaterCornerPacket.packed(b, p) || b.get(p + 32) != 127
              || WaterCornerPacket.opacityTarget(b, p) != 127) continue;
          float x = b.getFloat(p), z = b.getFloat(p + 8);
          int x2 = px * 2 + Math.round(x * 2);
          int z2 = pz * 2 + Math.round(z * 2);
          int y = py + Byte.toUnsignedInt(b.get(p + 35));
          float t = Math.clamp((clock - uploaded - (byte) b.getShort(p + 26)) / 100f, 0, 1);
          float shown = py + b.getFloat(p + 4) + b.getShort(p + 24) / 16384f * (1 - t);
          var vertex = new ShownVertex(px + x, shown, pz + z);
          var other = corners.putIfAbsent(x2 + ":" + y + ":" + z2, vertex);
          if (other != null) {
            comparisons++;
            float gap = Math.abs(shown - other.y());
            float horizontalGap = (float) Math.hypot(vertex.x() - other.x(), vertex.z() - other.z());
            maximumGap = Math.max(maximumGap, gap);
            maximumHorizontalGap = Math.max(maximumHorizontalGap, horizontalGap);
            if (gap > 1 / 16384f + .000001f || horizontalGap > .00001f) mismatches++;
          }
          pagesSeen.add(entry.getKey());
        }
      }
      timings.add(r.lastHeightBoundaryNanos);
      frames++;
    } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
  }

  private static void capture(Minecraft mc, String name, boolean stop) {
    var out = Path.of(System.getProperty("deepprimal.validationCapture"));
    try { Files.createDirectories(out.resolve("screenshots")); }
    catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    Screenshot.grab(out.toFile(), name, mc.getMainRenderTarget(), 1,
        message -> { if (stop) mc.execute(() -> {
          for (String expected : new String[] {"cliff-before.png", "cliff-moving.png", "cliff-settled.png"})
            if (!Files.isRegularFile(out.resolve("screenshots").resolve(expected)))
              throw new AssertionError("Missing rendered cliff capture: " + expected);
          mc.stop();
        }); });
  }

  private WaterCliffSceneChecks() {}
}
