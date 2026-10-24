from pathlib import Path
import subprocess,json,hashlib,re
R=Path(__file__).resolve().parent
def method(source,signature):
    start=source.index(signature);end=source.index('{',start)+1;depth=1
    while depth:
        depth+=(source[end]=='{')-(source[end]=='}');end+=1
    return source[start:end]
prefix='''import java.util.*;import java.util.concurrent.*;
public class EvictionExtracted {
 static final int MAX_DISK_KEYS=8192;
 record DeferredView(long cameraChunk,int memoryMiB){}
 HashMap<Long,DeferredView>deferredDisk=new HashMap<>();
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
 void add(long key,long revision,boolean persisted){cache.put(key,new Entry(revision,1024,persisted));residentBytes+=1024;}
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
'''
suffix='''
 public static void main(String[]args)throws Exception{
  var d=new EvictionExtracted();var camera=new net.minecraft.world.phys.Vec3(0,0,0);long key=key(96,0);
  try{
   d.add(key,7,true);d.evictToFit(0,0,null,camera);
   check(d.residentBytes==0&&Entry.closed==1,"GPU accounting/release");
   check(d.disk.contains(key),"Evicted persisted page lost its recovery key");
   check(!d.startDiskJob()&&d.disk.size()==1,"Far page retained without reading");
   Minecraft.ONE.player.x=96;check(d.startDiskJob(),"Camera return recovers page");
   check(d.future.get(2,TimeUnit.SECONDS).persisted(),"Disk result retains provenance");d.future=null;d.workingKey=null;
   for(String control:List.of("dirty","newer","not-persisted","disabled","no-store")){
    d.disk.clear();d.dirty.clear();d.latest.clear();d.add(key,7,!control.equals("not-persisted"));
    if(control.equals("dirty"))d.dirty.put(key,8L);
    if(control.equals("newer"))d.latest.put(key,8L);
    if(control.equals("disabled"))LodConfig.PERSIST.v=false;
    if(control.equals("no-store"))d.store=null;
    d.evictToFit(0,0,null,camera);check(d.disk.isEmpty(),"Invalid restore admitted: "+control);
    check(d.residentBytes==0,"Accounting after admission control");LodConfig.PERSIST.v=true;d.store=new LodStore();
   }
   d.dirty.clear();d.latest.clear();d.disk.add(key);d.latest.put(key,8L);
   d.forgetUnused(key);check(d.latest.getOrDefault(key,0L)==8L,"Queued edit revision forgotten after dirty overflow");
   check(d.obsolete(new Ready(0,key,7,new Object(),false,true)),"Stale restore accepted after dirty overflow");
   d.disk.clear();d.forgetUnused(key);check(!d.latest.containsKey(key),"Finished key metadata retained");
   for(int i=0;i<8192;i++)d.disk.add(key(1000+i,1000));
   d.add(key,7,true);d.evictToFit(0,0,null,camera);check(d.disk.size()==8192,"Index admission bound");
   check(!d.disk.contains(key),"Saturation scope: no unbounded new key");d.disk.clear();
   for(int i=0;i<1000;i++){d.add(key,7,true);d.evictToFit(0,0,null,camera);}
   check(d.disk.size()==1,"Unique recovery scheduling");d.disk.clear();
   Minecraft.ONE.player.x=0;
   for(int i=0;i<8192;i++)d.disk.add(key(1000+i,1000));
   for(int n=0;n<1000;n++){int before=Minecraft.probes;check(!d.startDiskJob(),"Out-of-range read");check(Minecraft.probes-before==64,"64 inspections per call");}
   check(d.disk.size()==8192,"Rotation preserves bounded index");d.disk.clear();
   for(int i=0;i<64;i++)d.disk.add(key(1000+i,1000));d.disk.add(key(0,0));
   check(!d.startDiskJob()&&d.startDiskJob(),"Fairness behind rotated prefix");d.future.get(2,TimeUnit.SECONDS);
   d.disk.clear();d.add(key,7,true);d.evictToFit(4L*1048576-1023,4096,null,camera);
   check(d.residentBytes==0&&d.disk.contains(key),"Payload pressure recovery");
   d.disk.clear();d.deferredDisk.clear();long local=key(0,0);d.add(local,7,true);d.evictToFit(0,0,null,new net.minecraft.world.phys.Vec3(32,0,0));
   Minecraft.ONE.player.x=0;int reads=d.store.reads;
   for(int i=0;i<1000;i++)check(!d.startDiskJob(),"Fixed-view eviction/read/upload churn");
   check(d.store.reads==reads&&d.disk.contains(local),"Fixed-view work remains deferred");
   LodConfig.MEMORY.v=8;check(d.startDiskJob(),"Larger memory budget reconsiders deferred page");d.future.get(2,TimeUnit.SECONDS);LodConfig.MEMORY.v=4;
   d.cache.clear();d.residentBytes=0;d.disk.clear();d.latest.clear();d.deferredDisk.clear();
   for(int i=0;i<8192;i++){long k=key(1000+i,1000);d.disk.add(k);d.latest.put(k,8L);d.deferredDisk.put(k,new DeferredView(0,4));}
   LodConfig.PERSIST.v=false;
   for(int i=0;i<128;i++){int before=d.disk.size();check(!d.startDiskJob(),"Disabled cache read");check(before-d.disk.size()<=64,"Bounded disabled cleanup");}
   check(d.disk.isEmpty()&&d.latest.isEmpty()&&d.deferredDisk.isEmpty(),"Disabled index metadata cleanup");
   System.out.println("EVICTION_EXTRACTED_PASS repeatedAdmissions=1000 admissionControls=5 maxIndex=8192 rotationCalls=1000 inspectionsPerCall=64 payloadAndEntryPressure=true queuedEditGuard=true disabledCleanupCalls=128 fixedViewIdleCalls=1000 rebudgetWake=true saturationRecovery=false");
  }finally{d.worker.shutdownNow();}
 }
}'''
rows=[];java=R/'toolchain/jdk-25.0.4.1+1/bin'
for label,path in [('before',R/'before-VoxelLodRenderer.java'),('intermediate',R/'intermediate-VoxelLodRenderer.java'),('after',R/'project/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java')]:
    source=path.read_text();folder=R/('eviction-extracted-'+label);folder.mkdir(exist_ok=True)
    queue=re.search(r'private final (?:ArrayDeque|LinkedHashSet)<Long> disk = new (?:ArrayDeque|LinkedHashSet)<>\(\);',source).group(0)
    # The previous Ready lacks only the newly recorded provenance component.
    methods='\n'.join(method(source,s) for s in ['private void evictToFit(','private void forgetUnused(long k)','private boolean obsolete(Ready r)','private boolean startDiskJob()','private boolean inRange(long k)','public static long key(int x, int z)','private static int x(long k)','private static int z(long k)'])
    if label=='before':methods=methods.replace('LodMesher.build(page), false)','LodMesher.build(page), false, true)')
    file=folder/'EvictionExtracted.java';file.write_text(prefix+queue+methods+suffix)
    stub=folder/'net/minecraft/world/phys/Vec3.java';stub.parent.mkdir(parents=True,exist_ok=True);stub.write_text('package net.minecraft.world.phys;public class Vec3{public double x,y,z;public Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}}')
    c=subprocess.run([str(java/'javac'),'-d',str(folder),str(stub),str(file)],capture_output=True,text=True);assert c.returncode==0,c.stderr
    r=subprocess.run([str(java/'java'),'-cp',str(folder),'EvictionExtracted'],capture_output=True,text=True,timeout=30)
    row={'variant':label,'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr,'scope':'Exact eviction, forgetting, queue selection/range methods and collection type; explicit worker/store/world/entry doubles. Previous Ready constructor adapted only to compile the common harness.'};rows.append(row)
    assert (r.returncode==0)==(label=='after'),row
    if label=='before':assert 'Evicted persisted page lost its recovery key' in r.stderr,row
    if label=='intermediate':assert 'Queued edit revision forgotten after dirty overflow' in r.stderr,row
(R/'eviction-source-extracted.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps(rows,indent=2))
