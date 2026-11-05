package com.shnewbs.deepprimal.core;
import java.math.BigInteger;
import java.util.Random;
public final class AquiferTickChecks {
  public static void main(String[] args) {
    long[] edge={Long.MIN_VALUE,Long.MIN_VALUE+19,-21,-20,-1,0,1,19,20,21,Long.MAX_VALUE-20,Long.MAX_VALUE};
    int count=0;
    for(long before:edge)for(long now:edge){check(before,now);count++;}
    Random random=new Random(7301);
    for(int i=0;i<50000;i++){check(random.nextLong(),random.nextLong());count++;}
    System.out.println("AQUIFER_TICK_PASS cases="+count);
  }
  static void check(long before,long now) {
    var water=new WaterAccount.Parcel(Aquifer.CAPACITY,71,29);
    var a=new Aquifer(water,WaterAccount.Parcel.EMPTY,0,false,before,0);
    boolean expected=BigInteger.valueOf(now).subtract(BigInteger.valueOf(before)).compareTo(BigInteger.valueOf(20))>=0;
    boolean actual=a.step(now);
    if(actual!=expected)throw new AssertionError("before="+before+" now="+now+" expected="+expected+" actual="+actual);
    if(!a.ground.snapshot().equals(water)||!a.snow.snapshot().equals(WaterAccount.Parcel.EMPTY))throw new AssertionError("quantity changed");
    if(a.wetSteps()!=(expected?1:0)||a.dynamicWetSteps()!=(expected?1:0))throw new AssertionError("catch-up rather than one step");
    if(a.lastTick()!=(expected?now:before))throw new AssertionError("saved tick");
    if(a.step(now))throw new AssertionError("duplicate tick replayed");
  }
}
