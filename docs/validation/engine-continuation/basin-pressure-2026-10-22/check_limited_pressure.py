"""Compile actual new core source and compare all limits with the preserved old pressure body."""
from pathlib import Path
import hashlib,json,subprocess,tempfile
ROOT=Path(__file__).resolve().parent
baseline=(ROOT/'baseline-WaterTransport.java').read_text()
start=baseline.index('  public static Exchange pressure(');brace=baseline.index('{',start);depth=1;end=brace+1
while depth:
    depth+=(baseline[end]=='{')-(baseline[end]=='}');end+=1
old=baseline[start:end].replace('public static Exchange pressure(', 'private static Exchange oldPressure(',1)
code='''import com.shnewbs.deepprimal.core.WaterTransport;
import com.shnewbs.deepprimal.core.WaterTransport.Exchange;
public class LimitedPressureAudit {
  private static final int CAPACITY=16;
'''+old+'''
  public static void main(String[] args) {
    int[] heights={Integer.MIN_VALUE,-2048,-513,-2,-1,0,1,2,513,2047,Integer.MAX_VALUE};
    int[] limits={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,Integer.MAX_VALUE};
    int legacy=0,capped=0;
    for(int fy:heights) for(int ty:heights) for(int from=0;from<=16;from++) for(int to=0;to<=16;to++) {
      var expected=oldPressure(fy,from,ty,to);
      if(!WaterTransport.pressure(fy,from,ty,to).equals(expected)) throw new AssertionError("Legacy behavior changed");
      legacy++;
      for(int cap:limits) {
        int moved=Math.min(cap,expected.moved());
        var actual=WaterTransport.pressure(fy,from,ty,to,cap);
        if(!actual.equals(new Exchange(from-moved,to+moved,moved))) throw new AssertionError("Capped head/capacity/source mismatch");
        capped++;
      }
    }
    boolean rejected=false;
    try {WaterTransport.pressure(0,1,0,0,-1);} catch(IllegalArgumentException correct){rejected=true;}
    if(!rejected)throw new AssertionError("Negative limit accepted");
    System.out.println("LIMITED_PRESSURE_DIFFERENTIAL_PASS legacy="+legacy+" capped="+capped
        +" exactOldBody=true actualNewCore=true extremeHeights=true negativeLimit=true");
  }
}
'''
java=ROOT/'toolchain/jdk-25.0.4.1+1/bin/java'
source=ROOT/'project/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java'
core=ROOT/'project/engine-core/build/classes/java/main'
with tempfile.TemporaryDirectory(prefix='limited-pressure-') as directory:
    p=Path(directory);test=p/'LimitedPressureAudit.java';test.write_text(code)
    compile=subprocess.run([str(java),'com.sun.tools.javac.Main','-cp',str(core),'-d',str(p),str(source),str(test)],text=True,capture_output=True)
    assert compile.returncode==0,compile.stderr
    result=subprocess.run([str(java),'-cp',str(p)+':'+str(core),'LimitedPressureAudit'],text=True,capture_output=True)
    assert result.returncode==0,result.stderr
record={'scope':'actual core source against exact preserved old body; not native world acceptance',
    'baseline_core_sha256':hashlib.sha256(baseline.encode()).hexdigest(),
    'candidate_core_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
    'record':result.stdout.strip(),'compile_exit':compile.returncode,'run_exit':result.returncode}
(ROOT/'limited-pressure-differential.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record))
