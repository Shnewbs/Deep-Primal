from pathlib import Path
import hashlib,json
r=Path(__file__).resolve().parent.parent
p=r/'staged/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java';s=p.read_text()
def method(text,signature):
 a=text.index(signature);b=text.index('{',a);depth=1;i=b+1
 while depth:
  if text[i]=='{':depth+=1
  if text[i]=='}':depth-=1
  i+=1
 return text[a:i]
service=method(s,'  private void serviceIndex(')
reset=method(s,'  public void reset()')
before=(r/'evidence/before-VoxelLodRenderer.java').read_text()
for signature in ['  private boolean startDiskJob()', '  private boolean obsolete(', '  private void forgetUnused(', '  private void deferPersisted(']:
 assert method(s,signature)==method(before,signature),signature
(r/'evidence/adapter-extraction.json').write_text(json.dumps({'renderer_sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'service_sha256':hashlib.sha256(service.encode()).hexdigest(),'reset_sha256':hashlib.sha256(reset.encode()).hexdigest(),'unchanged_guards':['startDiskJob','obsolete','forgetUnused','deferPersisted'],'scope':'Exact two method bodies compiled with minimal non-game state doubles. Not full renderer or native compilation.'},indent=2)+'\n')
header='''import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*; import java.util.*; import java.util.concurrent.*;
public class AdapterChecks {
 static class LodConfig { static class Flag { boolean on=true; boolean get(){return on;} } static Flag PERSIST=new Flag(); }
 static class Entry { boolean closed; void close(){closed=true;} }
 static int checks; static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 private static final int MAX_DISK_KEYS=8192;
 LodIndexWorker worker; Future<LodIndexRequest.Step> scan; LodIndexRequest indexRequest;
 int indexCameraX,indexCameraZ; boolean indexStarted; Path root; long session=1;
 LinkedHashSet<Long> disk=new LinkedHashSet<>(); HashMap<Long,Long> latest=new HashMap<>(),dirty=new HashMap<>();
 HashMap<Long,Entry> cache=new HashMap<>(); ArrayList<Object> draws=new ArrayList<>(); HashMap<Long,Object> deferredDisk=new HashMap<>();
 Entry pendingGpu; Future<?> future; Long workingKey; int captureBurst; boolean persistenceFailed;
 long residentBytes; int visibleSpan,visibleChunks,queuedChunks; Object store,level; String status;
 void start(Path p){root=p;level=new Object();worker=new LodIndexWorker(r->{Thread t=new Thread(r,"adapter-worker");t.setDaemon(true);return t;});}
 void finish(int x,int z)throws Exception {for(int n=0;n<10000;n++){serviceIndex(x,z);if(scan!=null)scan.get(10,TimeUnit.SECONDS);else if(indexRequest!=null&&indexRequest.isCancelled()&&!indexRequest.isTerminal())worker.submit(()->{}).get(10,TimeUnit.SECONDS);if(indexRequest!=null&&indexRequest.isTerminal()&&!indexRequest.isCancelled()&&scan==null&&indexCameraX==x&&indexCameraZ==z)return;}throw new AssertionError("adapter failed to finish");}
'''
body='''
 public static void main(String[] args)throws Exception {
 Path p=Files.createTempDirectory("r56-adapter-");
 try {
  for(int i=0;i<8205;i++)Files.createFile(p.resolve(i+"_0.plod"));
  AdapterChecks a=new AdapterChecks();a.start(p);a.serviceIndex(0,0);var first=a.indexRequest;a.scan.get();check(first.isOpen(),"initial partial cursor");
  a.serviceIndex(8204,0);if(a.scan!=null)a.scan.get();a.finish(8204,0);
  check(first.isCancelled()&&first.isTerminal()&&!first.isOpen(),"camera cancellation cleanup");
  check(a.disk.contains((long)8204<<32),"nearest beyond original prefix found");check(a.disk.size()==8192,"shared key cap");
  var done=a.indexRequest;a.serviceIndex(8204,0);check(a.indexRequest==done&&a.scan==null,"no rescan at unchanged view");
  LodConfig.PERSIST.on=false;a.serviceIndex(8204,0);check(a.indexRequest==null&&!a.indexStarted,"disable clears request");
  LodConfig.PERSIST.on=true;a.disk.clear();long recovery=Long.MIN_VALUE;a.disk.add(recovery);a.latest.put(recovery,77L);a.dirty.put(recovery,78L);a.finish(17,0);
  check(a.disk.iterator().next()==recovery&&a.disk.size()==8192,"recovery order and shared cap");check(a.latest.get(recovery)==77&&a.dirty.get(recovery)==78,"edit guards retained");
  var oldWorker=a.worker;var old=a.indexRequest;a.reset();check(oldWorker.awaitTermination(10,TimeUnit.SECONDS),"renderer reset graceful termination");check(a.session==2&&a.worker==null&&a.level==null&&a.disk.isEmpty()&&a.latest.isEmpty(),"renderer reset detached");check(!old.isOpen(),"reset completed cursor closed");
  a.start(p);a.serviceIndex(0,0);a.scan.get();old=a.indexRequest;oldWorker=a.worker;
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
  a.worker.submit(()->{entered.countDown();try{release.await();}catch(InterruptedException e){throw new AssertionError(e);}});
  check(entered.await(10,TimeUnit.SECONDS),"reset saturation entered");a.worker.submit(()->{});a.worker.submit(()->{});
  var gpu=new Entry();a.pendingGpu=gpu;var resident=new Entry();a.cache.put(1L,resident);a.reset();check(gpu.closed&&resident.closed,"render-owned GPU doubles closed");
  check(old.isCancelled(),"reset signals running index");release.countDown();check(oldWorker.awaitTermination(10,TimeUnit.SECONDS),"reset full queue drained");check(old.isTerminal()&&!old.isOpen(),"reset full queue cursor closed");
  a.start(p);a.serviceIndex(0,0);a.scan.get();old=a.indexRequest;
  a.serviceIndex(1,0);a.finish(0,0);check(a.indexRequest!=old&&!a.indexRequest.isCancelled(),"out-and-back camera restarts cancelled owner");
  oldWorker=a.worker;a.reset();check(oldWorker.awaitTermination(10,TimeUnit.SECONDS),"backtrack drain");
  a.start(p);a.serviceIndex(0,0);a.scan.get();old=a.indexRequest;LodConfig.PERSIST.on=false;a.serviceIndex(0,0);a.worker.submit(()->{}).get();a.serviceIndex(0,0);check(old.isTerminal()&&!old.isOpen()&&a.indexRequest==null,"midscan disable drains");oldWorker=a.worker;a.reset();check(oldWorker.awaitTermination(10,TimeUnit.SECONDS),"disable worker drained");
  System.out.println("PASS R56 extracted renderer adapter checks="+checks+" camera/reset/disable/recovery/cap");
 }finally{try(var ps=Files.walk(p)){for(Path x:ps.sorted(Comparator.reverseOrder()).toList())Files.delete(x);}}
 }
}
'''
(r/'evidence/AdapterChecks.java').write_text(header+service+'\n'+reset+body)
# Logger is the sole external reference in the extracted service method.
q=r/'evidence/stubs/com/shnewbs/deepprimal/lod/LodModule.java';q.parent.mkdir(parents=True,exist_ok=True)
q.write_text('package com.shnewbs.deepprimal.lod; public class LodModule { public static final Log LOGGER=new Log(); public static class Log { public void warn(String s,Throwable t){throw new AssertionError(s,t);} } }\n')
