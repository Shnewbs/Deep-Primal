import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterAccount.Parcel;
import java.math.BigInteger;
import java.util.Random;

public final class ExactDiffusionChecks {
  static long fast, fallback;
  static long oracle(long a,long b,long va,long vb,long cap) {
    if(a<=Long.MAX_VALUE/vb && b<=Long.MAX_VALUE/va) fast++;else fallback++;
    long desired=BigInteger.valueOf(a).multiply(BigInteger.valueOf(vb))
        .subtract(BigInteger.valueOf(b).multiply(BigInteger.valueOf(va)))
        .divide(BigInteger.valueOf(va+vb)).longValueExact();
    return desired>0?Math.min(desired,Math.min(cap,Math.min(a,WaterAccount.MAX*1024-b)))
        :-Math.min(-desired,Math.min(cap,Math.min(b,WaterAccount.MAX*1024-a)));
  }
  static void check(long a,long b,long va,long vb,long cap) {
    var x=new WaterAccount(WaterAccount.MAX,new Parcel(va,a,b));
    var y=new WaterAccount(WaterAccount.MAX,new Parcel(vb,b,a));
    long ds=oracle(a,b,va,vb,cap),dd=oracle(b,a,va,vb,cap);
    var got=x.diffuseWith(y,cap,cap);
    if(got.saltToOther()!=ds || got.sedimentToOther()!=dd ||
        !x.snapshot().equals(new Parcel(va,a-ds,b-dd)) ||
        !y.snapshot().equals(new Parcel(vb,b+ds,a+dd)))throw new AssertionError("oracle mismatch");
  }
  public static void main(String[] args) {
    long max=WaterAccount.MAX,mass=max*1024;
    // The two sides of each exact multiplication-overflow boundary.
    for(long v:new long[]{1,256,4096,8192,8193,65536,max}) {
      long edge=Math.min(mass,Long.MAX_VALUE/v);
      for(long a:new long[]{0,1,Math.max(0,edge-1),edge,Math.min(mass,edge+1),mass})
        for(long b:new long[]{0,1,mass})
          for(long cap:new long[]{0,1,Long.MAX_VALUE}) {
            check(a,b,v,v,cap);check(b,a,1,v,cap);check(a,b,v,1,cap);
          }
    }
    Random r=new Random(276201);
    for(int i=0;i<200000;i++) {
      boolean local=(i&1)==0;
      long va=1+r.nextLong(local?4096:max),vb=1+r.nextLong(local?4096:max);
      check(r.nextLong(local?4194304:mass+1),r.nextLong(local?4194304:mass+1),
          va,vb,r.nextLong(mass+1));
    }
    if(fast==0||fallback==0)throw new AssertionError("branch corpus");
    System.out.println("EXACT_DIFFUSION_PASS cases=201134 constituentFastCases="+fast+" constituentFallbackCases="+fallback+" arbitraryPrecisionOracle=true");
  }
}
