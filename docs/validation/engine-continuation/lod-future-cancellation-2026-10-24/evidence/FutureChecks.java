import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class FutureChecks {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static LodIndexWorker worker(){return new LodIndexWorker(r->{Thread t=new Thread(r,"r57-worker");t.setDaemon(true);return t;});}
 static LodIndexRequest req(Path p){return new LodIndexRequest(p,2,0,0,8,1);}
 static void barrier(LodIndexWorker w)throws Exception {
  long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
  while(true){try{w.submit(()->{}).get(5,TimeUnit.SECONDS);return;}catch(RejectedExecutionException full){if(System.nanoTime()>end)throw full;Thread.yield();}}
 }
 public static void main(String[]args)throws Exception{
  Path p=Files.createTempDirectory("r57-future-");
  try{
   for(int i=0;i<20;i++)Files.createFile(p.resolve(i+"_0.plod"));
   for(boolean interrupt:new boolean[]{false,true})for(int round=0;round<50;round++){
    LodIndexWorker w=worker();LodIndexRequest r=req(p);var enter=new CountDownLatch(1);var release=new CountDownLatch(1);
    try{
     w.submitIndex(r).get(5,TimeUnit.SECONDS);check(r.isOpen(),"initial cursor open");
     w.submit(()->{enter.countDown();try{release.await();}catch(InterruptedException e){throw new AssertionError(e);}});
     check(enter.await(5,TimeUnit.SECONDS),"worker blocked");
     var f=w.submitIndex(r);check(f.cancel(interrupt),"queued cancellation accepted");
     if(args.length==0)check(f.isCancelled()&&r.isCancelled(),"future cancellation signals request");else w.cancelIndex();
     release.countDown();barrier(w);
     // A barrier Future completes before afterExecute: a second barrier guarantees the first hook finished.
     barrier(w);
     check(r.isTerminal()&&!r.isOpen(),"cancelled future closes cursor");
     var replacement=req(p);check(w.submitIndex(replacement).get(5,TimeUnit.SECONDS).status()==LodIndexRequest.Status.MORE,"pending slot released after cancelled queue task");
     w.cancelIndex();barrier(w);barrier(w);
     check(!replacement.isOpen(),"replacement cleanup");
    }finally{release.countDown();w.shutdown();check(w.awaitTermination(5,TimeUnit.SECONDS),"worker terminated");}
   }
   LodIndexWorker w=worker();try{
    var r=req(p);var f=w.submitIndex(r);f.get(5,TimeUnit.SECONDS);check(!f.cancel(true)&&!r.isCancelled(),"completed turn cancellation has no effect");
    var last=f;while(!r.isTerminal()){last=w.submitIndex(r);last.get(5,TimeUnit.SECONDS);}var result=last.get();check(!last.cancel(false)&&r.canPublish(result,2,0,0),"completed result retained");
   }finally{w.shutdown();check(w.awaitTermination(5,TimeUnit.SECONDS),"completed worker terminated");}
   System.out.println("PASS R57 future checks="+checks+" queuedCancelRuns=100 falseAndTrue=true");
  }finally{try(var fs=Files.walk(p)){for(Path x:fs.sorted(Comparator.reverseOrder()).toList())Files.delete(x);}}
 }
}
