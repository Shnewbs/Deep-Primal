package com.shnewbs.deepprimal.core.lod;
import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;

/** Actual bounded single-worker executor, real directory, no renderer/world doubles. */
public class RequestChecks {
 static int checks;
 static void require(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 static List<Long> oracle(int cx,int cz){var all=new ArrayList<Long>();for(int i=0;i<8205;i++)all.add(key(i-4102,i%17-8));return all.stream().sorted(Comparator.comparing((Long k)->{
  var dx=BigInteger.valueOf((long)(int)(k>>32)-cx);var dz=BigInteger.valueOf((long)(int)(long)k-cz);return dx.multiply(dx).add(dz.multiply(dz));}).thenComparingInt(k->(int)(k>>32)).thenComparingInt(k->(int)(long)k)).limit(8192).toList();}
 static LodIndexRequest.Step finish(ThreadPoolExecutor worker,LodIndexRequest req,List<String> events)throws Exception{
  long before=req.visitedEntries();int turns=0;
  while(true){int n=turns++;var step=worker.submit(()->{events.add("turn"+n);return req.turn();}).get(20,TimeUnit.SECONDS);
   require(step.visitedEntries()-before<=256,"entry turn quota");before=step.visitedEntries();
   if(step.status()!=LodIndexRequest.Status.MORE)return step;
   require(step.keys().isEmpty(),"partial keys never published");worker.submit(()->events.add("geometry"+n)).get(20,TimeUnit.SECONDS);
   require(events.get(events.size()-1).equals("geometry"+n),"geometry interleaves before next turn");
   require(worker.getQueue().size()<=2,"executor queue bound");require(turns<100,"finite scan");
  }
 }
 public static void main(String[]args)throws Exception{
  Path root=Files.createTempDirectory("deepprimal-lod-request-");var worker=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(2));
  try{
   for(int i=0;i<8205;i++)Files.writeString(root.resolve((i-4102)+"_"+(i%17-8)+".plod"),"");
   var events=new ArrayList<String>();var scan=new LodIndexRequest(root,41,700,-3,8192,256);var done=finish(worker,scan,events);
   require(done.status()==LodIndexRequest.Status.COMPLETE&&done.epoch()==41&&done.cameraX()==700&&done.cameraZ()==-3,"complete result attribution");require(done.keys().equals(oracle(700,-3)),"executor result versus independent nearest oracle");require(!scan.isOpen()&&scan.isTerminal(),"complete closes cursor");
   require(scan.canPublish(done,41,700,-3),"current result publishable");require(!scan.canPublish(done,42,700,-3),"wrong session blocked");require(!scan.canPublish(done,41,701,-3),"wrong camera blocked");
   int geometry=(int)events.stream().filter(s->s.startsWith("geometry")).count();require(geometry==32,"32geometry jobs between33scan turns");
   var mid=new LodIndexRequest(root,42,0,0,8192,256);require(worker.submit(mid::turn).get().status()==LodIndexRequest.Status.MORE,"mid scan opens");require(mid.isOpen(),"mid cursor open");mid.cancel();require(mid.isOpen(),"render cancellation performs no IO close");
   var cancelled=worker.submit(mid::turn).get();require(cancelled.status()==LodIndexRequest.Status.CANCELLED&&cancelled.keys().isEmpty()&&!mid.isOpen(),"next worker turn cancels and closes");
   var close=new LodIndexRequest(root,43,0,0,8192,256);worker.submit(close::turn).get();boolean wrong=false;try{close.closeOnWorker();}catch(IllegalStateException expected){wrong=true;}require(wrong&&close.isOpen(),"wrong-thread cleanup rejected without IO");close.cancel();worker.submit(()->{close.closeOnWorker();close.closeOnWorker();return null;}).get();require(!close.isOpen()&&close.isTerminal(),"idempotent worker cleanup");
   var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var blocker=worker.submit(()->{entered.countDown();release.await();return null;});require(entered.await(5,TimeUnit.SECONDS),"worker blocked for deterministic queued cancellation");
   var queued=new LodIndexRequest(root,44,0,0,8192,256);var queuedFuture=worker.submit(queued::turn);queued.cancel();release.countDown();blocker.get();require(queuedFuture.get().status()==LodIndexRequest.Status.CANCELLED&&queued.visitedEntries()==0&&!queued.isOpen(),"cancelled queued request opens no directory");
   // A completed old result must still be rejected after a newer camera/session request.
   scan.cancel();require(!scan.canPublish(done,41,700,-3),"late cancellation blocks completed result");var sameView=new LodIndexRequest(root,41,700,-3,8192,256);require(!sameView.canPublish(done,41,700,-3),"same view new request blocks older completion");var newer=new LodIndexRequest(root,45,-500,7,8192,256);require(scan.isCancelled()&&done.epoch()!=45,"stale completion cannot be accepted as new epoch");var nd=finish(worker,newer,new ArrayList<>());require(nd.keys().equals(oracle(-500,7))&&nd.epoch()==45,"new camera request recomputes independently");
   require(newer.canPublish(nd,45,-500,7),"new request accepts own completion");require(!newer.canPublish(done,45,-500,7),"new request blocks old owner");
   // Rejection does not advance/open a request. Keep it and retry once queued work drains.
   var gateEntered=new CountDownLatch(1);var gateRelease=new CountDownLatch(1);var gate=worker.submit(()->{gateEntered.countDown();gateRelease.await();return null;});require(gateEntered.await(5,TimeUnit.SECONDS),"rejection gate entered");var filler1=worker.submit(()->{});var filler2=worker.submit(()->{});var rejected=new LodIndexRequest(root,46,0,0,8192,256);boolean rejection=false;try{worker.submit(rejected::turn);}catch(RejectedExecutionException expected){rejection=true;}require(rejection&&!rejected.isOpen()&&rejected.visitedEntries()==0&&!rejected.isTerminal(),"bounded executor rejection retains unstarted request");gateRelease.countDown();gate.get();filler1.get();filler2.get();worker.submit(rejected::turn).get();require(rejected.isOpen(),"retry after rejection opens once");rejected.cancel();worker.submit(()->{rejected.closeOnWorker();return null;}).get();
   // Cleanup is explicitly queued ahead of graceful shutdown; it is never Future.cancelled.
   var last=new LodIndexRequest(root,47,0,0,8192,256);worker.submit(last::turn).get();last.cancel();var cleanup=worker.submit(()->{last.closeOnWorker();return null;});worker.shutdown();cleanup.get();require(worker.awaitTermination(10,TimeUnit.SECONDS)&&!last.isOpen(),"graceful shutdown drains cursor cleanup");
   System.out.println("LOD_REQUEST_PASS checks="+checks+" directoryEntries=8205 turnsPerCompleteScan=33 geometryInterleaves=32 workerThreads=1 executorQueueCap=2 quota=256 epochCameraAttribution=true midScanAndQueuedCancel=true wrongThreadCloseRejected=true rejectionRetry=true gracefulClose=true");
  }finally{worker.shutdownNow();worker.awaitTermination(10,TimeUnit.SECONDS);try(var paths=Files.walk(root)){for(var p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}}
 }
}
