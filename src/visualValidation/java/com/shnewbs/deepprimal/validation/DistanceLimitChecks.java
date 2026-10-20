package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.DistancePolicy;
import net.minecraft.server.level.ServerLevel;

/** Exercise Mojang's actual tracker and signed-byte packet without allocating a 125-chunk world. */
final class DistanceLimitChecks {
  static void run(ServerLevel level) {
    try {
      var type =
          Class.forName(
              "net.minecraft.server.level.DistanceManager$FixedPlayerDistanceChunkTracker");
      var ctor =
          type.getDeclaredConstructor(net.minecraft.server.level.DistanceManager.class, int.class);
      ctor.setAccessible(true);
      var tracker =
          ctor.newInstance(
              level.getChunkSource().chunkMap.getDistanceManager(), DistancePolicy.MAX_RENDER);
      var field = type.getDeclaredField("chunks");
      field.setAccessible(true);
      var chunks = (it.unimi.dsi.fastutil.longs.Long2ByteMap) field.get(tracker);
      if (chunks.defaultReturnValue() != 127)
        throw new AssertionError("Native tracker sentinel wrapped");
      var defaults = net.minecraft.server.level.ClientInformation.createDefault();
      var info =
          new net.minecraft.server.level.ClientInformation(
              defaults.language(),
              125,
              defaults.chatVisibility(),
              defaults.chatColors(),
              defaults.modelCustomisation(),
              defaults.mainHand(),
              defaults.textFilteringEnabled(),
              defaults.allowsListing(),
              defaults.particleStatus());
      var bytes = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
      try {
        info.write(bytes);
        if (new net.minecraft.server.level.ClientInformation(bytes).viewDistance() != 125)
          throw new AssertionError("Native distance packet wrapped");
      } finally {
        bytes.release();
      }
      DrainageServerChecks.record(
          "DISTANCE_LIMIT_PASS requested=125 sentinel=127 packetRoundTrip=true");
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private DistanceLimitChecks() {}
}
