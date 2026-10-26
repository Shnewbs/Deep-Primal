"""Compile/exercise the actual regular-visit loop with deterministic native/clock doubles.
Not a Minecraft/native/package test. Baseline must reproduce stale index retention.
"""
from pathlib import Path
import subprocess, json, hashlib, argparse
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--baseline',type=Path,required=True,help='Previous staged HydrologyRuntime.java')
parser.add_argument('--stage',type=Path,required=True,help='Directory containing new staged Java files')
parser.add_argument('--jdk',type=Path,required=True,help='JDK directory, containing bin/')
parser.add_argument('--output',type=Path,required=True,help='Private output directory')
args=parser.parse_args();ROOT=args.output.resolve();ROOT.mkdir(parents=True,exist_ok=True)
STAGE=args.stage.resolve();BASELINE=args.baseline.resolve();JDK=args.jdk.resolve()/'bin'
def block(source, marker):
 start=source.index(marker); opening=source.index('{',start); depth=1; i=opening+1
 while depth:
  depth+=(source[i]=='{')-(source[i]=='}'); i+=1
 return source[start:i]
fixture='''
 static final class RegionWork {}
 static final class LevelChunk {}
 static final class ChunkStatus {static final Object FULL = new Object();}
 static final class ChunkPos {
  final int x,z; ChunkPos(int x,int z){this.x=x;this.z=z;}
  int x(){return x;} int z(){return z;} int getMinBlockX(){return x*16;} int getMinBlockZ(){return z*16;}
 }
 static final class Level {
  final Set<Long> loaded; Level(Set<Long> loaded){this.loaded=loaded;}
  Level getChunkSource(){return this;} long getGameTime(){return 100L;}
  Object getChunk(int x,int z,Object status,boolean load){
   if(load)throw new AssertionError("forced load");
   return loaded.contains((long)x|((long)z<<32))?new LevelChunk():null;
  }
 }
 static final class Clock { static int remaining;static long nanoTime(){return --remaining>=0?0:2;} }
 static void visit(Level l,Work w,Cursor c,int col,Object t,int x,int z){}
 static int tests;
 static void one(int n,int quota,boolean mixed,boolean expire){
  Work w=new Work(); Set<Long> available=new HashSet<>(); Set<Long> removed=new HashSet<>();
  for(long k=0;k<n;k++){
   Cursor c=new Cursor();c.streamSurface[0]=k%4;c.candidate(0,true);
   w.chunks.put(k,c);w.streamChunks.put(k,c,c.headwater());w.survey.add(k);
   w.regions.put(k,new RegionWork());if(mixed && k%3==0)available.add(k);
  }
  Level l=new Level(available);Object t=null;int limit=quota;long deadline=1;
  Clock.remaining=expire?0:100000;
  LOOP
  int processed=expire?0:Math.min(n,quota);
  for(long k=0;k<processed;k++)if(!available.contains(k))removed.add(k);
  if(w.chunks.size()!=n-removed.size())throw new AssertionError("chunk cursor count");
  for(long k:removed)if(w.streamChunks.contains(k)||w.survey.contains(k))
   throw new AssertionError("stale index/survey after missing FULL chunk key="+k);
  if(w.streamChunks.size()!=w.chunks.size())throw new AssertionError("source residency");
  if(removed.size()>0 && w.regions.size()>w.chunks.size())throw new AssertionError("regional retention");
  for(long k:available)if(!w.chunks.containsKey(k)||!w.streamChunks.contains(k))throw new AssertionError("live entry lost");
  tests++;
 }
 public static void main(String[] args){
  for(int n:new int[]{1,2,17,64,128,8192})for(int q:new int[]{0,1,2,16,64,128})
   for(boolean mixed:new boolean[]{false,true})for(boolean expired:new boolean[]{false,true})one(n,q,mixed,expired);
  System.out.println("SOURCE_INVALIDATION_LOOP_PASS cases="+tests+" actualLoop=true missingFull=true noForcedLoad=true expiredAdmission=true");
 }
'''
results=[]
for variant,path in [('baseline',BASELINE),('candidate',STAGE/'HydrologyRuntime.java')]:
 source=path.read_text(); dest=ROOT/variant;dest.mkdir(exist_ok=True)
 pieces=[block(source,'private static final class Cursor'),block(source,'private static final class Work'),block(source,'private static void refreshSource')]
 if variant=='candidate':pieces.append(block(source,'private static void forgetChunk'))
 loop=block(source,'for (int i = 0; i < limit && !work.chunks.isEmpty()')
 loop=loop.replace('work.', 'w.').replace('forgetChunk(work,','forgetChunk(w,').replace('refreshSource(work,','refreshSource(w,').replace('visit(l, work,','visit(l, w,')
 loop=loop.replace('System.nanoTime()', 'Clock.nanoTime()').replace('net.minecraft.world.level.ChunkPos','ChunkPos')
 program='package com.shnewbs.deepprimal.engine;\nimport java.util.*;\npublic class SourceInvalidationLoopChecks {\n'+ '\n'.join(pieces)+fixture.replace('  LOOP\n',loop+'\n')+'\n}'
 f=dest/'SourceInvalidationLoopChecks.java'; f.write_text(program)
 compile_cmd=[str(JDK/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-proc:none','-d',str(dest),str(STAGE/'StreamSourceIndex.java'),str(f)]
 r=subprocess.run(compile_cmd,text=True,capture_output=True)
 (dest/'compile.txt').write_text(r.stdout+r.stderr)
 if r.returncode:raise RuntimeError(r.stdout+r.stderr)
 r=subprocess.run([str(JDK/'java'),'-Xmx96m','-XX:-UsePerfData','-Xverify:all','-cp',str(dest),'com.shnewbs.deepprimal.engine.SourceInvalidationLoopChecks'],capture_output=True,text=True)
 (dest/'run.txt').write_text(r.stdout+r.stderr)
 results.append(dict(variant=variant,exit=r.returncode,output=r.stdout+r.stderr,source_sha256=hashlib.sha256(path.read_bytes()).hexdigest(),extracted_fixture_sha256=hashlib.sha256(f.read_bytes()).hexdigest()))
 if variant=='baseline' and (r.returncode==0 or 'stale index/survey' not in r.stderr):raise RuntimeError('baseline did not reproduce expected defect')
 if variant=='candidate' and (r.returncode!=0 or 'PASS cases=144' not in r.stdout):raise RuntimeError(r.stdout+r.stderr)
(ROOT/'loop-results.json').write_text(json.dumps(results,indent=2)+'\n')
print(json.dumps(results,indent=2))
