package com.shnewbs.deepprimal.validation;

import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import java.nio.file.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class TerrainRevisionServerChecks {
  public TerrainRevisionServerChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)
      throws Exception {
    String expected = System.getenv("DEEPPRIMAL_TERRAIN_REVISION");
    if (expected == null) return;
    var server = event.getServer();
    var level = server.overworld();
    var generator = (EngineChunkGenerator) level.getChunkSource().getGenerator();
    int revision = Integer.parseInt(expected);
    if (generator.geography().revision != revision)
      throw new AssertionError("Saved terrain revision");
    var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var encoded = ChunkGenerator.CODEC.encodeStart(ops, generator).getOrThrow();
    if (!encoded
        .getAsJsonObject()
        .get("type")
        .getAsString()
        .equals("deepprimal:continental_v" + revision))
      throw new AssertionError("Versioned generator encoding: " + encoded);
    var restored = (EngineChunkGenerator) ChunkGenerator.CODEC.parse(ops, encoded).getOrThrow();
    if (restored.geography().revision != revision) throw new AssertionError("Codec lost revision");
    long began = System.nanoTime();
    for (int z = -1; z <= 1; z++) for (int x = -1; x <= 1; x++) level.getChunk(x, z);
    boolean cached =
        java.util.Arrays.stream(EngineChunkGenerator.class.getDeclaredMethods())
            .anyMatch(m -> m.getName().equals("column"));
    String expectedCache = System.getenv("DEEPPRIMAL_EXPECT_COLUMN_CACHE");
    if (expectedCache != null && cached != Boolean.parseBoolean(expectedCache))
      throw new AssertionError("Wrong runtime JAR in differential check");
    var digest = java.security.MessageDigest.getInstance("SHA-256");
    var hashes = new java.util.HashMap<net.minecraft.world.level.block.state.BlockState, byte[]>();
    for (int z = -1; z <= 1; z++)
      for (int x = -1; x <= 1; x++) {
        var chunk = level.getChunk(x, z);
        for (var section : chunk.getSections())
          for (int y = 0; y < 16; y++)
            for (int zz = 0; zz < 16; zz++)
              for (int xx = 0; xx < 16; xx++) {
                var state = section.getBlockState(xx, y, zz);
                digest.update(
                    hashes.computeIfAbsent(
                        state,
                        st ->
                            (st.toString() + "\n")
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
              }
      }
    String blockDigest = java.util.HexFormat.of().formatHex(digest.digest());
    String result =
        "TERRAIN_REVISION_NATIVE_PASS revision="
            + revision
            + " codecRoundTrip=true chunks=9 generationMs="
            + (System.nanoTime() - began) / 1e6
            + " columnReuse="
            + cached
            + " blockDigest="
            + blockDigest
            + " minY="
            + level.getMinY()
            + " height="
            + level.getHeight()
            + "\n";
    Path out = Path.of(System.getenv("DEEPPRIMAL_TERRAIN_RECORD"));
    Files.createDirectories(out.getParent());
    Files.writeString(out, result);
    System.out.print(result);
    server.halt(false);
  }
}
