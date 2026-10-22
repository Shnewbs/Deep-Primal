package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterTransport;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Server-thread-only, bounded finite transport. Unloaded borders are sealed, never force-loaded.
 */
public final class WaterRuntime {
  private static final Direction[] ALL = Direction.values();
  private static final Direction[] SIDES = {
    Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
  };
  private static final WeakHashMap<ServerLevel, Budget> BUDGETS = new WeakHashMap<>();
  private static final int PRESSURE_VISITS_PER_TICK = 4096, PRESSURE_SEARCH = 128;
  private static final int MAX_ACTIVE = 65536, DRAIN_VISITS_PER_TICK = 2048, DRAIN_SEARCH = 128;
  // Three transport substeps per 50 ms host tick: nominal 60 Hz solver resolution.
  // World commits stay on the owning thread; presentation interpolates every rendered frame.
  public static final int SUBSTEPS = 3;
  private static final int CELLS_PER_SUBSTEP = 2048;
  private static final int TICKS_PER_LEVEL = CELLS_PER_SUBSTEP * SUBSTEPS;
  private static final int SIDE_TRANSFER = 2, PRESSURE_TRANSFER = 4;

  private static final class Budget {
    long time = Long.MIN_VALUE;
    int count, pressureVisits, drainVisits, transfers;
    final long[] searchQueue = new long[PRESSURE_SEARCH];
    final int[] firstStep = new int[PRESSURE_SEARCH];
    final LongOpenHashSet seen = new LongOpenHashSet(PRESSURE_SEARCH);
    int pressureLimit = PRESSURE_VISITS_PER_TICK, drainLimit = DRAIN_VISITS_PER_TICK;
    boolean stepped;
    final LinkedHashSet<BlockPos> active = new LinkedHashSet<>();
    final LinkedHashSet<BlockPos> pressure = new LinkedHashSet<>();
  }

  public static int units(ServerLevel l, BlockPos p) {
    var s = l.getBlockState(p);
    return s.is(EngineRegistry.WATER_BLOCK.get()) ? s.getValue(EngineFluid.UNITS) : 0;
  }

  private static boolean open(ServerLevel l, BlockPos p) {
    if (!l.isInWorldBounds(p) || !l.hasChunkAt(p)) return false;
    var s = l.getBlockState(p);
    return s.isAir()
        || s.is(EngineRegistry.WATER_BLOCK.get())
        || (s.canBeReplaced()
            && s.getFluidState().isEmpty()
            && s.getCollisionShape(l, p).isEmpty());
  }

  /** Cheap activation predicate for loaded generated cells; stable ocean interiors stay asleep. */
  public static boolean needsWake(ServerLevel l, BlockPos p) {
    int amount = units(l, p);
    if (amount == 0) return false;
    if (open(l, p.below()) && units(l, p.below()) < WaterTransport.CAPACITY) return true;
    for (Direction d : SIDES) {
      BlockPos q = p.relative(d);
      if (open(l, q)
          && (amount > units(l, q) + 1 || (amount == 1 && units(l, q) < WaterTransport.CAPACITY)))
        return true;
    }
    // One corner of an otherwise uniform free surface can test submerged communication.
    // This also covers narrow sealed vessels without queueing every stable ocean cell.
    return units(l, p.below()) > 0
        && surface(l, p, amount)
        && (!open(l, p.west()) || units(l, p.west()) != amount)
        && (!open(l, p.north()) || units(l, p.north()) != amount);
  }

  private static boolean surface(ServerLevel l, BlockPos p, int amount) {
    // A partial cell has a free surface inside its own block, even beneath a solid ceiling.
    return amount < WaterTransport.CAPACITY || (open(l, p.above()) && units(l, p.above()) == 0);
  }

  private static net.minecraft.world.level.block.state.BlockState state(
      net.minecraft.world.level.block.state.BlockState old, int units, int flow) {
    if (units == 0) return Blocks.AIR.defaultBlockState();
    return (old.is(EngineRegistry.WATER_BLOCK.get())
            ? old
            : EngineRegistry.WATER_BLOCK.get().defaultBlockState())
        .setValue(EngineFluid.UNITS, units)
        .setValue(EngineFluid.FLOW, flow);
  }

