#!/usr/bin/env python3
"""Compile the exact production tick method against deterministic boundary doubles.

This is a scheduling/admission reproduction, not Minecraft execution or a latency
benchmark. No production source or delivery artifact is modified.
"""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

p = argparse.ArgumentParser()
p.add_argument('--source-pin', required=True)
p.add_argument('--source', type=Path, required=True)
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--output', type=Path, required=True)
a = p.parse_args()
a.java_home = a.java_home.resolve()
a.output.mkdir(parents=True, exist_ok=True)
raw = a.source.read_bytes()
source = raw.decode()
begin = source.index('  @SubscribeEvent public void tick(LevelTickEvent.Post e) {')
end = source.index('  @SubscribeEvent public void unloaded(', begin)
method = source[begin:end]
old = 'for (int s = 0; s < c.streamCount; s++) {'
new = 'for (int s = 0; s < c.streamCount && System.nanoTime() < deadline; s++) {'
assert method.count(old) == 1, 'review source drift before applying candidate'
candidate = method.replace(old, new).replace('System.nanoTime() < deadline', 'System.nanoTime() - deadline < 0')
(a.output / 'baseline-tick.java.txt').write_text(method)
(a.output / 'candidate-tick.java.txt').write_text(candidate)

prefix = '''import java.util.*;
public class AdmissionProbe {
  @interface SubscribeEvent {}
  static class System { static long now, origin; static long nanoTime() { return now; } }
  static class LevelTickEvent { record Post(Object getLevel) {} }
  static class EngineChunkGenerator { Object geography() { return null; } }
  static class LevelChunk {}
  enum ChunkStatus { FULL }
  static class ChunkSource {
    Set<Integer> unloaded = new HashSet<>();
    Object getGenerator() { return new EngineChunkGenerator(); }
    Object getChunk(int x,int z,ChunkStatus s,boolean load) {
      if(load) throw new AssertionError("forced chunk loading");
      return unloaded.contains(x) ? null : new LevelChunk();
    }
  }
  static class ServerLevel {
    ChunkSource chunks = new ChunkSource();
    ChunkSource getChunkSource() { return chunks; }
    long getGameTime() { return 0; }
  }
  static class HydrologyConfig {
    static class Columns { int get() { return 0; } }
    static Columns COLUMNS = new Columns();
  }
  static class Cursor {
    int streamCount, column, id; int[] streams = new int[256];
    long[] visited = new long[256]; double head;
    Cursor(int id,int count,double head) {
      this.id=id;streamCount=count;this.head=head;
      for(int i=0;i<count;i++) streams[i]=i;
    }
    double headwater() { return head; }
  }
  static class Work {
    TreeMap<Long,Cursor> chunks = new TreeMap<>();
    TreeMap<Long,Cursor> streamChunks = new TreeMap<>();
  }
  static Map<ServerLevel,Work> WORK = new HashMap<>();
  static List<String> calls = new ArrayList<>();
  static long cost, surveyCost; static int late;
  static boolean enabled(ServerLevel l) { return true; }
  static void survey(ServerLevel l,Work w,Object t,long deadline) { System.now+=surveyCost; }
  static void replenishStream(ServerLevel l,Work w,Object t,Cursor c,int col,int x,int z) {
    if(System.now-System.origin>=2_000_000L) late++;
    calls.add(c.id+":"+col); System.now+=cost;
  }
  static void visit(ServerLevel l,Work w,Cursor c,int col,Object t,int x,int z) {
    throw new AssertionError("unexpected regular scan in zero-limit fixture");
  }
'''
suffix = '''
  static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
  static Work reset(ServerLevel l,long perCall,long survey) {
    System.now=System.origin;cost=perCall;surveyCost=survey;late=0;calls.clear();WORK.clear();
    Work w=new Work();WORK.put(l,w);return w;
  }
  static void page(Work w,int id,int count,double head) {
    Cursor c=new Cursor(id,count,head);w.chunks.put((long)id,c);w.streamChunks.put((long)id,c);
  }
  public static void main(String[] args) {
    boolean fixed=Boolean.parseBoolean(args[0]); int cases=0, lateTotal=0;
    for(long origin:(fixed?new long[]{0,Long.MAX_VALUE-2_000_000,Long.MAX_VALUE-1_000_000,Long.MAX_VALUE,Long.MIN_VALUE,-1}:new long[]{0}))
    for(int count:new int[]{0,1,2,20,64,256})
    for(long cost:new long[]{0,1,100_000,1_000_000,1_999_999,2_000_000,3_000_000})
    for(long survey:new long[]{0,1_000_000,1_999_999,2_000_000,3_000_000}) {
      System.origin=origin;
      ServerLevel l=new ServerLevel(); Work w=reset(l,cost,survey);page(w,0,count,10);
      new AdmissionProbe().tick(new LevelTickEvent.Post(l));
      int expected=survey>=2_000_000?0:count;
      if(fixed && cost>0) expected=(int)Math.min(expected,Math.max(0,(2_000_000-survey+cost-1)/cost));
      check(calls.size()==expected,"count="+count+" cost="+cost+" survey="+survey+" actual="+calls.size()+" expected="+expected);
      for(int j=0;j<calls.size();j++) check(calls.get(j).equals("0:"+j),"column order");
      if(fixed) check(late==0,"admitted stream after deadline");
      lateTotal+=late;cases++;
    }
    System.origin=0;
    ServerLevel l=new ServerLevel();Work w=reset(l,3_000_000,0);page(w,0,256,10);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    int reproduction=calls.size(), reproductionLate=late;
    check(reproduction==(fixed?1:256),"defect reproduction");cases++;
    l=new ServerLevel();w=reset(l,0,0);page(w,0,3,10);page(w,1,2,30);page(w,2,1,20);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    check(calls.equals(List.of("1:0","1:1","2:0","0:0","0:1","0:2")),"headwater ordering changed");cases++;
    l=new ServerLevel();w=reset(l,3_000_000,0);page(w,0,3,10);page(w,1,2,30);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    check(calls.equals(fixed?List.of("1:0"):List.of("1:0","1:1")),"page admission ordering");cases++;
    l=new ServerLevel();w=reset(l,0,0);page(w,0,3,10);page(w,1,2,30);l.chunks.unloaded.add(1);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    check(calls.equals(List.of("0:0","0:1","0:2")),"unloaded page admitted");cases++;
    l=new ServerLevel();w=reset(l,0,0);page(w,0,3,10);w.chunks.remove(0L);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    check(calls.isEmpty()&&w.streamChunks.isEmpty(),"stale page retained");cases++;
    System.origin=Long.MAX_VALUE-1_000_000;
    l=new ServerLevel();w=reset(l,100_000,0);page(w,0,64,10);
    new AdmissionProbe().tick(new LevelTickEvent.Post(l));
    int wrapped=calls.size();check(wrapped==(fixed?20:0),"wrapped deadline admission");cases++;
    java.lang.System.out.println("ADMISSION_PROBE_PASS fixed="+fixed+" cases="+cases+" late_matrix="+lateTotal+" reproduction_calls="+reproduction+" reproduction_late="+reproductionLate+" wrapped_calls="+wrapped);
  }
}
'''
pos = '''package net.minecraft.world.level;
public record ChunkPos(int x,int z) {
  public int getMinBlockX() { return x*16; }
  public int getMinBlockZ() { return z*16; }
}
'''
results = {}
for name, body in [('baseline', method), ('candidate', candidate)]:
    out = a.output / name
    pkg = out / 'net/minecraft/world/level'
    pkg.mkdir(parents=True, exist_ok=True)
    (pkg/'ChunkPos.java').write_text(pos)
    (out/'AdmissionProbe.java').write_text(prefix+body+suffix)
    subprocess.run([str(a.java_home/'bin/javac'), '-d', str(out), str(pkg/'ChunkPos.java'), str(out/'AdmissionProbe.java')], check=True)
    r = subprocess.run([str(a.java_home/'bin/java'), '-cp', str(out), 'AdmissionProbe', str(name=='candidate').lower()], text=True, capture_output=True, check=True)
    results[name] = r.stdout.strip()
    print(r.stdout, end='')
receipt = {'pass': True, 'source_sha256': hashlib.sha256(raw).hexdigest(),
           'source_pin': a.source_pin,
           'method_sha256': hashlib.sha256(method.encode()).hexdigest(),
           'candidate_method_sha256': hashlib.sha256(candidate.encode()).hexdigest(),
           'checks': results, 'native_execution': False, 'latency_benchmark': False,
           'limitations': ['Test doubles replace all Minecraft interactions and time.',
                           'One admitted call may itself exceed the budget.',
                           'Headwater-first scheduling is retained; fairness is not proven.',
                           'Full project and packaged native gates required before promotion.']}
(a.output/'receipt.json').write_text(json.dumps(receipt, indent=2)+'\n')
