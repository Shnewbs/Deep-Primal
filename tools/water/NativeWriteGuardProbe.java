package com.shnewbs.deepprimal.engine;

import java.lang.reflect.Field;

/** Same-cell callback ABA must not pass accounted water admission. */
public final class NativeWriteGuardProbe {
  public record BlockState(String name) {}

  public static final class BlockPos {
    private final long key;

    public BlockPos(long key) { this.key = key; }
    public long asLong() { return key; }
    public BlockPos immutable() { return new BlockPos(key); }
  }

  public static final class ServerLevel {
    private static final BlockState ORIGINAL = new BlockState("air");
    private static final BlockState FOREIGN = new BlockState("foreign");
    private final boolean aba;
    private BlockState state = ORIGINAL;
    private int expectedEvents;
    private int foreignEvents;

    ServerLevel(boolean aba) { this.aba = aba; }

    public boolean isInWorldBounds(BlockPos position) { return true; }
    public boolean hasChunkAt(BlockPos position) { return true; }
    public BlockState getBlockState(BlockPos position) { return state; }

    public boolean setBlock(BlockPos position, BlockState requested, int flags) {
      if (flags != (2 | 16)) throw new AssertionError("native flags changed");
      transition(position, requested);
      if (aba) {
        transition(position, FOREIGN);
        transition(position, requested);
      } else {
        state = FOREIGN; // Simulate a final replacement not reported to the event hook.
      }
      return true;
    }

    private void transition(BlockPos position, BlockState next) {
      BlockState before = state;
      state = next;
      if (before.equals(next)) return;
      if (WaterWriteScope.consumes(this, position.asLong(), before, next)) expectedEvents++;
      else foreignEvents++;
    }
  }

  public static void main(String[] args) throws Exception {
    Field active = WaterWriteScope.class.getDeclaredField("ACTIVE");
    active.setAccessible(true);
    verify(true, requested(), active);
    verify(false, requested(), active);
    System.out.println("NATIVE_WRITE_GUARD_PASS sameCellAba=true silentReplacement=true");
  }

  private static void verify(boolean aba, BlockState requested, Field active) throws Exception {
    ServerLevel level = new ServerLevel(aba);
    BlockPos position = new BlockPos(17L);
    boolean accepted = false;
    RuntimeException rejection = null;
    try {
      accepted = HydrologyRuntime.underTest(level, position, requested);
    } catch (RuntimeException failure) {
      rejection = failure;
    }
    check(!accepted && rejection != null,
        aba ? "ABA callback must reject accounting" : "silent replacement must reject accounting");
    check(level.state.equals(aba ? requested : ServerLevel.FOREIGN),
        "foreign callback final state preserved");
    check(level.expectedEvents == 1 && level.foreignEvents == (aba ? 2 : 0),
        "expected and foreign transition counts");
    check(((ThreadLocal<?>) active.get(null)).get() == null, "all scopes must close");
  }

  private static BlockState requested() {
    return new BlockState("water");
  }

  private static void check(boolean condition, String reason) {
    if (!condition) throw new AssertionError("NATIVE_WRITE_ABA_FAIL " + reason);
  }

  private NativeWriteGuardProbe() {}
}