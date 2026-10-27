package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterContactBudget;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** Server-thread-only local chemistry. No water/block writes, reservoir credit or unloaded catch-up. */
public final class WaterContactRuntime {
  static final int VISITS_PER_TICK = 2048;
  static final long MASS_PER_CONTACT = WaterAccount.SCALE;
  private static final Direction[] DIRECTIONS = Direction.values();
  private static final WeakHashMap<ServerLevel, WaterContactBudget> BUDGETS = new WeakHashMap<>();

  /** Called by the finite solver after loaded-cell admission; the existing FIFO owns retry work. */
  public static void mix(ServerLevel level, BlockPos origin) {
    if (!level.getServer().isSameThread())
      throw new IllegalStateException("Water contact requires the server thread");
    long now = level.getGameTime();
    WaterContactBudget budget = BUDGETS.computeIfAbsent(level,
        unused -> new WaterContactBudget(VISITS_PER_TICK));
    budget.beginTick(now);
    // Rotate the first direction; truncated visits must not permanently prefer one face.
    int rotation = (int) Math.floorMod(now, (long) DIRECTIONS.length);
    for (int n = 0; n < DIRECTIONS.length; n++) {
      if (!budget.tryVisit()) {
        retryOnNextTick(level, origin);
        return;
      }
      BlockPos neighbor = origin.relative(DIRECTIONS[(rotation + n) % DIRECTIONS.length]);
      if (!loaded(level, origin) || !loaded(level, neighbor)) continue;
      var a = level.getBlockState(origin);
      // A native read callback may unload the other chunk after the initial pair check.
      if (!loaded(level, neighbor)) continue;
      var b = level.getBlockState(neighbor);
      if (!a.is(EngineRegistry.WATER_BLOCK.get()) || !b.is(EngineRegistry.WATER_BLOCK.get())) continue;
      // A partial lower cell has an air gap to the cell above, even if the visual mesh bridges it.
      if (origin.getY() != neighbor.getY()) {
        var lower = origin.getY() < neighbor.getY() ? a : b;
        if (lower.getValue(EngineFluid.UNITS) != 16) continue;
      }
      int status = budget.contactStatus(origin.asLong(), neighbor.asLong());
      if (status == WaterContactBudget.DEFER) {
        retryOnNextTick(level, origin);
        return;
      }
      if (status != WaterContactBudget.FIRST) continue;
      var beforeA = HydrologyRuntime.contents(level, origin, a);
      var beforeB = HydrologyRuntime.contents(level, neighbor, b);
      var from = new WaterAccount(4096, beforeA);
      var to = new WaterAccount(4096, beforeB);
      var moved = from.diffuseWith(to, MASS_PER_CONTACT, MASS_PER_CONTACT);
      if (moved.saltToOther() == 0 && moved.sedimentToOther() == 0) continue;
      if (!HydrologyRuntime.commitContact(level, origin, neighbor, a, b,
          beforeA, beforeB, from.snapshot(), to.snapshot())) {
        // The FIRST identity is held until tick rotation even when metadata commit fails.
        // Intra-tick FIFO activation alone can be consumed as DUPLICATE and lose the retry.
        retryOnNextTick(level, origin);
        return;
      }
      WaterRuntime.activate(level, origin);
      WaterRuntime.activate(level, neighbor);
    }
  }

  /** Persist a failed contact into the host's next-tick scheduler, not just today's FIFO. */
  private static void retryOnNextTick(ServerLevel level, BlockPos origin) {
    WaterRuntime.activate(level, origin);
    // An unloaded/removed origin is recovered by chunk-load or its owner's normal wake path.
    if (loaded(level, origin)
        && level.getBlockState(origin).is(EngineRegistry.WATER_BLOCK.get()))
      level.scheduleTick(origin, EngineRegistry.WATER.get(), 1);
  }

  private static boolean loaded(ServerLevel level, BlockPos p) {
    return level.isInWorldBounds(p) && level.hasChunkAt(p);
  }
  static int visits(ServerLevel level) {
    var b = BUDGETS.get(level);
    return b == null ? 0 : b.visits();
  }
  static int retainedEdges(ServerLevel level) {
    var b = BUDGETS.get(level);
    return b == null ? 0 : b.retainedContacts();
  }
  public static void forget(ServerLevel level) { BUDGETS.remove(level); }
  public static void clear() { BUDGETS.clear(); }
  private WaterContactRuntime() {}
}
