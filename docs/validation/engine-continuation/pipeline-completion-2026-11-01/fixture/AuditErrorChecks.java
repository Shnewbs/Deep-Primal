package com.shnewbs.deepprimal.core;
import java.util.concurrent.*;
import com.shnewbs.deepprimal.core.ChunkMaterializer.*;

/** Independent Dev146 audit: a thrown Error must never strand a query future. */
public final class AuditErrorChecks {
  private static int checks;
  private static void ok(boolean value, String label) {
    checks++;
    if (!value) throw new AssertionError(label);
  }
  private static void failed(CompletableFuture<?> future, Class<? extends Throwable> expected) {
    try {
      future.get(300, TimeUnit.MILLISECONDS);
      throw new AssertionError("Unexpected success");
    } catch (ExecutionException e) {
      ok(expected.isInstance(e.getCause()), "Unexpected failure " + e.getCause());
    } catch (TimeoutException e) {
      throw new AssertionError("Query future stranded after Error", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }
  public static void main(String[] args) {
    for (int i = 0; i < 100; i++) {
      var key = new ChunkKey(i, -i, "audit");
      var synchronous = new WorldGenPipeline(
          new ChunkMaterializer(k -> { throw new AssertionError("materializer invariant"); }),
          new ChunkCache());
      failed(synchronous.generateChunk(key), AssertionError.class);
      ok(synchronous.pendingCount() == 0, "Synchronous Error leaked pending query");

      var cache = new ChunkCache() {
        @Override public synchronized void put(Object k, Object v, long weight) {
          throw new AssertionError("cache invariant");
        }
      };
      var delayed = new CompletableFuture<MaterializedChunk>();
      var pipeline = new WorldGenPipeline(new ChunkMaterializer(k -> delayed), cache);
      var returned = pipeline.generateChunk(key);
      delayed.complete(new MaterializedChunk(key, 10));
      failed(returned, AssertionError.class);
      ok(pipeline.pendingCount() == 0, "Cache Error leaked pending query");

      var linkage = new WorldGenPipeline(
          new ChunkMaterializer(k -> { throw new LinkageError("simulated linkage failure"); }),
          new ChunkCache());
      failed(linkage.generateChunk(key), LinkageError.class);
      ok(linkage.pendingCount() == 0, "Linkage Error leaked pending query");
    }
    System.out.println("AUDIT_ERROR_PASS checks=" + checks + " trials=100");
  }
}
