"""Compile exact admission/lifecycle bodies against explicit world/collection doubles.

These are source-extracted ownership checks, not native Minecraft or FPS evidence.
"""
from pathlib import Path
import hashlib,json,subprocess,tempfile
ROOT=Path(__file__).resolve().parent
def block(source,signature):
    start=source.index(signature);brace=source.index('{',start);end=brace+1;depth=1
    while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
    return source[start:end]
signatures=['  public static int units(ServerLevel l, BlockPos p) {','  private static final class PressureJob {',
 '  private static void watch(Budget budget, PressureJob job, BlockPos p) {',
 '  private static void finish(Budget budget, PressureJob job) {',
 '  private static void runPressure(ServerLevel level, Budget budget) {',
 '  private static PressureJob startPressure(ServerLevel level, BlockPos origin, Budget budget) {']
prefix='''import java.util.*;
public final class AdmissionOwnership {
  private static final int PRESSURE_SEARCH=128,MAX_PRESSURE_JOBS=16;
  static final class LongOpenHashSet extends HashSet<Long> {
    LongOpenHashSet(){} LongOpenHashSet(int capacity){super(capacity);}
  }
  static final class LongQueue extends LinkedHashSet<Long> {
    long removeFirstLong(){long key=iterator().next();remove(key);return key;}
  }
  record BlockPos(int x,int y,int z) {
    int getX(){return x;} int getY(){return y;} int getZ(){return z;}
    BlockPos immutable(){return this;}
    long asLong(){return ((long)x&0x3ffffffL)<<38|((long)z&0x3ffffffL)<<12|((long)y&0xfffL);}
    static BlockPos of(long key){return new BlockPos((int)(key>>38),(int)(key<<52>>52),(int)(key<<26>>38));}
  }
  static final class ServerLevel {
    final Set<BlockPos> wet=new HashSet<>(); boolean loaded=true; int reads,stateReads,minY=-2048,maxY=2047;
    boolean isInWorldBounds(BlockPos p){reads++;return p.y()>=minY && p.y()<=maxY;}
    boolean hasChunkAt(BlockPos p){reads++;return loaded;}
    State getBlockState(BlockPos p){reads++;stateReads++;return new State(wet.contains(p));}
  }
  static final class Budget {
    final LinkedHashMap<BlockPos,PressureJob> searches=new LinkedHashMap<>();
    final HashMap<Long,HashSet<PressureJob>> watchedChunks=new HashMap<>();
    final LongQueue pressure=new LongQueue(); final List<BlockPos> serviced=new ArrayList<>();
    int pressureVisits,pressureLimit;
  }
  static final class EngineRegistry {static final Holder WATER_BLOCK=new Holder();}
  static final class Holder {final Object value=new Object();Object get(){return value;}}
  static final class EngineFluid {static final Object UNITS=new Object();}
  record State(boolean wet) {
    boolean is(Object block){return wet && block==EngineRegistry.WATER_BLOCK.get();}
    int getValue(Object property){return 8;}
  }
  static boolean surface(ServerLevel level,BlockPos origin,int amount){level.reads++;return amount>0;}
  static boolean equalize(ServerLevel level,BlockPos origin,Budget budget){budget.serviced.add(origin);return false;}
  static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  static int watchers(Budget budget){
    Set<PressureJob> all=Collections.newSetFromMap(new IdentityHashMap<>());
    for(var set:budget.watchedChunks.values())all.addAll(set);return all.size();
  }
  static void clean(Budget budget){for(var job:new ArrayList<>(budget.searches.values()))finish(budget,job);}
'''
tests='''
  public static void main(String[] args){
    boolean expectFixed=Arrays.asList(args).contains("--expect-fixed");
    boolean centralized=Arrays.asList(args).contains("--centralized");
    ServerLevel level=new ServerLevel(); Budget budget=new Budget();
    BlockPos origin=new BlockPos(-4096,200,-8);level.wet.add(origin);
    PressureJob retained=startPressure(level,origin,budget);
    retained.queue=Arrays.copyOf(retained.queue,256);retained.head=128;retained.tail=129;
    retained.headsDirty=true; retained.highHead=3208; retained.lowHead=3201;
    budget.pressure.add(origin.asLong());
    int beforeReads=level.reads;runPressure(level,budget);
    var current=budget.searches.get(origin);boolean same=current==retained;
    int duplicateReads=level.reads-beforeReads,head=current.head;
    int orphan=watchers(budget)-budget.searches.size();clean(budget);int leftover=watchers(budget);
    System.out.println("PRESSURE_ADMISSION_MODEL_OBSERVED sameJob="+same+" retainedHead="+head
        +" orphanJobs="+orphan+" watchersAfterFinish="+leftover+" duplicateValidationCalls="+duplicateReads);
    if(expectFixed)check(same && head==128 && current.tail==129 && current.headsDirty
        && current.highHead==3208 && current.lowHead==3201 && orphan==0 && leftover==0 && duplicateReads==(centralized?0:3),
        "Duplicate queued admission must retain the exact frontier/heads/flags and all watcher ownership");
    ServerLevel freshLevel=new ServerLevel();Budget freshBudget=new Budget();
    freshLevel.wet.add(origin);freshBudget.pressure.add(origin.asLong());runPressure(freshLevel,freshBudget);
    check(freshBudget.searches.size()==1,"A normal new wet origin is admitted");
    if(expectFixed)check(freshLevel.stateReads==(centralized?1:3),"Fresh admission reads one water amount snapshot after centralization");
    System.out.println("FRESH_PRESSURE_ADMISSION_OBSERVED validationCalls="+freshLevel.reads+" waterStateReads="+freshLevel.stateReads);
    clean(freshBudget);
    int boundaryCases=0;
    for(boolean loaded:new boolean[]{false,true})for(boolean wet:new boolean[]{false,true})for(int y:new int[]{-1,200,257}){
      ServerLevel w=new ServerLevel();w.loaded=loaded;w.minY=0;w.maxY=256;Budget b=new Budget();
      BlockPos p=new BlockPos(-64,y,8);if(wet)w.wet.add(p);b.pressure.add(p.asLong());runPressure(w,b);
      boolean expected=loaded && wet && y>=0 && y<=256;
      check(b.searches.containsKey(p)==expected && b.pressure.isEmpty(),"New admission preserves bounds, availability and dry-state rejection");
      if(!loaded || y<0 || y>256)check(w.stateReads==0,"Unavailable or out-of-bounds admission never reads block state");
      clean(b);check(watchers(b)==0,"Boundary controls release every watcher");boundaryCases++;
    }
    System.out.println("PRESSURE_ADMISSION_BOUNDARY_PASS cases="+boundaryCases+" unavailableStateReads=0");
    // Fifteen resident duplicates followed by one new origin fit the existing 16-attempt lane.
    budget=new Budget();level=new ServerLevel();List<PressureJob> original=new ArrayList<>();
    for(int n=0;n<15;n++){var p=new BlockPos(n*32,200,8);level.wet.add(p);original.add(startPressure(level,p,budget));budget.pressure.add(p.asLong());}
    var fresh=new BlockPos(9000,200,8);level.wet.add(fresh);budget.pressure.add(fresh.asLong());
    budget.pressureLimit=128;beforeReads=level.reads;runPressure(level,budget);
    int unchanged=0;for(var job:original)if(budget.searches.get(job.origin)==job)unchanged++;
    int saturationReads=level.reads-beforeReads;
    check(budget.searches.size()==16 && budget.searches.containsKey(fresh) && budget.pressure.isEmpty(),"New unrelated origin remains admitted within 16 attempts");
    check(budget.serviced.size()==16 && budget.serviced.getLast().equals(fresh),"Existing FIFO service order remains intact");
    if(expectFixed)check(unchanged==15 && watchers(budget)==16 && saturationReads==(centralized?4:53),"Duplicate admission retains ownership; unchanged caller validation still reads the world");
    clean(budget);if(expectFixed)check(watchers(budget)==0,"Saturation cleanup releases every watcher");
    // Dirty and invalidated existing jobs retain their state for the unchanged equalize path.
    budget=new Budget();level=new ServerLevel();level.wet.add(origin);retained=startPressure(level,origin,budget);
    retained.invalidated=true;retained.headsDirty=true;level.loaded=false;
    if(expectFixed)check(startPressure(level,origin,budget)==retained && retained.invalidated && retained.headsDirty,"An unavailable/invalidated witness must not be silently replaced");
    clean(budget);check(watchers(budget)==0,"Explicit invalidation cleanup releases watchers");
    check(startPressure(level,new BlockPos(1,200,8),budget)==null,"New requests never force-load unavailable chunks");
    // Repeated overlap must not grow the watched-job set outside resident ownership.
    budget=new Budget();level=new ServerLevel();level.wet.add(origin);retained=startPressure(level,origin,budget);
    for(int n=0;n<512;n++)startPressure(level,origin,budget);
    int repeatedWatchers=watchers(budget);clean(budget);int repeatedLeft=watchers(budget);
    if(expectFixed)check(repeatedWatchers==1 && repeatedLeft==0,"Repeated requests remain bounded by their one resident owner");
    Random random=new Random(20261022);int operations=10000;
    budget=new Budget();level=new ServerLevel();
    for(int n=0;n<64;n++)level.wet.add(new BlockPos(n*16-512,200,n%4));
    List<BlockPos> positions=new ArrayList<>(level.wet);positions.sort(Comparator.comparingLong(BlockPos::asLong));
    for(int n=0;n<operations;n++){
      BlockPos p=positions.get(random.nextInt(positions.size()));var old=budget.searches.get(p);
      if(old!=null && random.nextInt(4)==0)finish(budget,old);
      else if(old!=null || budget.searches.size()<16){
        var next=startPressure(level,p,budget);
        if(expectFixed && old!=null)check(next==old,"Random duplicate retains owner");
      }
      check(budget.searches.size()<=16,"Caller residency stays bounded");
      if(expectFixed)check(watchers(budget)==budget.searches.size(),"Every watched job is a resident owner");
    }
    clean(budget);if(expectFixed)check(watchers(budget)==0,"Random lifecycle cleanup releases all watchers");
    System.out.println("PRESSURE_ADMISSION_MODEL_PASS fixed="+expectFixed+" randomizedOperations="+operations
        +" fifoAtCapacity=true invalidatedRetained=true repeatedWatchers="+repeatedWatchers+" repeatedLeft="+repeatedLeft);
  }
}
'''
records=[]
with tempfile.TemporaryDirectory(prefix='deepprimal-pressure-admission-') as directory:
    path=Path(directory);chunk=path/'net/minecraft/world/level/ChunkPos.java';chunk.parent.mkdir(parents=True)
    chunk.write_text('package net.minecraft.world.level; public final class ChunkPos { public static long pack(int x,int z){return ((long)z<<32)|((long)x&0xffffffffL);} }')
    java=ROOT/'toolchain/jdk-25.0.4.1+1/bin/java'
    for mode,name in [('baseline','baseline-WaterRuntime.java'),('accepted-baseline','accepted-baseline-WaterRuntime.java'),('accepted-cleanup','accepted-cleanup-WaterRuntime.java'),('staged-cleanup','staged-cleanup-WaterRuntime.java')]:
        source=(ROOT/name).read_text();fragments=[block(source,s) for s in signatures]
        code=prefix+'\n'.join(fragments)+tests;test=path/'AdmissionOwnership.java';test.write_text(code)
        compile=subprocess.run([str(java),'com.sun.tools.javac.Main','-d',str(path),str(chunk),str(test)],text=True,capture_output=True)
        assert compile.returncode==0,compile.stderr
        extra=['--centralized'] if 'cleanup' in mode else []
        observe=subprocess.run([str(java),'-cp',str(path),'AdmissionOwnership',*extra],text=True,capture_output=True)
        fixed=subprocess.run([str(java),'-cp',str(path),'AdmissionOwnership','--expect-fixed',*extra],text=True,capture_output=True)
        assert observe.returncode==0,observe.stderr
        assert fixed.returncode==(1 if 'baseline' in mode else 0),(mode,fixed.stderr)
        records.append({'mode':mode,'runtime_sha256':hashlib.sha256(source.encode()).hexdigest(),
            'exact_extracted_bodies':{sig:hashlib.sha256(text.encode()).hexdigest() for sig,text in zip(signatures,fragments)},
            'observation':observe.stdout,'acceptance_exit':fixed.returncode,'acceptance_stdout':fixed.stdout,'acceptance_stderr':fixed.stderr})
result={'scope':'Exact source units/admission/lifecycle methods and PressureJob; explicit Java world/collection/surface/equalize doubles; not native, transport or FPS acceptance','records':records}
(ROOT/'source-ownership-checks.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
