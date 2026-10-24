from pathlib import Path
import subprocess,json,hashlib,re
R=Path(__file__).resolve().parent
def method(source,signature):
    start=source.index(signature);end=source.index('{',start)+1;depth=1
    while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
    return source[start:end]
prefix='''import java.util.*;import java.util.concurrent.*;
public class CapacityExtracted {
 static final int MAX_DISK_KEYS=8192;
 static class Value<T>{T v;Value(T x){v=x;}T get(){return v;}}
 static class LodConfig{static Value<Boolean>PERSIST=new Value<>(true);static Value<Integer>MEMORY=new Value<>(4),RADIUS=new Value<>(16);}
 record ChunkPosition(int x,int z){}
 static class Player{int x,z;ChunkPosition chunkPosition(){return new ChunkPosition(x,z);}}
 static class Minecraft{static Minecraft ONE=new Minecraft();static int probes;Player player=new Player();static Minecraft getInstance(){probes++;return ONE;}}
 record Entry(long revision,long bytes,boolean persisted){static int closed;void close(){closed++;}}
 record ColumnarChunk(long revision){}
 static class LodStore{int reads;ColumnarChunk read(int x,int z){reads++;return new ColumnarChunk(7);}}
 static class LodMesher{static Object build(ColumnarChunk p){return p;}}
 record Ready(long session,long key,long revision,Object levels,boolean captured,boolean persisted){}
 static class Pending{Ready ready;}
 Map<Long,Entry>cache=new LinkedHashMap<>();Map<Long,Long>dirty=new HashMap<>(),latest=new HashMap<>();
 LodStore store=new LodStore();ExecutorService worker=Executors.newSingleThreadExecutor();
 long residentBytes,session,diskReads;Long workingKey;Pending pendingGpu;Future<Ready>future;String status;int captureBurst;
 static final long LIMIT=4L*1048576,TARGET=1024;static final net.minecraft.world.phys.Vec3 CAMERA=new net.minecraft.world.phys.Vec3(32,0,0);
 void add(long key,long bytes,boolean persisted){cache.put(key,new Entry(7,bytes,persisted));latest.put(key,7L);residentBytes+=bytes;}
 void clean(){cache.values().forEach(Entry::close);cache.clear();dirty.clear();latest.clear();disk.clear();deferredDisk.clear();residentBytes=0;future=null;workingKey=null;LodConfig.PERSIST.v=true;LodConfig.MEMORY.v=4;Minecraft.ONE.player=new Player();}
 void queued(long ballast){clean();add(key(0,0),TARGET,true);evictToFit(LIMIT-512,4095,null,CAMERA);add(key(1,0),ballast,false);}
 Ready complete()throws Exception{Ready r=future.get(2,TimeUnit.SECONDS);future=null;workingKey=null;forgetUnused(r.key);return r;}
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
'''
suffix='''
 public static void main(String[]args)throws Exception{
  var d=new CapacityExtracted();long target=key(0,0);
  try{
   d.queued(LIMIT-512);int reads=d.store.reads;
   for(int i=0;i<1000;i++)check(!d.startDiskJob(),"No fixed-view disk work without capacity");
   check(d.store.reads==reads&&d.disk.contains(target),"Deferred under real modeled occupancy");
   d.evictToFit(0,0,null,CAMERA);check(d.residentBytes==0,"Occupancy released");boolean started=d.startDiskJob();
   System.out.println("CAPACITY_RECOVERY_OBSERVED sameView=true sameBudget=true freedBytes="+(LIMIT-512)+" started="+started);
   check(started,"Freed GPU capacity did not wake a fixed-view deferred page");d.complete();
   d.queued(LIMIT-TARGET+1);check(!d.startDiskJob(),"One byte short must remain deferred");
   d.evictToFit(0,0,null,CAMERA);d.add(key(1,0),LIMIT-TARGET,false);
   check(d.startDiskJob(),"Exact available bytes must admit");d.complete();
   d.queued(0);d.cache.clear();d.residentBytes=0;
   for(int i=0;i<4096;i++)d.add(key(1000+i,1000),0,false);
   check(!d.startDiskJob(),"Entry capacity must not evict another page");
   d.evictToFit(0,4095,null,CAMERA);check(d.startDiskJob(),"Freed entry capacity must wake");d.complete();
   d.queued(LIMIT-512);LodConfig.MEMORY.v=8;check(d.startDiskJob(),"Budget increase wakes");d.complete();
   d.queued(LIMIT-512);Minecraft.ONE.player.x=1;check(d.startDiskJob(),"Existing camera movement behavior retained");d.complete();
   d.queued(0);d.latest.put(target,8L);d.forgetUnused(target);check(d.latest.get(target)==8L,"Queued edit guard retained");
   check(d.startDiskJob(),"Read may be checked after capacity frees");Ready stale=d.future.get(2,TimeUnit.SECONDS);
   check(d.obsolete(stale),"Stale disk result must lose to observed edit");d.complete();check(!d.latest.containsKey(target),"Guard released after candidate discarded");
   d.queued(0);Minecraft.ONE.player=null;check(!d.startDiskJob()&&d.disk.contains(target),"Missing player retains queue");
   d.clean();for(int i=0;i<9000;i++){d.add(key(1000+i,1000),TARGET,true);d.evictToFit(0,0,null,CAMERA);}
   check(d.disk.size()==8192&&d.deferredDisk.size()==8192&&d.latest.size()==8192,"Index and edit metadata bounds");
   LodConfig.PERSIST.v=false;
   for(int n=0;n<128;n++){int before=d.disk.size();check(!d.startDiskJob(),"Disabled read");check(before-d.disk.size()<=64,"64-key cleanup bound");}
   check(d.disk.isEmpty()&&d.deferredDisk.isEmpty()&&d.latest.isEmpty(),"Disabled metadata cleanup");
   d.clean();for(int i=0;i<8192;i++)d.disk.add(key(1000+i,1000));
   for(int i=0;i<1000;i++){int before=Minecraft.probes;check(!d.startDiskJob(),"No out-of-range read");check(Minecraft.probes-before==64,"64-key selection bound");}
   check(d.disk.size()==8192,"Rotation retention");d.disk.clear();
   for(int i=0;i<64;i++)d.disk.add(key(1000+i,1000));d.disk.add(target);
   check(!d.startDiskJob()&&d.startDiskJob(),"Fairness behind rotated prefix");d.complete();
   System.out.println("CAPACITY_EXTRACTED_PASS blockedCalls=1000 exactByteBoundary=true entryBound=4096 indexBound=8192 cleanupCalls=128 rotationCalls=1000 selectionBound=64 editGuard=true missingPlayer=true cameraAndBudgetBehaviorRetained=true saturationRecovery=false");
  }finally{d.worker.shutdownNow();}
 }
}'''
rows=[];java=R/'toolchain/jdk-25.0.4.1+1/bin'
for label,path in [('before',R/'before-VoxelLodRenderer.java'),('after',R/'candidate-VoxelLodRenderer.java')]:
    source=path.read_text();folder=R/('capacity-extracted-'+label);folder.mkdir(exist_ok=True)
    record=re.search(r'private record DeferredView\([^\n]+\) \{\}',source).group(0)
    fields='private final LinkedHashSet<Long> disk=new LinkedHashSet<>();private final HashMap<Long,DeferredView>deferredDisk=new HashMap<>();'
    methods='\n'.join(method(source,s) for s in ['private void evictToFit(','private void forgetUnused(long k)','private boolean obsolete(Ready r)','private boolean startDiskJob()','private boolean inRange(long k)','public static long key(int x, int z)','private static int x(long k)','private static int z(long k)'])
    file=folder/'CapacityExtracted.java';file.write_text(prefix+record+fields+methods+suffix)
    stub=folder/'net/minecraft/world/phys/Vec3.java';stub.parent.mkdir(parents=True,exist_ok=True);stub.write_text('package net.minecraft.world.phys;public class Vec3{public double x,y,z;public Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}}')
    c=subprocess.run([str(java/'javac'),'-d',str(folder),str(stub),str(file)],capture_output=True,text=True);assert c.returncode==0,c.stderr
    r=subprocess.run([str(java/'java'),'-cp',str(folder),'CapacityExtracted'],capture_output=True,text=True,timeout=30)
    row={'variant':label,'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr,'scope':'Exact production DeferredView, eviction/forgetting/obsolescence/queue/range methods; explicit Entry, GPU-byte-accounting, world and store doubles; not native execution.'};rows.append(row)
    assert (r.returncode==0)==(label=='after'),row
    if label=='before':assert 'Freed GPU capacity did not wake a fixed-view deferred page' in r.stderr,row
(R/'capacity-source-extracted.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps(rows,indent=2))
