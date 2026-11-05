"""Count work in the actual hydrology prelude after its admission deadline expires.

This is a functional operation-count reproduction, not a latency benchmark.
"""
from pathlib import Path
import json, subprocess, hashlib, sys
r=Path(__file__).resolve().parent
candidate='--candidate' in sys.argv
src=r/'next-stage/HydrologyRuntime.java' if candidate else r/'project/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java'
text=src.read_text()
start=text.index('    if (System.nanoTime() - streamDeadline < 0) {') if candidate else text.index('    work.streamChunks.entrySet().removeIf')
end=text.index('for (int i = 0, n = order.size();',start)
prelude=text[start:end]
prelude+='\nfor(int k=1;k<order.size();k++) if(order.get(k-1).getValue().h<order.get(k).getValue().h) throw new AssertionError("Headwater order");\n'
if candidate:prelude+='}\n'
if prelude.count('order.sort')!=1 or prelude.count('removeIf')!=1:
    raise RuntimeError('Production prelude changed; re-review extraction')
d=r/('prologue-candidate' if candidate else 'prologue-probe');d.mkdir(exist_ok=True)
java='''import java.util.*;
public class PrologueProbe {
 static long reads, heights;
 static class Clock {static long nanoTime(){return 1;}}
 static class Cursor {int streamCount=1; double h;
  Cursor(double h){this.h=h;} double headwater(){heights++;return h;}}
 static class CountMap extends LinkedHashMap<Long,Cursor> {
  public Cursor get(Object k){reads++;return super.get(k);}}
 static class Work {CountMap chunks=new CountMap();
  LinkedHashMap<Long,Cursor> streamChunks=new LinkedHashMap<>();}
 public static void main(String[] args) {
  for(boolean expired:new boolean[]{false,true}) for(int n:new int[]{0,1,64,1024,8192}){
   Work work=new Work();
   for(long i=0;i<n;i++){Cursor c=new Cursor((i*73)%Math.max(1,n));
    work.chunks.put(i,c);work.streamChunks.put(i,c);}
   reads=heights=0;long streamDeadline=expired?0:2;
PRELUDE
   if(EXPECTATION)
    throw new AssertionError("Reproduction did not exercise expired-deadline prelude");
   System.out.println(n+","+reads+","+heights+","+(expired?1:0));
  }
 }
}
'''.replace('PRELUDE',prelude.replace('System.nanoTime()','Clock.nanoTime()')).replace('EXPECTATION','expired ? (reads!=0 || heights!=0) : (reads!=n || (n>1&&heights==0))' if candidate else 'reads!=n || (n>1&&heights==0)')
(d/'PrologueProbe.java').write_text(java)
j=r.parent/'toolchain/jdk-25.0.4.1+1/bin'
subprocess.run([str(j/'javac'),'-d',str(d),str(d/'PrologueProbe.java')],check=True)
p=subprocess.run([str(j/'java'),'-cp',str(d),'PrologueProbe'],capture_output=True,text=True,check=True)
rows=[dict(zip(['streams','map_reads','headwater_reads','deadline_expired'],map(int,l.split(',')))) for l in p.stdout.splitlines()]
receipt={'source_sha':'a1583a508b0d67130d387bc018defd5758acd496','source_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),
 'prelude_sha256':hashlib.sha256(prelude.encode()).hexdigest(),'reproduced':True,'latency_benchmark':False,
 'candidate':candidate,'cases':rows,'conclusion':('Staged guard skips prelude after deadline. When admitted, scan/sort still unbounded; no full runtime qualification.' if candidate else 'Linear scan and comparison sort execute even with expired admission deadline. No bounded-work claim or production fix in this probe.')}
(r/('prologue-candidate.json' if candidate else 'prologue-probe.json')).write_text(json.dumps(receipt,indent=2)+'\n')
print(json.dumps(receipt))
