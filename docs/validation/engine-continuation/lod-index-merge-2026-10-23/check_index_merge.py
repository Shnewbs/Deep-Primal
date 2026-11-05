from pathlib import Path
import subprocess,json,hashlib,sys
R=Path(__file__).resolve().parent
prefix='''import java.util.*;import java.util.concurrent.*;
class IndexMergeExtracted {
 static final int MAX_DISK_KEYS=8192;
 static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}static int x(long k){return(int)(k>>32);}static int z(long k){return(int)k;}
 record Pos(int x,int z){}record Player(Pos chunkPosition){}record Minecraft(Player player){static Minecraft getInstance(){return new Minecraft(new Player(new Pos(0,0)));}}
 LinkedHashSet<Long>disk=new LinkedHashSet<>();Future<List<Long>>scan;String status;Map<Long,Long>latest=new HashMap<>();Map<Long,Object>deferred=new HashMap<>();
 void merge(){var mc=Minecraft.getInstance();
'''
suffix='''
 }
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[]args)throws Exception {
  int cases=0;
  for(int seeded:new int[]{1,8192,0,4096}) {
   var d=new IndexMergeExtracted();long guarded=key(-1,0);Object guard=new Object();
   for(int i=0;i<seeded;i++)d.disk.add(key(-1-i,0));if(seeded>0){d.latest.put(guarded,43L);d.deferred.put(guarded,guard);}
   List<Long>keys=new ArrayList<>();for(int i=8192;i>0;i--)keys.add(key(i,0));
   if(seeded==4096){keys.clear();keys.addAll(d.disk);for(int i=4096;i>0;i--)keys.add(key(i,0));}
   d.scan=CompletableFuture.completedFuture(keys);d.merge();System.out.println("INDEX_MERGE_OBSERVED seeded="+seeded+" incoming="+keys.size()+" merged="+d.disk.size());
   check(d.disk.size()==8192,"Recovery index exceeded 8192 keys");check(d.scan==null,"Completed scan retained");
   if(seeded>0)check(d.disk.getFirst()==guarded&&d.latest.get(guarded)==43L&&d.deferred.get(guarded)==guard,"Existing order/edit guard altered");
   if(seeded==1)check(d.disk.contains(key(1,0))&&!d.disk.contains(key(8192,0)),"Remaining admissions must be nearest first");cases++;
  }
  var d=new IndexMergeExtracted();d.disk.add(key(1,0));d.scan=CompletableFuture.failedFuture(new java.io.IOException("injected scan failure"));d.merge();check(d.disk.size()==1&&d.scan==null&&d.status.equals("Cache index unavailable"),"Failed scan cleanup changed");
  d.scan=new CompletableFuture<>();d.merge();check(d.scan!=null&&d.disk.size()==1,"Pending scan changed");
  System.out.println("INDEX_MERGE_EXTRACTED_PASS mergeCases="+cases+" failedAndPendingScan=true bound=8192 orderAndEditGuards=true nearestRemainingSlots=true");
 }
}'''
rows=[]
for label,path in [('before',R/'before-VoxelLodRenderer.java'),('after',R/'project/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java')]:
 source=path.read_text();start=source.index('    if (scan != null && scan.isDone()) {');end=source.index('    queuedChunks = dirty.size();',start);body=source[start:end]
 folder=R/('index-extracted-'+label);folder.mkdir(exist_ok=True);f=folder/'IndexMergeExtracted.java';f.write_text(prefix+body+suffix)
 java=R/'toolchain/jdk-25.0.4.1+1/bin';c=subprocess.run([str(java/'javac'),'-d',str(folder),str(f)],capture_output=True,text=True);assert c.returncode==0,c.stderr
 r=subprocess.run([str(java/'java'),'-cp',str(folder),'IndexMergeExtracted'],capture_output=True,text=True,timeout=15)
 row={'variant':label,'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr,'scope':'Exact completed-scan merge block from production tick, with explicit position/future/queue doubles; not disk discovery, actual-world coverage or performance acceptance.'};rows.append(row)
 if '--baseline-only' not in sys.argv:assert (r.returncode==0)==(label=='after'),row
 if label=='before':assert 'Recovery index exceeded 8192 keys' in r.stderr,row
 if '--baseline-only' in sys.argv:break
(R/'index-merge-extracted.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps(rows,indent=2))
