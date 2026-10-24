"""Compile exact lifecycle/helper bodies with world doubles; this is NOT native acceptance."""
import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent


def method(source, signature):
    start = source.index(signature)
    brace = source.index('{', start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


STUBS = r'''
import java.util.*;
public class WaterLoadProbe {
  static final int MAX_ACTIVE=65536, MAX_DRAIN_WAIT_LINKS=1024;
  static final WeakHashMap<ServerLevel,Budget> BUDGETS=new WeakHashMap<>();
  static final Direction[] SIDES=Direction.values();
  enum Direction {NORTH,EAST,SOUTH,WEST;
    int getStepX(){return this==EAST?1:this==WEST?-1:0;}
    int getStepZ(){return this==SOUTH?1:this==NORTH?-1:0;}}
  record BlockPos(long value){BlockPos immutable(){return this;} long asLong(){return value;}}
  record ChunkPos(int x,int z){long pack(){return pack(x,z);}
    static long pack(int x,int z){return (long)x&0xffffffffL|((long)z<<32);}}
  record Chunk(ChunkPos pos){ChunkPos getPos(){return pos;}}
  record Load(Object level,Chunk chunk){Object getLevel(){return level;}Chunk getChunk(){return chunk;}}
  static class LongOpenHashSet extends LinkedHashSet<Long>{}
  static class Server {boolean isSameThread(){return true;}}
  static class ServerLevel {
    boolean duringLoad; int reads; final Server server=new Server();
    Server getServer(){return server;}
    void worldRead(){reads++;if(duringLoad)throw new IllegalStateException("pre-FULL world interaction");}
    boolean isInWorldBounds(BlockPos p){worldRead();return true;}
    boolean hasChunkAt(BlockPos p){worldRead();return true;}
    void scheduleTick(BlockPos p,Object fluid,int ticks){throw new AssertionError("unexpected overflow");}
  }
  static class Registry {Object get(){return this;}}
  static class EngineRegistry {static final Registry WATER=new Registry();}
  static class Budget {
    final LinkedHashSet<Long> active=new LinkedHashSet<>();
    final LinkedHashMap<BlockPos,LongOpenHashSet> waitingDrains=new LinkedHashMap<>();
    final HashMap<Long,HashSet<BlockPos>> drainWakeChunks=new HashMap<>();
    final HashMap<Long,Object> watchedChunks=new HashMap<>(),drainChunks=new HashMap<>();
    final LongOpenHashSet loadedDrainChunks=new LongOpenHashSet();
    int drainWaitLinks;boolean drainLoadOverflow;
  }
  static Budget budget(ServerLevel level){return BUDGETS.get(level);}
  static void invalidateChunk(Budget budget,long key){}
  static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
  static void waiting(Budget b,BlockPos origin,long...keys){
    var set=new LongOpenHashSet();for(long key:keys){set.add(key);
      b.drainWakeChunks.computeIfAbsent(key,k->new HashSet<>()).add(origin);}
    b.waitingDrains.put(origin,set);b.drainWaitLinks+=set.size();
  }
  static void clean(Budget b,int count){check(b.active.size()==count,"exact FIFO admissions");
    check(b.waitingDrains.isEmpty()&&b.drainWakeChunks.isEmpty()&&b.drainWaitLinks==0,"wait-index cleanup");
    check(b.loadedDrainChunks.isEmpty()&&!b.drainLoadOverflow,"deferred cleanup");}
'''

CHECKS = r'''
  public static void main(String[] args){
    boolean fixed=Boolean.parseBoolean(args[0]);var level=new ServerLevel();
    var b=new Budget();BUDGETS.put(level,b);var origin=new BlockPos(4096);
    long key=ChunkPos.pack(262,0);waiting(b,origin,key);level.duringLoad=true;
    if(!fixed){
      boolean rejected=false;
      try {new WaterLoadProbe().chunkLoaded(new Load(level,new Chunk(new ChunkPos(262,0))));}
      catch(IllegalStateException expected){rejected=expected.getMessage().contains("pre-FULL");}
      check(rejected,"reproduce immediate world access from exact listener");
      System.out.println("LOAD_LIFECYCLE_REPRODUCED exactSource=true preFullWorldRead=true");return;
    }
    for(int i=0;i<2048;i++)new WaterLoadProbe().chunkLoaded(new Load(level,new Chunk(new ChunkPos(262,0))));
    check(level.reads==0&&b.waitingDrains.size()==1&&b.loadedDrainChunks.size()==1,"defer/coalesce callback");
    new WaterLoadProbe().chunkLoaded(new Load(level,new Chunk(new ChunkPos(999,0))));
    check(b.loadedDrainChunks.size()==1,"ignore unrelated loads");
    level.duringLoad=false;resumeLoadedDrainChunks(level,b);clean(b,1);
    int reads=level.reads;resumeLoadedDrainChunks(level,b);check(level.reads==reads,"idle flush does no world work");
    b=new Budget();BUDGETS.put(level,b);level.duringLoad=true;
    for(int i=0;i<128;i++){
      long[] keys=new long[8];for(int j=0;j<8;j++)keys[j]=ChunkPos.pack(i*8+j,0);
      waiting(b,new BlockPos(i),keys);
      for(long k:keys)rememberLoadedDrainChunk(b,k);
    }
    check(b.loadedDrainChunks.size()==1024,"link residency cap");
    rememberLoadedDrainChunk(b,ChunkPos.pack(0,0));
    check(!b.drainLoadOverflow,"duplicate at capacity is not overflow");
    level.duringLoad=false;resumeLoadedDrainChunks(level,b);clean(b,128);
    b=new Budget();BUDGETS.put(level,b);level.duringLoad=true;
    for(int i=0;i<1025;i++){
      forgetDrainWait(b,origin);waiting(b,origin,ChunkPos.pack(i,0));
      rememberLoadedDrainChunk(b,ChunkPos.pack(i,0));
    }
    check(b.loadedDrainChunks.size()==1024&&b.drainLoadOverflow,"bounded turnover fallback");
    level.duringLoad=false;resumeLoadedDrainChunks(level,b);clean(b,1);
    System.out.println("LOAD_LIFECYCLE_CANDIDATE_PASS exactSource=true cases=5 duplicateCallbacks=2048 origins=128 links=1024 turnoverEvents=1025 nativeAcceptance=false");
  }
}
'''


def run(path, fixed):
    source = path.read_text()
    names = ['  private static void forgetDrainWait(', '  private static void resumeDrainWaiters(',
             '  public static void activate(', '    public void chunkLoaded(']
    if fixed:
        names += ['  private static void rememberLoadedDrainChunk(', '  private static void resumeLoadedDrainChunks(']
    extracted = '\n'.join(method(source, n) for n in names)
    extracted = extracted.replace('net.neoforged.neoforge.event.level.ChunkEvent.Load', 'Load')
    extracted = extracted.replace('net.minecraft.world.level.ChunkPos', 'ChunkPos')
    # Both builds must compile the same assertion harness. The baseline never calls this stub.
    if not fixed:
        extracted += '\n static void rememberLoadedDrainChunk(Budget b,long k){}\n static void resumeLoadedDrainChunks(ServerLevel l,Budget b){}\n'
    if fixed:
        assert '    resumeLoadedDrainChunks(level, budget);\n    budget.stepped = true;' in source
    with tempfile.TemporaryDirectory(prefix='water-load-probe-') as directory:
        tmp = Path(directory)
        (tmp/'WaterLoadProbe.java').write_text(STUBS + extracted + CHECKS)
        compile_result = subprocess.run(['java','com.sun.tools.javac.Main',str(tmp/'WaterLoadProbe.java')],capture_output=True,text=True)
        if compile_result.returncode:
            raise RuntimeError(compile_result.stderr)
        result = subprocess.run(['java','-cp',str(tmp),'WaterLoadProbe',str(fixed).lower()],capture_output=True,text=True)
        if result.returncode:
            raise RuntimeError(result.stderr)
        return {'source_sha256':hashlib.sha256(source.encode()).hexdigest(),
                'extracted_sha256':hashlib.sha256(extracted.encode()).hexdigest(),
                'compile_exit':compile_result.returncode,'run_exit':result.returncode,'record':result.stdout.strip()}


if __name__ == '__main__':
    records = {'scope':'source-extracted Java17 lifecycle test using world doubles; not Minecraft',
               'audited_at_utc':datetime.now(timezone.utc).isoformat(),
               'production_source_changed':False,
               'full_build_native_visual_performance_run_this_pass':False,
               'before':run(ROOT/'staged-WaterRuntime.java',False),
               'candidate':run(ROOT/'candidate-WaterRuntime.java',True)}
    (ROOT/'lifecycle-results.json').write_text(json.dumps(records,indent=2)+'\n')
    print(json.dumps(records,indent=2))
