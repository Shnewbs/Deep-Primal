package com.shnewbs.deepprimal.engine;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Suppresses only the one native block-state notification authored by a synchronous
 * engine write. Unrelated edits, even from a nested callback or another level,
 * must remain visible to the chemistry ledger.
 *
 * <p>This is an event ownership hint, not a transaction journal. It never grants
 * ownership of the final native cell; callers must still check loaded state and
 * successful compensation before committing finite-water constituents.
 */
public final class WaterWriteScope {
  private static final ThreadLocal<ArrayDeque<Write>> ACTIVE = new ThreadLocal<>();

  private static final class Write {
    final Object level;
    final long position;
    final Object before;
    final Object after;
    boolean consumed;

    Write(Object level, long position, Object before, Object after) {
      this.level = level;
      this.position = position;
      this.before = before;
      this.after = after;
    }
  }

  /** Enter before the native write, using a snapshot of the packed position. */
  public static Scope begin(Object level, long position, Object before, Object after) {
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(before, "before");
    Objects.requireNonNull(after, "after");
    var stack = ACTIVE.get();
    if (stack == null) {
      stack = new ArrayDeque<>();
      ACTIVE.set(stack);
    }
    var write = new Write(level, position, before, after);
    stack.push(write);
    return new Scope(stack, write);
  }

  /**
   * Consume at most one EXACT expected transition for the innermost write to
   * this level and position. Do not fall through to an outer token for the same
   * cell while an inner write is active: a nested foreign edit must invalidate.
   */
  public static boolean consumes(Object level, long position, Object before, Object after) {
    if (before == null || after == null || before.equals(after)) return false;
    var stack = ACTIVE.get();
    if (stack == null) return false;
    for (var write : stack) {
      if (write.level != level || write.position != position) continue;
      if (write.consumed || !write.before.equals(before) || !write.after.equals(after))
        return false;
      write.consumed = true;
      return true;
    }
    return false;
  }

  public static final class Scope implements AutoCloseable {
    private final ArrayDeque<Write> stack;
    private final Write write;
    private final Thread ownerThread;
    private boolean closed;

    private Scope(ArrayDeque<Write> stack, Write write) {
      this.stack = stack;
      this.write = write;
      this.ownerThread = Thread.currentThread();
    }

    @Override public void close() {
      if (closed || Thread.currentThread() != ownerThread || stack.peek() != write)
        throw new IllegalStateException("Finite-water native write scope closed out of order");
      stack.pop();
      closed = true;
      if (stack.isEmpty()) ACTIVE.remove();
    }
  }

  private WaterWriteScope() {}
}
