"""Actual survey method, controlled native callbacks; not a live Minecraft test."""
from pathlib import Path
import subprocess,json,hashlib
R=Path(__file__).resolve().parent;J=R.parent/'toolchain/jdk-25.0.4.1+1/bin'
def block(s,m):
 a=s.index(m);i=s.index('{',a)+1;d=1
 while d:d+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[a:i]
fixture=r'''
 static class RegionWork {}
 static class LevelChunk {}
 static class ChunkStatus {static Object FULL=new Object();}
 static class ChunkPos {final int x,z;ChunkPos(int x,int z){this.x=x;this.z=z;}int x(){return x;}int z(){return z;}int getMinBlockX(){return x*16;}int getMinBlockZ(){return z*16;}}
 static class BlockPos {BlockPos(int x,int y,int z){}BlockPos below(){return this;}}
 static class Heightmap {static class Types {static Object WORLD_SURFACE=new Object();}}
 static class EngineRegistry {static class WATER_BLOCK {static Object get(){return null;}}}
 static class State {boolean is(Object o){return true;}boolean canBeReplaced(){return false;}}
 static boolean boundary(State s){return false;}
 static class Clock {static int left;static long nanoTime(){return left-->0?0:2;}}
 static class TerrainEngine {static int SEA=96;ServerLevel l;boolean ocean(int x,int z){return false;}Sample column(int x,int z){l.callback(3);return new Sample();}}
 static class Sample {boolean wet(){return true;}double water(){return 100;}}
 static class ServerLevel {
  Work w;Cursor old,fresh;long key=1;int phase,action,calls;boolean full;
  ServerLevel getChunkSource(){return this;}
  void callback(int p){if(p!=phase||calls++!=0)return;forgetChunk(w,key);if(action==2){w.chunks.put(key,fresh);w.survey.add(key);refreshSource(w,key,fresh);}}
  Object getChunk(int x,int z,Object status,boolean load){if(load)throw new AssertionError("forced load");callback(0);return full?new LevelChunk():null;}
  BlockPos getHeightmapPos(Object t,BlockPos p){callback(1);return p;}
  State getBlockState(BlockPos p){callback(2);return new State();}
 }
 static int tests;
 static void one(int phase,int action,boolean full,int budget,int initial){
  Work w=new Work();ServerLevel l=new ServerLevel();l.w=w;l.phase=phase;l.action=action;l.full=full;
  l.old=new Cursor();l.old.surveyed=initial;l.fresh=new Cursor();l.fresh.streamSurface[0]=200;l.fresh.candidate(0,true);
  w.chunks.put(1L,l.old);w.survey.add(1L);TerrainEngine t=new TerrainEngine();t.l=l;Clock.left=budget;
  survey(l,w,t,1);
  boolean changed=l.calls>0;
  if(changed&&action==1){if(w.chunks.containsKey(1L)||w.survey.contains(1L)||w.streamChunks.contains(1L))throw new AssertionError("retired survey requeued");}
  if(changed&&action==2){
   if(w.chunks.get(1L)!=l.fresh)throw new AssertionError("replacement lost");
   if(!w.streamChunks.contains(1L))throw new AssertionError("replacement index lost");
   if(l.old.surveyed>initial+1)throw new AssertionError("obsolete survey continued");
  }
  if(!changed&&w.chunks.get(1L)!=l.old)throw new AssertionError("ordinary ownership changed");
  tests++;
 }
 public static void main(String[] args){for(int phase=0;phase<4;phase++)for(int action=1;action<=2;action++)for(boolean full:new boolean[]{false,true})for(int budget:new int[]{0,1,2,4,32,260})for(int initial:new int[]{0,1,127,254,255})one(phase,action,full,budget,initial);System.out.println("SURVEY_CALLBACK_PASS cases="+tests);}
'''
results=[]
for name,path in [('baseline',R/'baseline-HydrologyRuntime.java'),('candidate',R/'stage/HydrologyRuntime.java')]:
 s=path.read_text();d=R/('survey-'+name);d.mkdir(exist_ok=True)
 pieces=[block(s,m) for m in ['private static final class Cursor','private static final class Work','private static void refreshSource','private static void forgetChunk','private static void plan','private static void survey']]
 f=d/'SurveyCallbackChecks.java';f.write_text(('package com.shnewbs.deepprimal.engine;\nimport java.util.*;\npublic class SurveyCallbackChecks {\n'+'\n'.join(pieces)+fixture+'\n}').replace('System.nanoTime()','Clock.nanoTime()').replace('net.minecraft.world.level.ChunkPos','ChunkPos'))
 c=subprocess.run([str(J/'javac'),'-J-XX:-UsePerfData','-d',str(d),str(R/'stage/StreamSourceIndex.java'),str(f)],capture_output=True,text=True);(d/'compile.txt').write_text(c.stdout+c.stderr)
 if c.returncode:raise RuntimeError(c.stderr)
 p=subprocess.run([str(J/'java'),'-XX:-UsePerfData','-Xmx128m','-Xverify:all','-cp',str(d),'com.shnewbs.deepprimal.engine.SurveyCallbackChecks'],capture_output=True,text=True);(d/'run.txt').write_text(p.stdout+p.stderr)
 results.append({'variant':name,'exit':p.returncode,'output':p.stdout+p.stderr,'source_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'fixture_sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
 if name=='baseline' and (p.returncode==0 or 'retired survey requeued' not in p.stderr):raise RuntimeError('baseline reproduction missing')
 if name=='candidate' and (p.returncode or 'PASS cases=480' not in p.stdout):raise RuntimeError(p.stdout+p.stderr)
(R/'survey-results.json').write_text(json.dumps(results,indent=2)+'\n');print(json.dumps(results,indent=2))

