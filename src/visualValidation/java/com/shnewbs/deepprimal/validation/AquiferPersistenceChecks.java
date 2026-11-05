package com.shnewbs.deepprimal.validation;

import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.engine.HydrologyData;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Private packaged regression: extreme persisted clocks, not a normal-world stall claim. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class AquiferPersistenceChecks {
  public AquiferPersistenceChecks() { NeoForge.EVENT_BUS.register(this); }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String output = System.getenv("DEEPPRIMAL_AQUIFER_PERSISTENCE_RECORD");
    if (output == null) return;
    String mode = System.getenv("DEEPPRIMAL_AQUIFER_PERSISTENCE_MODE");
    require(java.util.Set.of("fresh", "reopen", "verify").contains(mode), "Explicit fixture mode");
    var level = event.getServer().overworld();
    var data = HydrologyData.get(level);
    for (var type : java.util.List.of(Aquifer.class, HydrologyData.class)) {
      String origin = type.getProtectionDomain().getCodeSource().getLocation().toString();
      require(!origin.contains("classes/java/main"), "Must exercise packaged runtime: " + origin);
      System.out.println("AQUIFER_PERSISTENCE_ORIGIN " + type.getName() + " " + origin);
    }
    for (int depth : new int[] {1, Aquifer.MAX_DEPTH}) {
      int x = 1_000_000 + depth * 64;
      int z = -1_000_000;
      long key = HydrologyData.key(x, z);
      require(!level.hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)), "Remote fixture is unloaded");
      var ground = new WaterAccount.Parcel(Aquifer.CAPACITY * depth, 71, 29);
      var snow = new WaterAccount.Parcel(8193, 101, 37);
      if (mode.equals("fresh")) {
        require(!data.aquifers.containsKey(key), "Fresh fixture may not replace a saved account");
        data.aquifers.put(key, new Aquifer(ground, snow, 0, false, Long.MIN_VALUE, 0, depth));
      }
      var aquifer = data.aquifers.get(key);
      require(aquifer != null && aquifer.depth() == depth, "Saved aquifer/depth survives process restart");
      require(aquifer.ground.snapshot().equals(ground) && aquifer.snow.snapshot().equals(snow),
          "Saved exact water, salt and sediment");
      int expectedAge = mode.equals("verify") ? 1 : 0;
      long expectedTick = mode.equals("verify") ? 0 : Long.MIN_VALUE;
      require(aquifer.lastTick() == expectedTick && aquifer.wetSteps() == expectedAge
          && aquifer.dynamicWetSteps() == expectedAge && !aquifer.active(), "Persisted clock and age");
      var encoded = HydrologyData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
      var decoded = HydrologyData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().aquifers.get(key);
      require(decoded.lastTick() == expectedTick && decoded.depth() == depth
          && decoded.ground.snapshot().equals(ground) && decoded.snow.snapshot().equals(snow),
          "Codec round trip preserves signed clock and quantities");
      if (!mode.equals("fresh")) {
        long now = mode.equals("reopen") ? 0 : 20;
        require(aquifer.step(now), "Eligible persisted elapsed time must advance");
        require(aquifer.wetSteps() == expectedAge + 1 && aquifer.dynamicWetSteps() == expectedAge + 1
            && aquifer.lastTick() == now, "Long unload gap advances one fixed step, never catches up");
        require(!aquifer.step(now) && !aquifer.step(now + 19), "Duplicate and early visits do not advance");
        require(aquifer.ground.snapshot().equals(ground) && aquifer.snow.snapshot().equals(snow),
            "Aging must not create or remove water or constituents");
      }
      require(!level.hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)), "No forced remote chunk load");
    }
    data.setDirty();
    String record = "AQUIFER_PERSISTENCE_PASS mode=" + mode
        + " depths=1,64 exactConstituents=true boundedStep=true noForceLoad=true\n";
    Files.writeString(Path.of(output), record);
    System.out.print(record);
  }
}
