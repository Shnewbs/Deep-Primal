package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.engine.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Optional paired-output evidence; the sampled chunk rectangle is not a closed regional ledger. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class LakeWaterSnapshot {
  private WatershedRegion.LakeProbe lake;
  private String initial;
  private int ticks;
  public LakeWaterSnapshot() { NeoForge.EVENT_BUS.register(this); }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    if (System.getenv("DEEPPRIMAL_LAKE_SNAPSHOT") == null) return;
    if (System.getenv("DEEPPRIMAL_LAKE_RECORD") == null) throw new AssertionError("Lake fixture required");
    var level = event.getServer().overworld();
    var g = (EngineChunkGenerator) level.getChunkSource().getGenerator();
    lake = g.geography().waterways(0, 0).lakeProbes().stream()
        .filter(p -> p.area() >= 3072 && p.area() < 16000 && p.level() > 110
            && g.geography().sample(p.x(), p.z()).ground() < p.level() - 3)
        .min(Comparator.comparingDouble(WatershedRegion.LakeProbe::area)).orElseThrow();
    initial = snapshot(level);
  }

  private String snapshot(ServerLevel level) throws Exception {
    var digest = MessageDigest.getInstance("SHA-256");
    var tuple = ByteBuffer.allocate(16);
    long total = 0, cells = 0;
    int chunks = 0;
    for (int z = (int) Math.floor(lake.minZ()) >> 4; z <= (int) Math.ceil(lake.maxZ()) >> 4; z++)
      for (int x = (int) Math.floor(lake.minX()) >> 4; x <= (int) Math.ceil(lake.maxX()) >> 4; x++) {
        var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
        if (chunk == null) throw new AssertionError("Snapshot must not force-load missing chunks");
        chunks++;
        var sections = chunk.getSections();
        for (int section = 0; section < sections.length; section++) {
          var blocks = sections[section];
          if (blocks.hasOnlyAir()) continue;
          for (int y = 0; y < 16; y++)
            for (int zz = 0; zz < 16; zz++)
              for (int xx = 0; xx < 16; xx++) {
                var state = blocks.getBlockState(xx, y, zz);
                if (!state.is(EngineRegistry.WATER_BLOCK.get())) continue;
                int amount = state.getValue(EngineFluid.UNITS);
                total += amount; cells++;
                tuple.clear();
                tuple.putLong(BlockPos.asLong(x * 16 + xx, chunk.getMinY() + section * 16 + y, z * 16 + zz));
                tuple.putInt(amount).putInt(state.getValue(EngineFluid.FLOW));
                digest.update(tuple.array());
              }
        }
      }
    return "{\"chunks\":" + chunks + ",\"cells\":" + cells + ",\"units\":" + total
        + ",\"waterStateSha256\":\"" + HexFormat.of().formatHex(digest.digest()) + "\"}";
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void post(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) throws Exception {
    if (lake == null) return;
    if (++ticks < Integer.parseInt(System.getenv("DEEPPRIMAL_LAKE_TICKS"))) return;
    String result = "{\"scope\":\"loaded lake rectangle, open boundaries; units and FLOW\",\"ticks\":"
        + ticks + ",\"initial\":" + initial + ",\"final\":" + snapshot(event.getServer().overworld()) + "}\n";
    Files.writeString(Path.of(System.getenv("DEEPPRIMAL_LAKE_SNAPSHOT")), result);
    System.out.println("LAKE_WATER_SNAPSHOT_PASS " + result.trim());
    lake = null;
  }
}
