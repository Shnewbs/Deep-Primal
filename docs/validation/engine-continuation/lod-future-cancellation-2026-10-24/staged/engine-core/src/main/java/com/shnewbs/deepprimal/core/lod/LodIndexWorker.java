package com.shnewbs.deepprimal.core.lod;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Single bounded LOD worker. Index cleanup cannot be stranded behind a full submission queue. */
public final class LodIndexWorker extends ThreadPoolExecutor {
  private final AtomicReference<LodIndexRequest> index = new AtomicReference<>();
  private final AtomicReference<Object> indexPending = new AtomicReference<>();
  private volatile IOException cleanupFailure;

  public LodIndexWorker(ThreadFactory factory) {
    super(1, 1, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), factory);
  }

  /** One turn only; the render owner decides when to submit the next turn. */
  public Future<LodIndexRequest.Step> submitIndex(LodIndexRequest request) {
    Objects.requireNonNull(request);
    LodIndexRequest old = index.get();
    if (old != request) {
      if (old != null && !old.isTerminal())
        throw new IllegalStateException("Previous LOD index must finish cleanup before replacement");
      if (!index.compareAndSet(old, request)) throw new IllegalStateException("Concurrent LOD index owner");
    }
    Object token = new Object();
    if (!indexPending.compareAndSet(null, token))
      throw new RejectedExecutionException("LOD index turn already pending; retry after worker cleanup");
    FutureTask<LodIndexRequest.Step> task = new FutureTask<>(() -> {
      try {
      // A preceding job may have drained cancellation before this queued turn starts.
      if (request.isCancelled() && request.isTerminal())
        return new LodIndexRequest.Step(request, LodIndexRequest.Status.CANCELLED, 0, 0, 0,
            request.visitedEntries(), List.of());
      return request.turn();
      } finally { indexPending.compareAndSet(token, null); }
    }) {
      @Override public boolean cancel(boolean mayInterruptIfRunning) {
        // Never interrupt filesystem work: signal its owner and arrange a cleanup wake.
        // A failed cancellation must leave an already completed result publishable.
        boolean accepted = super.cancel(false);
        if (accepted) {
          request.cancel();
          wakeCleanup();
        }
        return accepted;
      }
      @Override public void run() {
        // FutureTask skips its Callable when cancelled in the queue. Release admission
        // outside that Callable so a cancelled turn cannot permanently occupy the slot.
        try { super.run(); }
        finally { indexPending.compareAndSet(token, null); }
      }
    };
    try {
      execute(task);
      return task;
    } catch (RejectedExecutionException failure) {
      indexPending.compareAndSet(token, null);
      throw failure; // Keep registered request so cancellation/close still has an owner.
    }
  }

  /** Signal only plus a nonblocking wake. Full queue guarantees a later afterExecute cleanup. */
  public void cancelIndex() {
    LodIndexRequest request = index.get();
    if (request == null) return;
    request.cancel();
    wakeCleanup();
  }

  private void wakeCleanup() {
    try { execute(() -> {}); }
    catch (RejectedExecutionException fullOrClosed) {
      // A full running executor has work that invokes afterExecute. shutdown() wakes before closing.
      // AbortPolicy rejects only a full or closed executor; either path already owns cleanup.
    }
  }

  @Override protected void afterExecute(Runnable task, Throwable failure) {
    try {
      LodIndexRequest request = index.get();
      if (request != null) {
        if (failure != null) request.cancel(); // Close on the original thread before abrupt replacement.
        if (request.isCancelled() && !request.isTerminal()) {
          try { request.closeOnWorker(); }
          catch (IOException close) { cleanupFailure = close; }
        }
      }
    } finally { super.afterExecute(task, failure); }
  }

  public IOException cleanupFailure() { return cleanupFailure; }

  /** Never discard a queued close/turn; render reset need not wait for worker filesystem work. */
  @Override public void shutdown() {
    cancelIndex();
    super.shutdown();
  }

  /** Forced discard cannot satisfy cursor ownership; callers must use graceful shutdown. */
  @Override public List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException("LOD worker requires graceful shutdown");
  }
}
