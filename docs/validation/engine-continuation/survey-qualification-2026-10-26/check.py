#!/usr/bin/env python3
"""Compile the exact survey method with deterministic platform boundaries."""
import argparse, hashlib, json, subprocess
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--source',type=Path,required=True)
p.add_argument('--candidate',type=Path,required=True)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--output',type=Path,required=True)
a=p.parse_args(); a.output.mkdir(parents=True,exist_ok=True)
prefix='''import java.util.*;
public class SurveyProbe {
 static class System { static long now; static long nanoTime(){return now;} }
 static long cost; static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static class Cursor {int surveyed,streamCount;boolean[] streamCandidate=new boolean[256];
  void candidate(int col,boolean value){check(!streamCandidate[col]);streamCandidate[col]=value;streamCount++;}}
 static class Work {Deque<Long> survey=new ArrayDeque<>();Map<Long,Cursor> chunks=new HashMap<>(),streamChunks=new HashMap<>();}
 record BlockPos(int x,int y,int z){BlockPos below(){return new BlockPos(x,y-1,z);}}
 static class Heightmap {enum Types {WORLD_SURFACE}}
 enum ChunkStatus {FULL}
 static class LevelChunk {}
 static class ChunkSource {boolean loaded=true;Object getChunk(int x,int z,ChunkStatus s,boolean force){check(!force);return loaded?new LevelChunk():null;}}
 static class BlockState {boolean canBeReplaced(){return false;}boolean is(Object b){return true;}}
 static class ServerLevel {ChunkSource chunks=new ChunkSource();List<Integer> columns=new ArrayList<>();
  ChunkSource getChunkSource(){return chunks;}
  BlockPos getHeightmapPos(Heightmap.Types type,BlockPos p){System.now+=cost;columns.add(p.x()+16*p.z());return p;}
  BlockState getBlockState(BlockPos p){return new BlockState();}}
 static class TerrainEngine {static int SEA=63;boolean wet=true,ocean=false;
  record Sample(boolean wet,double water){} Sample column(int x,int z){return new Sample(wet,63);}
  boolean ocean(int x,int z){return ocean;}}
 static class EngineRegistry {static class Holder{Object get(){return null;}}static Holder WATER_BLOCK=new Holder();}
 static boolean boundary(BlockState s){return false;}
 static void plan(Cursor c,int col,double water){}
'''
suffix='''
 static Work work(){Work w=new Work();w.chunks.put(0L,new Cursor());w.survey.add(0L);return w;}
 public static void main(String[] args){boolean fixed=Boolean.parseBoolean(args[0]);int cases=0;
  for(long origin:(fixed?new long[]{0,-1,Long.MAX_VALUE,Long.MAX_VALUE-500000,Long.MIN_VALUE}:new long[]{0}))
  for(long perColumn:new long[]{0,1,100000,999999,1000000,2000000})
  for(int already:new int[]{0,1,128,255,256}){
   Work w=work();Cursor c=w.chunks.get(0L);c.surveyed=already;
   ServerLevel l=new ServerLevel();TerrainEngine t=new TerrainEngine();cost=perColumn;System.now=origin;
   survey(l,w,t,origin+1000000);
   int expected=perColumn==0?256-already:(int)Math.min(256-already,(1000000+perColumn-1)/perColumn);
   check(c.surveyed==already+expected);check(l.columns.size()==expected);
   check(w.survey.size()==(c.surveyed<256?1:0));
   for(int i=0;i<expected;i++)check(l.columns.get(i)==((already+i)*73&255));
   // Resume with cheap work. Every remaining column occurs once, with no restart.
   cost=0;System.now=origin;survey(l,w,t,origin+1000000);
   check(c.surveyed==256);check(w.survey.isEmpty());check(l.columns.size()==256-already);
   check(new HashSet<>(l.columns).size()==l.columns.size());
   if(already<256)check(w.streamChunks.get(0L)==c);cases++;
  }
  Work w=work();ServerLevel l=new ServerLevel();TerrainEngine t=new TerrainEngine();cost=0;System.now=0;
  l.chunks.loaded=false;survey(l,w,t,1000000);check(w.survey.size()==1);check(w.chunks.get(0L).surveyed==0);check(l.columns.isEmpty());
  l.chunks.loaded=true;survey(l,w,t,1000000);check(w.survey.isEmpty());check(l.columns.size()==256);cases++;
  w=work();w.chunks.clear();survey(l,w,t,1000000);check(w.survey.isEmpty());cases++;
  for(boolean wet:new boolean[]{true,false})for(boolean ocean:new boolean[]{true,false}){
   w=work();l=new ServerLevel();t=new TerrainEngine();t.wet=wet;t.ocean=ocean;System.now=0;
   survey(l,w,t,1000000);check(w.chunks.get(0L).streamCount==(wet&&!ocean?256:0));cases++;
  }
  w=work();l=new ServerLevel();t=new TerrainEngine();System.now=1000000;survey(l,w,t,1000000);
  check(l.columns.isEmpty());check(w.survey.size()==1);cases++;
  w=work();l=new ServerLevel();System.now=Long.MAX_VALUE-500000;cost=100000;
  survey(l,w,t,System.now+1000000);check(l.columns.size()==(fixed?10:0));cases++;
  java.lang.System.out.println("SURVEY_PASS fixed="+fixed+" cases="+cases+" checks="+checks);
 }
}
'''
receipts={}
for mode,path in [('baseline',a.source),('candidate',a.candidate)]:
 raw=path.read_bytes();s=raw.decode();start=s.index('  private static void survey(');end=s.index('  private static void replenishStream(',start);method=s[start:end]
 out=a.output/mode;out.mkdir(exist_ok=True)
 (out/'SurveyProbe.java').write_text(prefix+method+suffix)
 pkg=out/'net/minecraft/world/level';pkg.mkdir(parents=True,exist_ok=True)
 (pkg/'ChunkPos.java').write_text('package net.minecraft.world.level; public record ChunkPos(int x,int z){public int getMinBlockX(){return x*16;}public int getMinBlockZ(){return z*16;}}')
 subprocess.run([str(a.java_home/'bin/javac'),'-d',str(out),str(out/'SurveyProbe.java'),str(pkg/'ChunkPos.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-XX:-UsePerfData','-Xverify:all','-cp',str(out),'SurveyProbe',str(mode=='candidate').lower()],capture_output=True,text=True)
 if r.returncode:raise RuntimeError(r.stdout+r.stderr)
 print(r.stdout,end='');receipts[mode]={'source_sha256':hashlib.sha256(raw).hexdigest(),'method_sha256':hashlib.sha256(method.encode()).hexdigest(),'result':r.stdout.strip()}
(a.output/'receipt.json').write_text(json.dumps({'pass':True,'checks':receipts,'native':False,'performance_claim':False},indent=2)+'\n')
