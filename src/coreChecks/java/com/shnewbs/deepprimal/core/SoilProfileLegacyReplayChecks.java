package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Frozen legacy soil material replay, without changing revision-2 generation. */
public final class SoilProfileLegacyReplayChecks {
  public static void main(String[] args) {
    Random random = new Random(20261102L);
    var geology = new Geology.Column(0,0,0,0,0,.5,0,0,0,0,0);
    int checks=0;
    for (int i=0;i<100_000;i++) {
      double rain=random.nextDouble(), temperature=random.nextDouble();
      double ground=70+random.nextDouble()*80, slope=random.nextDouble()*3;
      double sediment=random.nextDouble(), speed=random.nextDouble();
      double water=i%3==0?100:Double.NaN;
      var sample=new TerrainEngine.Sample(ground,water,rain,temperature,slope,sediment,0,0,speed,0);
      SoilProfile.Kind expected=temperature<.23?SoilProfile.Kind.FROZEN:Double.isFinite(water)
          ?(speed>.3?SoilProfile.Kind.GRAVEL:rain>.72?SoilProfile.Kind.CLAY:SoilProfile.Kind.LOAM)
          :ground<101||rain<.35?SoilProfile.Kind.SAND:temperature<.42?SoilProfile.Kind.PODZOL
          :rain>.8?SoilProfile.Kind.CLAY:SoilProfile.Kind.LOAM;
      int top=slope>1.6?0:expected==SoilProfile.Kind.GRAVEL?1:rain>.7?2:1;
      int sub=(int)Math.clamp(1+rain*3+sediment*2-slope*2,0,6);
      int weathered=slope>2.2?0:2;
      var actual=SoilProfile.at(sample,geology);
      if (!actual.equals(new SoilProfile(top,sub,weathered,expected)))
        throw new AssertionError("frozen legacy soil replay changed at sample " + i);
      checks++;
    }
    // The revised rain band belongs to a later versioned generation pass.
    // Do not accidentally change existing material revision 2 while repairing legacy.
    var noise = new TerrainNoise(20261102L);
    int[] sand = new int[5];
    double[] bands = {.329999,.34,.349999999,.36,.370001};
    for(int band=0;band<bands.length;band++) {
      for(int i=0;i<10_000;i++) {
        var sample=new TerrainEngine.Sample(105+i*.00123456789,Double.NaN,bands[band],.6,.2,0,0,0,0,0);
        if(SoilProfile.at(noise,i,0,sample,geology).kind()==SoilProfile.Kind.SAND) sand[band]++;
        checks++;
      }
    }
    if (sand[0]!=10_000 || sand[1]!=6256 || sand[2]!=4998 || sand[3]!=0 || sand[4]!=0)
      throw new AssertionError("saved revision-2 soil distribution changed: "+java.util.Arrays.toString(sand));
    checks++;
    System.out.println("SOIL_PROFILE_LEGACY_REPLAY_PASS checks="+checks+" frozenRev2Sand="
        +java.util.Arrays.toString(sand));
  }
  private SoilProfileLegacyReplayChecks() {}
}
