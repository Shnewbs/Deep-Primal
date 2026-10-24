import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class WorkerChecks {
  static int checks;
  static void check(boolean ok, String why) { checks++; if (!ok) throw new AssertionError(why); }
  static void await(CountDownLatch x) throws Exception { check(x.await(10, TimeUnit.SECONDS), "latch timeout"); }
  static void stop(LodIndexWorker w) throws Exception { w.shutdown(); check(w.awaitTermination(10, TimeUnit.SECONDS), "termination"); check(w.cleanupFailure()==null,"cleanup IO"); }
  static LodIndexWorker worker() { return new LodIndexWorker(r -> new Thread(r,"r56-worker")); }
  static LodIndexRequest req(Path p, int x) { return new LodIndexRequest(p, 7, x, 0,8192,256); }
  static void block(LodIndexWorker w, CountDownLatch entered, CountDownLatch release) {
    w.submit(() -> { entered.countDown(); try { release.await(); } catch (InterruptedException e) { throw new AssertionError(e); } });
  }
  public static void main(String[] args) throws Exception {
    Path root=Files.createTempDirectory("r56-worker-");
    try {
      for(int i=0;i<8205;i++) Files.createFile(root.resolve(i+"_0.plod"));
      LodIndexWorker w=worker(); LodIndexRequest r=req(root,8204); int turns=0,geometry=0;
      while(!r.isTerminal()) {
        var step=w.submitIndex(r).get(10,TimeUnit.SECONDS); turns++;
        check(step.visitedEntries()<=Math.min(8205,turns*256L),"bounded visit count");
        if(step.status()==LodIndexRequest.Status.MORE) { check(step.keys().isEmpty(),"partial publication"); w.submit(()->{}).get(); geometry++; }
        else { check(step.keys().size()==8192,"nearest retained"); check(step.keys().get(0)==((long)8204<<32),"late directory page found"); }
      }
      check(turns==33 && geometry==32,"fair turns"); stop(w);
      // Real defect reproduction: best-effort queued cleanup is rejected by a full queue.
      ThreadPoolExecutor baseline=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2));
      LodIndexRequest leaked=req(root,0); baseline.submit(leaked::turn).get();
      var entered=new CountDownLatch(1); var release=new CountDownLatch(1);
      final var baseEntered=entered; final var baseRelease=release;
      baseline.submit(()->{baseEntered.countDown();try{baseRelease.await();}catch(InterruptedException e){throw new AssertionError(e);}}); await(entered);
      var drained=new CountDownLatch(1); baseline.submit(()->{});baseline.submit(drained::countDown);leaked.cancel();boolean rejected=false;
      try{baseline.submit(()->{leaked.closeOnWorker();return null;});}catch(RejectedExecutionException expected){rejected=true;}
      check(rejected && leaked.isOpen(),"baseline rejection leaves cursor open");
      release.countDown(); await(drained);check(leaked.isOpen(),"baseline drained without cleanup");
      baseline.submit(()->{leaked.closeOnWorker();return null;}).get();
      baseline.shutdown();check(baseline.awaitTermination(10,TimeUnit.SECONDS),"baseline drain");
      check(!leaked.isOpen(),"baseline fixture cleanup on owner");
      for(int i=0;i<100;i++) {
        w=worker();r=req(root,i);w.submitIndex(r).get();check(r.isOpen(),"started cursor");
        entered=new CountDownLatch(1);release=new CountDownLatch(1);block(w,entered,release);await(entered);
        w.submit(()->{});w.submit(()->{});check(w.getQueue().size()==2,"saturated");
        w.shutdown();check(w.isShutdown(),"closed admission");check(r.isCancelled(),"signal");
        release.countDown();check(w.awaitTermination(10,TimeUnit.SECONDS),"saturated shutdown drain");
        check(r.isTerminal()&&!r.isOpen(),"full-queue cleanup completed");check(w.cleanupFailure()==null,"no cleanup failure");
      }
      w=worker();r=req(root,1);w.submitIndex(r).get();w.cancelIndex();w.submit(()->{}).get();check(!r.isOpen()&&r.isTerminal(),"idle wake cleanup");
      var replacement=req(root,2);var step=w.submitIndex(replacement).get();check(step.status()==LodIndexRequest.Status.MORE,"same worker replacement");stop(w);check(!replacement.isOpen(),"replacement drain");
      w=worker(); entered=new CountDownLatch(1);release=new CountDownLatch(1);block(w,entered,release);await(entered);
      r=req(root,0);var pending=w.submitIndex(r);w.submit(()->{});w.shutdown();release.countDown();
      check(pending.get().status()==LodIndexRequest.Status.CANCELLED,"queued cancel returns no result");check(w.awaitTermination(10,TimeUnit.SECONDS)&&!r.isOpen(),"queued drain");
      w=worker();entered=new CountDownLatch(1);release=new CountDownLatch(1);block(w,entered,release);await(entered);w.submit(()->{});w.submit(()->{});r=req(root,0);
      rejected=false;try{w.submitIndex(r);}catch(RejectedExecutionException expected){rejected=true;}check(rejected&&!r.isOpen(),"rejected admission retains unopened request");
      release.countDown();
      Future<?> barrier=null;long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
      while(barrier==null&&System.nanoTime()<deadline){try{barrier=w.submit(()->{});}catch(RejectedExecutionException full){Thread.yield();}}
      check(barrier!=null,"rejection retry barrier admitted");barrier.get();
      check(w.submitIndex(r).get().status()==LodIndexRequest.Status.MORE,"rejected request retries unchanged");
      boolean refused=false;try{w.shutdownNow();}catch(UnsupportedOperationException expected){refused=true;}
      check(refused&&!w.isShutdown(),"forced queue discard refused");
      stop(w);check(r.isTerminal()&&!r.isOpen(),"rejected request cleanup");
      var died=new CountDownLatch(1);var injected=new AtomicReference<Throwable>();
      w=new LodIndexWorker(task->{Thread t=new Thread(task,"r56-error-worker");t.setUncaughtExceptionHandler((thread,error)->{injected.set(error);died.countDown();});return t;});
      r=req(root,0);w.submitIndex(r).get();w.execute(()->{throw new AssertionError("injected geometry failure");});await(died);
      check(injected.get() instanceof AssertionError&&r.isCancelled()&&r.isTerminal()&&!r.isOpen(),"abrupt execute closes before worker replacement");
      replacement=req(root,3);check(w.submitIndex(replacement).get().status()==LodIndexRequest.Status.MORE,"replacement thread starts new owner");stop(w);check(!replacement.isOpen(),"replacement thread cleanup");
      System.out.println("PASS R56 worker checks="+checks+" saturationRuns=100 turns=33 geometry=32 baselineLeakReproduced=true");
    } finally { try(var paths=Files.walk(root)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);} }
  }
}
