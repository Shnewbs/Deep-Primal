package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterTransport;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
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
  // A search retains its frontier between 128-node turns, not an unbounded flood per call.
  // Explicit residency caps keep independent components from retaining a whole ocean.
  private static final int MAX_PRESSURE_JOBS = 16, MAX_PRESSURE_NODES = 8192;
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
    final LinkedHashMap<Long, com.shnewbs.deepprimal.core.TerrainEngine.Sample> geography = new LinkedHashMap<>();
    final LongLinkedOpenHashSet active = new LongLinkedOpenHashSet();
    final LongLinkedOpenHashSet pressure = new LongLinkedOpenHashSet();
    final LinkedHashMap<BlockPos, PressureJob> searches = new LinkedHashMap<>();
    final HashMap<Long, HashSet<PressureJob>> watchedChunks = new HashMap<>();
    int pressureRestarts, pressureCompleted, pressureSaturated, maxPressureSlice, pressureCoalesced;
    int pressureHeadRefreshes, pressureHeadDeferrals;
  }

  private static final class PressureJob {
    final BlockPos origin;
    long[] queue = new long[PRESSURE_SEARCH];
    final LongOpenHashSet seen = new LongOpenHashSet(PRESSURE_SEARCH);
    final LongOpenHashSet chunks = new LongOpenHashSet();
    int head, tail = 1;
    BlockPos high, lowSurface;
    long highHead = Long.MIN_VALUE, lowHead = Long.MAX_VALUE;
    boolean invalidated, saturated, headsDirty, refreshing;
    int refreshes;

    PressureJob(BlockPos origin) {
      this.origin = origin.immutable();
      queue[0] = origin.asLong();
      seen.add(origin.asLong());
    }
  }

  private static void watch(Budget budget, PressureJob job, BlockPos p) {
    long key = net.minecraft.world.level.ChunkPos.pack(p.getX() >> 4, p.getZ() >> 4);
    if (job.chunks.add(key))
      budget.watchedChunks.computeIfAbsent(key, unused -> new HashSet<>()).add(job);
  }

  private static void finish(Budget budget, PressureJob job) {
    budget.searches.remove(job.origin, job);
    for (long key : job.chunks) {
      var watchers = budget.watchedChunks.get(key);
      if (watchers != null) {
        watchers.remove(job);
        if (watchers.isEmpty()) budget.watchedChunks.remove(key);
      }
    }
  }

  /** Called after real chunk writes, including flags that suppress neighbor notifications. */
  public static void changed(ServerLevel level, BlockPos p,
      net.minecraft.world.level.block.state.BlockState before,
      net.minecraft.world.level.block.state.BlockState after) {
    HydrologyRuntime.externalChange(level, p, before, after);
    Budget budget = BUDGETS.get(level);
    if (budget == null || budget.watchedChunks.isEmpty() || before == null || before == after) return;
    // Current-direction animation alone cannot change hydraulic connectivity or head.
    boolean volumeOnly = before.is(EngineRegistry.WATER_BLOCK.get())
        && after.is(EngineRegistry.WATER_BLOCK.get());
    if (volumeOnly && before.getValue(EngineFluid.UNITS).equals(after.getValue(EngineFluid.UNITS))) return;
    var watchers = budget.watchedChunks.get(
        net.minecraft.world.level.ChunkPos.pack(p.getX() >> 4, p.getZ() >> 4));
    if (watchers == null) return;
    var neighbor = new BlockPos.MutableBlockPos();
    for (var job : watchers) {
      if (job.invalidated) continue;
      // Unrelated movement elsewhere in the same chunk must not starve a long search.
      // A changed witness or its adjacent outlet/boundary can alter the retained graph.
      boolean relevant = job.seen.contains(p.asLong());
      for (Direction direction : ALL) {
        if (relevant) break;
        neighbor.setWithOffset(p, direction);
        relevant = job.seen.contains(neighbor.asLong());
      }
      // Positive-to-positive volume changes preserve the wet witness graph. Refresh
      // heads without throwing away the frontier; additions/removals still cancel it.
      if (relevant) {
        if (volumeOnly) job.headsDirty = true;
        else job.invalidated = true;
      }
    }
  }

  private static void invalidateChunk(Budget budget, long key) {
    var watchers = budget.watchedChunks.get(key);
    if (watchers != null) for (var job : watchers) job.invalidated = true;
  }

  public record PressureDiagnostics(int jobs, int retainedNodes, int watchedChunks, int restarts,
      int completed, int saturated, int maxSliceVisits, int coalesced,
      int headRefreshes, int headDeferrals) {}

  public static PressureDiagnostics pressureDiagnostics(ServerLevel level) {
    Budget budget = BUDGETS.get(level);
    if (budget == null) return new PressureDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    int nodes = 0;
    for (var job : budget.searches.values()) nodes += job.tail;
    return new PressureDiagnostics(budget.searches.size(), nodes, budget.watchedChunks.size(), budget.pressureRestarts,
        budget.pressureCompleted, budget.pressureSaturated, budget.maxPressureSlice,
        budget.pressureCoalesced, budget.pressureHeadRefreshes, budget.pressureHeadDeferrals);
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
    if (amount < WaterTransport.CAPACITY) return true;
    BlockPos above = p.above();
    if (!l.isInWorldBounds(above) || !l.hasChunkAt(above)) return false;
    // One loaded-state read is enough: EngineFluid always has at least one unit.
    var state = l.getBlockState(above);
    return state.isAir()
        || (!state.is(EngineRegistry.WATER_BLOCK.get()) && state.canBeReplaced()
            && state.getFluidState().isEmpty() && state.getCollisionShape(l, above).isEmpty());
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
    var contentsA = HydrologyRuntime.contents(l, a, beforeA);
    var contentsB = HydrologyRuntime.contents(l, b, beforeB);
    var transfer = HydrologyRuntime.prepareTransfer(contentsA, contentsB, exchange.moved());
    if (transfer == null) return false;
    // Commit both volumes before notifying neighbors; failed writes retain the original volume.
    if (!HydrologyRuntime.write(l, a, state(beforeA, exchange.from(), flow))) return false;
    if (!HydrologyRuntime.write(l, b, state(beforeB, exchange.to(), flow))) {
      HydrologyRuntime.write(l, a, beforeA);
      return false;
    }
    if (!beforeB.isAir() && !beforeB.is(EngineRegistry.WATER_BLOCK.get()))
      net.minecraft.world.level.block.Block.dropResources(beforeB, l, b);
    HydrologyRuntime.transferred(l, a, b, transfer, beforeA);
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
    long key = p.asLong();
    if (budget.active.contains(key)) return;
    if (budget.active.size() >= MAX_ACTIVE) {
      level.scheduleTick(p, EngineRegistry.WATER.get(), 1);
      return;
    }
    budget.active.add(key);
  }

  public static int queued(ServerLevel level) {
    Budget b = BUDGETS.get(level);
    return b == null ? 0 : b.active.size() + b.pressure.size() + b.searches.size();
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
            b.pressure.size() + b.searches.size(),
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
    // The queue stores coordinate snapshots. No callee retains this mutable cursor:
    // activation packs immediately and pressure jobs copy their retained positions.
    var cursor = new BlockPos.MutableBlockPos();
    for (int phase = 0; phase < SUBSTEPS; phase++) {
      budget.pressureLimit = PRESSURE_VISITS_PER_TICK * (phase + 1);
      budget.drainLimit = DRAIN_VISITS_PER_TICK * (phase + 1);
      int remaining =
          Math.min(
              budget.active.size(), Math.min(CELLS_PER_SUBSTEP, TICKS_PER_LEVEL - budget.count));
      while (remaining-- > 0 && !budget.active.isEmpty()) {
        cursor.set(budget.active.removeFirstLong());
        advance(level, cursor, budget);
      }
      runPressure(level, budget);
      if (budget.active.isEmpty() && budget.pressure.isEmpty() && budget.searches.isEmpty()) break;
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
    if (b.pressureLimit > b.pressureVisits
        && (b.pressure.remove(p.asLong()) || b.searches.containsKey(p))) equalize(level, p, b);
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
      if (!budget.searches.containsKey(p)) {
        if (budget.pressure.size() < MAX_ACTIVE) budget.pressure.add(p.asLong());
        else activate(level, p);
      }
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
    if (HydrologyRuntime.oceanSink(l, a, b)) return true;
    int from = units(l, a), to = units(l, b);
    // Avoid creating zero-move exchanges, and reuse the same authoritative read for the cap.
    if (from == 0 || to == WaterTransport.CAPACITY || (!down && from <= to + 1)) return false;
    var e = WaterTransport.exchange(from, to, down);
    if (e.moved() == 0) return false;
    int dx = b.getX() - a.getX(), dz = b.getZ() - a.getZ();
    int flow = down ? 16 : dx > 0 ? 0 : dz > 0 ? 4 : dx < 0 ? 8 : 12;
    if (!down && e.moved() > SIDE_TRANSFER)
      e =
          new WaterTransport.Exchange(
              from - SIDE_TRANSFER, to + SIDE_TRANSFER, SIDE_TRANSFER);
    return transfer(l, a, b, e, flow);
  }

  /** Admit bounded work, then give every resident search one turn in FIFO order. */
  private static void runPressure(ServerLevel level, Budget budget) {
    int admission = MAX_PRESSURE_JOBS;
    while (admission-- > 0 && budget.searches.size() < MAX_PRESSURE_JOBS
        && !budget.pressure.isEmpty()) {
      BlockPos origin = BlockPos.of(budget.pressure.removeFirstLong());
      startPressure(level, origin, budget);
    }
    int turns = budget.searches.size();
    while (turns-- > 0 && !budget.searches.isEmpty()
        && budget.pressureVisits < budget.pressureLimit) {
      var entry = budget.searches.pollFirstEntry();
      // Keep no live map iterators across world reads/callbacks. Rotate before servicing.
      budget.searches.put(entry.getKey(), entry.getValue());
      equalize(level, entry.getKey(), budget);
    }
  }

  private static PressureJob startPressure(ServerLevel level, BlockPos origin, Budget budget) {
    // Keep a resident frontier if an overlapping request reaches admission.
    // Its normal completion or invalidation owns watcher cleanup.
    var existing = budget.searches.get(origin);
    if (existing != null) return existing;
    if (!level.isInWorldBounds(origin) || !level.hasChunkAt(origin)) return null;
    int amount = units(level, origin);
    if (amount == 0 || !surface(level, origin, amount)) return null;
    var job = new PressureJob(origin);
    budget.searches.put(job.origin, job);
    watch(budget, job, origin);
    return job;
  }

  private static void deferPressure(ServerLevel level, BlockPos origin, Budget budget) {
    if (budget.pressure.size() < MAX_ACTIVE) budget.pressure.add(origin.asLong());
    else activate(level, origin);
  }

  private static void refreshHeads(PressureJob job, Budget budget) {
    job.head = 0;
    job.high = job.lowSurface = null;
    job.highHead = Long.MIN_VALUE;
    job.lowHead = Long.MAX_VALUE;
    job.headsDirty = false;
    job.refreshing = true;
    job.refreshes++;
    budget.pressureHeadRefreshes++;
  }

  /** Retained connected-water frontier, bounded to 128 visits per fair scheduling turn. */
  private static boolean equalize(ServerLevel l, BlockPos origin, Budget budget) {
    var job = budget.searches.get(origin);
    if (job != null && job.invalidated) {
      finish(budget, job);
      budget.pressureRestarts++;
      // Do not let a constantly edited component occupy its resident slot forever.
      deferPressure(l, origin, budget);
      return false;
    }
    if (job == null) {
      if (budget.searches.size() >= MAX_PRESSURE_JOBS) {
        deferPressure(l, origin, budget);
        return false;
      }
      job = startPressure(l, origin, budget);
      if (job == null) return false;
    }
    int visits = 0;
    var p = new BlockPos.MutableBlockPos();
    var q = new BlockPos.MutableBlockPos();
    while (job.head < job.tail && visits < PRESSURE_SEARCH
        && budget.pressureVisits < budget.pressureLimit && !job.invalidated) {
      p.set(job.queue[job.head++]);
      visits++;
      budget.pressureVisits++;
      if (!l.isInWorldBounds(p) || !l.hasChunkAt(p)) {
        job.invalidated = true;
        break;
      }
      int amount = units(l, p);
      if (amount == 0) { job.invalidated = true; break; }
      if (surface(l, p, amount)) {
        long value = (long) p.getY() * WaterTransport.CAPACITY + amount;
        if (value > job.highHead) { job.highHead = value; job.high = p.immutable(); }
        if (value < job.lowHead) {
          job.lowHead = value;
          job.lowSurface = p.immutable();
        }
      }
      if (job.refreshing) continue;
      for (Direction d : ALL) {
        q.setWithOffset(p, d);
        long key = q.asLong();
        // Previously admitted wet cells are already watched; do not query their chunks again.
        if (job.seen.contains(key)) continue;
        if (!l.isInWorldBounds(q) || !l.hasChunkAt(q)) continue;
        watch(budget, job, q);
        if (units(l, q) > 0) {
          if (job.tail == MAX_PRESSURE_NODES) { job.saturated = true; continue; }
          job.seen.add(key);
          if (job.tail == job.queue.length)
            job.queue = Arrays.copyOf(job.queue, Math.min(MAX_PRESSURE_NODES, job.tail * 2));
          job.queue[job.tail++] = key;
        }
      }
    }
    budget.maxPressureSlice = Math.max(budget.maxPressureSlice, visits);
    if (job.invalidated) return false;
    BlockPos high = job.high, low = job.lowSurface;
    WaterTransport.Exchange exchange = null;
    int from = 0, to = 0;
    if (high != null && low != null && !high.equals(low)
        && l.hasChunkAt(high) && l.hasChunkAt(low)) {
      from = units(l, high);
      int lower = units(l, low);
      if (from > 0 && lower > 0 && surface(l, high, from) && surface(l, low, lower)) {
        low = lower == WaterTransport.CAPACITY ? low.above() : low;
        if (open(l, low)) {
          to = units(l, low);
          exchange = WaterTransport.pressure(high.getY(), from, low.getY(), to);
        }
      }
    }
    // A fresh connected witness already permits a conservative head-reducing transfer.
    // Preserve the frontier only while no useful pair is known; stable components still
    // finish their bounded search. This avoids rescanning a whole body before every 4 units.
    if (job.invalidated) return false;
    if (exchange != null && exchange.moved() > 0
        && (openChannel(l, high, budget) || openChannel(l, low, budget))) {
      // Keep local gravity/routing and its conserved flux. Remote hydrostatic transfers
      // otherwise pile upstream river water above downstream banks or above sea stage.
      finish(budget, job);
      budget.pressureCompleted++;
      return false;
    }
    if ((exchange == null || exchange.moved() == 0) && job.head < job.tail) return false;
    if ((exchange == null || exchange.moved() == 0) && job.headsDirty) {
      if (job.refreshes < 2) refreshHeads(job, budget);
      else {
        finish(budget, job);
        budget.pressureHeadDeferrals++;
        deferPressure(l, origin, budget);
      }
      return false;
    }
    // Every read chunk stays watched until commit. Edits/unloads invalidate the entire
    // witness graph, including already visited prefixes, so stale frontiers never teleport water.
    // Coalesce only a stable, fully explored component with no remaining pressure move.
    // A transfer can alter or split its wet topology; keep other requests in that case.
    if (!job.saturated && !job.headsDirty && job.head == job.tail
        && (exchange == null || exchange.moved() == 0)) coalesceComplete(budget, job);
    finish(budget, job);
    budget.pressureCompleted++;
    if (job.saturated) budget.pressureSaturated++;
    if (exchange == null || exchange.moved() == 0) return false;
    if (exchange.moved() > PRESSURE_TRANSFER)
      exchange = new WaterTransport.Exchange(from - PRESSURE_TRANSFER,
          to + PRESSURE_TRANSFER, PRESSURE_TRANSFER);
    return transfer(l, high, low, exchange, 16);
  }

  /** Open natural reaches carry a sloping current; they are not one static communicating tank. */
  private static boolean openChannel(ServerLevel level, BlockPos p, Budget budget) {
    if (!(level.getChunkSource().getGenerator() instanceof EngineChunkGenerator generator)) return false;
    long key = ((long) p.getX() << 32) | (p.getZ() & 0xffffffffL);
    var sample = budget.geography.get(key);
    if (sample == null) {
      sample = generator.geography().column(p.getX(), p.getZ());
      if (budget.geography.size() >= 1024) budget.geography.pollFirstEntry();
      budget.geography.put(key, sample);
    }
    if (!sample.wet() || sample.speed() == 0 || Math.abs(p.getY() - sample.water()) > 8
        || !level.canSeeSky(p.above())) return false;
    // An enclosed pipe/well built through the old channel still communicates under pressure.
    int walls = 0;
    for (Direction side : SIDES) if (!open(level, p.relative(side))) walls++;
    return walls < 3;
  }

  /** Only a completed, unchanged component can discharge its duplicate pressure requests. */
  private static void coalesceComplete(Budget budget, PressureJob complete) {
    // Cleanup is bounded by 8192 retained nodes and 16 resident jobs; no world reads occur.
    for (int n = 0; n < complete.tail; n++)
      if (budget.pressure.remove(complete.queue[n])) budget.pressureCoalesced++;
    var overlaps = new ArrayList<PressureJob>();
    for (var job : budget.searches.values())
      if (job != complete && complete.seen.contains(job.origin.asLong())) overlaps.add(job);
    for (var job : overlaps) {
      finish(budget, job);
      budget.pressureCoalesced++;
    }
  }

  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent
    public void chunkLoaded(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
      if (event.getLevel() instanceof ServerLevel level && level.getServer().isSameThread()) {
        Budget budget = BUDGETS.get(level);
        if (budget == null || budget.watchedChunks.isEmpty()) return;
        var pos = event.getChunk().getPos();
        invalidateChunk(budget, pos.pack());
        // A newly loaded neighbor changes a previously sealed search boundary.
        for (Direction side : SIDES)
          invalidateChunk(budget, net.minecraft.world.level.ChunkPos.pack(
              pos.x() + side.getStepX(), pos.z() + side.getStepZ()));
      }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void tick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
      if (event.getLevel() instanceof ServerLevel level) step(level);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
      BUDGETS.clear();
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void chunkUnloaded(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
      if (event.getLevel() instanceof ServerLevel level) {
        Budget budget = BUDGETS.get(level);
        if (budget != null) invalidateChunk(budget, event.getChunk().getPos().pack());
      }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void unloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
      if (event.getLevel() instanceof ServerLevel level) BUDGETS.remove(level);
    }
  }

  private WaterRuntime() {}
}
