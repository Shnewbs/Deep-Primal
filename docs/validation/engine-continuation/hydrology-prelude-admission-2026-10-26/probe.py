from pathlib import Path
import json,subprocess,hashlib
r=Path(__file__).resolve().parent;j=r.parent/'toolchain/jdk-25.0.4.1+1/bin';results=[]
variants={'baseline':r.parent/'builder1700/project/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java','single_guard':r.parent/'builder1700/next-stage/HydrologyRuntime.java','candidate':r/'stage/HydrologyRuntime.java'}
for variant,path in variants.items():
 s=path.read_text();start=s.index('    work.streamChunks.entrySet().removeIf') if variant=='baseline' else s.index('    if (System.nanoTime() - streamDeadline < 0) {');end=s.index('for (int i = 0, n = order.size();',start);pre=s[start:end];pre+='\nobserved=order.size(); for(int z=1;z<order.size();z++) if(order.get(z-1).getValue().h<order.get(z).getValue().h) throw new AssertionError("order");\n';pre+='}\n'*(0 if variant=='baseline' else 1 if variant=='single_guard' else 2)
 java='''import java.util.*;
public class Probe {
 static long clock, reads, heights;static int observed;
 static class Clock {static long nanoTime(){return clock;}}
 static class Cursor {int streamCount=1;double h;Cursor(double h){this.h=h;}double headwater(){heights++;return h;}}
 static class CMap extends LinkedHashMap<Long,Cursor>{public Cursor get(Object k){reads++;clock++;return super.get(k);}}
 static class Work {CMap chunks=new CMap();LinkedHashMap<Long,Cursor> streamChunks=new LinkedHashMap<>();}
 public static void main(String[] args){int cases=0,violations=0,initialSkips=0,cleanupSkips=0;
 for(long base:new long[]{0,Long.MAX_VALUE-5,Long.MIN_VALUE+5}) for(int n:new int[]{0,1,64,1024,8192}) for(int pattern=0;pattern<4;pattern++) for(int mode=0;mode<3;mode++){
 Work work=new Work();int valid=0;
 for(long k=0;k<n;k++){Cursor c=new Cursor(pattern==3?3:(k*73)%Math.max(1,n));work.streamChunks.put(k,c);if(pattern==1&&k%3==0)c.streamCount=0;else if(pattern!=2||k%3!=0){work.chunks.put(k,c);valid++;}}
 clock=base;long streamDeadline=base+(mode==0?-1:mode==1?Math.max(1,n/2):n+10);reads=heights=0;observed=-1;
 PRELUDE
 boolean initiallyClosed=mode==0,closedAfterCleanup=clock-streamDeadline>=0;
 if((initiallyClosed||closedAfterCleanup)&&observed>=0)violations++;
 if(!initiallyClosed&&!closedAfterCleanup&&observed!=valid)throw new AssertionError("membership");
 if(observed<0){if(initiallyClosed)initialSkips++;else cleanupSkips++;}
 cases++;
 }
 if(CANDIDATE&&violations!=0)throw new AssertionError("expired sort admitted");
 if(!CANDIDATE&&violations==0)throw new AssertionError("baseline did not reproduce");
 System.out.println("{\\"cases\\":"+cases+",\\"expired_sort_admissions\\":"+violations+",\\"initial_skips\\":"+initialSkips+",\\"cleanup_skips\\":"+cleanupSkips+"}");
 }}
'''.replace('PRELUDE',pre.replace('System.nanoTime()','Clock.nanoTime()')).replace('CANDIDATE','true' if variant=='candidate' else 'false')
 d=r/variant;d.mkdir(exist_ok=True);p=d/'Probe.java';p.write_text(java);subprocess.run([str(j/'javac'),'-d',str(d),str(p)],check=True);run=subprocess.run([str(j/'java'),'-cp',str(d),'Probe'],check=True,capture_output=True,text=True);results.append({'variant':variant,'source_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),**json.loads(run.stdout)})
(r/'probe-results.json').write_text(json.dumps(results,indent=2));print(json.dumps(results))
