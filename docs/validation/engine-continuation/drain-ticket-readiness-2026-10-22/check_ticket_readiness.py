"""Extract exact ticket/readiness bodies; exercise delayed FULL availability with doubles."""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile

parser=argparse.ArgumentParser()
parser.add_argument('source',type=Path)
parser.add_argument('--lifecycle-fixture',type=Path,default=Path(__file__).resolve().parent.parent/'drain-reload-audit-2026-10-22/check_deferred_load.py')
args=parser.parse_args()
spec=importlib.util.spec_from_file_location('lifecycle_fixture',args.lifecycle_fixture)
base=importlib.util.module_from_spec(spec);spec.loader.exec_module(base)
source=args.source.read_text()
stubs=base.STUBS.replace('static class LongOpenHashSet extends LinkedHashSet<Long>{}', '''static class LongOpenHashSet extends LinkedHashSet<Long>{}
  static class LongLinkedOpenHashSet extends LinkedHashSet<Long>{long removeFirstLong(){var it=iterator();long key=it.next();it.remove();return key;}}
  enum Status{FULL}
  record Ticket(ServerLevel level,long key,int ticket){ServerLevel getLevel(){return level;}long getChunkPos(){return key;}int getNewTicketLevel(){return ticket;}}
  static class ChunkLevel{static boolean isLoaded(int level){return level<=33;}}''')
stubs=stubs.replace('boolean duringLoad; int reads;', 'boolean duringLoad; boolean ready; int probes; int reads;')
stubs=stubs.replace('boolean ready; int probes;', 'boolean ready; int lastX,lastZ; int probes;')
stubs=stubs.replace('Server getServer(){return server;}', '''Server getServer(){return server;}
    ServerLevel getChunkSource(){return this;}
    Object getChunk(int x,int z,Status status,boolean force){worldRead();probes++;lastX=x;lastZ=z;check(!force,"never force load");return ready?this:null;}''')
stubs=stubs.replace('final LongOpenHashSet loadedDrainChunks=new LongOpenHashSet();', 'final LongLinkedOpenHashSet loadedDrainChunks=new LongLinkedOpenHashSet();')
stubs=stubs.replace('int drainWaitLinks;boolean drainLoadOverflow;', 'int drainWaitLinks;int drainReadyProbes;boolean drainLoadOverflow;')
signatures=['  private static void forgetDrainWait(', '  private static void resumeDrainWaiters(', '  public static void activate(',
 '  private static void rememberLoadedDrainChunk(', '  private static void resumeLoadedDrainChunks(', '    public void chunkTicketChanged(']
extracted='\n'.join(base.method(source,s) for s in signatures)
for old,new in [('net.minecraft.server.level.ChunkLevel','ChunkLevel'),('net.minecraft.world.level.chunk.status.ChunkStatus','Status'),('net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent','Ticket')]:extracted=extracted.replace(old,new)
checks=r'''
  static void flush(ServerLevel l,Budget b){b.drainReadyProbes=0;resumeLoadedDrainChunks(l,b);check(b.drainReadyProbes<=16,"per-host probe cap");}
  public static void main(String[] args){
    var level=new ServerLevel();var b=new Budget();BUDGETS.put(level,b);var origin=new BlockPos(4096);long key=ChunkPos.pack(261,0);
    waiting(b,origin,key);level.duringLoad=true;
    for(int i=0;i<2048;i++)new WaterLoadProbe().chunkTicketChanged(new Ticket(level,key,31));
    check(level.reads==0&&b.loadedDrainChunks.size()==1,"ticket callback defers/coalesces");
    level.duringLoad=false;flush(level,b);flush(level,b);
    check(b.active.isEmpty()&&b.waitingDrains.size()==1&&b.loadedDrainChunks.size()==1,"retain not-yet-FULL wake");
    new WaterLoadProbe().chunkTicketChanged(new Ticket(level,key,34));
    int reads=level.reads;flush(level,b);check(level.reads==reads,"withdrawn ticket stops probes");
    new WaterLoadProbe().chunkTicketChanged(new Ticket(level,key,32));level.ready=true;flush(level,b);clean(b,1);
    reads=level.reads;flush(level,b);check(level.reads==reads,"settled no-query idle");
    b=new Budget();BUDGETS.put(level,b);
    for(int i=0;i<128;i++){long[] keys=new long[8];for(int j=0;j<8;j++)keys[j]=ChunkPos.pack(i*8+j,0);
      waiting(b,new BlockPos(i),keys);for(long k:keys)rememberLoadedDrainChunk(b,k);}
    rememberLoadedDrainChunk(b,ChunkPos.pack(0,0));check(!b.drainLoadOverflow,"duplicate at capacity");
    int turns=0;while(!b.loadedDrainChunks.isEmpty()){flush(level,b);check(++turns<=64,"fair bounded progress");}clean(b,128);
    b=new Budget();BUDGETS.put(level,b);level.ready=false;
    for(int i=0;i<1025;i++){forgetDrainWait(b,origin);waiting(b,origin,ChunkPos.pack(i,0));rememberLoadedDrainChunk(b,ChunkPos.pack(i,0));}
    check(b.loadedDrainChunks.size()<=1024,"event residency cap");
    // Force the rare overflow fallback with all old keys obsolete and the newest
    // current wait key absent from the deferred queue, then require eventual wake.
    b.drainLoadOverflow=true;flush(level,b);check(b.waitingDrains.size()==1&&b.loadedDrainChunks.size()==1,"overflow retains current pending key");
    level.ready=true;flush(level,b);clean(b,1);
    b=new Budget();BUDGETS.put(level,b);key=ChunkPos.pack(-2,-3);waiting(b,origin,key);
    new WaterLoadProbe().chunkTicketChanged(new Ticket(level,key,31));flush(level,b);clean(b,1);
    check(level.lastX==-2&&level.lastZ==-3,"signed packed chunk coordinates");
    System.out.println("TICKET_READINESS_MODEL_PASS exactBodies=true duplicateEvents=2048 maxOrigins=128 maxLinks=1024 maxProbesPerHost=16 delayedFull=true withdrawal=true overflow=true idle=true nativeAcceptance=false");
  }
}
'''
with tempfile.TemporaryDirectory(prefix='ticket-readiness-') as directory:
 tmp=Path(directory);(tmp/'WaterLoadProbe.java').write_text(stubs+extracted+checks)
 compile_result=subprocess.run(['java','com.sun.tools.javac.Main',str(tmp/'WaterLoadProbe.java')],text=True,capture_output=True)
 assert compile_result.returncode==0,compile_result.stderr
 result=subprocess.run(['java','-cp',str(tmp),'WaterLoadProbe'],text=True,capture_output=True)
 assert result.returncode==0,result.stderr
 report={'scope':'exact source bodies with world doubles, not Minecraft','source_sha256':hashlib.sha256(source.encode()).hexdigest(),'extracted_sha256':hashlib.sha256(extracted.encode()).hexdigest(),'compile_exit':compile_result.returncode,'run_exit':result.returncode,'record':result.stdout.strip()}
 print(json.dumps(report,indent=2))