  private static void wake(ServerLevel l, BlockPos p) {
    if (units(l, p) > 0) activate(l, p);
    for (Direction d : ALL) {
      BlockPos q = p.relative(d);
      if (l.isInWorldBounds(q) && l.hasChunkAt(q)) {
        l.neighborChanged(q, EngineRegistry.WATER_BLOCK.get(), null);
        if (units(l, q) > 0) activate(l, q);
      }
    }
  }

  private static boolean transfer(
      ServerLevel l, BlockPos a, BlockPos b, WaterTransport.Exchange exchange, int flow) {
    if (exchange.moved() == 0) return false;
    var beforeA = l.getBlockState(a);
    var beforeB = l.getBlockState(b);
    // Commit both volumes before notifying neighbors; failed writes retain the original volume.
    if (!l.setBlock(a, state(beforeA, exchange.from(), flow), 2 | 16)) return false;
    if (!l.setBlock(b, state(beforeB, exchange.to(), flow), 2 | 16)) {
      l.setBlock(a, beforeA, 2 | 16);
      return false;
    }
    if (!beforeB.isAir() && !beforeB.is(EngineRegistry.WATER_BLOCK.get()))
      net.minecraft.world.level.block.Block.dropResources(beforeB, l, b);
    budget(l).transfers++;
    wake(l, a);
    wake(l, b);
    return true;
  }

  private static Budget budget(ServerLevel level) {
    Budget budget = BUDGETS.computeIfAbsent(level, k -> new Budget());
    if (budget.time != level.getGameTime()) {
      budget.time = level.getGameTime();
      budget.count = budget.pressureVisits = budget.drainVisits = budget.transfers = 0;
      budget.pressureLimit = PRESSURE_VISITS_PER_TICK;
      budget.drainLimit = DRAIN_VISITS_PER_TICK;
      budget.stepped = false;
    }
    return budget;
  }

  /** Coalesced FIFO work persists across ticks; overflow remains in the native wake queue. */
  public static void activate(ServerLevel level, BlockPos p) {
    if (!level.isInWorldBounds(p) || !level.hasChunkAt(p)) return;
    Budget budget = budget(level);
    if (budget.active.contains(p)) return;
    if (budget.active.size() >= MAX_ACTIVE) {
      level.scheduleTick(p, EngineRegistry.WATER.get(), 1);
      return;
    }
    budget.active.add(p.immutable());
  }

  public static int queued(ServerLevel level) {
    Budget b = BUDGETS.get(level);
    return b == null ? 0 : b.active.size() + b.pressure.size();
  }

  public record Diagnostics(
      int active,
      int pressure,
      int processed,
      int pressureVisits,
      int drainVisits,
      int transfers) {}

  public static Diagnostics diagnostics(ServerLevel level) {
    Budget b = BUDGETS.get(level);
    return b == null
        ? new Diagnostics(0, 0, 0, 0, 0, 0)
        : new Diagnostics(
            b.active.size(),
            b.pressure.size(),
            b.count,
            b.pressureVisits,
            b.drainVisits,
            b.transfers);
  }

  /**
   * Run newly activated cells again within this host tick instead of waiting another 50 ms. The
   * persistent FIFO contains only unresolved work. A stable world does no transport work; a large
   * connected flood cannot monopolize the server until the entire ocean equilibrates.
   */
  public static void step(ServerLevel level) {
    Budget budget = budget(level);
    if (budget.stepped) return;
    budget.stepped = true;
    for (int phase = 0; phase < SUBSTEPS; phase++) {
      budget.pressureLimit = PRESSURE_VISITS_PER_TICK * (phase + 1);
      budget.drainLimit = DRAIN_VISITS_PER_TICK * (phase + 1);
      int remaining =
          Math.min(
              budget.active.size(), Math.min(CELLS_PER_SUBSTEP, TICKS_PER_LEVEL - budget.count));
      while (remaining-- > 0 && !budget.active.isEmpty()) {
        BlockPos p = budget.active.removeFirst();
        advance(level, p, budget);
      }
      int pending = budget.pressure.size();
      while (pending-- > 0
          && !budget.pressure.isEmpty()
          && budget.pressureLimit - budget.pressureVisits >= PRESSURE_SEARCH) {
        BlockPos p = budget.pressure.removeFirst();
        if (level.isInWorldBounds(p) && level.hasChunkAt(p) && units(level, p) > 0)
          equalize(level, p, budget);
      }
      if (budget.active.isEmpty() && budget.pressure.isEmpty()) break;
    }
  }

