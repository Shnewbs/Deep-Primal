package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.EngineWaterBlock;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import com.shnewbs.deepprimal.settings.LodConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;

/** Inspect generated chunks before a simulation tick, then capture their delivered client views. */
public final class ShorelineSceneChecks {
  private static final String MODE = System.getenv("DEEPPRIMAL_SHORELINE_SCENE");
  private record Site(String name, int x, int z) {}
  private record Survey(BlockPos target, int cells, long units, long hash, int pads, int reeds, int riffles, int reefs, int rocks) {}
  private static final Site[] SITES = {new Site("lake", -485, -1012), new Site("river", 822, 1089),
      new Site("reef", -188, -2084)};
  private static CompletableFuture<Survey> pending;
  private static int site, waiting, settled;
  private static boolean captured, moved;
  private static int eyeX, eyeY, eyeZ;
  private static final StringBuilder evidence = new StringBuilder();

  public static boolean enabled() { return "current".equals(MODE) || "legacy".equals(MODE); }
  private static void require(boolean ok, String why) { if (!ok) throw new AssertionError(why); }

  private static void controls(ServerLevel level) {
    var p = new BlockPos(3, 460, 3);
    level.getChunkAt(p);
    var water = EngineRegistry.WATER_BLOCK.get().defaultBlockState();
    var pad = Blocks.LILY_PAD.defaultBlockState();
    HydrologyRuntime.write(level, p, water);
    HydrologyRuntime.write(level, p.above(), pad);
    require(pad.canSurvive(level, p.above()), "Full freshwater must carry its pad");
    HydrologyRuntime.write(level, p, water.setValue(EngineFluid.UNITS, 8));
    require(level.getBlockState(p.above()).isAir() && !pad.canSurvive(level, p.above()), "Dropping water removed pad");
    HydrologyRuntime.write(level, p, water.setValue(EngineWaterBlock.OCEAN, true));
    require(!pad.canSurvive(level, p.above()), "Ocean boundary cannot carry a freshwater pad");
    HydrologyRuntime.write(level, p, water);
    HydrologyRuntime.write(level, p.above(), pad);
    require(HydrologyRuntime.pickup(level, p) != null && level.getBlockState(p.above()).isAir(), "Measured pickup removed pad");
    HydrologyRuntime.write(level, p, water);
    require(WaterEffects.landingEdge(level, p) == null, "Flat water has no waterfall spray");
    HydrologyRuntime.write(level, p.west(), water);
    HydrologyRuntime.write(level, p.west().above(), water);
    require(WaterEffects.landingEdge(level, p) == Direction.WEST, "Impact on the lower pool");
    HydrologyRuntime.write(level, p.below(), Blocks.ANDESITE.defaultBlockState());
    require(!WaterEffects.riffle(level, p, water), "Still water has no riffle");
    var fast = water.setValue(EngineFluid.FLOW, 0).setValue(EngineFluid.SPEED, 7);
    require(WaterEffects.riffle(level, p, fast), "Shallow moving water over stone");
    HydrologyRuntime.write(level, p.east(), Blocks.SAND.defaultBlockState());
    require(WaterEffects.shoreEdge(level, p) == Direction.EAST, "Wave reaches open bank");
    var units = new byte[18 * 18 * 18];
    var snapshot = new WaterPatchMesher.Snapshot(units, new byte[units.length], new int[units.length], new int[256]);
    units[WaterPatchMesher.index(15,15,8)] = 16;
    require(!WaterPatchMesher.fallingSide(snapshot,15,15,8,2), "One-cell pool side");
    units[WaterPatchMesher.index(15,16,8)] = 16;
    require(WaterPatchMesher.fallingSide(snapshot,15,15,8,2), "Top and east page halo carry curtain");
    snapshot.solid()[WaterPatchMesher.index(16,16,8)] = 1;
    require(!WaterPatchMesher.fallingSide(snapshot,15,15,8,2), "Rock wall masks stacked side");
  }

  private static boolean reef(BlockState b) {
    return b.is(Blocks.TUBE_CORAL_BLOCK) || b.is(Blocks.BRAIN_CORAL_BLOCK) || b.is(Blocks.HORN_CORAL_BLOCK);
  }

