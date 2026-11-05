package com.shnewbs.deepprimal.core;
public final class RegionalBudgetChecks {

  private static final class RegionWork {
    final double datum;
    long tick = Long.MIN_VALUE;
    int discharged;
    RegionWork(double datum) { this.datum = datum; }
  }
  static int emit(Aquifer aquifer, RegionWork regional, long tick) {
    if (aquifer.step(tick)) { regional.tick = tick; regional.discharged = 0; }
    if (!(regional.discharged < 32)) return 0;
    int accepted = Math.min(4, 32 - regional.discharged);
    long before = aquifer.ground.water();
    aquifer.ground.take(accepted * WaterAccount.SCALE);
    regional.discharged += (int)((before - aquifer.ground.water()) / WaterAccount.SCALE);
    return accepted;
  }
  public static void main(String[] args) {
    int violations=0, maximum=0, cases=0;
    // Enumerate cache reconstruction schedules around fixed-step boundaries.
    for (int period=1; period<=41; period++) for(int phase=0; phase<period; phase++) {
      var aquifer=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY,0,0),WaterAccount.Parcel.EMPTY,30,true,0);
      var regional=new RegionWork(100); int window=0, total=0; long last=0;
      for(long tick=1; tick<=200; tick++) {
        if (tick%period==phase) regional=new RegionWork(100);
        long previousStep=aquifer.lastTick();
        int amount=emit(aquifer,regional,tick);
        if (aquifer.lastTick()!=previousStep) { window=0; last=aquifer.lastTick(); }
        window+=amount; total+=amount; maximum=Math.max(maximum,window);
        if(window>32) violations++;
        if(aquifer.ground.water()+total*256L!=Aquifer.CAPACITY) throw new AssertionError("volume conservation");
        cases++;
      }
    }
    // Without cache loss every completed interval still delivers the full allowance.
    var a=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY,0,0),WaterAccount.Parcel.EMPTY,30,true,0);
    var region=new RegionWork(100);int delivered=0;
    for(long tick=20;tick<40;tick++)delivered+=emit(a,region,tick);
    if(delivered!=32)throw new AssertionError("ordinary interval underdelivery");
    // Long offline gaps authorize one step and one allowance, never catch-up.
    var offline=new RegionWork(100);int once=0;
    for(int i=0;i<20;i++)once+=emit(a,offline,1000000);
    if(once!=32)throw new AssertionError("offline catch-up");
    System.out.println("REGIONAL_BUDGET_RESULT cases="+cases+" violations="+violations+" maximum="+maximum+" ordinary="+delivered+" offline="+once);
    if(args.length>0 && violations!=0)throw new AssertionError("cache churn exceeded shared allowance");
  }
}
