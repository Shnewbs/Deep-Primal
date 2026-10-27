import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterAccount.Parcel;
import java.math.BigInteger;
import java.util.Random;

public final class DiffusionChecks {
  static long cases;
  static void require(boolean b) { if (!b) throw new AssertionError("case " + cases); }
  static BigInteger excess(long a, long b, long va, long vb) {
    return BigInteger.valueOf(a).multiply(BigInteger.valueOf(vb))
        .subtract(BigInteger.valueOf(b).multiply(BigInteger.valueOf(va)));
  }
  static void check(long va, long vb, long sa, long sb, long da, long db, long limit) {
    cases++;
    var a = new WaterAccount(WaterAccount.MAX, new Parcel(va,sa,da));
    var b = new WaterAccount(WaterAccount.MAX, new Parcel(vb,sb,db));
    var diff = a.diffuseWith(b, limit, limit);
    var x = a.snapshot(); var y = b.snapshot();
    require(x.water()==va && y.water()==vb);
    require(x.salt()+y.salt()==sa+sb && x.sediment()+y.sediment()==da+db);
    require(diff.saltToOther()==sa-x.salt() && diff.sedimentToOther()==da-x.sediment());
    require(Math.abs(diff.saltToOther())<=limit && Math.abs(diff.sedimentToOther())<=limit);
    for (int i=0;i<2;i++) {
      long aa=i==0?sa:da, bb=i==0?sb:db, ax=i==0?x.salt():x.sediment(), by=i==0?y.salt():y.sediment();
      var old=excess(aa,bb,va,vb); var now=excess(ax,by,va,vb);
      require(now.abs().compareTo(old.abs())<=0);
      require(now.signum()==0 || now.signum()==old.signum());
    }
    if(va==0 || vb==0) require(x.equals(new Parcel(va,sa,da)) && y.equals(new Parcel(vb,sb,db)));
    // Reverse-order execution from the same input must produce the same accounts.
    var c=new WaterAccount(WaterAccount.MAX,new Parcel(va,sa,da));
    var d=new WaterAccount(WaterAccount.MAX,new Parcel(vb,sb,db));
    d.diffuseWith(c,limit,limit);
    require(c.snapshot().equals(x) && d.snapshot().equals(y));
  }
  public static void main(String[] args) {
    long max=WaterAccount.MAX, mass=max*1024;
    for(long va:new long[]{0,1,256,max})
      for(long vb:new long[]{0,1,256,max})
        for(long sa:new long[]{0,1,35,mass})
          for(long sb:new long[]{0,1,35,mass})
            for(long limit:new long[]{0,1,7,Long.MAX_VALUE})
              check(va,vb,sa,sb,sb,sa,limit);
    Random random=new Random(9471);
    for(int i=0;i<100000;i++) check(random.nextLong(max+1),random.nextLong(max+1),
        random.nextLong(mass+1),random.nextLong(mass+1),
        random.nextLong(mass+1),random.nextLong(mass+1),random.nextLong(mass+1));
    var a=new WaterAccount(max,new Parcel(256,8960,128));
    var b=new WaterAccount(max,new Parcel(256,0,0));
    a.diffuseWith(b,Long.MAX_VALUE,Long.MAX_VALUE);
    require(a.snapshot().equals(new Parcel(256,4480,64)));
    require(b.snapshot().equals(a.snapshot()));
    for(int i=0;i<10000;i++) {
      var before=a.snapshot(); b.diffuseWith(a,Long.MAX_VALUE,Long.MAX_VALUE);
      require(a.snapshot().equals(before) && b.snapshot().equals(before));
    }
    var before=a.snapshot();
    require(a.diffuseWith(a,10,10).equals(new WaterAccount.Diffusion(0,0)));
    try { a.diffuseWith(b,-1,0); throw new AssertionError(); } catch(IllegalArgumentException expected) {}
    require(a.snapshot().equals(before));
    System.out.println("DIFFUSION_PASS propertyCases="+cases+" equilibriumRepeats=10000 waterUnchanged=true orderSymmetric=true");
  }
}
