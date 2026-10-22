package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Actual generated water, materials, banks and connected paths. Never distributed. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class WatershedServerChecks {
  public WatershedServerChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent e) throws Exception {
    String destination = System.getenv("DEEPPRIMAL_WATERSHED_RECORD");
    if (destination == null) return;
    var level = e.getServer().overworld();
    var generator = (EngineChunkGenerator) level.getChunkSource().getGenerator();
    var terrain = generator.geography();
    if (terrain.revision != 3) throw new AssertionError("V3 required");
    long began = System.nanoTime();
    var region = terrain.waterways(0, 0);
    var sites = new ArrayList<int[]>();
    region.probes().stream()
        .filter(p -> p.source() && p.stage() > 110 && terrain.sample(p.x(), p.z()).speed() > 0)
        .limit(3)
        .forEach(p -> sites.add(new int[] {(int) Math.rint(p.x()), (int) Math.rint(p.z())}));
    region.lakeProbes().stream()
        .filter(p -> p.area() < 30000)
        .limit(2)
        .forEach(p -> sites.add(new int[] {(int) Math.rint(p.x()), (int) Math.rint(p.z())}));
    int wet = 0, dry = 0, trees = 0, sealedBanks = 0, columnChecks = 0;
    var pos = new BlockPos.MutableBlockPos();
    for (var site : sites)
      for (int z = site[1] - 16; z < site[1] + 16; z++)
        for (int x = site[0] - 16; x < site[0] + 16; x++) {
          var s = terrain.sample(x, z);
          var chunk = level.getChunk(x >> 4, z >> 4);
          int bed = (int) Math.floor(s.ground());
          if (s.wet()) {
            for (int y = bed + 1; y < s.water(); y++) {
              var actual = chunk.getBlockState(pos.set(x, y, z));
              int expected = (int) Math.clamp(Math.round((s.water() - y) * 16), 1, 16);
              if (!actual.is(EngineRegistry.WATER_BLOCK.get())
                  || actual.getValue(EngineFluid.UNITS) != expected)
                throw new AssertionError(
                    "Generated fluid mismatch " + pos + " " + s + " " + actual);
              wet++;
            }
            if (s.speed() > 0 && s.water() > 110)
              for (Direction d : Direction.Plane.HORIZONTAL) {
                int nx = x + d.getStepX(), nz = z + d.getStepZ();
                var neighbor = terrain.sample(nx, nz);
                if (neighbor.wet()) continue;
                var bank = level.getBlockState(pos.set(nx, (int) Math.ceil(s.water()) - 1, nz));
                if (!bank.isSolid())
                  throw new AssertionError(
                      "Open native bank "
                          + pos
                          + " head="
                          + s.water()
                          + " ground="
                          + neighbor.ground());
                sealedBanks++;
              }
          } else dry++;
          if (Math.floorMod(x, 8) == 0 && Math.floorMod(z, 8) == 0) {
            var column = generator.getBaseColumn(x, z, level, level.getChunkSource().randomState());
            for (int y = bed - 5; y <= bed; y++)
              if (!column.getBlock(y).equals(chunk.getBlockState(pos.set(x, y, z))))
                throw new AssertionError("Shaped column/chunk mismatch " + pos);
            columnChecks++;
          }
          var stem = chunk.getBlockState(pos.set(x, bed + 1, z));
          if (stem.is(Blocks.OAK_LOG) || stem.is(Blocks.BIRCH_LOG) || stem.is(Blocks.SPRUCE_LOG)) {
            trees++;
            if (s.wet()) throw new AssertionError("Trunk in water");
          }
        }
    var line = region.centerline();
    int paths = 0;
    for (int n = 0; n < line.size() && paths < 20; n += 16) {
      var a = line.get(n);
      var b = line.get(n + 15);
      if (!a.source() || a.stage() < 110 || terrain.sample(a.x(), a.z()).speed() == 0) continue;
      var start = top(level, generator, (int) Math.rint(a.x()), (int) Math.rint(a.z()));
      var end = top(level, generator, (int) Math.rint(b.x()), (int) Math.rint(b.z()));
      if (!connected(level, start, end))
        throw new AssertionError("Disconnected native source " + start + " -> " + end);
      paths++;
    }
    for (int[] route : new int[][] {{-52, -1607, -20, -1639}, {-52, -1159, -20, -1159}}) {
      var start = top(level, generator, route[0], route[1]);
      var end = top(level, generator, route[2], route[3]);
      if (!connected(level, start, end))
        throw new AssertionError("Native regression " + start + " -> " + end);
      paths++;
    }
    // The revised coast moves river mouths. Audit every actual land-to-sea reach rather
    // than assuming an obsolete mouth coordinate must remain a river in new geography.
    int mouths = 0;
    for (int n = 0; n < line.size(); n += 16) {
      var a = line.get(n);
      var b = line.get(n + 15);
      if (a.stage() <= com.shnewbs.deepprimal.core.TerrainEngine.SEA + .01
          || b.stage() > com.shnewbs.deepprimal.core.TerrainEngine.SEA + .01
          || terrain.sample(a.x(), a.z()).speed() == 0) continue;
      var start = top(level, generator, (int) Math.rint(a.x()), (int) Math.rint(a.z()));
      var end = top(level, generator, (int) Math.rint(b.x()), (int) Math.rint(b.z()));
      if (!connected(level, start, end))
        throw new AssertionError("Disconnected native estuary " + start + " -> " + end);
      mouths++;
      paths++;
    }
    if (mouths == 0) throw new AssertionError("No native estuaries audited");
    if (paths < 10 || wet < 100 || dry < 100) throw new AssertionError("Native coverage");
    String report =
        "WATERSHED_NATIVE_PASS sites="
            + sites.size()
            + " waterCells="
            + wet
            + " dryColumns="
            + dry
            + " materialColumns="
            + columnChecks
            + " sealedBanks="
            + sealedBanks
            + " trunks="
            + trees
            + " faceConnectedSourceReaches="
            + paths
            + " estuaryReaches="
            + mouths
            + " generationAndAuditMs="
            + (System.nanoTime() - began) / 1e6
            + "\n";
    Files.writeString(Path.of(destination), report);
    System.out.print(report);
    e.getServer().halt(false);
  }

  private static BlockPos top(ServerLevel l, EngineChunkGenerator g, int x, int z) {
    l.getChunk(x >> 4, z >> 4);
    var s = g.geography().sample(x, z);
    var p = new BlockPos(x, (int) Math.ceil(s.water()) - 1, z);
    if (WaterRuntime.units(l, p) == 0) throw new AssertionError("Dry native centerline " + p);
    return p;
  }

  private static boolean connected(ServerLevel l, BlockPos a, BlockPos b) {
    int x0 = Math.min(a.getX(), b.getX()) - 5, x1 = Math.max(a.getX(), b.getX()) + 5;
    int z0 = Math.min(a.getZ(), b.getZ()) - 5, z1 = Math.max(a.getZ(), b.getZ()) + 5;
    int y0 = Math.min(a.getY(), b.getY()) - 10, y1 = Math.max(a.getY(), b.getY()) + 2;
    for (int z = z0 >> 4; z <= z1 >> 4; z++)
      for (int x = x0 >> 4; x <= x1 >> 4; x++) l.getChunk(x, z);
    var q = new ArrayDeque<BlockPos>();
    var seen = new HashSet<BlockPos>();
    q.add(a);
    seen.add(a);
    while (!q.isEmpty()) {
      var p = q.removeFirst();
      if (p.equals(b)) return true;
      for (var d : Direction.values()) {
        var n = p.relative(d);
        if (n.getX() < x0
            || n.getX() > x1
            || n.getZ() < z0
            || n.getZ() > z1
            || n.getY() < y0
            || n.getY() > y1
            || seen.contains(n)) continue;
        if (WaterRuntime.units(l, n) > 0) {
          seen.add(n);
          q.add(n);
        }
      }
    }
    return false;
  }
}
