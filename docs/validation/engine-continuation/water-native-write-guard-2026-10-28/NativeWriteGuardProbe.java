package com.shnewbs.deepprimal.engine;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;

/** Test boundary doubles, NOT Minecraft classes or a native conservation test. */
public final class NativeWriteGuardProbe {
  public record BlockState(String kind, int units, boolean ocean) {}
  public static class BlockPos {
    private final long key;
    public BlockPos(long key) { this.key = key; }
    public long asLong() { return key; }
    public BlockPos immutable() { return this; }
  }
  public static final class MutablePos extends BlockPos {
    private long current;
    public MutablePos(long key) { super(key); current = key; }
    public void set(long key) { current = key; }
    @Override public long asLong() { return current; }
    @Override public BlockPos immutable() { return new BlockPos(current); }
  }
  @FunctionalInterface public interface Script {
    boolean write(ServerLevel level, BlockPos p, BlockState state, int invocation);
  }
  public static final class ServerLevel {
    final HashMap<Long, BlockState> cells = new HashMap<>();
    boolean loaded = true, inBounds = true;
    int reads, writes, exemptions, foreignEvents, ledgerCommits;
    Script script = (world, p, s, invocation) -> { world.transition(p, s); return true; };
    public boolean isInWorldBounds(BlockPos p) { return inBounds; }
    public boolean hasChunkAt(BlockPos p) { return loaded; }
    public BlockState getBlockState(BlockPos p) {
      check(loaded && inBounds, "read must not resolve an unloaded/out-of-bounds cell");
      reads++;
      return cells.getOrDefault(p.asLong(), AIR);
    }
    public boolean setBlock(BlockPos p, BlockState s, int flags) {
      check(loaded && inBounds, "native write must not load a missing cell");
      check(flags == (2 | 16), "native flags preserved");
      return script.write(this, p, s, ++writes);
    }
    void transition(BlockPos p, BlockState s) {
      var before = cells.getOrDefault(p.asLong(), AIR);
      cells.put(p.asLong(), s);
      if (!before.equals(s)) {
        if (WaterWriteScope.consumes(this, p.asLong(), before, s)) exemptions++;
        else foreignEvents++;
      }
    }
  }

  private static final BlockState AIR = new BlockState("air", 0, false);
  private static final BlockState STONE = new BlockState("stone", 0, false);
  private static final BlockState ICE = new BlockState("ice", 0, false);
  private static final BlockState FULL = new BlockState("water", 16, false);
  private static final BlockState HALF = new BlockState("water", 8, false);
  private static final BlockState LOW = new BlockState("water", 4, false);
  private static final BlockState SEA = new BlockState("water", 16, true);
  private static final BlockState LOW_SEA = new BlockState("water", 8, true);
  private static long assertions;
  private static int scenarios;
  private static Field active;
  private static void check(boolean value, String why) {
    assertions++;
    if (!value) throw new AssertionError("NATIVE_WRITE_GUARD_FAIL " + why);
  }
  private static void cleanup() throws Exception {
    check(((ThreadLocal<?>) active.get(null)).get() == null, "write scopes unwind completely");
  }
  private static boolean call(ServerLevel w, BlockPos p, BlockState desired) {
    boolean admitted = HydrologyRuntime.underTest(w, p, desired);
    if (admitted) w.ledgerCommits++; // models the caller's post-admission account update only
    return admitted;
  }

