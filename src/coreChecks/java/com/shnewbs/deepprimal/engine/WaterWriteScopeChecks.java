package com.shnewbs.deepprimal.engine;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/** Dependency-free, source-compiled ownership tests; native Minecraft acceptance remains separate. */
@SuppressWarnings("try")
public final class WaterWriteScopeChecks {
  private static long checks;
  private static void verify(boolean actual, String why) {
    checks++;
    if (!actual) throw new AssertionError("WATER_WRITE_SCOPE_FAIL " + why + " check=" + checks);
  }

  private record State(String kind, int units, boolean ocean) {}
  private static final State FRESH = new State("water", 8, false);
  private static final State MORE = new State("water", 9, false);
  private static final State SALT = new State("water", 8, true);
  private static final State AIR = new State("air", 0, false);

  private static void basics() {
    Object world = new Object(), otherWorld = new Object();
    long pos = -13_148_172_000L;
    verify(!WaterWriteScope.consumes(world, pos, FRESH, MORE), "without scope");
    try (var scope = WaterWriteScope.begin(world, pos, FRESH, MORE)) {
      verify(!WaterWriteScope.consumes(world, pos + 1, FRESH, MORE), "other cell");
      verify(!WaterWriteScope.consumes(otherWorld, pos, FRESH, MORE), "other world");
      verify(!WaterWriteScope.consumes(world, pos, MORE, FRESH), "reverse transition");
      verify(!WaterWriteScope.consumes(world, pos, FRESH, AIR), "different target");
      verify(!WaterWriteScope.consumes(world, pos, FRESH, SALT), "same amount other marker");
      verify(!WaterWriteScope.consumes(world, pos, FRESH, FRESH), "no-op");
      verify(!WaterWriteScope.consumes(world, pos, null, MORE), "null before");
      verify(WaterWriteScope.consumes(world, pos, new State("water", 8, false),
          new State("water", 9, false)), "value equality of native states");
      verify(!WaterWriteScope.consumes(world, pos, FRESH, MORE), "one expected event only");
    }
    verify(!WaterWriteScope.consumes(world, pos, FRESH, MORE), "scope closed");
    try (var scope = WaterWriteScope.begin(world, pos, FRESH, MORE)) {
      verify(WaterWriteScope.consumes(world, pos, FRESH, MORE), "scope reusable");
    }
  }

  private static void reentrancy() {
    Object world = new Object(), otherWorld = new Object();
    long a = 5, c = 6;
    // The previously global counter lost C's chemistry when called during A's write.
    try (var outer = WaterWriteScope.begin(world, a, FRESH, MORE)) {
      verify(!WaterWriteScope.consumes(world, c, FRESH, AIR), "same-world deletion of C");
      verify(!WaterWriteScope.consumes(otherWorld, c, FRESH, AIR), "other-world deletion of C");
      verify(!WaterWriteScope.consumes(world, a, FRESH, AIR), "same-cell foreign replacement");
      try (var inner = WaterWriteScope.begin(world, c, FRESH, SALT)) {
        verify(WaterWriteScope.consumes(world, c, FRESH, SALT), "nested own C");
        verify(WaterWriteScope.consumes(world, a, FRESH, MORE), "outer own A");
      }
    }
    try (var outer = WaterWriteScope.begin(world, a, FRESH, MORE)) {
      try (var inner = WaterWriteScope.begin(world, a, MORE, AIR)) {
        verify(!WaterWriteScope.consumes(world, a, FRESH, MORE), "inner must shadow outer");
        verify(WaterWriteScope.consumes(world, a, MORE, AIR), "inner own transition");
      }
      verify(WaterWriteScope.consumes(world, a, FRESH, MORE), "outer resumes after inner close");
    }
  }