  /** Single-cell entry retained for adapters and focused native fixtures. */
  public static void tick(ServerLevel level, BlockPos p) {
    Budget b = budget(level);
    if (b.count >= TICKS_PER_LEVEL) {
      activate(level, p);
      return;
    }
    advance(level, p, b);
    if (b.pressureLimit - b.pressureVisits >= PRESSURE_SEARCH && b.pressure.remove(p))
      equalize(level, p, b);
  }

  private static void advance(ServerLevel level, BlockPos p, Budget budget) {
    budget.count++;
    if (!level.isInWorldBounds(p) || !level.hasChunkAt(p) || units(level, p) == 0) return;
    boolean moved = move(level, p, p.below(), true);
    boolean triedDrain = !moved && units(level, p) <= SIDE_TRANSFER;
    if (triedDrain) moved = drain(level, p, budget);
    int rotate = Math.floorMod(p.hashCode() + (int) level.getGameTime(), 4);
    for (int n = 0; n < 4 && units(level, p) > 1; n++)
      moved |= move(level, p, p.relative(SIDES[(rotate + n) & 3]), false);
    if (!moved && units(level, p) > 0) moved = localPressure(level, p);
    if (!moved && !triedDrain && units(level, p) > 0) moved = drain(level, p, budget);
    if (!moved
        && units(level, p) > 0
        && units(level, p.below()) > 0
        && surface(level, p, units(level, p))) {
      if (budget.pressure.size() < MAX_ACTIVE) budget.pressure.add(p.immutable());
      else activate(level, p);
    }
  }

  /** Immediate L-shaped communication avoids queueing the last unit behind unrelated pools. */
  private static boolean localPressure(ServerLevel level, BlockPos origin) {
    BlockPos below = origin.below();
    if (!level.isInWorldBounds(below)
        || !level.hasChunkAt(below)
        || units(level, below) != WaterTransport.CAPACITY) return false;
    for (Direction side : SIDES) {
      BlockPos outlet = below.relative(side);
      if (!open(level, outlet)) continue;
      int from = units(level, origin), to = units(level, outlet);
      var exchange = WaterTransport.pressure(origin.getY(), from, outlet.getY(), to);
      if (exchange.moved() > PRESSURE_TRANSFER)
        exchange =
            new WaterTransport.Exchange(
                from - PRESSURE_TRANSFER, to + PRESSURE_TRANSFER, PRESSURE_TRANSFER);
      if (transfer(level, origin, outlet, exchange, 16)) return true;
    }
    return false;
  }

  /** A thin film follows a bounded downhill path instead of stopping at one unit. */
  private static boolean drain(ServerLevel l, BlockPos origin, Budget budget) {
    // Full submerged cells with no lateral capacity have no horizontal route to begin.
    // Check before budget admission: they must not requeue merely because other films used it.
    boolean exit = false;
    for (Direction d : SIDES) {
      BlockPos q = origin.relative(d);
      if (open(l, q) && units(l, q) < WaterTransport.CAPACITY) {
        exit = true;
        break;
      }
    }
    if (!exit) return false;
    if (budget.drainVisits >= budget.drainLimit) {
      activate(l, origin);
      return false;
    }
    int head = 0, tail = 0;
    var seen = budget.seen;
    seen.clear();
    seen.add(origin.asLong());
    long[] queue = budget.searchQueue;
    int[] first = budget.firstStep;
    var p = new BlockPos.MutableBlockPos();
    var q = new BlockPos.MutableBlockPos();
    for (int side = 0; side < SIDES.length; side++) {
      p.setWithOffset(origin, SIDES[side]);
      if (open(l, p) && units(l, p) < WaterTransport.CAPACITY) {
        long key = p.asLong();
        if (seen.add(key)) {
          queue[tail] = key;
          first[tail++] = side;
        }
      }
    }
    while (head < tail && budget.drainVisits < budget.drainLimit) {
      p.set(queue[head]);
      int direction = first[head++];
      budget.drainVisits++;
      q.set(p.getX(), p.getY() - 1, p.getZ());
      if (open(l, q) && units(l, q) < WaterTransport.CAPACITY) {
        BlockPos target = origin.relative(SIDES[direction]);
        int from = units(l, origin), to = units(l, target);
        int flow =
            switch (SIDES[direction]) {
              case EAST -> 0;
              case SOUTH -> 4;
              case WEST -> 8;
              default -> 12;
            };
        return transfer(l, origin, target, new WaterTransport.Exchange(from - 1, to + 1, 1), flow);
      }
      for (Direction d : SIDES) {
        if (seen.size() >= DRAIN_SEARCH) break;
        q.setWithOffset(p, d);
        long key = q.asLong();
        if (!seen.contains(key) && open(l, q)) {
          seen.add(key);
          queue[tail] = key;
          first[tail++] = direction;
        }
      }
    }
    if (head < tail) activate(l, origin);
    return false;
  }

