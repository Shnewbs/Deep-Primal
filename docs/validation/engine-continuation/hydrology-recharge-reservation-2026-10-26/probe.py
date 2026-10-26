from pathlib import Path
import ast,hashlib,json,subprocess,argparse
parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,required=True);parser.add_argument('--source-pin',required=True);parser.add_argument('--java-home',type=Path,required=True);parser.add_argument('--fixture',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);args=parser.parse_args();o=args.output;o.mkdir(parents=True,exist_ok=True)
raw=args.source.read_text()
a=raw.index('  @SubscribeEvent public void tick(LevelTickEvent.Post e) {');b=raw.index('  @SubscribeEvent public void unloaded(',a);tick=raw[a:b]
new=tick.replace('    // Planned waterways get their own bounded cadence;', '    // Preserve admission time for regional recharge/accounting after the source pass.\n    long streamDeadline = limit > 0 ? deadline - 500_000 : deadline;\n    // Planned waterways get their own bounded cadence;')
start=new.index('    for (int i = 0, n = order.size();');end=new.index('    for (int i = 0; i < limit',start)
new=new[:start]+new[start:end].replace('System.nanoTime() - deadline < 0','System.nanoTime() - streamDeadline < 0')+new[end:]
assert new.count('System.nanoTime() - streamDeadline < 0')==2
(o/'staged-HydrologyRuntime.java').write_text(raw[:a]+new+raw[b:])
parsed=ast.parse(args.fixture.read_text());prefix=next(ast.literal_eval(n.value) for n in parsed.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='prefix' for t in n.targets))
prefix=prefix.replace('TreeMap<Long,Cursor>','LinkedHashMap<Long,Cursor>').replace('new TreeMap<>()','new LinkedHashMap<>()').replace('long getGameTime() { return 0; }','long tick = 20; long getGameTime() { return tick; }').replace('static class Columns { int get() { return 0; } }','static class Columns { int get() { return breadth; } }').replace('    throw new AssertionError("unexpected regular scan in zero-limit fixture");','    regularCalls++; System.now += regularCost;')
prefix += '  static int breadth, regularCalls; static long regularCost=100_000;\n'
suffix='''
 static void check(boolean v,String reason){if(!v)throw new AssertionError(reason);}
 static Work setup(ServerLevel l,int count){WORK.clear();calls.clear();regularCalls=0;late=0;Work w=new Work();Cursor c=new Cursor(0,count,10);w.chunks.put(0L,c);if(count>0)w.streamChunks.put(0L,c);WORK.put(l,w);return w;}
 public static void main(String[] args){boolean fixed=Boolean.parseBoolean(args[0]);int cases=0;
 for(long origin:new long[]{0,-1,Long.MIN_VALUE,Long.MAX_VALUE,Long.MAX_VALUE-1_000_000})
 for(long survey:new long[]{0,500_000,1_000_000,1_499_999,1_500_000,1_999_999,2_000_000})
 for(long perCall:new long[]{0,1,100_000,500_000,2_000_000,3_000_000})
 for(int count:new int[]{0,1,20,256})
 for(int limit:new int[]{0,1,8,64}){
  System.origin=origin;System.now=origin;surveyCost=survey;cost=perCall;breadth=limit;
  ServerLevel l=new ServerLevel();setup(l,count);new AdmissionProbe().tick(new LevelTickEvent.Post(l));
  long streamEnd=fixed && limit>0?1_500_000:2_000_000;
  int streams=survey>=streamEnd?0:perCall==0?count:(int)Math.min(count,(streamEnd-survey+perCall-1)/perCall);
  long remaining=2_000_000-survey-streams*perCall;
  int regular=remaining<=0?0:(int)Math.min(limit,(remaining+regularCost-1)/regularCost);
  check(calls.size()==streams,"stream admission expected="+streams+" actual="+calls.size());
  check(regularCalls==regular,"regular expected="+regular+" actual="+regularCalls);check(late==0,"late source");cases++;
 }
 System.origin=0;cost=100_000;surveyCost=0;breadth=64;ServerLevel l=new ServerLevel();setup(l,256);
 int total=0;for(int i=0;i<100;i++){System.now=i*50_000_000L;System.origin=System.now;l.tick=20L*(i+1);regularCalls=0;new AdmissionProbe().tick(new LevelTickEvent.Post(l));total+=regularCalls;}
 check(total==(fixed?500:0),"recharge starvation reproduction="+total);
 java.lang.System.out.println("REGULAR_ADMISSION_PASS fixed="+fixed+" matrix="+cases+" simulatedTicks=100 regularVisits="+total);
 }
}
'''
result={}
for label,body in [('baseline',tick),('candidate',new)]:
 d=o/label;pkg=d/'net/minecraft/world/level';pkg.mkdir(parents=True,exist_ok=True)
 (pkg/'ChunkPos.java').write_text('package net.minecraft.world.level; public record ChunkPos(int x,int z){public int getMinBlockX(){return x*16;}public int getMinBlockZ(){return z*16;}}')
 (d/'AdmissionProbe.java').write_text(prefix+body+suffix)
 j=(args.java_home/'bin').resolve()
 subprocess.run([str(j/'javac'),'-d',str(d),str(pkg/'ChunkPos.java'),str(d/'AdmissionProbe.java')],check=True)
 p=subprocess.run([str(j/'java'),'-Xmx256m','-cp',str(d),'AdmissionProbe',str(label=='candidate').lower()],capture_output=True,text=True)
 assert p.returncode==0,p.stdout+p.stderr;result[label]=p.stdout.strip();print(p.stdout,end='')
(o/'receipt.json').write_text(json.dumps({'pass':True,'base_source_sha':args.source_pin,'baseline_sha256':hashlib.sha256(raw.encode()).hexdigest(),'candidate_sha256':hashlib.sha256((raw[:a]+new+raw[b:]).encode()).hexdigest(),'checks':result,'scope':'Exact tick extraction with deterministic platform doubles; not Minecraft execution or a latency benchmark','promoted':False,'remaining_gates':['headwater quota and weather cadence actual native tests','full Java25 build/module/package qualification','long-travel loaded/unloaded single-owner and multiplayer soak','per-column/page fairness is not repaired; one callback can consume all reserved time']},indent=2))
