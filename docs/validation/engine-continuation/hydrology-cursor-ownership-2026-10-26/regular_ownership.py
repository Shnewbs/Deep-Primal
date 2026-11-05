"""Run actual regular tick loop with deterministic callback and clock doubles."""
from pathlib import Path
import hashlib,json,subprocess
R=Path(__file__).resolve().parent
J=Path('/workspace/scratch/a22609fabda0/toolchain/jdk-25.0.4.1+1/bin')
def block(s,m):
 a=s.index(m);i=s.index('{',a)+1;depth=1
 while depth:
  depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[a:i]
fixture=r'''
 static final class RegionWork {}
 static final class LevelChunk {}
 static final class ChunkStatus {static final Object FULL=new Object();}
 static final class ChunkPos {final int x,z;ChunkPos(int x,int z){this.x=x;this.z=z;}int x(){return x;}int z(){return z;}int getMinBlockX(){return x*16;}int getMinBlockZ(){return z*16;}}
 static final class Clock {static long nanoTime(){return 0;}}
 static final class Level {
  Work w;Cursor old,fresh;int action;boolean full;int calls,visits;long key;
  Level getChunkSource(){return this;}long getGameTime(){return 100;}
  Object getChunk(int x,int z,Object status,boolean load){
   if(load)throw new AssertionError("forced chunk load");
   if(calls++==0){
    if(action==1||action==2){forgetChunk(w,key);if(action==2){w.chunks.put(key,fresh);w.survey.add(key);refreshSource(w,key,fresh);}}
    if(action==3){w.chunks.put(key,fresh);w.survey.add(key);refreshSource(w,key,fresh);}
    if(action==4)forgetChunk(w,key+1);
   }
   return full?new LevelChunk():null;
  }
 }
 static void visit(Level l,Work w,Cursor c,int col,Object t,int x,int z){
  if(w.chunks.get(l.key)!=c||c!=l.old)throw new AssertionError("obsolete cursor visited");l.visits++;
 }
 static int cases;
 static void one(int n,int action,boolean full,int repeats){
  Work work=new Work();Level l=new Level();l.w=work;l.action=action;l.full=full;l.key=91+(repeats-10)*65536L;
  for(long k=l.key;k<l.key+n;k++){Cursor c=new Cursor();c.column=repeats;c.streamSurface[0]=3;c.candidate(0,true);work.chunks.put(k,c);work.survey.add(k);refreshSource(work,k,c);}
  l.old=work.chunks.get(l.key);l.fresh=new Cursor();l.fresh.streamSurface[0]=7;l.fresh.candidate(0,true);
  Object t=null;int limit=1;long deadline=1;
  LOOP
  boolean changed=action>=1&&action<=3;
  if(changed){
   if(l.visits!=0)throw new AssertionError("obsolete cursor visited");
   if(action==1){if(work.chunks.containsKey(l.key)||work.survey.contains(l.key)||work.streamChunks.contains(l.key))throw new AssertionError("retired cursor resurrected");}
   else if(work.chunks.get(l.key)!=l.fresh||!work.survey.contains(l.key)||!work.streamChunks.contains(l.key)||l.fresh.column!=0)throw new AssertionError("replacement cursor damaged");
  }else if(full){
   if(l.visits!=1||work.chunks.get(l.key)!=l.old)throw new AssertionError("ordinary visit lost");
   if(work.chunks.lastEntry().getValue()!=l.old)throw new AssertionError("round robin lost");
  }else if(work.chunks.containsKey(l.key)||work.survey.contains(l.key)||work.streamChunks.contains(l.key))throw new AssertionError("missing FULL retained");
  cases++;
 }
 public static void main(String[] a){for(int n:new int[]{1,2,17,64,1024})for(int action=0;action<5;action++)for(boolean full:new boolean[]{true,false})for(int r=0;r<20;r++)one(n,action,full,r);System.out.println("REGULAR_CALLBACK_PASS cases="+cases);}
'''
results=[]
for name,path in [('baseline',R/'baseline-HydrologyRuntime.java'),('candidate',R/'stage/HydrologyRuntime.java')]:
 s=path.read_text();dest=R/('regular-'+name);dest.mkdir(exist_ok=True)
 pieces=[block(s,m) for m in ['private static final class Cursor','private static final class Work','private static void refreshSource','private static void forgetChunk']]
 loop=block(s,'for (int i = 0; i < limit && !work.chunks.isEmpty()').replace('System.nanoTime()','Clock.nanoTime()').replace('net.minecraft.world.level.ChunkPos','ChunkPos')
 f=dest/'RegularCallbackChecks.java';f.write_text('package com.shnewbs.deepprimal.engine;\nimport java.util.*;\npublic class RegularCallbackChecks {\n'+'\n'.join(pieces)+fixture.replace('  LOOP',loop)+'\n}')
 c=subprocess.run([str(J/'javac'),'-J-XX:-UsePerfData','-d',str(dest),str(R/'stage/StreamSourceIndex.java'),str(f)],capture_output=True,text=True)
 (dest/'compile.txt').write_text(c.stdout+c.stderr)
 if c.returncode:raise RuntimeError(c.stderr)
 p=subprocess.run([str(J/'java'),'-XX:-UsePerfData','-Xmx128m','-Xverify:all','-cp',str(dest),'com.shnewbs.deepprimal.engine.RegularCallbackChecks'],capture_output=True,text=True)
 (dest/'run.txt').write_text(p.stdout+p.stderr)
 results.append({'variant':name,'exit':p.returncode,'output':p.stdout+p.stderr,'source_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'fixture_sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
 if name=='baseline' and (p.returncode==0 or not any(x in p.stderr for x in ['retired cursor resurrected','obsolete cursor visited','replacement cursor damaged'])):raise RuntimeError('baseline witness missing')
 if name=='candidate' and (p.returncode or 'PASS cases=1000' not in p.stdout):raise RuntimeError(p.stdout+p.stderr)
(R/'regular-results.json').write_text(json.dumps(results,indent=2)+'\n');print(json.dumps(results,indent=2))
