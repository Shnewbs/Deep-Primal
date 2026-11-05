from pathlib import Path
import ast,re,subprocess,json,hashlib
R=Path(__file__).resolve().parent
old=ast.parse((R.parent/'coverage-2327/check_capacity_recovery.py').read_text())
prefix=next(ast.literal_eval(n.value) for n in old.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='prefix' for t in n.targets))
prefix=prefix.replace('static class LodMesher{static Object build(ColumnarChunk p){return p;}}','static class LodMesher{record Levels(long bytes){} static Levels build(ColumnarChunk p){return new Levels(1024);}}').replace('Object levels,boolean captured','LodMesher.Levels levels,boolean captured')
def method(source,signature):
 start=source.index(signature);end=source.index('{',start)+1;depth=1
 while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end]
source=(R/'project/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java').read_text()
methods='\n'.join(method(source,s) for s in ['private void deferPersisted(Ready r)','private void evictToFit(','private void forgetUnused(long k)','private boolean obsolete(Ready r)','private boolean startDiskJob()','private boolean inRange(long k)','public static long key(int x, int z)','private static int x(long k)','private static int z(long k)'])
record=re.search(r'private record DeferredView\([^\n]+\) \{\}',source).group(0)
fields='private final LinkedHashSet<Long> disk=new LinkedHashSet<>();private final HashMap<Long,DeferredView>deferredDisk=new HashMap<>();'
suffix='''
 public static void main(String[]args)throws Exception{
  var d=new CapacityExtracted();long target=key(0,0);var fresh=new Ready(0,target,42,new LodMesher.Levels(TARGET),false,true);
  try{
   d.residentBytes=LIMIT;d.deferPersisted(fresh);
   check(d.disk.contains(target)&&d.latest.get(target)==42,"Retain revision after cancellation");
   for(int i=0;i<1000;i++)check(!d.startDiskJob(),"No retry before bytes free");
   d.residentBytes=0;check(d.startDiskJob(),"Wake at same camera after capacity frees");d.complete();
   d.clean();d.deferPersisted(new Ready(0,target,42,new LodMesher.Levels(LIMIT+1),true,true));
   for(int i=0;i<1000;i++)check(!d.startDiskJob(),"Oversized page must not churn");
   LodConfig.MEMORY.v=8;check(d.startDiskJob(),"Oversized persisted page recovers after rebudget");d.complete();
   for(String control:List.of("dirty","newer","displayed-newer","unwritten","disabled","no-store","old-session","saturated")){
    d.clean();Ready r=fresh;var saved=d.store;
    if(control.equals("dirty"))d.dirty.put(target,43L);
    if(control.equals("newer"))d.latest.put(target,43L);
    if(control.equals("displayed-newer"))d.cache.put(target,new Entry(43,0,true));
    if(control.equals("unwritten"))r=new Ready(0,target,42,new LodMesher.Levels(TARGET),true,false);
    if(control.equals("disabled"))LodConfig.PERSIST.v=false;
    if(control.equals("no-store"))d.store=null;
    if(control.equals("old-session"))r=new Ready(1,target,42,new LodMesher.Levels(TARGET),false,true);
    if(control.equals("saturated"))for(int i=0;i<8192;i++)d.disk.add(key(1000+i,1000));
    d.deferPersisted(r);check(!d.disk.contains(target)&&!d.deferredDisk.containsKey(target),"Guard "+control);d.store=saved;
   }
   d.clean();d.cache.put(target,new Entry(41,0,true));d.deferPersisted(fresh);
   check(d.startDiskJob(),"Older resident must allow newer persisted replacement");d.complete();
   d.clean();d.cache.put(target,new Entry(42,0,true));d.latest.put(target,42L);d.disk.add(target);
   check(!d.startDiskJob()&&!d.disk.contains(target),"Current resident skips duplicate disk read");
   d.clean();for(int i=0;i<8191;i++)d.disk.add(key(1000+i,1000));d.disk.add(target);d.deferPersisted(fresh);
   check(d.disk.size()==8192&&d.deferredDisk.containsKey(target),"Existing saturated key still updates its deferral");
   LodConfig.PERSIST.v=false;for(int i=0;i<128;i++)d.startDiskJob();
   check(d.disk.isEmpty()&&d.latest.isEmpty()&&d.deferredDisk.isEmpty(),"Bounded disabled cleanup");
   System.out.println("UPLOAD_RETRY_EXTRACTED_PASS blockedCalls=2000 guards=8 oversizedBudgetRecovery=true olderResidentReplacement=true indexBound=8192 existingFullIndexKey=true disabledCleanupCalls=128");
  }finally{d.worker.shutdownNow();}
 }
}'''
folder=R/'upload-retry-extracted';folder.mkdir(exist_ok=True)
f=folder/'CapacityExtracted.java';f.write_text(prefix+record+fields+methods+suffix)
stub=folder/'net/minecraft/world/phys/Vec3.java';stub.parent.mkdir(parents=True,exist_ok=True);stub.write_text('package net.minecraft.world.phys;public class Vec3{public double x,y,z;public Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}}')
java=R/'toolchain/jdk-25.0.4.1+1/bin'
c=subprocess.run([str(java/'javac'),'-d',str(folder),str(stub),str(f)],capture_output=True,text=True);assert c.returncode==0,c.stderr
r=subprocess.run([str(java/'java'),'-cp',str(folder),'CapacityExtracted'],capture_output=True,text=True,timeout=30)
report={'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr,'scope':'Exact production retry, disk admission, eviction, edit guard and range methods with explicit world/store/GPU-accounting doubles; native cancellation is independently reproduced.'}
(R/'upload-retry-source-extracted.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2));assert r.returncode==0