  private static Survey survey(ServerLevel level, Site s) {
    var generator = (EngineChunkGenerator)level.getChunkSource().getGenerator();
    boolean habitats = "current".equals(MODE);
    require(level.getSeed() == 20261020 && generator.shorelineHabitats() == habitats, "Seed and saved habitat option");
    if (site == 0) controls(level);
    var terrain = generator.geography();
    int cells = 0, pads = 0, reeds = 0, riffles = 0, reefs = 0, rocks = 0, best = Integer.MAX_VALUE;
    long units = 0, hash = 0xcbf29ce484222325L;
    BlockPos target = null;
    for (int dx = -18; dx <= 18; dx++) for (int dz = -18; dz <= 18; dz++) {
      int x = s.x + dx, z = s.z + dz;
      var sample = terrain.column(x,z);
      int bed = (int)Math.floor(sample.ground());
      int top = sample.wet() ? (int)Math.ceil(sample.water()) : bed + 1;
      var chunk = level.getChunk(x >> 4, z >> 4);
      var at = new BlockPos(x, bed, z);
      var floor = chunk.getBlockState(at);
      var cap = chunk.getBlockState(new BlockPos(x, top, z));
      boolean pad = cap.is(Blocks.LILY_PAD), coral = reef(floor);
      boolean riffle = sample.wet() && sample.speed() >= .32
          && (floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.MOSSY_COBBLESTONE));
      if (pad) { pads++; require(padSurvives(level, x, top, z), "Unsupported natural pad"); }
      if (cap.is(Blocks.SUGAR_CANE)) reeds++;
      if (riffle) riffles++;
      if (coral) reefs++;
      if (!sample.wet() && (cap.is(Blocks.MOSSY_COBBLESTONE) || cap.is(Blocks.ANDESITE))) rocks++;
      for (int y = bed + 1; y <= top; y++) {
        var b = chunk.getBlockState(new BlockPos(x,y,z));
        if (!b.is(EngineRegistry.WATER_BLOCK.get())) continue;
        int n = b.getValue(EngineFluid.UNITS);
        cells++; units += n;
        hash = (hash ^ BlockPos.asLong(x,y,z)) * 0x100000001b3L;
        int material = n | (b.getValue(EngineFluid.FLOW) << 5) | (b.getValue(EngineFluid.SPEED) << 10)
            | (b.getValue(EngineWaterBlock.OCEAN) ? 8192 : 0);
        hash = (hash ^ material) * 0x100000001b3L;
      }
      boolean feature = site == 0 ? pad : site == 1 ? riffle : coral;
      int distance = dx*dx+dz*dz;
      if (feature && distance < best) { best = distance; target = new BlockPos(x, site == 0 ? top : bed, z); }
    }
    if (target == null) target = new BlockPos(s.x, (int)Math.floor(terrain.column(s.x,s.z).ground()), s.z);
    var result = new Survey(target, cells, units, hash, pads, reeds, riffles, reefs, rocks);
    String line = s.name + " " + result;
    System.out.println("SHORELINE_SAMPLE " + MODE + " " + line);
    evidence.append(line).append('\n');
    require(cells > 0 && (!habitats || (site == 0 ? pads > 0 : site == 1 ? riffles > 0 : reefs > 0)), "Generated " + line);
    return result;
  }

  private static boolean padSurvives(ServerLevel level,int x,int y,int z) {
    var pos = new BlockPos(x,y,z);
    return level.getBlockState(pos).canSurvive(level,pos);
  }

  public static void tick(Minecraft mc) {
    if (mc.level == null || mc.player == null || mc.screen != null || mc.getSingleplayerServer() == null) return;
    var server = mc.getSingleplayerServer();
    if (site == SITES.length) {
      try {
        Files.writeString(Path.of(System.getProperty("deepprimal.validationCapture"), "shoreline-" + MODE + ".txt"), evidence);
      } catch (java.io.IOException e) { throw new IllegalStateException(e); }
      System.out.println("SHORELINE_NATIVE_PASS mode=" + MODE + " sites=" + site);
      mc.disconnectWithSavingScreen(); mc.stop(); return;
    }
    Site s = SITES[site];
    if (pending == null) {
      mc.options.hideGui = true;
      LodConfig.ENABLED.set(false);
      pending = server.submit(() -> {
        var commands = server.getCommands();
        var source = server.createCommandSourceStack();
        if (site == 0) {
          commands.performPrefixedCommand(source, "gamemode spectator @a");
          commands.performPrefixedCommand(source, "weather clear");
          commands.performPrefixedCommand(source, "time set noon");
        }
        return survey(server.overworld(), s);
      });
      return;
    }
    if (!pending.isDone()) return;
    var result = pending.join();
    var target = result.target();
    if (!moved) {
      int offset = site == 2 ? 3 : site == 0 ? 8 : 12;
      eyeX = target.getX() + offset; eyeZ = target.getZ() + offset;
      eyeY = site == 2 ? target.getY() + 3 : target.getY() + (site == 0 ? 6 : 11);
      server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
          String.format(Locale.ROOT, "tp @a %.2f %d %.2f 135 30", eyeX + .5, eyeY, eyeZ + .5)));
      moved = true;
    }
    require(++waiting < 1800, "Timed out awaiting " + s.name);
    var chunk = mc.level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, false);
    if (chunk == null || chunk instanceof EmptyLevelChunk || mc.player.blockPosition().distSqr(new BlockPos(eyeX,eyeY,eyeZ)) > 9) return;
    if (++settled < 95 || captured) return;
    captured = true;
    var output = Path.of(System.getProperty("deepprimal.validationCapture"));
    output.toFile().mkdirs();
    Screenshot.grab(output.toFile(), s.name + ".png", mc.getMainRenderTarget(), 1, message -> mc.execute(() -> {
      site++; moved = false; captured = false; pending = null; waiting = settled = 0;
    }));
  }

  private ShorelineSceneChecks() {}
}
