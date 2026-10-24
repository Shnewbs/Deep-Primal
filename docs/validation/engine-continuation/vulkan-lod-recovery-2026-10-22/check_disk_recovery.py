from pathlib import Path
import subprocess,json,hashlib,difflib
R=Path(__file__).resolve().parent
before=(R/'before-VoxelLodRenderer.java').read_text()
old='''    int inspected = 0;
    while (!disk.isEmpty() && inspected++ < 64) {
      long k = disk.removeFirst();
      if (cache.containsKey(k) || dirty.containsKey(k) || !inRange(k)) continue;'''
new='''    // A later camera move can bring an indexed page into range. Keep those keys,
    // but inspect each at most once per call and retain the existing 64-key bound.
    int remaining = Math.min(64, disk.size());
    while (!disk.isEmpty() && remaining-- > 0) {
      long k = disk.removeFirst();
      if (cache.containsKey(k) || dirty.containsKey(k)) continue;
      if (!inRange(k)) {
        disk.addLast(k);
        continue;
      }'''
assert before.count(old)==1
after=before.replace(old,new)
(R/'candidate-VoxelLodRenderer.java').write_text(after)
(R/'disk-recovery.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),after.splitlines(True),fromfile='a/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java',tofile='b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java')))
def method(source,signature):
 start=source.index(signature);brace=source.index('{',start);depth=1;end=brace+1
 while depth:
  depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end]
prefix='''import java.util.*;import java.util.concurrent.*;
public class DiskRecoveryExtracted {
 static class Value<T>{T value;Value(T v){value=v;}T get(){return value;}}
 static class LodConfig{static Value<Boolean>PERSIST=new Value<>(true);static Value<Integer>RADIUS=new Value<>(16);}
 record ChunkPosition(int x,int z){}
 static class Player{int x,z;ChunkPosition chunkPosition(){return new ChunkPosition(x,z);}}
 static class Minecraft{static Minecraft ONE=new Minecraft();static int calls;Player player=new Player();static Minecraft getInstance(){calls++;return ONE;}}
 record ColumnarChunk(long revision){}
 static class LodStore{int reads;ColumnarChunk read(int x,int z){reads++;return new ColumnarChunk(7);}}
 static class LodMesher{static Object build(ColumnarChunk p){return p;}}
 record Ready(long session,long key,long revision,Object levels,boolean captured){}
 ArrayDeque<Long>disk=new ArrayDeque<>();Map<Long,Object>cache=new HashMap<>(),dirty=new HashMap<>();
 LodStore store=new LodStore();ExecutorService worker=Executors.newSingleThreadExecutor();
 long session,diskReads;Long workingKey;Future<Ready>future;String status;int captureBurst;
'''
suffix='''
 static void require(boolean v,String m){if(!v)throw new AssertionError(m);}
 boolean turn() {int before=Minecraft.calls;boolean result=startDiskJob();require(Minecraft.calls-before<=64,"64-key inspection bound");return result;}
 void complete()throws Exception{require(future.get(2,TimeUnit.SECONDS)!=null,"Completed disk read");future=null;}
 public static void main(String[]args)throws Exception{
  var d=new DiskRecoveryExtracted();int calls=0;
  try{
   d.disk.add(key(96,0));require(!d.turn(),"No out-of-range read");
   int retained=d.disk.size();Minecraft.ONE.player.x=96;boolean started=d.turn();
   System.out.println("DISK_RECOVERY_OBSERVED retainedAfterOutside="+retained+" startedAfterMove="+started);
   require(retained==1&&started,"Out-of-range key lost before camera move");d.complete();
   require(d.store.reads==1&&d.disk.isEmpty(),"Exactly one real selection");
   Minecraft.ONE.player.x=0;d.disk.add(key(96,0));
   for(int i=0;i<1000;i++){int b=Minecraft.calls;require(!d.turn(),"Out of range");require(Minecraft.calls-b==1,"Small queue revisited in one call");}
   require(d.disk.size()==1,"Retained without duplicate growth");d.disk.clear();
   for(int i=0;i<8192;i++)d.disk.add(key(1000+i,1000));
   for(int i=0;i<1000;i++)require(!d.turn(),"Large out-of-range index");
   require(d.disk.size()==8192,"Bounded index retained");d.disk.clear();
   for(int i=0;i<64;i++)d.disk.add(key(1000+i,1000));d.disk.add(key(0,0));
   require(!d.turn(),"At most64 inspected");require(d.turn(),"Near key after rotated prefix");d.complete();
   require(d.disk.size()==64,"No unrelated keys lost");
   LodConfig.PERSIST.value=false;require(!d.turn()&&d.disk.isEmpty(),"Disabled persistence clears queue");LodConfig.PERSIST.value=true;
   d.disk.add(key(0,0));Minecraft.ONE.player=null;require(!d.turn()&&d.disk.size()==1,"Absent player retains index");Minecraft.ONE.player=new Player();d.disk.clear();
   d.cache.put(key(0,0),new Object());d.dirty.put(key(1,0),new Object());
   d.disk.add(key(0,0));d.disk.add(key(1,0));d.disk.add(key(2,0));require(d.turn(),"Skip cached/dirty then service eligible");d.complete();require(d.disk.isEmpty(),"Cached/dirty legacy behavior");
   d.cache.clear();d.dirty.clear();var random=new Random(20261022);
   for(int trial=0;trial<1000;trial++){
    d.disk.clear();var keys=new ArrayList<Long>();for(int i=0;i<256;i++)keys.add(key(i*40-5120,0));Collections.shuffle(keys,random);d.disk.addAll(keys);
    long target=keys.get(random.nextInt(keys.size()));Minecraft.ONE.player.x=x(target);Minecraft.ONE.player.z=0;boolean found=false;
    for(int turn=0;turn<4&&!found;turn++){found=d.turn();calls++;}require(found,"Fair bounded reconsideration after move");d.complete();require(d.disk.size()==255,"Only serviced key removed");
   }
   System.out.println("DISK_RECOVERY_EXTRACTED_PASS seededMoves=1000 maxIndex=8192 idleCalls=2000 boundedCalls="+calls+" perCallInspections=64 smallQueueOnce=true");
  }finally{d.worker.shutdownNow();}
 }
}'''
records=[];java=R/'toolchain/jdk-25.0.4.1+1/bin'
for label,source in [('before',before),('after',after)]:
 folder=R/('extracted-'+label);folder.mkdir(exist_ok=True)
 text=prefix+'\n'.join(method(source,s) for s in ['private boolean startDiskJob()','private boolean inRange(long k)','public static long key(int x, int z)','private static int x(long k)','private static int z(long k)'])+suffix
 file=folder/'DiskRecoveryExtracted.java';file.write_text(text)
 compiled=subprocess.run([str(java/'javac'),'-d',str(folder),str(file)],capture_output=True,text=True)
 assert compiled.returncode==0,compiled.stderr
 result=subprocess.run([str(java/'java'),'-cp',str(folder),'DiskRecoveryExtracted'],capture_output=True,text=True,timeout=30)
 records.append({'variant':label,'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':result.returncode,'stdout':result.stdout,'stderr':result.stderr,'scope':'exact production queue/range methods; explicit world/worker/store doubles'})
 assert (result.returncode==0)==(label=='after'),records[-1]
(R/'disk-recovery-extracted.json').write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records,indent=2))
