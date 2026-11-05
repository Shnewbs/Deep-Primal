import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterAccount.Parcel;
import java.util.*;
public class DiffusionNetworkChecks {
  static long[] totals(WaterAccount[] a) {
    long[] t=new long[3];
    for(var x:a) { var p=x.snapshot(); t[0]+=p.water();t[1]+=p.salt();t[2]+=p.sediment(); }
    return t;
  }
  public static void main(String[] args) {
    var r=new Random(2761); var a=new WaterAccount[24];
    for(int i=0;i<a.length;i++) a[i]=new WaterAccount(100000,new Parcel(50000,i*7000,i*311));
    long[] expected=totals(a); int reconstructions=0;
    for(int step=0;step<200000;step++) {
      int i=r.nextInt(a.length),j=r.nextInt(a.length);
      if(step%4==0) WaterAccount.move(a[i],a[j],r.nextInt(1024));
      else if(step%4==1) a[i].evaporateInto(a[j],r.nextInt(256));
      else {
        long wi=a[i].water(),wj=a[j].water();
        var old=a[i].snapshot();
        var delta=a[i].diffuseWith(a[j],17,step%2==0?0:23);
        if(a[i].water()!=wi||a[j].water()!=wj) throw new AssertionError("volume changed");
        if(step%2==0 && (delta.sedimentToOther()!=0||a[i].snapshot().sediment()!=old.sediment()))
          throw new AssertionError("independent sediment limit");
      }
      if(step%127==0) { a[i]=new WaterAccount(a[i].capacity(),a[i].snapshot());reconstructions++; }
      if(!Arrays.equals(expected,totals(a)))throw new AssertionError("mass loss step="+step);
    }
    System.out.println("DIFFUSION_NETWORK_PASS steps=200000 accounts=24 reconstructions="+reconstructions+" exactTotals=true");
  }
}
