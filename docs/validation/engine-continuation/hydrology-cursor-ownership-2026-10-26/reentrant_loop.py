"""Exact staged source-loop extraction with deterministic native callback/clock doubles.
This validates callback sequencing, not a real Minecraft world or water transaction.
"""
from pathlib import Path
import subprocess,json,hashlib
P=Path(__file__).resolve().parent; B=Path('/dev/shm/dp-builder2000/stage'); S=P/'stage'
J=Path('/workspace/scratch/a22609fabda0/toolchain/jdk-25.0.4.1+1/bin')
def block(src,mark):
 a=src.index(mark); b=src.index('{',a); d=1; e=b+1
 while d:
  d+=(src[e]=='{')-(src[e]=='}');e+=1
 return src[a:e]
fixture=r'''
 static final class RegionWork {}
 static final class LevelChunk {}
 static final class ChunkStatus {static final Object FULL=new Object();}
 static final class ChunkPos {
  final int x,z;ChunkPos(int x,int z){this.x=x;this.z=z;}
  int x(){return x;}int z(){return z;}int getMinBlockX(){return x*16;}int getMinBlockZ(){return z*16;}
 }
 static final class Clock {static long nanoTime(){return 0;}}
 static final class Level {
  Work work;int phase,action,n;boolean fired,missing;int deliveries;
  Level getChunkSource(){return this;}
  Object getChunk(int x,int z,Object status,boolean force){
   check(!force,"forced load");if(phase==0)callback();return missing?null:new LevelChunk();
  }
  void callback(){
   if(fired)return;fired=true;
   Cursor c=work.chunks.get(0L),next=work.chunks.get(1L);
   switch(action){
    case 0 -> forgetChunk(work,0L);
    case 1 -> forgetChunk(work,1L);
    case 2 -> {Cursor replacement=cursor(200);work.chunks.put(0L,replacement);work.streamChunks.put(0L,replacement,replacement.headwater());}
    case 3 -> work.streamChunks.deactivate(0L);
    case 4 -> work.streamChunks.put(1L,next,500);
    case 5 -> {Cursor added=cursor(500);work.chunks.put(1000L,added);work.streamChunks.put(1000L,added,500);}
    case 6 -> work.streamChunks.put(0L,c,c.headwater());
    case 7 -> work.streamChunks.remove(-1L);
    case 8 -> work.streamChunks.remove(0L,new Cursor());
    case 9 -> {}
   }
  }
 }
 static Cursor cursor(double h){Cursor c=new Cursor();c.streamSurface[0]=h;c.streamSurface[1]=h-1;c.candidate(0,true);c.candidate(1,true);return c;}
 static void replenishStream(Level l,Work work,Object t,Cursor c,int col,int x,int z){
  l.deliveries++;if(l.phase==1)l.callback();
 }
 static int cases;
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 static void sourcePass(Level l,Work work){Object t=null;long streamDeadline=1;
  SOURCE_LOOP
 }
 static void one(int n,int phase,int action,boolean missing){
  Work w=new Work();for(long k=0;k<n;k++){Cursor c=cursor(200-k);w.chunks.put(k,c);w.streamChunks.put(k,c,c.headwater());w.survey.add(k);w.regions.put(k,new RegionWork());}
  Level l=new Level();l.work=w;l.phase=phase;l.action=action;l.n=n;l.missing=missing;
  sourcePass(l,w);
  int expected=missing?0:action<6?(phase==0?0:1):2*n;
  check(l.deliveries==expected,"obsolete traversal continued after callback: deliveries="+l.deliveries+" expected="+expected+" action="+action+" phase="+phase);
  if(missing)check(!w.chunks.containsKey(0L)&&!w.streamChunks.contains(0L)&&!w.survey.contains(0L),"missing FULL retained source");
  else{
   l.fired=true;l.deliveries=0;int current=w.streamChunks.size();sourcePass(l,w);
   check(l.deliveries==2*current,"next tick did not reopen current sources");
  }
  cases++;
 }
 public static void main(String[] args){
  if(args.length>0){one(1,0,0,false);return;}
  for(int repeat=0;repeat<25;repeat++)for(int n:new int[]{2,3,8,64})for(int phase=0;phase<2;phase++)for(int action=0;action<10;action++)one(n,phase,action,false);
  for(int n:new int[]{1,2,64,8192})one(n,0,9,true);
  System.out.println("SOURCE_CALLBACK_LOOP_PASS cases="+cases+" actualLoop=true nativeDoubles=true nextPassRecovery=true noForcedLoad=true");
 }
'''
results=[]
for label,stage in [('baseline',B),('candidate',S)]:
 src=(stage/'HydrologyRuntime.java').read_text();d=P/('callbacks-'+label);d.mkdir(exist_ok=True)
 pieces=[block(src,m) for m in ['private static final class Cursor','private static final class Work','private static void forgetChunk']]
 a=src.index('    work.streamChunks.beginPass();');b=src.index('    for (int i = 0; i < limit && !work.chunks.isEmpty()',a)
 loop=src[a:b].replace('System.nanoTime()','Clock.nanoTime()').replace('net.minecraft.world.level.ChunkPos','ChunkPos')
 program='package com.shnewbs.deepprimal.engine;\nimport java.util.*;\npublic class SourceCallbackLoopChecks {\n'+'\n'.join(pieces)+fixture.replace('  SOURCE_LOOP\n',loop)+'\n}'
 f=d/'SourceCallbackLoopChecks.java';f.write_text(program)
 r=subprocess.run([str(J/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-d',str(d),str(stage/'StreamSourceIndex.java'),str(f)],capture_output=True,text=True)
 (d/'compile.txt').write_text(r.stdout+r.stderr)
 if r.returncode:raise RuntimeError(r.stderr)
 cmd=[str(J/'java'),'-Xmx128m','-XX:-UsePerfData','-Xverify:all','-cp',str(d),'com.shnewbs.deepprimal.engine.SourceCallbackLoopChecks']+(['baseline'] if label=='baseline' else [])
 r=subprocess.run(cmd,capture_output=True,text=True);(d/'run.txt').write_text(r.stdout+r.stderr)
 if label=='baseline':
  if r.returncode==0 or 'obsolete traversal continued' not in r.stderr:raise RuntimeError('expected baseline failure absent: '+r.stdout+r.stderr)
 elif r.returncode or 'PASS cases=2004' not in r.stdout:raise RuntimeError(r.stdout+r.stderr)
 results.append(dict(variant=label,exit=r.returncode,output=r.stdout+r.stderr,runtime_sha256=hashlib.sha256((stage/'HydrologyRuntime.java').read_bytes()).hexdigest(),extracted_fixture_sha256=hashlib.sha256(f.read_bytes()).hexdigest()))
(P/'callback-results.json').write_text(json.dumps(results,indent=2)+'\n');print(json.dumps(results,indent=2))