  private static void unwindAndThread() throws Exception {
    Object world = new Object();
    try {
      try (var scope = WaterWriteScope.begin(world, 12, FRESH, MORE)) {
        throw new IllegalArgumentException("injected callback failure");
      }
    } catch (IllegalArgumentException expected) {
      verify(!WaterWriteScope.consumes(world, 12, FRESH, MORE), "exception unwind");
    }
    try (var scope = WaterWriteScope.begin(world, 12, FRESH, MORE)) {
      var leaked = new AtomicBoolean(false);
      Thread t = new Thread(() -> leaked.set(WaterWriteScope.consumes(world, 12, FRESH, MORE)));
      t.start(); t.join();
      verify(!leaked.get(), "independent callback thread cannot consume server thread");
      verify(WaterWriteScope.consumes(world, 12, FRESH, MORE), "same owner thread retains event");
    }
    var first = WaterWriteScope.begin(world, 21, FRESH, MORE);
    var second = WaterWriteScope.begin(world, 22, FRESH, MORE);
    boolean orderRejected = false;
    try { first.close(); } catch (IllegalStateException expected) { orderRejected = true; }
    verify(orderRejected, "bad close ordering detected");
    second.close(); first.close();
    verify(!WaterWriteScope.consumes(world, 21, FRESH, MORE), "fully cleaned after misorder");
  }

  private static void chemistryModel() {
    Object world = new Object(), otherWorld = new Object();
    // A stale parcel with precisely matching water amount must not survive an
    // unrelated callback edit; this models externalChange's invalidation contract.
    long a = 17, c = 18;
    long salt = 412, sediment = 73;
    try (var scope = WaterWriteScope.begin(world, a, FRESH, MORE)) {
      if (!WaterWriteScope.consumes(world, c, FRESH, AIR)) { salt = 0; sediment = 0; }
      verify(salt == 0 && sediment == 0, "fresh same-volume refill has no stale solutes");
      salt = 412; sediment = 73;
      if (!WaterWriteScope.consumes(otherWorld, c, FRESH, AIR)) { salt = 0; sediment = 0; }
      verify(salt == 0 && sediment == 0, "separate world chemistry invalidated");
      verify(WaterWriteScope.consumes(world, a, FRESH, MORE), "owner event still suppressed");
    }
  }

  private static void randomized() {
    var rng = new Random(20261027L);
    Object[] worlds = {new Object(), new Object(), new Object()};
    State[] states = {AIR, FRESH, MORE, SALT};
    for (int i = 0; i < 120_000; i++) {
      int ow = rng.nextInt(3), op = rng.nextInt(5), beforeIndex = rng.nextInt(4);
      int afterIndex = (beforeIndex + 1 + rng.nextInt(3)) % 4;
      State before = states[beforeIndex], after = states[afterIndex];
      Object writeWorld = worlds[ow];
      try (var scope = WaterWriteScope.begin(writeWorld, op, before, after)) {
        int eventWorld = rng.nextInt(3), eventPos = rng.nextInt(5);
        State observedBefore = states[rng.nextInt(4)], observedAfter = states[rng.nextInt(4)];
        boolean expected = eventWorld == ow && eventPos == op
            && before.equals(observedBefore) && after.equals(observedAfter);
        boolean first = WaterWriteScope.consumes(worlds[eventWorld], eventPos,
            observedBefore, observedAfter);
        verify(first == expected, "random ownership classification #" + i);
        boolean ownExpected = WaterWriteScope.consumes(writeWorld, op, before, after);
        verify(ownExpected == !expected, "exact transition accepted once #" + i);
        verify(!WaterWriteScope.consumes(writeWorld, op, before, after), "no double consumption #" + i);
      }
      verify(!WaterWriteScope.consumes(writeWorld, op, before, after), "random cleanup #" + i);
    }
  }

  public static void main(String[] args) throws Exception {
    basics(); reentrancy(); unwindAndThread(); chemistryModel(); randomized();
    System.out.println("WATER_WRITE_SCOPE_PASS checks=" + checks + " nested=true crossWorld=true crossThread=true repeat=false");
  }

  private WaterWriteScopeChecks() {}
}
