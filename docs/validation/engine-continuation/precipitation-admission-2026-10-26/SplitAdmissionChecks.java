package com.shnewbs.deepprimal.core;
import java.util.Random;
public final class SplitAdmissionChecks {
  public static void main(String[] args) {
    Random random = new Random(20261026);
    int rejected = 0, accepted = 0;
    for (int i = 0; i < 10000; i++) for (int side = 0; side < 2; side++)
      for (int constituent = 0; constituent < 2; constituent++) for (int exact = 0; exact < 2; exact++) {
        long a = 1 + random.nextInt(4096), b = 1 + random.nextInt(4096);
        long initial = WaterAccount.MAX * 1024 - (exact == 1 ? (side == 0 ? a : b) : 0);
        var from = new WaterAccount(a+b, new WaterAccount.Parcel(a+b, a+b, a+b));
        var saturated = new WaterAccount(a+b, new WaterAccount.Parcel(0,
            constituent == 0 ? initial : 0, constituent == 1 ? initial : 0));
        var empty = new WaterAccount(a+b, WaterAccount.Parcel.EMPTY);
        var first = side == 0 ? saturated : empty;
        var second = side == 0 ? empty : saturated;
        var fs = from.snapshot(); var as = first.snapshot(); var bs = second.snapshot();
        long moved = WaterAccount.distribute(from, first, second, a, b);
        if (exact == 0) {
          if (moved != 0 || !fs.equals(from.snapshot()) || !as.equals(first.snapshot())
              || !bs.equals(second.snapshot())) throw new AssertionError("rejection mutated accounts");
          rejected++;
        } else {
          var f = from.snapshot(); var x = first.snapshot(); var y = second.snapshot();
          if (moved != a+b || x.water()!=a || y.water()!=b
              || fs.water()+as.water()+bs.water()!=f.water()+x.water()+y.water()
              || fs.salt()+as.salt()+bs.salt()!=f.salt()+x.salt()+y.salt()
              || fs.sediment()+as.sediment()+bs.sediment()!=f.sediment()+x.sediment()+y.sediment())
            throw new AssertionError("accepted transfer lost quantities");
          accepted++;
        }
      }
    System.out.println("SPLIT_ADMISSION_PASS rejected="+rejected+" exactCapacity="+accepted);
  }
}
