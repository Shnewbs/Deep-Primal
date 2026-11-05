"""Execute exact production and candidate methods against an unbounded integer oracle."""
from pathlib import Path
import argparse,json,subprocess,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
r=Path(__file__).resolve().parent;o=a.output;o.mkdir(parents=True,exist_ok=True)
def method(path,name):
    raw=path.read_text();start=raw.index('  static int streamAllowance(');end=raw.index('  @SubscribeEvent public void loaded(',start)
    return raw[start:end].replace('streamAllowance(',name+'(',1)
source='''import java.math.BigInteger;
import java.util.Random;
public final class AllowanceChecks {
'''+method(r/'baseline-HydrologyRuntime.java','baseline')+method(r/'HydrologyRuntime.java','candidate')+'''
  static int oracle(int quota,long start,long now) {
    if(start==Long.MIN_VALUE || now<start)return quota;
    BigInteger phase=BigInteger.valueOf(now).subtract(BigInteger.valueOf(start)).min(BigInteger.valueOf(19));
    return BigInteger.valueOf(quota).multiply(phase.add(BigInteger.ONE)).divide(BigInteger.valueOf(20)).intValueExact();
  }
  static int cases,failures;
  static void check(int q,long s,long n) {
    int expected=oracle(q,s,n),actual=candidate(q,s,n);
    if(actual!=expected || actual<0 || actual>q)throw new AssertionError("q="+q+" start="+s+" now="+n+" actual="+actual+" expected="+expected);
    if(baseline(q,s,n)!=expected)failures++;
    cases++;
  }
  public static void main(String[] args) {
    long[] points={Long.MIN_VALUE,Long.MIN_VALUE+1,Long.MIN_VALUE+19,-100,-20,-19,-1,0,1,18,19,20,100,Long.MAX_VALUE-20,Long.MAX_VALUE-19,Long.MAX_VALUE-1,Long.MAX_VALUE};
    for(int q:new int[]{0,1,19,20,21,64,1024,8192,65536})for(long s:points)for(long n:points)check(q,s,n);
    Random random=new Random(20261026);
    for(int i=0;i<100000;i++)check(random.nextInt(65537),random.nextLong(),random.nextLong());
    // Every supported interval phase remains monotonic and reaches one quota.
    for(int q:new int[]{0,1,19,20,21,64,1024,8192,65536})for(long s:points) {
      int last=-1;
      if(s==Long.MIN_VALUE)continue;
      for(int phase=0;phase<20;phase++) {
        if(s>Long.MAX_VALUE-phase)break;
        int value=candidate(q,s,s+phase);check(q,s,s+phase);
        if(value<last)throw new AssertionError("nonmonotonic phase");last=value;
      }
    }
    if(baseline(1024,Long.MIN_VALUE+1,Long.MAX_VALUE)>=0 || candidate(1024,Long.MIN_VALUE+1,Long.MAX_VALUE)!=1024 || failures==0)throw new AssertionError("missing negative-quota reproduction");
    System.out.println("ALLOWANCE_PASS cases="+cases+" baselineMismatches="+failures+" reproduction="+baseline(1024,Long.MIN_VALUE+1,Long.MAX_VALUE)+" corrected="+candidate(1024,Long.MIN_VALUE+1,Long.MAX_VALUE));
  }
}
'''
(o/'AllowanceChecks.java').write_text(source)
j=a.java_home.resolve()/'bin'
subprocess.run([str(j/'javac'),'-g','-d',str(o),str(o/'AllowanceChecks.java')],check=True,timeout=60)
result=subprocess.run([str(j/'java'),'-Xmx128m','-cp',str(o),'AllowanceChecks'],capture_output=True,text=True,timeout=60)
(o/'result.log').write_text(result.stdout+result.stderr)
if result.returncode!=0 or not result.stdout.startswith('ALLOWANCE_PASS '):raise RuntimeError('Allowance test failed: '+str(result.returncode)+' '+result.stdout+result.stderr)
(o/'receipt.json').write_text(json.dumps({'pass':True,'output':result.stdout.strip(),'source_sha256':json.loads((r/'source-identity.json').read_text()),'scope':'Exact method extraction, BigInteger oracle; not Minecraft or full build','native_run':False},indent=2))
print(result.stdout,end='')