  private static boolean move(ServerLevel l, BlockPos a, BlockPos b, boolean down) {
    if (!open(l, b)) return false;
    var e = WaterTransport.exchange(units(l, a), units(l, b), down);
    if (e.moved() == 0) return false;
    int dx = b.getX() - a.getX(), dz = b.getZ() - a.getZ();
    int flow = down ? 16 : dx > 0 ? 0 : dz > 0 ? 4 : dx < 0 ? 8 : 12;
    if (!down && e.moved() > SIDE_TRANSFER)
      e =
          new WaterTransport.Exchange(
              units(l, a) - SIDE_TRANSFER, units(l, b) + SIDE_TRANSFER, SIDE_TRANSFER);
    return transfer(l, a, b, e, flow);
  }

  /** Bounded connected-water search; sealed and unloaded cells cannot become outlets. */
  private static boolean equalize(ServerLevel l, BlockPos origin, Budget budget) {
    if (budget.pressureVisits >= budget.pressureLimit) {
      budget.pressure.add(origin.immutable());
      return false;
    }
    int head = 0, tail = 1;
    var seen = budget.seen;
    seen.clear();
    seen.add(origin.asLong());
    long[] queue = budget.searchQueue;
    queue[0] = origin.asLong();
    var p = new BlockPos.MutableBlockPos();
    var q = new BlockPos.MutableBlockPos();
    BlockPos high = null, low = null;
    long highHead = Long.MIN_VALUE, lowHead = Long.MAX_VALUE;
    while (head < tail && budget.pressureVisits < budget.pressureLimit) {
      p.set(queue[head++]);
      budget.pressureVisits++;
      int amount = units(l, p);
      if (amount == 0) continue;
      if (surface(l, p, amount)) {
        long value = (long) p.getY() * WaterTransport.CAPACITY + amount;
        if (value > highHead) {
          highHead = value;
          high = p.immutable();
        }
        if (value < lowHead) {
          lowHead = value;
          low = amount == WaterTransport.CAPACITY ? p.above() : p.immutable();
        }
      }
      for (Direction d : ALL) {
        if (tail >= PRESSURE_SEARCH) break;
        q.setWithOffset(p, d);
        long key = q.asLong();
        if (!seen.contains(key) && l.isInWorldBounds(q) && l.hasChunkAt(q) && units(l, q) > 0) {
          seen.add(key);
          queue[tail++] = key;
        }
      }
    }
    if (head < tail) budget.pressure.add(origin.immutable());
    if (high == null || low == null || high.equals(low)) return false;
    var exchange = WaterTransport.pressure(high.getY(), units(l, high), low.getY(), units(l, low));
    if (exchange.moved() > PRESSURE_TRANSFER)
      exchange =
          new WaterTransport.Exchange(
              units(l, high) - PRESSURE_TRANSFER,
              units(l, low) + PRESSURE_TRANSFER,
              PRESSURE_TRANSFER);
    return transfer(l, high, low, exchange, 16);
  }

  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent
    public void tick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
      if (event.getLevel() instanceof ServerLevel level) step(level);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
      BUDGETS.clear();
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void unloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
      if (event.getLevel() instanceof ServerLevel level) BUDGETS.remove(level);
    }
  }

  private WaterRuntime() {}
}