  private static void caseAt(String operation, BlockState before, BlockState desired,
      long key, int mode) throws Exception {
    scenarios++;
    var w = new ServerLevel();
    var p = new MutablePos(key);
    w.cells.put(key, before);
    // Same desired state at a different address makes a moved-cursor false positive observable.
    w.cells.put(key ^ 0x4000L, desired);
    var marker = new IllegalArgumentException("injected callback exception");
    w.script = (world, position, target, invocation) -> {
      check(invocation <= 2, "bounded single compensation attempt");
      if (invocation == 2) {
        if (mode == 9) return false; // refuses rollback without changing the applied cell
        if (mode == 10) { world.transition(position, STONE); return true; }
        if (mode == 11) { world.loaded = false; return true; }
        if (mode == 14) throw marker;
        world.transition(position, target);
        if (mode == 15) throw marker;
        return mode != 8; // false-but-restored rollback is an observed original state
      }
      if (mode == 1) return false; // ordinary unchanged refusal
      if (mode == 2) return true;  // claimed success with original still present
      if (mode == 12) throw marker;
      world.transition(position, target);
      switch (mode) {
        case 3, 7 -> world.transition(position, STONE);
        case 4 -> world.transition(position, before);
        case 5 -> world.loaded = false;
        case 16 -> world.inBounds = false;
        case 17 -> p.set(key ^ 0x4000L);
        case 13 -> throw marker;
        default -> { }
      }
      return mode != 6 && mode != 7 && mode != 8 && mode != 9 && mode != 10
          && mode != 11 && mode != 14 && mode != 15;
    };
    boolean normal = false, result = false;
    RuntimeException failure = null;
    try { result = call(w, p, desired); normal = true; }
    catch (RuntimeException e) { failure = e; }
    boolean success = mode == 0;
    boolean refusal = mode == 1 || mode == 6 || mode == 8;
    boolean expectedNormal = success || refusal;
    String name = operation + " key=" + key + " mode=" + mode;
    check(normal == expectedNormal, "normal/exception classification " + name);
    check(!normal || result == success, "success/refusal classification " + name);
    check(w.ledgerCommits == (success ? 1 : 0), "no account commit on failed ownership " + name);
    boolean injectedException = mode >= 12 && mode <= 15;
    check(!injectedException || failure == marker, "original callback exception propagated " + name);
    check(expectedNormal || failure != null, "unsafe final state fails loudly " + name);
    int expectedWrites = mode == 6 || mode == 8 || mode == 9 || mode == 10
        || mode == 11 || mode == 14 || mode == 15 ? 2 : 1;
    check(w.writes == expectedWrites, "bounded write count " + name);
    BlockState expectedFinal = switch (mode) {
      case 1, 2, 4, 6, 8, 12, 15 -> before;
      case 3, 7, 10 -> STONE;
      default -> desired;
    };
    check(w.cells.get(key).equals(expectedFinal), "foreign edits never overwritten " + name);
    check(w.cells.get(key ^ 0x4000L).equals(desired), "other address unchanged " + name);
    if (mode == 6 || mode == 8)
      check(w.exemptions == 2 && w.foreignEvents == 0,
          "forward and compensating events have distinct single-use scopes " + name);
    cleanup();
  }

  private static void controls() throws Exception {
    for (int mask = 0; mask < 3; mask++) {
      var w = new ServerLevel();
      w.loaded = (mask & 1) != 0;
      w.inBounds = (mask & 2) != 0;
      check(!call(w, new BlockPos(-1), FULL), "initially missing cell refused");
      check(w.reads == 0 && w.writes == 0 && w.ledgerCommits == 0,
          "initial refusal has no cell read/native write/account update");
      cleanup(); scenarios++;
    }
    var w = new ServerLevel(); var p = new BlockPos(Long.MIN_VALUE);
    w.cells.put(p.asLong(), FULL);
    check(call(w, p, new BlockState("water", 16, false)), "value-equal no-op succeeds");
    check(w.writes == 0, "no-op requires no native callback");
    cleanup(); scenarios++;

    var other = new ServerLevel();
    w = new ServerLevel();
    w.cells.put(1L, AIR); w.cells.put(2L, HALF);
    w.script = (world, pos, target, invocation) -> {
      world.transition(pos, target);
      if (pos.asLong() == 1) {
        // Normal nested engine work at a different cell and a different level.
        check(HydrologyRuntime.underTest(world, new BlockPos(2), LOW), "nested cell accepted");
        check(HydrologyRuntime.underTest(other, new BlockPos(1), FULL), "nested level accepted");
        world.transition(new BlockPos(3), STONE);
      }
      return true;
    };
    check(call(w, new BlockPos(1), FULL), "outer cell survives unrelated nested writes");
    check(w.foreignEvents == 1, "unrelated external event remains visible");
    check(w.exemptions == 2 && other.exemptions == 1, "nested tokens stay scoped");
    cleanup(); scenarios++;
  }

  public static void main(String[] args) throws Exception {
    active = WaterWriteScope.class.getDeclaredField("ACTIVE"); active.setAccessible(true);
    controls();
    String[] names = {"place", "pickup", "emit", "infiltrate", "restoreOcean", "oceanSink", "meltIce"};
    BlockState[] old = {AIR, FULL, HALF, FULL, LOW_SEA, HALF, ICE};
    BlockState[] next = {FULL, AIR, FULL, HALF, SEA, LOW, FULL};
    long[] special = {0, -1, Long.MIN_VALUE, Long.MAX_VALUE, -0x7fffffffL, 0x7fffffffL};
    for (int i = 0; i < names.length; i++) {
      for (long key : special) for (int mode = 0; mode < 18; mode++)
        caseAt(names[i], old[i], next[i], key, mode);
    }
    var rng = new Random(202610280029L);
    for (int i = 0; i < 2_000; i++) {
      int operation = rng.nextInt(names.length);
      caseAt(names[operation], old[operation], next[operation], rng.nextLong(), rng.nextInt(18));
    }
    System.out.println("NATIVE_WRITE_GUARD_PASS scenarios=" + scenarios + " assertions=" + assertions
        + " nativeMinecraft=false sourceExtracted=true");
  }
  private NativeWriteGuardProbe() {}
}
